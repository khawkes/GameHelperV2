package game.gamehelper.DominoMT;

import android.util.Pair;

import java.util.LinkedList;

/**
 * Created by Jacob on 3/6/2015.
 * Contains run-calculating related algorithms.
 */
public class RunController {
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
     * @param h The hand to use to initialize the graph.
     * @param startDouble The starting double to target.
     */
    RunController(HandMT h, int startDouble) {
        this (h.getMaxDouble(), h.toArray(), startDouble);
    }

    /**
     * Generates a RunController from a domino edge array.
     * @param maximumDouble The biggest double possible (if double 8) -> 8.
     * @param edges The array of edges to use to initialize the graph.
     * @param startDouble The starting double to target.
     */
    RunController(int maximumDouble, Domino edges[], int startDouble) {
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
        mostPointRuns = new LinkedList<DominoRun>();
        longestRuns = new LinkedList<DominoRun>();
        currentRun = new DominoRun();
    }

    //Getter's & setters for rule field.
    public static boolean areMidTrainTrainHeadPlaysAllowed() {
        return midTrainTrainHeadPlaysAllowed;
    }

    public static void setMidTrainTrainHeadPlaysAllowed(boolean midTrainTrainHeadPlaysAllowed) {
        RunController.midTrainTrainHeadPlaysAllowed = midTrainTrainHeadPlaysAllowed;
    }


    //recalculates longest/most point path. Uses brute force.
    private void recalculatePaths() {
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
        if (!midTrainTrainHeadPlaysAllowed) {
            //Start edges must be played on the the start.
            boolean startEdges[] = graph.dumpEdges(TRAIN_HEAD);

            //We blank out the start edges; we can only ever play one off of that one.
            for (int i = 0; i <= MAX_EDGE; i++) {
                graph.removeEdgePair(TRAIN_HEAD, i);
            }

            //We need to start on the main domino, so we go through each possible lead-off individually.
            //Remember, edges that map to the main domino must be played on the main domino.
            if (target == TRAIN_HEAD) {
                for (int i = MAX_EDGE; i >= 0; i--) {
                    //if we have a start edge we can play off of, try to play on it.
                    if (startEdges[i]) {
                        currentRun.clear();
                        graph.addEdgePair(i, TRAIN_HEAD);

                        //early exit: if we find one that traverses whole of graph, we can exit early.
                        if (traverse(MAX_EDGE)) {
                            graph.removeEdgePair(i, TRAIN_HEAD);
                            break;
                        }
                        graph.removeEdgePair(i, TRAIN_HEAD);
                    }
                }
            }
            //Normal case: we don't have to play off the main domino.
            else {
                currentRun.clear();
                traverse(target);
            }

            //Re-add the edges to the main domino at the end.
            for (int i = 0; i <= MAX_EDGE; i++) {
                if (startEdges[i])
                    graph.addEdgePair(TRAIN_HEAD, i);
            }
        }
        //If mid-train trainHead plays area allowed, always default toward this.
        else {
            currentRun.clear();
            traverse(target);
        }

        //simple heuristic to remove copy runs. Still will need more processing.
        for (DominoRun run : mostPointRuns) {
            if (run.isShorterThan(mostPoints))
                mostPoints = run.deepCopy();
        }
        for (DominoRun run : longestRuns) {
            if (run.hasMorePointsThan(longest))
                longest = run.deepCopy();
        }

        System.out.println("last repeat lens found: " + repeatLensFound);
        System.out.println("last repeat points found: " + repeatPointsFound);
        System.out.println("total vertexs visited: " + numVisited);
        System.out.println("total paths found: " + pathsFound);

        pathsAreCurrent = true;
        longestRuns.clear();
        mostPointRuns.clear();
    }

    //Does a pseudo-DFS of the graph.
    //is O(e^n), where n is the number of dominoes in the graph.
    //TODO sorta fixed. works with up to 27 dominoes.
    private boolean traverse(int startVertex) {
        numVisited++;
        //the current run has ended! Calculate runs!
        if (graph.getEdgeNum(startVertex) == 0) {
            pathsFound++;
            //this run isn't useful in terms of points, skip it.
            if (mostPoints.hasMorePointsThan(currentRun)) {
                ;
            }
            //this run is so useful in terms of points, we have to get rid of the other runs.
            else if (currentRun.hasMorePointsThan(mostPoints)) {
                mostPointRuns.clear();
                mostPointRuns.add(currentRun.deepCopy());
                mostPoints = currentRun.deepCopy();
                System.out.println("more points: " + repeatPointsFound);
                repeatPointsFound = 0;
            }
            //this run has the same point value as the other runs.
            else {
                //We only want to get rid of the other run if this one is shorter.
                // If it's shorter, it's getting rid of points faster (on average).
                if (currentRun.isShorterThan(mostPoints)) {
                    mostPointRuns.clear();
                    mostPointRuns.add(currentRun.deepCopy());
                    mostPoints = currentRun.deepCopy();
                    System.out.println("more points: " + repeatPointsFound);
                    repeatPointsFound = 0;
                }
                repeatPointsFound++;
                //mostPointRuns.add(currentRun.deepCopy()); // removed because of memory problems.
            }

            //this run isn't useful in terms of length, skip it.
            if (longest.isLongerThan(currentRun)) {
                ;
            }
            //this run is so useful in terms of length, we have to get rid of the other runs.
            else if (currentRun.isLongerThan(longest)) {
                longestRuns.clear();
                longestRuns.add(currentRun.deepCopy());
                longest = currentRun.deepCopy();

                //early exit if we touch everything.
                if (longest.getLength() == totalEdgeNum)
                    return true;

                System.out.println("longer run: " + repeatLensFound);
                repeatLensFound = 0;
            }
            //this run has the same length as the other runs.
            else {
                //We want to get rid of the other run if this one is worth more points.
                // If it's worth more points, this one is getting rid of the points faster (on average).
                if (currentRun.hasMorePointsThan(longest)) {
                    longestRuns.clear();
                    longestRuns.add(currentRun.deepCopy());
                    longest = currentRun.deepCopy();
                    System.out.println("longer run: " + repeatLensFound);
                    repeatLensFound = 0;
                }
                repeatLensFound++;
            }

            //exit up to the other runs, this one's done!
            return false;
        }

        //we should always use the self-double first. There will never be a case where we shouldn't.
        if (graph.hasEdge(startVertex, startVertex)) {
            graph.toggleEdgePair(startVertex, startVertex);
            currentRun.addDomino(new Domino(startVertex, startVertex));

            //traverse down the graph; if we visit the whole graph, return early.
            if (traverse(startVertex)) {
                graph.toggleEdgePair(startVertex, startVertex);
                return true;
            }
            currentRun.popEnd();
            graph.toggleEdgePair(startVertex, startVertex);
            return false;
        }

        //Look at the edges in this vertex.
        for (int i = MAX_EDGE; i >= 0; i--) {
            if (graph.hasEdge(startVertex, i)) {
                graph.toggleEdgePair(startVertex, i);
                currentRun.addDomino(new Domino(startVertex, i));

                //traverse down the graph; if we visit the whole graph, return early.
                if (traverse(i)) {
                    graph.toggleEdgePair(startVertex, i);
                    return true;
                }
                currentRun.popEnd();
                graph.toggleEdgePair(startVertex, i);
            }
        }

        return false;
    }

    //returns the longest path, does pathfinding if necessary.
    public DominoRun getLongestPath() {
        if (!pathsAreCurrent)
            recalculatePaths();

        return longest;
    }

    //Returns the most-points path, does pathfinding if necessary.
    public DominoRun getMostPointPath() {
        if (!pathsAreCurrent)
            recalculatePaths();

        return mostPoints;
    }

    /**
     * Adds another domino to this graph.
     * @param d The domino to add.
     */
    public void addDomino(Domino d) {
        if (d.getVal1() > MAX_EDGE || d.getVal2() > MAX_EDGE)
            throw new AssertionError("New domino is too large.");

        //re-set paths if the domino isn't already in the graph.
        //TODO: Add BFS to determine if we need to re-calculate or not
        if (!graph.hasEdge(d.getVal1(), d.getVal2())) {
            //double-domino check, worth the O(n) time search for a potential O(2^n) savings.
            if (pathsAreCurrent && d.getVal1() == d.getVal2()) {
                //checks the two runs for equality up till the match, and adds to run if so.
                if (longest.addMidRunDouble(mostPoints, d.getVal1(), target)) {
                    graph.addEdgePair(d.getVal1(), d.getVal1());
                    totalEdgeNum++;
                    return;
                }
            }

            pathsAreCurrent = false;
            totalEdgeNum++;
            graph.addEdgePair(d.getVal1(), d.getVal2());
        }
    }

    /**
     * Re-adds a domino to this graph. Note: Performance-wise, there's really nothing we can do here,
     * as we don't know if the old value was in both longest and most points runs.
     *
     * @param d The domino to re-add.
     * @param targetVal The new target value.
     *
     */
    public void reAddDomino(Domino d, int targetVal) {
        if (d.getVal1() > MAX_EDGE || d.getVal2() > MAX_EDGE)
            throw new AssertionError("New domino is too large.");

        pathsAreCurrent = false;
        totalEdgeNum++;
        target = targetVal;

        graph.addEdgePair(d.getVal1(), d.getVal2());
    }

    /**
     * In the case where we want to re-set runs to a previous save, this might save computation time.
     * @param oldRuns The pair of ordered runs (longest, mostPoints)
     */
    public void reSetRuns(Pair<DominoRun, DominoRun> oldRuns) {
        //skip if we've already re-calculated, or we didn't have the runs at the time.
        if (pathsAreCurrent || oldRuns == null) {
            return;
        }

        //copy in old runs
        pathsAreCurrent = true;
        longest = oldRuns.first.deepCopy();
        mostPoints = oldRuns.second.deepCopy();
    }

    /**
     * Removes a domino in this graph.
     * Will only throw exceptions when the domino is larger than the maximum domino.
     * @param d The domino to remove.
     */
    public void removeDomino(Domino d) {
        if (d.getVal1() > MAX_EDGE || d.getVal2() > MAX_EDGE)
            throw new AssertionError("Tried to delete domino larger than max domino.");

        //re-set paths if we're deleting something in the graph
        if (graph.hasEdge(d.getVal1(), d.getVal2())) {
            //check to see if we can just de-queue the front of the pre-calculated runs.
            if (pathsAreCurrent && d.compareTo(getMostPointPath().peekFront())) {
                dequeueMostPoints();
            }
            else if (pathsAreCurrent && d.compareTo(getLongestPath().peekFront())) {
                dequeueLongest();
            }
            //since we can't just de-queue the front, we have to delete the value and re-calculate.
            else {
                pathsAreCurrent = false;
                totalEdgeNum--;

                //check to make sure we didn't delete the target, and if so, re-adjust target.
                if (d.getVal1() == target) {
                    target = d.getVal2();
                }
                else if (d.getVal2() == target) {
                    target = d.getVal1();
                }

                graph.removeEdgePair(d.getVal1(), d.getVal2());
            }
        }
    }

    /**
     * Dequeues the longest front, has to re-calculate for the most point path if it wasn't the other path's front.
     * @return The previous front of the longest list.
     */
    public Domino dequeueLongest() {
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
     * Dequeues the most point front, has to re-calculate for the longest path if it wasn't the other path's front.
     * @return The previous front of the most point path.
     */
    public Domino dequeueMostPoints() {
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
     * @param head The new head to use. Ignores if no change (same head).
     */
    public void setTrainHead(int head){
        if (head != target) {
            target = head;
            pathsAreCurrent = false;
        }
    }

    /**
     * Let's everyone know if the paths in this runcontroller are up-to-date.
     * @return Returns true if the paths are up to date, false if not.
     */
    public boolean isUpToDate() {
        return pathsAreCurrent;
    }
}
