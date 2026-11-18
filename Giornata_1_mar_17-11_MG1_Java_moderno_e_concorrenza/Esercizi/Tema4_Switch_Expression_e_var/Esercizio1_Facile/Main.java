import java.time.DayOfWeek;

public class Main {
    public static void main(String[] args) {
        for (var giorno : DayOfWeek.values()) {                 // var: DayOfWeek, evidente dal ciclo
            String tipo = switch (giorno) {                     // switch expression: produce un valore
                case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "feriale";
                case SATURDAY, SUNDAY -> "weekend";             // tutti i 7 giorni coperti: niente default
            };
            System.out.println(giorno + " → " + tipo);
        }
    }
}
