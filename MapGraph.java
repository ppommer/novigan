import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import heap.BinomialHeap;

/**
 * This class represents the graphs of
 * OpenStreetMap streets and pathways.
 */
public class MapGraph {
  private Map<Long, OSMNode> nodes;
  private Map<Long, Set<MapEdge>> edges;

  public MapGraph() {
    this.nodes = new TreeMap<>();
    this.edges = new HashMap<>();
  }

  /**
   * Evaluates if there is an edge between two nodes.
   * 
   * @param from start node
   * @param to target node
   *
   * @return 'true' if there is an edge, 'false' if not
   */
  boolean hasEdge(OSMNode from, OSMNode to) {
    Set<MapEdge> fromEdges = edges.get(from.getId());
    if (fromEdges == null)
      return false;
    else {
      for (MapEdge edge : fromEdges)
        if (edge.getTo() == to.getId())
          return true;
      return false;
    }
  }

  /**
   * This method adds a node to the graph. Only nodes
   * that already have an edge are included.
   * 
   * @param node to be added
   */
  public void addNode(OSMNode node) {
    if (edges.containsKey(node.getId()))
      this.nodes.put(node.getId(), node);
  }

  /**
   * This method adds a pathway to the graph.
   * 
   * @param way to be included
   */
  public void addWay(OSMWay way) {
    Long[] nodesWay = way.getNodes();
    BiConsumer<Integer, Integer> add = (from, to) -> {

      Function<Integer, Set<MapEdge>> getEdges = nodeId -> {
        Set<MapEdge> edgesNode = edges.get(nodesWay[nodeId]);
        if (edgesNode == null) {
          edgesNode = new TreeSet<>();
          edges.put(nodesWay[nodeId], edgesNode);
        }
        return edgesNode;
      };
      getEdges.apply(to);

      MapEdge edge = new MapEdge(nodesWay[to], way);
      getEdges.apply(from).add(edge);
    };
    for (int i = 0; i < nodesWay.length - 1; i++) {
      add.accept(i, i + 1);
      if (!way.isOneWay())
        add.accept(i + 1, i);
    }
  }

  /**
   * This method finds the nearest OpenStreetMap node
   * to a given map point. If there are several nodes
   * with the same smallest distance, the node with the
   * smallest id is returned.
   * 
   * @param p map point
   *
   * @return OpenStreetMap node
   */
  public OSMNode closest(MapPoint p) {
    Optional<OSMNode> nodeMin = Optional.empty();
    int distMin = Integer.MAX_VALUE;
    for (OSMNode node : nodes.values()) {
      int distNode = node.getLocation().distance(p);
      if (distNode < distMin) {
        distMin = distNode;
        nodeMin = Optional.of(node);
      } else if (distNode == distMin && node.getId() < nodeMin.get().getId())
        nodeMin = Optional.of(node);
    }
    return nodeMin.get();
  }

  public RoutingResult route(MapPoint from, MapPoint to) {
    OSMNode fromNode = closest(from);
    OSMNode toNode = closest(to);
    return sssp_dijkstra(fromNode, toNode);
  }

  /**
   * This method searches for the shortest route through the OpenStreetMap
   * street/pathway network for two map points.
   * 
   * @param fromNode start map point
   * @param toNode target map point
   * 
   * @return a possible route to the destination and its length; the length
   * of the route refers only to the length in the graph, the distance from
   * 'from' to the start node or 'to' to the end node is neglected.
   */
  private RoutingResult sssp_dijkstra(OSMNode fromNode, OSMNode toNode) {
    /*
     * This class represents an element of the priority queue for the Dijkstra
     * algorithm. It contains the costs and the corresponding node. The costs are
     * needed for sorting the priority queue.
     */
    class DijkstraState implements Comparable<DijkstraState> {
      private int cost;

      private Long node;

      public Long getNode() {
        return node;
      }

      public DijkstraState(int cost, Long node) {
        this.cost = cost;
        this.node = node;
      }

      @Override
      public int compareTo(DijkstraState o) {
        int costComp = ((Integer) cost).compareTo(o.cost);
        if (costComp != 0)
          return costComp;
        return node.compareTo(o.node);
      }

      @Override
      public boolean equals(Object obj) {
        DijkstraState otherCasted = (DijkstraState) obj;
        return otherCasted.node.equals(node) && otherCasted.cost == cost;
      }
    }

    /*
     * This variable is used to speed up the algorithm. If there is an estimate
     * for the maximum possible distance to the target node, all paths that are
     * known to be longer are ignored.
     */
    Optional<Integer> distMax = Optional.empty();

    HashMap<Long, Integer> dist = new HashMap<Long, Integer>();
    HashMap<Long, Long> prev = new HashMap<Long, Long>();

    // PriorityQueue<DijkstraState> heapJava = new PriorityQueue<>();
    // heapJava.add(new DijkstraState(0, new Node(0, 0)));

    /*
     * A handle is needed to lower the priority of an element. Handles are
     * stored in this variable accordingly.
     */
    HashMap<Long, Object> handles = new HashMap<>();

    Mutable<BinomialHeap<DijkstraState>> heap = new Mutable<>(null);

    // PriorityQueue<DijkstraState> heap = new PriorityQueue<>();
    // handles.put(fromNode.getId(), Optional.of(heap.add(new DijkstraState(0,
    // fromNode.getId()))));

    Consumer<Void> init = __ -> {
      dist.clear();
      dist.put(fromNode.getId(), 0);
      heap.set(new BinomialHeap<>());
      handles.clear();
      handles.put(fromNode.getId(), Optional.of(heap.get().insert(new DijkstraState(0, fromNode.getId()))));
    };

    init.accept(null);

    outer: while (heap.get().getSize() > 0) {
      DijkstraState minState = heap.get().poll();
      handles.remove(minState.getNode());
      // DijkstraState minJava = heapJava.poll();

      OSMNode minOSMNode = nodes.get(minState.getNode());

      if (edges.containsKey(minState.getNode()))
        for (MapEdge child : edges.get(minState.getNode())) {
          OSMNode childOSMNode = nodes.get(child.getTo());

          int distNew = dist.get(minState.getNode()) + minOSMNode.getLocation().distance(childOSMNode.getLocation());

          if (distNew < dist.getOrDefault(child.getTo(), Integer.MAX_VALUE)) {
            // heapJava.remove(new DijkstraState(dist[child.getIndex()],
            // child));
            // heapJava.add(new DijkstraState(distNew, child));

            int distAir = childOSMNode.getLocation().distance(toNode.getLocation());
            /*
             * The minimum distance to the destination is calculated. The path to the
             * current successor node plus the distance to the successor node are used
             * as a lower limit for the distance.
             *
             */
            int distToDestMin = distNew + distAir;

            if (distMax.isPresent()) {
              /*
               * The basic idea for optimization is not to follow paths if they are longer
               * than any known path.
               */

              /*
               * Two approaches are distinguished; the following code uses the lower limit
               * to ignore nodes that are not suitable for the shortest path.
               */
              if (distToDestMin <= distMax.get()) {
                if (handles.containsKey(child.getTo())) {
                  Object handle = handles.get(child.getTo());
                  heap.get().replaceWithSmallerElement(handle, new DijkstraState(distNew, child.getTo()));
                  // heap.remove(new DijkstraState(dist.get(child.getTo()),
                  // child.getTo()));
                  // heap.add(new DijkstraState(distNew, child.getTo()));
                } else
                  handles.put(child.getTo(), heap.get().insert(new DijkstraState(distNew, child.getTo())));
              }

              /*
               * The second approach optimizes more aggressively by adding to the linear distance
               * a proportion that is expected to undercut a path in the graph. This of course leads
               * to the fact that the minimum path might not be found if we are wrong.
               */
              // if (handles.containsKey(child.getTo())) {
              // int distToDestMinExpected = distNew + distAir + (1 * distAir) /
              // 6;
              // if (distToDestMinExpected <= distMax.get()) {
              // Object handle = handles.get(child.getTo());
              // heap.replaceWithSmallerElement(handle, new DijkstraState(distNew,
              // child.getTo()));
              // }
              // } else
              // handles.put(child.getTo(), heap.insert(new
              // DijkstraState(distNew, child.getTo())));

            } else if (!handles.containsKey(child.getTo()))
              /*
               * In this branch there is no estimation for the minimum distance yet.
               * Therefore, the algorithm of Dijkstra is not used, but always the node
               * is selected that is closest to the target in terms of linear distance.
               * This is to ensure that an estimate of the distance to the target node
               * can be obtained as quickly as possible.
               */
              handles.put(child.getTo(), heap.get().insert(new DijkstraState(distToDestMin, child.getTo())));

            dist.put(child.getTo(), distNew);
            prev.put(child.getTo(), minOSMNode.getId());
            if (child.getTo() == toNode.getId()) {
              /*
               * If a target node is discovered, the distance estimation if updated.
               */
              if (!distMax.isPresent()) {
                /*
                 * When the target node is discovered the first time, the algorithm is restarted.
                 * The reason is that Dijkstra has not yet been executed correctly and possibly
                 * incorrect distances have been saved.
                 */
                init.accept(null);
                dist.put(child.getTo(), distNew);

                continue outer;
              }
              distMax = Optional.of(distNew);
            }
          }
        }
    }

    Integer distTo = dist.get(toNode.getId());
    if (distTo == null)
      return null;

    /*
     * The path is assembled by tracing the prev references backwards.
     */
    ArrayList<OSMNode> path = new ArrayList<OSMNode>();
    path.add(toNode);
    OSMNode next = toNode;
    while (!next.equals(fromNode)) {
      next = nodes.get(prev.get(next.getId()));
      path.add(next);
    }
    OSMNode[] pathArray = new OSMNode[path.size()];
    for (int i = 0; i < pathArray.length; i++)
      pathArray[i] = path.get(path.size() - i - 1);

    return new RoutingResult(pathArray, distTo);
  }
}
