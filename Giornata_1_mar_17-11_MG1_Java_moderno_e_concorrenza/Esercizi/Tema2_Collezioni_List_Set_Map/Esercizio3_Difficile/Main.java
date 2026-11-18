import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        // Ricetta → ingredienti (TreeMap: ricette in ordine alfabetico)
        Map<String, Set<String>> ricette = new TreeMap<>();
        ricette.put("Carbonara", Set.of("uova", "guanciale", "pecorino", "pepe"));
        ricette.put("Amatriciana", Set.of("guanciale", "pomodoro", "pecorino", "pepe"));
        ricette.put("Frittata", Set.of("uova", "pecorino", "latte"));
        ricette.put("Pancake", Set.of("farina", "uova", "latte"));
        ricette.put("Pizza margherita", Set.of("farina", "pomodoro", "mozzarella"));

        // Indice inverso: ingrediente → ricette che lo usano
        Map<String, Set<String>> indice = new TreeMap<>();      // ingredienti in ordine alfabetico
        for (Map.Entry<String, Set<String>> ricetta : ricette.entrySet()) {
            for (String ingrediente : ricetta.getValue()) {
                indice.computeIfAbsent(ingrediente, k -> new TreeSet<>())   // se manca, crea il set vuoto
                      .add(ricetta.getKey());
            }
        }

        System.out.println("Ingredienti usati da almeno due ricette:");
        for (Map.Entry<String, Set<String>> voce : indice.entrySet()) {
            if (voce.getValue().size() >= 2) {
                System.out.println(voce.getKey() + " → " + voce.getValue());
            }
        }
    }
}
