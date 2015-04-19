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
 * A Domino graph, made of DominoVertexes.
 *
 * Created by Jacob on 2/11/2015.
 */
// TODO: Reduce brute force
public class DominoGraph implements Parcelable
{
    private final int MAX_EDGE;
    private DominoVertex graph[];

    /**
     * Generates a DominoGraph from a domino edge array.
     *
     * @param h The hand to use to initialize the graph.
     */
    @SuppressWarnings("unused")
    DominoGraph(HandMT h)
    {
        this(h.getMaxDouble(), h.toArray());
    }

    /**
     * Load a DominoGraph from a parcel.
     *
     * @param p the parcel to load the DominoGraph from
     */
    DominoGraph(Parcel p)
    {
        MAX_EDGE = p.readInt();
        graph = new DominoVertex[MAX_EDGE + 1];
        p.readTypedArray(graph, DominoVertex.CREATOR);
    }

    /**
     * Generates a DominoGraph from a domino edge array.
     *
     * @param maximumDouble the biggest double possible (if double 8) -> 8.
     * @param edges the array of edges to use to initialize the graph.
     */
    DominoGraph(int maximumDouble, Domino edges[])
    {
        MAX_EDGE = maximumDouble;

        graph = new DominoVertex[MAX_EDGE + 1];

        //reserves the graph's memory.
        for (int i = 0; i <= MAX_EDGE; i++)
        {
            graph[i] = new DominoVertex(MAX_EDGE);
        }

        //initializes the graph.
        for (Domino d : edges)
        {
            addEdgePair(d.getVal1(), d.getVal2());
        }
    }

    /**
     * Adds an edge pair to this graph.
     *
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     */
    public void addEdgePair(int v1, int v2)
    {
        try
        {
            graph[v1].addEdge(v2);
            graph[v2].addEdge(v1);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new AssertionError("Out of bounds! Bad domino! Max edge = "
                    + MAX_EDGE + ". Found edges = " + (v1) + " " + (v2));
        }
    }

    /**
     * Toggles an edge pair in this graph.
     *
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     */
    public void toggleEdgePair(int v1, int v2)
    {
        try
        {
            if (v1 != v2)
            {
                graph[v1].toggleEdge(v2);
                graph[v2].toggleEdge(v1);
            }
            else
            {
                graph[v1].toggleEdge(v2);
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new AssertionError("Out of bounds! Bad domino!");
        }
    }

    public boolean hasEdge(int v1, int v2)
    {
        return graph[v1].hasEdge(v2) && graph[v2].hasEdge(v1);
    }

    /**
     * Deletes an edge pair from this graph.
     *
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     */
    public void removeEdgePair(int v1, int v2)
    {
        try
        {
            graph[v1].removeEdge(v2);
            graph[v2].removeEdge(v1);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            e.printStackTrace();
            throw new AssertionError("Out of bounds! Bad domino!");
        }
    }

    /**
     * Dumps all the indicated position's edges.
     *
     * @return Returns all the edges on the specified vertex.
     */
    public boolean[] dumpEdges(int vertex)
    {
        return graph[vertex].dumpEdges();
    }

    /**
     * Returns an individual vertex's edge number.
     *
     * @param v The vertex to check.
     * @return The vertex's edge number.
     */
    public int getEdgeNum(int v)
    {
        return graph[v].getEdgeNum();
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
        dest.writeTypedArray(graph, 0);
    }

    /**
     * Parcel CREATOR for the Domino class.
     *
     * @see android.os.Parcelable.Creator
     */
    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public DominoGraph createFromParcel(Parcel source)
        {
            return new DominoGraph(source);
        }

        @Override
        public DominoGraph[] newArray(int size)
        {
            return new DominoGraph[size];
        }
    };
}
