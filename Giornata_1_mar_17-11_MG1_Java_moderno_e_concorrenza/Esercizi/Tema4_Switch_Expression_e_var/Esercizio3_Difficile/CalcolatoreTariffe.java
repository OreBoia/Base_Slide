public class CalcolatoreTariffe {
    private static final int TETTO_GIORNALIERO = 1500;     // in centesimi: 15,00 € al giorno

    // Tariffa oraria in centesimi: la fascia cambia il prezzo, dentro un blocco con yield
    public static int tariffaOraria(TipoVeicolo tipo, Fascia fascia) {
        return switch (tipo) {
            case AUTO -> {
                if (fascia == Fascia.DIURNA) yield 200;
                yield 100;
            }
            case MOTO -> {
                if (fascia == Fascia.DIURNA) yield 100;
                yield 50;
            }
            case FURGONE -> {
                if (fascia == Fascia.DIURNA) yield 300;
                yield 150;
            }
        };
    }

    // Importo per una sosta di al massimo una giornata (ore da 1 a 24), con tetto giornaliero
    public static int calcolaImporto(TipoVeicolo tipo, Fascia fascia, int ore) {
        if (ore <= 0 || ore > 24) {
            throw new IllegalArgumentException("Ore non valide (da 1 a 24): " + ore);
        }
        int importo = tariffaOraria(tipo, fascia) * ore;
        return Math.min(importo, TETTO_GIORNALIERO);
    }
}
