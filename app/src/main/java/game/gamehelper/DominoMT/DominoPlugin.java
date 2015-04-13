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

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import game.gamehelper.GameHelperPlugin;
import game.gamehelper.GameSet;
import game.gamehelper.R;
import game.gamehelper.RuleDetailActivity;
import game.gamehelper.ScoreBoard;

/**
 * The Game Helper App game plugin for assisting the user with the game of
 * Dominoes (Mexican Train variant).
 *
 * Created by Mark Andrews on 3/7/2015.
 */
public class DominoPlugin implements GameHelperPlugin
{
    static final int MAX_DOMINO_DISPLAY = 24;

    private ArrayList<GameSet> setList = new ArrayList<>();
    private ArrayList<String> playerList = new ArrayList<>();

    private int[][] debugTileList = new int[100][2];
    private int debugTotalTiles = 0;
    private int maxDouble = 12;

    @Override
    public String getName()
    {
        return "Mexican Train Dominoes";
    }

    @Override
    public String getDescription()
    {
        return "Dominoes:\nMexican Train";
    }

    @Override
    public Class<?> getEntryMenuClass()
    {
        return GameWindowMT.class;
    }

    @Override
    public Map<String, Integer> getRulesIDs()
    {
        HashMap<String, Integer> rulesIDs = new HashMap<>();
        rulesIDs.put("title", R.string.dominoMT_rules_title);
        rulesIDs.put("text", R.string.dominoMT_rules_text);
        rulesIDs.put("detail", R.string.dominoMT_rules_detail);
        return rulesIDs;
    }

    @Override
    public Bundle getDebugBundle()
    {
        randomDominos(24);
        randomScoreBoard(4, 8);

        Bundle debugBundle = new Bundle();
        debugBundle.putBoolean("debug", true);
        debugBundle.putSerializable("dominoList", debugTileList); //GameWindowMT.class
        debugBundle.putInt("dominoTotal", debugTotalTiles);
        debugBundle.putInt("maxDouble", maxDouble);
        debugBundle.putParcelableArrayList("setList", setList);
        debugBundle.putStringArrayList("playerList", playerList);

        return debugBundle;
    }

    @Override
    public ScoreBoard getScoreBoard()
    {
        return new ScoreBoard();
    }

    //generate a random set of tiles for hand
    //produces a maximum double of 12
    public void randomDominos(int total)
    {
        Random generator = new Random(222);
        boolean[][] used = new boolean[13][13];
        debugTotalTiles = 0;

        if (total > (13 * 14 / 2 - 1))
        {
            total = (13 * 14 / 2) - 1;
        }

        for (boolean[] a : used)
        {
            for (boolean b : a)
            {
                b = false;
            }
        }

        used[maxDouble][maxDouble] = true;

        for (int[] i : debugTileList)
        {

            if (total-- <= 0)
                break;

            i[0] = generator.nextInt(13);
            i[1] = generator.nextInt(13);

            while (used[i[0]][i[1]] != false)
            {
                i[0] = generator.nextInt(13);
                i[1] = generator.nextInt(13);
            }
            used[i[0]][i[1]] = true;
            used[i[1]][i[0]] = true;
            debugTotalTiles++;

            maxDouble = 12;
        }
    }

    //create random list of players and scores
    public void randomScoreBoard(int player, int set)
    {
        Random generator = new Random();
        setList.clear();

        for (int i = 0; i < set; i++)
        {
            GameSet setScores = new GameSet();
            setList.add(setScores);
            for (int j = 0; j < player; j++)
            {
                setList.get(i).addPlayer(generator.nextInt(500));
            }
        }
        for (int j = 0; j < player; j++)
        {
            playerList.add("Player " + j);
        }
    }
}
