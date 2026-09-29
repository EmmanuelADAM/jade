package td.negociations.behaviours.strategies;

/**
 * Stratégie de concession dépendante de l'écart de prix.
 * la taille du pas de concession dépend de l'écart courant entre l'offre reçue et le prix actuellement
 * proposé, et non du temps écoulé.
 *
 * <pre>
 *     ecart = prixRecu - prixCourant
 *     nouveauPrix = prixCourant + k * ecart
 * </pre>
 * - k proche de 0 : petits pas, agent prudent.<br>
 * - k = 0.5 :  l'agent se replie systématiquement sur le milieu entre les deux offres ;<br>
 * - k proche de 1 : l'agent va presque directement au niveau de l'offre adverse (quasi-acceptation).<br>
 * <p>
 * Variante adaptative "petits pas puis grand saut" :
 *  - tant que l'écart relatif (|ecart| / prixCourant) reste au-dessus de {@code nearThreshold},
 *  la concession utilise {@code kFar} (typiquement petit, prudent) ;
 *  - une fois l'écart relatif passé sous ce seuil (les deux agents sont déjà proches), la
 * concession bascule sur {@code kNear} (plus grand, pour conclure).
 * <p>
 *
 * @author emmanueladam
 */
public class DistanceStrategy implements PricingStrategy {
    /**taux d'avancement lorsque l'écart entre les offres est grand*/
    private final double kFar;
    /**taux d'avancement lorsque l'écart entre les offres est réduit*/
    private final double kNear;
    /**seuil en indiquant la proximité */
    private final double nearThreshold;

    /** stratégie à taux de concession k constant, entre 0 (prudent) et 1 (quasi-acceptation) ; 0.5 = split the difference */
    public DistanceStrategy(double k) {
        this(k, k, 0.0);
    }

    /**
     * stratégie adaptative : kFar tant que l'écart relatif est important, kNear une fois proche du compte.
     *
     * @param kFar          taux d'ajustement utilisé tant que l'écart relatif est &gt;= nearThreshold
     * @param kNear         taux d'ajustement utilisé une fois l'écart relatif &lt; nearThreshold (typiquement plus grand que kFar)
     * @param nearThreshold seuil d'écart relatif (ex. 0.15 pour 15%) déclenchant le passage de kFar à kNear
     */
    public DistanceStrategy(double kFar, double kNear, double nearThreshold) {
        this.kFar = kFar;
        this.kNear = kNear;
        this.nearThreshold = nearThreshold;
    }

    @Override
    public double nextPrice(double currentPrice, double receivedPrice, int round, int maxRounds) {
        double ecart = receivedPrice - currentPrice;
        double k = kNear;
        //si on souhaite une approche differenciee selon la proximite
        if(nearThreshold>0){
            double ecartRelatif = currentPrice == 0 ? 0 : Math.abs(ecart) / Math.abs(currentPrice);
            k = ecartRelatif < nearThreshold ? kNear : kFar;
        }
        return currentPrice + k * ecart;
    }
}