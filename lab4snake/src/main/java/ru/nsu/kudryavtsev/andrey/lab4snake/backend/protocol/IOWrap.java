package ru.nsu.kudryavtsev.andrey.lab4snake.backend.protocol;

import ru.nsu.kudryavtsev.andrey.lab4snake.backend.protoClass.SnakesProto;

public interface IOWrap {
    void send(SnakesProto.GameMessage message, String receiver, int receiverPort);
    GameMessageWrap receive();
}
