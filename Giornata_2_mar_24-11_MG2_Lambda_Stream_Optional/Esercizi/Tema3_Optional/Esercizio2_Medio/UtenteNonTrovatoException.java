public class UtenteNonTrovatoException extends RuntimeException {

    public UtenteNonTrovatoException(String username) {
        super("Utente non trovato: " + username);
    }
}
