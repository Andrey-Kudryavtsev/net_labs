package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDescription {
    @JsonProperty private String description;
    @JsonProperty private String icon;
}
