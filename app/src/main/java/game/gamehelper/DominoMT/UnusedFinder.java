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

/**
 * Created by Tim on 4/6/2015.
 */
import java.util.ArrayList;

import game.gamehelper.DominoMT.Domino;

public class UnusedFinder {

    public static Domino[] FindUnused( DominoRun currentRun, HandMT currentHand) {
        ArrayList<Domino> unUsed = new ArrayList<Domino>();
        Domino[] usedRun = currentRun.toArray();
        Domino[] hand = currentHand.toArray();

        //cycles through the hand
        for (Domino inHand : hand) {
            boolean exists = false;

            //cycles through this run.
            for (Domino inRun : usedRun) {
                if (inRun.compareTo(inHand)) {
                    exists = true;
                    break;
                }
            }

            //flag-based checking, if not in run, adds to unused.
            if (!exists) {
                unUsed.add(inHand);
            }
        }

        return unUsed.toArray(new Domino[unUsed.size()]);
    }
}
