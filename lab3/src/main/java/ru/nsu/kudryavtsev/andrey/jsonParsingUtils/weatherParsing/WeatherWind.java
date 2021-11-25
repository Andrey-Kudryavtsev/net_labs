package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherWind {
    private Double speed;
}
