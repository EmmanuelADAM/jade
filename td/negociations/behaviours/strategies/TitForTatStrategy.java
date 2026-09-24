package td.negociations.behaviours.strategies;

/**
 * Stratégie de réciprocité (<i>tit-for-tat</i> , coup pour coup) :
 * l'agent imite (en l'atténuant) la dernière concession faite par l'adversaire.
 *
 * <pre>
 *     concessionAdverse = |dernièreOffreAdverse - offreAdverseD'avant|
 *     nouveauPrix = prixCourant ± modification * concessionAdverse   (+ si le prix doit monter, - s'il doit descendre)
 * </pre>
 * - modification = 1 : réciprocité totale, l'agent rend exactement ce qu'il reçoit.<br>
 * - modification &lt; 1 (par défaut) : réciprocité prudente/atténuée.<br>
 * - modification &gt; 1  : réciprocité généreuse.<br>
 * - modification = 0 : équivaut à ne jamais changer (agent totalement têtu).
 * <p>
 *
 * @author emmanueladam
 */
public class TitForTatStrategy implements PricingStrategy {

    /**coefficient de modification du changement d'offre de l'adversaire*/
    private final double modification;
    /** true si le prix de cet agent doit monter au fil de la négociation (acheteur), false s'il doit descendre (vendeur) */
    private final boolean increasing;

    /** offre adverse précédente, null tant qu'aucune offre n'a encore été reçue deux fois (ou après reset()) */
    private Double previousOpponentPrice;

    /**
     * @param modification  fraction de la concession adverse à répercuter, entre 0 (ne pas changer le prix) et 1 (réciprocité totale), peut être supérieur à 1
     * @param increasing true si le prix de cet agent doit monter (acheteur), false s'il doit descendre (vendeur)
     */
    public TitForTatStrategy(double modification, boolean increasing) {
        this.modification = modification;
        this.increasing = increasing;
    }

    @Override
    public double nextPrice(double currentPrice, double receivedPrice, int round, int maxRounds) {
        double concessionAdverse = previousOpponentPrice == null
                ? 0
                : Math.abs(receivedPrice - previousOpponentPrice);
        previousOpponentPrice = receivedPrice;

        double delta = modification * concessionAdverse;
        return increasing ? currentPrice + delta : currentPrice - delta;
    }

    /** à appeler quand l'agent redémarre une nouvelle négociation, pour oublier l'historique de la précédente */
    public void reset() {
        previousOpponentPrice = null;
    }
}
