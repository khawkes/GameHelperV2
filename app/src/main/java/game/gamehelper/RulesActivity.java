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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class RulesActivity extends ActionBarActivity implements ImageButton.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_rules);

        setTitle(R.string.help_title);

        // Set up so that formatted text can be in the help_page_intro text and so that html links are handled.
        TextView textView = (TextView) findViewById(R.id.help_page_intro);
        if (textView != null)
        {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.help_page_intro_html)));
        }

        GridView rulesGrid = (GridView) findViewById(R.id.rulesGrid);
        rulesGrid.setSmoothScrollbarEnabled(true);

        RuleAdapter gamesAdapter = new RuleAdapter(this, R.layout.rule_item);
        gamesAdapter.setGames(MainWindow.getGames());
        rulesGrid.setAdapter(gamesAdapter);
    }

    @Override
    public void onClick(View v)
    {
        GameHelperPlugin game = (GameHelperPlugin)v.getTag();
        if (game != null)
        {
            Bundle bundle = new Bundle();
            for(Map.Entry<String, Integer> ids : game.getRulesIDs().entrySet())
                bundle.putInt(ids.getKey(), ids.getValue());

            Intent activity = new Intent(this, RuleDetailActivity.class);
            activity.putExtras(bundle);
            startActivityForResult(activity, RuleDetailActivity.RULES_EXIT);
        }
    }

    public class RuleAdapter extends ArrayAdapter<GameHelperPlugin>
    {
        private GameHelperPlugin[] games;
        private Context context;
        private int resource;

        public RuleAdapter(Context context, @LayoutRes int resource)
        {
            super(context, resource);
            this.context = context;
            this.resource = resource;
        }

        private void setGames(Collection<GameHelperPlugin> games)
        {
            ArrayList<GameHelperPlugin> withRules = new ArrayList<>();
            for(GameHelperPlugin game : games)
                if (game.isRuleSetReady()) withRules.add(game);

            this.games = withRules.toArray(
                    new GameHelperPlugin[withRules.size()]);
        }

        @Override
        public int getCount() { return games.length; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            GameHelperPlugin game = games[position];

            if (row == null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(resource, parent, false);

                row.setTag(R.id.btnGameImage, game.getImageIcon());
                row.setTag(R.id.txtGameDescr, game.getDescription());
            }

            RelativeLayout rl = (RelativeLayout)row;
            ImageButton img = (ImageButton)rl.getChildAt(0);
            Integer resId = (Integer)row.getTag(R.id.btnGameImage);
            Drawable icon = context.getResources().getDrawable(resId);
            img.setImageDrawable(icon);
            img.setOnClickListener(RulesActivity.this);
            img.setTag(game);

            TextView descr = (TextView)rl.getChildAt(1);
            descr.setText((String)row.getTag(R.id.txtGameDescr));

            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rules, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case R.id.menu_exit:
            {
                finish();
                break;
            }
        }

        return (super.onOptionsItemSelected(item));
    }
}
