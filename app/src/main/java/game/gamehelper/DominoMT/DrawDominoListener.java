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

/**
 * Listener interface for responses from the DrawFragment and DrawRepeatFragment
 * dialogs.
 *
 * Created by khawkes on 4/17/15.
 */
public interface DrawDominoListener
{
    /**
     * Callback for when the DrawFragment dialog closes and when the final domino
     * selection occurs in the DrawRepeatFragment dialog.
     *
     * @param overwrite contains the original source domino if the long press edit was selected.
     * @param added the domino that was added / updated in the fragment
     */
    public void onDrawClose(Domino overwrite, Domino added);

    /**
     * Callback for when the DrawRepeatFragment returns in intermediate domino (not the final domino).
     *
     * @param added the added domino
     */
    public void onDrawRepeatClose(Domino added);
}

