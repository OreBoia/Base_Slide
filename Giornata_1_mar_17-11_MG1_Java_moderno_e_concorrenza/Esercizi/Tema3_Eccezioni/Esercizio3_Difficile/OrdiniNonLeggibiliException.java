import java.nio.file.Path;

// Eccezione di dominio: nasconde i dettagli del file system, ma conserva la causa originale
public class OrdiniNonLeggibiliException extends RuntimeException {

    public OrdiniNonLeggibiliException(Path file, Throwable causa) {
        super("Impossibile leggere gli ordini: " + file, causa);
    }
}
