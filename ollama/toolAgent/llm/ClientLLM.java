package ollama.toolAgent.llm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Client minimal pour l'API /api/chat d'Ollama, avec sortie structurée (réponse imposée par un schéma JSON).
 * Adresse et modèle réglables par -Dollama.url=... et -Dollama.modele=...
 */
public class ClientLLM {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5)).build();

    /** url de Ollama */
    private final String url;
    /** nom du modèle LLM utilisé */
    private String modele;

    public ClientLLM() {
        this(System.getProperty("ollama.url", "http://localhost:11434"),
                System.getProperty("ollama.modele", "gemma4:31b-cloud"));//llama3.2:3b"));
        //TODO: REMPLACER PAR UN MODELE PRESENT SUR VOTRE DISQUE OU ACCESSIBLE VIA VOTRE OLLAMA
    }

    public ClientLLM(String url, String modele) {
        this.url = url;
        this.modele = modele;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    /**
     * envoie tout le dialogue au LLM et retourne sa réponse, déjà transformée en objet JSON
     *
     * @param messages le dialogue (system, user, assistant, user...)
     * @param schema   schéma JSON que la réponse doit respecter (sous forme de texte, pour garder l'ordre des champs)
     * @return un objet JSON
     */
    public JSONObject discuter(List<JSONObject> messages, String schema) throws IOException, InterruptedException {
        JSONObject corps = new JSONObject()
                .put("model", modele)
                .put("stream", false)
                .put("think", false)          // pas de phase de réflexion pour les modèles "thinking"
                .put("keep_alive", "30m")     // le modèle reste chargé 30 min
                .put("format", "__SCHEMA__")  // la réponse DOIT respecter ce schéma (inséré tel quel plus bas)
                .put("options", new JSONObject().put("temperature", 0)) // réponses stables, plus fiables pour les outils
                .put("messages", new JSONArray(messages)); //l'historique des messages

        // on autorise 3mn d'attente pour la reponse....
        HttpRequest requete = HttpRequest.newBuilder(URI.create(url + "/api/chat"))
                .timeout(Duration.ofMinutes(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        corps.toString().replace("\"__SCHEMA__\"", schema))) // schéma inséré sans changer l'ordre des champs
                .build();

        HttpResponse<String> reponse = HTTP.send(requete, HttpResponse.BodyHandlers.ofString());
        if (reponse.statusCode() != 200)
            throw new IOException("Ollama a répondu " + reponse.statusCode() + " : " + reponse.body());

        String contenu = new JSONObject(reponse.body()).getJSONObject("message").getString("content");
        return new JSONObject(extraireJson(contenu));
    }

    /**
     * certains modèles entourent le JSON de ```json ... ``` ou de texte :
     * on ne garde que ce qui est entre la première '{' et la dernière '}'
     * normalement inutile ici
     */
    static String extraireJson(String texte) {
        int debut = texte.indexOf('{');
        int fin = texte.lastIndexOf('}');
        if (debut < 0 || fin < debut) throw new IllegalStateException("pas de JSON dans la réponse : " + texte);
        return texte.substring(debut, fin + 1);
    }
}
