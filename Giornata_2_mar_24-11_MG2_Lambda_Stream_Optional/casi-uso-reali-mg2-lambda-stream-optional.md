# Casi d'uso reali — Lambda, Stream API e Optional (Base · MG2)

Corso Java e Spring Boot Base (EXP_DATLAS_024) · Java 25 LTS

Sei casi d'uso reali, due per ciascun tema di MG2, con la **soluzione di riferimento** per il docente. I casi (Contesto, Problema attuale, Compito richiesto, Vincoli e dati, Risultato atteso) sono gli stessi inseriti nel file delle slide; la sezione *Implementazione* è nuova e **non va distribuita agli studenti** prima dell'esercitazione.

Ogni implementazione è stata compilata ed eseguita con Java 25: l'output riportato è quello reale. Ogni caso è un solo file, eseguibile con `java NomeFile.java` (senza compilare prima). I domini non sono quello di BiblioTech: i casi simulano scenari professionali diversi (logistica, help desk, vendite, log, assicurazioni, e-commerce).

---

## Tema 1 — Lambda e interfacce funzionali

### Caso 1: Tariffe di spedizione che cambiano ogni due mesi

#### Il caso

- **Contesto**: sei sviluppatore nel team logistica di un e-commerce di articoli sportivi (circa 1.200 ordini al giorno). Il responsabile spedizioni aggiorna le regole di calcolo del costo di consegna ogni 2 mesi circa, e ogni corriere ha le sue.
- **Problema attuale**: il calcolo è un metodo con uno `switch` di circa 140 righe su 6 corrieri. Ogni modifica tariffaria richiede di riaprire il metodo e rifare i test dell'intero blocco. Nell'ultimo anno 3 aggiornamenti hanno introdotto errori: in un caso 85 ordini sono stati spediti con costo sbagliato prima che ce ne accorgessimo.
- **Compito richiesto**: ridisegna il calcolo con il pattern Strategy. Definisci un'interfaccia funzionale per la regola, scrivi le regole come lambda e rendile selezionabili per nome del corriere. Decidi poi quali regole restano lambda e quali devono diventare classi, e motiva la scelta.
- **Vincoli e dati**:
  - Corriere A: 4,90 € fino a 2 kg, poi +1,20 € per kg aggiuntivo
  - Corriere B: tariffa fissa 6,50 €, gratis oltre i 59 €
  - Corriere C: tariffa per zona (Nord, Centro, Sud, Isole), letta da una tabella caricata all'avvio
  - Corriere D: come A, ma con un supplemento del 15% in dicembre
  - Le regole che dipendono dal peso e dall'importo ricevono solo quei dati
  - Il codice del corriere può arrivare sconosciuto: va gestito con un errore chiaro
- **Risultato atteso**:
  - Aggiungere un settimo corriere richiede una riga in più, senza toccare la classe che calcola
  - Ogni regola si testa da sola con almeno 3 valori di prova
  - La regola per zona è una classe e le altre sono lambda, con una motivazione scritta in 2 righe

#### Implementazione

File `SpedizioniDemo.java`:

```java
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class SpedizioniDemo {

    public static void main(String[] args) {
        // Tabella delle zone: in produzione viene caricata all'avvio da un file
        TabellaZone tabella = new TabellaZone(Map.of("Nord", 5.90, "Centro", 7.20, "Sud", 8.50, "Isole", 12.00));

        CalcolatoreSpedizioni calcolatore = new CalcolatoreSpedizioni(Map.of(
                "A", corriereA(),
                "B", corriereB(),
                "C", new CorriereZona(tabella),
                "D", conSupplementoDicembre(corriereA())));

        LocalDate novembre = LocalDate.of(2026, 11, 24);
        LocalDate dicembre = LocalDate.of(2026, 12, 10);

        // Tre valori di prova per ogni regola
        System.out.println(calcolatore.costo("A", new DatiSpedizione(1.5, 30, "Nord", novembre)));   // 4.9
        System.out.println(calcolatore.costo("A", new DatiSpedizione(2.0, 30, "Nord", novembre)));   // 4.9
        System.out.println(calcolatore.costo("A", new DatiSpedizione(5.2, 30, "Nord", novembre)));   // 4.9 + 4*1.2 = 9.7
        System.out.println(calcolatore.costo("B", new DatiSpedizione(1, 40, "Sud", novembre)));      // 6.5
        System.out.println(calcolatore.costo("B", new DatiSpedizione(1, 59, "Sud", novembre)));      // 6.5 (gratis oltre 59)
        System.out.println(calcolatore.costo("B", new DatiSpedizione(1, 59.01, "Sud", novembre)));   // 0.0
        System.out.println(calcolatore.costo("C", new DatiSpedizione(1, 40, "Nord", novembre)));     // 5.9
        System.out.println(calcolatore.costo("C", new DatiSpedizione(1, 40, "Isole", novembre)));    // 12.0
        System.out.println(calcolatore.costo("D", new DatiSpedizione(1.5, 30, "Nord", novembre)));   // 4.9
        System.out.println(calcolatore.costo("D", new DatiSpedizione(1.5, 30, "Nord", dicembre)));   // 5.635
        try {
            calcolatore.costo("Z", new DatiSpedizione(1, 40, "Nord", novembre));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());                                                      // Corriere sconosciuto: Z
        }
    }

    // Corriere A: 4,90 € fino a 2 kg, poi 1,20 € per ogni kg (o frazione) aggiuntivo
    static RegolaSpedizione corriereA() {
        return d -> d.pesoKg() <= 2 ? 4.90 : 4.90 + Math.ceil(d.pesoKg() - 2) * 1.20;
    }

    // Corriere B: 6,50 € fissi, gratis oltre i 59 €
    static RegolaSpedizione corriereB() {
        return d -> d.importoOrdine() > 59 ? 0.0 : 6.50;
    }

    // Corriere D: una regola che ne "avvolge" un'altra e aggiunge il 15% in dicembre
    static RegolaSpedizione conSupplementoDicembre(RegolaSpedizione base) {
        return d -> d.data().getMonth() == Month.DECEMBER ? base.costo(d) * 1.15 : base.costo(d);
    }
}

record DatiSpedizione(double pesoKg, double importoOrdine, String zona, LocalDate data) { }

@FunctionalInterface
interface RegolaSpedizione {
    double costo(DatiSpedizione dati);
}

// Il corriere C ha una dipendenza (la tabella delle zone): qui serve una classe
class CorriereZona implements RegolaSpedizione {
    private final TabellaZone tabella;

    CorriereZona(TabellaZone tabella) {
        this.tabella = tabella;
    }

    @Override
    public double costo(DatiSpedizione dati) {
        return tabella.tariffaPer(dati.zona());
    }
}

class TabellaZone {
    private final Map<String, Double> tariffe;

    TabellaZone(Map<String, Double> tariffe) {
        this.tariffe = new HashMap<>(tariffe);
    }

    double tariffaPer(String zona) {
        Double tariffa = tariffe.get(zona);
        if (tariffa == null) {
            throw new IllegalArgumentException("Zona sconosciuta: " + zona);
        }
        return tariffa;
    }
}

class CalcolatoreSpedizioni {
    private final Map<String, RegolaSpedizione> regole;

    CalcolatoreSpedizioni(Map<String, RegolaSpedizione> regole) {
        this.regole = regole;
    }

    double costo(String corriere, DatiSpedizione dati) {
        RegolaSpedizione regola = regole.get(corriere);
        if (regola == null) {
            throw new IllegalArgumentException("Corriere sconosciuto: " + corriere);
        }
        return regola.costo(dati);
    }
}
```

Output ottenuto:

```text
4.9
4.9
9.7
6.5
6.5
0.0
5.9
12.0
4.9
5.635
Corriere sconosciuto: Z
```

#### Note sulla soluzione

- **Una sola interfaccia funzionale** (`RegolaSpedizione`): tutte le regole hanno la stessa firma. Siccome la regola D dipende dalla data (dicembre), l'input è un piccolo `record` (`DatiSpedizione`) invece di due numeri sparsi: così la firma non cambia se una regola futura ha bisogno di un dato in più. Ogni lambda legge solo i campi che le servono: A il peso, B l'importo, D la data.
- **A, B, D sono lambda**: sono formule. D è un esempio di *composizione*: `conSupplementoDicembre` riceve una regola e ne restituisce una nuova che la avvolge, senza duplicare il calcolo di A.
- **C è una classe**: ha una dipendenza da ricevere dall'esterno (la `TabellaZone` caricata all'avvio) e può avere altri metodi in futuro. Motivazione in 2 righe: *«una formula basta una lambda; una regola che dipende da una tabella caricata da fuori è meglio una classe, che la riceve dal costruttore e si testa con una tabella finta»*.
- **`CalcolatoreSpedizioni` non cambia mai**: aggiungere un settimo corriere è una riga nella `Map`.
- **Corriere sconosciuto**: `Map.get` restituisce `null`, quindi si controlla e si lancia un `IllegalArgumentException` con il nome ricevuto. (Nel Tema 3 vedrai la stessa cosa con `Optional.ofNullable(...).orElseThrow(...)`.)
- Il costo A per i pesi decimali si arrotonda **per eccesso** (`Math.ceil`): 5,2 kg → 4 kg aggiuntivi. È una scelta da concordare con il responsabile spedizioni e da scrivere nel codice.

### Caso 2: Filtri combinabili sul pannello ticket dell'help desk

#### Il caso

- **Contesto**: lavori sul gestionale interno di un help desk con 14 operatori. I ticket aperti sono circa 650 e ogni operatore consulta il pannello più volte al giorno.
- **Problema attuale**: oggi esistono 5 metodi separati (`ticketUrgenti`, `ticketScaduti`, `ticketDiMario`, …) e ognuno duplica il ciclo `for`. Gli operatori chiedono combinazioni nuove ogni settimana, per esempio «urgenti e scaduti, ma non assegnati a me». Ogni richiesta costa circa mezza giornata di sviluppo.
- **Compito richiesto**: costruisci un sistema di filtri in cui ogni condizione è un `Predicate<Ticket>` e l'operatore le combina. Aggiungi l'ordinamento con `Comparator` composti: per priorità decrescente, poi per scadenza, poi per data di apertura. Decidi come rappresentare i filtri disponibili (per esempio in una `Map` per nome) in modo che la schermata li possa elencare.
- **Vincoli e dati**:
  - `Ticket(id, titolo, priorita, stato, assegnatario, scadenza)`, con `assegnatario` che può essere vuoto per i ticket non ancora presi in carico
  - 4 priorità, 3 stati e una scadenza sempre presente
  - Dati di prova: almeno 12 ticket, che coprano tutte le combinazioni
  - La data «di oggi» non va letta dentro i filtri, ma ricevuta dall'esterno (come in `Supplier<LocalDate>`), per poter testare i casi di scadenza
- **Risultato atteso**:
  - Le 5 funzioni duplicate si riducono a un solo metodo `filtra(lista, predicato, comparatore)`
  - La richiesta «urgenti e scaduti, non miei» si ottiene componendo 3 filtri in una riga, senza scrivere codice nuovo
  - I filtri sono verificati con almeno 6 test sui dati di prova

#### Implementazione

File `HelpDeskDemo.java`:

```java
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class HelpDeskDemo {

    public static void main(String[] args) {
        Supplier<LocalDate> oggi = () -> LocalDate.of(2027, 1, 20);       // la data arriva dall'esterno
        FiltriTicket f = new FiltriTicket(oggi, "mario");
        List<Ticket> ticket = datiDiProva();

        // Ordinamento: priorità decrescente, poi scadenza, poi data di apertura
        Comparator<Ticket> ordine = Comparator.comparing(Ticket::priorita).reversed()
                .thenComparing(Ticket::scadenza)
                .thenComparing(Ticket::apertura);

        // Una richiesta nuova = una composizione, nessun metodo nuovo
        Predicate<Ticket> urgentiScadutiNonMiei = f.get("urgenti").and(f.get("scaduti")).and(f.get("miei").negate());

        System.out.println(ids(HelpDesk.filtra(ticket, f.get("urgenti"), ordine)));         // [2, 3, 1, 4]
        System.out.println(ids(HelpDesk.filtra(ticket, f.get("scaduti"), ordine)));         // [2, 3, 1, 5, 9, 12]
        System.out.println(ids(HelpDesk.filtra(ticket, urgentiScadutiNonMiei, ordine)));    // [3, 1]
        System.out.println(ids(HelpDesk.filtra(ticket, f.get("nonAssegnati"), ordine)));    // [1, 5, 11]
        System.out.println(ids(HelpDesk.filtra(ticket, f.get("miei").and(f.get("chiusi").negate()), ordine)));   // [2, 6, 9]
        System.out.println(ids(HelpDesk.filtra(ticket,
                f.get("urgenti").or(f.get("alti")).and(f.get("chiusi").negate()), ordine)));  // [2, 3, 1, 4, 5, 6]

        System.out.println(f.nomi());   // la schermata può elencare i filtri disponibili
    }

    static List<Integer> ids(List<Ticket> lista) {
        return lista.stream().map(Ticket::id).toList();
    }

    static List<Ticket> datiDiProva() {
        return List.of(
            new Ticket(1,  "Server posta fermo",     Priorita.URGENTE, Stato.APERTO,          null,     LocalDate.of(2027, 1, 18), LocalDate.of(2027, 1, 19)),
            new Ticket(2,  "VPN non funziona",       Priorita.URGENTE, Stato.IN_LAVORAZIONE,  "mario",  LocalDate.of(2027, 1, 10), LocalDate.of(2027, 1, 15)),
            new Ticket(3,  "Stampante contabilità",  Priorita.URGENTE, Stato.IN_LAVORAZIONE,  "giulia", LocalDate.of(2027, 1, 12), LocalDate.of(2027, 1, 18)),
            new Ticket(4,  "Backup notturno fallito",Priorita.URGENTE, Stato.APERTO,          "giulia", LocalDate.of(2027, 1, 19), LocalDate.of(2027, 1, 25)),
            new Ticket(5,  "Nuovo account stagista",  Priorita.ALTA,    Stato.APERTO,          null,     LocalDate.of(2027, 1, 5),  LocalDate.of(2027, 1, 12)),
            new Ticket(6,  "Licenza software",       Priorita.ALTA,    Stato.IN_LAVORAZIONE,  "mario",  LocalDate.of(2027, 1, 14), LocalDate.of(2027, 1, 21)),
            new Ticket(7,  "Password dimenticata",   Priorita.ALTA,    Stato.CHIUSO,          "giulia", LocalDate.of(2027, 1, 2),  LocalDate.of(2027, 1, 10)),
            new Ticket(8,  "Monitor difettoso",      Priorita.MEDIA,   Stato.APERTO,          "giulia", LocalDate.of(2027, 1, 15), LocalDate.of(2027, 1, 22)),
            new Ticket(9,  "Wi-Fi sala riunioni",    Priorita.MEDIA,   Stato.IN_LAVORAZIONE,  "mario",  LocalDate.of(2027, 1, 8),  LocalDate.of(2027, 1, 19)),
            new Ticket(10, "Aggiornamento browser",  Priorita.MEDIA,   Stato.CHIUSO,          "mario",  LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 5)),
            new Ticket(11, "Tastiera nuova",         Priorita.BASSA,   Stato.APERTO,          null,     LocalDate.of(2027, 1, 17), LocalDate.of(2027, 2, 1)),
            new Ticket(12, "Cavo di rete",           Priorita.BASSA,   Stato.IN_LAVORAZIONE,  "giulia", LocalDate.of(2027, 1, 3),  LocalDate.of(2027, 1, 16)));
    }
}

enum Priorita { BASSA, MEDIA, ALTA, URGENTE }       // l'ordine dichiarato è l'ordine naturale

enum Stato { APERTO, IN_LAVORAZIONE, CHIUSO }

record Ticket(int id, String titolo, Priorita priorita, Stato stato,
              String assegnatario, LocalDate apertura, LocalDate scadenza) { }

class FiltriTicket {
    private final Map<String, Predicate<Ticket>> filtri = new LinkedHashMap<>();

    FiltriTicket(Supplier<LocalDate> oggi, String operatore) {
        filtri.put("urgenti",      t -> t.priorita() == Priorita.URGENTE);
        filtri.put("alti",         t -> t.priorita() == Priorita.ALTA);
        filtri.put("chiusi",       t -> t.stato() == Stato.CHIUSO);
        filtri.put("scaduti",      t -> t.stato() != Stato.CHIUSO && t.scadenza().isBefore(oggi.get()));
        filtri.put("nonAssegnati", t -> t.assegnatario() == null);
        filtri.put("miei",         t -> operatore.equals(t.assegnatario()));
    }

    Predicate<Ticket> get(String nome) {
        return filtri.get(nome);
    }

    List<String> nomi() {
        return new ArrayList<>(filtri.keySet());
    }
}

class HelpDesk {
    // Un solo metodo al posto dei 5 duplicati: la condizione e l'ordine arrivano dall'esterno
    static List<Ticket> filtra(List<Ticket> ticket, Predicate<Ticket> condizione, Comparator<Ticket> ordine) {
        List<Ticket> risultato = new ArrayList<>(ticket);
        risultato.removeIf(condizione.negate());      // tiene solo i ticket che soddisfano la condizione
        risultato.sort(ordine);
        return risultato;
    }
}
```

Output ottenuto:

```text
[2, 3, 1, 4]
[2, 3, 1, 5, 9, 12]
[3, 1]
[1, 5, 11]
[2, 6, 9]
[2, 3, 1, 4, 5, 6]
[urgenti, alti, chiusi, scaduti, nonAssegnati, miei]
```

#### Note sulla soluzione

- **Ogni condizione è un `Predicate<Ticket>`** salvato in una `Map` (`LinkedHashMap`, per mantenere l'ordine di inserimento): la schermata elenca i filtri con `nomi()` e l'operatore ne sceglie alcuni da combinare.
- **La data non è letta dentro il filtro**: `FiltriTicket` riceve un `Supplier<LocalDate>`. In produzione sarà `LocalDate::now`, nei test una data fissa: stessi filtri, risultati ripetibili.
- **Combinazione**: «urgenti e scaduti, non miei» è `urgenti.and(scaduti).and(miei.negate())`, cioè una riga e nessun metodo nuovo. Nota che «non miei» include anche i ticket senza assegnatario, come richiesto dal caso. Gli operatori si leggono da sinistra a destra: `urgenti.or(alti).and(chiusi.negate())` significa «(urgenti o alti) e non chiusi».
- **Ordinamento**: `Comparator.comparing(Ticket::priorita).reversed().thenComparing(Ticket::scadenza).thenComparing(Ticket::apertura)`. L'`enum Priorita` è `Comparable` secondo l'ordine di dichiarazione, quindi `reversed()` porta `URGENTE` in cima.
- **`filtra`** copia la lista (l'originale non cambia), toglie quello che non soddisfa la condizione con `removeIf(condizione.negate())` e ordina con `sort`. Resta tutto nel Tema 1: nessuno stream.
- **I 6 test** dell'output sono i controlli richiesti: ognuno ha l'elenco di `id` atteso (si calcola a mano dai 12 ticket di prova). Si possono poi trasformare in test automatici, con il risultato atteso come valore da confrontare.

---

## Tema 2 — Stream API

### Caso 1: Report mensile vendite per una catena di 12 negozi

#### Il caso

- **Contesto**: lavori nell'ufficio controllo di gestione di una catena di abbigliamento con 12 negozi. Ogni primo del mese il direttore commerciale vuole il report delle vendite del mese precedente.
- **Problema attuale**: il report lo prepara un impiegato su un foglio di calcolo partendo da circa 48.000 righe di export, in circa 3 ore. Due volte nell'ultimo semestre il totale di un negozio era sbagliato perché i resi (importi negativi) erano stati contati due volte.
- **Compito richiesto**: partendo da una `List<RigaVendita>` già caricata, produci con gli stream:
  - il totale incassato per negozio
  - i 5 prodotti più venduti per quantità
  - lo scontrino medio per negozio
  - le categorie il cui incasso è sotto la media delle categorie

  Decidi come trattare i resi e dichiara la scelta nel codice con un commento.
- **Vincoli e dati**:
  - `RigaVendita(negozio, scontrino, categoria, prodotto, quantita, importo)`
  - Una riga con `importo` negativo è un reso
  - Uno scontrino ha più righe, quindi lo scontrino medio richiede un raggruppamento in due passi
  - Le chiavi della mappa dei negozi devono uscire in ordine alfabetico (suggerimento: la factory di `groupingBy`)
  - Non modificare liste esterne dentro le lambda
- **Risultato atteso**:
  - I 4 risultati escono da pipeline separate e leggibili, ciascuna di meno di 10 righe
  - Il totale generale dei negozi coincide con la somma di tutte le righe, resi inclusi
  - L'elaborazione passa da 3 ore di lavoro manuale a una esecuzione di pochi secondi, ripetibile ogni mese

#### Implementazione

File `ReportVenditeDemo.java`:

```java
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ReportVenditeDemo {

    public static void main(String[] args) {
        List<RigaVendita> vendite = datiDiProva();
        ReportVendite report = new ReportVendite(vendite);

        System.out.println(report.totalePerNegozio());       // {Milano=..., Roma=..., Torino=...}
        System.out.println(report.top5Prodotti());
        System.out.println(report.scontrinoMedioPerNegozio());
        System.out.println(report.categorieSottoMedia());

        // Controllo: il totale generale coincide con la somma delle righe, resi inclusi
        double sommaRighe = vendite.stream().mapToDouble(RigaVendita::importo).sum();
        double sommaNegozi = report.totalePerNegozio().values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println(Math.abs(sommaRighe - sommaNegozi) < 0.001);   // true
    }

    static List<RigaVendita> datiDiProva() {
        return List.of(
            new RigaVendita("Milano", "M-1", "Giacche",  "Parka",      1, 120.00),
            new RigaVendita("Milano", "M-1", "Scarpe",   "Sneaker",    2, 130.00),
            new RigaVendita("Milano", "M-2", "Maglie",   "Polo",       3,  75.00),
            new RigaVendita("Milano", "M-3", "Scarpe",   "Sneaker",   -1, -65.00),   // reso
            new RigaVendita("Roma",   "R-1", "Giacche",  "Parka",      2, 240.00),
            new RigaVendita("Roma",   "R-1", "Maglie",   "Polo",       1,  25.00),
            new RigaVendita("Roma",   "R-2", "Accessori","Cintura",    4,  60.00),
            new RigaVendita("Roma",   "M-1", "Maglie",   "Felpa",      2,  90.00),   // stesso numero scontrino di Milano
            new RigaVendita("Torino", "T-1", "Scarpe",   "Stivale",    1,  95.00),
            new RigaVendita("Torino", "T-1", "Accessori","Sciarpa",    3,  54.00),
            new RigaVendita("Torino", "T-2", "Maglie",   "Polo",       2,  50.00),
            new RigaVendita("Torino", "T-3", "Giacche",  "Parka",     -1, -120.00));  // reso
    }
}

record RigaVendita(String negozio, String scontrino, String categoria,
                   String prodotto, int quantita, double importo) { }

class ReportVendite {
    private final List<RigaVendita> vendite;

    ReportVendite(List<RigaVendita> vendite) {
        this.vendite = vendite;
    }

    // Scelta sui resi: restano nelle righe (importo e quantità negativi) e riducono i totali.
    // Il report misura quindi il venduto NETTO, e la somma dei negozi coincide con la somma delle righe.
    Map<String, Double> totalePerNegozio() {
        return vendite.stream()
            .collect(Collectors.groupingBy(RigaVendita::negozio, TreeMap::new,       // TreeMap: negozi in ordine alfabetico
                     Collectors.summingDouble(RigaVendita::importo)));
    }

    List<String> top5Prodotti() {
        return vendite.stream()
            .collect(Collectors.groupingBy(RigaVendita::prodotto, Collectors.summingInt(RigaVendita::quantita)))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                             .thenComparing(Map.Entry.comparingByKey()))              // a parità di quantità, ordine alfabetico
            .limit(5)
            .map(e -> e.getKey() + " (" + e.getValue() + ")")
            .toList();
    }

    // Due passi: prima il totale di ogni scontrino (negozio + numero), poi la media degli scontrini di ogni negozio
    Map<String, Double> scontrinoMedioPerNegozio() {
        Map<String, Map<String, Double>> totaliScontrino = vendite.stream()
            .collect(Collectors.groupingBy(RigaVendita::negozio, TreeMap::new,
                     Collectors.groupingBy(RigaVendita::scontrino,
                                           Collectors.summingDouble(RigaVendita::importo))));

        return totaliScontrino.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().values().stream().mapToDouble(Double::doubleValue).average().orElse(0),
                (a, b) -> a,                                                           // chiavi uniche: non serve unire
                TreeMap::new));
    }

    List<String> categorieSottoMedia() {
        Map<String, Double> perCategoria = vendite.stream()
            .collect(Collectors.groupingBy(RigaVendita::categoria, TreeMap::new,
                     Collectors.summingDouble(RigaVendita::importo)));

        double mediaCategorie = perCategoria.values().stream()
            .mapToDouble(Double::doubleValue).average().orElse(0);

        return perCategoria.entrySet().stream()
            .filter(e -> e.getValue() < mediaCategorie)
            .map(Map.Entry::getKey)
            .toList();
    }
}
```

Output ottenuto:

```text
{Milano=260.0, Roma=415.0, Torino=79.0}
[Polo (6), Cintura (4), Sciarpa (3), Felpa (2), Parka (2)]
{Milano=86.66666666666667, Roma=138.33333333333334, Torino=26.333333333333332}
[Accessori, Scarpe]
true
```

#### Note sulla soluzione

- **Resi**: la scelta è scritta nel commento sopra `totalePerNegozio`. I resi restano nelle righe, con importo e quantità negativi, e riducono i totali: il report misura il **venduto netto**. Così la somma dei negozi coincide con la somma delle righe (il controllo finale stampa `true`) e un reso non può essere contato due volte, perché non c'è nessun trattamento speciale.
- **Totale per negozio**: `groupingBy` con `TreeMap::new` come factory (negozi in ordine alfabetico) e `summingDouble` come collector a valle.
- **Top 5**: si raggruppa per prodotto sommando le quantità (il reso la riduce), poi si ordina l'`entrySet` per valore decrescente e, a parità, per nome: l'ordine diventa **deterministico**, importante per un report che si ripete ogni mese. `Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())` serve perché il compilatore, da solo, non riesce a dedurre i tipi.
- **Scontrino medio in due passi**: il numero dello scontrino **non è unico** tra i negozi (nei dati di prova `M-1` esiste a Milano e a Roma), quindi si raggruppa prima per negozio e poi per scontrino, sommando gli importi. Poi si fa la media dei totali di ogni negozio. Se si raggruppasse solo per numero di scontrino, due scontrini diversi verrebbero fusi.
- **Categorie sotto la media**: si calcola prima la media (`double mediaCategorie`, variabile *effectively final*, usata nella lambda del `filter`), poi si filtrano le categorie. Sono due pipeline, perché il secondo passo ha bisogno del risultato del primo.
- **Nessuna lista esterna modificata**: tutte le pipeline restituiscono il risultato (`collect`, `toList`).
- Il programma lavora su una `List` già in memoria: 48.000 righe sono poche per un computer normale, quindi i secondi richiesti sono ampiamente rispettati.

### Caso 2: Analisi dei log di un servizio dopo un'ora di rallentamento

#### Il caso

- **Contesto**: sei in un team di supporto di un servizio di prenotazione online. Dopo un rallentamento serale, il responsabile tecnico vuole capire quali richieste hanno avuto errori o tempi lenti. I log sono 30 file di testo, uno per giorno, in una cartella.
- **Problema attuale**: oggi la ricerca si fa a mano con strumenti da terminale, per circa 90 minuti a ogni incidente. Ogni incidente richiede la stessa analisi, ma nessuno ricorda i comandi esatti.
- **Compito richiesto**: leggi i 30 file, trasforma ogni riga in un oggetto e produci con gli stream:
  - il numero di errori per ora del giorno
  - i 10 endpoint con il tempo medio più alto
  - l'endpoint con più errori, gestendo il caso in cui non ci sia nessun errore

  La lettura dei file solleva eccezioni checked: decidi come gestirle senza far fallire tutto per un solo file.
- **Vincoli e dati**:
  - Ogni riga ha il formato `2027-01-18T21:14:03 GET /prenota 200 1240` (momento, metodo, percorso, codice di risposta, millisecondi)
  - Circa 200.000 righe in totale
  - Circa l'1% delle righe è malformato e va scartato, ma conteggiato
  - Il codice di risposta ≥ 500 è un errore
  - Un file può mancare o non essere leggibile
- **Risultato atteso**:
  - L'analisi completa gira con un solo comando
  - Le righe scartate e i file non leggibili compaiono nel riepilogo finale (numero e nome)
  - Il tempo per un'analisi passa da circa 90 minuti a meno di 5

#### Implementazione

File `AnalisiLog.java`:

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalisiLog {

    private static final Pattern FORMATO =
        Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2} (GET|POST|PUT|DELETE) \\S+ \\d{3} \\d+");

    // Un solo comando: java AnalisiLog.java /percorso/cartella-log
    public static void main(String[] args) throws IOException {
        List<Path> files;
        try (Stream<Path> contenuto = Files.list(Path.of(args[0]))) {      // Files.list va chiuso: try-with-resources
            files = contenuto.filter(p -> p.toString().endsWith(".log")).sorted().toList();
        }
        analizza(files);
    }

    static void analizza(List<Path> files) {
        // 1. Lettura: ogni file produce un esito, un file illeggibile NON ferma gli altri
        List<EsitoFile> esiti = files.stream().map(AnalisiLog::leggi).toList();

        List<String> nonLeggibili = esiti.stream()
            .filter(e -> !e.letto())
            .map(e -> e.file().getFileName().toString())
            .toList();

        // 2. Tutte le righe di tutti i file in un unico stream
        List<String> righe = esiti.stream()
            .flatMap(e -> e.righe().stream())
            .toList();

        // 3. Righe valide e righe malformate: groupingBy su un boolean
        Map<Boolean, List<String>> perValidita = righe.stream()
            .collect(Collectors.groupingBy(r -> FORMATO.matcher(r).matches()));
        List<RigaLog> log = perValidita.getOrDefault(true, List.of()).stream()
            .map(AnalisiLog::parse)
            .toList();
        int scartate = perValidita.getOrDefault(false, List.of()).size();

        // 4. Analisi
        Map<Integer, Long> erroriPerOra = log.stream()
            .filter(RigaLog::errore)
            .collect(Collectors.groupingBy(r -> r.momento().getHour(), TreeMap::new, Collectors.counting()));

        List<String> piuLenti = log.stream()
            .collect(Collectors.groupingBy(RigaLog::percorso, Collectors.averagingInt(RigaLog::millisecondi)))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(10)
            .map(e -> e.getKey() + " " + Math.round(e.getValue()) + " ms")
            .toList();

        String piuErrori = log.stream()
            .filter(RigaLog::errore)
            .collect(Collectors.groupingBy(RigaLog::percorso, Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())                      // Optional: vuoto se non ci sono errori
            .map(e -> e.getKey() + " (" + e.getValue() + " errori)")
            .orElse("nessun errore");

        // 5. Riepilogo
        System.out.println("File letti: " + (files.size() - nonLeggibili.size()) + "/" + files.size());
        System.out.println("File non leggibili: " + nonLeggibili);
        System.out.println("Righe valide: " + log.size() + ", scartate: " + scartate);
        System.out.println("Errori per ora: " + erroriPerOra);
        System.out.println("Endpoint più lenti: " + piuLenti);
        System.out.println("Endpoint con più errori: " + piuErrori);
    }

    // Gestione dell'eccezione checked dentro un metodo, non dentro la lambda dello stream
    static EsitoFile leggi(Path file) {
        try {
            return new EsitoFile(file, Files.readAllLines(file), true);
        } catch (IOException e) {
            return new EsitoFile(file, List.of(), false);
        }
    }

    static RigaLog parse(String riga) {
        String[] parti = riga.split(" ");
        return new RigaLog(LocalDateTime.parse(parti[0]), parti[1], parti[2],
                           Integer.parseInt(parti[3]), Integer.parseInt(parti[4]));
    }
}

record EsitoFile(Path file, List<String> righe, boolean letto) { }

record RigaLog(LocalDateTime momento, String metodo, String percorso, int codice, int millisecondi) {
    boolean errore() {
        return codice >= 500;
    }
}
```

Output ottenuto:

```text
File letti: 2/4
File non leggibili: [2027-01-20.log, 2027-01-21.log]
Righe valide: 6, scartate: 1
Errori per ora: {9=2, 21=1}
Endpoint più lenti: [/prenota 2380 ms, /camere 540 ms, /home 90 ms]
Endpoint con più errori: /prenota (2 errori)
```

#### Note sulla soluzione

- **Un file illeggibile non ferma gli altri**: l'eccezione checked (`IOException`) viene gestita in un metodo a parte (`leggi`), non dentro la lambda. Il metodo restituisce sempre un `EsitoFile` (file, righe, `letto` vero/falso): in caso di errore le righe sono vuote e `letto` è `false`. Così l'elenco dei file non leggibili si ricava con un normale `filter` + `map`.
- **`flatMap`**: `esiti.stream().flatMap(e -> e.righe().stream())` trasforma «uno stream di file, ognuno con la sua lista di righe» in un unico stream di righe (il cenno del Tema 2).
- **Righe malformate**: un `Pattern` stabilisce se la riga ha il formato giusto. `groupingBy` su un boolean divide le righe in due gruppi (`true` valide, `false` scartate): in questo modo le righe scartate si **contano** senza un secondo passaggio sui file. Solo le righe valide arrivano al `parse`, che quindi non può fallire.
- **Errori per ora**: `filter(RigaLog::errore)` + `groupingBy` sull'ora + `counting()`, con `TreeMap::new` per avere le ore in ordine.
- **Endpoint più lenti**: `averagingInt` come collector a valle, poi ordinamento decrescente e `limit(10)`.
- **Nessun errore**: `max(...)` restituisce un `Optional`, e `.map(...).orElse("nessun errore")` copre il caso dello stream vuoto senza `if` e senza `get()`.
- **`Files.list`** restituisce uno stream che va **chiuso**: per questo è dentro un `try` con le risorse. Il programma si lancia con un solo comando: `java AnalisiLog.java /percorso/cartella-log`.
- Nell'output di esempio il file `2027-01-20.log` contiene byte non validi (non decodificabile in UTF-8) e `2027-01-21.log` è una cartella: entrambi finiscono nell'elenco dei file non leggibili, e l'analisi continua con gli altri.

---

## Tema 3 — `Optional`

### Caso 1: Scegliere il canale di contatto per un sollecito di pagamento

#### Il caso

- **Contesto**: lavori sul sistema di una piccola compagnia assicurativa. Ogni mese circa 800 clienti hanno una rata scaduta e vanno avvisati: per email se c'è, altrimenti per SMS, altrimenti per lettera all'indirizzo di residenza.
- **Problema attuale**: l'anagrafica ha molti campi facoltativi: circa il 18% dei clienti non ha l'email, circa il 25% non ha un cellulare valido, il 3% non ha nemmeno un indirizzo completo. Il codice attuale contiene una cascata di `if (x != null)` annidati: ha già prodotto 2 `NullPointerException` in produzione che hanno bloccato tutto l'invio.
- **Compito richiesto**: riscrivi la scelta del canale con `Optional`. I getter dei campi facoltativi restituiscono `Optional`, e il metodo `canalePreferito(cliente)` descrive la sequenza di ripiego senza `if` e senza `get()`. Distingui poi i clienti per cui nessun canale è disponibile, che vanno segnalati all'ufficio clienti.
- **Vincoli e dati**:
  - `Cliente(codice, nome, email, cellulare, indirizzo)`, con i tre ultimi campi che possono essere `null` (campi normali, `Optional` solo nei getter)
  - Un'email è valida solo se contiene `@`, un cellulare solo se inizia con `3` ed è lungo 10 cifre: un valore non valido equivale a un valore mancante
  - Email, telefono e indirizzo sono dati personali: i messaggi di log non devono riportarli
- **Risultato atteso**:
  - Nessun `if` di controllo `null` nel metodo, e nessun `get()`
  - Con 6 clienti di prova (uno per ogni combinazione significativa di dati mancanti) il canale scelto è quello atteso, senza eccezioni
  - Il riepilogo finale elenca il numero di clienti raggiunti per canale e i codici di quelli non raggiungibili, senza dati personali

#### Implementazione

File `SollecitiDemo.java`:

```java
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SollecitiDemo {

    public static void main(String[] args) {
        List<Cliente> clienti = List.of(
            new Cliente("C001", "Anna Verdi",    "anna@example.it", "3331234567", "Via Roma 1, Milano"),   // email
            new Cliente("C002", "Luca Neri",     "luca.example.it", "3339876543", null),                   // email senza @ -> SMS
            new Cliente("C003", "Sara Bianchi",  null,              "33312345",   "Via Po 7, Torino"),     // cellulare corto -> lettera
            new Cliente("C004", "Paolo Rossi",   null,              null,         null),                   // nessun canale
            new Cliente("C005", "Elena Gialli",  "elena@example.it", null,        "Via Dante 3, Roma"),    // email
            new Cliente("C006", "Marco Blu",     null,              "3471112222", "   "));                 // SMS (indirizzo vuoto)

        for (Cliente c : clienti) {
            System.out.println(c.codice() + " -> " + Solleciti.canalePreferito(c).tipo());   // niente dati personali nel log
        }

        Solleciti.riepilogo(clienti);
    }
}

class Cliente {
    private final String codice;
    private final String nome;
    private final String email;          // campi normali: possono essere null
    private final String cellulare;
    private final String indirizzo;

    Cliente(String codice, String nome, String email, String cellulare, String indirizzo) {
        this.codice = codice;
        this.nome = nome;
        this.email = email;
        this.cellulare = cellulare;
        this.indirizzo = indirizzo;
    }

    String codice() { return codice; }
    String nome()   { return nome; }

    // Un valore non valido equivale a un valore mancante: il filter lo trasforma in Optional vuoto
    Optional<String> email() {
        return Optional.ofNullable(email).filter(e -> e.contains("@"));
    }

    Optional<String> cellulare() {
        return Optional.ofNullable(cellulare).filter(c -> c.matches("3\\d{9}"));
    }

    Optional<String> indirizzo() {
        return Optional.ofNullable(indirizzo).filter(a -> !a.isBlank());
    }
}

enum TipoCanale { EMAIL, SMS, LETTERA, NESSUNO }

record Canale(TipoCanale tipo, String destinazione) {
    static final Canale NESSUNO = new Canale(TipoCanale.NESSUNO, null);
}

class Solleciti {

    // Sequenza di ripiego: email, poi SMS, poi lettera, poi nessun canale. Nessun if, nessun get().
    static Canale canalePreferito(Cliente c) {
        return c.email().map(e -> new Canale(TipoCanale.EMAIL, e))
            .orElseGet(() -> c.cellulare().map(t -> new Canale(TipoCanale.SMS, t))
                .orElseGet(() -> c.indirizzo().map(a -> new Canale(TipoCanale.LETTERA, a))
                    .orElse(Canale.NESSUNO)));
    }

    // Variante più compatta con Optional.or (Java 9): ogni ripiego è un Optional alternativo
    static Optional<Canale> canalePreferitoConOr(Cliente c) {
        return c.email().map(e -> new Canale(TipoCanale.EMAIL, e))
            .or(() -> c.cellulare().map(t -> new Canale(TipoCanale.SMS, t)))
            .or(() -> c.indirizzo().map(a -> new Canale(TipoCanale.LETTERA, a)));
    }

    static void riepilogo(List<Cliente> clienti) {
        Map<TipoCanale, Long> perCanale = clienti.stream()
            .collect(Collectors.groupingBy(c -> canalePreferito(c).tipo(), TreeMap::new, Collectors.counting()));

        List<String> nonRaggiungibili = clienti.stream()
            .filter(c -> canalePreferito(c).tipo() == TipoCanale.NESSUNO)
            .map(Cliente::codice)                               // solo il codice cliente, nessun dato personale
            .toList();

        System.out.println("Clienti per canale: " + perCanale);
        System.out.println("Non raggiungibili: " + nonRaggiungibili);
    }
}
```

Output ottenuto:

```text
C001 -> EMAIL
C002 -> SMS
C003 -> LETTERA
C004 -> NESSUNO
C005 -> EMAIL
C006 -> SMS
Clienti per canale: {EMAIL=2, SMS=2, LETTERA=1, NESSUNO=1}
Non raggiungibili: [C004]
```

#### Note sulla soluzione

- **I campi restano normali, i getter restituiscono `Optional`**: `Optional.ofNullable(campo)` trasforma il `null` in un `Optional` vuoto. È una classe e non un `record` perché un record non permette di cambiare il tipo restituito dall'accessor.
- **Un valore non valido equivale a un valore mancante**: il `filter` dentro il getter (`contains("@")`, `matches("3\\d{9}")`, `!isBlank()`) svuota l'`Optional` quando il dato non è valido. Il resto del codice non deve più preoccuparsi della validità.
- **Sequenza di ripiego senza `if` e senza `get()`**: `map` costruisce il canale se il dato c'è; `orElseGet` passa al ripiego successivo **solo se serve** (la lambda non viene eseguita altrimenti); l'ultimo ripiego è la costante `Canale.NESSUNO` con `orElse`. Il risultato non è mai `null`.
- **Variante con `Optional.or`** (`canalePreferitoConOr`, Java 9): più compatta, restituisce un `Optional<Canale>` vuoto quando nessun canale è disponibile. È un cenno oltre il programma del corso, utile se gli studenti la incontrano.
- **Privacy**: il log stampa solo il codice cliente e il *tipo* di canale; mai email, numero o indirizzo. Lo stesso nel riepilogo, che contiene numeri e codici.
- **I 6 clienti di prova** coprono: email valida; email senza `@` (ripiego su SMS); cellulare troppo corto (ripiego su lettera); nessun dato; email e indirizzo senza cellulare; solo cellulare con indirizzo vuoto.

### Caso 2: Consultazione dello stato di un ordine con costi di ripiego

#### Il caso

- **Contesto**: sei nel team che sviluppa il servizio «Dov'è il mio ordine?» di un negozio online, con circa 6.000 consultazioni al giorno. Il servizio cerca l'ordine e, se è già partito, il codice di tracciamento del corriere.
- **Problema attuale**: oggi un ordine inesistente restituisce `null`, e l'errore emerge solo più avanti in una `NullPointerException` che il cliente vede come «Errore 500». Nell'ultima settimana 37 clienti hanno segnalato il problema. Inoltre, quando il tracking manca, il codice interroga comunque ogni volta il sistema del corriere (circa 400 ms a chiamata), anche quando il dato era già disponibile.
- **Compito richiesto**: riscrivi la ricerca in modo che:
  - un ordine inesistente produca un'eccezione di dominio con un messaggio chiaro (da tradurre in un 404 in MG5)
  - un ordine esistente ma non ancora spedito dia la risposta «Non ancora spedito»
  - la chiamata costosa al corriere avvenga solo quando serve davvero

  Spiega, con un esempio, perché in questo caso `orElse` e `orElseGet` danno risultati diversi in termini di costo.
- **Vincoli e dati**:
  - `Ordine(numero, cliente, trackingCode)`, con `trackingCode` nullo finché l'ordine non parte
  - La ricerca per numero è una `Map<String, Ordine>` (restituisce `null` per i mancanti)
  - Il metodo che interroga il corriere è simulato con `System.out.println` e una pausa breve, per rendere visibile quante volte viene chiamato
  - Non usare `Optional` come parametro o come campo
- **Risultato atteso**:
  - Con 3 ordini di prova (inesistente, esistente non spedito, esistente spedito) i tre casi producono i tre comportamenti attesi
  - La chiamata simulata al corriere compare 0 volte per l'ordine non spedito e 1 sola volta per quello spedito
  - Nessun `null` esce dal metodo e nessun `get()` compare nel codice

#### Implementazione

File `OrdiniDemo.java`:

```java
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OrdiniDemo {

    public static void main(String[] args) {
        Map<String, Ordine> archivio = Map.of(
            "ORD-2", new Ordine("ORD-2", "Anna Verdi", null),          // esiste, non ancora spedito
            "ORD-3", new Ordine("ORD-3", "Luca Neri", "TRK-555"));     // esiste, spedito

        ClienteCorriere corriere = new ClienteCorriere();
        ServizioOrdini servizio = new ServizioOrdini(archivio, corriere);

        try {
            servizio.stato("ORD-1");                                   // inesistente
        } catch (OrdineNonTrovatoException e) {
            System.out.println(e.getMessage());                        // Ordine inesistente: ORD-1
        }

        System.out.println(servizio.stato("ORD-2"));                   // Non ancora spedito
        System.out.println("Chiamate al corriere: " + corriere.chiamate());   // 0

        System.out.println(servizio.stato("ORD-3"));                   // In transito
        System.out.println("Chiamate al corriere: " + corriere.chiamate());   // 1

        System.out.println(servizio.stato("ORD-3"));                   // già in cache
        System.out.println("Chiamate al corriere: " + corriere.chiamate());   // ancora 1

        // Confronto con la versione sbagliata (orElse): il corriere viene interrogato anche con il dato in cache
        System.out.println(servizio.statoConOrElse("ORD-3"));
        System.out.println("Chiamate al corriere: " + corriere.chiamate());   // 2
    }
}

class OrdineNonTrovatoException extends RuntimeException {
    OrdineNonTrovatoException(String numero) {
        super("Ordine inesistente: " + numero);
    }
}

record Ordine(String numero, String cliente, String trackingCode) {     // trackingCode è null finché l'ordine non parte
    Optional<String> tracking() {
        return Optional.ofNullable(trackingCode);
    }
}

// Simula il sistema del corriere: lento e contabilizza le chiamate
class ClienteCorriere {
    private int chiamate;

    String stato(String tracking) {
        chiamate++;
        System.out.println("  [corriere] interrogo " + tracking);
        try {
            Thread.sleep(50);                                           // nella realtà ~400 ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "In transito";
    }

    int chiamate() {
        return chiamate;
    }
}

class ServizioOrdini {
    private final Map<String, Ordine> archivio;
    private final ClienteCorriere corriere;
    private final Map<String, String> cacheStati = new HashMap<>();     // tracking -> ultimo stato noto

    ServizioOrdini(Map<String, Ordine> archivio, ClienteCorriere corriere) {
        this.archivio = archivio;
        this.corriere = corriere;
    }

    Optional<Ordine> cerca(String numero) {
        return Optional.ofNullable(archivio.get(numero));               // Map.get restituisce null: lo trasformo
    }

    String stato(String numero) {
        Ordine ordine = cerca(numero)
            .orElseThrow(() -> new OrdineNonTrovatoException(numero));  // eccezione di dominio (in MG5: 404)

        return ordine.tracking()
            .map(this::statoDelCorriere)                                // eseguito solo se il tracking esiste
            .orElse("Non ancora spedito");                              // costante: orElse va bene
    }

    // Giusto: la chiamata al corriere sta in una lambda, parte solo se la cache non ha il dato
    private String statoDelCorriere(String tracking) {
        return Optional.ofNullable(cacheStati.get(tracking))
            .orElseGet(() -> {
                String stato = corriere.stato(tracking);
                cacheStati.put(tracking, stato);
                return stato;
            });
    }

    // Sbagliato: l'argomento di orElse viene calcolato PRIMA, quindi il corriere è chiamato sempre
    String statoConOrElse(String numero) {
        Ordine ordine = cerca(numero).orElseThrow(() -> new OrdineNonTrovatoException(numero));
        return ordine.tracking()
            .map(t -> Optional.ofNullable(cacheStati.get(t)).orElse(corriere.stato(t)))
            .orElse("Non ancora spedito");
    }
}
```

Output ottenuto:

```text
Ordine inesistente: ORD-1
Non ancora spedito
Chiamate al corriere: 0
  [corriere] interrogo TRK-555
In transito
Chiamate al corriere: 1
In transito
Chiamate al corriere: 1
  [corriere] interrogo TRK-555
In transito
Chiamate al corriere: 2
```

#### Note sulla soluzione

- **Ordine inesistente**: `cerca` trasforma il `null` della `Map` in un `Optional` (`ofNullable`), e `orElseThrow` lancia `OrdineNonTrovatoException` con il numero nel messaggio. In MG5 questa eccezione diventerà una risposta 404.
- **Ordine non spedito**: `trackingCode` resta un campo normale; il metodo `tracking()` lo restituisce come `Optional`. `map(this::statoDelCorriere)` si esegue solo se il tracking esiste, e `orElse("Non ancora spedito")` copre l'altro caso. L'output mostra **0 chiamate** al corriere.
- **Ordine spedito**: il primo accesso interroga il corriere (**1 chiamata**) e salva lo stato in una cache; i successivi usano la cache e il contatore resta a 1.
- **`orElse` o `orElseGet`?** Dipende dal costo del ripiego:
  - `"Non ancora spedito"` è una costante già pronta: `orElse` va bene.
  - La chiamata al corriere è costosa (400 ms): deve stare in `orElseGet(() -> ...)`, che esegue la lambda solo se la cache è vuota.
  - `statoConOrElse` mostra l'errore: `Optional.ofNullable(cache.get(t)).orElse(corriere.stato(t))` calcola `corriere.stato(t)` **prima** di sapere se serve. Con la cache già piena, l'output mostra comunque una chiamata in più (da 1 a 2). Con 6.000 consultazioni al giorno e 400 ms a chiamata, sono fino a 40 minuti di attese inutili.
- **Nessun `null` esce dal metodo**, nessun `get()`, nessun `Optional` come parametro o come campo (`tracking()` e `cerca()` lo usano solo come tipo di ritorno).

---

## Nota per il docente

Un caso d'uso è pronto quando non si può riassumere in una frase senza numeri né esempi concreti. In fase di correzione o discussione in aula, usa questa domanda-test con gli studenti: «puoi dirmelo con un numero o un esempio reale?»

Le implementazioni sono **una** soluzione possibile: accetta alternative che rispettino i vincoli del caso (per esempio, per il caso dei ticket, una versione con gli stream; per i solleciti, la variante con `Optional.or`).
