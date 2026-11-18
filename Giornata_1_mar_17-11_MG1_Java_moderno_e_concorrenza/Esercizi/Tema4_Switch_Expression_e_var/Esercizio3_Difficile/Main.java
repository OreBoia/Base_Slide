import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        System.out.println("Tariffe orarie:");
        for (TipoVeicolo tipo : TipoVeicolo.values()) {
            for (Fascia fascia : Fascia.values()) {
                System.out.println("  " + tipo + " " + fascia + ": " + euro(CalcolatoreTariffe.tariffaOraria(tipo, fascia)) + "/ora");
            }
        }

        System.out.println("\nImporti:");
        System.out.println("  AUTO diurna, 2 ore: "
                + euro(CalcolatoreTariffe.calcolaImporto(TipoVeicolo.AUTO, Fascia.DIURNA, 2)));     // 4,00 €
        System.out.println("  AUTO diurna, 10 ore: "
                + euro(CalcolatoreTariffe.calcolaImporto(TipoVeicolo.AUTO, Fascia.DIURNA, 10)));    // 20,00 → tetto 15,00 €
        System.out.println("  FURGONE notturna, 5 ore: "
                + euro(CalcolatoreTariffe.calcolaImporto(TipoVeicolo.FURGONE, Fascia.NOTTURNA, 5))); // 7,50 €
    }

    static String euro(int centesimi) {
        return String.format(Locale.ITALY, "%.2f €", centesimi / 100.0);
    }
}
