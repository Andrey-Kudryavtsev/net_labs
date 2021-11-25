package ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Place {
    private String xid;
    private String name;
    private Double dist;
    private Integer rate;
    private String info;

    public void setInfo(String info) {
        this.info = info;
    }
}
