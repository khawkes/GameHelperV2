package game.gamehelper.DominoMT;

import game.gamehelper.GameHelperPlugin;
import game.gamehelper.ScoreBoard;

/**
 * Created by Mark Andrews on 3/7/2015.
 */
public class DominoPlugin implements GameHelperPlugin {
    @Override
    public ScoreBoard getScoreBoard() {
        return new ScoreBoard();
    }

    @Override
    public void getRules() {

    }

    @Override
    public void displayMenu() {

    }
}
