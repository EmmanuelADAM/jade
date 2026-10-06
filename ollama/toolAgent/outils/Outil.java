package ollama.toolAgent.outils;

import org.json.JSONObject;

/**
 * Un OUTIL que l'agent LLM peut demander à utiliser.
 * <p>
 * Le LLM ne sait que produire du texte : il ne connaît ni la date du jour, ni la météo,
 * ni le calendrier scolaire. Il peut seulement DEMANDER à l'agent d'exécuter un outil,
 * en respectant le petit protocole JSON défini dans {@link ollama.toolAgent.llm.Protocole}.
 * <p>
 * Pour être utilisable, un outil doit se décrire (nom, description, paramètres) :
 * c'est cette "fiche" qui est donnée au LLM dans le prompt système.
 * Une bonne description est essentielle : c'est elle qui permet au LLM de choisir le bon outil !
 */
public interface Outil {

    /** nom unique de l'outil (sans espace), ex. "meteo" */
    String nom();

    /** à quoi sert l'outil, quand l'utiliser (texte lu par le LLM) */
    String description();

    /**
     * paramètres attendus : nom du paramètre -> description (type, exemple, valeur par défaut)
     * ex. {"ville": "nom de la ville (texte), ex. \"Lille\""}
     * un objet vide s'il n'y a pas de paramètre
     */
    JSONObject parametres();

    /**
     * exécute l'outil
     *
     * @param arguments les arguments fournis par le LLM (peuvent être incomplets ou faux !)
     * @return le résultat, au format JSON
     * @throws IllegalArgumentException si un argument est absent ou invalide (le message sera renvoyé au LLM)
     * @throws Exception                si l'outil échoue (réseau, ...)
     */
    JSONObject executer(JSONObject arguments) throws Exception;

    /** fiche descriptive de l'outil, donnée au LLM */
    default JSONObject fiche() {
        return new JSONObject()
                .put("nom", nom())
                .put("description", description())
                .put("parametres", parametres());
    }
}
