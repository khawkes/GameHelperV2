/*
 * COP4331C - Class Project - The Game Helper App
 * Spring 2015
 *
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
import android.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Run calculating algorithms.
 *
 * Created on 3/6/2015.
 */
public class RunController implements Parcelable
{
    private final int MAX_EDGE;
    private final int TRAIN_HEAD;
    private DominoGraph graph;
    private int target;
    private static boolean midTrainTrainHeadPlaysAllowed = false;

    //debug/stats variables
    private int totalEdgeNum;
    private int repeatPointsFound;
    private int repeatLensFound;
    private int pathsFound;
    private int numVisited;

    //path variables
    private boolean pathsAreCurrent;
    private DominoRun longest;
    private DominoRun mostPoints;

    private LinkedList<DominoRun> mostPointRuns;
    private LinkedList<DominoRun> longestRuns;
    private DominoRun currentRun;

    /**
     * Generates a RunController from a domino edge array.
     *
     * @param hand the hand to use to initialize the graph.
     * @param startDouble the starting double to target.
     */
    public RunController(HandMT hand, int startDouble)
    {
        this(hand.getMaxDouble(), hand.toArray(), startDouble);
    }

    /**
     * Generates a RunController from a domino edge array.
     *
     * @param maximumDouble the biggest double possible (if double 8) -> 8.
     * @param edges the array of edges to use to initialize the graph.
     * @param startDouble the starting double to target.
     */
    public RunController(int maximumDouble, Domino edges[], int startDouble)
    {
        MAX_EDGE = maximumDouble;

        graph = new DominoGraph(maximumDouble, edges);
        totalEdgeNum = edges.length;

        //debug variables
        repeatLensFound = 0;
        repeatPointsFound = 0;
        numVisited = 0;
        pathsFound = 0;

        pathsAreCurrent = false;
        longest = new DominoRun();
        mostPoints = new DominoRun();
        TRAIN_HEAD = target = startDouble;
        mostPointRuns = new LinkedList<>();
        longestRuns = new LinkedList<>();
        currentRun = new DominoRun();
    }

    /**
     * Reconstructs a run controller from the provided parcel.
     *
     * @param p the parcel to construct the parcel from
     */
    public RunController(Parcel p)
    {
        ArrayList<DominoRun> tempList = new ArrayList<>();

        longest = new DominoRun();
        mostPoints = new DominoRun();
        mostPointRuns = new LinkedList<>();
        longestRuns = new LinkedList<>();
        currentRun = new DominoRun();

        MAX_EDGE = p.readInt();
        TRAIN_HEAD = p.readInt();
        graph = p.readParcelable(DominoGraph.class.getClassLoader());
        target = p.readInt();
        midTrainTrainHeadPlaysAllowed = (p.readByte() == 1);
        pathsAreCurrent = (p.readByte() == 1);
        longest = p.readParcelable(DominoRun.class.getClassLoader());
        mostPoints = p.readParcelable(DominoRun.class.getClassLoader());
        p.readList(tempList, null);

        for (DominoRun d : tempList)
        {
            mostPointRuns.add(d);
        }

        tempList.clear();
        p.readList(tempList, null);

        for (DominoRun d : tempList)
        {
            longestRuns.add(d);
        }

        currentRun = p.readParcelable(DominoRun.class.getClassLoader());
    }

    /**
     * Future support to allow rule variation (traditional / custom).
     * Not currently implemented. Future support.
     *
     * @return if we are in custom rules mode
     */
    @SuppressWarnings("unused")
    public static boolean areMidTrainTrainHeadPlaysAllowed()
    {
        return midTrainTrainHeadPlaysAllowed;
    }

    /**
     * Future support to allow rule variation (traditional / custom).
     * Not currently implemented.  Future support.
     * Set rule format.
     *
     * @param midTrainTrainHeadPlaysAllowed use custom rules mode
     */
    @SuppressWarnings("unused")
    public static void setMidTrainTrainHeadPlaysAllowed(boolean midTrainTrainHeadPlaysAllowed)
    {
        RunController.midTrainTrainHeadPlaysAllowed = midTrainTrainHeadPlaysAllowed;
    }

    /**
     * Recalculates the longest and most points paths.  Brute force method
     * implemented.  Brute force method sufficient for data set size.
     */
    // TODO: When time permits, look for better algorithm
    private void recalculatePaths()
    {
        currentRun = new DominoRun();
        mostPoints = new DominoRun();
        longest = new DominoRun();

        numVisited = 0;
        repeatLensFound = 0;
        repeatPointsFound = 0;
        pathsFound = 0;

        //Try to build off the current target.
        mostPointRuns.clear();
        longestRuns.clear();
        currentRun.clear();

        //potential for rule change
        if (!midTrainTrainHeadPlaysAllowed)
        {
            //Start edges must be played on the the start.
            boolean startEdges[] = graph.dumpEdges(TRAIN_HEAD);

            //We blank out the start edges; we can only ever play one off of that one.
            for (int i = 0; i <= MAX_EDGE; i++)
            {
                graph.removeEdgePair(TRAIN_HEAD, i);
            }

            //We need to start on the main domino, so we go through each possible lead-off individually.
            //Remember, edges that map to the main domino must be played on the main domino.
            if (target == TRAIN_HEAD)
            {
                for (int i = MAX_EDGE; i >= 0; i--)
                {
                    //if we have a start edge we can play off of, try to play on it.
                    if (startEdges[i])
                    {
                        currentRun.clear();

                        //if we have the starting double in-hand, add it to this path.
                        if (startEdges[TRAIN_HEAD] && i != TRAIN_HEAD)
                            currentRun.addDomino(new Domino(TRAIN_HEAD, TRAIN_HEAD));

                        graph.addEdgePair(i, TRAIN_HEAD);

                        //early exit: if we find one that traverses whole of graph, we can exit early.
                        if (traverse(TRAIN_HEAD))
                        {
                            graph.removeEdgePair(i, TRAIN_HEAD);
                            break;
                        }
                        graph.removeEdgePair(i, TRAIN_HEAD);
                    }
                }
            }
            //Normal case: we don't have to play off the main domino.
            else
            {
                currentRun.clear();
                traverse(target);
            }

            //Re-add the edges to the main domino at the end.
            for (int i = 0; i <= MAX_EDGE; i++)
            {
                if (startEdges[i])
                    graph.addEdgePair(TRAIN_HEAD, i);
            }
        }
        //If mid-train trainHead plays area allowed, always default toward this.
        else
        {
            currentRun.clear();
            traverse(target);
        }

        //simple heuristic to remove copy runs. Still will need more processing.
        for (DominoRun run : mostPointRuns)
        {
            if (run.isShorterThan(mostPoints))
                mostPoints = run.deepClone();
        }
        for (DominoRun run : longestRuns)
        {
            if (run.hasMorePointsThan(longest))
                longest = run.deepClone();
        }

        System.out.println("last repeat lens found: " + repeatLensFound);
        System.out.println("last repeat points found: " + repeatPointsFound);
        System.out.println("total vertexs visited: " + numVisited);
        System.out.println("total paths found: " + pathsFound);

        pathsAreCurrent = true;
        longestRuns.clear();
        mostPointRuns.clear();
    }

    /**
     * Perform a pseudo-DFS of the domino graph.
     * Method is an O(e^n) algorithm, where n is the number of
     * dominoes in the graph.
     *
     * @param startVertex starting vertex to traverse from
     */
    //TODO sorta fixed. works with up to 27 dominoes.
    private boolean traverse(int startVertex)
    {
        numVisited++;
        //the current run has ended! Calculate runs!
        if (graph.getEdgeNum(startVertex) == 0)
        {
            pathsFound++;
            //this run is so useful in terms of points, we have to get rid of the other runs.
            if (currentRun.hasMorePointsThan(mostPoints))
            {
                mostPointRuns.clear();
                mostPointRuns.add(currentRun.deepClone());
                mostPoints = currentRun.deepClone();
                System.out.println("more points: " + repeatPointsFound);
                repeatPointsFound = 0;
            }
            //this run has the same point value as the other runs.
            else if (!mostPoints.hasMorePointsThan(currentRun))
            {
                //We only want to get rid of the other run if this one is shorter.
                // If it's shorter, it's getting rid of points faster (on average).
                if (currentRun.isShorterThan(mostPoints))
                {
                    mostPointRuns.clear();
                    mostPointRuns.add(currentRun.deepClone());
                    mostPoints = currentRun.deepClone();
                    System.out.println("more points: " + repeatPointsFound);
                    repeatPointsFound = 0;
                }
                repeatPointsFound++;
                //mostPointRuns.add(currentRun.deepClone()); // removed because of memory problems.
            }

            //this run is so useful in terms of length, we have to get rid of the other runs.
            if (currentRun.isLongerThan(longest))
            {
                longestRuns.clear();
                longestRuns.add(currentRun.deepClone());
                longest = currentRun.deepClone();

                //early exit if we touch everything.
                if (longest.getLength() == totalEdgeNum)
                    return true;

                System.out.println("longer run: " + repeatLensFound);
                repeatLensFound = 0;
            }
            //this run has the same length as the other runs.
            else if (!longest.isLongerThan(currentRun))
            {
                //We want to get rid of the other run if this one is worth more points.
                // If it's worth more points, this one is getting rid of the points faster (on average).
                if (currentRun.hasMorePointsThan(longest))
                {
                    longestRuns.clear();
                    longestRuns.add(currentRun.deepClone());
                    longest = currentRun.deepClone();
                    System.out.println("longer run: " + repeatLensFound);
                    repeatLensFound = 0;
                }
                repeatLensFound++;
            }

            //exit up to the other runs, this one's done!
            return false;
        }

        //we should always use the self-double first. There will never be a case where we shouldn't.
        if (graph.hasEdge(startVertex, startVertex))
        {
            graph.toggleEdgePair(startVertex, startVertex);
            currentRun.addDomino(new Domino(startVertex, startVertex));

            //traverse down the graph; if we visit the whole graph, return early.
            if (traverse(startVertex))
            {
                graph.toggleEdgePair(startVertex, startVertex);
                return true;
            }
            currentRun.popEnd();
            graph.toggleEdgePair(startVertex, startVertex);
            return false;
        }

        //Look at the edges in this vertex.
        for (int i = MAX_EDGE; i >= 0; i--)
        {
            if (graph.hasEdge(startVertex, i))
            {
                graph.toggleEdgePair(startVertex, i);
                currentRun.addDomino(new Domino(startVertex, i));

                //traverse down the graph; if we visit the whole graph, return early.
                if (traverse(i))
                {
                    graph.toggleEdgePair(startVertex, i);
                    return true;
                }
                currentRun.popEnd();
                graph.toggleEdgePair(startVertex, i);
            }
        }

        return false;
    }

    /**
     * Returns the longest path.  Performs path finding if necessary.
     *
     * @return the longest path domino run
     */
    public DominoRun getLongestPath()
    {
        if (!pathsAreCurrent)
            recalculatePaths();

        return longest;
    }

    //Returns the most-points path, does pathfinding if necessary.

    /**
     * Return the most points path.  Performs path finding if necessary.
     *
     * @return the most points path domino run
     */
    public DominoRun getMostPointPath()
    {
        if (!pathsAreCurrent)
            recalculatePaths();

        return mostPoints;
    }

    /**
     * Adds another domino to this graph.
     *
     * @param domino the domino to add.
     */
    public void addDomino(Domino domino)
    {
        if (domino.getVal1() > MAX_EDGE || domino.getVal2() > MAX_EDGE)
            throw new AssertionError("New domino is too large.");

        //re-set paths if the domino isn't already in the graph.
        //TODO: Add BFS to determine if we need to re-calculate or not
        if (!graph.hasEdge(domino.getVal1(), domino.getVal2()))
        {
            //double-domino check, worth the O(n) time search for a potential O(2^n) savings.
            if (pathsAreCurrent && domino.getVal1() == domino.getVal2())
            {
                //checks the two runs for equality up till the match, and adds to run if so.
                if (longest.addMidRunDouble(mostPoints, domino.getVal1(), target))
                {
                    graph.addEdgePair(domino.getVal1(), domino.getVal1());
                    totalEdgeNum++;
                    return;
                }
            }

            pathsAreCurrent = false;
            totalEdgeNum++;
            graph.addEdgePair(domino.getVal1(), domino.getVal2());
        }
    }

    /**
     * Re-adds a domino to this graph. Note: Performance-wise, there's really nothing we can do here,
     * as we don't know if the old value was in both longest and most points runs.
     *
     * @param domino the domino to re-add.
     * @param targetVal the new target value.
     */
    public void reAddDomino(Domino domino, int targetVal)
    {
        if (domino.getVal1() > MAX_EDGE || domino.getVal2() > MAX_EDGE)
            throw new AssertionError("New domino is too large.");

        pathsAreCurrent = false;
        totalEdgeNum++;
        target = targetVal;

        graph.addEdgePair(domino.getVal1(), domino.getVal2());
    }

    /**
     * In the case where we want to re-set runs to a previous save, this might save computation time.
     *
     * @param oldRuns the pair of ordered runs (longest, mostPoints)
     */
    public void reSetRuns(Pair<DominoRun, DominoRun> oldRuns)
    {
        //skip if we've already re-calculated, or we didn't have the runs at the time.
        if (pathsAreCurrent || oldRuns == null)
        {
            return;
        }

        //copy in old runs
        pathsAreCurrent = true;
        longest = oldRuns.first.deepClone();
        mostPoints = oldRuns.second.deepClone();
    }

    /**
     * Removes a domino in this graph.
     * Will only throw exceptions when the domino is larger than the maximum domino.
     *
     * @param domino the domino to remove.
     */
    public void removeDomino(Domino domino)
    {
        if (domino.getVal1() > MAX_EDGE || domino.getVal2() > MAX_EDGE)
            throw new AssertionError("Tried to delete domino larger than max domino.");

        //re-set paths if we're deleting something in the graph
        if (graph.hasEdge(domino.getVal1(), domino.getVal2()))
        {
            //check to see if we can just de-queue the front of the pre-calculated runs.
            if (pathsAreCurrent && domino.compareTo(getMostPointPath().peekFront()))
            {
                dequeueMostPoints();
            }
            else if (pathsAreCurrent && domino.compareTo(getLongestPath().peekFront()))
            {
                dequeueLongest();
            }
            //since we can't just de-queue the front, we have to delete the value and re-calculate.
            else
            {
                pathsAreCurrent = false;
                totalEdgeNum--;

                //check to make sure we didn't delete the target, and if so, re-adjust target.
                if (domino.getVal1() == target)
                {
                    target = domino.getVal2();
                }
                else if (domino.getVal2() == target)
                {
                    target = domino.getVal1();
                }

                graph.removeEdgePair(domino.getVal1(), domino.getVal2());
            }
        }
    }

    /**
     * Dequeues the longest front, has to re-calculate for the most point path if it wasn't
     * the other path's front.
     *
     * @return the previous front of the longest list.
     */
    public Domino dequeueLongest()
    {
        Domino retVal;

        //get the front domino
        retVal = getLongestPath().popFront();

        //remove the old domino, set the new target.
        graph.removeEdgePair(retVal.getVal1(), retVal.getVal2());
        target = retVal.getOtherVal(target);
        totalEdgeNum--;

        //if it's not the same as the most point path's front, re-calculate runs.
        if (!retVal.compareTo(getMostPointPath().popFront()))
            recalculatePaths();

        return retVal;
    }

    /**
     * Dequeues the most point front, has to re-calculate for the longest path if it
     * wasn't the other path's front.
     *
     * @return the previous front of the most point path.
     */
    public Domino dequeueMostPoints()
    {
        Domino retVal;

        //get the front domino
        retVal = getMostPointPath().popFront();

        //remove the old domino, set the new target.
        graph.removeEdgePair(retVal.getVal1(), retVal.getVal2());
        target = retVal.getOtherVal(target);
        totalEdgeNum--;

        //If it's not the same as the longest path's front, re-calculate runs.
        if (!retVal.compareTo(getLongestPath().popFront()))
            recalculatePaths();

        return retVal;
    }

    /**
     * Causes this train to use a new head.
     *
     * @param head the new head to use. Ignores if no change (same head).
     */
    public void setTrainHead(int head)
    {
        if (head != target)
        {
            target = head;
            pathsAreCurrent = false;
        }
    }

    /**
     * Let's everyone know if the paths in this runcontroller are up-to-date.
     *
     * @return returns true if the paths are up to date, false if not.
     */
    public boolean isUpToDate()
    {
        return pathsAreCurrent;
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
        dest.writeInt(TRAIN_HEAD);
        dest.writeParcelable(graph, 0);
        dest.writeInt(target);
        dest.writeByte((byte) (midTrainTrainHeadPlaysAllowed ? 1 : 0));

        dest.writeByte((byte) (pathsAreCurrent ? 1 : 0));
        dest.writeParcelable(longest, 0);
        dest.writeParcelable(mostPoints, 0);

        ArrayList<DominoRun> listTemp = new ArrayList<>();
        for (DominoRun d : mostPointRuns)
        {
            listTemp.add(d);
        }

        dest.writeList(listTemp);

        listTemp.clear();
        for (DominoRun d : longestRuns)
        {
            listTemp.add(d);
        }

        dest.writeList(listTemp);
        dest.writeParcelable(currentRun, 0);
    }

    /**
     * Parcel CREATOR for the Domino class.
     *
     * @see android.os.Parcelable.Creator
     */
    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public RunController createFromParcel(Parcel source)
        {
            return new RunController(source);
        }

        @Override
        public Object[] newArray(int size)
        {
            return new RunController[size];
        }
    };
}
