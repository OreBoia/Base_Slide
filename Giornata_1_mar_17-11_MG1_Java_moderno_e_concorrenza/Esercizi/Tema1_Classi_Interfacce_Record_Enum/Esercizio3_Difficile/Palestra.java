import java.time.LocalTime;
import java.util.List;

// Riceve le regole dal costruttore (composizione): fa entrare il socio solo se tutte le regole lo consentono
public class Palestra {
    private final List<RegolaAccesso> regole;

    public Palestra(List<RegolaAccesso> regole) {
        this.regole = List.copyOf(regole);                 // copia difensiva: nessuno altera le regole dopo
    }

    public boolean entra(Iscrizione iscrizione, LocalTime orario) {
        for (RegolaAccesso regola : regole) {
            if (!regola.consenti(iscrizione, orario)) {
                return false;                              // basta una regola che nega
            }
        }
        return true;                                       // tutte le regole consentono
    }
}
