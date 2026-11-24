public class Main {
    public static void main(String[] args) {
        Rubrica rubrica = new Rubrica();

        stampa(rubrica, "Anna");
        stampa(rubrica, "Giorgio");
    }

    static void stampa(Rubrica rubrica, String nome) {
        // Nessun if, nessun get(): map trasforma se presente, orElse è il ripiego
        String messaggio = rubrica.telefonoDi(nome)
            .map(telefono -> "Telefono: " + telefono)
            .orElse("Contatto non presente");
        System.out.println(nome + " → " + messaggio);
    }
}
