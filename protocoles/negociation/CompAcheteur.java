package protocoles.negociation;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.MessageTemplate;

public class CompAcheteur extends Behaviour {

    int step = 0;
    boolean accord;
    boolean rejet;
    double offreAutre, offrePrecedente, offre;
    double epsilon = 0.1;
    Negociateur monAgent;
    MessageTemplate modele;

    CompAcheteur(Negociateur monAgent, MessageTemplate modele){
        super(monAgent);
        this.monAgent = monAgent;
        this.modele = modele;
        monAgent.seuil = 110;
        monAgent.prixSouhaite = 60;
    }

    @Override
    public void action() {
        var msg = myAgent.receive(modele);
        if (msg != null) {
            step++;
            var content = msg.getContent();
            var sender = msg.getSender();
            offreAutre = Double.parseDouble(msg.getContent());
            monAgent.println("j'ai reçu une offre à " + content );
            if (offreAutre<offrePrecedente) accord =true;
            if (offreAutre>monAgent.seuil) rejet =true;
            if(!accord || !rejet){
                if (offrePrecedente==0) offrePrecedente = monAgent.prixSouhaite;
                offre = offrePrecedente * (1+epsilon);
                offrePrecedente = offre;
                var reponse = msg.createReply();
                reponse.setContent(String.valueOf(offre));
                myAgent.send(reponse);
                monAgent.println("je propose %.2f".formatted(offre));
            }
        } else block();
    }

    @Override
    public boolean done() {
        if (accord)
            monAgent.println("Accord sur cette proposition %.2f".formatted(offreAutre));
        if (rejet)
            monAgent.println("Rejet des négociation sur cette dernière offre recue %.2f".formatted(offreAutre));
        if (step == 10)
            monAgent.println("Temps de négociation expiré");
        return step == 10 || accord || rejet;
    }
}
