package ollama;

import jade.core.Agent;
import jade.gui.AgentWindowed;
import jade.gui.SimpleWindow4Agent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AgentLLM  extends AgentWindowed {
    private HttpClient httpClient;
    private  String baseUrl;
    String modelName;

    /**
     * this main launch JADE plateforme and asks it to create an agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("blablaAgent:ollama.AgentLLM;");
        sbAgents.append("blablaAgent2:ollama.AgentLLM;");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.setButtonActivated(true);
        println("Hello! I'm an agent able to use LLM models. My name is " + getLocalName() + ". ");

        try {
        this.baseUrl = "http://localhost:11434";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String texteHello = "Hello everybody and especially you !";

        // Lister les modèles disponibles
        println("=== Available LLM models  ===");
        String[] models = listModels();
        for (String model : models) {
            println("- " + model);
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
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }

    public void onGuiEvent(jade.gui.GuiEvent gev) {

        println("~".repeat(50));
        // Test génération simple
        println("\n=== Test génération simple ===");
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
            println(prompt);
            String response = generateResponse(modelName, prompt);
            println("?".repeat(20));
            println("Réponse: " + response);

            println("~".repeat(50));

            // Test chat simple
            println("\n=== Test chat simple ===");
            println("(patientez quelques secondes si le modèle est volumineux)");
            println("System: Tu es un assistant utile et concis");
            println("User: Salut ! Il est tard, donne moi une idée de repas à faire..");
            println("?".repeat(20));
            String chatResponse = simpleChat(modelName,
                    "Tu es un assistant utile et concis",
                    "Salut ! Il est tard, donne moi une idée de repas à faire..");
            println("Chat: " + chatResponse);

            println("~".repeat(50));
            // Test chat avec historique
            println("\n=== Test chat avec historique ===");
            println("(patientez quelques secondes si le modèle est volumineux)");
            String[] history = {
                    "Donne moi une idée de diner rapide", "Je te propose des pates au pesto.",
                    "Je n'ai pas de pates, j'aime les oeufs", "Je te propose une omelette."
            };
            println("System: Tu es un assistant sympathique");
            println("Historique forcée :");
            println("User: Donne moi une idée de diner rapide --> Assistant: Je te propose des pates au pesto.");
            println("User: Je n'ai pas de pates, j'aime les oeufs --> Assistant: Je te propose une omelette.");
            println("---".repeat(20));
            println("prompt: Comment cuisiner le repas ?");
            println("?".repeat(20));
            String historyResponse = chatWithHistory(modelName,
                    "Tu es un assistant sympathique",
                    "Comment cuisiner le repas ?",
                    history);
            println( historyResponse);
            println("~".repeat(50));
        }
        catch (Exception e) {e.printStackTrace();}

    }


}
