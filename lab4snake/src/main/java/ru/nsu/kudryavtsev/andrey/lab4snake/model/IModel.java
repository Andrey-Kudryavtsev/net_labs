package ru.nsu.kudryavtsev.andrey.lab4snake.model;

public interface IModel {
    void oneTurnGame();
    void updateModel();
    boolean addNewPlayer(Player player);
    void notifyListeners();
    void addListener(IListener listener);
    void removeListener(IListener listener);
}
