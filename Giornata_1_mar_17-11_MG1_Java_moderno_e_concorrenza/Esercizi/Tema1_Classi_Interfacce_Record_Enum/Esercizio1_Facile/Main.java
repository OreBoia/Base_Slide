import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Abbonamento a = new Abbonamento("Mario Rossi", LocalDate.of(2026, 12, 31));
        Abbonamento b = new Abbonamento("Mario Rossi", LocalDate.of(2026, 12, 31));
        Abbonamento c = new Abbonamento("Giulia Verdi", LocalDate.of(2027, 1, 15));

        System.out.println(a);                                  // toString generato
        System.out.println(c);

        System.out.println("a equals b: " + a.equals(b));       // true: stessi valori
        System.out.println("a == b: " + (a == b));              // false: due oggetti diversi

        try {
            new Abbonamento("   ", LocalDate.of(2026, 12, 31)); // nome vuoto: il costruttore compatto rifiuta
        } catch (IllegalArgumentException e) {
            System.out.println("Errore atteso: " + e.getMessage());
        }
    }
}
