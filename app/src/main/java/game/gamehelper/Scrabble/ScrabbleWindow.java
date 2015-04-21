package game.gamehelper.Scrabble;

import android.app.Service;
import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;

import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;

import android.widget.TextView;


import game.gamehelper.R;

/**
 * Created by Jacob on 4/15/2015.
 * Most of the code by Mark (copy-pasted in).
 */
public class ScrabbleWindow extends ActionBarActivity implements AnagramLibrary.AnagramCallback
{
    private GridView listView;
    private TextView lettersText;
    private TextView titleText;
    private AnagramLibrary dictionary;
    private ScrabbleWord[] wordList;
    private boolean ready;

    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrabble_game_window);

        lettersText = (TextView) findViewById(R.id.letters);
        titleText = (TextView) findViewById(R.id.titleText);
        listView = (GridView) findViewById(R.id.gridViewMain);
        listView.setNumColumns(getResources().getConfiguration().orientation);

        imm = (InputMethodManager)this.getSystemService(Service.INPUT_METHOD_SERVICE);
        lettersText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                imm.showSoftInput(lettersText, 0);
            }
        });

        lettersText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&  (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    imm.hideSoftInputFromWindow(lettersText.getWindowToken(), 0);
                    if(ready)
                    {
                        updateWords();
                    }
                    return true;
                }
                return false;
            }
        });

        dictionary = new AnagramLibrary(this);

        titleText.setClickable(false);
        Button listWords = (Button) findViewById(R.id.listWords);
        listWords.setEnabled(false);

        listView.setSmoothScrollbarEnabled(true);
        String[] tempMessage = {"Loading Dictionary"};
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tempMessage));
    }


    /**
     * Adds the "list words" button's behavior
     */
    public void addButtonBehavior()
    {
        final Button listWords = (Button) findViewById(R.id.listWords);
        listWords.setEnabled(true);
        ready = true;

        listWords.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        updateWords();
                        imm.hideSoftInputFromWindow(lettersText.getWindowToken(), 0);
                        listWords.setEnabled(false);
                    }
                }
        );
    }



    /**
     * Asks for the word list from the dictionary.
     */
    public void updateWords()
    {
        String[] tempMessage = {"Searching Dictionary"};
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tempMessage));
        dictionary.processString(lettersText.getText().toString());
    }


    @Override
    /**
     * Required callback from Anagram, takes the array of scrabble-words, and displays them.
     * @param foundStrings The list of strings found from AnagramLibrary
     */
    public void callbackAnagram(ScrabbleWord[] foundStrings)
    {
        wordList = foundStrings;
        String[] stringArr = new String[wordList.length];

        for (int i = 0; i < wordList.length; i++)
        {
            stringArr[i] = wordList[i].toString();
        }

        if (stringArr.length == 0)
        {
            stringArr = new String[1];
            stringArr[0] = "Word not found. Point value of hand is "
                    + (ScrabbleWord.findScore(lettersText.getText().toString()))
                    + ".";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, stringArr);

        listView.setAdapter(adapter);
        addButtonBehavior();
    }

    @Override
    /**
     * Required callback from Anagram, Anagram calls us when its dictionary is ready.
     */
    public void stringsAreReady()
    {
        String[] tempMessage = {"Dictionary Ready"};
        listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tempMessage));
        addButtonBehavior();
    }
}