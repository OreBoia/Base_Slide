import java.time.LocalTime;

// Interfaccia: il contratto («cosa si può chiedere») senza dire «come»
public interface RegolaAccesso {
    boolean consenti(Iscrizione iscrizione, LocalTime orario);
}
