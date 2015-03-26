package game.gamehelper;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;

/**
 * Created by Mark Andrews on 3/7/2015.
 */
public class GameSet implements Parcelable {

    private boolean isWinner;

    private LinkedList<Integer> setScore = new LinkedList<Integer>();
    private Hand hand;


    public static final Parcelable.Creator<GameSet> CREATOR = new Parcelable.Creator<GameSet>(){

        @Override
        public GameSet createFromParcel(Parcel source) {
            return new GameSet(source);
        }

        @Override
        public GameSet[] newArray(int size) {
            return new GameSet[size];
        }
    };

    public GameSet(Hand hand){
        this.hand = hand;
        setScore.add(hand.getTotalPointsHand());
    }

    public GameSet(){};

    public GameSet(Parcel in){
        setScore = (LinkedList<Integer>) in.readSerializable();
    }

    public void deletePlayer(int location){
        setScore.remove(location);
    }

    public void addPlayer(){
        setScore.add(0);
    }
    public void addPlayer(int a){
        setScore.add(a);
    }

    public void changeScore(int location, int score){
        setScore.remove(location);
        setScore.add(location, score);
    }

    public int getScore(int location){
        return setScore.get(location);
    }

    public int getSize(){
        return setScore.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(setScore);
    }
    public void readFromParcel(Parcel in){
        setScore = (LinkedList<Integer>) in.readSerializable();
        //TODO make Hand parcelable and add to parcel then modify constructor to reflect this
    }
}
