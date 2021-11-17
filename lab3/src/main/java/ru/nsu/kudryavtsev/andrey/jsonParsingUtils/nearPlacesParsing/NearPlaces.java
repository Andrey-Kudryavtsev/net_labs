package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.ArrayList;

public class NearPlaces {
    @Getter private final ArrayList<Place> places;

    public NearPlaces(JsonNode root) {
        if (root == null || !root.isArray()) {
            places = null;
        } else {
            places = new ArrayList<>();
            var iter = root.elements();
            while (iter.hasNext()) {
                places.add(new Place(iter.next()));
            }
        }
    }

    public boolean noInfo() {
        return (places == null || places.size() == 0);
    }

    public static class Place {
        @Getter private final String xid;
        @Getter private final String name;
        @Getter private final Double dist;
        @Getter private final Integer rate;
        @Getter private String info;

        public Place(JsonNode root) {
            xid = root.get("xid") == null ? null : root.get("xid").asText();
            name = root.get("name") == null ? null : root.get("name").asText();
            dist = root.get("dist") == null ? null : root.get("dist").asDouble();
            rate = root.get("rate") == null ? null : root.get("rate").asInt();
        }

        public void setInfo(String info) {
            this.info = info == null ? null : info.replaceAll("<.*?>", "");
        }
    }
}
