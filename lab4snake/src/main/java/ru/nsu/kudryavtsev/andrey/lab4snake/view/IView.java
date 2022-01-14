package ru.nsu.kudryavtsev.andrey.lab4snake.view;

import ru.nsu.kudryavtsev.andrey.lab4snake.backend.protoClass.SnakesProto;

public interface IView {
    void render(StateSystem stateSystem, String message);
    void sendServerInfoToGameController(String message);
    void sendConfigToGameController(SnakesProto.GameConfig config);
}
