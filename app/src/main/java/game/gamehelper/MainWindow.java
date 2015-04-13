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
import java.util.HashMap;
import java.util.Map;

import game.gamehelper.DominoMT.DominoPlugin;

/**
 * Entry point for the Game Helper App.  Allows for the user to select
 * the desired game to get playing assistance with.
 */
public class MainWindow extends ActionBarActivity
{
    private static final boolean DEBUG_MODE = false;

    private static ArrayList<GameHelperPlugin> games = new ArrayList<>();

    private HashMap<Integer, String> gameList = new HashMap<>();
    private int selectedGame = 0;

    // Register all plugins in this static block.
    static
    {
        games.add(new DominoPlugin());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        int numGames = 0;
        for (GameHelperPlugin game : games) {

            gameList.put(numGames++, game.getDescription());
        }
        gameList.put(numGames++, "Other Games\nComing Soon");
        //TODO: add other games

        Button newGameButton = (Button) findViewById(R.id.newGameButton);
        Button nextGameButton = (Button) findViewById(R.id.nextGameButton);

        final TextView gameTitle = (TextView) findViewById(R.id.gameTitle);

        //New Game Button
        newGameButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        GameHelperPlugin game = games.get(selectedGame);

                        Bundle debugBundle;
                        if (DEBUG_MODE) debugBundle = game.getDebugBundle();
                        else debugBundle = new Bundle();

                        startActivity(new Intent(MainWindow.this, game.getEntryMenuClass()).putExtras(debugBundle));
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
                GameHelperPlugin game = games.get(selectedGame);
                Bundle bundle = new Bundle();
                for(Map.Entry<String, Integer> ids : game.getRulesIDs().entrySet())
                    bundle.putInt(ids.getKey(), ids.getValue());

                Intent activity = new Intent(this, RuleDetailActivity.class);
                activity.putExtras(bundle);
                startActivityForResult(activity, RuleDetailActivity.RULES_EXIT);

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
}
