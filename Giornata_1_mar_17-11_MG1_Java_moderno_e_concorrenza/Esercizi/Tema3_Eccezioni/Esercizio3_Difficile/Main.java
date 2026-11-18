import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        LettoreOrdini lettore = new LettoreOrdini();

        System.out.println("== File ordini.txt ==");
        stampa(lettore, Path.of("ordini.txt"));

        System.out.println("\n== File inesistente ==");
        stampa(lettore, Path.of("non_esiste.txt"));
    }

    static void stampa(LettoreOrdini lettore, Path file) {
        try {
            LettoreOrdini.Risultato risultato = lettore.leggi(file);
            System.out.println("Ordini validi: " + risultato.validi().size());
            risultato.validi().forEach(o -> System.out.println("  " + o));

            System.out.println("Errori: " + risultato.errori().size());
            for (ErroreRiga errore : risultato.errori()) {
                System.out.println("  riga " + errore.numeroRiga() + ": " + errore.motivo() + " → \"" + errore.testo() + "\"");
            }
        } catch (OrdiniNonLeggibiliException e) {
            System.out.println("Errore: " + e.getMessage());
            System.out.println("Causa: " + e.getCause());     // la causa originale (IOException)
        }
    }
}
