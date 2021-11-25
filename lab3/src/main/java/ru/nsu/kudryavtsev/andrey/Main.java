package ru.nsu.kudryavtsev.andrey;

import ru.nsu.kudryavtsev.andrey.controller.BasicController;
import ru.nsu.kudryavtsev.andrey.controller.Controller;
import ru.nsu.kudryavtsev.andrey.model.Model;
import ru.nsu.kudryavtsev.andrey.view.BasicView;

public class Main {
    public static void main(String[] args) {
        Model model = new Model();
        BasicView view = new BasicView();
        Controller controller = new BasicController(model);
        model.addListener(view);
        view.addListener(controller);
    }
}
