package ollama.toolAgent.outils;

import ollama.internationalCuisine.tools.Meteo;
import ollama.internationalCuisine.tools.WeatherData;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Outil météo : météo actuelle d'une ville, via la classe {@link Meteo} (API OpenWeatherMap) d'un projet voisin
 * <p>
 * ATTENTION : la clé gratuite d'OpenWeatherMap est limitée en nombre d'appels.
 * Un LLM peut appeler plusieurs fois le même outil : les résultats sont donc gardés en cache
 * pendant {@link #DUREE_CACHE_MS} ms (la météo ne change pas toutes les minutes...).
 * <p>
 * Exemple d'appel par le LLM : <code>{"ville": "Valenciennes"}</code><br>
 * Exemple de réponse : <code>{"ville":"Valenciennes","pays":"FR","temperature_c":12.4,"ressenti_c":11.8,
 * "nature":"...","ciel":"nuageux","humidite_pct":81,"vent_kmh":14,"source":"api"}</code>
 */
public class OutilMeteo implements Outil {
    /** durée de validité d'une météo en cache : 15 minutes */
    public static final long DUREE_CACHE_MS = 15 * 60 * 1000;
    /** cache partagé par tous les agents : ville (en minuscules) -> (instant de la mesure, résultat) */
    private static final Map<String, Object[]> cache = new HashMap<>();
    /** nombre d'appels réels à l'API (pour surveiller la consommation) */
    private static int nbAppelsApi = 0;

    /** nom de l'outil, tel que le LLM doit l'utiliser pour l'appeler */
    @Override
    public String nom() {
        return "meteo";
    }

    /**
     * description de l'outil, transmise au LLM pour qu'il sache QUAND l'utiliser.
     * On précise aussi ce que l'outil ne sait PAS faire (les prévisions), pour éviter les mauvais appels.
     */
    @Override
    public String description() {
        return "donne la météo ACTUELLE d'une ville (température, ressenti, ciel, humidité, vent). "
                + "Ne donne pas de prévisions pour les jours suivants.";
    }

    /** paramètres attendus par l'outil : nom du paramètre -> explication pour le LLM */
    @Override
    public JSONObject parametres() {
        return new JSONObject().put("ville", "nom de la ville (texte), ex. \"Valenciennes\" ou \"Lille,FR\"");
    }

    /**
     * exécute l'outil : renvoie la météo actuelle de la ville demandée,
     * depuis le cache si elle est assez récente, sinon depuis l'API OpenWeatherMap.
     *
     * @param arguments arguments fournis par le LLM, doit contenir "ville"
     * @return la météo au format JSON, avec un champ "source" = "cache" ou "api"
     * @throws IllegalArgumentException si la ville est absente ou si la météo est introuvable
     */
    @Override
    public JSONObject executer(JSONObject arguments) {
        // récupération et vérification du paramètre
        String ville = arguments.optString("ville", "").trim();
        if (ville.isEmpty()) throw new IllegalArgumentException("le paramètre 'ville' est obligatoire");

        // la clé du cache est en minuscules : "Lille" et "lille" désignent la même entrée
        String cle = ville.toLowerCase(Locale.ROOT);
        // le cache est partagé par plusieurs agents (threads) : accès synchronisé
        synchronized (cache) {
            Object[] entree = cache.get(cle);
            // entrée trouvée et encore valide -> on renvoie une COPIE (pour ne pas modifier l'objet en cache)
            if (entree != null && System.currentTimeMillis() - (long) entree[0] < DUREE_CACHE_MS)
                return new JSONObject(((JSONObject) entree[1]).toString()).put("source", "cache");
        }

        // pas de résultat en cache (ou résultat périmé) : appel réel à l'API
        nbAppelsApi++;
        WeatherData w = new Meteo().getWeatherByCity(ville);
        if (w == null || !w.isValid())
            throw new IllegalArgumentException("météo introuvable pour la ville '" + ville + "'");

        // construction du résultat : valeurs arrondies (1 décimale pour les températures),
        // noms de champs explicites avec unités, pour être faciles à interpréter par le LLM
        JSONObject resultat = new JSONObject()
                .put("ville", w.cityName())
                .put("pays", w.country())
                .put("temperature_c", Math.round(w.temperature() * 10) / 10.0)
                .put("ressenti_c", Math.round(w.feelsLike() * 10) / 10.0)
                .put("nature", w.natureTemperature())
                .put("ciel", w.description())
                .put("humidite_pct", w.humidity())
                .put("vent_kmh", Math.round(w.windSpeedKmh()));
        // mémorisation dans le cache avec l'instant de la mesure
        synchronized (cache) {
            cache.put(cle, new Object[]{System.currentTimeMillis(), resultat});
        }
        // on renvoie une copie, marquée comme provenant de l'API
        return new JSONObject(resultat.toString()).put("source", "api");
    }

    /** nombre d'appels réellement envoyés à l'API météo depuis le lancement */
    public static int getNbAppelsApi() {
        return nbAppelsApi;
    }
}
