import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        for (Livello l : Livello.values()) {
            System.out.println(l + " → " + l.costoMensile() + " €/mese, piscina: " + l.includePiscina());
        }

        Iscrizione iscrizione = Iscrizione.attiva("Mario Rossi", Livello.PREMIUM, LocalDate.of(2026, 11, 1));
        System.out.println("Attiva: " + iscrizione.isAttiva());                        // true

        iscrizione.disdici(LocalDate.of(2026, 11, 15));
        System.out.println("Disdetta il " + iscrizione.dataDisdetta() + ", attiva: " + iscrizione.isAttiva());

        try {
            iscrizione.disdici(LocalDate.of(2026, 11, 16));                             // seconda disdetta: non deve funzionare
        } catch (IllegalStateException e) {
            System.out.println("Errore atteso: " + e.getMessage());
        }
    }
}
