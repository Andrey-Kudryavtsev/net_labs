package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Hit {
    @JsonProperty private Point point;
    @JsonProperty private String name;
    @JsonProperty private String country;
    @JsonProperty private String city;
    @JsonProperty private String state;
    @JsonProperty private String street;
    @JsonProperty private String housenumber;
    @JsonProperty private String postcode;
}
