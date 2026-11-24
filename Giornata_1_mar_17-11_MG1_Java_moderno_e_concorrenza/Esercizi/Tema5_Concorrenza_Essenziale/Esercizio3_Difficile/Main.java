import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    static final int POSTI = 50;
    static final int RICHIESTE = 200;
    static final int THREAD = 8;

    public static void main(String[] args) throws Exception {
        Cinema cinema = new Cinema(POSTI);
        List<Future<Boolean>> esiti = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(THREAD)) {
            for (int i = 1; i <= RICHIESTE; i++) {
                int posto = ThreadLocalRandom.current().nextInt(1, POSTI + 1);   // posto scelto a caso
                String cliente = "cliente" + i;
                esiti.add(pool.submit(() -> cinema.prenota(posto, cliente)));
            }

            int accolte = 0;
            int rifiutate = 0;
            for (Future<Boolean> esito : esiti) {
                if (esito.get()) {
                    accolte++;
                } else {
                    rifiutate++;
                }
            }

            // Il risultato finale è una copia immutabile: il riepilogo non può cambiare mentre lo leggiamo
            var assegnazioni = cinema.assegnazioni();

            System.out.println("Richieste totali: " + RICHIESTE);
            System.out.println("Accolte: " + accolte);
            System.out.println("Rifiutate: " + rifiutate);
            System.out.println("Posti occupati: " + assegnazioni.size() + " su " + POSTI);
            System.out.println("Nessun posto assegnato due volte: " + (accolte == assegnazioni.size()));
        }

        // Due righe di spiegazione:
        // Una copia immutabile del risultato non cambia più mentre viene letta o stampata, anche se altri thread
        // stanno ancora lavorando: nessuno può aggiungere o togliere posti dopo il riepilogo.
        // Così il risultato si può condividere tra thread senza sincronizzazione né copie difensive ogni volta.
    }
}
