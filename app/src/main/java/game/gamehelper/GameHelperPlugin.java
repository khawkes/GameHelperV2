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

import android.os.Bundle;

import java.util.Map;

/**
 * Interface that all game type plugins in the game helper app must implement.
 * There will be one plugin per game type.
 *
 * Created by Mark Andrews on 3/7/2015.
 */
public interface GameHelperPlugin
{
    /**
     * Returns the name of the game plugin.
     *
     * @return plugin game name
     */
    public String getName();

    /**
     * Returns a short description of the game plugin.
     *
     * @return plugin short description
     */
    public String getDescription();

    /**
     * Returns the entry class for the game plugin.  This class will
     * be set in the intent created in the main application menu.
     * This should be the entry screen for the game plugin.
     *
     * @return game plugin entry screen class
     */
    public Class<?> getEntryMenuClass();

    /**
     * Returns the rules display class for the game plugin.  This class
     * will be set in the intent used to display the game rules.
     */
    public Map<String, Integer> getRulesIDs();

    /**
     * For debugging purposes - returns a bundle with some pre-generated
     * values used during testing of the plugin.
     *
     * @return bundle with debugging values to use for the plugin
     */
    public Bundle getDebugBundle();

    public ScoreBoard getScoreBoard();
}
