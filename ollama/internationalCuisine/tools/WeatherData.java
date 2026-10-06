package ollama.internationalCuisine.tools;

/**
 * Données météo (immuables) d'une ville
 *
 * @param temperature   en °C
 * @param windSpeed     en m/s
 * @param windDirection en degrés
 * @param visibility    en mètres
 * @param timestamp     instant de la mesure, en ms
 * @param sunrise       lever du soleil, en ms
 * @param sunset        coucher du soleil, en ms
 */
public record WeatherData(String cityName, String country,
                          double temperature, double feelsLike, double tempMin, double tempMax,
                          int humidity, int pressure, String description, String mainCondition,
                          double windSpeed, int windDirection, int visibility,
                          double latitude, double longitude,
                          long timestamp, long sunrise, long sunset) {

    /**
     * Méthode utilitaire pour formater la direction du vent
     */
    public String windDirectionText() {
        if (windDirection >= 337.5 || windDirection < 22.5) return "Nord";
        else if (windDirection < 67.5) return "Nord-Est";
        else if (windDirection < 112.5) return "Est";
        else if (windDirection < 157.5) return "Sud-Est";
        else if (windDirection < 202.5) return "Sud";
        else if (windDirection < 247.5) return "Sud-Ouest";
        else if (windDirection < 292.5) return "Ouest";
        else return "Nord-Ouest";
    }

    /**
     * Convertit la vitesse du vent en km/h
     */
    public double windSpeedKmh() {
        return windSpeed * 3.6;
    }

    /**
     * nature de la température (froid, tempéré, chaud...)
     */
    public String natureTemperature() {
        if (temperature < 0) return "très froid";
        if (temperature < 10) return "froid";
        if (temperature < 17) return "tempéré";
        if (temperature < 26) return "chaud";
        if (temperature < 35) return "très chaud";
        return "extrêmement chaud";
    }

    /**
     * Vérifie si les données météo sont valides
     */
    public boolean isValid() {
        return cityName != null && !cityName.equals("N/A") &&
                !"N/A".equals(description) && temperature != 0.0;
    }

    @Override
    public String toString() {
        if (!isValid()) {
            return "Données météo non disponibles";
        }

        return String.format(
                "Météo à %s, %s (%.4f, %.4f):\n" +
                        "Temperature: %.1f°C (ressenti: %.1f°C)\n" +
                        "Min/Max: %.1f°C / %.1f°C\n" +
                        "Conditions: %s (%s)\n" +
                        "Humidité: %d%%\n" +
                        "Pression: %d hPa\n" +
                        "Vent: %.1f m/s (%.1f km/h), direction %d° (%s)\n" +
                        "Visibilité: %d m\n" +
                        "Lever/Coucher du soleil: %tT / %tT",
                cityName, country, latitude, longitude,
                temperature, feelsLike, tempMin, tempMax,
                description, mainCondition, humidity, pressure,
                windSpeed, windSpeedKmh(), windDirection, windDirectionText(),
                visibility, new java.util.Date(sunrise), new java.util.Date(sunset)
        );
    }
}
