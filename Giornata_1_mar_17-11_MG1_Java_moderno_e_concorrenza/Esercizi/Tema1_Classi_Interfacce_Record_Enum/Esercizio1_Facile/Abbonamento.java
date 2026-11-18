import java.time.LocalDate;

// Record: classe dati immutabile con costruttore, accessor, equals, hashCode e toString generati
public record Abbonamento(String nomeSocio, LocalDate scadenza) {

    public Abbonamento {                                    // costruttore compatto: valida i dati
        if (nomeSocio == null || nomeSocio.isBlank()) {
            throw new IllegalArgumentException("Il nome del socio è obbligatorio");
        }
    }
}
