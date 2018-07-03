package com.onezeros.chinesechess;

public class ChessResult {
    private int id;
    private String time;
    private int result;//Com win =0, Player=1
    private int level;//Lv 3-4-5

    public ChessResult(int id, String time, int result, int level) {
        this.id = id;
        this.time = time;
        this.result = result;
        this.level = level;
    }

    public ChessResult() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        String r = "";
        String lv = "";
        switch (level) {
            case 3:
                lv = "Easy";
                break;
            case 4:
                lv = "Medium";
                break;
            case 5:
                lv = "Hard";
                break;
        }

        switch (result) {
            case 0:
                r="Lose";
                break;
            case 1:
                r="Win";
                break;
        }
        return time + "   " + lv+"  "+r;
    }
}
