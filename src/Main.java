import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    private static final String apiKey = "033dbcba-53e1-43ee-a9ed-6b89ea456dd5";
    private static final String baseURL = "https://api.weather.yandex.ru/v2/forecast";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public static void main(String[] args) {
        // Получаем константы от пользователя
        Scanner scanner = new Scanner(System.in);
            System.out.println("Введите широту: ");
            String lat = scanner.nextLine();

            System.out.println("Введите долготу: ");
            String lon = scanner.nextLine();

            System.out.println("Введите количество дней для расчета средней температуры: ");
            int limit = Integer.parseInt(scanner.nextLine());

        try {
            // Запрос к сервису Яндекс погоды
            String response = makeApiRequest(lat, lon, limit);
            System.out.println("Полный ответ от сервиса: ");
            System.out.println(response);

            JSONObject jsonResponse = new JSONObject(response);
            int currentTemp = jsonResponse.getJSONObject("fact").getInt("temp");
            System.out.println("Текущая температура: " + currentTemp + " градусов");

            // Расчет средней температуры
            double avgTemp = averageTemperature(jsonResponse, limit);
            String text = String.format("Средняя температура за %d дней: %f градусов", limit, avgTemp);
            System.out.println(text);
        } catch (Exception e) {
            System.err.println("Error making HTTP request: " + e.getMessage());
        }

    }

    // Метод для получения всех данных от API Яндекс погоды
    private static String makeApiRequest(String lat, String lon, int limit) throws Exception {
        String url = String.format("%s?lat=%s&lon=%s&limit=%d", baseURL, lat, lon, limit);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .headers("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Метод для расчета средней температуры
    private static double averageTemperature(JSONObject jsonResponse, int limit) {
        JSONArray forecasts = jsonResponse.getJSONArray("forecasts");
        double sum =0;
        int count = Math.min(limit, forecasts.length());

        for (int i = 0; i < count; i++) {
            JSONObject forecast = forecasts.getJSONObject(i);
            int dayTemp = forecast.getJSONObject("parts").getJSONObject("day").getInt("temp_avg");
            sum += dayTemp;
        }
        return sum / count;
    }

}