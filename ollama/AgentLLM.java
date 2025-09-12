package ollama;

import jade.core.Agent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import org.json.JSONObject;
import org.json.JSONArray;

public class AgentLLM  extends Agent {
    private HttpClient httpClient;
    private  String baseUrl;

    /**
     * this main launch JADE plateforme and asks it to create an agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("blablaAgent:ollama.AgentLLM");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * agent set-up
     */
    @Override
    protected void setup() {

        try {
            OllamaClient01 client = new OllamaClient01("http://localhost:11434");
        this.baseUrl = "http://localhost:11434";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String texteHello = "Hello everybody and especially you !";

        println("From agent " + getLocalName() + " : " + texteHello);
        println("My address is " + getAID());
        // Lister les modèles disponibles
        System.out.println("=== Modèles disponibles ===");
        String[] models = client.listModels();
        for (String model : models) {
            System.out.println("- " + model);
        }

        if (models.length > 0) {
            String modelName = models[0];
            System.out.println("\n=== Utilisation du modèle: " + modelName + " ===");

            // Informations sur le modèle
            try {
                OllamaClient01.ModelInfo info = client.getModelInfo(modelName);
                System.out.println("Info modèle: " + info);
            } catch (Exception e) {
                System.out.println("Impossible d'obtenir les infos du modèle");
            }

            // Test génération simple
            System.out.println("\n=== Test génération simple ===");
            // Test génération simple
            System.out.println("\n=== Test génération simple ===");
            String texte = """
                        -> start a vote for these options Pizza,Vegetables,RedGrill,Sushi,FishAndFish,
                        ----------------------------------------
                                participant found : [votant_4, votant_9, votant_5, votant_0, votant_3, votant_2, votant_1, votant_7, votant_6, votant_8]
                ----------------------------------------
                        agent votant_4 proposes FishAndFish>Vegetables>RedGrill>Pizza>Sushi
                agent votant_5 proposes Vegetables>FishAndFish>RedGrill>Pizza>Sushi
                agent votant_9 proposes FishAndFish>Vegetables>Pizza>Sushi>RedGrill
                agent votant_3 proposes Pizza>RedGrill>Vegetables>Sushi>FishAndFish
                agent votant_0 proposes FishAndFish>Sushi>Pizza>Vegetables>RedGrill
                agent votant_2 proposes RedGrill>Vegetables>FishAndFish>Sushi>Pizza
                agent votant_1 proposes Sushi>RedGrill>FishAndFish>Vegetables>Pizza
                agent votant_7 proposes Vegetables>Pizza>RedGrill>Sushi>FishAndFish
                agent votant_6 proposes RedGrill>Sushi>Vegetables>FishAndFish>Pizza
                agent votant_8 proposes Sushi>FishAndFish>Vegetables>RedGrill>Pizza
                        ----------------------------------------
                Pizza obtained 23 points
                Vegetables obtained 35 points
                RedGrill obtained 31 points
                Sushi obtained 28 points
                FishAndFish obtained 33 points
                        ----------------------------------------
[[[ Voting result [Vegetables]]]]
                ----------------------------------------
                        ~~~~~~~~~~~~~~~~~~~~
                                vote has been accepted by votant_4
                vote has been accepted by votant_5
                vote has been accepted by votant_3
                vote has been accepted by votant_0
                vote has been accepted by votant_2
                vote has been accepted by votant_9
                vote has been accepted by votant_6
                vote has been accepted by votant_7
                vote has been accepted by votant_8
                vote has been accepted by votant_1
                """;
            String response = client.generateResponse(modelName, "give a summary of this vote, without showing the details of the vote by agent, juste the result and the 3 first choices : "+texte);
//                String response = client.generateResponse(modelName, "Explique-moi brièvement ce qu'est JSON");
            System.out.println("Réponse: " + response);

        }
        } catch (Exception e) {e.printStackTrace();}

                //agent asks to be removed from the platform
        //doDelete();

    }

    // 'clean-up' of the agent
    @Override
    protected void takeDown() {
        println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }
}
