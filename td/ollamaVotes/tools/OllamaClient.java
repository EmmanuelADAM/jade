package td.ollamaVotes.tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client minimal pour l'API /api/chat d'Ollama, avec sortie structuree (format = schema JSON).
 * Adresse et modele reglables par -Dollama.url=... et -Dollama.modele=...
 */
public class OllamaClient {
    /**client HTTP pour se connecter à OLLAMA*/
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)   // evite la negociation HTTP/2 inutile en local
            .connectTimeout(Duration.ofSeconds(5)).build();

    /**url de OLLAMA*/
    private final String url;
    /**nom du modèle LLM utilisé*/
    private final String modele;

    public OllamaClient() {
        this(System.getProperty("ollama.url", "http://localhost:11434"),
             System.getProperty("ollama.modele", "gemma4:31b-cloud"));//));//"llama3.2:3b"));""gpt-oss:120b-cloud
        //TODO: REMPLACER PAR UN MODELE PRESENT SUR VOTRE DISQUE OU ACCESSIBLE VIA VOTRE OLLAMA
    }

    public OllamaClient(String url, String modele) {
        this.url = url;
        this.modele = modele;
    }

    public String getModele() { return modele; }

    /**
     * Envoie une question au LLM et retourne sa reponse, deja parsee en objet JSON.
     * @param systeme     prompt systeme (la personnalite de l'agent)
     * @param question    message utilisateur
     * @param schema      schema JSON que la reponse doit respecter
     * @param temperature 0 = deterministe, 1 = tres variable
     */
    public JSONObject demander(String systeme, String question, JSONObject schema, double temperature)
            throws IOException, InterruptedException {
        JSONObject corps = new JSONObject()
                .put("model", modele)
                .put("stream", false)
                .put("format", schema)
                .put("options", new JSONObject().put("temperature", temperature))
                .put("messages", new JSONArray()
                        .put(new JSONObject().put("role", "system").put("content", systeme))
                        .put(new JSONObject().put("role", "user").put("content", question)));

        HttpRequest requete = HttpRequest.newBuilder(URI.create(url + "/api/chat"))
                .timeout(Duration.ofSeconds(90))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corps.toString()))
                .build();

        HttpResponse<String> reponse = HTTP.send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200)
            throw new IOException("Ollama a repondu " + reponse.statusCode() + " : " + reponse.body());

        //enleve ```json\n de la reponse si present...
        // en effet, certains LLM de haut niveau mettent en forme la sortie et précède de ```json etc.
        // pour afficher le retour en mode code json au lieu de simplement repondre le format json
        String body = correctBody(reponse.body());

        // le texte genere est dans message.content ; grace a "format" c'est du JSON
        String contenu = new JSONObject(body).getJSONObject("message").getString("content");
        return new JSONObject(contenu);
    }

    /**  enleve ```json\n de la reponse si present...
     *  en effet, certains LLM de haut niveau mettent en forme la sortie et précède de ```json etc.
     *  pour afficher le retour en mode code json au lieu de simplement repondre le format json
     * @param body le texte à nettoyer si besoin qui correspond à un format JSON
     * @return le code json sans mise en forme (passage à al ligne..)
     *  */
    private String correctBody(String body) {
        String newBody = ""+body;
        int presenceTag = body.indexOf("```json");
        if (presenceTag != -1) {
            newBody = body.replace("```json", "");
            newBody = newBody.replace("\\n", "");
        }
        return newBody;
    }
}
