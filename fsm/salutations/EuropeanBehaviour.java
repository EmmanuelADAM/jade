package fsm.salutations;

import jade.core.behaviours.Behaviour;

/**minimal behaviour that display nbCycles times a msg
 *@author emmanueladam
 *@version 2021-11-24*/
class EuropeanBehaviour extends Behaviour {
    /**msg to display*/
    String msg;
    /**nb cyles of execution*/
    int nbCycles;
    /**current cycle*/
    int i = 0;

    EuropeanBehaviour(String msg, int stop) {
        this.msg = msg;
        this.nbCycles = stop;
    }

    /**if the behaviour is "reseted", current cycle goes to 0*/
    @Override
    public void reset() {i=0;}

    /**
     * display the agent name, the msg, the current cycle and the nb of cycles to do
     * */
    @Override
    public void action() {
        i++;
        System.out.println(myAgent.getLocalName() + " -> " + msg + " " + i + "/" + nbCycles);
    }

    /**behaviour done if current cycle = nb of cycles to do*/
    @Override
    public boolean done() {
        return i >= nbCycles;
    }


    /**at the end, return randomly an integer, 0 or 1 */
    @Override
    public int onEnd() {
        return (int) (Math.random() + 0.5);
    }

};