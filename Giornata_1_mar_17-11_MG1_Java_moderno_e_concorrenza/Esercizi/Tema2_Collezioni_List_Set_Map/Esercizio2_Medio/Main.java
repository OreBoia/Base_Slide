public class Main {
    public static void main(String[] args) {
        Parcheggio parcheggio = new Parcheggio();

        System.out.println("Entra AB123CD: " + parcheggio.entra(new Auto("AB123CD", "Fiat Panda")));                 // true
        System.out.println("Entra AB123CD di nuovo: " + parcheggio.entra(new Auto("AB123CD", "Fiat Panda")));     // false: targa già presente

        System.out.println("Cerca AB123CD: " + parcheggio.cerca("AB123CD").map(Auto::modello).orElse("non presente"));
        System.out.println("Cerca ZZ999ZZ: " + parcheggio.cerca("ZZ999ZZ").map(Auto::modello).orElse("non presente"));

        System.out.println("Esce ZZ999ZZ (inesistente): " + parcheggio.esce("ZZ999ZZ").isPresent());   // false, nessuna eccezione
        System.out.println("Esce AB123CD: " + parcheggio.esce("AB123CD").isPresent());               // true
        System.out.println("Posti occupati: " + parcheggio.postiOccupati());                          // 0
    }
}
