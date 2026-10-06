package ollama.internationalCuisine.agents;

import jade.core.behaviours.WakerBehaviour;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import ollama.internationalCuisine.tools.Meteo;
import ollama.internationalCuisine.gui.GuiOllamaAgent;
import ollama.internationalCuisine.tools.OllamaTools;
import ollama.internationalCuisine.tools.WeatherData;

import java.time.LocalDate;
import java.time.MonthDay;

/**
 * agent qui va interroger un modèle LLM via Ollama et une API Météo
 * pour démo.. les échanges tournent sur cet exemple sur la cuisine
 *
 */
public class AgentLLM extends GuiAgent {
    /**outils pour interroger les modèles LLM via ollama*/
    private OllamaTools ollama;
    /**name of the model to interact with*/
    String modelName;
    /**graphical user interface dedicated to this agent*/
    GuiOllamaAgent window;
    /**
     * city of the user
     */
    String ville = "Valenciennes";
    /**
     * météo actuelle de la ville, donnée par l'API OpenWeatherMap
     */
    String laMeteo = "inconnue";
    /**
     * latitude de la ville (> 0 : hémisphère nord), donnée avec la météo
     */
    double latitude = 50.36;


    /**
     * agent set-up
     */
    @Override
    protected void setup() {
        window = new GuiOllamaAgent(this);
        window.println("Hello! I'm an agent able to use LLM models. My name is " + getLocalName() + ". ");

        try {//tentative de connection à OLLAMA
            ollama = new OllamaTools();

            // Lister les modèles disponibles
            window.println("=== Available LLM models  ===");
            String[] models = ollama.listModels();
            for (String model : models) {
                window.println("- " + model);
            }
            // choix par defaut du premier modele nom embedding
            modelName = OllamaTools.chooseModel(models);
            window.println("modèle utilisé : " + modelName);
            // les modèles d'embedding ne savent pas discuter, ils ne sont pas proposés
            window.setModels(OllamaTools.chatModels(models), modelName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // météo de la ville par défaut
        window.setCity(ville);
        demanderMeteo(ville);
    }

    // 'clean-up' of the agent
    @Override
    protected void takeDown() {
        window.println("Me, Agent " + getLocalName() + " I leave the platform ! ");
    }


    /**pose une question au modele et y joint :
     * - la ville et sa meteo actuelle
     * - la saison selon la position de la ville
     * - un historique des échanges (sur la nourriture)*/
    private void queryBienEntouree(String query) {
        addBehaviour(new WakerBehaviour(this, 100) {
            public void onWake() {
                try {
                    // Test chat avec historique
                    window.println("\n=== Test chat avec historique ===", true);
                    window.println("(patientez quelques secondes si le modèle est volumineux)", true);
                    String[] history = {
                            "Bonjour, je cherche des idées de repas pour cette semaine.",
                            "Avec plaisir ! Dites-moi vos goûts et vos contraintes.",
                            "Je suis originaire du Nord, j'adore la cuisine flamande.",
                            "Parfait : carbonade, potjevleesch, waterzooi, welsh… je garde ça en tête.",
                            "J'aime découvrir les spécialités locales.",
                            "D'accord, je regarderai les légumes, viandes, plats et desserts proche de ta ville et du moment pour faciliter le circuit court.",
                            "Attention, je suis allergique aux choux.",
                            "C'est noté : ni choux rouges, ni choux fleurs, ni brocolis.",
                            "Et je n'ai pas de four, seulement des plaques.",
                            "D'accord, je ne proposerai que des recettes à la poêle ou en cocotte."
                    };
                    // météo actuelle de la ville au moment de la question
                    demanderMeteo(ville);
                    String caracteristiques = "Tu es un assistant inventif et sympathique. Tu proposes des recettes de cuisine en fonction du temps et de la saison. L'utilisateur est à " + ville + ".";
                    caracteristiques += " Nous sommes en " + saison(latitude, LocalDate.now())
                            + " (hémisphère " + (latitude >= 0 ? "nord" : "sud") + ").";
                    caracteristiques += " La météo actuelle à " + ville + " est : " + laMeteo + ".";
                    window.println("System: " + caracteristiques, true);

                    window.println("Historique forcée :", true);
                    for (int i = 0; i < history.length; i += 2)
                        window.println("User: %s  --> Assistant: %s".formatted(history[i], history[i + 1]), true);
                    window.println("---".repeat(20), true);
                    window.println("->" + query, true);
                    window.println("?".repeat(20), true);
                    // animation jusqu'au 1er morceau de réponse (chargement du modèle, lecture du prompt)
                    window.startWaiting("chargement de " + modelName + "...");
                    boolean[] started = {false};
                    // la réponse s'affiche au fur et à mesure qu'elle est générée
                    try {
                        ollama.chatWithHistory(modelName,
                                caracteristiques,
                                query,
                                history,
                                chunk -> {
                                    if (!started[0]) {
                                        started[0] = true;
                                        window.stopWaiting();
                                    }
                                    window.print(chunk);
                                });
                    } finally {
                        window.stopWaiting(); // même en cas d'erreur
                    }
//            String response = chatWithHistory(modelName, caracteristiques, query,  null);
                    window.println("");
                    window.println("~".repeat(50), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * récupère la météo actuelle d'une ville par l'API OpenWeatherMap (classe Meteo) ;
     * la ville et la météo sont mémorisées pour les prochaines questions au modèle
     */
    private void demanderMeteo(String nouvelleVille) {
        window.println("demande de la météo à " + nouvelleVille + "...");
        WeatherData weather = new Meteo().getWeatherByCity(nouvelleVille);
        if (weather != null && weather.isValid()) {
            ville = nouvelleVille;
            latitude = weather.latitude();
            // ex. "chaud, 22,3°C (ressenti 21,8°C), ciel dégagé, humidité 60%, vent 12 km/h"
            laMeteo = "%s, %.1f°C (ressenti %.1f°C), %s, humidité %d%%, vent %.0f km/h".formatted(
                    weather.natureTemperature(), weather.temperature(), weather.feelsLike(),
                    weather.description(), weather.humidity(), weather.windSpeedKmh());
            window.println("météo à " + ville + " : " + laMeteo);
            window.println("La saison est " + saison(latitude, LocalDate.now()));
        } else {
            window.println("données météo non disponibles pour " + nouvelleVille + " (on garde " + ville + ")");
            window.setCity(ville);
        }
    }

    /**
     * saison à une date donnée, selon l'hémisphère (dates approximatives des équinoxes et solstices) :
     * les saisons de l'hémisphère sud sont inversées par rapport à celles de l'hémisphère nord
     *
     * @param latitude latitude du lieu (> 0 : hémisphère nord, < 0 : hémisphère sud)
     * @param date     la date
     * @return "printemps", "été", "automne" ou "hiver"
     */
    static String saison(double latitude, LocalDate date) {
        MonthDay jour = MonthDay.from(date);
        String saisonNord;
        if (jour.isBefore(MonthDay.of(3, 21))) saisonNord = "hiver";
        else if (jour.isBefore(MonthDay.of(6, 21))) saisonNord = "printemps";
        else if (jour.isBefore(MonthDay.of(9, 23))) saisonNord = "été";
        else if (jour.isBefore(MonthDay.of(12, 21))) saisonNord = "automne";
        else saisonNord = "hiver";
        if (latitude >= 0) return saisonNord;
        return switch (saisonNord) {
            case "hiver" -> "été";
            case "printemps" -> "automne";
            case "été" -> "hiver";
            default -> "printemps";
        };
    }


    @Override
    protected void onGuiEvent(GuiEvent ev) {
        switch (ev.getType()) {
            case GuiOllamaAgent.SENDQUERY -> queryBienEntouree(window.lowTextArea.getText());
            case GuiOllamaAgent.CHANGEMODEL -> {
                modelName = (String) ev.getParameter(0);
                window.println("modèle utilisé : " + modelName);
            }
            case GuiOllamaAgent.CHANGECITY -> demanderMeteo((String) ev.getParameter(0));
            case GuiOllamaAgent.QUITCODE -> {
                window.dispose();
                doDelete();
                System.exit(0);
            }
        }
    }

    /**
     * this main launch JADE plateforme and asks it to create an agent
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        jadeArgs[0] = "-gui";
        jadeArgs[1] = "blablaAgent:ollama.internationalCuisine.agents.AgentLLM(french)";
        jade.Boot.main(jadeArgs);
    }

}
