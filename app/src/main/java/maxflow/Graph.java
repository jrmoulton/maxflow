
package maxflow;

import java.util.*;
import java.util.stream.Stream;

import maxflow.GraphNode.EdgeInfo;

public class Graph {
    private final GraphNode[] vertices; // Adjacency list for graph.
    private final String name; // The file from which the graph was created.
    private ResEdgeInfo[][] resGraph;

    public Graph(String name, int vertexCount) {
        this.name = name;
        resGraph = new ResEdgeInfo[vertexCount][vertexCount];

        vertices = new GraphNode[vertexCount];
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            this.vertices[vertex] = new GraphNode(vertex);
        }
    }

    public boolean addEdge(int source, int destination, int capacity) {
        // A little bit of validation
        if (outOfBounds(source) || outOfBounds(destination))
            return false;

        // This adds the actual requested edge, along with its capacity
        this.vertices[source].addEdge(source, destination, capacity);
        this.resGraph[destination][source] = new ResEdgeInfo(destination, source, 0);

        return true;
    }

    private boolean outOfBounds(int vertex) {
        return vertex < 0 || vertex >= vertices.length;
    }

    /**
     * Algorithm to find max-flow in a network
     */
    public int findMaxFlow(int source, int destination, boolean report) {
        // TODO:
        int totalFlow = 0;
        while (hasAugmentingPath(source, destination)) {
            var temp = destination;
            if (report) {
                printPath(source, destination);
            }
        }
        return 0;
    }

    private void printPath(int source, int destination) {
        int flow = Integer.MAX_VALUE;
        var track = destination;
        Deque<Integer> values = new ArrayDeque<>();
        while (track != source) {
            values.addFirst(track);
            flow = Math.min(flow, this.resGraph[track][this.vertices[track].parent].flow);
            track = this.vertices[track].parent;
        }
        values.addFirst(source);
        System.out.print("Flow " + flow + ": ");
        System.out.println(values);
    }

    /**
     * Algorithm to find an augmenting path in a network
     */
    private Boolean hasAugmentingPath(int source, int destination) {
        // DONE:
        // Is there a path from verteces[s] to verteces[t]
        // Need to set up a BFS with a queue

        for (var vertex : this.vertices) {
            // Reset all parents
            vertex.parent = -1;
            vertex.visited = false;
        }

        Queue<Integer> vertexQueue = new ArrayDeque<>(this.vertices.length / 2);
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty() && !parentSet(destination)) {
            var currIdx = vertexQueue.remove();
            if (!prevUsed(currIdx)) {
                // Add the currIdx to the history
                this.vertices[currIdx].visited = true;
                for (var childEdge : this.vertices[currIdx].successor) {
                    if (!prevUsed(childEdge.to) && existsResCap(currIdx, childEdge)) {
                        // Set the parent of the next vertex to the current vertex
                        this.vertices[childEdge.to].parent = currIdx;
                        // Set the resgraph to be the min of the parent flow and the edge's capacity
                        if (currIdx == source) {
                            this.resGraph[childEdge.to][currIdx].flow = childEdge.capacity;
                        } else {
                            int parentFlow = this.resGraph[currIdx][this.vertices[currIdx].parent].flow;
                            this.resGraph[childEdge.to][currIdx].flow = Math.min(childEdge.capacity,
                                    parentFlow);
                        }
                        // Add the next next vertex to the queue
                        vertexQueue.add(childEdge.to);
                    }
                }
                for (int target = 0; target < this.resGraph[currIdx].length; target++) {
                    // Curr index is the source. i is the destination
                    var resEdge = this.resGraph[currIdx][target];
                    if (resEdge != null && !prevUsed(target) && resEdge.flow > 0) {

                        // Add the next next vertex to the queue
                        vertexQueue.add(target);
                        this.vertices[resEdge.from].parent = currIdx;
                        var parentResEdge = this.resGraph[currIdx][this.vertices[currIdx].parent];
                        int parentFlow = parentResEdge.flow;
                        resEdge.flow = Math.min(resEdge.flow, parentFlow);
                    }

                }
            }

        }
        if (parentSet(destination)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean existsResCap(Integer currIdx, EdgeInfo childEdge) {
        return this.resGraph[childEdge.to][currIdx].flow < childEdge.capacity;
    }

    private boolean prevUsed(Integer idx) {
        return this.vertices[idx].visited;
    }

    private boolean parentSet(int destination) {
        return vertices[destination].parent != -1;
    }

    /**
     * Algorithm to find the min-cut edges in a network
     */
    public void findMinCut(int s) {
        // TODO:
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("The Graph " + name + " \n");
        for (var vertex : vertices) {
            sb.append((vertex.toString()));
        }
        return sb.toString();
    }

    public class Tuple<E, V> {
        public E first;
        public V second;

        public Tuple(E first, V second) {
            this.first = first;
            this.second = second;
        }
    }

    public class ResEdgeInfo {
        int from; // source of edge
        int to; // destination of edge
        int flow; // capacity of edge

        public ResEdgeInfo(int from, int to, int flow) {
            this.from = from;
            this.to = to;
            this.flow = flow;

        }

        public String toString() {
            return "Backwards Edge " + from + "->" + to + " (" + flow + ") ";
        }
    }
}
