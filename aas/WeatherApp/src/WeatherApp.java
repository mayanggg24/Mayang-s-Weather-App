import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp extends JFrame {
    private final JTextField cityField;
    private final JLabel cityLabel;
    private final JLabel temperatureLabel;
    private final JLabel conditionLabel;
    private final JLabel humidityLabel;
    private final JLabel windLabel;
    private final JLabel statusLabel;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WeatherApp() {
        setTitle("Weather App");
        setSize(420, 470);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(new Color(235, 245, 255));

        JLabel titleLabel = new JLabel("Weather App");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cityField = new JTextField();
        cityField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cityField.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton searchButton = new JButton("Search Weather");
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        cityLabel = createLabel("Enter a city to begin", 18);
        temperatureLabel = createLabel("-- °C", 34);
        conditionLabel = createLabel("Weather condition: --", 16);
        humidityLabel = createLabel("Humidity: --", 16);
        windLabel = createLabel("Wind speed: --", 16);
        statusLabel = createLabel("Ready", 12);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(new JLabel("Enter city name:"));
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(cityField);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(searchButton);
        mainPanel.add(Box.createVerticalStrut(22));
        mainPanel.add(cityLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(temperatureLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(conditionLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(humidityLabel);
        mainPanel.add(Box.createVerticalStrut(8));
        mainPanel.add(windLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(statusLabel);
        add(mainPanel);

        searchButton.addActionListener(e -> searchWeather());
        cityField.addActionListener(e -> searchWeather());
    }

    private JLabel createLabel(String text, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void searchWeather() {
        String city = cityField.getText().trim();

        if (city.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter a city name.",
                "Missing Input", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String apiKey = System.getenv("WEATHER_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            JOptionPane.showMessageDialog(
                this,
                "API key is missing. Set the WEATHER_API_KEY environment variable.",
                "Configuration Error", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        statusLabel.setText("Fetching weather data...");
        cityLabel.setText("Searching for " + city + "...");

        new SwingWorker<JSONObject, Void>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                return fetchWeather(city, apiKey);
            }

            @Override
            protected void done() {
                try {
                    displayWeather(get());
                    statusLabel.setText("Weather updated successfully.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    String message = cause != null ? cause.getMessage() : ex.getMessage();
                    statusLabel.setText("Unable to get weather.");
                    JOptionPane.showMessageDialog(
                        WeatherApp.this,
                        message == null ? "An unexpected error occurred." : message,
                        "Weather Error", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private JSONObject fetchWeather(String city, String apiKey) throws Exception {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
        String url = "https://api.openweathermap.org/data/2.5/weather"
            + "?q=" + encodedCity + "&appid=" + encodedKey + "&units=metric";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(
            request, HttpResponse.BodyHandlers.ofString()
        );
        JSONObject json = new JSONObject(response.body());

        if (response.statusCode() != 200) {
            int code = response.statusCode();
            if (code == 404) {
                throw new Exception("City not found. Check the spelling and try again.");
            } else if (code == 401) {
                throw new Exception("Invalid API key or the key is not active yet.");
            } else if (code == 429) {
                throw new Exception("API request limit reached. Please try again later.");
            }
            throw new Exception("API request failed (" + code + "): "
                + json.optString("message", "Please try again later."));
        }
        return json;
    }

    private void displayWeather(JSONObject data) {
        String city = data.optString("name", "Unknown city");
        JSONObject system = data.optJSONObject("sys");
        String country = system == null ? "" : system.optString("country", "");
        JSONObject main = data.getJSONObject("main");
        JSONArray weatherArray = data.getJSONArray("weather");
        JSONObject weather = weatherArray.getJSONObject(0);
        JSONObject wind = data.optJSONObject("wind");

        double temperature = main.getDouble("temp");
        int humidity = main.getInt("humidity");
        String condition = weather.optString("description", "Unavailable");
        double windSpeed = wind == null ? 0.0 : wind.optDouble("speed", 0.0);

        cityLabel.setText(country.isBlank() ? city : city + ", " + country);
        temperatureLabel.setText(String.format("%.1f °C", temperature));
        conditionLabel.setText("Conditions: " + condition);
        humidityLabel.setText("Humidity: " + humidity + "%");
        windLabel.setText(String.format("Wind speed: %.1f m/s", windSpeed));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WeatherApp app = new WeatherApp();
            app.setVisible(true);
        });
    }
}
