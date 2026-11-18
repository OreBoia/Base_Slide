import java.time.LocalTime;

// Regola 1: il livello BASE può entrare solo prima delle 12:00
public class RegolaOrarioMattutino implements RegolaAccesso {
    private static final LocalTime FINE_MATTINA = LocalTime.of(12, 0);

    @Override
    public boolean consenti(Iscrizione iscrizione, LocalTime orario) {
        if (iscrizione.livello() != Livello.BASE) {
            return true;                                   // gli altri livelli non sono limitati
        }
        return orario.isBefore(FINE_MATTINA);
    }
}
