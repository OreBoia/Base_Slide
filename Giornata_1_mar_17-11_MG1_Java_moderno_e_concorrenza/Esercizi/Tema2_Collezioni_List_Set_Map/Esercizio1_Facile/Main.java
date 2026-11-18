import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        List<String> ingredienti = List.of("farina", "uova", "latte", "farina", "zucchero", "uova", "burro");

        // LinkedHashSet: toglie i duplicati e mantiene l'ordine di prima comparsa
        Set<String> distinti = new LinkedHashSet<>(ingredienti);

        System.out.println("Ingredienti nella lista: " + ingredienti.size());   // 7
        System.out.println("Ingredienti distinti: " + distinti.size());          // 5
        System.out.println("Elenco: " + distinti);
    }
}
