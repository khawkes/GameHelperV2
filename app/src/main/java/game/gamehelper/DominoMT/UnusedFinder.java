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

import java.util.ArrayList;

/**
 * Find and return the collection of unused dominoes in a hand.
 *
 * Created by Tim on 4/6/2015.
 */
public class UnusedFinder
{
    /**
     * Based upon the provided current run and the current hand, return
     * and array of all dominoes that are in the hand that are not in
     * the run.
     *
     * @param currentRun the run to exclude dominoes
     * @param currentHand the hand containing all dominoes
     * @return an array of dominoes that are not in the run
     */
    public static Domino[] FindUnused(DominoRun currentRun, HandMT currentHand)
    {
        ArrayList<Domino> unUsed = new ArrayList<>();
        Domino[] usedRun = currentRun.toArray();
        Domino[] hand = currentHand.toArray();

        //cycles through the hand
        for (Domino inHand : hand)
        {
            boolean exists = false;

            //cycles through this run.
            for (Domino inRun : usedRun)
            {
                if (inRun.compareTo(inHand))
                {
                    exists = true;
                    break;
                }
            }

            //flag-based checking, if not in run, adds to unused.
            if (!exists)
            {
                unUsed.add(inHand);
            }
        }

        return unUsed.toArray(new Domino[unUsed.size()]);
    }
}
