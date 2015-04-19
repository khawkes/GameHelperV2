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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import game.gamehelper.ConfirmationFragment;
import game.gamehelper.GameSet;
import game.gamehelper.MainActivity;
import game.gamehelper.R;
import game.gamehelper.RuleDetailActivity;
import game.gamehelper.ScoreBoard;

/**
 * Window containing a visual representation of a hand, with options
 * to change arrangement
 */

public class GameWindowMT extends ActionBarActivity implements
        ConfirmationFragment.ConfirmationListener,
        DrawDominoListener,
        EndSelectFragment.EndListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        NewGameMT.NewGameListener,
        OptionPickerFragment.OptionPickerListener
{

    private static int DOUBLE_NINE = 9;
    private static int DOUBLE_TWELVE = 12;
    private static int DOUBLE_FIFTEEN = 15;
    private static int DOUBLE_EIGHTEEN = 18;
    private static int DEFAULT_SET = 12;

    private HandMT hand;
    private GridView listView;
    private ImageView trainHeadImage;
    private TextView pointValText;
    private TextView titleText;
    private DominoAdapter adapter;
    private Bundle scoreHistory = new Bundle();
    private ArrayList<GameSet> setList = new ArrayList<GameSet>();
    private ArrayList<String> playerList = new ArrayList<String>();
    private int trainHead = 0;
    private int originalStartHead;

    /**
     * @param handInformation keys:
     * "dominoList" : serializable, int[][] representing hand
     * "dominoTotal" : int, total number of dominoes in array
     * "maxDouble" : int, highest possible domino in set
     * "originalStartHead" : int, the original starting train head (used for other rule versions)
     * "trainHead" : int, current train head
     * "windowState" : WindowContext, current hand displayed
     * "setList" : parcelableArrayList, for set history
     * "playerList" : stringArrayList, for player names
     * "players" : int, total players
     * "rules" : int, temp; no current use
     * "trainHead" : int, train head
     * booleans : "loadGame", "gameTypeSelected", "trainHeadSelected", "gameStarted"
     */
    private Bundle handInformation;
    private int[][] dominoList = new int[100][2];
    private int dominoTotal = 0;
    Domino[] data = new Domino[0];
    private int maxDouble = 0;
    private int rules;
    private int players;

    /**
     * @param loadGame new game selected, deck size/players dialog has been created
     * @param gameTypeSelected deck/players selected, train head dialog has been created
     * @param trainHeadSelected train head selected, camera dialog has been created
     */
    private boolean loadGame = false;
    private boolean gameTypeSelected = false;
    private boolean trainHeadSelected = false;

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
        SHOWING_UNUSED_LONGEST,
        SHOWING_UNUSED_MOST_POINTS
    }

    private WindowContext windowState;

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
        trainHeadImage = (ImageView) findViewById(R.id.imageView2);

        pointValText.setClickable(false);
        titleText.setClickable(false);
        addButtonBehavior();

        listView.setLongClickable(true);
        listView.setSmoothScrollbarEnabled(true);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        if (handInformation != null)
            loadInformation();

        if (loadGame)
            return;

        if (debugMode)
        {
            newGameDebug();
            return;
        }

        newGame();
    }

    //Creates hand from camera domino array
    public void createHand()
    {

        hand = new HandMT(dominoList, dominoTotal, maxDouble, originalStartHead);
        hand.setTrainHead(trainHead);

        //create domino array for adapter, set pointValText and image to corresponding values
        Domino temp[] = hand.toArray();

        data = new Domino[(temp.length < DominoPlugin.MAX_DOMINO_DISPLAY) ? temp.length : DominoPlugin.MAX_DOMINO_DISPLAY];

        //generate bitmaps for hand
        for (int i = 0; i < data.length; i++)
        {
            data[i] = temp[i];
        }

        trainHeadImage.setImageBitmap(Domino.getSide(hand.getTrainHead(), getApplicationContext()));
        updatePointValueText();
    }

    private void updatePointValueText()
    {
        if (windowState == WindowContext.SHOWING_LONGEST)
        {
            titleText.setText("Longest Run");
            pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getLongestRun().getPointVal())
                    + " (" + (hand.getTotalDominos() - hand.getLongestRun().getLength()) + ")");
        }
        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
        {
            titleText.setText("Highest Scoring Run");
            pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getMostPointRun().getPointVal())
                    + " (" + (hand.getTotalDominos() - hand.getMostPointRun().getLength()) + ")");
        }
        else if (windowState == WindowContext.SHOWING_UNSORTED)
        {
            titleText.setText("Unsorted Hand");
            pointValText.setText("Value: " + (hand.getTotalPointsHand())
                    + " (" + (hand.getTotalDominos())
                    + " domino" + ((hand.getTotalDominos() == 1) ? ")" : "s)"));
        }
        else if (windowState == WindowContext.SHOWING_UNUSED_LONGEST)
        {
            titleText.setText("Unused Dominos");
            pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getLongestRun().getPointVal())
                    + " (" + (hand.getTotalDominos() - hand.getLongestRun().getLength()) + ")");
        }
        else if (windowState == WindowContext.SHOWING_UNUSED_MOST_POINTS)
        {
            pointValText.setText("Junk Value: " + (hand.getTotalPointsHand() - hand.getMostPointRun().getPointVal())
                    + " (" + (hand.getTotalDominos() - hand.getMostPointRun().getLength()) + ")");
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
        trainHeadImage.setOnClickListener(
                new ImageView.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        DialogFragment endSelect = new EndSelectFragment();
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
                        if (hand != null && hand.getTotalDominos() > 0)
                        {
                            DialogFragment newFragment = new DrawFragment();
                            newFragment.setArguments(handInformation);
                            newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
                        }
                        else
                        {
                            DialogFragment newFragment = new DrawRepeatFragment();
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
                            data = hand.getLongestRun().toArray();
                        }
                        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
                        {
                            data = hand.getMostPointRun().toArray();
                        }
                        else if (windowState == WindowContext.SHOWING_UNSORTED)
                        {
                            data = hand.toArray();
                        }

                        updatePointValueText();

                        adapter.clear();
                        adapter = new DominoAdapter(v.getContext(), R.layout.hand_display_grid, data);
                        listView.setAdapter(adapter);

                        //re-sets the old train head.
                        trainHeadImage.setImageBitmap(Domino.getSide(hand.getTrainHead(), getApplicationContext()));
                    }
                }
        );


        unused.setOnClickListener(
                new Button.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        if (windowState == WindowContext.SHOWING_LONGEST)
                            windowState = WindowContext.SHOWING_UNUSED_LONGEST;
                        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
                            windowState = WindowContext.SHOWING_UNUSED_MOST_POINTS;
                        updateUI();
                    }
                }
        );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_game_window, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        DialogFragment newFragment;
        Bundle b;

        switch (item.getItemId())
        {
            case R.id.action_new_game:
                b = new Bundle();
                b.putString("positive", getString(R.string.txtDlgYes));
                b.putString("negative", getString(R.string.txtDlgCancel));
                b.putString("mainText", getString(R.string.newGameText));
                b.putString("callName", getString(R.string.newGame));
                newFragment = new ConfirmationFragment();
                newFragment.setArguments(b);
                newFragment.show(getSupportFragmentManager(), getString(R.string.newGame));

                break;

            case R.id.action_score_board:
                //display score board
                scoreHistory.clear();
                scoreHistory.putSerializable("setList", setList);
                scoreHistory.putStringArrayList("playerList", playerList);
                startActivityForResult(new Intent(GameWindowMT.this, ScoreBoard.class).putExtras(scoreHistory), 0);
//                startActivity(new Intent(GameWindow.this, ScoreBoard.class).putExtras(scoreHistory));

                break;

            case R.id.action_end_round:
                //write to scoreboard and wipe hand
                b = new Bundle();
                b.putString("positive", getString(R.string.txtDlgYes));
                b.putString("negative", getString(R.string.txtDlgCancel));
                b.putString("mainText", getString(R.string.endSetText));
                b.putString("callName", getString(R.string.endSet));
                newFragment = new ConfirmationFragment();
                newFragment.setArguments(b);
                newFragment.show(getSupportFragmentManager(), getString(R.string.endSet));

                break;


            case R.id.action_camera:
                //camera call, overwrite hand
                startActivityForResult(new Intent(GameWindowMT.this, MainActivity.class), 1);

                break;

            case R.id.menu_rules:
            {
                DominoPlugin plugin = new DominoPlugin();
                Bundle bundle = new Bundle();
                for (Map.Entry<String, Integer> ids : plugin.getRulesIDs().entrySet())
                {
                    bundle.putInt(ids.getKey(), ids.getValue());
                }

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
                //TODO perform other

                break;
        }

        return true;
    }


    public void newGameDebug()
    {
        loadGame = true;
        gameTypeSelected = true;
        trainHeadSelected = true;

        //new game used when randomly generating from title screen
        if (handInformation != null)
        {
            if (handInformation.getInt("dominoTotal") != 0)
            {
                createHand();
            }
            else
            {
                maxDouble = handInformation.getInt("maxDouble");
                originalStartHead = handInformation.getInt("originalStartHead");
                hand = new HandMT(maxDouble, originalStartHead);
                data = hand.toArray();
                updatePointValueText();
            }
        }
        if (playerList.size() == 0)
        {
            playerList.add("Player 1");
        }
        createHand();
        updateUI();

    }

    public void newGame()
    {
        //initiate data and settings for new game
        scoreHistory.clear();
        setList.clear();
        playerList.clear();
        maxDouble = 0;
        originalStartHead = -1;
        loadGame = true;
        if (!gameTypeSelected)
        {
            newSet(0);
            DialogFragment newGame = new NewGameMT();
            newGame.setCancelable(false);
            newGame.show(getSupportFragmentManager(), getString(R.string.newGameMT));
        }
        //on positive button click, new game continued in the method onNewGameCreate()
    }

    public void newSet(int maxTrainHead)
    {
        hand = new HandMT(maxDouble, maxTrainHead);
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
            trainHeadSelected = false;
            dominoTotal = 0;
            saveInformation();
            newGame();
        }
        else if (tag.compareTo(getString(R.string.endSet)) == 0)
        {
            //create gameset from hand and add to scoreboard
            trainHeadSelected = false;
            GameSet newSet = new GameSet(hand);
            dominoTotal = 0;

            DialogFragment fragment = new EndSelectFragment();
            fragment.setArguments(handInformation);
            fragment.show(getSupportFragmentManager(), getString(R.string.endSelect));

            //add rows for all current players
            for (int i = 1; i < playerList.size(); i++)
            {
                newSet.addPlayer();
            }
            setList.add(newSet);
            newSet(0);
        }
        else if (tag.compareTo(getString(R.string.startCamera)) == 0)
        {
            //camera was called on new game
            startActivityForResult(new Intent(GameWindowMT.this, MainActivity.class), 1);

        }

    }

    @Override
    public void onDialogNegativeClick(String tag)
    {
        //behavior for confirmation fragment negative button
        if (getString(R.string.startCamera).equals(tag))
        {
            //camera was cancelled, add in hand manually.
            DialogFragment newFragment = new DrawRepeatFragment();
            newFragment.setArguments(handInformation);
            newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
        }
    }

    @Override
    public void onDrawClose(Domino overwrite, Domino added)
    {
        loadGame = true;
        gameTypeSelected = true;
        trainHeadSelected = true;
        if (overwrite == null) hand.addDomino(added);
        else hand.replaceDomino(overwrite, added);

        handInformation.remove("overwrite");
        updateUI();
    }

    @Override
    public void onDrawRepeatClose(Domino d)
    {
        //adds from repeating draw button.
        loadGame = true;
        gameTypeSelected = true;
        trainHeadSelected = true;
        hand.addDomino(d);

        updateUI();

        //call repeat again!
        DialogFragment newFragment = new DrawRepeatFragment();
        newFragment.setArguments(handInformation);
        newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
    }

    @Override
    public void onClose(int var1)
    {
        //From end piece select, replace largest double value in hand

        trainHead = var1;
        //in the case we're starting a hand
        if (originalStartHead == -1)
        {
            originalStartHead = var1;
            newSet(originalStartHead);
        }

        hand.setTrainHead(trainHead);

        updateUI();

        //call for camera on new game
        if (!trainHeadSelected && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            Bundle b = new Bundle();
            b.putString("mainText", getString(R.string.startCameraText));
            b.putString("positive", getString(R.string.txtDlgYes));
            b.putString("negative", getString(R.string.txtDlgNo));
            b.putString("callName", getString(R.string.startCamera));
            DialogFragment fragment = new ConfirmationFragment();
            fragment.setArguments(b);
            fragment.show(getSupportFragmentManager(), getString(R.string.startCamera));
            //positive button click will continue in the method onActivityResult()
        }
        trainHeadSelected = true;
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
                b = (data != null) ? data.getExtras() : null;

                if (b != null)
                {
                    setList.clear();
                    setList = b.getParcelableArrayList("setList");
                    playerList.clear();
                    playerList = b.getStringArrayList("playerList");
                }
                return;

            case 1:
                //Data from camera
                b = data.getExtras();

                if (b != null)
                {
                    //read from camera information
                    dominoTotal = b.getInt("dominoTotal");
                    dominoList = new int[dominoTotal][2];
                    convertSerializable(b);

                    createHand();
                    this.data = hand.toArray();
                    saveInformation();
                    updateUI();
                    return;
                }

                Log.w("MainWindow", "No camera data found");
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
        hand.dominoPlayed(position, windowState);
        updateUI();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Log.w("GameWindow", "Long Clicked " + position);

        Domino sel = hand.getDomino(position, windowState);
        handInformation.putParcelable("overwrite", sel);

        DialogFragment newFragment = new DrawFragment();
        newFragment.setArguments(handInformation);
        newFragment.show(getSupportFragmentManager(), getString(R.string.draw));
        return true;
    }

    private void updateUI()
    {
        //update the screen to reflect changes to hand

        //update the pictures & score shown based on our current context
        if (windowState == WindowContext.SHOWING_LONGEST)
        {
            data = hand.getLongestRun().toArray();
        }
        else if (windowState == WindowContext.SHOWING_MOST_POINTS)
        {
            data = hand.getMostPointRun().toArray();
        }
        else if (windowState == WindowContext.SHOWING_UNSORTED)
        {
            data = hand.toArray();
        }
        else if (windowState == WindowContext.SHOWING_UNUSED_LONGEST)
        {
            data = UnusedFinder.FindUnused(hand.getLongestRun(), hand);
        }
        else if (windowState == WindowContext.SHOWING_UNUSED_MOST_POINTS)
        {
            data = UnusedFinder.FindUnused(hand.getMostPointRun(), hand);
        }

        updatePointValueText();

        //update the picture to our data array of dominoes.
        if (adapter == null)
            adapter = new DominoAdapter(this, R.layout.hand_display_grid, this.data);
        else
            adapter.changeData(data);
        listView.setAdapter(adapter);

        //update the train head's image
        trainHeadImage.setImageBitmap(Domino.getSide(hand.getTrainHead(), getApplicationContext()));
    }

    @Override
    public void onNewGameCreate(int set, int player, int rules)
    {
        gameTypeSelected = true;
        this.rules = rules;
        this.players = player;
        switch (set)
        {
            case 0:
                maxDouble = DOUBLE_NINE;
                break;
            case 1:
                maxDouble = DOUBLE_TWELVE;
                break;
            case 2:
                maxDouble = DOUBLE_FIFTEEN;
                break;
            case 3:
                maxDouble = DOUBLE_EIGHTEEN;
                break;
            default:
                maxDouble = DOUBLE_TWELVE;
                break;
        }

        if (!trainHeadSelected)
        {
            for (int i = 1; i <= player; i++)
            {
                playerList.add("Player " + i);
            }

            handInformation.putInt("maxDouble", maxDouble);

            DialogFragment fragment = new EndSelectFragment();
            fragment.setArguments(handInformation);
            fragment.setCancelable(false);
            fragment.show(getSupportFragmentManager(), getString(R.string.endSelect));
            //On train head select, new game creation continued at method onDrawClose() for train head select
        }

    }

    @Override
    public void onNewGameCancel()
    {
        //Create game with default settings if user cancels new game settings window
        finish();
    }

    private void convertSerializable(Bundle b)
    {
        Object[] object = (Object[]) b.getSerializable("dominoList");
        if (object == null || object.length == 0) return;

        int i = 0;
        for (Object o : object)
        {
            dominoList[i][0] = ((int[]) o)[0];
            dominoList[i++][1] = ((int[]) o)[1];
        }
    }

    private void saveInformation()
    {
        //save information to bundle
        handInformation.putParcelable("hand", hand);
        handInformation.putInt("maxDouble", maxDouble);
        handInformation.putInt("originalStartHead", originalStartHead);
        handInformation.putInt("players", players);
        handInformation.putInt("rules", rules);
        handInformation.putBoolean("loadGame", loadGame);
        handInformation.putBoolean("gameTypeSelected", gameTypeSelected);
        handInformation.putBoolean("trainHeadSelected", trainHeadSelected);
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
            dominoTotal = handInformation.getInt("dominoTotal");
            convertSerializable(handInformation);
        }
        maxDouble = handInformation.getInt("maxDouble");
        originalStartHead = handInformation.getInt("originalStartHead");
        loadGame = handInformation.getBoolean("loadGame");
        gameTypeSelected = handInformation.getBoolean("gameTypeSelected");
        trainHeadSelected = handInformation.getBoolean("trainHeadSelected");
        rules = handInformation.getInt("rules");
        players = handInformation.getInt("players");

        //set/player data selected, train head not yet selected
        if (loadGame)
        {
            setList = handInformation.getParcelableArrayList("setList");
            playerList = handInformation.getStringArrayList("playerList");
            data = hand.toArray();
        }

        if (trainHeadSelected)
        {
            trainHead = hand.getTrainHead();
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
        NewGameMT fragment = (NewGameMT) getSupportFragmentManager().findFragmentByTag(getString(R.string.newGameMT));
        fragment.setOption(option, caller);
    }
}