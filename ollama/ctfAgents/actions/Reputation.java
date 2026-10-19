package ollama.ctfAgents.actions;

import org.json.JSONObject;

import java.util.*;

/**
 * Réputation des décodeurs automatiques, tenue par le PROGRAMME du Capitaine (pas par le LLM) :
 * +1 pour chaque agent d'une chaîne dont le résultat a donné un flag validé par l'arbitre,
 * -1 si le flag a été refusé. Elle est montrée au LLM à chaque consultation.
 * <p>
 * Partagée par les actions consulter_tous (affichage), retenir (dernier choix) et proposer_flag (mise à jour).
 */
public class Reputation {
    private final Map<String, Integer> scores = new TreeMap<>();
    /** texte du dernier candidat retenu et agents qui l'ont produit */
    private String dernierTexte;
    private List<String> dernierPar = List.of();

    public int de(String agent) {
        return scores.getOrDefault(agent, 0);
    }

    public void retenir(String texte, List<String> par) {
        dernierTexte = texte;
        dernierPar = List.copyOf(par);
    }

    /**
     * appelé après la réponse de l'arbitre : si le flag vient du dernier candidat retenu, on met à jour la réputation
     *
     * @return les agents concernés (vide si le flag ne vient pas du dernier candidat)
     */
    public List<String> resultat(String flag, boolean accepte) {
        if (dernierTexte == null || !dernierTexte.contains(flag)) return List.of();
        Set<String> agents = new LinkedHashSet<>(dernierPar);
        for (String a : agents) scores.merge(a, accepte ? 1 : -1, Integer::sum);
        dernierTexte = null;
        return List.copyOf(agents);
    }

    public JSONObject enJson() {
        return new JSONObject(scores);
    }
}
