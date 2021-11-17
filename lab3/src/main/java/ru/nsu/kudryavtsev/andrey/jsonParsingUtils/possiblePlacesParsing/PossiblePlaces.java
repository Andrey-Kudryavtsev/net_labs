package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

public class PossiblePlaces {
    @Getter private final ArrayList<Hit> hits;

    public PossiblePlaces(JsonNode root) {
        if (root == null || root.get("hits") == null || !root.get("hits").isArray()) {
            hits = null;
        } else {
            hits = new ArrayList<>();
            var iter = root.get("hits").elements();
            while (iter.hasNext()) {
                hits.add(new Hit(iter.next()));
            }
        }
    }

    public boolean noInfo() {
        return (hits == null || hits.size() == 0);
    }

    public static class Hit {
        @Getter private final Double lat;
        @Getter private final Double lng;
        @Getter private final String name;
        @Getter private final String country;
        @Getter private final String city;
        @Getter private final String state;
        @Getter private final String street;
        @Getter private final String housenumber;
        @Getter private final String postcode;

        public Hit(JsonNode root) {
            lat = root.get("point") == null ? null : (root.get("point").get("lat") == null ? null : root.get("point").get("lat").asDouble());
            lng = root.get("point") == null ? null : (root.get("point").get("lng") == null ? null : root.get("point").get("lng").asDouble());
            name = root.get("name") == null ? null : root.get("name").asText();
            country = root.get("country") == null ? null : root.get("country").asText();
            city = root.get("city") == null ? null : root.get("city").asText();
            state = root.get("state") == null ? null : root.get("state").asText();
            street = root.get("street") == null ? null : root.get("street").asText();
            housenumber = root.get("housenumber") == null ? null : root.get("housenumber").asText();
            postcode = root.get("postcode") == null ? null : root.get("postcode").asText();
        }
    }
}