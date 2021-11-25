package ru.nsu.kudryavtsev.andrey.controller;

public interface Controller {
    void onSearch(String query);
    void onPossiblePlaceSelect(double lat, double lng);
    void onExit();
}
