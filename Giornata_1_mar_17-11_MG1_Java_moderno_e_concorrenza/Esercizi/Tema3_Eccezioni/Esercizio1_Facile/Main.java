public class Main {

    // Restituisce il numero contenuto nel testo, oppure il valore predefinito se il testo non è un intero valido
    static int leggiIntero(String testo, int predefinito) {
        try {
            return Integer.parseInt(testo);
        } catch (NumberFormatException e) {                // eccezione unchecked: la gestisco qui, dove so cosa fare
            return predefinito;
        }
    }

    public static void main(String[] args) {
        System.out.println(leggiIntero("42", 0));          // 42
        System.out.println(leggiIntero("4 2", 0));         // 0: lo spazio rende il testo non valido
        System.out.println(leggiIntero("", -1));           // -1: stringa vuota
        System.out.println(leggiIntero(null, 7));          // 7: anche null è gestito
    }
}
