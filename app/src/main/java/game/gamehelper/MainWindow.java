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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import game.gamehelper.DominoMT.GameHelperOverrides.DominoPlugin;
import game.gamehelper.Scrabble.ScrabblePlugin;

/**
 * Entry point for the Game Helper App.  Allows for the user to select
 * the desired game to get playing assistance with.
 */
public class MainWindow extends ActionBarActivity
{
    private static final boolean DEBUG_MODE = false;

    private static HashMap<Integer, GameHelperPlugin> games = new HashMap<>();

    private int selectedGame = 0;

    // Register all plugins in this static block.
    static
    {
        games.put(0, new DominoPlugin());
        games.put(1, new ScrabblePlugin());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        games.put(games.size(), new GameHelperPlugin() {
            @Override
            public String getName() { return "Other"; }

            @Override
            public String getDescription()
            {
                return "Other Games\n" +
                        "Coming Soon";
            }

            @Override
            public Class<?> getEntryMenuClass() { return null; }

            @Override
            public Map<String, Integer> getRulesIDs() { return null; }

            @Override
            public Bundle getDebugBundle() { return null; }

            @Override
            public int getImageIcon() { return 0; }

            @Override
            public boolean isGameReady() { return false; }

            @Override
            public boolean isRuleSetReady() { return false; }

            @Override
            public ScoreBoard getScoreBoard() { return null; }
        });

        final Button newGameButton = (Button) findViewById(R.id.newGameButton);
        Button nextGameButton = (Button) findViewById(R.id.nextGameButton);
        Button allRulesButton = (Button) findViewById(R.id.listRulesButton);

        final TextView gameTitle = (TextView) findViewById(R.id.gameTitle);
        gameTitle.setText(games.get(0).getDescription());

        //New Game Button
        newGameButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        GameHelperPlugin game = games.get(selectedGame);
                        if (!game.isGameReady()) return;

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
                        if (++selectedGame >= games.size()) selectedGame = 0;
                        GameHelperPlugin game = games.get(selectedGame);
                        gameTitle.setText(game.getDescription());
                        newGameButton.setEnabled(game.isGameReady());
                    }
                }
        );

        //All Rules Button
        allRulesButton.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {

                        startActivity(new Intent(MainWindow.this, RulesActivity.class));
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

    public static Collection<GameHelperPlugin> getGames() {

        return Collections.unmodifiableCollection(games.values());
    }
}
