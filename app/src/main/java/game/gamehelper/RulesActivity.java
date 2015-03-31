package game.gamehelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class RulesActivity extends ActionBarActivity
{

    static public final String ARG_TEXT_ID = "text_id";

    private static int RULES_EXIT = 88;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_rules);

        // Set up so that formatted text can be in the help_page_intro text and so that html links are handled.
        TextView textView = (TextView) findViewById (R.id.help_page_intro);
        if (textView != null) {
            textView.setMovementMethod ( LinkMovementMethod.getInstance());
            textView.setText ( Html.fromHtml( getString( R.string.help_page_intro_html ) ));
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
        int id = item.getItemId();

        switch (item.getItemId())
        {
            case R.id.menu_rules:
            {
                startActivityForResult(new Intent(RulesActivity.this, RulesActivity.class),RULES_EXIT);

                break;
            }

            case R.id.menu_exit:
            {
                finish();
                System.exit( 0 );
                break;
            }

            default:

        }

        return( super.onOptionsItemSelected(item) );
    }

    /**
     * Handle the click of one of the help buttons on the page.
     * Start an activity to display the help text for the topic selected.
     */

    public void onClickHelp (View v)
    {
        int id = v.getId ();
        int textId = -1;
        switch (id) {
            case R.id.help_button1 :
                textId = R.string.topic_section1;
                break;
            case R.id.help_button2 :
                textId = R.string.topic_section2;
                break;
            default:
                break;
        }

        if (textId >= 0) startInfoActivity (textId);
        else toast ("Detailed Help for that topic is not available.", true);
    }

    /**
     * Start a TopicActivity and show the text indicated by argument 1.
     */

    public void startInfoActivity (int textId)
    {
        if (textId >= 0) {
            Intent intent = (new Intent(this, RuleDetailActivity.class));
            intent.putExtra (ARG_TEXT_ID, textId);
            startActivity (intent);
        } else {
            toast ("No information is available for topic: " + textId, true);
        }
    } // end Activity

    /**
     * Show a string on the screen via Toast.

     */

    public void toast (String msg, boolean longLength)
    {
        Toast.makeText (getApplicationContext(), msg,
                (longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)
        ).show ();
    }

}
