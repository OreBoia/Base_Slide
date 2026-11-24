import java.util.Map;
import java.util.Optional;

public class Rubrica {
    private final Map<String, String> contatti = Map.of(
        "Anna", "333-1111111",
        "Marco", "333-2222222",
        "Luca", "333-3333333");

    // Map.get restituisce null se la chiave non c'è: ofNullable lo trasforma in Optional vuoto
    public Optional<String> telefonoDi(String nome) {
        return Optional.ofNullable(contatti.get(nome));
    }
}
