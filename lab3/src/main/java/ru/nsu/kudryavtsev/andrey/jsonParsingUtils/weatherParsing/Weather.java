package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
    private static final Logger logger = LoggerFactory.getLogger("APP");

    @JsonProperty private ArrayList<WeatherDescription> weather;
    @JsonProperty private WeatherMain main;
    @JsonProperty private WeatherWind wind;

    public Icon getIcon() {
        if (weather != null && weather.size() != 0) {
            String iconID = weather.get(0).getIcon();
            URL iconUrl;
            try {
                iconUrl = new URL("http://openweathermap.org/img/wn/" + iconID + ".png");
                return new ImageIcon(iconUrl, null);
            } catch (MalformedURLException e) {
                logger.error("Weather -- " + ExceptionUtils.getStackTrace(e));
                return null;
            }
        } else {
            return null;
        }
    }

    public String getDescription() {
        if (weather != null && weather.size() != 0) {
            return weather.get(0).getDescription();
        } else {
            return null;
        }
    }

    public Double getTemperature() {
        if (main != null) {
            return main.getTemp();
        } else {
            return null;
        }
    }

    public Double getFeelsLike() {
        if (main != null) {
            return main.getFeels_like();
        } else {
            return null;
        }
    }

    public Double getWindSpeed() {
        if (wind != null) {
            return wind.getSpeed();
        } else {
            return null;
        }
    }

    public boolean noInfo() {
        return ((weather == null || weather.size() == 0) &&
                main == null &&
                wind ==  null);
    }
}
