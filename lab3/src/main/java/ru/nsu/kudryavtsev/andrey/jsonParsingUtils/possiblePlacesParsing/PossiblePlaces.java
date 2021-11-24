package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.ArrayList;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PossiblePlaces {
    @JsonProperty
    private ArrayList<Hit> hits;

    public boolean noInfo() {
        return (hits == null || hits.size() == 0);
    }
}