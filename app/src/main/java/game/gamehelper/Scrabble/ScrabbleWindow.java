package game.gamehelper.Scrabble;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import game.gamehelper.ConfirmationFragment;
import game.gamehelper.DominoMT.NewGameMT;
import game.gamehelper.DominoMT.OptionPickerFragment;
import game.gamehelper.GameSet;
import game.gamehelper.MainActivity;
import game.gamehelper.R;

/**
 * Created by Jacob on 4/15/2015.
 * Most of the code by Mark (copy-pasted in).
 */
public class ScrabbleWindow extends ActionBarActivity implements
        OptionPickerFragment.OptionPickerListener,
        ConfirmationFragment.ConfirmationListener,
        LetterSelectFragment.LetterSelectListener,
        LeadOffLetterSelect.LeadOffListener,
        AdapterView.OnItemClickListener,
        LetterSelectRepeatFragment.LetterSelectRepeatListener
{
    private ScrabbleHand hand;
    private GridView listView;
    private ImageView playoffLetterImage;
    private TextView pointValText;
    private TextView titleText;
    private LetterAdapter adapter;
    private Bundle scoreHistory = new Bundle();
    private ArrayList<GameSet> setList = new ArrayList<GameSet>();
    private ArrayList<String> playerList = new ArrayList<String>();
    private ScrabbleLetter playoffLetter;

    /**
     * @param handInformation keys:
     * "letterList" : serializable, char[] representing hand
     * "letterTotal" : int, total number of dominoes in array
     * "playoffHead" : int, current playoff head
     * "windowState" : WindowContext, current hand displayed
     * "setList" : parcelableArrayList, for set history
     * "playerList" : stringArrayList, for player names
     * "players" : int, total players
     * "rules" : int, temp; no current use
     * booleans : "loadGame", "gameTypeSelected", "playoffHeadSelected", "gameStarted"
     */
    private Bundle handInformation;
    private char[] letterList = new char[100];
    private int letterTotal = 0;
    ScrabbleLetter[] data = new ScrabbleLetter[0];
    private int rules;
    private int players;

    /**
     * @param loadGame new game selected, deck size/players dialog has been created
     * @param gameTypeSelected deck/players selected, train head dialog has been created
     * @param playoffLetterSelected train head selected, camera dialog has been created
     */
    private boolean loadGame = false;
    private boolean gameTypeSelected = false;
    private boolean playoffLetterSelected = false;
    private boolean debugMode = false;
    static final int SCOREBOARD_EXIT = 10;

    /**
     * Context of a play. Whether we played on the longest, the most points, or the unsorted screen.
     */
    public enum WindowContext
    {
        SHOWING_LONGEST,
        SHOWING_MOST_POINTS,
        SHOWING_UNSORTED,
        SHOWING_UNUSED
    }

    public enum LastRunTypeShown
    {
        SHOWING_LONGEST,
        SHOWING_MOST_POINTS
    }

    private WindowContext windowState;
    private LastRunTypeShown lastWindowState;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_window);
        handInformation = getIntent().getExtras();
        windowState = WindowContext.SHOWING_UNSORTED;

        pointValText = (TextView) findViewById(R.id.remPoint);
        titleText = (TextView) findViewById(R.id.titleText);
        listView = (GridView) findViewById(R.id.gridViewMain);
        listView.setNumColumns(getResources().getConfiguration().orientation);
        playoffLetterImage = (ImageView) findViewById(R.id.imageView2);

        pointValText.setClickable(false);
        titleText.setClickable(false);
        addButtonBehavior();

        listView.setSmoothScrollbarEnabled(true);
        listView.setOnItemClickListener(this);

        if (handInformation != null)
            loadInformation();

        if (loadGame)
            return;

        newGame();
    }

    private void updatePointValueText()
    {
        if (windowState == WindowContext.SHOWING_LONGEST)
        {
            titleText.setText("Longest Run");
            pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getLongestWord().getPointVal())
                    + " (" + (hand.getTotalLetters() - hand.getLongestWord().getLength()) + ")");
        }
        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
        {
            titleText.setText("Highest Scoring Run");
            pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getMostPointWord().getPointVal())
                    + " (" + (hand.getTotalLetters() - hand.getMostPointWord().getLength()) + ")");
        }
        else if (windowState == WindowContext.SHOWING_UNSORTED)
        {
            titleText.setText("Unsorted Hand");
            pointValText.setText("Value: " + (hand.getTotalPointsHand())
                    + " (" + (hand.getTotalLetters())
                    + " domino" + ((hand.getTotalLetters() == 1) ? ")" : "s)"));
        }
        else if (windowState == WindowContext.SHOWING_UNUSED)
        {
            titleText.setText("Unused Dominos");

            if (lastWindowState == LastRunTypeShown.SHOWING_LONGEST)
            {
                pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getLongestWord().getPointVal())
                        + " (" + (hand.getTotalLetters() - hand.getLongestWord().getLength()) + ")");
            }
            else if (lastWindowState == LastRunTypeShown.SHOWING_MOST_POINTS)
            {
                pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getMostPointWord().getPointVal())
                        + " (" + (hand.getTotalLetters() - hand.getMostPointWord().getLength()) + ")");
            }
        }
    }
    
    public void addButtonBehavior()
    {
        Button longestRun = (Button) findViewById(R.id.longestRunButton);
        Button highestScore = (Button) findViewById(R.id.highestScoreButton);
        Button draw = (Button) findViewById(R.id.drawButton);
        Button unsorted = (Button) findViewById(R.id.unsortedButton);
        Button undo = (Button) findViewById(R.id.undoButton);
        Button unused = (Button) findViewById(R.id.unusedButton);

        //Train head image behavior
        playoffLetterImage.setOnClickListener(
                new ImageView.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DialogFragment endSelect = new LeadOffLetterSelect();
                        endSelect.setArguments(handInformation);
                        endSelect.show(getSupportFragmentManager(), "Select_End");
                    }
                }
        );

        //longest run click handler
        longestRun.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        windowState = WindowContext.SHOWING_LONGEST;
                        lastWindowState = LastRunTypeShown.SHOWING_LONGEST;
                        updateUI();
                    }
                }
        );

        //highest score click handler
        highestScore.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        windowState = WindowContext.SHOWING_MOST_POINTS;
                        lastWindowState = LastRunTypeShown.SHOWING_MOST_POINTS;
                        updateUI();
                    }
                }
        );

        //draw click handler
        draw.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        if (hand != null && hand.getTotalLetters() > 0)
                        {
                            DialogFragment newFragment = new LetterSelectFragment();
                            newFragment.setArguments(handInformation);
                            newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
                        }
                        else
                        {
                            DialogFragment newFragment = new LetterSelectRepeatFragment();
                            newFragment.setArguments(handInformation);
                            newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
                        }
                    }
                }
        );

        //unsorted click handler
        unsorted.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        windowState = WindowContext.SHOWING_UNSORTED;
                        updateUI();
                    }
                }
        );

        //undo click handler
        undo.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        //do nothing if nothing to undo
                        if (!hand.undo())
                            return;

                        //update the pictures & score shown based on our current context
                        if (windowState == WindowContext.SHOWING_LONGEST)
                        {
                            data = hand.getLongestWord().toArray();
                        }
                        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
                        {
                            data = hand.getMostPointWord().toArray();
                        }
                        else if (windowState == WindowContext.SHOWING_UNSORTED)
                        {
                            data = hand.toArray();
                        }

                        updatePointValueText();

                        adapter.clear();
                        adapter = new LetterAdapter(v.getContext(), R.layout.hand_display_grid, data);
                        listView.setAdapter(adapter);

                        //re-sets the old train head.
                        playoffLetterImage.setImageBitmap(ScrabbleLetter.getLetterPic(hand.getPlayoffLetter(),
                                getApplicationContext()));
                    }
                }
        );


        unused.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        windowState = WindowContext.SHOWING_UNUSED;
                        updateUI();
                    }
                }
        );

    }

    public void newGame()
    {
        //initiate data and settings for new game
        scoreHistory.clear();
        setList.clear();
        playerList.clear();
        loadGame = true;
        if (!gameTypeSelected)
        {
            newSet(' ');
            Bundle bundle = new Bundle();
            bundle.putInt("option", NewGameMT.PLAYER_SELECT_OPTION);
            DialogFragment fragment = new OptionPickerFragment();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "optionPicker");
        }
        //on positive button click, new game continued in the method onNewGameCreate()
    }

    public void newSet(char playoffLetter)
    {
        hand = new ScrabbleHand(playoffLetter);
        data = hand.toArray();
        updateUI();
    }

    @Override
    public void onDialogPositiveClick(String tag)
    {
        //behavior for confirmation fragment (new game/ end set)
        if (tag.compareTo(getString(R.string.newGame)) == 0)
        {
            //clear data and start new set
            loadGame = false;
            gameTypeSelected = false;
            playoffLetterSelected = false;
            letterTotal = 0;
            saveInformation();
            newGame();
        }
        else if (tag.compareTo(getString(R.string.endSet)) == 0)
        {
            //create gameset from hand and add to scoreboard
            playoffLetterSelected = false;
            GameSet newSet = new GameSet(hand);
            letterTotal = 0;

            DialogFragment fragment = new LeadOffLetterSelect();
            fragment.setArguments(handInformation);
            fragment.show(getSupportFragmentManager(), getString(R.string.endSelect));

            //add rows for all current players
            for (int i = 1; i < playerList.size(); i++)
            {
                newSet.addPlayer();
            }
            setList.add(newSet);
            newSet(' ');
        }
        else if (tag.compareTo(getString(R.string.startCamera)) == 0)
        {
            //camera was called on new game
        }

    }

    @Override
    public void onDialogNegativeClick(String tag)
    {
        //behavior for confirmation fragment negative button
        if (tag.compareTo(getString(R.string.startCamera)) == 0)
        {
            //camera was cancelled, add in hand manually.
            DialogFragment newFragment = new LetterSelectRepeatFragment();
            newFragment.setArguments(handInformation);
            newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
        }
    }

    @Override
    public void callbackLetterSelect(ScrabbleLetter l)
    {
        //From draw button, add to hand.
        loadGame = true;
        gameTypeSelected = true;
        playoffLetterSelected = true;
        hand.addLetter(l);

        updateUI();
    }

    @Override
    public void callbackLetterRepeatSelect(ScrabbleLetter l)
    {
        //adds from repeating draw button.
        loadGame = true;
        gameTypeSelected = true;
        playoffLetterSelected = true;
        hand.addLetter(l);

        updateUI();
        //call repeat again!
        DialogFragment newFragment = new LetterSelectRepeatFragment();
        newFragment.setArguments(handInformation);
        newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
    }

    @Override
    public void callbackLeadOff(ScrabbleLetter l)
    {
        //From end piece select, replace largest double value in hand

        playoffLetter = l;

        hand.setPlayoffLetter(playoffLetter);

        updateUI();

        //call for camera on new game
        if (!playoffLetterSelected && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            Bundle b = new Bundle();
            b.putString("mainText", getString(R.string.startCameraText));
            b.putString("positive", getString(R.string.yes));
            b.putString("negative", getString(R.string.cancel));
            b.putString("callName", getString(R.string.startCamera));
            DialogFragment fragment = new ConfirmationFragment();
            fragment.setArguments(b);
            fragment.show(getSupportFragmentManager(), getString(R.string.startCamera));
            //positive button click will continue in the method onActivityResult()
        }
        playoffLetterSelected = true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Bundle b;

        if (requestCode == SCOREBOARD_EXIT)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
            {
                this.finish();
            }
        }


        if (resultCode != RESULT_OK)
            return;
        switch (requestCode)
        {
            case 0:
                //data returned from scoreboard
                b = data.getExtras();

                if (b != null)
                {
                    setList.clear();
                    setList = b.getParcelableArrayList("setList");
                    playerList.clear();
                    playerList = b.getStringArrayList("playerList");
                }
                return;

            default:
                //other
                return;
        }
    }

    //When each domino is clicked, we delete it from the hand, and update the pictures.
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.w("GameWindow", "Clicked " + position);
        hand.letterPlayed(position, windowState);
        updateUI();
    }

    private void updateUI()
    {
        //update the screen to reflect changes to hand

        //update the pictures & score shown based on our current context
        if (windowState == WindowContext.SHOWING_LONGEST)
        {
            data = hand.getLongestWord().toArray();
        }
        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
        {
            data = hand.getMostPointWord().toArray();
        }
        else if (windowState == WindowContext.SHOWING_UNSORTED)
        {
            data = hand.toArray();
        }
        else if (windowState == WindowContext.SHOWING_UNUSED)
        {
            if (lastWindowState == LastRunTypeShown.SHOWING_LONGEST)
            {
                data = hand.findUnused(hand.getLongestWord());
            }
            else if (lastWindowState == LastRunTypeShown.SHOWING_MOST_POINTS)
            {
                data = hand.findUnused(hand.getMostPointWord());
            }
        }

        updatePointValueText();

        //update the picture to our data array of dominoes.
        if (adapter == null)
            adapter = new LetterAdapter(this, R.layout.hand_display_grid, this.data);
        else
            adapter.changeData(data);
        listView.setAdapter(adapter);

        //update the train head's image
        playoffLetterImage.setImageBitmap(ScrabbleLetter.getLetterPic(hand.getPlayoffLetter(), getApplicationContext()));
    }
/*
    @Override
    public void onNewGameCreate(int player)
    {
        gameTypeSelected = true;
        this.players = player;

        if (!playoffLetterSelected)
        {
            for (int i = 1; i <= player; i++)
            {
                playerList.add("Player " + i);
            }

            DialogFragment fragment = new LeadOffLetterSelect();
            fragment.setArguments(handInformation);
            fragment.setCancelable(false);
            fragment.show(getSupportFragmentManager(), getString(R.string.letterSelect));
            //On train head select, new game creation continued at method onClose() for train head select
        }
    }

    @Override
    public void onNewGameCancel()
    {
        //Create game with default settings if user cancels new game settings window

        loadGame = true;
        gameTypeSelected = true;
        playoffLetterSelected = true;
        this.rules = 0;
        this.players = 1;

        for (int i = 1; i <= players; i++)
        {
            playerList.add("Player " + i);
        }

        newSet(' ');
        playoffLetter = new ScrabbleLetter(' ');
        hand.setPlayoffLetter(playoffLetter);
        updateUI();

        //calls hand creation repeater
        DialogFragment newFragment = new LetterSelectRepeatFragment();
        newFragment.setArguments(handInformation);
        newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
    }
*/
    private void convertSerializable(Bundle b)
    {
        Object[] object = (Object[]) b.getSerializable("letterList");
        if (object == null || object.length == 0) return;

        int i = 0;
        for (Object o : object)
        {
            letterList[i++] = (char) o;
        }
    }

    private void saveInformation()
    {
        //save information to bundle
        handInformation.putParcelable("hand", hand);
        handInformation.putInt("players", players);
        handInformation.putInt("rules", rules);
        handInformation.putBoolean("loadGame", loadGame);
        handInformation.putBoolean("gameTypeSelected", gameTypeSelected);
        handInformation.putBoolean("playoffLetterSelected", playoffLetterSelected);
        handInformation.putParcelableArrayList("setList", setList);
        handInformation.putStringArrayList("playerList", playerList);
        handInformation.putSerializable("windowState", windowState);
    }

    private void loadInformation()
    {
        //load information
        Boolean _debugMode = handInformation.getBoolean("debug");
        if (_debugMode != null) debugMode = _debugMode;

        hand = handInformation.getParcelable("hand");
        if (hand != null)
        {
            letterTotal = handInformation.getInt("letterTotal");
            convertSerializable(handInformation);
        }
        loadGame = handInformation.getBoolean("loadGame");
        gameTypeSelected = handInformation.getBoolean("gameTypeSelected");
        playoffLetterSelected = handInformation.getBoolean("playoffLetterSelected");
        rules = handInformation.getInt("rules");
        players = handInformation.getInt("players");

        //set/player data selected, train head not yet selected
        if (loadGame)
        {
            setList = handInformation.getParcelableArrayList("setList");
            playerList = handInformation.getStringArrayList("playerList");
            data = hand.toArray();
        }

        if (playoffLetterSelected)
        {
            playoffLetter = hand.getPlayoffLetter();
            windowState = (WindowContext) handInformation.getSerializable("windowState");
            updateUI();
        }
    }

    @Override
    protected void onDestroy()
    {
        saveInformation();
        getIntent().putExtras(handInformation);
        adapter.clear();
        super.onDestroy();
    }

    @Override
    public void setOption(int option, int caller)
    {
        //opens dialog within new game window for selecting values for each option
        Bundle bundle = new Bundle();
        bundle.putInt("option", NewGameMT.PLAYER_SELECT_OPTION);
        DialogFragment fragment = new OptionPickerFragment();
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "optionPicker");
    }
}
