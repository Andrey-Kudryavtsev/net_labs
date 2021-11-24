package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class NearPlaces {
    private final ArrayList<Place> places;

    public NearPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    public boolean noInfo() {
        return (places == null || places.size() == 0);
    }
}
