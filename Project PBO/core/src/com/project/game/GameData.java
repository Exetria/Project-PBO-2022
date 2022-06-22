package com.project.game;

import java.io.Serializable;

public class GameData implements Serializable {

    private static final long seralVersionUID = 1;

    private final int maxScore = 10;
    private long[] highScores;
    private String[] names;

    private long yourScore;

    public GameData(){
        highScores = new long[maxScore];
        names = new String[maxScore];
    }

    //init high score kosong
    public void init(){
        for (int i = 0; i < maxScore; i++) {
            highScores[i] = 0;
            names[i] = "---";
        }
    }

    public long[] getHighScore() {
        return highScores;
    }

    public String[] getNames() {
        return names;
    }

    public long getYourScore() {
        return yourScore;
    }

    public void setYourScore(long yourScore) {
        this.yourScore = yourScore;
    }

    //cek apakah masuk highscore
    public boolean isHighScore(long score){
        return score > highScores[maxScore-1];
    }

    //add highscore
    public void addHighScore(long newScore, String name){
        if(isHighScore(newScore)){
            highScores[maxScore - 1] = newScore;
            names[maxScore - 1] = name;
            sortHighScore();
        }
    }

    public void sortHighScore(){
        for(int i = 0; i < maxScore; i++) {
            long score = highScores[i];
            String name = names[i];
            int j;
            for(j = i - 1;
                j >= 0 && highScores[j] < score;
                j--) {
                highScores[j + 1] = highScores[j];
                names[j + 1] = names[j];
            }
            highScores[j + 1] = score;
            names[j + 1] = name;
        }
    }

}

