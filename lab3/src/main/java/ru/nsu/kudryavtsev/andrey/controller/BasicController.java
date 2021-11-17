package ru.nsu.kudryavtsev.andrey.controller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.kudryavtsev.andrey.model.Model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BasicController implements Controller {
    private static final Logger logger = LoggerFactory.getLogger("APP");
    private final Model model;

    public BasicController(Model model) {
        this.model = model;
    }

    @Override
    public void onSearch(String query) {
        logger.info("BasicController -- Going to make search with query: " + query);
        try {
            model.httpGetPossiblePlaces(query);
        } catch (UnsupportedEncodingException e) {
            logger.error("BasicController -- " + ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void onPossiblePlaceSelect(double lat, double lng) {
        logger.info("BasicController -- Going to find a place with coordinates: " + "[" + lat + ", " + lng + "]");
        try {
            model.httpGetNearPlaces(lat, lng);
        } catch (UnsupportedEncodingException e) {
            logger.error("BasicController -- " + ExceptionUtils.getStackTrace(e));
        }
        model.httpGetWeather(lat, lng);
    }

    @Override
    public void onExit() {
        try {
            model.shutdown();
        } catch (IOException e) {
            logger.error("BasicController -- " + ExceptionUtils.getStackTrace(e));
        }
    }
}
