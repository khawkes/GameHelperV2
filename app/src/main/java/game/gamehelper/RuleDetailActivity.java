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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class RuleDetailActivity extends ActionBarActivity
//public class RuleDetailActivity extends Activity
{
    int mTextResourceId = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rule_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_detail);

        // Read the arguments from the Intent object.
        Intent in = getIntent();
        mTextResourceId = in.getIntExtra(RulesActivity.ARG_TEXT_ID, 0);
        if (mTextResourceId <= 0) mTextResourceId = R.string.no_help_available;

        TextView textView = (TextView) findViewById(R.id.topic_text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(getString(mTextResourceId)));
    }

} // end class


