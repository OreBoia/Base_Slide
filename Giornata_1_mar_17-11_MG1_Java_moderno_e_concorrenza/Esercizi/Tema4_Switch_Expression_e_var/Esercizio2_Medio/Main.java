import java.util.List;

public class Main {

    // Voto 0–10 → giudizio. Fuori dall'intervallo il default lancia un'eccezione.
    static String giudizio(int voto) {
        return switch (voto) {
            case 0, 1, 2, 3, 4, 5 -> "insufficiente";
            case 6 -> "sufficiente";
            case 7, 8 -> "buono";
            case 9, 10 -> "ottimo";
            default -> throw new IllegalArgumentException("Voto fuori dall'intervallo 0–10: " + voto);
        };
    }

    public static void main(String[] args) {
        var voti = List.of(3, 6, 8, 10);                        // var: List<Integer>, evidente da destra
        for (int voto : voti) {                                 // tipo esplicito: qui int è la scelta più chiara
            System.out.println(voto + " → " + giudizio(voto));
        }

        try {
            giudizio(11);
        } catch (IllegalArgumentException e) {
            System.out.println("Errore atteso: " + e.getMessage());
        }
    }
}
