package ollama.ctfAgents.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import ollama.ctfAgents.commun.Ctf;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

/**
 * Agent ANALYSTE : agent "pur" (sans LLM) qui permet d'interroger un fichier CSV.
 * <p>
 * Au démarrage, l'agent charge en mémoire un fichier CSV (séparateur {@code ;}, première ligne
 * = noms des colonnes) situé dans le dossier du CTF, puis s'inscrit au DF sous le type
 * {@link Ctf#ANALYSTE} en publiant le nom du fichier, ses colonnes et les opérations disponibles.
 * <p>
 * <b>Argument de lancement</b> : {@code [0]} nom du fichier, par défaut {@code journal_connexions.csv}.
 *
 * <h2>Jeu courant</h2>
 * Chaque demandeur (identifié par son nom local) possède son propre <i>jeu courant</i> :
 * l'ensemble des lignes restantes après ses filtres successifs. À la première requête, ce jeu
 * correspond au fichier complet. Les filtres se cumulent ; seule l'opération
 * {@code reinitialiser} repart du fichier complet. Plusieurs équipes peuvent ainsi analyser
 * le même fichier en parallèle sans se gêner.
 *
 * <h2>Protocole</h2>
 * L'agent ne traite que les messages {@code REQUEST} dont le contenu est un objet JSON
 * contenant au moins le champ {@code "operation"}. Réponses :
 * <ul>
 *     <li>{@code INFORM} avec {@code "ok": true} et le résultat de l'opération ;</li>
 *     <li>{@code FAILURE} avec {@code "ok": false} et un champ {@code "erreur"} (opération ou colonne inconnue, paramètre manquant) ;</li>
 *     <li>{@code NOT_UNDERSTOOD} si le message n'est pas un {@code REQUEST}.</li>
 * </ul>
 *
 * <h2>Opérations</h2>
 * <table border="1">
 *     <caption>Opérations reconnues</caption>
 *     <tr><th>operation</th><th>paramètres</th><th>effet / réponse</th></tr>
 *     <tr><td>{@code colonnes}</td><td>aucun</td>
 *         <td>noms des colonnes et nombre total de lignes du fichier</td></tr>
 *     <tr><td>{@code reinitialiser}</td><td>aucun</td>
 *         <td>le jeu courant redevient le fichier complet</td></tr>
 *     <tr><td>{@code filtrer}</td><td>{@code colonne} + {@code valeur} (égalité, insensible à la casse)
 *         <br>ou {@code colonne} + {@code contient} (sous-chaîne, sensible à la casse)</td>
 *         <td>restreint le jeu courant ; renvoie le nombre de lignes avant/après et un aperçu de 3 lignes</td></tr>
 *     <tr><td>{@code compter_par}</td><td>{@code colonne}, {@code top} (défaut 5)</td>
 *         <td>les {@code top} valeurs les plus fréquentes de la colonne dans le jeu courant</td></tr>
 *     <tr><td>{@code apercu}</td><td>{@code n} (défaut 5)</td>
 *         <td>les {@code n} premières lignes du jeu courant</td></tr>
 * </table>
 *
 * <h2>Exemples de requêtes</h2>
 * <pre>
 * {"operation":"filtrer", "colonne":"resultat", "valeur":"ECHEC"}
 * {"operation":"filtrer", "colonne":"horodatage", "contient":"T02:"}
 * {"operation":"compter_par", "colonne":"ip", "top":3}
 * {"operation":"apercu", "n":5}
 * </pre>
 */
public class AgentAnalyste extends Agent {
    /** nom du fichier CSV analysé (relatif au dossier du CTF) */
    private String fichier = "journal_connexions.csv";
    /** noms des colonnes, lus sur la première ligne du fichier */
    private List<String> colonnes = List.of();
    /** contenu complet du fichier : une map colonne -> valeur par ligne (non modifiable) */
    private List<Map<String, String>> lignes = List.of();
    /** demandeur -> lignes restantes après ses filtres */
    private final Map<String, List<Map<String, String>>> jeux = new HashMap<>();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) fichier = String.valueOf(args[0]);
        charger(Ctf.dossier().resolve(fichier));
        Ctf.inscrire(this, Ctf.ANALYSTE, Map.of(
                "fichier", fichier,
                "colonnes", String.join(",", colonnes),
                "operations", "colonnes,reinitialiser,filtrer,compter_par,apercu"));
        Ctf.log(this, "prêt, " + lignes.size() + " lignes dans " + fichier + ", colonnes " + colonnes);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) {
                    block();
                    return;
                }
                if (msg.getPerformative() != ACLMessage.REQUEST) {
                    send(Ctf.repondre(msg, ACLMessage.NOT_UNDERSTOOD, new JSONObject().put("erreur", "seules les REQUEST sont traitées")));
                    return;
                }
                try {
                    JSONObject res = traiter(msg.getSender().getLocalName(), Ctf.contenu(msg));
                    send(Ctf.repondre(msg, ACLMessage.INFORM, res.put("ok", true)));
                } catch (IllegalArgumentException e) {
                    send(Ctf.repondre(msg, ACLMessage.FAILURE, new JSONObject().put("ok", false).put("erreur", e.getMessage())));
                }
            }
        });
    }

    /**
     * Charge le fichier CSV en mémoire : la première ligne donne les colonnes, les lignes vides sont ignorées.
     * En cas d'erreur de lecture, l'erreur est journalisée et l'agent reste actif avec un jeu vide.
     * @param chemin chemin complet du fichier CSV
     */
    private void charger(Path chemin) {
        try {
            List<String> texte = Files.readAllLines(chemin);
            colonnes = List.of(texte.get(0).split(";"));
            List<Map<String, String>> l = new ArrayList<>();
            for (String ligne : texte.subList(1, texte.size())) {
                if (ligne.isBlank()) continue;
                String[] champs = ligne.split(";");
                Map<String, String> m = new LinkedHashMap<>();
                for (int i = 0; i < colonnes.size() && i < champs.length; i++) m.put(colonnes.get(i), champs[i]);
                l.add(m);
            }
            lignes = List.copyOf(l);
        } catch (Exception e) {
            Ctf.log(this, "ERREUR lecture de " + chemin + " : " + e.getMessage());
        }
    }

    /**
     * Exécute une opération sur le jeu courant du demandeur (initialisé avec le fichier complet si besoin).
     * @param demandeur nom local de l'agent à l'origine de la requête
     * @param req contenu JSON de la requête, avec au moins le champ {@code operation}
     * @return le résultat de l'opération (le champ {@code ok} est ajouté par l'appelant)
     * @throws IllegalArgumentException si l'opération, la colonne ou un paramètre est invalide
     */
    private JSONObject traiter(String demandeur, JSONObject req) {
        List<Map<String, String>> jeu = jeux.computeIfAbsent(demandeur, k -> lignes);
        String op = req.optString("operation");
        Ctf.log(this, op + " " + req + " pour " + demandeur);
        return switch (op) {
            case "colonnes" -> new JSONObject().put("colonnes", new JSONArray(colonnes)).put("nb_lignes", lignes.size());
            case "reinitialiser" -> {
                jeux.put(demandeur, lignes);
                yield new JSONObject().put("nb_lignes", lignes.size());
            }
            case "filtrer" -> {
                String col = colonne(req);
                Predicate<Map<String, String>> garde;
                if (req.has("valeur")) {
                    String v = req.getString("valeur");
                    garde = m -> v.equalsIgnoreCase(m.getOrDefault(col, ""));
                } else if (req.has("contient")) {
                    String v = req.getString("contient");
                    garde = m -> m.getOrDefault(col, "").contains(v);
                } else throw new IllegalArgumentException("il faut 'valeur' (égalité) ou 'contient' (sous-chaîne)");
                List<Map<String, String>> nouveau = jeu.stream().filter(garde).toList();
                jeux.put(demandeur, nouveau);
                yield new JSONObject().put("nb_lignes_avant", jeu.size()).put("nb_lignes", nouveau.size())
                        .put("apercu", apercu(nouveau, 3));
            }
            case "compter_par" -> {
                String col = colonne(req);
                int top = req.optInt("top", 5);
                Map<String, Integer> compte = new HashMap<>();
                for (Map<String, String> m : jeu) compte.merge(m.getOrDefault(col, ""), 1, Integer::sum);
                var tab = new JSONArray();
                compte.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                        .limit(top)
                        .forEach(e -> tab.put(new JSONObject().put("valeur", e.getKey()).put("nombre", e.getValue())));
                yield new JSONObject().put("colonne", col).put("nb_lignes", jeu.size()).put("classement", tab);
            }
            case "apercu" -> new JSONObject().put("nb_lignes", jeu.size()).put("lignes", apercu(jeu, req.optInt("n", 5)));
            default -> throw new IllegalArgumentException("opération inconnue '" + op
                    + "' (colonnes, reinitialiser, filtrer, compter_par, apercu)");
        };
    }

    /**
     * Lit et vérifie le champ {@code colonne} de la requête.
     * @throws IllegalArgumentException si la colonne n'existe pas dans le fichier
     */
    private String colonne(JSONObject req) {
        String col = req.optString("colonne");
        if (!colonnes.contains(col)) throw new IllegalArgumentException("colonne '" + col + "' inconnue, colonnes : " + colonnes);
        return col;
    }

    /** @return les {@code n} premières lignes du jeu, sous forme de tableau JSON d'objets */
    private static JSONArray apercu(List<Map<String, String>> jeu, int n) {
        var tab = new JSONArray();
        jeu.stream().limit(n).forEach(m -> tab.put(new JSONObject(m)));
        return tab;
    }

    @Override
    protected void takeDown() {
        Ctf.desinscrire(this);
    }
}
