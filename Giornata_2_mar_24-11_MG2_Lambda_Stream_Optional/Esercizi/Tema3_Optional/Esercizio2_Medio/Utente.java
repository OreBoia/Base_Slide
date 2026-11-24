import java.util.Optional;

public class Utente {
    private final String username;
    private final String email;                  // facoltativa: può essere null

    public Utente(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String username() {
        return username;
    }

    // Il campo è normale, è il getter a restituire l'Optional
    public Optional<String> email() {
        return Optional.ofNullable(email);
    }
}
