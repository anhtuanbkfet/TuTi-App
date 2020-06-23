package com.example.tuti;

public enum StatisticMode {
    All(0),
    Today(1);
    private final int value;

    StatisticMode(int value) {
        this.value = value;
    }


    public int getValue(){
        return this.value;
    }
}
