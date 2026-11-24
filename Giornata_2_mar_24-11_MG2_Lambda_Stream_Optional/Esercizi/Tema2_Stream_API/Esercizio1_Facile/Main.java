import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> voti = List.of(27, 15, 30, 18, 22, 12, 30, 25);
        List<Integer> nessunSufficiente = List.of(10, 14, 17);

        stampa(voti);
        stampa(nessunSufficiente);
        stampa(List.of());                               // lista vuota: nessun errore
    }

    static void stampa(List<Integer> voti) {
        List<Integer> sufficienti = voti.stream()
            .filter(v -> v >= 18)
            .sorted(Comparator.reverseOrder())           // decrescente
            .toList();

        double media = voti.stream()
            .filter(v -> v >= 18)
            .mapToInt(Integer::intValue)                 // IntStream: ha average()
            .average()                                   // OptionalDouble: vuoto se non ci sono elementi
            .orElse(0);                                  // nessun voto sufficiente: 0, senza eccezioni

        System.out.println("Voti: " + voti);
        System.out.println("  Sufficienti (decrescente): " + sufficienti);   // [] se non ce ne sono
        System.out.println("  Media: " + media);
    }
}
