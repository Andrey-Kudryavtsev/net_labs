package ru.nsu.kudryavtsev.andrey.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.kudryavtsev.andrey.controller.Controller;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.NearPlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.nearPlacesParsing.Place;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing.Hit;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.possiblePlacesParsing.PossiblePlaces;
import ru.nsu.kudryavtsev.andrey.jsonParsingUtils.weatherParsing.Weather;
import ru.nsu.kudryavtsev.andrey.model.ModelListener;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

import static javax.swing.SwingUtilities.invokeLater;

public class BasicView implements View, ModelListener {
    private static final Logger logger = LoggerFactory.getLogger("APP");
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final Color BORDER_COLOR = new Color(6, 165, 196);
    private static final Color BACKGROUND_COLOR = new Color(194, 245, 255);
    private final JFrame appFrame;
    private Controller controller;

    private PossiblePlaces curPossiblePlaces = null;
    private NearPlaces curNearPlaces = null;
    private Weather curWeather = null;
    private String curPlaceInfo = null;



    private enum State {
        ONLY_POSSIBLE,
        POSSIBLE_WITH_NEAR_AND_WEATHER,
        POSSIBLE_WITH_INFO_AND_WEATHER
    }
    public BasicView() {
        appFrame = new JFrame();
        Container contentPane = appFrame.getContentPane();
        contentPane.setLayout(new GridBagLayout());
        contentPane.setBackground(BACKGROUND_COLOR);

        JPanel searchPanel = createSearchPanel();
        addToTop(searchPanel, contentPane);

        appFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        appFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closeButtonPressed();
                logger.info("BasicView -- Closing window");
                super.windowClosed(e);
            }
        });
        appFrame.setSize(new Dimension(WIDTH, HEIGHT));
        appFrame.validate();
        appFrame.setVisible(true);
    }

    public void addListener(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void drawPossiblePlaces(PossiblePlaces possiblePlaces) {
        logger.info("BasicView -- Got new possible places, draw only possible places");
        curPossiblePlaces = possiblePlaces;
        draw(State.ONLY_POSSIBLE);
    }

    @Override
    public void drawNearPlaces(NearPlaces nearPlaces) {
        logger.info("BasicView -- Got new near places, draw possible places with near places and weather");
        curNearPlaces = nearPlaces;
        draw(State.POSSIBLE_WITH_NEAR_AND_WEATHER);
    }

    @Override
    public void drawWeather(Weather weather) {
        logger.info("BasicView -- Got new weather, draw possible places with near places and weather");
        curWeather = weather;
        draw(State.POSSIBLE_WITH_NEAR_AND_WEATHER);
    }

    @Override
    public void drawNearPlaceInfo(Place place) {
        logger.info("BasicView -- Got new near place info, draw possible places with near place info and weather");
        curPlaceInfo = place.getInfo();
        draw(State.POSSIBLE_WITH_INFO_AND_WEATHER);
    }

    private void draw(State state) {
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());

        JPanel searchPanel = createSearchPanel();
        addToTop(searchPanel, contentPane);

        JScrollPane placesScrollPane = createPlacesScrollPane(curPossiblePlaces);
        placesScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 30, 10, state == State.ONLY_POSSIBLE ? 30 : 3, BACKGROUND_COLOR),
                BorderFactory.createMatteBorder(3, 3, 3, 3, BORDER_COLOR)
        ));

        if (state == State.ONLY_POSSIBLE) {
            addToBottom(placesScrollPane, contentPane);
        } else {
            JScrollPane scrollPane = null;
            if (state == State.POSSIBLE_WITH_NEAR_AND_WEATHER) {
                scrollPane = createNearPlacesScrollPane(curNearPlaces);
                scrollPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 10, 30, BACKGROUND_COLOR),
                        BorderFactory.createMatteBorder(3, 3, 3, 3, BORDER_COLOR)
                ));
            } else if (state == State.POSSIBLE_WITH_INFO_AND_WEATHER) {
                scrollPane = createInfoScrollPane(curPlaceInfo);
                scrollPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 10, 30, BACKGROUND_COLOR),
                        BorderFactory.createMatteBorder(3, 3, 3, 3, BORDER_COLOR)
                ));
            }
            JPanel weatherPanel = createWeatherPanel(curWeather);
            weatherPanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 30, BACKGROUND_COLOR));
            JPanel generalPanel = createGeneralPanel(weatherPanel, scrollPane);
            JSplitPane splitPane = createSplitPane(placesScrollPane, generalPanel);
            addToBottom(splitPane, contentPane);
        }

        appFrame.setContentPane(contentPane);
        appFrame.validate();
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(BACKGROUND_COLOR);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.LINE_AXIS));
        searchPanel.setBorder(BorderFactory.createMatteBorder(10, 0, 20, 0, BACKGROUND_COLOR));

        JTextField searchField = new JTextField();

        JButton searchButton = new JButton("поиск");
        searchButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        searchButton.addActionListener(e -> searchButtonPressed(searchField));

        searchPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        searchPanel.add(searchField);
        searchPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createRigidArea(new Dimension(30, 0)));

        return searchPanel;
    }

    private JScrollPane createPlacesScrollPane(PossiblePlaces possiblePlaces) {
        JPanel placesPanel = new JPanel();
        placesPanel.setLayout(new BoxLayout(placesPanel, BoxLayout.PAGE_AXIS));

        if (possiblePlaces == null || possiblePlaces.noInfo()) {
            JTextArea l = new JTextArea("Ничего не удалось найти");
            placesPanel.add(l);
        } else {
            int placesNumber = possiblePlaces.getHits().size();
            for (int i = 0; i < placesNumber; i++) {
                JTextArea l = new JTextArea(formatPossiblePlaces(possiblePlaces.getHits().get(i)));
                l.setBorder(createCoolBorder(i, placesNumber));
                l.setEditable(false);
                l.setLineWrap(true);
                l.setWrapStyleWord(true);
                double lat = possiblePlaces.getHits().get(i).getPoint().getLat();
                double lng = possiblePlaces.getHits().get(i).getPoint().getLng();
                l.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        possiblePlaceClicked(lat, lng);
                    }
                });
                placesPanel.add(l);
            }
        }

        return new JScrollPane(placesPanel);
    }

    private JScrollPane createNearPlacesScrollPane(NearPlaces nearPlaces) {
        JPanel placesPanel = new JPanel();
        placesPanel.setLayout(new BoxLayout(placesPanel, BoxLayout.PAGE_AXIS));
        if (nearPlaces == null || nearPlaces.noInfo()) {
            JTextArea l = new JTextArea("Ничего не удалось найти");
            placesPanel.add(l);
        } else {
            int placesNumber = nearPlaces.getPlaces().size();
            for (int i = 0; i < placesNumber; i++) {
                var place = nearPlaces.getPlaces().get(i);
                JTextArea l = new JTextArea(formatNearPlaces(place));
                l.setBorder(createCoolBorder(i, placesNumber));
                l.setEditable(false);
                l.setLineWrap(true);
                l.setWrapStyleWord(true);
                l.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        nearPlaceClicked(place);
                    }
                });
                placesPanel.add(l);
            }
        }

        return new JScrollPane(placesPanel);
    }

    private JPanel createWeatherPanel(Weather weather) {
        JPanel weatherPanel = new JPanel();
        JLabel weatherArea;
        if (weather == null || weather.noInfo()) {
            weatherArea = new JLabel("Нет информации о погоде");
        } else {
            weatherArea = new JLabel(formatWeather(weather), weather.getIcon(), JLabel.LEADING);
        }
        weatherPanel.add(weatherArea);
        weatherPanel.setMaximumSize(new Dimension(250, 130));
        weatherPanel.setPreferredSize(new Dimension(250, 130));
        weatherPanel.setBackground(BACKGROUND_COLOR);

        return weatherPanel;
    }

    private JScrollPane createInfoScrollPane(String info) {
        JTextArea nearPlaceInfo;
        if (info == null) {
            nearPlaceInfo = new JTextArea("Нет описания\n");
        } else {
            nearPlaceInfo = new JTextArea(info.replaceAll("<.*?>", ""));
        }
        nearPlaceInfo.setEditable(false);
        nearPlaceInfo.setLineWrap(true);
        nearPlaceInfo.setWrapStyleWord(true);
        nearPlaceInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nearPlaceInfoClicked();
            }
        });
        nearPlaceInfo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return new JScrollPane(nearPlaceInfo);
    }

    private JPanel createGeneralPanel(Container first, Container second) {
        if (first == null || second == null) {
            return null;
        }

        JPanel generalPanel = new JPanel();
        generalPanel.setBackground(BACKGROUND_COLOR);
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.PAGE_AXIS));
        generalPanel.add(first);
        generalPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        generalPanel.add(second);

        return generalPanel;
    }

    private JSplitPane createSplitPane(Container first, Container second) {
        if (first == null || second == null) {
            return null;
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, first, second);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(appFrame.getWidth()/2);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        return splitPane;
    }

    private void addToTop(Component component, Container contentPane) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        contentPane.add(component, c);
    }

    private Border createCoolBorder(int i, int placesNumber) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createMatteBorder(0, 0, i == placesNumber-1 ? 0 : 3, 0, BORDER_COLOR)
        );
    }

    private void addToBottom(Component component, Container contentPane) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.weighty = 0.5;
        contentPane.add(component, c);
    }

    private String formatPossiblePlaces(Hit place) {
        var str = new StringBuilder();
        if (place.getName() == null) {
            str.append("No name").append("\n");
        } else {
            str.append(place.getName()).append("\n");
        }
        if (place.getCountry() != null) {
            str.append(place.getCountry()).append(", ");
        }
        if (place.getCity() != null) {
            str.append(place.getCity()).append(", ");
        }
        if (place.getState() != null) {
            str.append(place.getState()).append(", ");
        }
        if (place.getStreet() != null) {
            str.append("ул. : ").append(place.getStreet()).append(", ");
        }
        if (place.getHousenumber() != null) {
            str.append("д. : ").append(place.getHousenumber()).append(", ");
        }
        if (place.getPostcode() != null) {
            str.append("\nПочтовый индекс: ").append(place.getPostcode()).append("\n");
        }
        return str.toString();
    }

    private String formatNearPlaces(Place place) {
        var str = new StringBuilder();
        if (place.getName() == null) {
            str.append("No name").append("\n");
        } else {
            str.append(place.getName()).append("\n");
        }
        if (place.getDist() != null) {
            str.append("В ").append(Math.round(place.getDist())).append(" метрах от вас.\n");
        }
        if (place.getRate() != null) {
            str.append("Рейтинг: ").append(place.getRate()).append("\n");
        }
        return str.toString();
    }

    private String formatWeather(Weather weather) {
        var str = new StringBuilder();
        str.append("<html><table>");
        if (weather.getDescription() != null) {
            str.append("<tr>").append(weather.getDescription()).append("</tr>");
        }
        if (weather.getTemperature() != null) {
            str.append("<tr>Температура: ").append(Math.round(weather.getTemperature())).append(" &#176C</tr>");
        }
        if (weather.getFeelsLike() != null) {
            str.append("<tr>Ощущается как: ").append(Math.round(weather.getFeelsLike())).append(" &#176C</tr>");
        }
        if (weather.getWindSpeed() != null) {
            str.append("<tr>Скорость ветра: ").append(weather.getWindSpeed()).append(" м/с</tr>");
        }
        str.append("</table></html>");
        return str.toString();
    }

    private void closeButtonPressed() {
        logger.info("BasicView -- Close button pressed");
        controller.onExit();
    }

    private void searchButtonPressed(JTextField searchField) {
        logger.info("BasicView -- Search button pressed");
        if (controller != null && searchField != null) {
            controller.onSearch(searchField.getText());
        }
    }

    private void possiblePlaceClicked(double lat, double lng) {
        logger.info("BasicView -- Possible place clicked");
        if (controller != null) {
            controller.onPossiblePlaceSelect(lat, lng);
        }
    }

    private void nearPlaceClicked(Place place) {
        logger.info("BasicView -- Near place clicked");
        invokeLater(() -> drawNearPlaceInfo(place));
    }

    private void nearPlaceInfoClicked() {
        logger.info("BasicView -- Near place info clicked");
        invokeLater(() -> drawNearPlaces(curNearPlaces));
    }

    @Override
    public void possiblePlacesGetRequestDone(PossiblePlaces possiblePlaces) {
        logger.info("BasicView -- Possible places get request done, add drawing possible places in queue");
        invokeLater(() -> drawPossiblePlaces(possiblePlaces));
    }

    @Override
    public void nearPlacesGetRequestDone(NearPlaces nearPlaces) {
        logger.info("BasicView -- Near places get request done, add drawing near places in queue");
        invokeLater(() -> drawNearPlaces(nearPlaces));
    }

    @Override
    public void weatherGetRequestDone(Weather weather) {
        logger.info("BasicView -- Weather get request done, add drawing weather in queue");
        invokeLater(() -> drawWeather(weather));
    }
}
