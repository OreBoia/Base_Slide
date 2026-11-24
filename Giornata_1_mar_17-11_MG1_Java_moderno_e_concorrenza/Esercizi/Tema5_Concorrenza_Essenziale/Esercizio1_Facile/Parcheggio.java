// Versione SENZA synchronized: race condition tra controllo e scrittura
public class Parcheggio {
    private final int capienza;
    private int postiOccupati = 0;                         // stato condiviso da tutti i thread

    public Parcheggio(int capienza) {
        this.capienza = capienza;
    }

    public boolean entra() {
        if (postiOccupati < capienza) {                    // 1. controllo: c'è un posto libero?
            pausa();                                       // solo per rendere visibile il problema: attesa tra 1 e 2
            postiOccupati++;                               // 2. occupo il posto: nel frattempo altri thread hanno passato il punto 1
            return true;
        }
        return false;
    }

    public int postiOccupati() {
        return postiOccupati;
    }

    // Pausa di 1 ms, uguale nelle due versioni del parcheggio: rende la race condition riproducibile
    static void pausa() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
