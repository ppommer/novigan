/**
 * This class implements a map point. A map point has a
 * position in the form of a longitude and latitude.
 */
public class MapPoint {
  /**
   * Latitude
   */
  private double lat;
  
  public double getLat () {
    return lat;
  }
  
  /**
   * Longitude
   */
  private double lon;
  
  public double getLon () {
    return lon;
  }
  
  public MapPoint (double lat, double lon) {
    this.lat = lat;
    this.lon = lon;
  }
  
  /**
   * This method calculated the distance from one map point
   * to another
   * 
   * @param other map point
   *
   * @return distance in meters
   */
  public int distance(MapPoint other) {
    double R = 6371e3; // metres
    double φ1 = Math.toRadians(lat);
    double φ2 = Math.toRadians(other.lat);
    double Δφ = Math.toRadians(other.lat - lat);
    double Δλ = Math.toRadians(other.lon - lon);

    double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ/2) * Math.sin(Δλ/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    double d = R * c;
    
    if(d < 0)
      throw new RuntimeException("Invalid distance");
    
    return (int)d;
  }
  
  @Override public String toString () {
    return  "lat = " + lat + ", lon = " + lon;
  }

}
