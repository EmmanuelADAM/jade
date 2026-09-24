package td.negociations.behaviours.strategies;

import java.util.function.DoubleSupplier;

/**
 * Stratégie de concession dépendante du temps écoulé dans la négociation.
 * Est appelée qu'à chaque étape (round) de la négociation;
 * l'avancée dans le temps est donc mesurée en temps t=round/maxRounds.
 * Selon la définition de la stratégie :
 * <pre>
 *     t = round / maxRounds
 *     prix(t) = prixInitial + (seuil - prixInitial) * t^(1/beta)
 * </pre>
 * - beta &lt; 1 : agent "dur" (pratiquant le "Boulwarisme", la 1ere offre est quasiment définitive),
 * ici ne cède fortement qu'en toute fin de négociation, mais on pourrait paramétrer la décision de céder ou non.<br>
 * - beta = 1 : concession linéaire, à vitesse constante.<br>
 * - beta &gt; 1 : agent "conciliant" (Conceder), cède surtout en début de négociation.
 * <p>
 * <p>
 * beta, prixInitial et seuil sont fournis à la construction.
 *
 * @author emmanueladam
 */
public class TemporalStrategy implements PricingStrategy {

    /**parametre de vitesse de modification */
    private final double beta;
    /**prix initial*/
    private double initialPrice;
    /**seuil à ne pas dépasser (maxi si acheteur, mini si vendeur)*/
    private double threshold;

    /**
     * @param beta           coefficient de concession : &lt;1 dur (Boulware), 1 linéaire, &gt;1 conciliant (Conceder)
     * @param initialPrice   prix de départ courant de la négociation
     * @param threshold      seuil (mini pour un vendeur, maxi pour un acheteur) courant
     */
    public TemporalStrategy(double beta, double initialPrice, double threshold) {
        this.beta = beta;
        this.initialPrice  = initialPrice;
        this.threshold  = threshold;
    }

    @Override
    public double nextPrice(double currentPrice, double receivedPrice, int round, int maxRounds) {
        double t = Math.min(1.0, (double) round / Math.max(1, maxRounds));
        double concession = Math.pow(t, 1.0 / beta);
        return initialPrice + (threshold - initialPrice) * concession;
    }

}
