package td.negociations.behaviours.strategies;

/**
 * Stratégie de calcul du prix à proposer par un agent en négociation.
 * Invoqué par {@code td.negociations.behaviours.NegociationBehaviour}
 * à chaque étape de la négociation, c'est-à-dire à chaque fois qu'une offre est reçue
 *
 * @author emmanueladam
 */
@FunctionalInterface
public interface PricingStrategy {

    /**
     * calcule le prix que l'agent doit proposer pour ce tour.
     *
     * @param currentPrice  prix actuellement proposé par l'agent, avant ce tour
     * @param receivedPrice prix qui vient d'être reçu de l'adversaire, pour ce tour
     * @param round         numéro du tour de négociation en cours (1 pour la première offre reçue)
     * @param maxRounds     nombre de tours max autorisés pour cette négociation
     * @return le nouveau prix à proposer
     */
    double nextPrice(double currentPrice, double receivedPrice, int round, int maxRounds);
}
