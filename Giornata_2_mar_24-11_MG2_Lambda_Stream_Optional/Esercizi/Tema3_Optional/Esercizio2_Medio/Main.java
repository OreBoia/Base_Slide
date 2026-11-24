public class Main {
    public static void main(String[] args) {
        Utenti utenti = new Utenti();

        System.out.println("mario → " + utenti.dominioEmail("mario"));      // AZIENDA.IT
        System.out.println("luca  → " + utenti.dominioEmail("luca"));       // ESEMPIO.COM
        System.out.println("anna  → " + utenti.dominioEmail("anna"));       // SENZA EMAIL (email assente)
        System.out.println("zeno  → " + utenti.dominioEmail("zeno"));       // SENZA EMAIL (utente assente)

        System.out.println("Trovato: " + utenti.trova("mario").username());
        try {
            utenti.trova("zeno");
        } catch (UtenteNonTrovatoException e) {
            System.out.println("Errore: " + e.getMessage());
        }
    }
}
