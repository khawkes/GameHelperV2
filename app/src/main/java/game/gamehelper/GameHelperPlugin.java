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

package game.gamehelper;

/**
 * Created by Mark Andrews on 3/7/2015.
 */
public interface GameHelperPlugin {

    public ScoreBoard getScoreBoard();

    public void getRules();

    public void displayMenu();


}
