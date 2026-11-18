import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LettoreOrdini {

    public record Risultato(List<Ordine> validi, List<ErroreRiga> errori) {}

    public Risultato leggi(Path file) {
        List<Ordine> validi = new ArrayList<>();
        List<ErroreRiga> errori = new ArrayList<>();

        // try-with-resources: il lettore si chiude anche se la lettura fallisce
        try (BufferedReader lettore = Files.newBufferedReader(file)) {
            String riga;
            int numero = 0;
            while ((riga = lettore.readLine()) != null) {   // readLine lancia IOException (checked)
                numero++;
                if (riga.isBlank()) {
                    continue;                                // riga vuota: non è un errore
                }
                try {
                    validi.add(converti(riga));
                } catch (IllegalArgumentException e) {       // riga malformata: la raccolgo e vado avanti
                    errori.add(new ErroreRiga(numero, riga, e.getMessage()));
                }
            }
        } catch (IOException e) {                            // file mancante o illeggibile: qui non posso recuperare
            throw new OrdiniNonLeggibiliException(file, e);
        }
        return new Risultato(List.copyOf(validi), List.copyOf(errori));
    }

    static Ordine converti(String riga) {
        String[] campi = riga.split(";");                    // cliente;pizza;quantità
        if (campi.length != 3) {
            throw new IllegalArgumentException("attesi 3 campi, trovati " + campi.length);
        }
        int quantita;
        try {
            quantita = Integer.parseInt(campi[2].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("quantità non numerica: " + campi[2].trim());
        }
        return new Ordine(campi[0].trim(), campi[1].trim(), quantita);   // il record valida i valori
    }
}
