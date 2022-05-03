package ru.nsu.kudryavtsev.andrey.lab4snake.model;

import lombok.Getter;

public class Food {
    @Getter
    private final Point place;

    public Food(Point point) {
        this.place = point;
    }
}
