// Eccezione di dominio unchecked: il nome dice cosa è successo
public class ProdottoEsauritoException extends RuntimeException {

    public ProdottoEsauritoException(String codice) {
        super("Prodotto esaurito: " + codice);
    }
}
