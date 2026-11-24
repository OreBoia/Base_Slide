import java.util.Map;
import java.util.Optional;

public class Utenti {
    private final Map<String, Utente> perUsername = Map.of(
        "mario", new Utente("mario", "mario.rossi@azienda.it"),
        "anna", new Utente("anna", null),                              // senza email
        "luca", new Utente("luca", "luca@esempio.com"));

    public Optional<Utente> cerca(String username) {
        return Optional.ofNullable(perUsername.get(username));
    }

    // Dominio dell'email in maiuscolo, oppure «SENZA EMAIL» (anche se l'utente non esiste)
    public String dominioEmail(String username) {
        return cerca(username)
            .flatMap(Utente::email)                                    // email() è già un Optional: flatMap, non map
            .map(email -> email.substring(email.indexOf('@') + 1))
            .map(String::toUpperCase)
            .orElse("SENZA EMAIL");
    }

    // Qui l'utente è obbligatorio: se manca si lancia un'eccezione di dominio con messaggio chiaro
    public Utente trova(String username) {
        return cerca(username)
            .orElseThrow(() -> new UtenteNonTrovatoException(username));
    }
}
