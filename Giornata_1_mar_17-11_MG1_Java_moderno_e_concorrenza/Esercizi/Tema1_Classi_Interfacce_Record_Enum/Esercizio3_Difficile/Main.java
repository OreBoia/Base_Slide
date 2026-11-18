import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LocalDate oggi = LocalDate.of(2026, 11, 18);
        Palestra palestra = new Palestra(List.of(
                new RegolaOrarioMattutino(),
                new RegolaAbbonamentoNonScaduto(oggi)));

        Iscrizione base = Iscrizione.attiva("Mario Rossi", Livello.BASE, LocalDate.of(2026, 11, 1));
        Iscrizione premium = Iscrizione.attiva("Giulia Verdi", Livello.PREMIUM, LocalDate.of(2026, 11, 1));
        Iscrizione premiumScaduta = Iscrizione.attiva("Luca Neri", Livello.PREMIUM, LocalDate.of(2026, 9, 1));

        // Caso 1: BASE al mattino, abbonamento valido → entra
        System.out.println("BASE 10:00, valido: " + palestra.entra(base, LocalTime.of(10, 0)));                 // true
        // Caso 2: BASE nel pomeriggio → non entra (regola dell'orario)
        System.out.println("BASE 18:00, valido: " + palestra.entra(base, LocalTime.of(18, 0)));                // false
        // Caso 3: PREMIUM nel pomeriggio, abbonamento valido → entra
        System.out.println("PREMIUM 18:00, valido: " + palestra.entra(premium, LocalTime.of(18, 0)));          // true
        // Caso 4: PREMIUM con abbonamento scaduto → non entra (regola della scadenza)
        System.out.println("PREMIUM 10:00, scaduto: " + palestra.entra(premiumScaduta, LocalTime.of(10, 0))); // false
    }
}
