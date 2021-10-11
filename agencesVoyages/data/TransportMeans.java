package agencesVoyages.data;

public enum TransportMeans {CAR(3), BUS(50), TRAIN(200);
    private final int nbTickets;
    TransportMeans(int _nbTickets){nbTickets = _nbTickets;}

    public int getNbTickets() {
        return nbTickets;
    }
}
