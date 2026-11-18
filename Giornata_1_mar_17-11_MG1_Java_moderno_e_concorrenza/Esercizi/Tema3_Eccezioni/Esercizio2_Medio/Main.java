public class Main {

    public static void main(String[] args) {
        Distributore distributore = new Distributore();
        distributore.carica(new Prodotto("A1", "Caffè", 80), 2);
        distributore.carica(new Prodotto("B2", "Acqua", 120), 0);

        provaErogazione(distributore, "A1", 50);      // credito insufficiente: mancano 30 centesimi
        provaErogazione(distributore, "A1", 100);     // successo: resto 20 centesimi
        provaErogazione(distributore, "B2", 200);     // esaurito
    }

    static void provaErogazione(Distributore distributore, String codice, int credito) {
        try {
            int resto = distributore.erogaProdotto(codice, credito);
            System.out.println("Successo " + codice + ": resto " + resto + " centesimi");
        } catch (ProdottoEsauritoException e) {
            System.out.println("Esaurito -> " + e.getMessage());
        } catch (CreditoInsufficienteException e) {
            System.out.println("Credito -> " + e.getMessage());
        }
    }
}
