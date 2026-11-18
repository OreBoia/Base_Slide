public record Ordine(String cliente, String pizza, int quantita) {

    public Ordine {                                        // validazione nel costruttore compatto
        if (cliente.isBlank()) {
            throw new IllegalArgumentException("cliente mancante");
        }
        if (pizza.isBlank()) {
            throw new IllegalArgumentException("pizza mancante");
        }
        if (quantita <= 0) {
            throw new IllegalArgumentException("quantità non valida: " + quantita);
        }
    }
}
