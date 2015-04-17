package game.gamehelper.Scrabble;

/**
 * Created by Andrew on 4/13/2015.
 */
public class ScrabbleWord
{

    int score;
    String word;

    ScrabbleWord(String in)
    {
        word = in.toUpperCase();
        setScore();
    }


    private void setScore()
    {
        score = 0;
        int[] scoreTable = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10};

        char[] charArray = word.toCharArray();
        for (char c : charArray)
        {
            score += scoreTable[c - 'A'];
        }
    }
}
