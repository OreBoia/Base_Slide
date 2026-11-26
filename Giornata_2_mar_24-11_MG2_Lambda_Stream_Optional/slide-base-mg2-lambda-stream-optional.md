# Lambda, Stream API e Optional (Base · MG2)

Corso Java e Spring Boot Base (EXP_DATLAS_024) · Java 25 LTS · Progetto fil rouge: *BiblioTech*

Tre temi, ciascuno con i 4 blocchi: lambda e interfacce funzionali, Stream API, `Optional`. Pattern della mezza giornata: **Strategy con le lambda** (regole di calcolo della multa intercambiabili).

Le classi `Libro`, `Genere`, `Prestito` sono quelle di MG1.

---

## Tema 1 — Lambda e interfacce funzionali

### Spiegazione teorica

- **Lambda**: una funzione senza nome, scritta dove serve: `(parametri) -> espressione`
- Permette di passare un **comportamento** come se fosse un dato (a un metodo, in una variabile, in una mappa)
- **Interfaccia funzionale**: interfaccia con **un solo metodo astratto**; la lambda ne fornisce l'implementazione
- Interfacce già pronte in `java.util.function`:
  - `Predicate<T>`: `T` → `boolean` (metodo `test`)
  - `Function<T, R>`: `T` → `R` (metodo `apply`)
  - `Consumer<T>`: usa un `T`, non restituisce nulla (metodo `accept`)
  - `BiConsumer<T, U>`: usa due valori `T` e `U`, non restituisce nulla (metodo `accept`)
  - `Supplier<T>`: nessun parametro, restituisce un `T` (metodo `get`)
  - `Comparator<T>`: confronta due `T` (metodo `compare`)
  - `Runnable`: nessun parametro, nessun risultato (il compito dei thread di MG1)
- **Riferimento a metodo**: forma breve di una lambda che chiama solo un metodo (`Libro::titolo`, `System.out::println`, `ArrayList::new`)
- **Composizione**: `Predicate` ha `and`, `or`, `negate`; `Function` ha `andThen`
- **Versioni «Bi»**: `BiConsumer`, `BiFunction`, `BiPredicate` sono le varianti con **due parametri** di `Consumer`, `Function`, `Predicate`; il caso tipico è `Map.forEach`, che riceve chiave e valore
- Una lambda può usare le variabili locali esterne solo se sono **effectively final** (non riassegnate)
- **Strategy**: scegliere un algoritmo a runtime; con le lambda non serve una classe per ogni variante

### Esempi teorici di utilizzo

- **Ordinare** il catalogo con criteri diversi (titolo, anno, genere) senza scrivere una classe per ogni criterio
- **Filtrare** con una condizione passata dall'esterno: libri recenti, prestiti scaduti
- **Strategy**: regole di calcolo della multa (fissa, giornaliera, con tetto) scelte da configurazione
- **Callback**: «quando hai finito, esegui questa azione»
- **Metodi già visti in MG1**: `removeIf`, `computeIfAbsent`, `merge`, il `Runnable` dei thread erano lambda e riferimenti a metodo
- **Framework**: Spring e le sue API usano ovunque lambda per configurare comportamenti (la configurazione della sicurezza di MG7, per esempio)
- **Lambda o classe?** Una lambda basta se la regola è una formula; serve una classe quando la regola ha stato o dipendenze (vedi l'ultimo esempio)

### Esempi di codice

#### Base
Cosa mostra: le quattro interfacce funzionali più comuni, scritte come lambda, e un ordinamento.

```java
Predicate<Libro> recente = l -> l.anno() >= 2000;          // Libro -> boolean
Function<Libro, String> titolo = l -> l.titolo();          // Libro -> String
Consumer<Libro> stampa = l -> System.out.println(l.titolo());   // usa il libro, non restituisce nulla
Supplier<LocalDate> oggi = () -> LocalDate.of(2026, 11, 24);    // nessun parametro

System.out.println(recente.test(rosa));       // false (1980)
System.out.println(titolo.apply(rosa));       // Il nome della rosa
stampa.accept(rosa);
System.out.println(oggi.get());

Comparator<String> ordineTitolo = (a, b) -> a.titolo().compareTo(b.titolo); 
// Con le collezioni
catalogo.sort((a, b) -> a.titolo().compareTo(b.titolo()));   // Comparator come lambda
catalogo.sort(ordineTitolo);
catalogo.forEach(stampa);                                    // Consumer applicato a ogni elemento
```

- La freccia `->` separa i parametri dal corpo; il tipo dei parametri è dedotto
- Con più di un parametro servono le parentesi: `(a, b) -> ...`; senza parametri: `() -> ...`
- Ogni interfaccia ha un solo metodo: `test`, `apply`, `accept`, `get`
- `sort` e `forEach` ricevono un comportamento: è il concetto base di tutto il Tema 2

#### Intermedio
Cosa mostra: riferimenti a metodo, `Comparator` composti e `Predicate` combinati.

```java
catalogo.sort(Comparator.comparing(Libro::titolo));                       // riferimento a metodo: Classe::metodo
catalogo.sort(Comparator.comparingInt(Libro::anno).reversed());          // numerico, dal più recente
catalogo.sort(Comparator.comparing(Libro::genere)
                        .thenComparing(Libro::titolo));                   // per genere, poi per titolo

catalogo.forEach(System.out::println);                                    // riferimento a metodo di un oggetto

Supplier<List<Libro>> nuovaLista = ArrayList::new;                        // riferimento a costruttore

Predicate<Libro> recente = l -> l.anno() >= 2000;
Predicate<Libro> fumetto = l -> l.genere() == Genere.FUMETTO;
Predicate<Libro> recenteNonFumetto = recente.and(fumetto.negate());       // composizione di condizioni
System.out.println(recenteNonFumetto.test(rosa));                         // false
```

- `Libro::titolo` equivale a `l -> l.titolo()`: più corto e più leggibile
- `Comparator.comparing` costruisce il confronto a partire dalla proprietà da usare
- `thenComparing` serve per i pareggi, `reversed` per l'ordine opposto
- `and`, `or`, `negate` evitano di scrivere una lambda lunga con più condizioni

Cosa mostra: composizione di due funzioni con `andThen`.

```java
UnaryOperator<Double> dieciPercento = p -> p * 0.90; 
// Double -> Double
Function<Double, Double> dieciPercentoFun = p -> p * 0.90; 
//queste due Function sono equivalenti, UnaryOperator assicura solo lo stesso tipo per entrmbi
UnaryOperator<Double> menoCinque = p -> p - 5;

Function<Double, Double> entrambi = dieciPercento.andThen(menoCinque);   // prima il 10%, poi -5
System.out.println(entrambi.apply(100.0));                                // 85.0
```
- `UnaryOperator<T>` è una `Function<T, T>`: ingresso e uscita dello stesso tipo
- `andThen` esegue prima la funzione di partenza, poi quella passata (`compose` fa l'ordine opposto)
- Si costruiscono regole complesse componendo regole semplici
- Stesso schema di `Predicate.and` e `or`: piccole condizioni combinate

Cosa mostra: `BiConsumer`, l'interfaccia con due parametri che non restituisce nulla.

```java
BiConsumer<Libro, Integer> stampaCopie =
    (libro, copie) -> System.out.println(libro.titolo() + ": " + copie + " copie");

stampaCopie.accept(rosa, 3);                      // Il nome della rosa: 3 copie

// Caso tipico: Map.forEach riceve chiave e valore
Map<Genere, Long> libriPerGenere = Map.of(Genere.SAGGIO, 12L, Genere.FUMETTO, 5L);
libriPerGenere.forEach((genere, quanti) -> System.out.println(genere + " -> " + quanti));

// Concatenare due azioni sugli stessi argomenti con andThen
BiConsumer<Libro, Integer> log =
    (libro, copie) -> System.out.println("LOG: aggiornato " + libro.titolo());
stampaCopie.andThen(log).accept(rosa, 3);         // prima stampa, poi log
```

- `accept(T, U)` ha due parametri e restituisce `void`: serve per fare un'azione, non per calcolare un risultato
- `Map.forEach` vuole un `BiConsumer`, perché ogni elemento della mappa ha due parti (chiave e valore)
- `andThen` esegue i due `BiConsumer` in sequenza, passando a entrambi gli **stessi** argomenti
- Se ti serve un risultato, usa `BiFunction<T, U, R>` (metodo `apply`) al posto di `BiConsumer`

#### Avanzato
Cosa mostra: il pattern Strategy con le lambda, per le regole di multa.

```java
@FunctionalInterface
public interface RegolaMulta {
    double calcola(long giorniRitardo);                        // unico metodo astratto = interfaccia funzionale
}

public class CalcolatoreMulte {
    private final RegolaMulta regola;                          // la strategia scelta

    public CalcolatoreMulte(RegolaMulta regola) {
        this.regola = regola;
    }

    public double multa(Prestito p, LocalDate oggi) {
        long ritardo = ChronoUnit.DAYS.between(p.scadenza(), oggi);
        return ritardo <= 0 ? 0 : regola.calcola(ritardo);     // delega alla strategia
    }
}

// Tre strategie: tre lambda, nessuna classe in più
RegolaMulta fissa = giorni -> 5.0;
RegolaMulta perGiorno = giorni -> giorni * 0.50;
RegolaMulta conTetto = giorni -> Math.min(giorni * 0.50, 10.0);

// Una lambda può usare una variabile esterna, se non cambia
double tariffa = 0.50;
RegolaMulta configurabile = giorni -> giorni * tariffa;        // tariffa è "effectively final"
// tariffa = 0.80;                                             // se decommentata: errore di compilazione

// Scelta a runtime, per esempio da un valore di configurazione
Map<String, RegolaMulta> regole = Map.of("fissa", fissa, "giornaliera", perGiorno, "tetto", conTetto);
CalcolatoreMulte calcolatore = new CalcolatoreMulte(regole.get("tetto"));
System.out.println(calcolatore.multa(prestito, LocalDate.of(2027, 1, 20)));   // multa con tetto a 10,0
```

- `RegolaMulta` ha un solo metodo: per questo si può scrivere come lambda
- `CalcolatoreMulte` non cambia mai: cambia la strategia che riceve nel costruttore
- Le strategie sono messe in una `Map` e scelte per nome: è lo schema che in MG4 Spring farà con `@Qualifier` e i profili
- Rispetto a MG1 (`Notificatore` con due classi) qui bastano due righe per variante, perché l'interfaccia ha un solo metodo

Cosa mostra: perché una lambda non può modificare una variabile esterna e come accumulare.

```java
int totale = 0;
catalogo.forEach(l -> totale += l.anno());                   // errore di compilazione: totale non è "effectively final"

int somma = catalogo.stream().mapToInt(Libro::anno).sum();   // soluzione 1: l'operazione giusta dello stream

AtomicInteger contatore = new AtomicInteger();               // soluzione 2: la variabile non cambia, cambia l'oggetto
catalogo.forEach(l -> contatore.addAndGet(l.anno()));
```
- La lambda lavora su una **copia** del valore: la variabile potrebbe non esistere più quando la lambda viene eseguita, magari su un altro thread
- Per questo deve essere *effectively final*: assegnata una volta sola
- Quasi sempre la soluzione è un'operazione dello stream (`sum`, `count`, `collect`)
- `AtomicInteger` funziona perché la *variabile* non cambia (cambia l'oggetto a cui punta); si usa solo se non c'è un'operazione migliore

Cosa mostra: il confronto con lo `switch` e il caso in cui serve una classe.

```java
// ❌ Con lo switch: ogni regola nuova obbliga a modificare questo metodo
public double multa(String nomeRegola, long giorni) {
    return switch (nomeRegola) {
        case "fissa" -> 5.0;
        case "giornaliera" -> giorni * 0.50;
        case "tetto" -> Math.min(giorni * 0.50, 10.0);
        default -> throw new IllegalArgumentException("Regola sconosciuta: " + nomeRegola);
    };
}

// ✅ Con la strategia: una regola nuova è una voce in più, CalcolatoreMulte non cambia
Map<String, RegolaMulta> regole = Map.of(
    "fissa", giorni -> 5.0,
    "giornaliera", giorni -> giorni * 0.50,
    "tetto", giorni -> Math.min(giorni * 0.50, 10.0),
    "festivo", giorni -> giorni * 0.25);                      // regola nuova

// Quando serve una CLASSE: la regola ha una dipendenza da ricevere dall'esterno
public class MultaDaTabella implements RegolaMulta {
    private final TabellaTariffe tabella;                     // dipendenza (schema: la classe non è definita qui)

    public MultaDaTabella(TabellaTariffe tabella) {
        this.tabella = tabella;
    }

    @Override
    public double calcola(long giorni) {
        return tabella.importoPer(giorni);
    }
}
```
- Con lo `switch` il codice che calcola la multa va riaperto e ritestato a ogni regola nuova
- Con la strategia ogni regola è indipendente e si prova da sola
- Si usa una **classe** quando la regola ha stato, più metodi o dipendenze da ricevere dall'esterno
- In MG4 le strategie di BiblioTech saranno classi gestite da Spring e scelte con `@Qualifier`: la logica è la stessa

### Esercizi

#### Facile
Obiettivo: data una lista di nomi di partecipanti a un corso, ordinarla per lunghezza del nome (e a parità in ordine alfabetico) con `Comparator`, e stamparla con un riferimento a metodo.

#### Medio
Obiettivo: nel dominio di un negozio online, definire `Prodotto(nome, prezzo, inMagazzino)`. Scrivere tre `Predicate<Prodotto>` (disponibile, economico sotto i 50 €, nome che inizia con una lettera data) e combinarli con `and`, `or`, `negate` per ottenere «disponibile ed economico, oppure con un nome che inizia per A».

#### Difficile
Obiettivo: nel dominio di un negozio online, rappresentare le regole di sconto (nessuno, 10%, 5 € fissi, 20% oltre i 100 €) come `UnaryOperator<Double>` raccolti in una `Map` indicizzata dal codice coupon. Scrivere il metodo `prezzoFinale(prezzo, codice)` che applica lo sconto scelto e, quando il codice non esiste, non applica nulla. Poi permettere di *combinare* due coupon con `andThen`.

---

## Tema 2 — Stream API

### Spiegazione teorica

- **Stream**: una sequenza di elementi su cui si descrive una **pipeline**: sorgente → operazioni intermedie → operazione terminale
- Non modifica la collezione di partenza e **si usa una volta sola**
- È **lazy**: finché non c'è l'operazione terminale non succede niente
- **Intermedie** (restituiscono uno stream): `filter`, `map`, `sorted`, `distinct`, `limit`
- **Terminali** (producono un risultato): `toList()`, `collect(...)`, `count()`, `forEach`, `anyMatch`, `allMatch`, `findFirst`, `min`, `max`, `reduce`
- **`Collectors`**: `toSet`, `groupingBy`, `counting`, `mapping`, `joining`, `summingInt`, `summingDouble`, `averagingInt`, `averagingDouble`
- **Stream di numeri** (`IntStream`) con `mapToInt`: `sum`, `average`, `max`
- Stile **dichiarativo**: si descrive *che cosa* si vuole, non *come* scorrere
- Le lambda di un stream non devono modificare variabili esterne (niente effetti collaterali)
- `flatMap` appiattisce uno stream di liste in uno stream di elementi (cenno)
- `parallelStream()` esiste, ma non si usa senza misurare: il parallelismo ha costi e rischi (MG1)

#### Operazioni intermedie e terminali: riepilogo

**Operazioni intermedie** (restituiscono uno stream: si possono concatenare, ma non eseguono nulla da sole)

| Operazione | Cosa fa | Esempio | Cosa restituisce |
|---|---|---|---|
| `filter(Predicate)` | Tiene solo gli elementi che soddisfano la condizione | `.filter(l -> l.anno() >= 2000)` | `Stream<Libro>` (meno elementi) |
| `map(Function)` | Trasforma ogni elemento in un altro valore | `.map(Libro::titolo)` | `Stream<String>` (stesso numero di elementi) |
| `sorted()` / `sorted(Comparator)` | Ordina gli elementi; senza argomento serve che siano `Comparable` | `.sorted(Comparator.comparing(Libro::anno))` | `Stream<Libro>` |
| `distinct()` | Elimina i duplicati (usa `equals`) | `.map(Libro::genere).distinct()` | `Stream<Genere>` |
| `limit(n)` | Si ferma ai primi `n` elementi | `.limit(3)` | `Stream<Libro>` (al massimo `n`) |
| `mapToInt(...)` | Trasforma in numeri interi, per usare `sum` e `average` senza unboxing | `.mapToInt(Libro::anno)` | `IntStream` |
| `flatMap(...)` | Trasforma ogni elemento in uno stream e li unisce in uno solo (cenno) | `.flatMap(e -> e.righe().stream())` | `Stream<...>` appiattito |
| `peek(Consumer)` | Guarda gli elementi che passano, senza cambiarli: serve per il debug | `.peek(System.out::println)` | `Stream<Libro>` (identico) |

**Operazioni terminali** (producono un risultato e fanno partire la pipeline: dopo, lo stream è consumato)

| Operazione | Cosa fa | Esempio | Cosa restituisce |
|---|---|---|---|
| `toList()` | Raccoglie gli elementi in una lista **non modificabile** | `.toList()` | `List<T>` |
| `collect(...)` | Raccoglie con un `Collector`: liste, `Set`, `Map`, testo | `.collect(Collectors.groupingBy(Libro::genere))` | dipende dal collector |
| `count()` | Conta gli elementi | `.filter(...).count()` | `long` |
| `forEach(Consumer)` | Esegue un'azione su ogni elemento (stampare, inviare), non per costruire risultati | `.forEach(System.out::println)` | niente (`void`) |
| `anyMatch` / `allMatch` / `noneMatch` | Verifica se **almeno uno** / **tutti** / **nessuno** soddisfa la condizione | `.anyMatch(l -> l.genere() == Genere.SAGGIO)` | `boolean` |
| `findFirst()` | Restituisce il primo elemento, se c'è | `.filter(p -> p.inRitardo(oggi)).findFirst()` | `Optional<T>` |
| `min(Comparator)` / `max(Comparator)` | Restituisce l'elemento più piccolo / più grande | `.max(Comparator.comparing(Libro::anno))` | `Optional<T>` |
| `sum()` / `average()` | Somma o media di uno stream di numeri (`IntStream`) | `.mapToInt(Libro::anno).sum()` | `int` / `OptionalDouble` |
| `reduce(inizio, operazione)` | Combina tutti gli elementi in un solo valore | `.map(Libro::anno).reduce(0, Integer::sum)` | `T` (o `Optional<T>` senza valore iniziale) |

- **Come riconoscerle**: se il metodo restituisce uno stream è intermedio, se restituisce un valore (o `void`) è terminale
- **Regola pratica**: una pipeline ha zero o più intermedie e **una sola** terminale, sempre alla fine
- Le operazioni che possono non avere risultato (`findFirst`, `min`, `max`, `average` su uno stream vuoto) restituiscono un `Optional`: lo vedremo nel Tema 3

### Esempi teorici di utilizzo

- **Elenchi per la pagina**: i titoli dei libri recenti, ordinati
- **Statistiche di BiblioTech**: libri per genere, prestiti scaduti, utente più attivo
- **Controlli**: «c'è almeno un libro in ritardo?», «tutti i prestiti hanno una data?»
- **Trasformazioni da entità a DTO** (MG5): `entita.stream().map(mapper::toResponse).toList()`
- **Report**: totale di giorni di prestito, media per utente
- **Quando non usarli**: un ciclo semplice con una sola istruzione, o con logica complicata da debuggare passo per passo

### Esempi di codice

#### Base
Cosa mostra: una pipeline con sorgente, filtro, trasformazione, ordinamento e risultato.

```java
List<String> titoliRecenti = catalogo.stream()           // sorgente: la lista
    .filter(l -> l.anno() >= 2000)                       // intermedia: tiene i libri dal 2000
    .map(Libro::titolo)                                  // intermedia: Libro -> String
    .sorted()                                            // intermedia: ordine alfabetico
    .toList();                                           // terminale: lista immutabile

long fumetti = catalogo.stream()
    .filter(l -> l.genere() == Genere.FUMETTO)
    .count();                                            // terminale: numero di elementi

boolean esisteSaggio = catalogo.stream()
    .anyMatch(l -> l.genere() == Genere.SAGGIO);         // terminale: true/false

System.out.println(titoliRecenti);
System.out.println(fumetti + " fumetti, saggi presenti: " + esisteSaggio);
```

- Si legge dall'alto verso il basso: prendi, filtra, trasforma, ordina, raccogli
- `catalogo` non cambia: lo stream produce un risultato nuovo
- Senza `toList()` (o un altro terminale) la pipeline non esegue nulla
- Per ogni stream va scritta una nuova pipeline: non si riusa

Cosa mostra: l'ordinamento con un criterio e la differenza tra i due modi di ottenere una lista.

```java
catalogo.stream().sorted().toList();                                      // ClassCastException: Libro non è Comparable
catalogo.stream().sorted(Comparator.comparing(Libro::titolo)).toList();   // si passa il criterio

List<Libro> a = catalogo.stream().toList();                               // non modificabile (da Java 16)
List<Libro> b = catalogo.stream().collect(Collectors.toList());           // modificabile (ArrayList)
b.add(rosa);                                                              // ok
a.add(rosa);                                                              // UnsupportedOperationException
```
- `sorted()` senza argomenti funziona su `String` e numeri, che sono `Comparable`; su un record dà `ClassCastException` a runtime, non in compilazione
- Con un `Comparator` si decide il criterio, come con `List.sort`
- `toList()` è la forma da usare oggi; se la lista va modificata dopo, `collect(Collectors.toList())` o `new ArrayList<>(...)`
- Nei tutorial più vecchi si trova solo `collect(Collectors.toList())`

Cosa mostra: la pipeline non esegue nulla senza l'operazione terminale, e lo stream si consuma una volta sola.

```java
Stream<Libro> s = catalogo.stream()
    .filter(l -> l.anno() >= 2000)
    .peek(l -> System.out.println("dopo filter: " + l.titolo()));   // peek: guarda gli elementi che passano

System.out.println("niente ancora");       // peek non ha ancora stampato nulla
long n = s.count();                        // ora la pipeline gira
s.count();                                 // IllegalStateException: stream has already been operated upon or closed
```
- Finché non c'è l'operazione terminale nessuna lambda viene eseguita
- `peek` mostra gli elementi in un punto della pipeline: serve per il debug, non per la logica
- Uno stream si consuma una volta sola: per ripetere si crea un nuovo stream dalla collezione
- Se serve riusare un risultato, lo si salva in una lista

#### Intermedio
Cosa mostra: raggruppamenti con `groupingBy` e i collector più comuni.

```java
// Libri per genere
Map<Genere, List<Libro>> perGenere = catalogo.stream()
    .collect(Collectors.groupingBy(Libro::genere));

// Quanti libri per genere
Map<Genere, Long> quanti = catalogo.stream()
    .collect(Collectors.groupingBy(Libro::genere, Collectors.counting()));      // collector a valle

// Solo i titoli, per genere
Map<Genere, List<String>> titoliPerGenere = catalogo.stream()
    .collect(Collectors.groupingBy(Libro::genere,
             Collectors.mapping(Libro::titolo, Collectors.toList())));

// Titoli in un'unica stringa
String elenco = catalogo.stream()
    .map(Libro::titolo)
    .collect(Collectors.joining(", ", "[", "]"));                               // separatore, prefisso, suffisso

System.out.println(quanti);       // {ROMANZO=1, SAGGIO=1, FUMETTO=1} (l'ordine di una HashMap non è garantito)
System.out.println(elenco);
```

- `groupingBy` costruisce una `Map`: la chiave è il risultato della funzione, il valore è la lista degli elementi
- Il secondo argomento (*downstream collector*) decide che cosa fare dei gruppi: contare, trasformare, sommare
- È la versione dichiarativa del `computeIfAbsent` e del `merge` di MG1
- L'ordine delle chiavi di una `HashMap` non è garantito: per un ordine fisso si usa la versione di `groupingBy` che accetta come secondo argomento una *factory* della mappa (per esempio `TreeMap::new`)

Cosa mostra: costruire un risultato modificando una lista esterna, e la versione corretta.

```java
List<String> titoli = new ArrayList<>();
catalogo.stream().forEach(l -> titoli.add(l.titolo()));              // ❌ la lambda modifica un oggetto esterno

List<String> titoliCorretti = catalogo.stream()
    .map(Libro::titolo)
    .toList();                                                       // ✅ lo stream restituisce il risultato
```
- Le lambda di uno stream devono limitarsi a restituire un valore, senza modificare oggetti esterni
- Con `parallelStream()` più thread aggiungerebbero alla stessa `ArrayList`: è la race condition di MG1
- `map` e `toList()` (o `collect`) esprimono la stessa cosa senza rischi
- `forEach` va bene per azioni come stampare o inviare, non per costruire risultati

Cosa mostra: sommare e fare la media dentro i gruppi.

```java
// Giorni di prestito consentiti, sommati per utente
Map<String, Integer> giorniPerUtente = prestiti.stream()
    .collect(Collectors.groupingBy(Prestito::utente,
             Collectors.summingInt(p -> p.libro().genere().giorniPrestito())));

// Anno medio di pubblicazione per genere
Map<Genere, Double> annoMedio = catalogo.stream()
    .collect(Collectors.groupingBy(Libro::genere, Collectors.averagingInt(Libro::anno)));
```
- `summingInt` e `averagingInt` si usano come collector a valle di `groupingBy`
- Per importi con i decimali esistono `summingDouble` e `averagingDouble`
- La media è sempre un `Double`, anche se gli elementi sono interi
- Il collector a valle può essere uno qualsiasi: `counting`, `mapping`, `summingInt`…

#### Avanzato
Cosa mostra: analisi dei prestiti di BiblioTech con più operazioni, `mapToInt` e `reduce`.

```java
LocalDate oggi = LocalDate.of(2027, 1, 20);

// Prestiti ancora attivi e scaduti
List<Prestito> scaduti = prestiti.stream()
    .filter(Prestito::attivo)                            // riferimento a metodo
    .filter(p -> p.inRitardo(oggi))
    .toList();

// Utente più attivo: raggruppo e conto, poi cerco il massimo
Optional<Map.Entry<String, Long>> piuAttivo = prestiti.stream()
    .collect(Collectors.groupingBy(Prestito::utente, Collectors.counting()))
    .entrySet().stream()                                 // nuovo stream sulle coppie utente-numero
    .max(Map.Entry.comparingByValue());                  // il massimo restituisce un Optional (Tema 3)

// Stream di numeri: somma e media dei giorni di prestito consentiti
int totaleGiorni = prestiti.stream()
    .mapToInt(p -> p.libro().genere().giorniPrestito())  // IntStream
    .sum();
double media = prestiti.stream()
    .mapToInt(p -> p.libro().genere().giorniPrestito())
    .average()
    .orElse(0);                                          // average restituisce un OptionalDouble

// Se manca il tempo si salta: reduce
int sommaAnni = catalogo.stream()
    .map(Libro::anno)
    .reduce(0, Integer::sum);                            // valore iniziale + come combinare due elementi
```

- Più `filter` consecutivi sono equivalenti a uno solo con `&&`: si scrive come è più leggibile
- Per «il massimo» di un conteggio si fa in due fasi: prima `groupingBy` e `counting`, poi `max` sulle coppie
- `mapToInt` produce un `IntStream`, che ha `sum` e `average` senza dover fare unboxing
- `max`, `min` e `average` possono non avere un risultato (stream vuoto): per questo restituiscono un `Optional`

Cosa mostra: l'errore del compilatore su una lambda che legge un file, e la soluzione.

```java
List<String> righe = percorsi.stream()
    .map(p -> Files.readString(p))            // errore di compilazione: IOException non gestita
    .toList();

List<String> righeCorrette = percorsi.stream()
    .map(p -> {
        try {
            return Files.readString(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);   // la incapsulo in una eccezione unchecked, con la causa
        }
    })
    .toList();
```
- `Function`, `Consumer` e le altre interfacce standard non dichiarano eccezioni checked (MG1): dentro la lambda vanno gestite
- Si cattura e si rilancia una eccezione unchecked, conservando la causa
- `UncheckedIOException` è quella standard per `IOException`
- Se il blocco diventa lungo, meglio un metodo privato che gestisce l'eccezione e un riferimento a metodo che lo chiama

### Esercizi

#### Facile
Obiettivo: data una lista di voti d'esame (numeri interi), ottenere con uno stream i voti sufficienti (almeno 18) in ordine decrescente, e calcolarne la media. Il risultato deve essere vuoto o 0 se non ci sono voti sufficienti, senza errori.

#### Medio
Obiettivo: nel dominio di un istituto di formazione, definire `Studente(nome, corso, voto)`. Con gli stream ottenere: la media dei voti per corso (`averagingInt`), il numero di studenti per corso e l'elenco dei nomi degli studenti con voto sopra la media generale.

#### Difficile
Obiettivo: nel dominio di un e-commerce, definire `Ordine(cliente, categoria, importo)`. Ottenere: il totale speso da ciascun cliente, i 3 migliori clienti in ordine decrescente di spesa, e la categoria con l'incasso maggiore. Poi riscrivere le tre estrazioni con un ciclo `for` e confrontare leggibilità e lunghezza.

---

## Tema 3 — `Optional`

### Spiegazione teorica

- **`Optional<T>`**: un contenitore che contiene un valore oppure è vuoto
- Nel tipo di ritorno dice chiaramente: «questo metodo può non trovare nulla»
- Serve a evitare le `NullPointerException` quando un risultato può mancare
- **Creazione**: `Optional.of(x)` (x non può essere `null`), `Optional.ofNullable(x)` (accetta `null`), `Optional.empty()`
- **Uso**: `isPresent()`, `isEmpty()`, `ifPresent(...)`, `ifPresentOrElse(...)`
- **Valore di ripiego**: `orElse(...)`, `orElseGet(...)` (calcolato solo se serve), `orElseThrow(...)` (lancia un'eccezione)
- **Trasformazioni**: `map`, `filter`; `flatMap` quando la funzione restituisce già un `Optional`
- `get()` senza controllo lancia `NoSuchElementException`: evitarlo
- **Dove usarlo**: tipo di ritorno di un metodo. **Dove non usarlo**: campi, parametri, elementi di collezioni (per «nessun elemento» si usa la lista vuota)
- **Perché non nei parametri**: ci sono due modi per dire «niente» (`null` e `Optional.empty()`) e il chiamante può comunque passare `null`; meglio due metodi
- **Perché non nei campi**: `Optional` non è `Serializable` e JPA non lo accetta come tipo di un attributo di entità (MG6); si tiene il campo normale e il getter può restituire un `Optional`
- Un metodo che restituisce un `Optional` non deve **mai** restituire `null` al posto dell'`Optional`
- `Map.get` restituisce ancora `null` (API precedente a `Optional`): si scrive `Optional.ofNullable(mappa.get(chiave))`
- Nel corso: `findFirst`, `max`, `min` degli stream e `findById` di Spring Data (MG6) restituiscono un `Optional`

### Esempi teorici di utilizzo

- **Ricerca per chiave**: libro per ISBN, utente per username
- **Spring Data**: `repository.findById(id)` restituisce un `Optional`
- **404 coerenti nelle API** (MG5): `orElseThrow(() -> new LibroNotFoundException(id))`
- **Valore facoltativo**: l'email di un utente, che può non esserci
- **Azione condizionata**: inviare un sollecito solo se esiste un prestito scaduto
- **Da evitare**: un `Optional` come campo di un'entità o come parametro di un metodo, e `Optional<List<…>>`

### Esempi di codice

#### Base
Cosa mostra: un metodo di ricerca che restituisce un `Optional` e i modi base di consumarlo.

```java
public Optional<Libro> cercaPerIsbn(String isbn) {
    return Optional.ofNullable(perIsbn.get(isbn));       // da null a Optional vuoto
}

Optional<Libro> trovato = cercaPerIsbn("978-88-0000-002-8");
Optional<Libro> assente = cercaPerIsbn("000");

System.out.println(trovato.isPresent());                 // true
System.out.println(assente.isEmpty());                   // true

trovato.ifPresent(l -> System.out.println("Trovato: " + l.titolo()));   // eseguita solo se c'è il valore

String titolo = assente
    .map(Libro::titolo)                                  // trasforma solo se presente
    .orElse("Titolo sconosciuto");                       // valore di ripiego
System.out.println(titolo);
```

- Il chiamante *deve* decidere cosa fare quando manca il valore: il tipo lo ricorda
- `ofNullable` è il ponte tra il vecchio codice che restituisce `null` e il nuovo
- `map` su un `Optional` vuoto restituisce un `Optional` vuoto: nessuna `NullPointerException`
- Non si scrive `if (opt.isPresent()) { opt.get() }`: si usano `map`, `ifPresent` e `orElse`

#### Intermedio
Cosa mostra: `orElseThrow` per i casi in cui il valore è obbligatorio, e una catena `filter`/`map`/`orElse`.

```java
public Libro trova(String isbn) {
    return cercaPerIsbn(isbn)
        .orElseThrow(() -> new NoSuchElementException("Libro inesistente: " + isbn));   // eccezione di dominio
}

String descrizione = cercaPerIsbn("978-88-0000-002-8")
    .filter(l -> l.anno() >= 2000)                                       // diventa vuoto se non recente
    .map(l -> l.titolo() + " (" + l.genere().etichetta() + ")")
    .orElse("Nessun libro recente con questo ISBN");

System.out.println(descrizione);                                         // Sapiens (Saggio)

try {
    trova("000");                                                        // NoSuchElementException
} catch (NoSuchElementException e) {
    System.out.println(e.getMessage());
}
```

- `orElseThrow` accetta una lambda che crea l'eccezione: si crea solo se serve
- È il modo normale di tradurre «non trovato» in un errore: in MG5 diventerà una risposta 404
- Una catena di `filter` e `map` si legge come una pipeline di stream, con al massimo un elemento
- `get()` e `orElseThrow()` senza argomenti fanno la stessa cosa; è meglio usare quella con l'eccezione esplicita

Cosa mostra: `map` che produce un `Optional` dentro un `Optional`, e `flatMap` che lo risolve.

```java
public class Utente {
    private final String username;
    private final String email;                  // può essere null: campo normale

    public Utente(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public Optional<String> email() {            // il getter restituisce un Optional
        return Optional.ofNullable(email);
    }
}

Optional<Optional<String>> doppio = trova("mario").map(Utente::email);     // due livelli: scomodo
Optional<String> email = trova("mario").flatMap(Utente::email);            // un solo livello
```
- `map` avvolge sempre il risultato in un `Optional`: se la funzione restituisce già un `Optional` se ne ottengono due, uno dentro l'altro
- `flatMap` toglie un livello: o c'è il valore, o l'`Optional` è vuoto
- Le catene `flatMap`/`map`/`orElse` sostituiscono una cascata di controlli `if (x != null)`
- Stessa idea di `Stream.flatMap` (cenno nel Tema 2)

#### Avanzato
Cosa mostra: `Optional` che arrivano dagli stream, `ifPresentOrElse` e la differenza tra `orElse` e `orElseGet`.

```java
LocalDate oggi = LocalDate.of(2027, 1, 20);

// findFirst restituisce un Optional
Optional<Prestito> primoScaduto = prestiti.stream()
    .filter(p -> p.inRitardo(oggi))
    .findFirst();

primoScaduto.ifPresentOrElse(
    p -> System.out.println("Sollecito a " + p.utente()),              // se presente
    () -> System.out.println("Nessun prestito scaduto"));              // se vuoto

// orElse e orElseGet: quando viene eseguito il valore di ripiego?
static Libro creaLibroPredefinito() {
    System.out.println("Creo il libro predefinito...");               // effetto visibile
    return new Libro("000", "Sconosciuto", 2000, Genere.SAGGIO);
}

Libro l1 = cercaPerIsbn("978-88-0000-002-8")
    .orElse(creaLibroPredefinito());                                  // il metodo viene chiamato SEMPRE

Libro l2 = cercaPerIsbn("978-88-0000-002-8")
    .orElseGet(() -> creaLibroPredefinito());                         // chiamato solo se l'Optional è vuoto
```

- `ifPresentOrElse` copre i due casi senza un `if`
- `orElse(x)` calcola `x` prima di sapere se serve: la scritta «Creo il libro…» compare anche quando il libro esiste
- `orElseGet` riceve una lambda ed è la scelta giusta quando il ripiego costa (database, creazione di oggetti)
- Con un valore già pronto (una costante) `orElse` va bene

### Esercizi

#### Facile
Obiettivo: nel dominio di una rubrica telefonica (`Map<String, String>` nome → telefono), scrivere `Optional<String> telefonoDi(String nome)` e usarlo per stampare «Telefono: …» oppure «Contatto non presente», senza usare `if` e senza `get()`.

#### Medio
Obiettivo: nel dominio di un sistema di utenti, scrivere `Optional<Utente> cerca(String username)` e un metodo che restituisce il dominio dell'email dell'utente in maiuscolo, oppure «SENZA EMAIL». Poi un secondo metodo che lancia una eccezione di dominio con un messaggio chiaro quando l'utente non esiste.

#### Difficile
Obiettivo: nel dominio aziendale, definire `Impiegato(nome, reparto, stipendio, responsabile)` dove `responsabile` può mancare (il costruttore o un metodo restituisce un `Optional<Impiegato>`). Trovare con uno stream l'impiegato più pagato di un reparto e ottenere il nome del suo responsabile, oppure «nessun responsabile», concatenando `flatMap` e `map` senza `if` né `get()`.
