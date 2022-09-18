package protocoles.voteCondorcet.agents;


import jade.core.AID;
import jade.core.AgentServicesTools;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.gui.AgentWindowed;
import jade.gui.GuiEvent;
import jade.gui.SimpleWindow4Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * classe d'un agent qui soumet un appel au vote a d'autres agents  par le protocole ContractNet
 *
 * @author eadam
 */
public class AgentBureauVote extends AgentWindowed {

    /**
     * ajout du suivi de protocole AchieveRE
     */
    protected void setup() {
        window = new SimpleWindow4Agent(getAID().getName(), this);
        window.println("Hello! Agent  " + getLocalName() + " is ready, my address is " + this.getAID().getName());
        window.setButtonActivated(true);
        window.setBackgroundTextColor(Color.CYAN);
    }

    /**
     * add a ContractNet protocol to launch a vote
     */
    private void createVote(String id, String objet) {

        println("_/ \\".repeat(20));
        println("/ \\_".repeat(20));
        println("debut d'un vote pour les options " + objet);
        String[] options = objet.split(",");
        var listeOptions = List.of(options);
        int dim = options.length;
        final int[][]  votes = new int[dim][dim];
        println("-".repeat(30));

        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setConversationId(id);
        msg.setContent(objet);

        var adresses = AgentServicesTools.searchAgents(this, "vote", "participant");
        msg.addReceivers(adresses);
        println("destinataires trouves : " + Arrays.stream(adresses).map(AID::getLocalName).toList().toString());
        println("-".repeat(40));

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 1000));


        ContractNetInitiator init = new ContractNetInitiator(this, msg) {
            /**fonction lancee a chaque proposition*/
            @Override
            public void handlePropose(ACLMessage propose, List<ACLMessage> acceptations) {
                println("l'agent %s propose %s ".formatted(propose.getSender().getLocalName(), propose.getContent()));
            }

            /**fonction lancee quand un participant refuse de continuer*/
            @Override
            protected void handleRefuse(ACLMessage refuse) {
                println("REFUS ! j'ai recu un refus  de " + refuse.getSender().getLocalName());
            }

            /**fonction lancee quand toutes les reponses ont ete recues*/
            @Override
            protected void handleAllResponses(List<ACLMessage> leursVotes, List<ACLMessage> mesRetours) {
                ArrayList<ACLMessage> listeVotes = new ArrayList<>(leursVotes);
                //on ne garde que les propositions
                listeVotes.removeIf(v -> v.getPerformative() != ACLMessage.PROPOSE);
                String[][] strMatVotes = new String[listeVotes.size()][dim];
                String[] electedOption={""};

                for (int i=0; i<listeVotes.size(); i++)
                {
                    ACLMessage vote = listeVotes.get(i);
                    //par defaut, on accepte tout vote
                    var retour = vote.createReply();
                    retour.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    mesRetours.add(retour);
                    var content = vote.getContent();
                    //analyse du contenu sous la forme resto1>resto2>,...
                    strMatVotes[i] = content.split(">");
                }

                var tabNoOptions = IntStream.range(0, dim).toArray();
                List<Integer> selectedOptions = new ArrayList<>();
                for(int i:tabNoOptions) selectedOptions.add(i);

                selectedOptions = voteCondorcet(strMatVotes, listeOptions, selectedOptions);
                electedOption[0] = options[selectedOptions.get(0)];

                if(selectedOptions.size()>1) {
                    println("=".repeat(30));
                    println("encore un ex-aequo ! => condorcet limite a ces options...");
                    for(int[] ligne:votes)Arrays.fill(ligne, 0);

                    selectedOptions = voteCondorcet(strMatVotes, listeOptions, selectedOptions);
                    electedOption[0] = options[selectedOptions.get(0)];



                    if(selectedOptions.size()>1) {
                        println("encore un ex-aequo ! => tirage aleatoire entre ces options...");
                        electedOption[0] = options[selectedOptions.stream().findAny().orElse(0)];
                        println("Choix final : " + electedOption[0]);
                    }

                }

                mesRetours.forEach(msg -> msg.setContent(electedOption[0]));
                println("#-".repeat(30));
            }


        };

        addBehaviour(init);

    }

    /**
     * @param strMatVotes matrice (n,m) de m votes par n votants
     * @param listeOptions choix d'un votant classe ordre de preference
     * @param selectedOptions liste des options acceptables
     * @return liste des options ayant gagne le plus de duels et en ayant perdu le moins */
    private List<Integer> voteCondorcet(String [][]strMatVotes, List<String> listeOptions, List<Integer> selectedOptions)
    {
        int dim = listeOptions.size();
        final int[][]  votes = new int[dim][dim];
        for(String[]sesVotes:strMatVotes)
        {
            String strChoixI, strChoixJ;

            for (int i=0; i<sesVotes.length-1; i++) {
                strChoixI = sesVotes[i];
                int oi = listeOptions.indexOf(strChoixI);
                if(selectedOptions.contains(oi)) {
                    for (int j = i + 1; j < sesVotes.length; j++) {
                        strChoixJ = sesVotes[j];
                        int oj = listeOptions.indexOf(strChoixJ);
                        if(selectedOptions.contains(oj)) {
                            votes[oi][oj] += 1;
                            votes[oj][oi] -= 1;
                        }
                    }
                }
            }
        }

        println("-".repeat(40));
        //affichage du total des votes
        println("resultat des votes : ");
        for(int[]ligne:votes) println(Arrays.toString(ligne));

        // on regarde s'il existe une option gagnante (preferee des autres)
        int[]duelsGagnes = new int[dim];
        final int[]sommePoints = new int[dim];
        for(int i=0; i<dim; i++) {
            duelsGagnes[i] = (int)Arrays.stream(votes[i]).filter(j->j>0).count();
            sommePoints[i] = Arrays.stream(votes[i]).sum();
        }

        println("nb de duels gagnes / options : "+ Arrays.toString(duelsGagnes));
        println("points / options : "+ Arrays.toString(sommePoints));

        int maxMax = Arrays.stream(duelsGagnes).max().orElse(0);
        var newSelectedOptions = new ArrayList<Integer>();
        for(int i=0; i<dim; i++) if (duelsGagnes[i]==maxMax && selectedOptions.contains(i)) newSelectedOptions.add(i);

        StringBuilder sbMax = new StringBuilder("Options ayant gagnes le plus de duels :");
        newSelectedOptions.forEach(i->sbMax.append(listeOptions.get(i)).append(", "));
        println(sbMax.toString());

        if(newSelectedOptions.size()>1) {
            condorcetParPoints(listeOptions, dim, sommePoints, newSelectedOptions);
        }
        return newSelectedOptions;
    }

    /**
     * @param listeOptions choix d'un votant classe ordre de preference
     * @param sommePoints somme des points par liste 'duels gagnes/duels perdus)
     * @param newSelectedOptions liste des options acceptables
     * */
    private void condorcetParPoints(List<String> listeOptions, int dim, int[] sommePoints,
                          ArrayList<Integer> newSelectedOptions) {
        int maxMax;
        println("=".repeat(30));
        println("ex-aequo !");
        int[] sommePointsSelected = new int[dim];
        Arrays.setAll(sommePointsSelected, i -> newSelectedOptions.contains(i) ? sommePoints[i] : 0);
        println("max de points parmi " + Arrays.toString(sommePointsSelected));
        maxMax = Arrays.stream(sommePointsSelected).max().orElse(0);
        newSelectedOptions.clear();
        for (int i = 0; i < dim; i++) if (sommePointsSelected[i] == maxMax) newSelectedOptions.add(i);
        StringBuilder sbTotal = new StringBuilder("Options ayant obtenu le plus de points : ");
        newSelectedOptions.forEach(i -> sbTotal.append(listeOptions.get(i)).append(", "));
        println(sbTotal.toString());
    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        launchRequest();
    }


    public void launchRequest() {
        StringBuilder sb = new StringBuilder();
        for (Resto r : Resto.values()) sb.append(r).append(",");
        createVote("voteNo1", sb.toString());
    }

    @Override
    public void takeDown() {
        System.err.println("moi " + this.getLocalName() + ", je quitte la plateforme...");
        window.dispose();
    }

}
