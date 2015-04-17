package game.gamehelper.Scrabble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import game.gamehelper.R;


/**
 * Created by Andrew on 4/13/2015.
 */


public class AnagramLibraryUnused extends AsyncTask<ArrayList<ScrabbleLetter>, Void, ArrayList<ScrabbleWord>>
{
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
}