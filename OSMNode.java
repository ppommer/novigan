/**
 * This class implements an OSM node.
 */
public class OSMNode implements Comparable<OSMNode> {
  /**
   * The node ID.
   */
  private long id;
  
  public long getId() {
    return id;
  }
  
  /**
   * The coordinates of the map point.
   */
  private MapPoint location;
  
  public MapPoint getLocation () {
    return location;
  }
  
  public OSMNode(long id, double lat, double lon) {
    this.id = id;
    this.location = new MapPoint(lat, lon);
  }
  
  @Override
  public String toString() {
    return "Node {id = " + id + ", " + location + "}";
  }

  @Override
  public int compareTo(OSMNode o) {
    return ((Long)id).compareTo(o.getId());
  }
}
