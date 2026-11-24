import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    static final int N = 1_000_000;
    static final int PARTI = 4;

    public static void main(String[] args) throws Exception {
        int[] numeri = new int[N];
        for (int i = 0; i < N; i++) {
            numeri[i] = i + 1;                             // 1, 2, ..., 1.000.000
        }

        int dimensione = N / PARTI;
        List<Future<Long>> futuri = new ArrayList<>();

        // try-with-resources: il pool viene chiuso e attende la fine dei compiti (Java 19+)
        try (ExecutorService pool = Executors.newFixedThreadPool(PARTI)) {
            for (int p = 0; p < PARTI; p++) {
                int inizio = p * dimensione;
                int fine = inizio + dimensione;
                futuri.add(pool.submit(() -> somma(numeri, inizio, fine)));   // ogni compito somma una parte
            }

            long totaleParallelo = 0;
            for (Future<Long> futuro : futuri) {
                totaleParallelo += futuro.get();           // get() attende il risultato di ciascuna parte
            }

            long totaleSingolo = somma(numeri, 0, N);      // stesso calcolo in un solo thread

            System.out.println("Somma in " + PARTI + " thread: " + totaleParallelo);
            System.out.println("Somma in un thread: " + totaleSingolo);
            System.out.println("Risultati uguali: " + (totaleParallelo == totaleSingolo));
        }
    }

    // long: la somma supera il limite di int
    static long somma(int[] numeri, int da, int a) {
        long totale = 0;
        for (int i = da; i < a; i++) {
            totale += numeri[i];
        }
        return totale;
    }
}
