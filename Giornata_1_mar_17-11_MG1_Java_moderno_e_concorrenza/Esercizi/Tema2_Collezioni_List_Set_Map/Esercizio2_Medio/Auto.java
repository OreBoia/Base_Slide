public record Auto(String targa, String modello) {

    public Auto {
        if (targa == null || targa.isBlank()) {
            throw new IllegalArgumentException("Targa obbligatoria");
        }
    }
}
