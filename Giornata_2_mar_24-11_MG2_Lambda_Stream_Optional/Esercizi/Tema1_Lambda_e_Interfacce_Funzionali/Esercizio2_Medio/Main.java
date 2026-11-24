import java.util.List;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        List<Prodotto> prodotti = List.of(
            new Prodotto("Aspirapolvere", 120.0, true),
            new Prodotto("Anello", 450.0, false),      // nome con A, ma non disponibile e costoso: passa per la regola «oppure A»
            new Prodotto("Bicchiere", 8.5, true),
            new Prodotto("Bilancia", 30.0, false),
            new Prodotto("Tablet", 299.0, true),
            new Prodotto("Cuffie", 49.99, true));

        // Tre condizioni semplici...
        Predicate<Prodotto> disponibile = p -> p.inMagazzino();
        Predicate<Prodotto> economico = p -> p.prezzo() < 50;

        // ...e un metodo che crea il Predicate per una lettera a scelta
        Predicate<Prodotto> iniziaPerA = inizia('A');

        // «disponibile ed economico, oppure con un nome che inizia per A»
        Predicate<Prodotto> regola = disponibile.and(economico).or(iniziaPerA);

        System.out.println("== Disponibili ed economici, oppure con nome che inizia per A ==");
        prodotti.stream().filter(regola).forEach(p -> System.out.println("  " + p));

        // negate: i prodotti NON disponibili
        System.out.println("== Non disponibili ==");
        prodotti.stream().filter(disponibile.negate()).forEach(p -> System.out.println("  " + p));

        // Stessa regola, lettera diversa: il Predicate si costruisce a runtime
        System.out.println("== Disponibili ed economici, oppure che iniziano per T ==");
        prodotti.stream().filter(disponibile.and(economico).or(inizia('T'))).forEach(p -> System.out.println("  " + p));
    }

    // Metodo che restituisce una lambda: la lettera è "effectively final" e resta catturata
    static Predicate<Prodotto> inizia(char lettera) {
        return p -> !p.nome().isEmpty()
                 && Character.toUpperCase(p.nome().charAt(0)) == Character.toUpperCase(lettera);
    }
}
