package game.gamehelper.DominoMT;

/**
 * Created by Jacob on 2/11/2015.
 * Vertex for a DominoGraph; contains edge information, too.
 */
public class DominoVertex {
    private final int MAX_EDGE;
    private boolean edgeList[];
    private int numEdges;

    DominoVertex(int highestDouble) {
        MAX_EDGE = highestDouble;
        edgeList = new boolean[MAX_EDGE + 1];
        numEdges = 0;
    }

    /**
     * Adds an edge to this vertex
     * @param edgeNum The vertex to add an edge with.
     */
    public void addEdge(int edgeNum) {
        if (!edgeList[edgeNum]) {
            edgeList[edgeNum] = true;
            numEdges++;
        }
    }

    /**
     * Toggles an edge with this vertex
     * @param edgeNum The vertex to toggle an edge with.
     */
    public void toggleEdge(int edgeNum) {
        if (edgeList[edgeNum])
            numEdges--;
        else
            numEdges++;
        edgeList[edgeNum] = !edgeList[edgeNum];
    }

    /**
     * Removes an edge from this vertex
     * @param edgeNum The vertex to remove and edge from.
     */
    public void removeEdge(int edgeNum) {
        if (edgeList[edgeNum]) {
            edgeList[edgeNum] = false;
            numEdges--;
        }
    }

    /**
     * Tests whether this vertex has an edge with another vertex.
     * @param edgeNum The vertex to test against.
     * @return True if has edge, false otherwise.
     */
    public boolean hasEdge(int edgeNum) {
        return edgeList[edgeNum];
    }

    /**
     * Dumps a copy of the edges in this vertex.
     * @return Returns a copy of the edges in this vertex.
     */
    public boolean[] dumpEdges() {
        return edgeList.clone();
    }

    //returns the edge num for this vertex.
    public int getEdgeNum() {
        return numEdges;
    }
}
