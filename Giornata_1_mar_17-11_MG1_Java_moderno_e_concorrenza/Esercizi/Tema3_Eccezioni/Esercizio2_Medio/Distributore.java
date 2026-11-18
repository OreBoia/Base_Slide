import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class Distributore {
    private final Map<String, Prodotto> prodotti = new HashMap<>();
    private final Map<String, Integer> scorte = new HashMap<>();

    public void carica(Prodotto prodotto, int quantita) {
        prodotti.put(prodotto.codice(), prodotto);
        scorte.put(prodotto.codice(), quantita);
    }

    // Restituisce il resto in centesimi; lancia un'eccezione nei casi che non permettono l'erogazione
    public int erogaProdotto(String codice, int credito) {
        Prodotto prodotto = prodotti.get(codice);
        if (prodotto == null) {
            throw new NoSuchElementException("Codice inesistente: " + codice);     // eccezione standard
        }
        if (scorte.get(codice) == 0) {
            throw new ProdottoEsauritoException(codice);
        }
        if (credito < prodotto.prezzoCentesimi()) {
            throw new CreditoInsufficienteException(prodotto.prezzoCentesimi() - credito);
        }
        scorte.put(codice, scorte.get(codice) - 1);
        return credito - prodotto.prezzoCentesimi();
    }
}
