package ru.nsu.kudryavtsev.andrey.view;

import ru.nsu.kudryavtsev.andrey.controller.Controller;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.NearPlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.Place;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing.PossiblePlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing.Weather;

public interface View {
    void addListener(Controller controller);
    void drawPossiblePlaces(PossiblePlaces possiblePlaces);
    void drawNearPlaces(NearPlaces nearPlaces);
    void drawWeather(Weather weather);
    void drawNearPlaceInfo(Place place);
}
