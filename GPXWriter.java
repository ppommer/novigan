import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class GPXWriter {
  private PrintWriter writer;

  public GPXWriter(String fileName) throws FileNotFoundException {
    writer = new PrintWriter(new File(fileName));
  }

  private void writeLine(String line) {
    writer.println(line);
  }

  public void close() {
    writer.close();
  }

  public void writeGPX(RoutingResult rr) {
    writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
    writeLine("<gpx version=\"1.1\" creator=\"Nogivan\">");

    OSMNode[] path = rr.getPath();
    for (OSMNode next : path)
      writeLine("  <wpt lat=\"" + next.getLocation().getLat() + "\" lon=\""
          + next.getLocation().getLon() + "\"></wpt>");

    writeLine("</gpx>");
  }
}
