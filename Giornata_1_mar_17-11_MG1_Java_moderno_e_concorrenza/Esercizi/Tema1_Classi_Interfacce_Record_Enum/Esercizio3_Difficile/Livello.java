// Enum con campi, costruttore e metodi
public enum Livello {
    BASE(30, false),
    PREMIUM(50, true),
    VIP(80, true);

    private final int costoMensile;          // in euro
    private final boolean includePiscina;

    Livello(int costoMensile, boolean includePiscina) {   // il costruttore di un enum è sempre privato
        this.costoMensile = costoMensile;
        this.includePiscina = includePiscina;
    }

    public int costoMensile() { return costoMensile; }
    public boolean includePiscina() { return includePiscina; }
}
