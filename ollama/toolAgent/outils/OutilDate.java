package ollama.toolAgent.outils;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Outil le plus simple possible : la date et l'heure actuelles.
 * Un LLM ne connaît pas la date du jour (il ne connaît que la date de fin de ses données d'entraînement) !
 */
public class OutilDate implements Outil {

    @Override
    public String nom() {
        return "date";
    }

    @Override
    public String description() {
        return "donne la date et l'heure actuelles (tu ne les connais pas !). "
                + "A utiliser dès que la question dépend du jour présent (aujourd'hui, demain, prochain, dans combien de jours...).";
    }

    @Override
    public JSONObject parametres() {
        return new JSONObject(); // aucun paramètre
    }

    @Override
    public JSONObject executer(JSONObject arguments) {
        LocalDateTime maintenant = LocalDateTime.now();
        return new JSONObject()
                .put("date", maintenant.toLocalDate().toString())   // format ISO : 2026-10-06
                .put("jour", maintenant.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH))
                .put("heure", maintenant.format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}
