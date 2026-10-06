package ollama.internationalCuisine.tools;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service météo utilisant l'API OpenWeatherMap
 * Fournit des méthodes pour récupérer et analyser les données météo
 * avec une gestion robuste des erreurs et des entrées utilisateur.
 *
 * @author Claude.AI (adapté par E.ADAM)
 */
public class Meteo {

    private static final String API_KEY = "123456789"; // Remplacez par votre clé API
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    private static final Logger logger = Logger.getLogger(Meteo.class.getName());
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Récupère les données météo pour une ville donnée
     *
     * @param cityName nom de la ville
     * @return WeatherData ou null en cas d'erreur
     */
    public WeatherData getWeatherByCity(String cityName) {
        // Supprimer les espaces en début/fin et les caractères de contrôle
        String cleaned = cityName == null ? "" : cityName.trim().replaceAll("\\p{Cntrl}", "");
        if (cleaned.isEmpty() || cleaned.length() > 100) {
            logger.warning("Nom de ville invalide: " + cityName);
            return null;
        }
        return fetchWeather("q=" + URLEncoder.encode(cleaned, StandardCharsets.UTF_8));
    }

    /**
     * Récupère les données météo par coordonnées
     *
     * @param lat latitude
     * @param lon longitude
     * @return WeatherData ou null en cas d'erreur
     */
    public WeatherData getWeatherByCoordinates(double lat, double lon) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            logger.warning("Coordonnées invalides: lat=" + lat + ", lon=" + lon);
            return null;
        }
        // Locale.ROOT : point décimal quelle que soit la langue du système
        return fetchWeather(String.format(java.util.Locale.ROOT, "lat=%f&lon=%f", lat, lon));
    }

    /**
     * Interroge l'API puis analyse la réponse
     *
     * @param query partie spécifique de la requête (ville ou coordonnées)
     * @return WeatherData ou null en cas d'erreur
     */
    private WeatherData fetchWeather(String query) {
        String url = "%s?%s&appid=%s&units=metric&lang=fr".formatted(BASE_URL, query, API_KEY);
        logger.info("Requête météo: " + query);
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return switch (response.statusCode()) {
                case 200 -> parseWeatherData(response.body());
                case 401 -> { logger.severe("Clé API invalide ou manquante"); yield null; }
                case 404 -> { logger.warning("Ville non trouvée (HTTP 404)"); yield null; }
                default -> { logger.warning("Erreur HTTP: " + response.statusCode()); yield null; }
            };
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la requête météo: " + query, e);
            return null;
        }
    }

    /**
     * Parse les données JSON de l'API météo avec org.json
     * (un bloc absent est remplacé par un objet vide => valeurs par défaut)
     *
     * @param jsonResponse réponse JSON de l'API
     * @return WeatherData
     */
    private WeatherData parseWeatherData(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        JSONObject sys = json.optJSONObject("sys", new JSONObject());
        JSONObject main = json.optJSONObject("main", new JSONObject());
        JSONObject wind = json.optJSONObject("wind", new JSONObject());
        JSONObject coord = json.optJSONObject("coord", new JSONObject());
        JSONArray weatherArray = json.optJSONArray("weather");
        JSONObject weather = (weatherArray != null && !weatherArray.isEmpty()) ? weatherArray.getJSONObject(0) : new JSONObject();

        // les dates de l'API sont en secondes, on les convertit en ms
        return new WeatherData(
                json.optString("name", "N/A"), sys.optString("country", "N/A"),
                main.optDouble("temp", 0.0), main.optDouble("feels_like", 0.0),
                main.optDouble("temp_min", 0.0), main.optDouble("temp_max", 0.0),
                main.optInt("humidity", 0), main.optInt("pressure", 0),
                weather.optString("description", "N/A"), weather.optString("main", "N/A"),
                wind.optDouble("speed", 0.0), wind.optInt("deg", 0),
                json.optInt("visibility", 0),
                coord.optDouble("lat", 0.0), coord.optDouble("lon", 0.0),
                json.optLong("dt", 0) * 1000, sys.optLong("sunrise", 0) * 1000, sys.optLong("sunset", 0) * 1000);
    }


    /**
     * Méthode utilitaire pour obtenir des informations météo formatées
     *
     * @param cityName nom de la ville
     * @return String formaté avec les données météo ou message d'erreur
     */
    public String getFormattedWeather(String cityName) {
        WeatherData weather = getWeatherByCity(cityName);
        return (weather != null && weather.isValid())
                ? weather.toString()
                : "Impossible de récupérer les données météo pour " + cityName;
    }

    /**
     * Vérifie si les conditions sont favorables (pour un agent)
     *
     * @param cityName nom de la ville
     * @return true si favorables, false sinon ou en cas d'erreur
     */
    public boolean isWeatherFavorable(String cityName) {
        WeatherData weather = getWeatherByCity(cityName);
        if (weather == null || !weather.isValid()) {
            logger.warning("Impossible de vérifier les conditions météo pour: " + cityName);
            return false;
        }

        // Conditions considérées comme favorables
        String condition = weather.mainCondition().toLowerCase();
        return weather.temperature() >= 15 && weather.temperature() <= 25
                && weather.windSpeed() < 10
                && !condition.contains("rain") && !condition.contains("storm");
    }

    /**
     * Obtient un conseil météo
     *
     * @param cityName nom de la ville
     * @return conseil sous forme de String
     */
    public String getWeatherAdvice(String cityName) {
        WeatherData weather = getWeatherByCity(cityName);
        if (weather == null || !weather.isValid()) {
            return "Données météo non disponibles pour " + cityName;
        }

        StringBuilder advice = new StringBuilder();
        String condition = weather.mainCondition().toLowerCase();

        if (weather.temperature() < 0) {
            advice.append("⚠️ Attention au gel ! Prévoyez des vêtements chauds. ");
        } else if (weather.temperature() > 30) {
            advice.append("🌡️ Il fait très chaud, pensez à vous hydrater. ");
        }

        if (condition.contains("rain")) {
            advice.append("🌧️ Il pleut, n'oubliez pas votre parapluie ! ");
        } else if (condition.contains("snow")) {
            advice.append("❄️ Il neige, attention aux routes glissantes. ");
        }

        if (weather.windSpeed() > 15) {
            advice.append("💨 Vent fort (%.1f km/h). ".formatted(weather.windSpeedKmh()));
        }

        if (weather.humidity() > 80) {
            advice.append("💧 Humidité élevée (%d%%). ".formatted(weather.humidity()));
        }

        return advice.isEmpty() ? "Conditions météo stables !" : advice.toString().trim();
    }
}
