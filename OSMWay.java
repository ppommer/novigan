import java.util.Optional;

/**
 * This class represents an OSM way.
 */
public class OSMWay {
  /**
   * way id
   */
  private long id;
  
  public long getId() {
    return id;
  }
  
  /**
   * The nodes containing the way
   */
  private Long[] nodes;
  
  public Long[] getNodes() {
    return nodes;
  }
  
  /**
   * Defines if the way is a one-way street
   */
  private boolean oneWay;
  
  public boolean isOneWay() {
    return oneWay;
  }
  
  /**
   * The name of the way, if available
   */
  private Optional<String> name;
  
  public Optional<String> getName() {
    return name;
  }
  
  public OSMWay(long id, Long[] nodes, boolean oneWay, Optional<String> name) {
    this.id = id;
    this.nodes = nodes;
    this.oneWay = oneWay;
    this.name = name;
  }
  
  @Override
  public String toString() {
    StringBuilder sB = new StringBuilder();
    sB.append("Way {id=").append(id).append(", oneWay=").append(oneWay).append(", name=").append(name).append("}").append('\n');
    sB.append('\t');
    for (int i = 0; i < nodes.length; i++) {
      if(i > 0)
        sB.append("->");
      sB.append(nodes[i]);
    }
    sB.append('\n');
    return sB.toString();
  }
  
  @Override public boolean equals (Object obj) {
    OSMWay objCasted = (OSMWay)obj;
    if(id != objCasted.id)
      return false;
    if(nodes.length != objCasted.nodes.length)
      return false;
    for (int i = 0; i < nodes.length; i++)
      if(!nodes[i].equals(objCasted.nodes[i]))
        return false;
    if(oneWay != objCasted.oneWay)
      return false;
    if(!name.equals(objCasted.name))
      return false;
    return true;
  }
}
