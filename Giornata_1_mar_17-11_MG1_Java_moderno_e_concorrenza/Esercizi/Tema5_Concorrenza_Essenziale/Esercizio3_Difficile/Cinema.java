import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Posto → cliente. putIfAbsent controlla e assegna in un'unica operazione atomica.
public class Cinema {
    private final int posti;
    private final ConcurrentMap<Integer, String> assegnazioni = new ConcurrentHashMap<>();

    public Cinema(int posti) {
        this.posti = posti;
    }

    // true se il posto era libero e ora è assegnato; false se era già occupato
    public boolean prenota(int posto, String cliente) {
        return assegnazioni.putIfAbsent(posto, cliente) == null;
    }

    // Copia immutabile dello stato: chi la riceve non può modificare il risultato
    public Map<Integer, String> assegnazioni() {
        return Map.copyOf(assegnazioni);
    }

    public int posti() {
        return posti;
    }
}
