# Novigan

Programming exercise in the course of the practical lecture _Fundamentals of Programming_ (IN0002) at the Technical University of Munich.

The program efficiently calculates a route of minimum length between two points in an OpenStreetMaps XML extract using the Dijkstra algorithm.

![alt text](osm.jpg)

The **MapParser** class creates an instance of the MapGraph class from a file in OSM format. The class MapGraph represents the graph of streets and roads, on which later minimal distances are searched. The MapParser ignores unused nodes to save memory.

The **MapPoint** class calculates the distance between two points on the map from longitude and latitude.

The **MapGraph** class determines for each point on the map the node of the graph which is closest to the point. If two nodes have exactly the same distance from the point, the node with the smallest ID is selected. Furthermore, this class calculates all smallest distances from the node closest to the map point 'from' using the Dijkstra algorithm and returns an object of type RoutingResult which contains the distance between start and end nodes (along the path) and the corresponding path. If no path can be found, NULL is returned.

The **GPXWriter** class outputs routes in GPX format.

A BinomialHeap (package **heap**) is used to select shortest distance connections in the Dijkstra algorithm.
