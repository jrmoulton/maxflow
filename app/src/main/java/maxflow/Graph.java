
package maxflow;

import java.util.*;

import maxflow.GraphNode.EdgeInfo;

public class Graph {
    private final GraphNode[] vertices; // Adjacency list for graph.
    private final String name; // The file from which the graph was created.
    private int[][] resGraph;

    public Graph(String name, int vertexCount) {
        this.name = name;
        resGraph = new int[vertexCount][vertexCount];

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
        this.resGraph[destination][source] = 0;

        return true;
    }

    /**
     * Algorithm to find max-flow in a network
     */
    public int findMaxFlow(int source, int destination, boolean report) {
        // DONE:
        if (report) {
            System.out.println(String.format("\n-- Max Flow: %s --", this.name));
        }
        int totalFlow = 0;
        while (hasAugmentingPath(source, destination)) {
            var temp = destination;
            var availableFlow = Integer.MAX_VALUE;

            // Walk path backwards and find max flow for that path
            while (temp != source) {
                var parentEdges = this.vertices[this.vertices[temp].parent].successor;
                for (var next : parentEdges) {
                    if (next.to == temp) {
                        availableFlow = Math.min(availableFlow, next.capacity);
                    }
                }
                temp = this.vertices[temp].parent;
            }

            // Walk path backwards again and set changes in flow for each path and residual
            // path
            temp = destination;
            while (temp != source) {
                for (var next : this.vertices[this.vertices[temp].parent].successor) {
                    if (next.to == temp) {
                        next.capacity -= availableFlow;
                    }
                }
                this.resGraph[temp][this.vertices[temp].parent] += availableFlow;
                temp = this.vertices[temp].parent;
            }
            totalFlow += availableFlow;
            if (report) {
                printPath(source, destination);
            }
        }
        if (report) {
            System.out.println();
            printResPath(source, destination);
        }
        return totalFlow;
    }

    /**
     * Algorithm to find the min-cut edges in a network
     */
    public void findMinCut(int source) {
        // DONE:

        // Compute the final residual graph by getting a path until there isn't one
        boolean moreWork = hasAugmentingPath(0, this.vertices.length - 1);
        while (moreWork) {
            moreWork = hasAugmentingPath(0, this.vertices.length - 1);
        }

        var verticeSet = new HashSet<>();
        Queue<Integer> vertexQueue = new ArrayDeque<>(this.vertices.length / 2);
        verticeSet.add(source);
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            var trackVertex = vertexQueue.remove();

            // For every edge that has a path in the residual graph and was used for at
            // least some flow, add the target of that edge to a set and add it to the queue
            for (var edge : this.vertices[trackVertex].successor) {
                if (!verticeSet.contains(edge.to) && pathUsed(edge) && existsResCap(edge)) {
                    verticeSet.add(edge.to);
                    vertexQueue.add(edge.to);
                }
            }
        }
        System.out.println(String.format("\n-- Min Cut: %s --", this.name));
        for (var vertex : this.vertices) {
            for (var edge : vertex.successor) {
                if (verticeSet.contains(edge.from) && !verticeSet.contains(edge.to)) {
                    System.out.println(String.format("Min Cut Edge: (%d, %d)", edge.from, edge.to));
                }
            }
        }
    }

    /**
     * Algorithm to find an augmenting path in a network
     */
    private Boolean hasAugmentingPath(int source, int destination) {
        // DONE:
        for (var vertex : this.vertices) {
            // Reset all parents and visited
            vertex.parent = -1;
            vertex.visited = false;
        }

        Queue<Integer> vertexQueue = new ArrayDeque<>(this.vertices.length / 2);
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty() && !parentSet(destination)) {
            var currIdx = vertexQueue.remove();
            for (var childEdge : this.vertices[currIdx].successor) {
                if (existsResCap(childEdge) && !prevUsed(childEdge.to)) {
                    // Set the parent of the next vertex to the current vertex
                    this.vertices[childEdge.to].parent = currIdx;
                    this.vertices[childEdge.to].visited = true;
                    // Add the next next vertex to the queue
                    vertexQueue.add(childEdge.to);
                }
            }
            for (int target = 0; target < this.resGraph[currIdx].length; target++) {
                int resValue = this.resGraph[currIdx][target];
                if (existsResCap(resValue) && !prevUsed(target)) {
                    this.vertices[target].parent = currIdx;
                    this.vertices[target].visited = true;
                    vertexQueue.add(target);
                }
            }

        }
        if (parentSet(destination)) {
            return true;
        } else {
            return false;
        }
    }

    private void printPath(int source, int destination) {
        int flow = Integer.MAX_VALUE;
        var track = destination;
        Deque<Integer> values = new ArrayDeque<>();
        while (track != source) {
            values.addFirst(track);
            flow = Math.min(flow, this.resGraph[track][this.vertices[track].parent]);
            track = this.vertices[track].parent;
        }
        values.addFirst(source);
        System.out.print(String.format("Flow %2d: ", flow));
        for (var value : values) {
            System.out.print(value + " ");
        }
        System.out.println();
    }

    private void printResPath(int source, int destination) {
        var currIdx = 0;
        while (currIdx != destination + 1) {
            for (var childEdge : this.vertices[currIdx].successor) {
                if (this.resGraph[childEdge.to][childEdge.from] > 0) {
                    System.out.println(String.format("Edge(%d, %d) transports %d items", childEdge.from, childEdge.to,
                            this.resGraph[childEdge.to][childEdge.from]));
                }
            }
            currIdx += 1;
        }
    }

    private boolean existsResCap(EdgeInfo childEdge) {
        return childEdge.capacity > 0;
    }

    private boolean existsResCap(int resEdge) {
        return resEdge > 0;
    }

    private boolean pathUsed(EdgeInfo edge) {
        return this.resGraph[edge.to][edge.from] > 0;
    }

    private boolean prevUsed(Integer idx) {
        return this.vertices[idx].visited;
    }

    private boolean parentSet(int destination) {
        return vertices[destination].parent != -1;
    }

    private boolean outOfBounds(int vertex) {
        return vertex < 0 || vertex >= vertices.length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("The Graph " + name + " \n");
        for (var vertex : vertices) {
            sb.append((vertex.toString()));
        }
        return sb.toString();
    }
}
