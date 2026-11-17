# Java a oggetti moderno e concorrenza essenziale (Base · MG1)

Corso Java e Spring Boot Base (EXP_DATLAS_024) · Java 25 LTS · Progetto fil rouge: *BiblioTech*

Cinque temi, ciascuno con i 4 blocchi: classi/interfacce/record/enum, collezioni, eccezioni, `switch` expression e `var`, concorrenza essenziale.

---

## Tema 1 — Classi, interfacce, record ed enum

### Spiegazione teorica

- **Classe**: stato (campi) + comportamento (metodi)
- **Incapsulamento**: campi `private`, accesso solo tramite metodi che proteggono le regole dell'oggetto
- **Interfaccia**: contratto (cosa si può fare) senza dire come; più classi possono implementarla
- **Metodo `default`**: un'interfaccia può fornire un'implementazione di base di un metodo; le classi che la implementano la ereditano e possono sovrascriverla
- **Composizione invece di ereditarietà**: un oggetto *ha un* collaboratore (campo) invece di *essere un* sottotipo
- **`record`**: classe dati immutabile; costruttore, accessor, `equals`, `hashCode`, `toString` generati
- **Costruttore compatto** del record: punto in cui validare i dati
- **`enum`**: insieme chiuso di valori; può avere campi e metodi
- **Factory method statico**: metodo `static` con nome parlante che crea l'oggetto (`Prestito.apri(...)`), con costruttore `private`
- **Oggetto immutabile**: non cambia dopo la creazione, quindi si può condividere senza rischi
- **`java.time`**: `LocalDate` è una data senza orario, immutabile; `LocalDate.of(2026, 11, 17)` la crea, `plusDays` e simili restituiscono una nuova data, `isAfter` confronta, `ChronoUnit.DAYS.between(a, b)` conta i giorni. Negli esempi `oggi` si passa come parametro invece di usare `LocalDate.now()`: il codice è prevedibile e si può provare (MG8)
- Da Java 25 un programma minimo può omettere classe e `static void main` (compact source file, JEP 512): lo citiamo soltanto, perché Spring richiede classi complete

**Record o classe?**
- **Record**: dati che non cambiano, identificati per valore (un libro del catalogo, un DTO, una chiave di `Map`)
- **Classe**: stato che cambia nel tempo con regole (`Prestito`), oppure quando un framework richiede un costruttore vuoto: le entità JPA di MG6 sono classi, non record
- L'immutabilità del record è **superficiale**: se contiene una lista, la lista resta modificabile da fuori
- Soluzione: copia difensiva nel costruttore compatto

```java
public record Scaffale(String nome, List<Libro> libri) {
    public Scaffale {
        libri = List.copyOf(libri);     // copia immutabile: chi ha passato la lista non può più alterarla
    }
}
```
- Dopo la copia, `scaffale.libri().add(...)` lancia `UnsupportedOperationException`
- Svuotare la lista originale non cambia più lo scaffale

### Esempi teorici di utilizzo

- **`record`** per i dati che non cambiano: un libro del catalogo, una riga di risposta di un'API (i DTO di MG5 saranno record)
- **`enum`** quando i valori possibili sono pochi e noti: genere di un libro, stato di un prestito, ruolo di un utente
- **Incapsulamento** quando una regola deve valere sempre: un prestito non si restituisce due volte
- **Factory method** quando creare l'oggetto richiede un calcolo: la scadenza dipende dal genere del libro
- **Interfaccia + composizione** quando un comportamento deve poter cambiare senza toccare chi lo usa: inviare un avviso per email, SMS o console
- **Immutabilità** quando un oggetto è letto da più parti del programma: nessuno può modificarlo per errore

### Esempi di codice

#### Base
Cosa mostra: un `enum` con campi e un `record` che valida i propri dati.

```java
// Genere.java — un enum può avere campi, costruttore e metodi
public enum Genere {
    ROMANZO("Romanzo", 30),
    SAGGIO("Saggio", 21),
    FUMETTO("Fumetto", 14);

    private final String etichetta;       // campo final: non cambia dopo la creazione
    private final int giorniPrestito;

    Genere(String etichetta, int giorniPrestito) {   // il costruttore di un enum è sempre privato
        this.etichetta = etichetta;
        this.giorniPrestito = giorniPrestito;
    }

    public String etichetta() { return etichetta; }
    public int giorniPrestito() { return giorniPrestito; }
}

// Libro.java — un record è una classe dati immutabile
public record Libro(String isbn, String titolo, int anno, Genere genere) {
    public Libro {                                    // costruttore compatto: controlla i dati
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN obbligatorio");
        }
        if (anno < 1450) {                            // prima della stampa a caratteri mobili
            throw new IllegalArgumentException("Anno non valido: " + anno);
        }
    }
}

// Uso
Libro rosa = new Libro("978-88-0000-001-1", "Il nome della rosa", 1980, Genere.ROMANZO);
System.out.println(rosa);                  // toString generato: Libro[isbn=..., titolo=..., ...]
System.out.println(rosa.titolo());         // accessor: titolo(), non getTitolo()
System.out.println(rosa.genere().giorniPrestito());   // 30

Libro copia = new Libro("978-88-0000-001-1", "Il nome della rosa", 1980, Genere.ROMANZO);
System.out.println(rosa.equals(copia));                        // true: stessi valori
System.out.println(rosa == copia);                             // false: sono due oggetti diversi
System.out.println(rosa.hashCode() == copia.hashCode());       // true
```

- Il record non ha setter: i suoi campi non cambiano mai
- Due record con gli stessi valori sono `equals` (e hanno lo stesso `hashCode`): l'uguaglianza è per valore, mentre `==` confronta i riferimenti
- Il costruttore compatto non ripete i parametri e assegna i campi da solo, dopo i controlli
- Il valore dell'enum non si crea con `new`: si usa `Genere.ROMANZO`

Cosa mostra: scorrere i valori di un enum e convertire un testo in enum.

```java
for (Genere g : Genere.values()) {                             // tutti i valori, nell'ordine di dichiarazione
    System.out.println(g + " → " + g.etichetta() + ", " + g.giorniPrestito() + " giorni");
}

Genere saggio = Genere.valueOf("SAGGIO");                      // da testo a enum
System.out.println(saggio.name() + " " + saggio.ordinal());    // SAGGIO 1  (ordinal: posizione, da 0)

Genere errato = Genere.valueOf("saggio");                      // IllegalArgumentException: No enum constant Genere.saggio
```

- `values()` serve per cicli, menu e test su tutti i casi
- `valueOf` vuole il nome identico, maiuscole comprese: altrimenti `IllegalArgumentException`
- `ordinal()` cambia se si riordina l'enum: non va salvato altrove (database, file)

#### Intermedio
Cosa mostra: una classe con stato modificabile, incapsulata, creata da un factory method statico.

```java
public class Prestito {
    private final Libro libro;                 // campi private: nessuno li tocca da fuori
    private final String utente;
    private final LocalDate scadenza;
    private LocalDate restituzione;            // null finché il libro è fuori

    private Prestito(Libro libro, String utente, LocalDate scadenza) {   // costruttore privato
        this.libro = libro;
        this.utente = utente;
        this.scadenza = scadenza;
    }

    // Factory method: nome parlante e calcolo della scadenza in un posto solo
    public static Prestito apri(Libro libro, String utente, LocalDate oggi) {
        LocalDate scadenza = oggi.plusDays(libro.genere().giorniPrestito());
        return new Prestito(libro, utente, scadenza);
    }

    public void restituisci(LocalDate data) {
        if (restituzione != null) {                         // la regola vive dentro la classe
            throw new IllegalStateException("Prestito già restituito");
        }
        restituzione = data;
    }

    public boolean attivo() { return restituzione == null; }

    public boolean inRitardo(LocalDate oggi) {
        LocalDate riferimento = attivo() ? oggi : restituzione;
        return riferimento.isAfter(scadenza);
    }

    public Libro libro() { return libro; }
    public String utente() { return utente; }
    public LocalDate scadenza() { return scadenza; }
}

// Uso
Prestito p = Prestito.apri(rosa, "mario", LocalDate.of(2026, 11, 17));
p.restituisci(LocalDate.of(2026, 12, 1));
p.restituisci(LocalDate.of(2026, 12, 2));     // IllegalStateException
```

- Chi usa `Prestito` non può metterlo in uno stato incoerente: l'unico modo per cambiarlo è `restituisci`
- `new Prestito(...)` non è accessibile fuori dalla classe: si passa da `Prestito.apri`
- La durata del prestito dipende dal genere ed è calcolata in un solo punto
- Un metodo `setRestituzione` pubblico avrebbe permesso di saltare la regola

#### Avanzato
Cosa mostra: interfaccia, due implementazioni e composizione. `Biblioteca` *ha un* `Notificatore`: si cambia comportamento senza modificare `Biblioteca`.

```java
public interface Notificatore {
    void invia(String destinatario, String messaggio);          // il contratto

    default void avvisaScadenza(Prestito p) {                   // metodo default: logica condivisa
        invia(p.utente(), "Il libro '" + p.libro().titolo() + "' scade il " + p.scadenza());
    }
}

public class NotificatoreConsole implements Notificatore {
    @Override
    public void invia(String destinatario, String messaggio) {
        System.out.println("[console] a " + destinatario + ": " + messaggio);
    }
}

public class NotificatoreEmail implements Notificatore {
    @Override
    public void invia(String destinatario, String messaggio) {
        System.out.println("[email] a " + destinatario + "@bibliotech.it: " + messaggio);
    }
}

public class Biblioteca {
    private final Notificatore notificatore;                    // composizione: un campo, non un "extends"

    public Biblioteca(Notificatore notificatore) {              // il collaboratore arriva da fuori
        this.notificatore = notificatore;
    }

    public Prestito presta(Libro libro, String utente, LocalDate oggi) {
        Prestito prestito = Prestito.apri(libro, utente, oggi);
        notificatore.avvisaScadenza(prestito);                  // Biblioteca non sa quale implementazione usa
        return prestito;
    }
}

// Uso: cambio il comportamento cambiando solo l'oggetto passato
Biblioteca conConsole = new Biblioteca(new NotificatoreConsole());
Biblioteca conEmail = new Biblioteca(new NotificatoreEmail());
conEmail.presta(rosa, "mario", LocalDate.of(2026, 11, 17));
```

- `Biblioteca` dipende dall'interfaccia, non da una classe concreta
- Il collaboratore si riceve dal costruttore: è l'idea che in MG3 diventerà Dependency Injection di Spring
- Con l'ereditarietà (`BibliotecaEmail extends Biblioteca`) ci sarebbe una sottoclasse per ogni variante
- Il metodo `default` evita di ripetere `avvisaScadenza` in ogni implementazione

Cosa mostra: la stessa funzione con una sottoclasse per ogni variante (alternativa alla `Biblioteca` qui sopra).

```java
// ❌ Con l'ereditarietà: una sottoclasse per ogni variante
public class Biblioteca {
    public Prestito presta(Libro libro, String utente, LocalDate oggi) {
        Prestito p = Prestito.apri(libro, utente, oggi);
        notifica(p);                                          // metodo che le sottoclassi ridefiniscono
        return p;
    }
    protected void notifica(Prestito p) { }
}

public class BibliotecaConsole extends Biblioteca {
    @Override protected void notifica(Prestito p) { System.out.println("[console] " + p.scadenza()); }
}

public class BibliotecaEmail extends Biblioteca {
    @Override protected void notifica(Prestito p) { System.out.println("[email] " + p.scadenza()); }
}

public class BibliotecaEmailConLog extends BibliotecaEmail {   // e se serve anche BibliotecaConsoleConLog?
    @Override protected void notifica(Prestito p) { System.out.println("[log] invio"); super.notifica(p); }
}
```

- Ogni combinazione (email + log, console + log, …) richiede una nuova sottoclasse: le classi crescono come il prodotto delle varianti
- Il comportamento è deciso quando si scrive il codice (`new BibliotecaEmail()`), non passando un oggetto diverso
- Una classe ha una sola superclasse; con la composizione si combinano più collaboratori
- L'ereditarietà resta giusta quando c'è davvero una relazione «è un» (`NoSuchFileException` è un `IOException`)

### Esercizi

#### Facile
Obiettivo: nel dominio di una palestra, creare il record `Abbonamento` (nome del socio, data di scadenza). Il costruttore compatto deve rifiutare un nome vuoto. Stampare un abbonamento e verificare che due abbonamenti con gli stessi valori siano uguali.

#### Medio
Obiettivo: creare l'`enum` `Livello` (BASE, PREMIUM, VIP) con il costo mensile come campo e un metodo che indica se il livello include la piscina. Creare la classe `Iscrizione` con costruttore privato e factory method statico `attiva(nome, livello, data)`. Il metodo `disdici(data)` non deve funzionare due volte.

#### Difficile
Obiettivo: definire l'interfaccia `RegolaAccesso` con il metodo `consenti(Iscrizione, LocalTime)`. Scrivere due implementazioni (per esempio: solo orario mattutino per il livello BASE; abbonamento non scaduto). La classe `Palestra` riceve nel costruttore la lista delle regole e fa entrare un socio solo se tutte le regole lo consentono. Scrivere un programma che provi almeno tre casi.

---

## Tema 2 — Collezioni: List, Set e Map

### Spiegazione teorica

- **`List`**: sequenza ordinata, con duplicati, accesso per posizione (`ArrayList`)
- **`Set`**: insieme senza duplicati (`HashSet` senza ordine, `LinkedHashSet` ordine di inserimento, `TreeSet` ordine naturale)
- **`Map`**: coppie chiave → valore, chiavi uniche (`HashMap`, `TreeMap`, `EnumMap`)
- Si dichiara l'**interfaccia** (`List`, `Set`, `Map`) e si sceglie l'**implementazione** solo alla creazione
- **Generics**: `List<Libro>` dice al compilatore cosa contiene la lista
- `List.of(...)`, `Set.of(...)`, `Map.of(...)` creano collezioni **immutabili**: ogni modifica lancia `UnsupportedOperationException`
- `Set` e chiavi di `Map` usano `equals` e `hashCode`: i `record` li hanno già corretti, per le classi vanno scritti
- Per scrivere `equals`: `o instanceof Autore a` controlla il tipo e dichiara la variabile `a` in un colpo solo (da Java 16); `Objects.equals(x, y)` confronta gestendo il `null`; `Objects.hash(...)` calcola l'hash dai campi
- Metodi utili di `Map`: `getOrDefault`, `computeIfAbsent`, `merge`

### Esempi teorici di utilizzo

- **`List`** per il catalogo mostrato in ordine, o per i prestiti di un utente
- **`Set`** per tenere gli ISBN già inseriti ed evitare i duplicati
- **`Map`** per trovare un libro dal suo ISBN senza scorrere tutto il catalogo
- **`Map` di liste** per raggruppare: i prestiti di ciascun utente
- **`Map` con conteggio** per statistiche: quanti prestiti per genere
- **Collezioni immutabili** per restituire dati senza permettere a chi li riceve di modificarli

### Esempi di codice

#### Base
Cosa mostra: una `List` per il catalogo e un `Set` per scartare i duplicati.

```java
List<Libro> catalogo = new ArrayList<>();      // variabile di tipo interfaccia, oggetto ArrayList
catalogo.add(rosa);
catalogo.add(new Libro("978-88-0000-002-8", "Sapiens", 2011, Genere.SAGGIO));
catalogo.add(new Libro("978-88-0000-003-5", "Maus", 1986, Genere.FUMETTO));

for (Libro libro : catalogo) {                 // for-each: scorre in ordine di inserimento
    System.out.println(libro.titolo() + " (" + libro.anno() + ")");
}
System.out.println("Libri in catalogo: " + catalogo.size());
System.out.println("Primo: " + catalogo.get(0).titolo());      // accesso per posizione

Set<String> isbnVisti = new HashSet<>();
System.out.println(isbnVisti.add(rosa.isbn()));    // true: inserito
System.out.println(isbnVisti.add(rosa.isbn()));    // false: già presente, il Set non cambia
```

- `List<Libro>` accetta solo `Libro`: un errore di tipo si scopre in compilazione
- La `List` mantiene l'ordine; l'`add` del `Set` restituisce `false` se il valore c'è già
- Per scorrere basta il for-each, senza indici
- Dichiarare `List` e non `ArrayList` permette di cambiare implementazione in seguito

Cosa mostra: lo stesso input in tre `Set` con ordine diverso.

```java
List<String> nomi = List.of("Rossi", "Bianchi", "Verdi", "Bianchi", "Esposito");

Set<String> hash = new HashSet<>(nomi);                        // nessun ordine garantito
Set<String> inserimento = new LinkedHashSet<>(nomi);           // ordine di inserimento
Set<String> alfabetico = new TreeSet<>(nomi);                  // ordine naturale

System.out.println(hash);          // [Esposito, Bianchi, Verdi, Rossi]  (l'ordine può variare)
System.out.println(inserimento);   // [Rossi, Bianchi, Verdi, Esposito]
System.out.println(alfabetico);    // [Bianchi, Esposito, Rossi, Verdi]
```

- Il costruttore accetta una collezione; il doppione «Bianchi» sparisce in tutti e tre
- `HashSet` è il più veloce ma senza ordine; `LinkedHashSet` ricorda l'ordine di arrivo; `TreeSet` ordina
- Stessa famiglia per le mappe: `HashMap`, `LinkedHashMap`, `TreeMap`
- Un `TreeSet` di classi proprie richiede `Comparable` o un `Comparator` (come `sorted()` in MG2), altrimenti `ClassCastException`

#### Intermedio
Cosa mostra: una `Map` per cercare un libro dal suo ISBN.

```java
Map<String, Libro> perIsbn = new HashMap<>();
for (Libro libro : catalogo) {
    perIsbn.put(libro.isbn(), libro);          // chiave: ISBN, valore: il libro
}

Libro trovato = perIsbn.get("978-88-0000-002-8");      // ricerca diretta, senza scorrere la lista
System.out.println(trovato.titolo());

Libro assente = perIsbn.get("000");                    // chiave inesistente: restituisce null
System.out.println(assente);                           // null

Libro predefinito = new Libro("000", "Sconosciuto", 2000, Genere.SAGGIO);
Libro scelto = perIsbn.getOrDefault("000", predefinito);       // chiave assente: valore di ripiego, niente null
System.out.println(scelto.titolo());                           // Sconosciuto

System.out.println(perIsbn.containsKey("978-88-0000-003-5"));   // true

List<Libro> sola = List.of(rosa);
sola.add(trovato);                                     // UnsupportedOperationException: List.of è immutabile
```

- `get` con una chiave assente restituisce `null`: va gestito, altrimenti `NullPointerException`
- `getOrDefault(chiave, ripiego)` restituisce il ripiego se la chiave manca, **senza** inserirlo nella mappa. Il ripiego viene creato comunque: è la differenza tra `orElse` e `orElseGet` che si vedrà in MG2
- `put` con una chiave già presente sostituisce il valore precedente
- Una `Map` non ha ordine garantito: per l'ordine servono `LinkedHashMap` o `TreeMap`
- `List.of` serve per dati che non devono cambiare

Cosa mostra: i tre modi di scorrere una `Map` e come togliere un elemento.

```java
for (Map.Entry<String, Libro> e : perIsbn.entrySet()) {        // ogni coppia chiave → valore
    System.out.println(e.getKey() + " → " + e.getValue().titolo());
}
for (String isbn : perIsbn.keySet()) { System.out.println(isbn); }          // solo le chiavi
for (Libro libro : perIsbn.values()) { System.out.println(libro.titolo()); } // solo i valori

Libro rimosso = perIsbn.remove("978-88-0000-003-5");           // restituisce il valore tolto
System.out.println(perIsbn.remove("000"));                     // null: la chiave non c'era

perIsbn.entrySet().removeIf(e -> e.getValue().anno() < 2000);  // togliere durante lo scorrimento: solo così
```

- `entrySet()` dà le coppie; `keySet()` e `values()` servono quando basta una metà
- `remove` restituisce il valore tolto, o `null` se la chiave non c'era
- Togliere elementi dentro un for-each su una `Map` dà `ConcurrentModificationException`, come per la lista: si usa `removeIf`
- L'ordine di scorrimento dipende dalla `Map` scelta (`HashMap`: nessun ordine)

Errore comune: togliere elementi dalla lista durante il for-each.

```java
for (Libro l : catalogo) {
    if (l.anno() < 2000) {
        catalogo.remove(l);                  // ConcurrentModificationException al giro successivo
    }
}

catalogo.removeIf(l -> l.anno() < 2000);     // soluzione: removeIf (la lambda si spiega in MG2)
```
- Il nome è fuorviante: **non c'entrano i thread**, l'iteratore del for-each si accorge che la lista è cambiata
- L'errore compare al giro successivo, non sulla riga del `remove`
- Vale anche per `add` dentro il ciclo
- Il modo corretto è `removeIf` (oppure `Iterator.remove`)

Errore comune: un `Set` con classi senza `equals` e `hashCode`.

```java
class Autore {
    private final String nome;
    Autore(String nome) { this.nome = nome; }
    // nessun equals/hashCode: due oggetti sono "uguali" solo se sono lo stesso riferimento
}

Set<Autore> autori = new HashSet<>();
autori.add(new Autore("Eco"));
autori.add(new Autore("Eco"));
System.out.println(autori.size());           // 2, non 1
```

La correzione:

```java
@Override
public boolean equals(Object o) {
    return o instanceof Autore a && Objects.equals(nome, a.nome);   // confronto per valore
}

@Override
public int hashCode() {
    return Objects.hash(nome);                                      // stessi campi di equals
}
// Alternativa: dichiarare Autore come record, che li genera da solo
```
- `HashSet` e `HashMap` usano `hashCode` per trovare il «cassetto» e `equals` per confrontare
- `equals` e `hashCode` si scrivono sempre insieme e sugli stessi campi
- Se si modifica un campo usato da `hashCode` mentre l'oggetto è nel `Set` o è chiave di una `Map`, l'oggetto «si perde»
- Per questo le chiavi di `Map` sono di solito `String`, numeri, enum o record

Cosa mostra: l'incapsulamento «bucato» da un getter, e la correzione.

```java
public class Catalogo {
    private final List<Libro> libri = new ArrayList<>();

    public void aggiungi(Libro libro) { libri.add(libro); }

    public List<Libro> libriEsposti() { return libri; }           // ❌ espone la lista interna
    public List<Libro> libri() { return List.copyOf(libri); }     // ✅ copia non modificabile
}

catalogo.libriEsposti().clear();      // svuota il catalogo senza passare da Catalogo
catalogo.libri().clear();             // UnsupportedOperationException
```

- Un campo `private` non basta: se il getter restituisce la lista, chi la riceve la modifica senza passare dalle regole della classe
- `List.copyOf` restituisce una copia non modificabile: si può leggere, non alterare il catalogo
- `Collections.unmodifiableList(libri)` non copia: è una *vista* in sola lettura che segue le modifiche successive alla lista originale
- La copia ha un costo a ogni chiamata: per liste molto grandi si preferisce la vista

#### Avanzato
Cosa mostra: raggruppare i prestiti per utente e contare i prestiti per genere.

```java
List<Prestito> prestiti = List.of(
    Prestito.apri(rosa, "mario", LocalDate.of(2026, 11, 17)),
    Prestito.apri(catalogo.get(1), "mario", LocalDate.of(2026, 11, 18)),
    Prestito.apri(catalogo.get(2), "giulia", LocalDate.of(2026, 11, 18))
);

// Raggruppamento: utente -> lista dei suoi prestiti
Map<String, List<Prestito>> perUtente = new HashMap<>();
for (Prestito p : prestiti) {
    perUtente
        .computeIfAbsent(p.utente(), chiave -> new ArrayList<>())   // se manca, crea la lista vuota
        .add(p);                                                    // poi aggiunge il prestito
}
System.out.println(perUtente.get("mario").size());                 // 2

// Conteggio: genere -> numero di prestiti
Map<Genere, Integer> perGenere = new EnumMap<>(Genere.class);       // Map ottimizzata per le chiavi enum
for (Prestito p : prestiti) {
    perGenere.merge(p.libro().genere(), 1, Integer::sum);           // se la chiave manca mette 1, altrimenti somma
}
System.out.println(perGenere);    // {ROMANZO=1, SAGGIO=1, FUMETTO=1}
```

- `computeIfAbsent` evita il classico `if (!map.containsKey(...)) { map.put(..., new ArrayList<>()); }`
- `merge` aggiorna un conteggio in una riga sola
- Le espressioni `chiave -> ...` e `Integer::sum` sono lambda e riferimenti a metodo: li spiega MG2, per ora si leggono come «cosa fare in quel caso»
- `EnumMap` mantiene le chiavi nell'ordine dell'enum

### Esercizi

#### Facile
Obiettivo: nel dominio di un ricettario, data una lista di ingredienti con ripetizioni, produrre l'elenco senza duplicati mantenendo l'ordine di prima comparsa (`LinkedHashSet`). Stampare quanti ingredienti c'erano e quanti ne restano.

#### Medio
Obiettivo: nel dominio di un parcheggio, usare una `Map<String, Auto>` con la targa come chiave. Scrivere le operazioni «entra», «esce» e «cerca per targa». Gestire i casi di targa già presente e di targa inesistente senza `NullPointerException`.

#### Difficile
Obiettivo: nel dominio di un ricettario, partire da `Map<String, Set<String>>` (ricetta → ingredienti) e costruire l'indice inverso `Map<String, Set<String>>` (ingrediente → ricette che lo usano). Stampare gli ingredienti usati da almeno due ricette, in ordine alfabetico.

---

## Tema 3 — Eccezioni

### Spiegazione teorica

- Un'**eccezione** è un oggetto che descrive un errore e interrompe il flusso normale
- **`try` / `catch` / `finally`**: provo, intercetto, eseguo sempre il blocco finale
- **Checked** (derivano da `Exception`, il compilatore obbliga a gestirle o dichiararle con `throws`) e **unchecked** (derivano da `RuntimeException`)
- `throw` lancia un'eccezione; `throws` dichiara che un metodo può lanciarla
- Eccezioni standard utili: `IllegalArgumentException` (argomento sbagliato), `IllegalStateException` (oggetto nello stato sbagliato), `NoSuchElementException`
- **Eccezioni di dominio**: classi proprie (`LibroNonDisponibileException`) con nome che descrive il problema
- **Propagazione**: se un metodo non cattura l'eccezione, questa risale la catena delle chiamate (`a()` → `b()` → `c()`) fino a `main`; se nessuno la cattura il thread termina e stampa lo *stack trace*
- In Spring la cattura il framework: senza una gestione diventa una risposta HTTP 500 (MG5)
- **Checked o unchecked?** Checked quando il chiamante può davvero recuperare e va costretto a pensarci (file, rete); unchecked per errori di programmazione e violazioni di regole di dominio
- Spring usa quasi solo unchecked; `@Transactional` (MG6) annulla le modifiche di default solo con eccezioni unchecked
- **Più `catch`**: prima il tipo più specifico; il multi-catch `catch (IOException | NumberFormatException e)` gestisce due tipi non imparentati nello stesso modo
- **`try-with-resources`**: chiude automaticamente file e connessioni
- Si intercetta il tipo più specifico, non si ignora mai un'eccezione, si conserva la **causa** quando se ne rilancia un'altra
- In Spring le eccezioni di dominio unchecked verranno trasformate in risposte HTTP (MG5)

### Esempi teorici di utilizzo

- **Input non valido**: l'anno di un libro arriva come testo e non è un numero
- **Regola di business violata**: prestito di un libro già in prestito
- **Risorsa non trovata**: ISBN che non esiste nel catalogo
- **Problema di input/output**: file del catalogo mancante o illeggibile
- **Pulizia delle risorse**: chiudere sempre il file, anche se la lettura fallisce
- **Errori uniformi in un'API**: un'eccezione di dominio per ogni tipo di errore, tradotta una sola volta in risposta HTTP

### Esempi di codice

#### Base
Cosa mostra: `try`/`catch`/`finally` su una conversione che può fallire.

```java
String testo = "19x0";
try {
    int anno = Integer.parseInt(testo);        // può lanciare NumberFormatException
    System.out.println("Anno: " + anno);       // non viene eseguita se la riga sopra lancia
} catch (NumberFormatException e) {            // intercetto il tipo più specifico
    System.out.println("Anno non valido: " + testo);
} finally {
    System.out.println("Lettura terminata");   // eseguito sempre, errore o no
}
```

- Se `parseInt` fallisce, l'esecuzione salta direttamente al `catch`
- `NumberFormatException` è unchecked: il compilatore non obbliga a gestirla, ma qui conviene farlo
- `finally` serve per le operazioni che devono avvenire comunque
- Catturare `Exception` per «sicurezza» nasconde errori diversi da quello previsto

Cosa mostra: l'eccezione che risale da sola e l'ordine dei `catch`.

```java
static void leggiAnno(String testo) {
    int anno = Integer.parseInt(testo);                        // lancia NumberFormatException
    if (anno < 1450) {
        throw new IllegalArgumentException("Anno troppo antico: " + anno);
    }
}

static void importaLibro(String testo) {
    leggiAnno(testo);                                          // nessun try: l'eccezione risale a chi chiama
}

public static void main(String[] args) {
    try {
        importaLibro("1200");
    } catch (NumberFormatException e) {                        // prima il tipo più specifico
        System.out.println("Non è un numero");
    } catch (IllegalArgumentException e) {                     // poi quello più generale
        System.out.println("Dato non valido: " + e.getMessage());   // stampa: Dato non valido: Anno troppo antico: 1200
    }
}
```
- `importaLibro` non ha un `try`: l'eccezione di `leggiAnno` arriva fino a `main`
- `NumberFormatException` è una sottoclasse di `IllegalArgumentException`: con i due `catch` invertiti il codice non compila
- Con `"abc"` si entra nel primo `catch`, con `"1200"` nel secondo
- Se `main` non catturasse nulla, il programma terminerebbe con lo stack trace

Cosa mostra: un metodo che dichiara `throws`, l'errore del compilatore e le due correzioni.

```java
static String leggiTitolo(Path file) throws IOException {      // dichiara: può lanciare IOException
    return Files.readString(file);
}

static void carica() {
    String t = leggiTitolo(Path.of("catalogo.txt"));           // errore di compilazione:
}                                                              // unreported exception IOException; must be caught or declared to be thrown

// Soluzione 1: gestirla qui
static void caricaGestita() {
    try {
        String t = leggiTitolo(Path.of("catalogo.txt"));
    } catch (IOException e) {
        System.out.println("File non leggibile: " + e.getMessage());
    }
}

// Soluzione 2: dichiararla a sua volta: la decisione passa a chi chiama
static void caricaDichiarata() throws IOException {
    String t = leggiTitolo(Path.of("catalogo.txt"));
}
```

- Con una eccezione checked il compilatore **obbliga** a scegliere: gestirla o dichiararla
- `throws` non gestisce niente: sposta la responsabilità a chi chiama
- La si gestisce dove si sa cosa fare (messaggio, valore di ripiego, nuovo tentativo)

Cosa mostra: un'eccezione rilanciata con la causa, e come leggere ciò che stampa Java.

```java
public class Importa {
    static void leggiAnno(String testo) {
        int anno = Integer.parseInt(testo);
        if (anno < 1450) throw new IllegalArgumentException("Anno troppo antico: " + anno);
    }

    static void importaLibro(String testo) {
        try {
            leggiAnno(testo);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Import fallito per: " + testo, e);
        }
    }

    public static void main(String[] args) {
        importaLibro("19x0");
    }
}
```

```
Exception in thread "main" java.lang.IllegalStateException: Import fallito per: 19x0   ← 1. l'eccezione uscita: tipo e messaggio
	at Importa.importaLibro(Importa.java:11)          ← 2. dove è stata lanciata: la prima riga del TUO codice
	at Importa.main(Importa.java:16)                  ← chi l'ha chiamata, risalendo
Caused by: java.lang.NumberFormatException: For input string: "19x0"   ← 3. la causa originale: spesso è l'errore vero
	at java.base/java.lang.Integer.parseInt(...)      ← righe del JDK (abbreviate)
	at Importa.leggiAnno(Importa.java:3)              ← 4. la prima riga del tuo codice nella causa
	at Importa.importaLibro(Importa.java:9)
	... 1 more                                        ← righe in comune con quelle sopra, non ripetute
```

- Si legge dall'alto: prima il tipo e il messaggio, poi le righe `at`, dalla più recente alla più vecchia
- Tra tante righe di librerie e framework, si cerca la prima del **proprio** package (`it.corso…`)
- `Caused by:` è la causa originale: nei log di Spring l'errore vero è quasi sempre nell'ultimo `Caused by`
- Senza la causa passata come secondo argomento (`new IllegalStateException(msg, e)`) questa parte sparisce

#### Intermedio
Cosa mostra: un'eccezione di dominio lanciata da una regola di business.

```java
// Eccezione di dominio: unchecked, con un messaggio che spiega il problema
public class LibroNonDisponibileException extends RuntimeException {
    public LibroNonDisponibileException(String isbn) {
        super("Libro già in prestito: " + isbn);
    }
}

public class ServizioPrestiti {
    private final Map<String, Libro> catalogo = new HashMap<>();
    private final Set<String> inPrestito = new HashSet<>();

    public void aggiungi(Libro libro) { catalogo.put(libro.isbn(), libro); }

    public Prestito presta(String isbn, String utente, LocalDate oggi) {
        Libro libro = catalogo.get(isbn);
        if (libro == null) {
            throw new NoSuchElementException("Libro inesistente: " + isbn);   // eccezione standard
        }
        if (!inPrestito.add(isbn)) {                                          // add = false se già presente
            throw new LibroNonDisponibileException(isbn);                     // eccezione di dominio
        }
        return Prestito.apri(libro, utente, oggi);
    }
}

// Chi chiama decide cosa fare
try {
    servizio.presta("978-88-0000-001-1", "mario", oggi);
    servizio.presta("978-88-0000-001-1", "giulia", oggi);     // secondo prestito dello stesso libro
} catch (LibroNonDisponibileException e) {
    System.out.println("Impossibile: " + e.getMessage());
}
```

- Il nome della classe dice che cosa è andato storto, senza leggere il messaggio
- Il servizio non stampa e non decide come mostrare l'errore: lo segnala e basta
- Chi chiama intercetta l'eccezione che sa gestire; le altre risalgono
- Estendere `RuntimeException` evita `throws` su ogni metodo della catena

Cosa mostra: due modi sbagliati di gestire un'eccezione e quello corretto.

```java
// ❌ 1. catch vuoto: l'errore sparisce e il programma prosegue in uno stato sbagliato
try {
    servizio.presta(isbn, utente, oggi);
} catch (Exception e) { }

// ❌ 2. si rilancia una nuova eccezione con il solo messaggio: la causa originale è persa
try {
    leggiCatalogo(file);
} catch (IOException e) {
    throw new IllegalStateException("Errore: " + e.getMessage());
}

// ✅ si intercetta il tipo che si sa gestire; se si rilancia, si passa la causa
try {
    servizio.presta(isbn, utente, oggi);
} catch (LibroNonDisponibileException e) {
    System.out.println("Impossibile: " + e.getMessage());                    // da MG8: un logger al posto di println
}
// ...  throw new IllegalStateException("Catalogo non leggibile", e);        // la causa è il secondo argomento
```

- Un `catch` vuoto è il caso peggiore: il problema non compare né a video né nel log
- `catch (Exception e)` intercetta anche errori imprevisti (un `NullPointerException`, un bug): si cattura il tipo che si sa gestire
- Un'eccezione rilanciata senza causa cancella l'errore originale: nello stack trace manca il `Caused by:` che serve per capire
- In un'applicazione vera si scrive nel log (SLF4J, MG8): né `println` né `printStackTrace()`

#### Avanzato
Cosa mostra: `try-with-resources`, eccezione checked incapsulata in una di dominio, causa conservata.

```java
public class CatalogoNonLeggibileException extends RuntimeException {
    public CatalogoNonLeggibileException(Path file, Throwable causa) {
        super("Impossibile leggere il catalogo: " + file, causa);     // la causa originale non si perde
    }
}

public List<String> leggiIsbn(Path file) {
    List<String> isbn = new ArrayList<>();
    try (BufferedReader lettore = Files.newBufferedReader(file)) {    // chiuso automaticamente a fine blocco
        String riga;
        while ((riga = lettore.readLine()) != null) {                 // readLine lancia IOException (checked)
            if (!riga.isBlank()) {
                isbn.add(riga.trim());
            }
        }
        return isbn;
    } catch (IOException e) {                                         // eccezione checked: gestita qui
        throw new CatalogoNonLeggibileException(file, e);             // ne lancio una di dominio, con la causa
    }
}
```

- Il lettore si chiude anche se `readLine` fallisce: non serve un `finally`
- `IOException` è checked: o la si gestisce, o il metodo deve dichiarare `throws IOException`
- Il chiamante riceve un'eccezione del dominio e non deve conoscere i dettagli del file system
- Passare `e` come causa conserva la traccia completa dell'errore originale nel log

Cosa mostra: spezzare una riga `cliente;pizza;quantità` in campi.

```java
String riga = "mario;Margherita;2";
String[] campi = riga.split(";");                              // ["mario", "Margherita", "2"]
String cliente = campi[0];
int quantita = Integer.parseInt(campi[2]);                     // NumberFormatException se non è un numero

String[] corta = "mario;Margherita".split(";");
System.out.println(corta[2]);                                  // ArrayIndexOutOfBoundsException: Index 2 out of bounds for length 2
```

- `split` restituisce un array di `String`
- Una riga con meno campi del previsto dà `ArrayIndexOutOfBoundsException`: si controlla `campi.length` prima di leggerli
- L'argomento è un'espressione regolare: per `|` o `.` serve l'escape (`split("\\|")`)

### Esercizi

#### Facile
Obiettivo: scrivere il metodo `int leggiIntero(String testo, int predefinito)` che restituisce il numero contenuto nel testo, oppure il valore predefinito se il testo non è un intero valido. Provarlo con «42», «4 2» e una stringa vuota.

#### Medio
Obiettivo: nel dominio di un distributore automatico, creare le eccezioni `ProdottoEsauritoException` e `CreditoInsufficienteException` (con il credito mancante nel messaggio). Scrivere il metodo `erogaProdotto(codice, credito)` che le lancia nei casi opportuni e un programma che prova i tre esiti (successo, esaurito, credito insufficiente).

#### Difficile
Obiettivo: nel dominio di una pizzeria, leggere da un file di testo gli ordini (una riga per ordine: `cliente;pizza;quantità`) con `try-with-resources`. Le righe malformate non devono interrompere la lettura: vanno raccolte in un elenco di errori con il numero di riga. Se il file non esiste, lanciare un'eccezione di dominio che conserva la causa. Stampare alla fine ordini validi ed errori.

---

## Tema 4 — `switch` expression e `var`

### Spiegazione teorica

- **`switch` expression**: `switch` che produce un valore, da assegnare a una variabile o restituire
- Sintassi con la freccia `->`: niente `break`, niente *fall-through* (nessun «cadere» nel caso successivo)
- Più valori nello stesso caso: `case ROMANZO, SAGGIO -> ...`
- Con un `enum`, se tutti i valori sono coperti **non serve `default`** e il compilatore segnala se ne manca uno
- Con un blocco `{ ... }` si usa **`yield`** per restituire il valore
- **`var`**: il compilatore deduce il tipo di una variabile **locale** dal lato destro
- Il tipo resta statico e controllato: `var` non rende Java dinamico
- `var` non si usa per campi, parametri, tipi di ritorno, né quando il tipo non è chiaro dalla riga

### Esempi teorici di utilizzo

- **Tariffe e regole per categoria**: multa giornaliera diversa per ogni genere
- **Messaggi per stato**: testo diverso per prestito regolare, in scadenza, scaduto
- **Conversioni**: codice di errore → descrizione
- **`var`** quando il tipo è già scritto a destra: `var catalogo = new ArrayList<Libro>()`
- **`var`** nei for-each per evitare tipi lunghi
- **Da evitare `var`** quando nasconde il tipo: `var x = servizio.calcola()` non dice cosa sia `x`

### Esempi di codice

#### Base
Cosa mostra: una `switch` expression su un `enum`, senza `default`.

```java
Genere genere = Genere.FUMETTO;

double multaGiornaliera = switch (genere) {      // l'intero switch è un'espressione che produce un double
    case ROMANZO, SAGGIO -> 0.50;                // due valori nello stesso caso
    case FUMETTO -> 0.30;                        // tutti i valori dell'enum sono coperti: niente default
};

System.out.println("Multa al giorno: " + multaGiornaliera + " €");
```

- Ogni ramo con `->` si conclude da solo: non servono `break`
- Se si aggiunge un valore a `Genere` e non lo si gestisce qui, il codice non compila: un errore scoperto subito
- Il risultato si assegna direttamente a una variabile, senza variabile temporanea da inizializzare
- Il punto e virgola chiude l'assegnazione, dopo la graffa

#### Intermedio
Cosa mostra: `switch` su un numero, con `default` e un ramo a blocco con `yield`.

```java
int giorniRitardo = 12;

String fascia = switch (giorniRitardo / 7) {      // settimane intere di ritardo
    case 0 -> "lieve";                            // meno di una settimana
    case 1 -> "medio";                            // da 7 a 13 giorni
    default -> {                                  // per valori non enumerabili serve il default
        String dettaglio = "grave";
        yield dettaglio + " (" + giorniRitardo + " giorni)";    // yield restituisce il valore del blocco
    }
};

System.out.println("Ritardo " + fascia);          // Ritardo medio
```

- Con tipi non enumerati (numeri, stringhe) il `default` è obbligatorio
- Se un ramo ha bisogno di più istruzioni si apre un blocco e si chiude con `yield`
- Una `switch` expression deve sempre produrre un valore in ogni ramo
- `return` dentro un ramo non è ammesso: l'espressione deve completarsi

#### Avanzato
Cosa mostra: `var` e `switch` expression insieme, su uno stato calcolato.

```java
enum StatoPrestito { RESTITUITO, REGOLARE, IN_SCADENZA, SCADUTO }

// import java.time.temporal.ChronoUnit;
static StatoPrestito stato(Prestito p, LocalDate oggi) {
    if (!p.attivo()) return StatoPrestito.RESTITUITO;
    long giorniAllaScadenza = ChronoUnit.DAYS.between(oggi, p.scadenza());
    if (giorniAllaScadenza < 0) return StatoPrestito.SCADUTO;
    return giorniAllaScadenza <= 3 ? StatoPrestito.IN_SCADENZA : StatoPrestito.REGOLARE;
}

var oggi = LocalDate.of(2026, 12, 20);                  // var: LocalDate, evidente da destra
for (var p : prestiti) {                                // var: Prestito, dedotto dalla lista
    var messaggio = switch (stato(p, oggi)) {           // var: String, dedotta dai rami
        case RESTITUITO -> "Chiuso";
        case REGOLARE -> "Nessuna azione";
        case IN_SCADENZA -> "Promemoria a " + p.utente();
        case SCADUTO -> {
            long ritardo = ChronoUnit.DAYS.between(p.scadenza(), oggi);
            yield "Sollecito a " + p.utente() + ": " + ritardo + " giorni di ritardo";
        }
    };
    System.out.println(messaggio);
}
```

- Lo stato è calcolato in un solo metodo; il `switch` decide solo il messaggio
- `var` accorcia il codice senza perdere il tipo: l'IDE mostra `String` passando il mouse
- Tutti e quattro i casi sono coperti, quindi nessun `default`
- Se il tipo non è chiaro dalla riga, è meglio scriverlo esplicitamente

Cosa mostra: gli errori del compilatore su `var` e il caso in cui è meglio il tipo esplicito.

```java
var n = 5;                    // int: il tipo è dedotto e resta int
n = "ciao";                   // errore: incompatible types: String cannot be converted to int
var x;                        // errore: cannot infer type for local variable x (manca l'inizializzazione)
var y = null;                 // errore: cannot infer type for local variable y (null non ha un tipo)

var risultato = servizio.calcola();    // compila, ma da qui non si capisce che cosa sia: meglio il tipo esplicito
```

- `var` non rende Java dinamico: il tipo è deciso una volta dal compilatore e non cambia
- Serve sempre un valore a destra da cui dedurlo: niente `var x;` e niente `null`
- Si usa quando il tipo si legge già sulla stessa riga (`new ArrayList<Libro>()`, `LocalDate.of(...)`)

### Esercizi

#### Facile
Obiettivo: usando `DayOfWeek`, scrivere una `switch` expression che restituisce «feriale» o «weekend» per un giorno dato. Provarla con tutti i sette giorni in un ciclo.

#### Medio
Obiettivo: nel dominio scolastico, trasformare un voto da 0 a 10 in un giudizio («insufficiente», «sufficiente», «buono», «ottimo») con una `switch` expression. Il voto fuori dall'intervallo 0–10 deve produrre un'eccezione. Usare `var` solo dove il tipo è evidente.

#### Difficile
Obiettivo: nel dominio di un parcheggio, definire gli `enum` `TipoVeicolo` (AUTO, MOTO, FURGONE) e `Fascia` (DIURNA, NOTTURNA). Calcolare la tariffa oraria con una `switch` expression su `TipoVeicolo`, usando nei rami un blocco con `yield` dove la fascia cambia il prezzo. Il metodo `calcolaImporto(tipo, fascia, ore)` applica un tetto giornaliero.

---

## Tema 5 — Concorrenza essenziale

### Spiegazione teorica

- **Thread**: un flusso di esecuzione; un'applicazione può averne molti in parallelo
- `start()` avvia il thread, `join()` ne attende la fine; `join` e `sleep` lanciano `InterruptedException` (checked, Tema 3): negli esempi la si dichiara con `throws` nel `main`
- Un server web gestisce ogni richiesta con un thread diverso: **i bean di Spring (singleton) sono condivisi da più thread insieme**
- **Race condition**: due thread leggono e modificano lo stesso dato; il risultato dipende dall'ordine di esecuzione
- `visite++` sembra una operazione, ma sono tre: leggi, somma, scrivi: **non è atomica**
- **`synchronized`**: un solo thread alla volta può eseguire il blocco o il metodo
- **Classi atomiche** (`AtomicInteger`): incrementi sicuri senza blocchi
- **Collezioni concorrenti** (`ConcurrentHashMap`): operazioni sicure; i metodi atomici come `putIfAbsent` e `merge` evitano il *check-then-act*
- **`ExecutorService`**: un pool di thread a cui si consegnano compiti (`submit`); il risultato arriva con un `Future`
- **Immutabilità**: un oggetto che non cambia (record, campi `final`) si condivide tra thread senza problemi
- I **virtual thread** (thread leggeri) si vedono nel corso Avanzato, MG2

### Esempi teorici di utilizzo

- **Contatore di visite** di una pagina: molte richieste lo incrementano insieme
- **Ultimo libro disponibile**: due utenti lo chiedono nello stesso istante, deve vincerne uno solo
- **Cache condivisa** tra richieste: una mappa letta e scritta da più thread
- **Elaborazione in parallelo**: inviare mille avvisi di scadenza senza farli uno dopo l'altro
- **Un servizio Spring con un campo modificabile** (un contatore, una lista) è un bug in attesa di manifestarsi sotto carico: la regola è nell'ultimo esempio
- **Oggetti immutabili** per i dati condivisi: il modo più semplice di non avere race condition

### Esempi di codice

#### Base
Cosa mostra: un contatore condiviso che sbaglia il conteggio (race condition).

```java
public class ContatoreVisite {
    private int visite = 0;

    public void incrementa() {
        visite++;                            // NON atomico: leggi, somma, scrivi
    }

    public int valore() { return visite; }
}

public static void main(String[] args) throws InterruptedException {
    ContatoreVisite contatore = new ContatoreVisite();

    // Il compito da eseguire: 100.000 incrementi (la sintassi () -> {...} è una lambda, la spiega MG2)
    Runnable compito = () -> {
        for (int i = 0; i < 100_000; i++) {
            contatore.incrementa();
        }
    };

    Thread t1 = new Thread(compito);         // due thread sullo stesso oggetto
    Thread t2 = new Thread(compito);
    t1.start();
    t2.start();
    t1.join();                               // attendo che finiscano entrambi
    t2.join();

    System.out.println(contatore.valore());  // atteso 200000, di solito molto meno
}
```

- Due thread leggono lo stesso valore, lo incrementano e lo riscrivono: un incremento va perso
- Il risultato cambia a ogni esecuzione: i bug di concorrenza sono difficili da riprodurre
- `join` serve per leggere il valore solo dopo la fine dei thread
- Il codice «sembra corretto»: nessun errore, nessuna eccezione, solo un numero sbagliato

#### Intermedio
Cosa mostra: due correzioni, `synchronized` e `AtomicInteger`.

```java
// Correzione 1: blocco con synchronized
public class ContatoreSicuro {
    private int visite = 0;

    public synchronized void incrementa() {   // un solo thread alla volta
        visite++;
    }

    public synchronized int valore() {        // anche la lettura sullo stesso "lucchetto"
        return visite;
    }
}

// Correzione 2: classe atomica, senza blocchi
public class ContatoreAtomico {
    private final AtomicInteger visite = new AtomicInteger();

    public void incrementa() {
        visite.incrementAndGet();              // lettura + somma + scrittura in un'unica operazione indivisibile
    }

    public int valore() { return visite.get(); }
}

// Stesso programma di prima, con ContatoreSicuro o ContatoreAtomico: il risultato è sempre 200000
```

- `synchronized` su un metodo usa l'oggetto stesso come «lucchetto»: se un thread è dentro, gli altri aspettano
- Va protetta anche la lettura, non solo la scrittura
- `AtomicInteger` è più semplice e più veloce quando serve solo un contatore
- Più `synchronized` si mettono, più i thread aspettano: si protegge solo ciò che serve

Perché sincronizzare anche la lettura: la visibilità.

```java
public class Lavoratore implements Runnable {
    private volatile boolean fermato = false;     // senza volatile il ciclo potrebbe non vedere mai il cambio

    public void ferma() { fermato = true; }       // chiamato da un altro thread

    @Override
    public void run() {
        while (!fermato) {
            // ... lavoro periodico
        }
        System.out.println("Fermato");
    }
}
```
- Senza sincronizzazione un thread può continuare a vedere un valore vecchio (cache dei core, riordino delle istruzioni): oltre all'atomicità serve la **visibilità**
- `synchronized` garantisce che chi legge veda l'ultima scrittura: per questo anche `valore()` è sincronizzato
- `volatile` garantisce la visibilità ma **non** l'atomicità: con un `volatile int` il `++` resta sbagliato
- `volatile` va bene per un indicatore on/off; per i contatori servono `AtomicInteger` o `synchronized`

#### Avanzato
Cosa mostra: prestiti concorrenti sullo stesso libro con `ConcurrentHashMap`, `putIfAbsent` e `ExecutorService`.

```java
public class RegistroPrestiti {
    private final Map<String, String> inPrestito = new ConcurrentHashMap<>();   // ISBN -> utente

    public boolean presta(String isbn, String utente) {
        // putIfAbsent controlla e inserisce in un colpo solo: restituisce null se la chiave non c'era
        return inPrestito.putIfAbsent(isbn, utente) == null;
    }
}

public static void main(String[] args) throws Exception {
    RegistroPrestiti registro = new RegistroPrestiti();
    ExecutorService pool = Executors.newFixedThreadPool(4);           // 4 thread riutilizzati
    List<Future<Boolean>> esiti = new ArrayList<>();

    for (int i = 1; i <= 10; i++) {
        String utente = "utente" + i;
        // 10 tentativi concorrenti sullo stesso libro; submit restituisce subito un Future
        esiti.add(pool.submit(() -> registro.presta("978-88-0000-001-1", utente)));
    }

    int riusciti = 0;
    for (Future<Boolean> esito : esiti) {
        if (esito.get()) {                  // get() attende il risultato del compito
            riusciti++;
        }
    }
    pool.shutdown();                        // il pool non accetta più compiti e i thread terminano

    System.out.println("Prestiti riusciti: " + riusciti);    // sempre 1
}
```

- Scrivere `if (!mappa.containsKey(k)) mappa.put(k, v)` **non è sicuro** nemmeno con `ConcurrentHashMap`: tra le due righe un altro thread può inserire
- `putIfAbsent` rende il controllo e la scrittura un'unica operazione
- `ExecutorService` evita di creare un thread per ogni compito e restituisce i risultati con `Future`
- Senza `shutdown()` il programma non termina: i thread del pool restano vivi
- Da Java 19 `ExecutorService` è `AutoCloseable`: si può scrivere `try (var pool = Executors.newFixedThreadPool(4)) { ... }`

Cosa mostra: `shutdown` non attende; `awaitTermination` sì.

```java
pool.shutdown();                                               // non accetta nuovi compiti, NON attende
if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {            // attende la fine dei compiti (massimo 10 secondi)
    pool.shutdownNow();                                        // se non finiscono, li interrompe
}
```

- Quando i compiti restituiscono un risultato, `Future.get()` basta ad attendere
- Quando non restituiscono nulla (`execute`, `Runnable`), serve `awaitTermination` prima di leggere un risultato condiviso
- `awaitTermination` lancia `InterruptedException`: come `join`, va dichiarata o gestita

Cosa mostra: un service Spring è un singleton condiviso da tutte le richieste (`@Service` marca la classe come bean: in MG3 si vede `@Component`, gli stereotipi in MG4; qui basta sapere che Spring crea una sola istanza).

```java
@Service
public class VisiteService {
    private int visite = 0;                          // ❌ stato modificabile in un singleton: race condition sotto carico

    public void registra() { visite++; }
}

@Service
public class VisiteServiceCorretto {
    private final VisiteRepository repository;       // ✅ solo dipendenze final: il bean non ha stato proprio

    public VisiteServiceCorretto(VisiteRepository repository) {
        this.repository = repository;
    }

    public void registra(String pagina) {
        String chiave = pagina.toLowerCase();        // ✅ variabile locale: ogni thread ha la sua
        repository.incrementa(chiave);               // lo stato condiviso sta nel database
    }
}
```
- Spring crea **una sola istanza** del bean e la usa per tutte le richieste, ciascuna su un thread diverso
- Regola: nei campi di un bean solo dipendenze `final`; niente contatori, liste o variabili di lavoro modificabili
- Lo stato condiviso va nel database, o in una struttura concorrente (`ConcurrentHashMap`, `AtomicInteger`)
- Le variabili di lavoro vanno dentro i metodi: sono locali e ogni thread ha le sue

### Esercizi

#### Facile
Obiettivo: nel dominio di un parcheggio con 10 posti, la classe `Parcheggio` ha il metodo `entra()` che, se ci sono posti liberi, ne occupa uno. Lanciare 50 thread che tentano di entrare e mostrare che i posti occupati possono superare 10. Poi correggere con `synchronized`.

#### Medio
Obiettivo: nel dominio dell'analisi dati, sommare un array di 1.000.000 di numeri dividendolo in 4 parti, ciascuna sommata da un compito inviato a un `ExecutorService` con `Future<Long>`. Combinare i quattro risultati e confrontarli con la somma calcolata in un solo thread.

#### Difficile
Obiettivo: nel dominio di un cinema con 50 posti, simulare 200 richieste di prenotazione (ciascuna sceglie un posto a caso) eseguite da un pool di 8 thread. Garantire che nessun posto sia assegnato due volte, contare richieste accolte e rifiutate e stampare il riepilogo. Descrivere in due righe perché una struttura dati immutabile per il risultato finale evita altri problemi.
