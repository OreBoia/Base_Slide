public class Main {
    public static void main(String[] args) {
        Sconti sconti = new Sconti();

        System.out.println("Nessuno su 80:        " + sconti.prezzoFinale(80.0, "NESSUNO"));     // 80.0
        System.out.println("SCONTO10 su 80:       " + sconti.prezzoFinale(80.0, "SCONTO10"));    // 72.0
        System.out.println("MENO5 su 80:          " + sconti.prezzoFinale(80.0, "MENO5"));       // 75.0
        System.out.println("OVER100 su 80:        " + sconti.prezzoFinale(80.0, "OVER100"));     // 80.0 (sotto soglia)
        System.out.println("OVER100 su 150:       " + sconti.prezzoFinale(150.0, "OVER100"));    // 120.0
        System.out.println("Codice FALSO su 80:   " + sconti.prezzoFinale(80.0, "FALSO"));       // 80.0, nessun errore

        // Combinazione con andThen: l'ordine conta
        System.out.println("OVER100 poi MENO5 su 150: " + sconti.prezzoFinale(150.0, "OVER100", "MENO5"));   // 120 - 5 = 115.0
        System.out.println("MENO5 poi OVER100 su 150: " + sconti.prezzoFinale(150.0, "MENO5", "OVER100"));   // 145 * 0.8 = 116.0
        System.out.println("SCONTO10 poi FALSO su 100: " + sconti.prezzoFinale(100.0, "SCONTO10", "FALSO")); // 90.0
    }
}
