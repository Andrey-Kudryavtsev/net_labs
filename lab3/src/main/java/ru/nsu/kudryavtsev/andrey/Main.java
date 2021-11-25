package ru.nsu.kudryavtsev.andrey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.kudryavtsev.andrey.controller.BasicController;
import ru.nsu.kudryavtsev.andrey.controller.Controller;
import ru.nsu.kudryavtsev.andrey.model.Model;
import ru.nsu.kudryavtsev.andrey.view.BasicView;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger("APP");

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.error("Main -- Not enough arguments");
        } else {
            Model model = new Model(args[0]);
            BasicView view = new BasicView();
            Controller controller = new BasicController(model);
            model.addListener(view);
            view.addListener(controller);
        }
    }
}
