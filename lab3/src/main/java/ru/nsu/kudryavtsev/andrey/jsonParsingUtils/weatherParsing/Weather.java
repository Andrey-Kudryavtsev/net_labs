package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

public class Weather {
    @Getter private final String description;
    @Getter private final ImageIcon icon;
    @Getter private final Double temperature;
    @Getter private final Double feelsLike;
    @Getter private final Double windSpeed;

    public Weather(JsonNode root) {
        ImageIcon icon1;
        if (root.get("weather") == null) {
            description = null;
            icon1 = null;
        } else {
            description = root.get("weather").get(0).get("description") == null ? null : root.get("weather").get(0).get("description").asText();

            if (root.get("weather").get(0).get("icon") == null) {
                icon1 = null;
            } else {
                String iconID = root.get("weather").get(0).get("icon").asText();
                try {
                    var iconUrl = new URL("http://openweathermap.org/img/wn/" + iconID + ".png");
                    icon1 = new ImageIcon(iconUrl, null);
                } catch (MalformedURLException e) {
                    icon1 = null;
                }
            }
        }
        icon = icon1;
        temperature = root.get("main") == null ? null : (root.get("main").get("temp") == null ? null : root.get("main").get("temp").asDouble());
        feelsLike = root.get("main") == null ? null : (root.get("main").get("feels_like") == null ? null : root.get("main").get("feels_like").asDouble());
        windSpeed = root.get("wind") == null ? null : (root.get("wind").get("speed") == null ? null : root.get("wind").get("speed").asDouble());
    }

    public boolean noInfo() {
        return (description == null &&
                icon == null &&
                temperature ==  null &&
                feelsLike == null &&
                windSpeed == null);
    }
}
