package game.gamehelper.Scrabble;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import game.gamehelper.GameHelperPlugin;
import game.gamehelper.R;
import game.gamehelper.ScoreBoard;

/**
 * Created by khawkes on 4/13/15.
 */
public class ScrabblePlugin implements GameHelperPlugin
{
    @Override
    public String getName()
    {
        return "Scrabble";
    }

    @Override
    public String getDescription()
    {
        return "Scrabble";
    }

    @Override
    public Class<?> getEntryMenuClass()
    {
        return null;
    }

    @Override
    public Map<String, Integer> getRulesIDs()
    {
        HashMap<String, Integer> rulesIDs = new HashMap<>();
        rulesIDs.put("title", R.string.scrabble_rules_title);
        rulesIDs.put("text", R.string.scrabble_rules_text);
        rulesIDs.put("detail", R.string.scrabble_rules_detail);
        return rulesIDs;
    }

    @Override
    public Bundle getDebugBundle()
    {
        return null;
    }

    @Override
    public int getImageIcon()
    {
        return R.drawable.scrabble_default;
    }

    @Override
    public boolean isGameReady()
    {
        return false;
    }

    @Override
    public boolean isRuleSetReady()
    {
        return true;
    }

    @Override
    public ScoreBoard getScoreBoard()
    {
        return null;
    }
}
