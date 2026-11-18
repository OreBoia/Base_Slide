import java.time.LocalDate;

// Classe con stato modificabile: costruttore privato e factory method statico
public class Iscrizione {
    private final String nomeSocio;
    private final Livello livello;
    private final LocalDate dataInizio;
    private LocalDate dataDisdetta;                        // null finché l'iscrizione non viene disdetta

    private Iscrizione(String nomeSocio, Livello livello, LocalDate dataInizio) {
        this.nomeSocio = nomeSocio;
        this.livello = livello;
        this.dataInizio = dataInizio;
    }

    public static Iscrizione attiva(String nomeSocio, Livello livello, LocalDate data) {
        if (nomeSocio == null || nomeSocio.isBlank()) {
            throw new IllegalArgumentException("Nome socio obbligatorio");
        }
        return new Iscrizione(nomeSocio, livello, data);
    }

    public void disdici(LocalDate data) {
        if (dataDisdetta != null) {                        // la regola vive dentro la classe
            throw new IllegalStateException("Iscrizione già disdetta il " + dataDisdetta);
        }
        dataDisdetta = data;
    }

    public boolean isAttiva() { return dataDisdetta == null; }
    public LocalDate scadenza() { return dataInizio.plusMonths(1); }   // abbonamento mensile

    public String nomeSocio() { return nomeSocio; }
    public Livello livello() { return livello; }
    public LocalDate dataInizio() { return dataInizio; }
    public LocalDate dataDisdetta() { return dataDisdetta; }
}
