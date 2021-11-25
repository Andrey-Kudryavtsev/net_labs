package ru.nsu.kudryavtsev.andrey.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hc.client5.http.async.methods.*;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.NearPlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.Place;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing.PossiblePlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing.Weather;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Model {
    private static final Logger logger = LoggerFactory.getLogger("APP");
    private static String GRAPHHOPPPER_API_KEY = "<DEFINE YOUR KEY IN .PROPERTIES FILE>";
    private static String OPENTRIPMAP_API_KEY = "<DEFINE YOUR KEY IN .PROPERTIES FILE>";
    private static String OPENWEATHERMAP_API_KEY = "<DEFINE YOUR KEY IN .PROPERTIES FILE>";
    private static final int RADIUS_M = 1000;
    private static final int RATE = 3;
    private static final int LIMIT = 10;
    private ModelListener listener;
    private final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();

    public Model(String path) {
        logger.info("Model -- Start async http client");
        try {
            Properties apiKeys = new Properties();
            apiKeys.load(new FileInputStream(path));
            GRAPHHOPPPER_API_KEY = apiKeys.getProperty("GRAPHHOPPPER_API_KEY");
            OPENTRIPMAP_API_KEY = apiKeys.getProperty("OPENTRIPMAP_API_KEY");
            OPENWEATHERMAP_API_KEY = apiKeys.getProperty("OPENWEATHERMAP_API_KEY");
        } catch (IOException e) {
            logger.error("Model -- Fail to load api keys");
        }
        httpClient.start();
    }

    public void shutdown() throws IOException {
        logger.info("Model -- Close async http client");
        httpClient.close();
    }

    public void addListener(ModelListener listener) {
        this.listener = listener;
    }

    public void httpGetPossiblePlaces(String query) throws UnsupportedEncodingException {
        String queryUtf = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String url = String.format("https://graphhopper.com/api/1/geocode?q=%s&limit=%s&locale=%s&debug=%s&key=%s",
                queryUtf, LIMIT, "en", "debug", GRAPHHOPPPER_API_KEY);
        SimpleHttpRequest getPossiblePlacesRequest = SimpleRequestBuilder.get(url).build();
        logger.info("Model -- Executing request " + getPossiblePlacesRequest);

        httpClient.execute(
                SimpleRequestProducer.create(getPossiblePlacesRequest),
                SimpleResponseConsumer.create(),
                possiblePlacesGetRequestCallback(getPossiblePlacesRequest)
        );
    }

    public void httpGetNearPlaces(double lat, double lng) throws UnsupportedEncodingException {
        String url = String.format("https://api.opentripmap.com/0.1/ru/places/radius?rate=%s&limit=%s&radius=%s&lon=%s&lat=%s&format=%s&apikey=%s",
                RATE, LIMIT, RADIUS_M, lng, lat, "json", OPENTRIPMAP_API_KEY);
        SimpleHttpRequest getNearPlacesRequest = SimpleRequestBuilder.get(url).build();
        logger.info("Model -- Executing request " + getNearPlacesRequest);

        httpClient.execute(
                SimpleRequestProducer.create(getNearPlacesRequest),
                SimpleResponseConsumer.create(),
                nearPlacesGetRequestCallback(getNearPlacesRequest)
        );
    }

    public void httpGetWeather(double lat, double lng) {
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?lang=ru&units=metric&lat=%s&lon=%s&appid=%s",
                lat, lng, OPENWEATHERMAP_API_KEY);
        SimpleHttpRequest getWeatherRequest = SimpleRequestBuilder.get(url).build();
        logger.info("Model -- Executing request " + getWeatherRequest);

        httpClient.execute(
                SimpleRequestProducer.create(getWeatherRequest),
                SimpleResponseConsumer.create(),
                nearWeatherRequestCallback(getWeatherRequest)
        );
    }

    public void httpGetNearPlacesInfo(NearPlaces nearPlaces) {
        if (nearPlaces == null || nearPlaces.noInfo()) {
            logger.info("Model -- No near places");
            return;
        }

        ArrayList<Future<SimpleHttpResponse>> responses = new ArrayList<>();
        for (int i = 0; i < nearPlaces.getPlaces().size(); i++) {
            String url = String.format("https://api.opentripmap.com/0.1/ru/places/xid/%s?apikey=%s",
                    nearPlaces.getPlaces().get(i).getXid(), OPENTRIPMAP_API_KEY);
            SimpleHttpRequest getNearPlaceInfo = SimpleRequestBuilder.get(url).build();
            logger.info("Model -- Executing request " + getNearPlaceInfo);

            var getNearPlaceInfoResponse = httpClient.execute(
                    SimpleRequestProducer.create(getNearPlaceInfo),
                    SimpleResponseConsumer.create(),
                    null
            );
            responses.add(getNearPlaceInfoResponse);
        }
        for (int i = 0; i < responses.size(); i++) {
            try {
                var response = responses.get(i).get();
                var objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response.getBodyText());
                if (root == null || root.get("info") == null || root.get("info").get("descr") == null) {
                    nearPlaces.getPlaces().get(i).setInfo(null);
                } else {
                    nearPlaces.getPlaces().get(i).setInfo(root.get("info").get("descr").asText());
                }
            } catch (InterruptedException | ExecutionException | JsonProcessingException e) {
                logger.error("Model -- " + ExceptionUtils.getStackTrace(e));
                nearPlaces.getPlaces().get(i).setInfo(null);
            }
        }
    }

    private FutureCallback<SimpleHttpResponse> possiblePlacesGetRequestCallback(SimpleHttpRequest getPossiblePlacesRequest) {
        return new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                logger.info("Model -- " + getPossiblePlacesRequest + "->" + new StatusLine(result));
                logger.info("Model -- " + result.getBody().toString());
                if (result.getCode() != 200) {
                    listener.possiblePlacesGetRequestDone(null);
                    return;
                }

                var objectMapper = new ObjectMapper();
                PossiblePlaces possiblePlaces = null;
                try {
                    possiblePlaces = objectMapper.readValue(result.getBodyText(), PossiblePlaces.class);
                } catch (JsonProcessingException e) {
                    logger.error("Model -- " + ExceptionUtils.getStackTrace(e));
                }

                listener.possiblePlacesGetRequestDone(possiblePlaces);
            }

            @Override
            public void failed(Exception ex) {
                logger.info("Model -- " + getPossiblePlacesRequest + "->" + ex);
                listener.possiblePlacesGetRequestDone(null);
            }

            @Override
            public void cancelled() {
                logger.info("Model -- " + getPossiblePlacesRequest + " cancelled");
                listener.possiblePlacesGetRequestDone(null);
            }
        };
    }

    private FutureCallback<SimpleHttpResponse> nearPlacesGetRequestCallback(SimpleHttpRequest getNearPlacesRequest) {
        return new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                logger.info("Model -- " + getNearPlacesRequest + "->" + new StatusLine(result));
                logger.info("Model -- " + result.getBody().toString());
                if (result.getCode() != 200) {
                    listener.nearPlacesGetRequestDone(null);
                    return;
                }

                var objectMapper = new ObjectMapper();
                NearPlaces nearPlaces = null;
                try {
                    nearPlaces = new NearPlaces(objectMapper.readValue(result.getBodyText(), new TypeReference<ArrayList<Place>>() {
                    }));
                } catch (JsonProcessingException e) {
                    logger.error("Model -- " + ExceptionUtils.getStackTrace(e));
                }

                httpGetNearPlacesInfo(nearPlaces);
                listener.nearPlacesGetRequestDone(nearPlaces);
            }

            @Override
            public void failed(Exception ex) {
                logger.info("Model -- " + getNearPlacesRequest + "->" + ex);
                listener.possiblePlacesGetRequestDone(null);
            }

            @Override
            public void cancelled() {
                logger.info("Model -- " + getNearPlacesRequest + " cancelled");
                listener.possiblePlacesGetRequestDone(null);
            }
        };
    }

    private FutureCallback<SimpleHttpResponse> nearWeatherRequestCallback(SimpleHttpRequest getWeatherRequest) {
        return new FutureCallback<>() {
            @Override
            public void completed(SimpleHttpResponse result) {
                logger.info("Model -- " + getWeatherRequest + "->" + new StatusLine(result));
                logger.info("Model -- " + result.getBody().toString());
                if (result.getCode() != 200) {
                    listener.weatherGetRequestDone(null);
                    return;
                }

                var objectMapper = new ObjectMapper();
                Weather weather = null;
                try {
                    weather = objectMapper.readValue(result.getBodyText(), Weather.class);
                } catch (JsonProcessingException e) {
                    logger.error("Model -- " + ExceptionUtils.getStackTrace(e));
                }

                listener.weatherGetRequestDone(weather);
            }

            @Override
            public void failed(Exception ex) {
                logger.info("Model -- " + getWeatherRequest + "->" + ex);
                listener.weatherGetRequestDone(null);
            }

            @Override
            public void cancelled() {
                logger.info("Model -- " + getWeatherRequest + " cancelled");
                listener.weatherGetRequestDone(null);
            }
        };
    }
}
