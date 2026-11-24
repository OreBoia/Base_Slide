import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BooleanSupplier;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Parcheggio senzaSync = new Parcheggio(10);
        lanciaThread(() -> senzaSync.entra());
        System.out.println("Senza synchronized: " + senzaSync.postiOccupati() + " posti occupati su 10");

        ParcheggioSincronizzato conSync = new ParcheggioSincronizzato(10);
        lanciaThread(() -> conSync.entra());
        System.out.println("Con synchronized: " + conSync.postiOccupati() + " posti occupati su 10");
    }

    // Lancia 50 thread che tentano tutti di entrare nello stesso momento, poi attende che finiscano
    static void lanciaThread(BooleanSupplier entra) throws InterruptedException {
        CountDownLatch via = new CountDownLatch(1);        // «semaforo di partenza»: tutti aspettano il via
        List<Thread> thread = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Thread t = new Thread(() -> {
                try {
                    via.await();                           // ogni thread si ferma qui...
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                entra.getAsBoolean();                      // ...e poi entra: così i thread si sovrappongono davvero
            });
            thread.add(t);
            t.start();
        }
        via.countDown();                                   // via: tutti partono insieme
        for (Thread t : thread) {
            t.join();                                      // join: leggo il risultato solo a fine esecuzione
        }
    }
}
