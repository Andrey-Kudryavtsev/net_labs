package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place {
    @JsonProperty private String xid;
    @JsonProperty private String name;
    @JsonProperty private Double dist;
    @JsonProperty private Integer rate;
    private String info;

    public void setInfo(String info) {
        this.info = info;
    }
}
