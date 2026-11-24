import java.util.Optional;

public class Impiegato {
    private final String nome;
    private final String reparto;
    private final double stipendio;
    private final Impiegato responsabile;        // campo normale: può essere null

    public Impiegato(String nome, String reparto, double stipendio, Impiegato responsabile) {
        this.nome = nome;
        this.reparto = reparto;
        this.stipendio = stipendio;
        this.responsabile = responsabile;
    }

    public String nome() {
        return nome;
    }

    public String reparto() {
        return reparto;
    }

    public double stipendio() {
        return stipendio;
    }

    // Il getter restituisce un Optional (il campo no)
    public Optional<Impiegato> responsabile() {
        return Optional.ofNullable(responsabile);
    }
}
