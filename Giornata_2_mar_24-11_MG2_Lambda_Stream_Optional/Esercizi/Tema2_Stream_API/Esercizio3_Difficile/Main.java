import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Ordine> ordini = List.of(
            new Ordine("Rossi", "Libri", 45.0),
            new Ordine("Bianchi", "Elettronica", 300.0),
            new Ordine("Rossi", "Elettronica", 150.0),
            new Ordine("Verdi", "Casa", 80.0),
            new Ordine("Neri", "Libri", 20.0),
            new Ordine("Bianchi", "Casa", 120.0),
            new Ordine("Rossi", "Casa", 60.0),
            new Ordine("Verdi", "Elettronica", 200.0),
            new Ordine("Gialli", "Libri", 35.0));

        System.out.println("===== CON GLI STREAM =====");

        // 1. Totale speso da ciascun cliente
        Map<String, Double> totalePerCliente = ordini.stream()
            .collect(Collectors.groupingBy(Ordine::cliente, Collectors.summingDouble(Ordine::importo)));
        System.out.println("Totale per cliente: " + totalePerCliente);

        // 2. I 3 migliori clienti: raggruppo, poi ordino le coppie cliente-totale
        List<Map.Entry<String, Double>> top3 = totalePerCliente.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(3)
            .toList();
        System.out.println("Top 3 clienti: " + top3);

        // 3. Categoria con l'incasso maggiore (max restituisce un Optional)
        Optional<Map.Entry<String, Double>> categoriaTop = ordini.stream()
            .collect(Collectors.groupingBy(Ordine::categoria, Collectors.summingDouble(Ordine::importo)))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue());
        System.out.println("Categoria top: " + categoriaTop.map(e -> e.getKey() + " (" + e.getValue() + ")").orElse("nessuna"));

        System.out.println("\n===== CON IL CICLO FOR =====");

        // 1. Totale per cliente
        Map<String, Double> totaleFor = new HashMap<>();
        for (Ordine o : ordini) {
            totaleFor.merge(o.cliente(), o.importo(), Double::sum);
        }
        System.out.println("Totale per cliente: " + totaleFor);

        // 2. Top 3: serve una lista di coppie, un ordinamento e un secondo ciclo con contatore
        List<Map.Entry<String, Double>> coppie = new ArrayList<>(totaleFor.entrySet());
        coppie.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<Map.Entry<String, Double>> top3For = new ArrayList<>();
        for (int i = 0; i < coppie.size() && i < 3; i++) {
            top3For.add(coppie.get(i));
        }
        System.out.println("Top 3 clienti: " + top3For);

        // 3. Categoria top: mappa dei totali, poi ricerca del massimo con variabili d'appoggio
        Map<String, Double> totaleCategorie = new HashMap<>();
        for (Ordine o : ordini) {
            totaleCategorie.merge(o.categoria(), o.importo(), Double::sum);
        }
        String migliore = null;
        double massimo = Double.NEGATIVE_INFINITY;
        for (Map.Entry<String, Double> e : totaleCategorie.entrySet()) {
            if (e.getValue() > massimo) {
                massimo = e.getValue();
                migliore = e.getKey();
            }
        }
        System.out.println("Categoria top: " + (migliore == null ? "nessuna" : migliore + " (" + massimo + ")"));

        // Confronto: lo stream descrive COSA si vuole (raggruppa, ordina, prendi 3), il ciclo COME farlo
        // (mappe d'appoggio, indici, variabili "massimo" da aggiornare). Lo stream è più corto e meno
        // soggetto a errori; il ciclo è più semplice da seguire passo per passo con il debugger.
    }
}
