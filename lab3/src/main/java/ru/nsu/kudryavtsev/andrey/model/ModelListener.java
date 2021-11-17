package ru.nsu.kudryavtsev.andrey.model;

import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.NearPlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing.PossiblePlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing.Weather;

public interface ModelListener {
    void possiblePlacesGetRequestDone(PossiblePlaces possiblePlaces);
    void nearPlacesGetRequestDone(NearPlaces nearPlaces);
    void weatherGetRequestDone(Weather weather);
}
