// Eccezione di dominio unchecked: porta con sé il credito mancante (in centesimi)
public class CreditoInsufficienteException extends RuntimeException {
    private final int mancanti;

    public CreditoInsufficienteException(int mancanti) {
        super("Credito insufficiente: mancano " + mancanti + " centesimi");
        this.mancanti = mancanti;
    }

    public int mancanti() {
        return mancanti;
    }
}
