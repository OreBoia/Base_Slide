import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Map con la targa come chiave: ricerca diretta, senza scorrere tutte le auto
public class Parcheggio {
    private final Map<String, Auto> auto = new HashMap<>();

    // putIfAbsent controlla e inserisce in un colpo solo: restituisce null se la targa non c'era
    public boolean entra(Auto a) {
        return auto.putIfAbsent(a.targa(), a) == null;     // false: targa già presente
    }

    // Optional evita il null: il chiamante è costretto a gestire il caso "non presente"
    public Optional<Auto> esce(String targa) {
        return Optional.ofNullable(auto.remove(targa));    // remove restituisce null se la targa non c'è
    }

    public Optional<Auto> cerca(String targa) {
        return Optional.ofNullable(auto.get(targa));
    }

    public int postiOccupati() {
        return auto.size();
    }
}
