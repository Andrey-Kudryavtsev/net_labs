package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherMain {
    private Double temp;
    private Double feels_like;
}
