import java.time.LocalDate;
import java.time.LocalTime;

// Regola 2: l'iscrizione deve essere attiva e non scaduta alla data indicata
public class RegolaAbbonamentoNonScaduto implements RegolaAccesso {
    private final LocalDate oggi;                          // data passata dall'esterno: il codice è prevedibile e provabile

    public RegolaAbbonamentoNonScaduto(LocalDate oggi) {
        this.oggi = oggi;
    }

    @Override
    public boolean consenti(Iscrizione iscrizione, LocalTime orario) {
        return iscrizione.isAttiva() && !oggi.isAfter(iscrizione.scadenza());
    }
}
