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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import game.gamehelper.DominoMT.GameWindowMT;

public class MainWindow extends ActionBarActivity
{
    public static final int MEXICAN_TRAIN = 0;
    public static final int MAX_DOMINO_DISPLAY = 24;
    public static boolean debug = false;

    public int[][] tileList = new int[100][2];
    ArrayList<GameSet> setList = new ArrayList<GameSet>();
    ArrayList<String> playerList = new ArrayList<String>();
    ArrayList<String> gameList = new ArrayList<String>();
    public int totalTiles = 0;
    public int maxDouble = 12;
    public int player = 4;
    public int set = 8;
    public int selectedGame = 0;

    static final int RULES_EXIT = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        //getSupportActionBar().hide();

        gameList.add("Dominos:\nMexicanTrain");
        gameList.add("Other games");
        //TODO add other games

        Button newGameButton = (Button) findViewById(R.id.newGameButton);
        Button nextGameButton = (Button) findViewById(R.id.nextGameButton);


        final TextView gameTitle = (TextView) findViewById(R.id.gameTitle);

        final Bundle bundle = new Bundle();

        //New Game Button
        newGameButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {

                        debug = false;
                        switch (selectedGame)
                        {

                            case MEXICAN_TRAIN:

                                bundle.clear();
                                bundle.putSerializable("dominoList", tileList);
                                bundle.putInt("dominoTotal", totalTiles);
                                bundle.putInt("maxDouble", maxDouble);
                                startActivity(new Intent(MainWindow.this, GameWindowMT.class).putExtras(bundle));
                                break;

                            default:
                                //TODO add other games
                                break;
                        }
                    }
                }
        );

        //Next Game Button
        nextGameButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        selectedGame++;
                        if (selectedGame > gameList.size() - 1)
                            selectedGame = 0;
                        gameTitle.setText(gameList.get(selectedGame));
                    }
                }
        );


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_window, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        switch (item.getItemId())
        {
            case R.id.menu_rules:
            {
                startActivityForResult(new Intent(MainWindow.this, RulesActivity.class), RULES_EXIT);

                break;
            }

            case R.id.menu_exit:
            {

                finish();
                System.exit(0);
                break;
            }

            default:

        }

        return (super.onOptionsItemSelected(item));
    }

    //generate a random set of tiles for hand
    //produces a maximum double of 12
    public void randomDominos(int total)
    {
        Random generator = new Random(222);
        boolean[][] used = new boolean[13][13];
        totalTiles = 0;

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

        for (int[] i : tileList)
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
            totalTiles++;

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
