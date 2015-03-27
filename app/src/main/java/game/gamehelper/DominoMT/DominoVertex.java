package game.gamehelper.DominoMT;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jacob on 2/11/2015.
 * Vertex for a DominoGraph; contains edge information, too.
 */
public class DominoVertex implements Parcelable {
    private final int MAX_EDGE;
    private boolean edgeList[];
    private int numEdges;

    DominoVertex(int highestDouble) {
        MAX_EDGE = highestDouble;
        edgeList = new boolean[MAX_EDGE + 1];
        numEdges = 0;
    }

    DominoVertex(Parcel p){
        MAX_EDGE = p.readInt();
        numEdges = p.readInt();

        edgeList = new boolean[MAX_EDGE+1];
        byte[] tempList = new byte[MAX_EDGE + 1];
        p.readByteArray(tempList);

        for(int i = 0 ; i < MAX_EDGE+1 ; i++)
            edgeList[i] = (boolean) (tempList[i] == 1);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(MAX_EDGE);
        dest.writeInt(numEdges);

        byte[] tempList = new byte[MAX_EDGE + 1];
        for(int i = 0 ; i < MAX_EDGE + 1 ; i++ ){
            tempList[i] = (byte) (edgeList[i] ? 1 : 0);
        }
        dest.writeByteArray(tempList);
    }

    public static Parcelable.Creator CREATOR = new Parcelable.Creator(){
        @Override
        public DominoVertex createFromParcel(Parcel source) {
            return new DominoVertex(source);
        }

        @Override
        public DominoVertex[] newArray(int size) {
            return new DominoVertex[size];
        }
    };
}
