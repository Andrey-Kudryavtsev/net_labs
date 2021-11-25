package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hit {
    private Point point;
    private String name;
    private String country;
    private String city;
    private String state;
    private String street;
    private String housenumber;
    private String postcode;
}
