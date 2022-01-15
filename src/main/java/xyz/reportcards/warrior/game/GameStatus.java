package xyz.reportcards.warrior.game;

import lombok.Getter;

public enum GameStatus {

    WAITING("Waiting"), STARTING("Starting"), RUNNING("In Progress");

    @Getter private final String name;

    GameStatus(String name) {
        this.name = name;
    }

    public boolean isStartedState() {
        return this == STARTING || this == RUNNING;
    }

}
