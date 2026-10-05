package td.ollamaVotes.launch;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.wrapper.StaleProxyException;
import td.ollamaVotes.agents.AgentOrganisateur;
import td.ollamaVotes.agents.AgentVotant;

import java.util.Map;
import java.util.TreeMap;

/** Lance la plateforme, les votants (une personnalite chacun) et l'organisateur. */
public class LanceurVote {
    public static void main(String... args) throws StaleProxyException {
        var pp = new ExtendedProperties();
        pp.setProperty(Profile.GUI, System.getProperty("jade.gui", "true"));
        var conteneur = Runtime.instance().createMainContainer(new ProfileImpl(pp));

        // createNewAgent permet de passer des arguments contenant des virgules
        Map<String, String> personnalites = new TreeMap<>(Map.of(
                "lea",   "Tu es Lea, 22 ans, végane et militante écologiste, tu détestes tout ce qui touche a la viande.",
                "karim", "Tu es Karim, 21 ans, gamer pro qui reste concentré, tu préfères des sucres lents.",
                "ines",  "Tu es Ines, 20 ans, étudiante boursière, tu fais très attention à chaque euro dépensé.",
                "hugo",  "Tu es Hugo, 23 ans, sportif pro lanceur de poids, tu privilégie les apports en glucides et protéines.",
                "zoe",   "Tu es Zoe, 22 ans, gourmande et curieuse, tu adores découvrir des cuisines épicées."));

        for (var e : personnalites.entrySet())
            conteneur.createNewAgent(e.getKey(), AgentVotant.class.getName(), new Object[]{e.getValue()}).start();

        conteneur.createNewAgent("organisateur", AgentOrganisateur.class.getName(), null).start();
    }
}
