import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Impiegato direttore = new Impiegato("Bianchi", "Direzione", 9000, null);        // nessun responsabile
        Impiegato capoIt = new Impiegato("Verdi", "IT", 5000, direttore);
        Impiegato capoVendite = new Impiegato("Neri", "Vendite", 4500, direttore);

        List<Impiegato> impiegati = List.of(
            direttore,
            capoIt,
            capoVendite,
            new Impiegato("Rossi", "IT", 3000, capoIt),
            new Impiegato("Gialli", "IT", 3800, capoIt),
            new Impiegato("Blu", "Vendite", 2800, capoVendite),
            new Impiegato("Viola", "Vendite", 3100, capoVendite));

        for (String reparto : List.of("IT", "Vendite", "Direzione", "Logistica")) {
            System.out.println(reparto + " → " + responsabileDelPiuPagato(impiegati, reparto));
        }
    }

    static String responsabileDelPiuPagato(List<Impiegato> impiegati, String reparto) {
        return impiegati.stream()
            .filter(i -> i.reparto().equals(reparto))
            .max(Comparator.comparingDouble(Impiegato::stipendio))      // Optional<Impiegato>: reparto vuoto = Optional vuoto
            .flatMap(Impiegato::responsabile)                           // responsabile() è già un Optional: flatMap
            .map(Impiegato::nome)
            .orElse("nessun responsabile");
    }
}
