import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class WayBuilder {
  private BiConsumer<String, String> addTag;

  public BiConsumer<String, String> getAddTag() {
    return addTag;
  }

  private Consumer<Long> addWaypoint;

  public Consumer<Long> getAddWaypoint() {
    return addWaypoint;
  }

  private Function<Void, Optional<OSMWay>> finish;

  public Function<Void, Optional<OSMWay>> getFinish() {
    return finish;
  }

  public WayBuilder(BiConsumer<String, String> addTag, Consumer<Long> addWaypoint,
      Function<Void, Optional<OSMWay>> finish) {
    this.addTag = addTag;
    this.addWaypoint = addWaypoint;
    this.finish = finish;
  }
}

class WayParserHandler extends DefaultHandler {
  private MapGraph mapGraph;

  private Function<Long, WayBuilder> beginWay;
  private Optional<WayBuilder> wayBuilder = Optional.empty();

  public WayParserHandler(MapGraph mapGraph, Function<Long, WayBuilder> beginWay) {
    this.mapGraph = mapGraph;
    this.beginWay = beginWay;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    switch (qName) {
      case "node": {
        break;
      }
      case "way": {
        if (wayBuilder.isPresent())
          throw new RuntimeException("Invalid input");

        long id = Long.parseLong(attributes.getValue("id"));
        wayBuilder = Optional.of(beginWay.apply(id));
        break;
      }
      case "tag": {
        if (wayBuilder.isPresent())
          wayBuilder.get().getAddTag().accept(attributes.getValue("k"), attributes.getValue("v"));
      }
      case "nd": {
        if (wayBuilder.isPresent()) {
          String ref = attributes.getValue("ref");
          if (ref != null) {
            long waypoint = Long.parseLong(attributes.getValue("ref"));
            wayBuilder.get().getAddWaypoint().accept(waypoint);
          }
        }
      }
      default:
        break;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equals("way") && wayBuilder.isPresent()) {
      Optional<OSMWay> way = wayBuilder.get().getFinish().apply(null);
      if (way.isPresent())
        mapGraph.addWay(way.get());
      wayBuilder = Optional.empty();
    }
  }
}


class NodeParserHandler extends DefaultHandler {
  private MapGraph mapGraph;

  public NodeParserHandler(MapGraph mapGraph) {
    this.mapGraph = mapGraph;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    switch (qName) {
      case "node": {
        long id = Long.parseLong(attributes.getValue("id"));
        double lat = Double.parseDouble(attributes.getValue("lat"));
        double lon = Double.parseDouble(attributes.getValue("lon"));
        OSMNode osmNode = new OSMNode(id, lat, lon);
        mapGraph.addNode(osmNode);
        break;
      }
    }
  }
}


/**
 * This class allows to create a MapGraph object from a file in
 * OSM format by using an XML parser.
 */
public class MapParser {
  public static MapGraph parseFile(String fileName)
      throws SAXException, IOException, ParserConfigurationException {
    File inputFile = new File(fileName);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();

    Function<Long, WayBuilder> beginWay = (id) -> {
      ArrayList<Long> waypointsList = new ArrayList<>();
      Mutable<Optional<String>> name = new Mutable<>(Optional.empty());
      Mutable<Boolean> validWay = new Mutable<>(false);
      Mutable<Boolean> isOneWay = new Mutable<>(false);
      return new WayBuilder((k, v) -> {
        switch (k) {
          case "name":
            name.set(Optional.of(v));
            break;
          case "oneway":
            isOneWay.set(v.equals("yes"));
            break;
          case "highway":
            switch (v) {
              // case "footway":
              // validWay.set(false);
              // break;
              // case "bridleway":
              // validWay.set(false);
              // break;
              // case "steps":
              // validWay.set(false);
              // break;
              // case "path":
              // validWay.set(false);
              // break;
              // case "cycleway":
              // validWay.set(false);
              // break;
              case "proposed":
                validWay.set(false);
                break;
              case "construction":
                validWay.set(false);
                break;
              default:
                validWay.set(true);
                break;
            }
            break;
        }
      }, waypointId -> {
        waypointsList.add(waypointId);
      }, __ -> {
        return validWay.get()
            ? Optional
                .of(new OSMWay(id, waypointsList.toArray(new Long[0]), isOneWay.get(), name.get()))
            : Optional.empty();
      });
    };

    MapGraph mapGraph = new MapGraph();

    WayParserHandler userhandler = new WayParserHandler(mapGraph, beginWay);
    saxParser.parse(inputFile, userhandler);

    System.out.println("Finished reading ways, reading nodes...");

    NodeParserHandler nodeHandler = new NodeParserHandler(mapGraph);
    saxParser.parse(inputFile, nodeHandler);

    return mapGraph;
  }
}
