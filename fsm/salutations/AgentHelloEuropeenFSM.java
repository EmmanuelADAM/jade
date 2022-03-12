package fsm.salutations;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.WakerBehaviour;

import static java.lang.System.out;

/**
 * classe d'un agent qui contient 1 comportement en machine d'etats (6 etats)
 * A <---\
 * /   \    \
 * B     C    \
 * / \    |    |
 * D   \   |    |
 * \   \ /     |
 * \-->E-----/
 * |
 * F
 *
 * @author emmanueladam
 * @since 2021-11-24
 */
public class AgentHelloEuropeenFSM extends Agent {

    /**
     * procedure principale.
     * lance les agents (1 de type AgentHelloEuropeenFSM ici)
     */
    public static void main(String[] args) {
        String[] jadeArgs = new String[2];
        StringBuilder sbAgents = new StringBuilder();
        sbAgents.append("a1:fsm.salutations.AgentHelloEuropeenFSM").append(";");
        jadeArgs[0] = "-gui";
        jadeArgs[1] = sbAgents.toString();
        jade.Boot.main(jadeArgs);
    }

    /**
     * Initialisation de l'agent
     */
    @Override
    protected void setup() {
        out.println("Moi, Agent " + getLocalName() + ", mon  adresse est " + getAID());
        out.println("J'execute des comportements selon une machine d'états finis");

        //creation du comportement de type machine d'etat finis
        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                out.println("FSM behaviour terminé, je m'en vais");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        //____LES ETATS
        //on enregistre les etats (idealement, les noms A, B, .. F devraient etre dans des constantes
        //ETAT INITIAL (2 x bonjour)
        fsm.registerFirstState(new EuropeanBehaviour("bonjour", 2), "A");
        // autres etats
        fsm.registerState(new EuropeanBehaviour("hallo", 2), "B");
        fsm.registerState(new EuropeanBehaviour("buongiorno", 3), "C");
        fsm.registerState(new EuropeanBehaviour("buenos dias", 3), "D");
        fsm.registerState(new EuropeanBehaviour("Olá", 1), "E");
        //ETAT FINAL (1 x saluton)
        fsm.registerLastState(new EuropeanBehaviour("saluton", 1), "F");

        //____LES TRANSITIONS
        // de A on va à B si le comportement lie a A retourne 0 en fin (OnEnd)
        fsm.registerTransition("A", "B", 0);
        // de A on va a C si le comportement lie a A retourne 1 en fin (OnEnd)
        fsm.registerTransition("A", "C", 1);
        // de B on va a D si le comportement lie a B retourne 0 en fin (OnEnd)
        fsm.registerTransition("B", "D", 0);
        // de B on va a E si le comportement lie B retourne 1 en fin (OnEnd)
        fsm.registerTransition("B", "E", 1);
        // de C on va a E sans condition
        fsm.registerDefaultTransition("C", "E");
        // de D on va a E sans condition
        fsm.registerDefaultTransition("D", "E");
        // de E on va a F si le comportement lie a E retourne 0 en fin (OnEnd)
        fsm.registerTransition("E", "F", 0);
        // de E on retourne a A si le comportement lie a E retourne 1 en fin (OnEnd)
        //alors on precise les comportement qui doivent realiser un reset
        //si on ne "resette" pas, les comportements ne vont pas reinitialiser le nb de cycles courant
        fsm.registerTransition("E", "A", 1, new String[]{"A", "B", "C", "D", "E", "F"});

        // ajout d'un comportement qui ajoute le comportement fsm dans 100ms
        addBehaviour(new WakerBehaviour(this, 100) {
            protected void onWake() {
                myAgent.addBehaviour(fsm);
            }
        });
    }

}
