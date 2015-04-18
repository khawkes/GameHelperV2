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
import android.util.Pair;

import java.util.ArrayList;
import java.util.Stack;

import game.gamehelper.Hand;

/**
 * Original creation date: 2/11/2015.
 * A hand of Dominoes.
 * TODO add remove/add domino functionality.
 */

public class HandMT implements Hand, Parcelable
{
    //experimental undo history may cause problems if something goes wrong.
    private static final boolean enableExperimentalUndoHistory = true;

    private ArrayList<Domino> dominoHandHistory;
    private ArrayList<Domino> currentHand;

    private RunController runs;

    private int totalPointsHand = 0;
    private int totalDominos;
    private final int MAXIMUM_DOUBLE;
    private final int ORIGINAL_TRAIN_HEAD;
    private int trainHead;

    //undo stuff, should probably be made into an UndoObject at this point.
    //Or rely on the Command pattern.
    private Stack<Domino> playHistory = new Stack<>();
    private Stack<Integer> trainHeadHistory = new Stack<>();
    private Stack<Integer> positionPlayedHistory = new Stack<>();
    private Stack<Pair<DominoRun, DominoRun>> runsHistory = new Stack<>();

    //Initializes the hand
    //Requires maximum double possible.
    //NOTE: We have to have the largest double so the pathfinding calculates a legal path.
    public HandMT(int[][] tileList, int totalTiles, int largestDouble, int startHead)
    {
        dominoHandHistory = new ArrayList<Domino>();
        currentHand = new ArrayList<Domino>();
        totalDominos = totalTiles;

        //create list of tiles
        for (int[] i : tileList)
        {
            if (totalTiles-- <= 0)
                break;

            dominoHandHistory.add(new Domino(i[0], i[1]));
            currentHand.add(new Domino(i[0], i[1]));
            totalPointsHand += i[0] + i[1];
        }

        //sets final hand size and starting head.
        MAXIMUM_DOUBLE = largestDouble;
        ORIGINAL_TRAIN_HEAD = startHead;

        //sets current trainhead.
        trainHead = startHead;

        runs = new RunController(this, ORIGINAL_TRAIN_HEAD);
    }

    //NOTE: We have to have the largest double so the pathfinding calculates a legal path.
    public HandMT(int largestDouble, int startHead)
    {
        dominoHandHistory = new ArrayList<Domino>();
        currentHand = new ArrayList<Domino>();
        totalDominos = 0;

        //sets final hand size and starting head.
        MAXIMUM_DOUBLE = largestDouble;
        ORIGINAL_TRAIN_HEAD = startHead;

        //sets current trainhead.
        trainHead = startHead;

        runs = new RunController(this, ORIGINAL_TRAIN_HEAD);
    }

    //This allows a Hand to be retrieved from a Parcel.
    public HandMT(Parcel p)
    {
        dominoHandHistory = new ArrayList<>();
        currentHand = new ArrayList<>();
        ArrayList<Domino> tempDomList = new ArrayList<>();

        p.readTypedList(dominoHandHistory, Domino.CREATOR);
        p.readTypedList(currentHand, Domino.CREATOR);
        runs = p.readParcelable(DominoRun.class.getClassLoader());
        totalPointsHand = p.readInt();
        totalDominos = p.readInt();
        MAXIMUM_DOUBLE = p.readInt();
        ORIGINAL_TRAIN_HEAD = p.readInt();
        trainHead = p.readInt();

        p.readTypedList(tempDomList, Domino.CREATOR);
        for (Domino d : tempDomList)
        {
            playHistory.push(d);
        }

        ArrayList<Integer> tempInt = (ArrayList<Integer>) p.readSerializable();
        for (Integer i : tempInt)
        {
            trainHeadHistory.push(i);
        }

        tempInt.clear();
        tempInt = (ArrayList<Integer>) p.readSerializable();
        for (Integer i : tempInt)
        {
            positionPlayedHistory.push(i);
        }

        ArrayList<Pair<DominoRun, DominoRun>> tempHistory =
                (ArrayList<Pair<DominoRun, DominoRun>>) p.readSerializable();
        for (Pair<DominoRun, DominoRun> d : tempHistory)
        {
            runsHistory.push(d);
        }
    }

    //Adds a domino to the hand, but only if it doesn't exist
    public void addDomino(Domino d)
    {
        if (exists(d)) return;

        //Sorta like memoization, remembering previous runs.
        rememberRuns();

        dominoHandHistory.add(d);
        currentHand.add(d);
        totalPointsHand = getTotalPointsHand() + d.getDominoValue();
        totalDominos++;
        runs.addDomino(d);

        playHistory.push(d);
        trainHeadHistory.push(trainHead);
        positionPlayedHistory.push(null);
    }

    public void replaceDomino(Domino overwrite, Domino d)
    {
        if (exists(d)) return;

        rememberRuns();

        int pos = findDomino(dominoHandHistory, overwrite);
        if (pos != -1) dominoHandHistory.set(pos, d);

        pos = findDomino(currentHand, overwrite);
        if (pos != -1) currentHand.set(pos, d);

        totalPointsHand = computeHandTotal();
        runs.removeDomino(overwrite);
        runs.addDomino(d);
    }

    public int computeHandTotal()
    {
        int total = 0;

        for(Domino d : currentHand)
        {
            total += d.getDominoValue();
        }

        return total;
    }

    public int findDomino(ArrayList<Domino> list, Domino d)
    {
        if (d == null) return -1;

        for(int i = 0; i < list.size(); i++)
            if (d.equals(list.get(i))) return i;

        return -1;
    }

    public boolean exists(Domino d)
    {
        for (Domino a : currentHand)
        {
            if (a.compareTo(d))
            {
                return true;
            }
        }

        return false;
    }

    //Removes a domino to hand if it exists.
    private void removeDomino(Domino d)
    {
        for (Domino a : currentHand)
        {
            if (a.compareTo(d))
            {
                currentHand.remove(a);
                totalPointsHand = getTotalPointsHand() - d.getDominoValue();
                runs.removeDomino(a);
                totalDominos--;
                break;
            }
        }
    }

    public Domino getDomino(int position, GameWindowMT.WindowContext playContext) {

        //We find the "real" position, and change position to match it so we can delete it below.

        //find the longest path position.
        if (playContext == GameWindowMT.WindowContext.SHOWING_LONGEST)
        {
            // Converts the longest run to an array and indexes the position.
            Domino d = getLongestRun().toArray()[position];
            //finds the domino in the current hand, and sets the correct position.
            position = currentHand.indexOf(d);
        }
        //find the most points path position.
        else if (playContext == GameWindowMT.WindowContext.SHOWING_MOST_POINTS)
        {
            // Converts the most points run to an array and indexes the position.
            Domino d = getMostPointRun().toArray()[position];
            //finds the domino in the current hand, and sets the correct position.
            position = currentHand.indexOf(d);
        }
        //the position given is actually the correct position.
        else
        {
            //position = position;
        }

        //find the domino to remove, and get it from the current hand.
        return currentHand.get(position);
    }

    /**
     * Plays a domino based on its position in the arraylist.
     *
     * @param position    The play position; what order we made the play in.
     * @param playContext The context in which the play was made.
     */
    public void dominoPlayed(int position, GameWindowMT.WindowContext playContext)
    {
        Domino toRemove = getDomino(position, playContext);

        //add to our undo stacks.
        playHistory.push(toRemove);
        positionPlayedHistory.push(position);
        trainHeadHistory.push(trainHead);

        //Sorta like memoization, remembering previous runs.
        rememberRuns();

        //we removed the train head, adjust the train head accordingly.
        if (toRemove.getVal1() == trainHead)
        {
            trainHead = toRemove.getVal2();
        }
        else if (toRemove.getVal2() == trainHead)
        {
            trainHead = toRemove.getVal1();
        }

        //removes the domino & its information from the hand.
        removeDomino(toRemove);
    }

    /**
     * Undoes a previous play. Re-sets the train head to the old one, and
     * adds the domino back in its old play position.
     */
    public boolean undo()
    {
        if (positionPlayedHistory.size() == 0)
            return false;

        Domino lastDomino;
        Integer position;
        Integer savedTrainHead;
        Pair<DominoRun, DominoRun> oldRuns;

        //retrieve last move
        position = positionPlayedHistory.pop();
        savedTrainHead = trainHeadHistory.pop();
        lastDomino = playHistory.pop();
        oldRuns = runsHistory.pop();

        //in the case we only changed the train head
        if (position == null && lastDomino == null)
        {
            runs.setTrainHead(savedTrainHead);
            //re-sets the runs if possible, saving calculation time.
            runs.reSetRuns(oldRuns);

            trainHead = savedTrainHead;
            return true;
        }

        //in the case we added something before (null), we want to remove it now.
        if (position == null)
        {
            removeDomino(lastDomino);

            //re-set train head to the saved one, fixes a bug where the runs decides to "play" the domino.
            trainHead = savedTrainHead;
            runs.setTrainHead(trainHead);

            //re-sets the runs if possible, saving calculation time.
            runs.reSetRuns(oldRuns);
            return true;
        }

        //add information back to hand
        currentHand.add(position, lastDomino);
        runs.reAddDomino(lastDomino, savedTrainHead);
        totalPointsHand += lastDomino.getDominoValue();
        totalDominos++;
        trainHead = savedTrainHead;

        //re-sets the runs if possible, saving calculation time.
        runs.reSetRuns(oldRuns);
        return true;
    }

    /**
     * Gets the longest run from our RunController.
     *
     * @return Returns the longest run.
     */
    public DominoRun getLongestRun()
    {
        return runs.getLongestPath();
    }

    /**
     * Gets the most points run from our RunController.
     *
     * @return Returns the most points run.
     */
    public DominoRun getMostPointRun()
    {
        return runs.getMostPointPath();
    }

    //Returns true if the paths in the run controller are up to date.
    public boolean runsAreUpToDate()
    {
        return runs.isUpToDate();
    }

    /**
     * Gets the total points of the current hand.
     *
     * @return Returns the total points of the current hand.
     */
    @Override
    public int getTotalPointsHand()
    {
        return totalPointsHand;
    }

    /**
     * Gets the number of dominoes in this hand.
     *
     * @return Returns the number of dominos in this hand.
     */
    public int getTotalDominos()
    {
        return totalDominos;
    }

    /**
     * Converts the current hand to a domino array.
     *
     * @return Returns the converted current hand to a domino array.
     */
    public Domino[] toArray()
    {
        return currentHand.toArray(new Domino[currentHand.size()]);
    }

    //returns int[][] for saving between onCreate calls in gameWindow
    public int[][] smallArray()
    {
        int[][] list = new int[getTotalDominos()][2];
        int i = 0;
        for (Domino currentDomino : currentHand)
        {
            list[i][0] = currentDomino.getVal1();
            list[i][1] = currentDomino.getVal2();
            i++;
        }
        return list;
    }

    /**
     * Gets the maximum double value, which all dominoes with that value must be played on.
     *
     * @return Returns maximum double (the start domino)
     */
    public int getMaxDouble()
    {
        return MAXIMUM_DOUBLE;
    }

    /**
     * Gets the current train head.
     *
     * @return Returns the current train head.
     */
    public int getTrainHead()
    {
        return trainHead;
    }

    /*
     * Changes current domino head based on manual input in GameWindow
     */
    public void setTrainHead(int head)
    {
        //Sorta like memoization, remembering previous runs.
        rememberRuns();

        //undo stacks
        playHistory.push(null);
        trainHeadHistory.push(trainHead);
        positionPlayedHistory.push(null);

        trainHead = head;
        runs.setTrainHead(head);
    }

    //so everyone uses the right order!
    private void rememberRuns()
    {
        if (runsAreUpToDate() && enableExperimentalUndoHistory)
            runsHistory.push(new Pair<DominoRun, DominoRun>
                    (getLongestRun().deepCopy(), getMostPointRun().deepCopy()));
        else
            runsHistory.push(null);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeTypedList(dominoHandHistory);
        dest.writeTypedList(currentHand);
        dest.writeParcelable(runs, 0);
        dest.writeInt(totalPointsHand);
        dest.writeInt(totalDominos);
        dest.writeInt(MAXIMUM_DOUBLE);
        dest.writeInt(ORIGINAL_TRAIN_HEAD);
        dest.writeInt(trainHead);

        ArrayList<Domino> tempDomList = new ArrayList<>();
        ArrayList<Integer> tempInt = new ArrayList<>();
        ArrayList<Pair<DominoRun, DominoRun>> tempHistory = new ArrayList<>();

        for (Domino d : playHistory)
        {
            tempDomList.add(d);
        }

        dest.writeTypedList(tempDomList);

        for (Integer i : trainHeadHistory)
        {
            tempInt.add(i);
        }

        dest.writeSerializable(tempInt);
        tempInt.clear();

        for (Integer i : positionPlayedHistory)
        {
            tempInt.add(i);
        }

        dest.writeSerializable(tempInt);

        for (Pair<DominoRun, DominoRun> d : runsHistory)
        {
            tempHistory.add(d);
        }

        dest.writeSerializable(tempHistory);
    }

    public static Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        @Override
        public HandMT createFromParcel(Parcel source)
        {
            return new HandMT(source);
        }

        @Override
        public HandMT[] newArray(int size)
        {
            return new HandMT[size];
        }
    };
}
