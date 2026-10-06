package ollama.internationalCuisine.tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Outils pour dialoguer avec des modèles LLM via l'API d'Ollama :
 * liste des modèles, génération simple, chat avec historique (en streaming)
 */
public class OllamaTools {
    private static final Logger logger = Logger.getLogger(OllamaTools.class.getName());
    /**http client to ask ollama*/
    private final HttpClient httpClient;
    /**ollama url*/
    private final String baseUrl;

    /**
     * outils pour l'Ollama installé sur la machine (http://localhost:11434)
     */
    public OllamaTools() {
        this("http://localhost:11434");
    }

    /**
     * @param baseUrl url du serveur Ollama
     */
    public OllamaTools(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Méthode pour lister les modèles LLM disponibles sur la machine par l'API Ollama.
     * Attention ! Un modèle "cloud" n'est qu'un raccourci local (champ remote_model) vers ollama.com :
     * il n'est gardé que s'il est encore en service sur ollama.com (sinon erreur HTTP 410)
     */
    public String[] listModels() throws Exception {
        JSONArray modelsArray = getModels(baseUrl + "/api/tags");
        Set<String> activeCloudModels = null;
        List<String> modelNames = new ArrayList<>();
        for (int i = 0; i < modelsArray.length(); i++) {
            JSONObject model = modelsArray.getJSONObject(i);
            String remoteModel = model.optString("remote_model", null);
            if (remoteModel != null) {
                if (activeCloudModels == null) activeCloudModels = listActiveCloudModels();
                if (!activeCloudModels.contains(remoteModel)) continue; // modèle cloud retiré
            }
            //on est ici si le modele cloud est toujours actif, on l'ajoute à la liste
            modelNames.add(model.getString("name"));
        }
        return modelNames.toArray(new String[0]);
    }

    /**
     * Méthode pour lister les modèles cloud actuellement en service sur ollama.com
     * (ensemble vide si ollama.com est injoignable)
     */
    public Set<String> listActiveCloudModels() {
        Set<String> names = new HashSet<>();
        try {
            JSONArray modelsArray = getModels("https://ollama.com/api/tags");
            for (int i = 0; i < modelsArray.length(); i++)
                names.add(modelsArray.getJSONObject(i).getString("name"));
        } catch (Exception e) {
            logger.warning("ollama.com injoignable, les modèles cloud sont ignorés : " + e.getMessage());
        }
        return names;
    }

    /**
     * interroge une API /api/tags et retourne le tableau "models" de la réponse
     * retourne les modèles présents ou référencés par ollama installé sur la machine
     */
    private JSONArray getModels(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return new JSONObject(response.body()).getJSONArray("models");
        }
        throw new RuntimeException("Erreur HTTP: " + response.statusCode() + " - " + response.body());
    }

    /**
     * un modèle d'embedding ne sait pas discuter
     */
    public static boolean isChatModel(String model) {
        return !model.contains("embed");
    }

    /**
     * les modèles de la liste capables de discuter (sans les modèles d'embedding)
     */
    public static String[] chatModels(String[] models) {
        return Arrays.stream(models).filter(OllamaTools::isChatModel).toArray(String[]::new);
    }

    /**
     * choisit de préférence un modèle de chat qui ne soit pas
     * un modèle d'embedding (ils ne savent pas discuter)
     */
    public static String chooseModel(String[] models) {
        for (String m : models)
            if (isChatModel(m))
                return m;
        //au pire, tant pis, on retoure le 1er de la liste
        return models[0];
    }

    /**
     * Méthode pour générer une réponse simple (non chat) avec un modèle donné
     *
     * @param model  le nom du modèle LLM à utiliser
     * @param prompt le texte d'entrée pour la génération
     *
     */
    public String generateResponse(String model, String prompt) throws Exception {
        // Construction du JSON avec org.json
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", model);
        jsonRequest.put("prompt", prompt);
        jsonRequest.put("stream", false);
        jsonRequest.put("think", false); // pas de phase de réflexion (modèles "thinking" comme qwen3.5)
        jsonRequest.put("keep_alive", "30m"); // le modèle reste chargé en mémoire 30 min après la dernière question

        // Création de la requête HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .timeout(Duration.ofMinutes(5))
                .build();

        // Envoi de la requête
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Traitement de la réponse avec org.json
        if (response.statusCode() == 200) { //ok
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getString("response");
        } else {
            throw new RuntimeException("Erreur HTTP: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Méthode pour un chat avec historique
     *
     * @param model            le nom du modèle LLM à utiliser
     * @param systemPrompt     le prompt système (instructions pour le modèle)
     * @param userMessage      le message utilisateur actuel
     * @param previousMessages un tableau de messages précédents (alternance personne/assistant)
     *
     */
    public String chatWithHistory(String model, String systemPrompt,
                                  String userMessage, String[] previousMessages) throws Exception {
        return chatWithHistory(model, systemPrompt, userMessage, previousMessages, null);
    }

    /**
     * Méthode pour un chat avec historique, la réponse étant reçue morceau par morceau (streaming)
     * effet "à la chat gpt"
     *
     * @param model            le nom du modèle LLM à utiliser
     * @param systemPrompt     le prompt système (instructions pour le modèle)
     * @param userMessage      le message utilisateur actuel
     * @param previousMessages un tableau de messages précédents (alternance personne/assistant)
     * @param onChunk          appelé à chaque morceau de réponse reçu (ex. pour l'afficher), peut être null
     * @return la réponse complète
     *
     */
    public String chatWithHistory(String model, String systemPrompt,
                                  String userMessage, String[] previousMessages,
                                  Consumer<String> onChunk) throws Exception {

        // Construction du JSON pour l'API chat
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", model);
        jsonRequest.put("stream", true); // la réponse arrive morceau par morceau, une ligne JSON par morceau
        jsonRequest.put("think", false); // pas de phase de réflexion (modèles "thinking" comme qwen3.5)
        jsonRequest.put("keep_alive", "30m"); // le modèle reste chargé en mémoire 30 min après la dernière question

        // Construction du tableau de messages
        JSONArray messages = new JSONArray();

        // Message système
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.put(message("system", systemPrompt));
        }

        // Ajout des messages précédents (historique) : alternance utilisateur / assistant
        if (previousMessages != null) {
            for (int i = 0; i + 1 < previousMessages.length; i += 2) {
                messages.put(message("user", previousMessages[i]));
                messages.put(message("assistant", previousMessages[i + 1]));
            }
        }

        // Message utilisateur actuel
        messages.put(message("user", userMessage));

        jsonRequest.put("messages", messages);

        // Envoi de la requête
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                .timeout(Duration.ofMinutes(5)) //on relance dans 5mn si pas de reponse
                .build();

        // normalement, la réponse tient compte de l'historique
        HttpResponse<Stream<String>> response = httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Erreur HTTP: " + response.statusCode() + " - "
                    + response.body().collect(Collectors.joining("\n")));
        }
        //traitement de la reponse par morceaux
        StringBuilder fullResponse = new StringBuilder();
        try (Stream<String> lines = response.body()) {
            for (String line : (Iterable<String>) lines::iterator) {
                if (line.isBlank()) continue;
                JSONObject chunk = new JSONObject(line);
                if (chunk.has("error")) throw new RuntimeException("Erreur Ollama: " + chunk.getString("error"));
                String content = chunk.getJSONObject("message").getString("content");
                fullResponse.append(content);
                if (onChunk != null) onChunk.accept(content);
            }
        }
        return fullResponse.toString();
    }

    /**
     * un message du chat
     *
     * @param role "system", "user" ou "assistant"
     */
    private static JSONObject message(String role, String content) {
        return new JSONObject().put("role", role).put("content", content);
    }
}
