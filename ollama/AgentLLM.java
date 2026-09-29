package ollama;

import jade.core.behaviours.WakerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import ollama.gui.GuiOllamaAgent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * agent qui va interroger un modèle LLM via Ollama et une API Météo
 * pour démo.. les échanges tournent sur cet exemple sur la cuisine
 *
 */
public class AgentLLM extends GuiAgent {
    private HttpClient httpClient;
    private String baseUrl;
    String modelName;
    GuiOllamaAgent window;
    /**
     * ville de l'utilisateur, modifiable dans la fenêtre
     */
    String ville = "Valenciennes";
    /**
     * météo actuelle de la ville, donnée par l'API OpenWeatherMap
     */
    String laMeteo = "inconnue";
    /**
     * latitude de la ville (> 0 : hémisphère nord), donnée avec la météo
     */
    double latitude = 50.36;


    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        window = new GuiOllamaAgent(this);
        window.println("Hello! I'm an agent able to use LLM models. My name is " + getLocalName() + ". ");

        try {//tentative de connection à OLLAMA
            this.baseUrl = "http://localhost:11434";
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            // Lister les modèles disponibles
            window.println("=== Available LLM models  ===");
            String[] models = listModels();
            for (String model : models) {
                window.println("- " + model);
            }
            // choix par defaut du premier modele nom embedded
            modelName = chooseModel(models);
            window.println("modèle utilisé : " + modelName);
            // les modèles d'embedding ne savent pas discuter, ils ne sont pas proposés
            window.setModels(Arrays.stream(models).filter(m -> !m.contains("embed")).toArray(String[]::new), modelName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // météo de la ville par défaut
        window.setCity(ville);
        demanderMeteo(ville);

    }

    /**
     * Méthode pour lister les modèles LLM disponibles sur la machine par l'API Ollama.
     * Attention ! Un modèle "cloud" n'est qu'un raccourci local (champ remote_model) vers ollama.com :
     * il n'est gardé que s'il est encore en service sur ollama.com (sinon erreur HTTP 410)
     */
    public String[] listModels() throws Exception {
        JSONArray modelsArray = getModels(baseUrl + "/api/tags");
        Set<String> activeCloudModels = null; // chargé seulement s'il y a des modèles cloud

        List<String> modelNames = new ArrayList<>();
        for (int i = 0; i < modelsArray.length(); i++) {
            JSONObject model = modelsArray.getJSONObject(i);
            String remoteModel = model.optString("remote_model", null);
            if (remoteModel != null) {
                if (activeCloudModels == null) activeCloudModels = listActiveCloudModels();
                if (!activeCloudModels.contains(remoteModel)) continue; // modèle cloud retiré
            }
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
            window.println("ollama.com injoignable, les modèles cloud sont ignorés : " + e.getMessage());
        }
        return names;
    }

    /**
     * interroge une API /api/tags et retourne le tableau "models" de la réponse
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
     * choisit de préférence un modèle local de chat (les modèles cloud listés sont actifs, mais dépendent du réseau)
     * les modèles d'embedding ne savent pas discuter
     */
    private String chooseModel(String[] models) {
        for (String m : models)
            if (!m.contains("embed")) // && !m.contains("cloud"))
                return m;
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
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.put(systemMessage);
        }

        // Ajout des messages précédents (historique)
        if (previousMessages != null) {
            for (int i = 0; i < previousMessages.length; i += 2) {
                if (i + 1 < previousMessages.length) {
                    // Message utilisateur
                    JSONObject userMsg = new JSONObject();
                    userMsg.put("role", "user");
                    userMsg.put("content", previousMessages[i]);
                    messages.put(userMsg);

                    // Message assistant
                    JSONObject assistantMsg = new JSONObject();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", previousMessages[i + 1]);
                    messages.put(assistantMsg);
                }
            }
        }

        // Message utilisateur actuel
        JSONObject currentUserMessage = new JSONObject();
        currentUserMessage.put("role", "user");
        currentUserMessage.put("content", userMessage);
        messages.put(currentUserMessage);

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


    // 'clean-up' of the agent
    @Override
    protected void takeDown() {
        window.println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }


    /**pose une question au modele et y joint :
     * - la ville et sa meteo actuelle
     * - la saison selon la position de la ville
     * - un historique des échanges (sur la nourriture)*/
    private void queryBienEntouree(String query) {
        addBehaviour(new WakerBehaviour(this, 100) {
            public void onWake() {
                try {
                    // Test chat avec historique
                    window.println("\n=== Test chat avec historique ===", true);
                    window.println("(patientez quelques secondes si le modèle est volumineux)", true);
                    String[] history = {
                            "que manger quand il fait froid ?", "Je  propose du cassoulet ou de la choucroute, mais c'est un peu lourd et long à préparer.",
                            "de la raclette ?", "oui, de la raclette est aussi un plat préféré quand il fait froid et il est rapide à préparer.",
                            "que manger quand il fait très chaud ?", "pourquoi pas une salade garnie d'oeufs, tomates ?",
                            "oui, les tomates j'aime bien.", "alors du gazpacho manger froid est très bon l'été",
                            "Je suis originaire du nord de la france.", "Alors un pojtelevech : morceaux de viande de poule, lapin, porc et parfois veau consommés froids et pris dans de la gelée culinaire légèrement vinaigrée.",
                            "Parfois l'automne je ne sais que que manger ?", "S'il fait frais, une carbonade flamande réchauffe; ou un lapin au pruneau et pain d'épice.",
                            "Au printemps, j'adore les asperges", "oui, à la saison asperges vertes ou blanches..  il existe de nombreuses façon de les cuisiner"
                    };
                    // météo actuelle de la ville au moment de la question
                    demanderMeteo(ville);
                    String caracteristiques = "Tu es un assistant inventif et sympathique. Tu proposes des recettes de cuisine en fonction du temps et de la saison. L'utilisateur est à " + ville + ".";
                    caracteristiques += " Nous sommes en " + saison(latitude, LocalDate.now())
                            + " (hémisphère " + (latitude >= 0 ? "nord" : "sud") + ").";
                    caracteristiques += " La météo actuelle à " + ville + " est : " + laMeteo + ".";
//            String caracteristiques = "Tu es un assistant strict et autoritaire. L'utilisateur parle le Chti, dialecte du nord de la france.";
                    window.println("System: " + caracteristiques, true);

                    window.println("Historique forcée :", true);
                    for (int i = 0; i < history.length; i += 2)
                        window.println("User: %s  --> Assistant: %s".formatted(history[i], history[i + 1]), true);
                    window.println("---".repeat(20), true);
                    window.println("->" + query, true);
                    window.println("?".repeat(20), true);
                    // animation jusqu'au 1er morceau de réponse (chargement du modèle, lecture du prompt)
                    window.startWaiting("chargement de " + modelName + "...");
                    boolean[] started = {false};
                    // la réponse s'affiche au fur et à mesure qu'elle est générée
                    try {
                        chatWithHistory(modelName,
                                caracteristiques,
                                query,
                                history,
                                chunk -> {
                                    if (!started[0]) {
                                        started[0] = true;
                                        window.stopWaiting();
                                    }
                                    window.print(chunk);
                                });
                    } finally {
                        window.stopWaiting(); // même en cas d'erreur
                    }
//            String response = chatWithHistory(modelName, caracteristiques, query,  null);
                    window.println("");
                    window.println("~".repeat(50), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * récupère la météo actuelle d'une ville par l'API OpenWeatherMap (classe Meteo) ;
     * la ville et la météo sont mémorisées pour les prochaines questions au modèle
     */
    private void demanderMeteo(String nouvelleVille) {
        window.println("demande de la météo à " + nouvelleVille + "...");
        Meteo.WeatherData weather = new Meteo().getWeatherByCity(nouvelleVille);
        if (weather != null && weather.isValid()) {
            ville = nouvelleVille;
            latitude = weather.getLatitude();
            // ex. "chaud, 22,3°C (ressenti 21,8°C), ciel dégagé, humidité 60%, vent 12 km/h"
            laMeteo = "%s, %.1f°C (ressenti %.1f°C), %s, humidité %d%%, vent %.0f km/h".formatted(
                    natureTemperature(weather.getTemperature()), weather.getTemperature(), weather.getFeelsLike(),
                    weather.getDescription(), weather.getHumidity(), weather.getWindSpeedKmh());
            window.println("météo à " + ville + " : " + laMeteo);
        } else {
            window.println("données météo non disponibles pour " + nouvelleVille + " (on garde " + ville + ")");
            window.setCity(ville);
        }
    }

    /**
     * saison à une date donnée, selon l'hémisphère (dates approximatives des équinoxes et solstices) :
     * les saisons de l'hémisphère sud sont inversées par rapport à celles de l'hémisphère nord
     *
     * @param latitude latitude du lieu (> 0 : hémisphère nord, < 0 : hémisphère sud)
     * @param date     la date
     * @return "printemps", "été", "automne" ou "hiver"
     */
    static String saison(double latitude, LocalDate date) {
        MonthDay jour = MonthDay.from(date);
        String saisonNord;
        if (jour.isBefore(MonthDay.of(3, 21))) saisonNord = "hiver";
        else if (jour.isBefore(MonthDay.of(6, 21))) saisonNord = "printemps";
        else if (jour.isBefore(MonthDay.of(9, 23))) saisonNord = "été";
        else if (jour.isBefore(MonthDay.of(12, 21))) saisonNord = "automne";
        else saisonNord = "hiver";
        if (latitude >= 0) return saisonNord;
        return switch (saisonNord) {
            case "hiver" -> "été";
            case "printemps" -> "automne";
            case "été" -> "hiver";
            default -> "printemps";
        };
    }

    /**
     * nature d'une température (froid, tempéré, chaud...)
     */
    private String natureTemperature(double temp) {
        if (temp < 0) return "très froid";
        if (temp < 10) return "froid";
        if (temp < 17) return "tempéré";
        if (temp < 26) return "chaud";
        if (temp < 35) return "très chaud";
        return "extrêmement chaud";
    }


    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case GuiOllamaAgent.SENDQUERY -> queryBienEntouree(window.lowTextArea.getText());
            case GuiOllamaAgent.CHANGEMODEL -> {
                modelName = (String) ev.getParameter(0);
                window.println("modèle utilisé : " + modelName);
            }
            case GuiOllamaAgent.CHANGECITY -> demanderMeteo((String) ev.getParameter(0));
            case GuiOllamaAgent.QUITCODE -> {
                window.dispose();
                doDelete();
                System.exit(0);
            }
        }
    }

    /**
     * this main launch JADE plateforme and asks it to create an agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("blablaAgent:ollama.AgentLLM;");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

}
