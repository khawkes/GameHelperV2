/*
 * COP4331C - Class Project - The Game Helper App
 * Spring 2015
 *
 * Project authors:
 *   Mark Andrews
 *   Jacob Cassagnol
 *   Kurt Hawkes
 *   Tim McCarthy
 *   Andrew McKenzie
 *   Amber Stewart
 */

package game.gamehelper.DominoMT;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Vertex for a DominoGraph; contains edge information, too.
 *
 * Created by Jacob on 2/11/2015.
 */
public class DominoVertex implements Parcelable
{
    private final int MAX_EDGE;
    private boolean edgeList[];
    private int numEdges;

    /**
     * Construct a new domino vertex with the provided highest double value.
     *
     * @param highestDouble highest double for the vertex
     */
    DominoVertex(int highestDouble)
    {
        MAX_EDGE = highestDouble;
        edgeList = new boolean[MAX_EDGE + 1];
        numEdges = 0;
    }

    /**
     * Construct a domino vertex from the provided parcel.
     *
     * @param p the parcel to inflate back to a vertex
     */
    DominoVertex(Parcel p)
    {
        MAX_EDGE = p.readInt();
        numEdges = p.readInt();
        edgeList = new boolean[MAX_EDGE + 1];
        p.readBooleanArray(edgeList);
    }

    /**
     * Adds an edge to this vertex
     *
     * @param edgeNum The vertex to add an edge with.
     */
    public void addEdge(int edgeNum)
    {
        if (!edgeList[edgeNum])
        {
            edgeList[edgeNum] = true;
            numEdges++;
        }
    }

    /**
     * Toggles an edge with this vertex
     *
     * @param edgeNum The vertex to toggle an edge with.
     */
    public void toggleEdge(int edgeNum)
    {
        if (edgeList[edgeNum])
            numEdges--;
        else
            numEdges++;
        edgeList[edgeNum] = !edgeList[edgeNum];
    }

    /**
     * Removes an edge from this vertex
     *
     * @param edgeNum The vertex to remove and edge from.
     */
    public void removeEdge(int edgeNum)
    {
        if (edgeList[edgeNum])
        {
            edgeList[edgeNum] = false;
            numEdges--;
        }
    }

    /**
     * Tests whether this vertex has an edge with another vertex.
     *
     * @param edgeNum the vertex to test against.
     * @return true if has edge, false otherwise.
     */
    public boolean hasEdge(int edgeNum)
    {
        return edgeList[edgeNum];
    }

    /**
     * Dumps a copy of the edges in this vertex.
     *
     * @return a copy of the edges in this vertex.
     */
    public boolean[] dumpEdges()
    {
        return edgeList.clone();
    }

    /**
     * Return number of edges for the vertex.
     *
     * @return number of edges for the vertex.
     */
    public int getEdgeNum()
    {
        return numEdges;
    }

    /**
     * Required for Parcelable interface.
     * Not used.
     *
     * @return zero
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Save this domino graph instance to a Parcel.
     *
     * @param dest the parcel to write the domino graph to
     * @param flags additional flags on how to write the parcel (not used)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(MAX_EDGE);
        dest.writeInt(numEdges);
        dest.writeBooleanArray(edgeList);
    }

    /**
     * Parcel CREATOR for the Domino class.
     *
     * @see android.os.Parcelable.Creator
     */
    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public DominoVertex createFromParcel(Parcel source)
        {
            return new DominoVertex(source);
        }

        @Override
        public DominoVertex[] newArray(int size)
        {
            return new DominoVertex[size];
        }
    };
}
