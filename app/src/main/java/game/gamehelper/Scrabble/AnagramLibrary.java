package game.gamehelper.Scrabble;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import game.gamehelper.R;

/**
 * Created by Andrew on 4/17/2015.
 * Content by Jacob
 */
public class AnagramLibrary
{
    //allows this library to call the other library.
    public interface AnagramCallback
    {
        //to let you know that it found possible words.
        public void callbackAnagram(ScrabbleWord[] a);
        //to let you know the dictionary is ready.
        public void stringsAreReady();
    }

    public String[] stringList;
    public String wordChoice;
    public AsyncLoadStrings fileWorker;
    public AsyncFindWords stringWorker;
    public Activity parent;

    /**
     * Creates the anagram library, and starts the loading process of the dictionary.
     *
     * @param mainActivity the activity that called this library
     */
    public AnagramLibrary(Activity mainActivity)
    {
        stringList = null;
        parent = mainActivity;
        wordChoice = "";
        stringWorker = new AsyncFindWords();

        //creates a fileWorker on a different thread.
        fileWorker = new AsyncLoadStrings();
        fileWorker.execute(this);
    }

    /**
     * Callback from AsyncLoadStrings, below.
     *
     * @param loadList The returned list of strings that were loaded in AsyncLoadStrings.
     */
    public void finishLoadingStrings(String[] loadList)
    {
        //captures the string load list, and calls back to the ScrabbleWindow.
        stringList = loadList;
        ((AnagramCallback) parent).stringsAreReady();
    }

    /**
     * Inner class that loads strings from scrabble_words.txt
     */
    public class AsyncLoadStrings extends AsyncTask<AnagramLibrary, Void, String[]>
    {
        //holds the parent instance so we can call it below
        AnagramLibrary parentInstance;

        @Override
        /**
         * @param params is the filename to load strings from.
         */
        protected String[] doInBackground(AnagramLibrary... params)
        {
            parentInstance = params[0];

            //android file IO
            String filename[];
            Scanner scanIn;
            ArrayList<String> listOfStrings = new ArrayList<>();

            //open the file, throw any exceptions back to caller if something goes wrong.
            try
            {
                InputStream file = parentInstance.parent.getResources().openRawResource(R.raw.scrabble_words);
                scanIn = new Scanner(new BufferedInputStream(file));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //in java, you can't write "throw;"
                throw e;
            }

            //scan through the dictionary.
            while (scanIn.hasNext())
            {
                String tempStr = scanIn.nextLine();
                if (tempStr.length() > 1)
                {
                    listOfStrings.add(tempStr);
                }
            }

            return listOfStrings.toArray(new String[listOfStrings.size()]);
        }

        @Override
        /**
         * Calls back Main
         */
        protected void onPostExecute(String[] stringsFound)
        {
            //the parent instance might have been garbage collected. This takes care of that.
            if (parentInstance != null)
                parentInstance.finishLoadingStrings(stringsFound);
        }
    }

    /**
     * Processes a given string.
     * @param in The string to process.
     */
    public void processString(String in)
    {
        //Each task can only run once; this ensures that that occurs.
        if (stringWorker.getStatus() == AsyncTask.Status.PENDING)
        {
            wordChoice = in;
            stringWorker.execute(this);
        }
        else if (stringWorker.getStatus() == AsyncTask.Status.FINISHED)
        {
            wordChoice = in;
            stringWorker = new AsyncFindWords();
            stringWorker.execute(this);
        }
    }

    /**
     * Callback from AsyncFindWords
     * @param in The wordlist found in AsyncFindWords
     */
    private void finishFindingWords(ScrabbleWord[] in)
    {
        ((AnagramCallback) parent).callbackAnagram(in);
    }

    public class AsyncFindWords extends AsyncTask<AnagramLibrary, Void, ScrabbleWord[]>
    {
        AnagramLibrary parentInstance;

        @Override
        /**
         * @param params is set of words to process.
         */
        protected ScrabbleWord[] doInBackground(AnagramLibrary... params)
        {
            parentInstance = params[0];
            byte[] histogramOrig = new byte[26];
            int blanks = 0;

            //for-each not applicable to string... java...
            for (char c : parentInstance.wordChoice.toLowerCase().toCharArray())
            {
                //validates input
                if (c >= 'a' && c <= 'z' && c < 128)
                    histogramOrig[c - 'a']++;
                else if (c == ' ')
                    blanks++;
            }

            //stores our list of found words.
            ArrayList<ScrabbleWord> wordList = new ArrayList<>();
            int[] scoreTable = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

            //Goes through entire dictionary, comparing deviations to the string.
            for (String compare : stringList)
            {
                //make a copy of the original
                byte[] copy = histogramOrig.clone();
                int dev = 0;
                int devScore = 0;

                //Compares the deviations to the main string.
                for (char c : compare.toCharArray())
                {
                    if (copy[c - 'A'] > 0)
                        copy[c - 'A']--;
                    else
                    {
                        //store the deviation count, and the score of the actual letter, so we can use the correct score.
                        dev++;
                        devScore += scoreTable[c - 'A'];
                    }
                }

                //blanks allows a certain number of deviations from the original word.
                if (dev <= blanks)
                    wordList.add(new ScrabbleWord(compare, devScore));
            }

            //sorts the list so that the arrays will be sorted by score (larger scores in beginning)
            Collections.sort(wordList, new ScrabbleWord.invertCompareByLength());
            Collections.sort(wordList, new ScrabbleWord.invertCompareByScore());

            return wordList.toArray(new ScrabbleWord[wordList.size()]);
        }

        @Override
        /**
         * Returns the untrimmed list to main
         */
        protected void onPostExecute(ScrabbleWord[] stringsFound)
        {
            //the parent instance might have been garbage collected. This takes care of that.
            if (parentInstance != null)
                parentInstance.finishFindingWords(stringsFound);
        }
    }
}
