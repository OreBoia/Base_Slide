import java.util.Map;
import java.util.function.UnaryOperator;

public class Sconti {

    // Ogni regola di sconto è una lambda Double -> Double, scelta per codice coupon
    private final Map<String, UnaryOperator<Double>> coupon = Map.of(
        "NESSUNO", p -> p,
        "SCONTO10", p -> p * 0.90,                           // 10%
        "MENO5", p -> Math.max(0.0, p - 5.0),                // 5 € fissi (mai sotto zero)
        "OVER100", p -> p > 100 ? p * 0.80 : p);             // 20% solo oltre i 100 €

    // Codice inesistente: restituisce l'identità, quindi il prezzo non cambia
    public UnaryOperator<Double> scontoPer(String codice) {
        return coupon.getOrDefault(codice, p -> p);
    }

    public double prezzoFinale(double prezzo, String codice) {
        return scontoPer(codice).apply(prezzo);
    }

    // Due coupon in sequenza: prima il primo, poi il secondo sul prezzo già scontato
    public double prezzoFinale(double prezzo, String primo, String secondo) {
        UnaryOperator<Double> insieme = p -> scontoPer(primo).andThen(scontoPer(secondo)).apply(p);
        return insieme.apply(prezzo);
    }
}
