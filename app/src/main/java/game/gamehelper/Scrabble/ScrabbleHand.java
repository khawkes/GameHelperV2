package game.gamehelper.Scrabble;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import game.gamehelper.DominoMT.Domino;
import game.gamehelper.DominoMT.DominoRun;
import game.gamehelper.Hand;

/**
 * Created by Jacob on 4/15/2015.
 */
public class ScrabbleHand implements Hand, Parcelable
{
    private ArrayList<ScrabbleLetter> currentHand;
    private ScrabbleLetter playoff;
    private boolean isCurrent;
    private ScrabbleWord longestWord;
    private ScrabbleWord mostPointWord;
    private AnagramLibrary Worker;
    private int score;
    private HandInterface h;

    public interface HandInterface
    {
        public void updateUI();
    }

    public ScrabbleHand(HandInterface window)
    {
        currentHand = new ArrayList<>();
        playoff = new ScrabbleLetter(' ');
        isCurrent = true;
        longestWord = new ScrabbleWord("");
        mostPointWord = new ScrabbleWord("");
        score = 0;
        h = window;
        Worker = new AnagramLibrary(h);
    }

    public ScrabbleHand(char playoffLetter, HandInterface window)
    {
        currentHand = new ArrayList<>();
        playoff = new ScrabbleLetter(playoffLetter);
        isCurrent = true;
        longestWord = new ScrabbleWord("");
        mostPointWord = new ScrabbleWord("");
        score = 0;
        h = window;
        Worker = new AnagramLibrary(h);
    }

    @Override
    public int getTotalPointsHand()
    {
        return score;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {

    }

    public void setPlayoffLetter(ScrabbleLetter l)
    {
        playoff = l;
    }

    public void letterPlayed(int position, ScrabbleWindow.WindowContext context)
    {
        score -= currentHand.get(position).getPointVal();
        currentHand.remove(position);
        isCurrent = false;
    }

    public void addLetter(ScrabbleLetter l)
    {
        score += l.getPointVal();
        currentHand.add(l);
        isCurrent = false;
    }

    public ScrabbleLetter[] findUnused(ScrabbleWord compareAgainst)
    {
        return new ScrabbleLetter[0];
    }

    public ScrabbleLetter getPlayoffLetter()
    {
        return playoff;
    }

    public ScrabbleWord getMostPointWord()
    {
        if (isCurrent)
        {
            return mostPointWord;
        }
        recalculate();
        //return getMostPointWord();
        return new ScrabbleWord("");
    }

    public ScrabbleWord getLongestWord()
    {
        if (isCurrent)
        {
            return longestWord;
        }
        recalculate();
        //return getLongestWord();
        return new ScrabbleWord("");
    }

    private void recalculate()
    {
        ArrayList<ScrabbleLetter> copy = new ArrayList<>();
        copy.addAll(currentHand);
        copy.add(playoff);
        if (Worker.getStatus() != AsyncTask.Status.RUNNING)
            Worker.execute(copy);
    }

    private void recalculateCallback(ArrayList<ScrabbleWord> bestWords)
    {
        //ensures we get the best most point word and the best longest word.
        Collections.sort(bestWords, new ScrabbleWord.compareByLength());
        Collections.sort(bestWords, new ScrabbleWord.compareByScore());
        mostPointWord = bestWords.get(bestWords.size() - 1);

        //we have to sort again, since sorting is stable, this will get the best one.
        Collections.sort(bestWords, new ScrabbleWord.compareByLength());
        longestWord = bestWords.get(bestWords.size() - 1);
        isCurrent = true;
        h.updateUI();
    }

    public boolean undo()
    {
        return true;
    }

    public int getTotalLetters()
    {
        return currentHand.size();
    }

    public ScrabbleLetter[] toArray()
    {
        return currentHand.toArray(new ScrabbleLetter[currentHand.size()]);
    }

    public class AnagramLibrary extends AsyncTask<ArrayList<ScrabbleLetter>, Void, ArrayList<ScrabbleWord>>
    {
        HandInterface myActivity;

        public AnagramLibrary(HandInterface active)
        {
            myActivity = active;
        }

        @Override
        protected ArrayList<ScrabbleWord> doInBackground(ArrayList<ScrabbleLetter>... letters)
        {
            //create the url for request
            String urlString = "http://www.anagramica.com/all/:";
            for (ScrabbleLetter l : letters[0])
            {
                if (l.getChar() != ' ')
                    urlString += l.getChar();
            }

            System.err.println(urlString);

            ArrayList<ScrabbleWord> wordList = new ArrayList<>();

            try
            {
                URL url = new URL(urlString);
                //connect to the url
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.connect();
                BufferedReader bReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //compile the response in to a string
                String temp, response = "";
                while ((temp = bReader.readLine()) != null)
                {
                    response += temp;
                }

                //close reader and connection
                bReader.close();
                connection.disconnect();

                //creat a JSON object from the url response
                JSONObject jObject = new JSONObject(response);

                //get the array from that object
                JSONArray jArray = jObject.getJSONArray("all");

                //add all the JSON array objects bigger then length 1 to a list
                for (int i = 0; i < jArray.length(); i++)
                {
                    String word = (String) jArray.get(i);
                    if (word.length() > 1)
                    {
                        ScrabbleWord sWord = new ScrabbleWord(word);
                        wordList.add(sWord);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            //return the list as an ArrayList
            return wordList;
        }

        @Override
        protected void onPostExecute(ArrayList<ScrabbleWord> scrabbleWords)
        {
            if (myActivity != null)
                recalculateCallback(scrabbleWords);
        }
    }
}
