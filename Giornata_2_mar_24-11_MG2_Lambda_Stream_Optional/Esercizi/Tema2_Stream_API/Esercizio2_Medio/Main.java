import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Studente> studenti = List.of(
            new Studente("Anna", "Java", 28),
            new Studente("Marco", "Java", 22),
            new Studente("Luca", "Java", 30),
            new Studente("Giulia", "SQL", 25),
            new Studente("Paolo", "SQL", 19),
            new Studente("Elena", "Python", 27),
            new Studente("Chiara", "Python", 24),
            new Studente("Davide", "Python", 21));

        // 1. Media dei voti per corso (TreeMap: chiavi in ordine alfabetico)
        Map<String, Double> mediaPerCorso = studenti.stream()
            .collect(Collectors.groupingBy( Studente::corso, 
                                            TreeMap::new, 
                                            Collectors.averagingInt(Studente::voto)));
        System.out.println("Media per corso: " + mediaPerCorso);

        // 2. Numero di studenti per corso
        Map<String, Long> studentiPerCorso = studenti.stream()
            .collect(Collectors.groupingBy( Studente::corso, 
                                            TreeMap::new, 
                                            Collectors.counting()));
        System.out.println("Studenti per corso: " + studentiPerCorso);

        // 3. Nomi degli studenti con voto sopra la media generale
        double mediaGenerale = studenti.stream()
            .mapToInt(Studente::voto)
            .average()
            .orElse(0);
        System.out.println("Media generale: " + mediaGenerale);

        List<String> sopraMedia = studenti.stream()
            .filter(s -> s.voto() > mediaGenerale)       // mediaGenerale è effectively final
            .map(Studente::nome)
            .toList();
        System.out.println("Sopra la media: " + sopraMedia);
    }
}
