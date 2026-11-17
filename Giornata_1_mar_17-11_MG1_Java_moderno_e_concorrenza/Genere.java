public enum Genere {

    ROMANZO,
    SAGGIO,
    FUMETTO;
}

public class Main(){
    public static void main(string[] args){
        System.out.println(Genere.ROMANZO);
        System.out.println(Genere.SAGGIO);

        for (Genere g : Genere.values()) {                             // tutti i valori, nell'ordine di dichiarazione
            System.out.println(g + " → " + g.etichetta() + ", " + g.giorniPrestito() + " giorni");
        }
    }
}