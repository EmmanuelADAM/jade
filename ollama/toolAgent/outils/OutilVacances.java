package ollama.toolAgent.outils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Outil "calendrier scolaire" : prochaines vacances (et rentrées, ponts...) d'une zone scolaire française.
 * <p>
 * Les données viennent du calendrier officiel publié au format iCalendar (.ics) sur data.education.gouv.fr :
 * <a href="https://fr.ftp.opendatasoft.com/openscol/fr-en-calendrier-scolaire/Zone-B.ics">Zone-B.ics</a>
 * <p>
 * Un fichier .ics est un simple fichier texte :
 * <pre>
 * BEGIN:VEVENT
 * DTSTART;VALUE=DATE:20261017
 * DTEND;VALUE=DATE:20261102
 * SUMMARY:Vacances de la Toussaint - Zone B
 * END:VEVENT
 * </pre>
 * Le fichier est téléchargé une seule fois par zone (cache en mémoire).
 * Pour travailler hors ligne : -Dvacances.ics=chemin/vers/Zone-B.ics
 * <p>
 * Exemple d'appel par le LLM : <code>{"zone": "B", "nombre": 2}</code><br>
 * Exemple de réponse : <code>{"zone":"B","a_partir_du":"2026-10-06","evenements":[
 * {"titre":"Vacances de la Toussaint - Zone B","debut":"2026-10-17","fin":"2026-11-02"}, ...],"remarque":"..."}</code>
 * @author Claude
 */
public class OutilVacances implements Outil {
    /** adresse des calendriers, %s = A, B ou C */
    public static final String URL_ICS = "https://fr.ftp.opendatasoft.com/openscol/fr-en-calendrier-scolaire/Zone-%s.ics";
    /** contenu des fichiers ics déjà téléchargés : zone -> texte */
    private static final Map<String, String> cache = new HashMap<>();

    /**
     * un évènement du calendrier
     *
     * @param titre texte de l'évènement (ex. "Vacances de la Toussaint - Zone B")
     * @param debut date de début
     * @param fin   date de fin (peut être null si le calendrier n'en donne pas)
     */
    public record Evenement(String titre, LocalDate debut, LocalDate fin) {
    }

    /** nom de l'outil, tel que le LLM doit l'utiliser pour l'appeler */
    @Override
    public String nom() {
        return "vacances_scolaires";
    }

    /**
     * description de l'outil, transmise au LLM pour qu'il sache QUAND l'utiliser.
     * On y cite des villes de la zone B : le LLM ne connaît pas forcément le découpage des zones.
     */
    @Override
    public String description() {
        return "donne les prochains évènements du calendrier scolaire officiel français (vacances, ponts, rentrées) "
                + "pour une zone (A, B ou C), à partir d'une date. Valenciennes, Lille, Amiens, Rouen, Strasbourg, Marseille, Nantes... sont en zone B.";
    }

    /** paramètres acceptés par l'outil (tous facultatifs) : nom du paramètre -> explication pour le LLM */
    @Override
    public JSONObject parametres() {
        return new JSONObject()
                .put("zone", "zone scolaire : \"A\", \"B\" ou \"C\" (défaut \"B\")")
                .put("nombre", "nombre d'évènements voulus (entier, défaut 3)")
                .put("a_partir_du", "date de début de la recherche au format AAAA-MM-JJ (défaut : aujourd'hui)");
    }

    /**
     * exécute l'outil : renvoie les prochains évènements du calendrier scolaire de la zone demandée.
     *
     * @param arguments arguments fournis par le LLM : "zone", "nombre", "a_partir_du" (tous facultatifs)
     * @return un JSON contenant la zone, la date de départ et la liste des évènements trouvés
     * @throws IllegalArgumentException si la zone ou la date sont invalides
     * @throws Exception                si le calendrier ne peut être ni lu ni téléchargé
     */
    @Override
    public JSONObject executer(JSONObject arguments) throws Exception {
        // lecture (prudente) des arguments donnés par le LLM
        // zone : on accepte "b", " B ", "Zone B"... que l'on ramène à "B"
        String zone = arguments.optString("zone", "B").trim().toUpperCase(Locale.ROOT);
        if (zone.startsWith("ZONE")) zone = zone.substring(4).trim();
        if (zone.isEmpty()) zone = "B";
        if (!List.of("A", "B", "C").contains(zone))
            throw new IllegalArgumentException("zone '" + zone + "' inconnue, choisir A, B ou C");

        // nombre d'évènements : valeur par défaut si absent ou déraisonnable
        int nombre = arguments.optInt("nombre", 3);
        if (nombre < 1 || nombre > 20) nombre = 3;

        // date de départ de la recherche : aujourd'hui par défaut
        LocalDate aPartirDu = LocalDate.now();
        String date = arguments.optString("a_partir_du", "").trim();
        if (!date.isEmpty()) {
            try { aPartirDu = LocalDate.parse(date); }
            catch (DateTimeParseException e) { throw new IllegalArgumentException("date '" + date + "' invalide, format attendu AAAA-MM-JJ"); }
        }

        // recherche des évènements non terminés à la date demandée
        List<Evenement> prochains = prochainsEvenements(lireEvenements(contenuIcs(zone)), aPartirDu, nombre);

        // conversion en JSON pour le LLM ; "en_cours" signale un évènement déjà commencé
        JSONArray tab = new JSONArray();
        for (Evenement e : prochains) {
            JSONObject o = new JSONObject().put("titre", e.titre()).put("debut", e.debut().toString());
            if (e.fin() != null) o.put("fin", e.fin().toString());
            if (!e.debut().isAfter(aPartirDu)) o.put("en_cours", true);
            tab.put(o);
        }
        // la remarque aide le LLM à bien interpréter les dates de début et de fin
        return new JSONObject()
                .put("zone", zone)
                .put("a_partir_du", aPartirDu.toString())
                .put("evenements", tab)
                .put("remarque", "dates telles que données par le calendrier officiel (en général 'debut' = fin des cours, 'fin' = jour de reprise)");
    }

    /**
     * contenu du fichier ics de la zone : fichier local (-Dvacances.ics=...) ou téléchargement (une seule fois)
     */
    private static String contenuIcs(String zone) throws Exception {
        // fichier local fourni au lancement : prioritaire (pratique hors ligne ou pour les tests)
        String local = System.getProperty("vacances.ics");
        if (local != null) return Files.readString(Path.of(local));
        // cache partagé par tous les agents (threads) : accès synchronisé
        synchronized (cache) {
            String contenu = cache.get(zone);
            // premier appel pour cette zone : téléchargement du fichier
            if (contenu == null) {
                // suivi des redirections et délais maximum, pour ne pas bloquer l'agent indéfiniment
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10)).build();
                HttpRequest requete = HttpRequest.newBuilder(URI.create(URL_ICS.formatted(zone)))
                        .timeout(Duration.ofSeconds(20)).GET().build();
                HttpResponse<String> reponse = client.send(requete, HttpResponse.BodyHandlers.ofString());
                if (reponse.statusCode() != 200)
                    throw new RuntimeException("calendrier indisponible (HTTP " + reponse.statusCode() + ")");
                contenu = reponse.body();
                cache.put(zone, contenu);
            }
            return contenu;
        }
    }

    /**
     * lecture (simplifiée) d'un fichier iCalendar : on ne garde que SUMMARY, DTSTART et DTEND des VEVENT
     *
     * @param ics contenu du fichier
     * @return les évènements, sans doublon, triés par date de début
     */
    public static List<Evenement> lireEvenements(String ics) {
        // une ligne longue peut être "pliée" : la suite commence par un espace ou une tabulation (RFC 5545)
        String[] lignes = ics.replace("\r\n", "\n").replaceAll("\n[ \t]", "").split("\n");
        // TreeSet : trie les évènements et supprime les doublons (même début et même titre)
        Set<Evenement> evenements = new TreeSet<>(Comparator.comparing(Evenement::debut)
                .thenComparing(Evenement::titre));
        // informations de l'évènement en cours de lecture
        String titre = null;
        LocalDate debut = null, fin = null;
        for (String ligne : lignes) {
            ligne = ligne.trim();
            // début d'un évènement : on oublie les informations du précédent
            if (ligne.equals("BEGIN:VEVENT")) {
                titre = null;
                debut = fin = null;
                continue;
            }
            // fin d'un évènement : on le garde s'il a au moins un titre et une date de début
            if (ligne.equals("END:VEVENT")) {
                if (titre != null && debut != null) evenements.add(new Evenement(titre, debut, fin));
                continue;
            }
            int deuxPoints = ligne.indexOf(':');
            if (deuxPoints < 0) continue;
            // "DTSTART;VALUE=DATE:20261017" -> nom = "DTSTART", valeur = "20261017"
            String nom = ligne.substring(0, deuxPoints).split(";")[0].toUpperCase(Locale.ROOT);
            String valeur = ligne.substring(deuxPoints + 1).trim();
            // seules 3 propriétés nous intéressent ; dans SUMMARY, on retire les caractères "échappés" du format ics
            switch (nom) {
                case "SUMMARY" -> titre = valeur.replace("\\,", ",").replace("\\;", ";").replace("\\n", " ");
                case "DTSTART" -> debut = lireDate(valeur);
                case "DTEND" -> fin = lireDate(valeur);
            }
        }
        return new ArrayList<>(evenements);
    }

    /** "20261017" ou "20261017T000000Z" -> 2026-10-17 (null si illisible) */
    private static LocalDate lireDate(String valeur) {
        if (valeur.length() < 8) return null;
        try {
            // seuls les 8 premiers caractères (AAAAMMJJ) sont utiles, l'heure éventuelle est ignorée
            return LocalDate.parse(valeur.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * les 'nombre' premiers évènements qui ne sont pas terminés à la date donnée
     * (un évènement sans date de fin est considéré comme se terminant le jour de son début)
     *
     * @param evenements évènements triés par date de début
     * @param date       date de référence
     * @param nombre     nombre maximum d'évènements renvoyés
     * @return au plus 'nombre' évènements, dans l'ordre chronologique
     */
    public static List<Evenement> prochainsEvenements(List<Evenement> evenements, LocalDate date, int nombre) {
        return evenements.stream()
                .filter(e -> (e.fin() != null ? e.fin() : e.debut()).isAfter(date) || e.debut().equals(date))
                .limit(nombre)
                .toList();
    }
}
