package game.gamehelper.Scrabble;

import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;

import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;

import android.widget.TextView;


import game.gamehelper.ConfirmationFragment;
import game.gamehelper.DominoMT.OptionPickerFragment;
import game.gamehelper.R;

/**
 * Created by Jacob on 4/15/2015.
 * Most of the code by Mark (copy-pasted in).
 */
public class ScrabbleWindow extends ActionBarActivity
{

    private GridView listView;
    private TextView lettersText;
    private TextView titleText;

    private ScrabbleWord[] wordList;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_window);


        lettersText = (TextView) findViewById(R.id.letters);
        titleText = (TextView) findViewById(R.id.titleText);
        listView = (GridView) findViewById(R.id.gridViewMain);
        listView.setNumColumns(getResources().getConfiguration().orientation);


        titleText.setClickable(false);
        addButtonBehavior();

        listView.setSmoothScrollbarEnabled(true);



    }



    public void addButtonBehavior()
    {
        Button listWords = (Button) findViewById(R.id.listWords);

        listWords.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        updateWords();
                    }
                }
        );


    }


    public void updateWords()
    {
        AnagramLibrary AL = new AnagramLibrary();
        wordList = AL.getWordArray(lettersText.toString());

        String[] stringArr = new String[wordList.length];

        for(int i = 0; i < wordList.length; i++)
        {
            stringArr[i] = wordList[i].word + " " + wordList[i].score;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, stringArr);

        listView.setAdapter(adapter);
    }



}