package ollama;

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

public class AgentLLM  extends GuiAgent {
    private HttpClient httpClient;
    private  String baseUrl;
    String modelName;
    GuiOllamaAgent window;

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

    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        window = new GuiOllamaAgent( this);
        window.println("Hello! I'm an agent able to use LLM models. My name is " + getLocalName() + ". ");

        try {
        this.baseUrl = "http://localhost:11434";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String texteHello = "Hello everybody and especially you !";

        // Lister les modèles disponibles
        window.println("=== Available LLM models  ===");
        String[] models = listModels();
        for (String model : models) {
            window.println("- " + model);
        }
            modelName = models[0];
        } catch (Exception e) {e.printStackTrace();}


                //agent asks to be removed from the platform
        //doDelete();

    }
    /**
     * Méthode pour lister les modèles LLM disponibles sur la machine par l'API Ollama
     */
    public String[] listModels() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/tags"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray modelsArray = jsonResponse.getJSONArray("models");

            String[] modelNames = new String[modelsArray.length()];
            for (int i = 0; i < modelsArray.length(); i++) {
                JSONObject model = modelsArray.getJSONObject(i);
                modelNames[i] = model.getString("name");
            }
            return modelNames;
        }
        return new String[0];
    }

    /**
     * Méthode pour générer une réponse simple (non chat) avec un modèle donné
     * @param model  le nom du modèle LLM à utiliser
     * @param prompt le texte d'entrée pour la génération
     * */
    public String generateResponse(String model, String prompt) throws Exception {
        // Construction du JSON avec org.json
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", model);
        jsonRequest.put("prompt", prompt);
        jsonRequest.put("stream", false);

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
     * @param model            le nom du modèle LLM à utiliser
     * @param systemPrompt     le prompt système (instructions pour le modèle)
     * @param userMessage      le message utilisateur actuel
     * @param previousMessages un tableau de messages précédents (alternance personne/assistant)
     * */
    public String chatWithHistory(String model, String systemPrompt,
                                  String userMessage, String[] previousMessages) throws Exception {

        // Construction du JSON pour l'API chat
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("model", model);
        jsonRequest.put("stream", false);

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
                .timeout(Duration.ofMinutes(5))
                .build();

        // normalement, la réponse tient compte de l'historique
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONObject message = jsonResponse.getJSONObject("message");
            return message.getString("content");
        } else {
            throw new RuntimeException("Erreur HTTP: " + response.statusCode());
        }
    }


    /**
     * * Méthode pour un chat simple sans historique
     * @param model        le nom du modèle LLM à utiliser
     * @param systemPrompt le prompt système (instructions pour le modèle)
     * @param userMessage  le message utilisateur actuel
     */
    public String simpleChat(String model, String systemPrompt, String userMessage) throws Exception {
        return chatWithHistory(model, systemPrompt, userMessage, null);
    }

    // 'clean-up' of the agent
    @Override
    protected void takeDown() {
        window.println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }

    private void sample2()
    {
        try {
            // Test chat avec historique
            window.println("\n=== Test chat avec historique ===");
            window.println("(patientez quelques secondes si le modèle est volumineux)");
            String[] history = {
                    "que manger quand il fait froid ?", "Je  propose du cassoulet ou de la choucroute, mais c'est un peu lourd et long à préparer.",
                    "de la raclette ?", "oui, de la raclette est aussi un plat préféré quand il fait froid et il est rapide à préparer.",
                    "que manger quand il fait très chaud ?", "pourquoi pas une salade garnie d'oeufs, tomates ?", "oui, les tomates j'aime bien.",
                    "alors du gazpacho manger froid est très bon l'été", "Je suis dans le nord de la france.", "Alors un pojtelevech : morceaux de viande de poule, lapin, porc et parfois veau consommés froids et pris dans de la gelée culinaire légèrement vinaigrée.",
                    "C'est le début de l'automne, que manger ?", "S'il fait frais, une carbonade flamande réchauffe; ou un lapin au pruneau et pain d'épice."
            };
            window.println("System: Tu es un assistant sympathique");
            window.println("Historique forcée :");
            for(int i=0; i<history.length; i+=2)
                window.println("User: %s  --> Assistant: %s".formatted(history[i], history[i+1]));
            window.println("---".repeat(20));
            window.println("prompt: il fait 12° et nuageux. que cuisiner ? et comment ?");
            window.println("?".repeat(20));
            String historyResponse = chatWithHistory(modelName,
                    "Tu es un assistant sympathique",
                    "Comment cuisiner le repas ?",
                    history);
            window.println( historyResponse);
            window.println("~".repeat(50));
        }
        catch (Exception e) {e.printStackTrace();}
    }
    private void sample3(String query)
    {
        try {
            // Test chat avec historique
            window.println("\n=== Test chat avec historique ===", true);
            window.println("(patientez quelques secondes si le modèle est volumineux)", true);
            String[] history = {
                    "que manger quand il fait froid ?", "Je  propose du cassoulet ou de la choucroute, mais c'est un peu lourd et long à préparer.",
                    "de la raclette ?", "oui, de la raclette est aussi un plat préféré quand il fait froid et il est rapide à préparer.",
                    "que manger quand il fait très chaud ?", "pourquoi pas une salade garnie d'oeufs, tomates ?",
                    "oui, les tomates j'aime bien.", "alors du gazpacho manger froid est très bon l'été",
                    "Je suis dans le nord de la france.", "Alors un pojtelevech : morceaux de viande de poule, lapin, porc et parfois veau consommés froids et pris dans de la gelée culinaire légèrement vinaigrée.",
                    "C'est le début de l'automne, que manger ?", "S'il fait frais, une carbonade flamande réchauffe; ou un lapin au pruneau et pain d'épice."
            };
            window.println("System: Tu es un assistant sympathique", true);
            window.println("Historique forcée :", true);
            for(int i=0; i<history.length; i+=2)
                window.println("User: %s  --> Assistant: %s".formatted(history[i], history[i+1]), true);
            window.println("---".repeat(20), true);
            window.println("->" + query, true);
            window.println("?".repeat(20), true);
            String historyResponse = chatWithHistory(modelName,
                    "Tu es un assistant inventif et sympathique. Tu proposes des recettes de cuisine en fonction du temps. l'utilisateur est dans le nord de la france.",
                    query,
                    history);
            window.println( historyResponse, true);
            window.println("~".repeat(50), true);
        }
        catch (Exception e) {e.printStackTrace();}
    }

    /**
     * * Exemple d'utilisation des différentes méthodes
     * */
    private void sample1() {
        window.println("~".repeat(50));
        // Test génération simple
        window.println("\n=== Test génération simple ===");
        String texte = """ 
                        during a vote for a restaurant, the following results were obtained :
                        ----------------------------------------
                Pizza obtained 23 points
                Vegetables obtained 35 points
                RedGrill obtained 31 points
                Sushi obtained 28 points
                FishAndFish obtained 33 points
                        ----------------------------------------
                [[[ Voting result [Vegetables]]]]
                """;
        try {
            var prompt = "give a summary of this vote : the top 3 results, and the winner choice: " + texte;
            window.println(prompt,true);
            String response = generateResponse(modelName, prompt);
            window.println("?".repeat(20), true);
            window.println("Réponse: " + response, true);

            window.println("~".repeat(50), true);

        }
        catch (Exception e) {e.printStackTrace();}
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case GuiOllamaAgent.SENDQUERY -> sample3(window.lowTextArea.getText());
            case GuiOllamaAgent.QUITCODE ->
                    {
                        window.dispose();
                        doDelete();
                        System.exit(0);
                    }
        }
    }

}
