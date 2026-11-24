// Versione CORRETTA: un solo thread alla volta può eseguire entra() e leggere il contatore
public class ParcheggioSincronizzato {
    private final int capienza;
    private int postiOccupati = 0;

    public ParcheggioSincronizzato(int capienza) {
        this.capienza = capienza;
    }

    public synchronized boolean entra() {
        if (postiOccupati < capienza) {
            Parcheggio.pausa();                            // stessa pausa della versione senza synchronized
            postiOccupati++;                               // ora il controllo e la scrittura non si possono separare
            return true;
        }
        return false;
    }

    public synchronized int postiOccupati() {              // anche la lettura è protetta (visibilità)
        return postiOccupati;
    }
}
