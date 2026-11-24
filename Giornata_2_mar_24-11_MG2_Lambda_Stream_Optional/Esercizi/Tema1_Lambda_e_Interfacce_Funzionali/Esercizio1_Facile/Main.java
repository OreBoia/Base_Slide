import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // List.of è immutabile: per ordinare si lavora su una copia modificabile
        List<String> partecipanti = new ArrayList<>(List.of("Giulia", "Marco", "Anna", "Luca", "Chiara", "Elena", "Paolo"));

        // Prima per lunghezza del nome, a parità in ordine alfabetico
        partecipanti.sort(Comparator.comparingInt(String::length)
                                    .thenComparing(Comparator.naturalOrder()));

        partecipanti.forEach(System.out::println);       // riferimento a metodo: niente lambda da scrivere
    }
}
