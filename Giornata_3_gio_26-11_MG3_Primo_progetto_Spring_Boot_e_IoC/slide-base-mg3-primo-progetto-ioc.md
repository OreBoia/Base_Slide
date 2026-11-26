# Primo progetto Spring Boot e Inversion of Control (Base · MG3)

Corso Java e Spring Boot Base (EXP_DATLAS_024) · Java 25 LTS · Spring Boot 4.1.2 (Spring Framework 7) · Maven · Progetto fil rouge: *BiblioTech*

Tre temi, ciascuno con i 4 blocchi: Spring Boot, Initializr e starter; struttura del progetto, configurazione e avvio; IoC, Dependency Injection e Dependency Inversion. Principio della mezza giornata: **Dependency Inversion** (la «D» di SOLID), con l'IoC come pattern.

Collegamento con MG1: lì `Biblioteca` riceveva un `Notificatore` dal costruttore e lo passavamo noi a mano; da oggi lo fa Spring.

---

## Tema 1 — Spring Boot, Initializr, Maven e starter

### Spiegazione teorica

- **Spring Framework**: il nucleo (IoC, DI, MVC, transazioni); **Spring Boot**: lo avvia e lo configura al posto tuo
- Nato nel 2014 per eliminare la configurazione manuale: *«just run»*. Non sostituisce Spring, lo usa

| | Spring Framework | Spring Boot |
| --- | --- | --- |
| Configurazione | Manuale (XML o classi Java) | **Automatica** (auto-configuration) |
| Server web | Esterno (deploy di un WAR) | **Embedded** (Tomcat dentro il JAR) |
| Dipendenze | Una per una, con le versioni giuste | **Starter** con versioni già compatibili |
| Avvio | Configurazione del server + deploy | Un normale `main()` |

- **Convention over Configuration**: valori di default sensati, si configura solo ciò che si vuole cambiare
- **Auto-configuration**: Spring Boot guarda le librerie presenti e si configura di conseguenza (c'è Spring MVC → avvia Tomcat; c'è JPA → configura Hibernate e il DataSource)
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`
- **Spring Initializr** (start.spring.io, o *New → Spring Starter Project* nell'IDE) genera lo scheletro del progetto
- Scelte del corso: **Maven**, Java, Spring Boot **4.1.2**, **Java 25**, packaging **Jar**
- **Maven** gestisce dipendenze, compilazione, test e packaging; il `pom.xml` è il «manifesto» del progetto:
  - **parent** `spring-boot-starter-parent`: eredita le versioni compatibili di tutte le librerie
  - **coordinate** `groupId`, `artifactId`, `version`
  - **dependencies**: gli starter
  - **build**: `spring-boot-maven-plugin` crea il JAR eseguibile
- **Maven Wrapper** (`mvnw`): stessa versione di Maven per tutto il team, senza installarla
- Uno **starter** è una dipendenza «pacchetto» con tutto il necessario per una funzionalità; si scrive **senza versione** (la decide il parent)

| Starter | Cosa porta |
| --- | --- |
| `spring-boot-starter-webmvc` | Spring MVC, Tomcat embedded, Jackson (JSON) |
| `spring-boot-starter-data-jpa` | Hibernate, Spring Data JPA |
| `spring-boot-starter-validation` | Bean Validation |
| `spring-boot-starter-security` | Spring Security |
| `spring-boot-starter-actuator` | Stato e metriche dell'applicazione |
| `spring-boot-devtools` | Riavvio automatico in sviluppo |

- In Spring Boot 4 lo starter web si chiama `spring-boot-starter-webmvc` e ogni tecnologia ha il suo starter di test (es. `spring-boot-starter-webmvc-test`)
- **Dipendenze transitive**: uno starter porta altre librerie, che ne portano altre (`./mvnw dependency:tree`)
- **Scope**: quando serve una dipendenza (sempre, solo a runtime, solo nei test)

### Esempi teorici di utilizzo

- **Gestionale aziendale** (ordini, magazzino, clienti): Spring è lo standard di fatto in molte aziende che sviluppano in Java
- **API per un front-end separato**: Spring Boot espone servizi REST in pochi minuti (è il percorso di questo corso)
- **Microservizi**: ogni servizio è un piccolo progetto Spring Boot con il suo JAR, avviabile in un container
- **Nuovo collega nel team**: clona il progetto e lo avvia con `./mvnw spring-boot:run`, senza installare Maven né configurare un server
- **Aggiungere una funzionalità** (sicurezza, database, validazione): si aggiunge uno starter e Spring Boot si configura da solo
- **Aggiornare le versioni**: si cambia il parent e tutte le librerie restano compatibili tra loro

### Esempi di codice

#### Base
Cosa mostra: la classe principale di un'applicazione Spring Boot e cosa succede all'avvio.

```java
package it.corso.biblioteca;                              // package RADICE del progetto

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication                                    // configurazione + auto-config + scansione componenti
public class BibliotecaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApplication.class, args);   // avvia il container e il server embedded
    }
}
```

```
 :: Spring Boot ::                (v4.1.2)
Starting BibliotecaApplication using Java 25 ...
Tomcat started on port 8080 (http) with context path '/'
Started BibliotecaApplication in 1.9 seconds
```

- Un normale `main()`: nessun server da installare, nessun file di deploy
- Nel log si leggono la versione di Spring Boot, la versione di Java e la porta di Tomcat
- Aprendo `http://localhost:8080` compare la **Whitelabel Error Page**: l'app funziona, ma non ci sono ancora pagine
- La classe principale sta nel package radice: da qui Spring cercherà tutto il resto

#### Intermedio
Cosa mostra: il `pom.xml` generato da Initializr con Java 25 e gli starter di base.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>      <!-- versioni compatibili di tutte le librerie -->
    <version>4.1.2</version>
</parent>

<groupId>it.corso</groupId>                                  <!-- organizzazione -->
<artifactId>biblioteca</artifactId>                          <!-- nome del progetto -->
<version>0.0.1-SNAPSHOT</version>

<properties>
    <java.version>25</java.version>                          <!-- JDK usato dal corso -->
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>  <!-- web: Spring MVC + Tomcat + Jackson -->
    </dependency>                                            <!-- nessuna <version>: la decide il parent -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>                               <!-- solo in esecuzione, non in compilazione -->
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>                                  <!-- solo per i test -->
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>   <!-- crea il JAR eseguibile -->
        </plugin>
    </plugins>
</build>
```

```bash
./mvnw dependency:tree      # mostra le dipendenze transitive (Tomcat, Jackson, Spring MVC...)
```

- Tre starter portano decine di librerie, già in versioni compatibili tra loro
- Lo `scope` decide **quando** serve una dipendenza: sempre, solo a runtime, solo nei test
- Per cambiare la versione di Spring Boot si modifica **solo** il parent
- `./mvnw` (con il wrapper) funziona anche su un computer dove Maven non è installato

#### Avanzato
Cosa mostra: aggiungere uno starter e osservare cosa cambia all'avvio, con il report di auto-configurazione.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>    <!-- un solo starter in più -->
</dependency>
```

```bash
./mvnw spring-boot:run                                       # avvio normale
curl http://localhost:8080/actuator/health                   # {"status":"UP"}: nuovo endpoint, nessuna riga di codice

java -jar target/biblioteca-0.0.1-SNAPSHOT.jar --debug       # stampa il CONDITIONS EVALUATION REPORT
```

```
============================
CONDITIONS EVALUATION REPORT
============================

Positive matches:          ← auto-configurazioni applicate, con la condizione che le ha attivate
-----------------

Negative matches:          ← auto-configurazioni scartate, con il motivo (per esempio: classe non presente)
-----------------
```

- L'endpoint `/actuator/health` esiste perché lo starter è nel classpath: nessuna riga di codice o di configurazione
- `--debug` mostra, per ogni auto-configurazione, perché è stata applicata o scartata
- Se si aggiunge `spring-boot-starter-data-jpa` senza un database configurato, l'avvio fallisce con un messaggio sul `DataSource`: Spring Boot ha provato a configurare un database che non trova
- Con un database incorporato (come H2, in laboratorio) nel classpath, la stessa auto-configurazione parte da sola

### Esercizi

> Tema degli esercizi: **Gestionale di una palestra** (iscritti, abbonamenti, corsi).

#### Facile
Obiettivo: creare con Initializr (o dall'IDE) un nuovo progetto `palestra` (Maven, Java 25, Spring Boot 4.1.2, packaging Jar) con le dipendenze *Spring Web* e *Spring Boot DevTools*. Avviarlo e individuare nel log la versione di Spring Boot, la versione di Java, la porta e il tempo di avvio. Aprire `http://localhost:8080` e spiegare perché compare la Whitelabel Error Page.

#### Medio
Obiettivo: nel `pom.xml` individuare il parent, la versione di Java e gli starter, e spiegare a cosa serve ciascuno `scope`. Eseguire `./mvnw dependency:tree` e trovare quale versione di Tomcat è arrivata come dipendenza transitiva. Creare il JAR con `./mvnw clean package` e avviarlo da terminale con `java -jar`, passando `--server.port=9090` per cambiare la porta.

#### Difficile
Obiettivo: aggiungere allo stesso progetto lo starter *Actuator* e verificare che `/actuator/health` risponde. Avviare con `--debug` e trovare nel report una auto-configurazione applicata e una scartata, spiegando il motivo di ciascuna. Poi aggiungere `spring-boot-starter-data-jpa` **senza** database e leggere l'errore, infine aggiungere H2 e osservare cosa cambia.

---

## Tema 2 — Struttura del progetto, configurazione e avvio

### Spiegazione teorica

- Struttura di un progetto Spring Boot (Maven):

```
src/main/java/it/corso/biblioteca/
├── BibliotecaApplication.java   ← @SpringBootApplication nel package RADICE
├── controller/                  ← riceve le richieste HTTP
├── service/                     ← logica di business
├── repository/                  ← accesso ai dati
├── model/                       ← entità del dominio
├── dto/                         ← oggetti scambiati con l'esterno
└── config/                      ← classi di configurazione
src/main/resources/
├── application.properties       ← configurazione
└── static/                      ← CSS, JavaScript, immagini
src/test/java/                   ← test
```

- ⚠️ La classe principale va nel **package radice**: Spring cerca i componenti solo in quel package e nei suoi sotto-package
- **Per layer** (`controller/`, `service/`…): semplice, ideale per iniziare (usato nel corso)
- **Per funzionalità** (`libri/`, `autori/`, `prestiti/`): scala meglio nei progetti grandi
- **`application.properties`**: coppie `chiave=valore` lette all'avvio (formato alternativo: `application.yml`)
- Proprietà standard: `server.port`, `spring.application.name`, `logging.level.*`
- **Tre modi di avviare** l'applicazione: dall'IDE, `./mvnw spring-boot:run`, JAR eseguibile (`java -jar`)
- Il **fat JAR** contiene codice, librerie e server: gira su qualsiasi computer con Java 25
- Un **controller** è una classe con metodi collegati a un URL (`@RestController`, `@GetMapping`): al centro c'è il `DispatcherServlet`, che riceve tutte le richieste e le smista (dettagli in MG5)
- **Precedenza** della configurazione (vince il più in alto): argomento `--server.port=9090`, variabile d'ambiente `SERVER_PORT=9090`, `application.properties`

### Esempi teorici di utilizzo

- **Controllo di salute** (`/ping`): un endpoint minimo che un sistema di monitoraggio chiama per sapere se l'applicazione è viva
- **Porta diversa per ambiente**: 8080 in produzione, 8081 sul computer dello sviluppatore, senza ricompilare
- **Un team con più sviluppatori**: i package per livello mostrano subito dove mettere ogni classe
- **Progetti grandi**: i package per funzionalità tengono insieme controller, service e repository della stessa area
- **Consegna dell'applicazione**: un unico JAR da copiare su un server e avviare
- **Valori di configurazione modificabili senza ricompilare**: nome dell'applicazione, livello di log, porta

### Esempi di codice

#### Base
Cosa mostra: il primo endpoint dell'applicazione, `GET /ping`.

```java
package it.corso.biblioteca.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController                                  // classe che risponde a richieste HTTP
public class PingController {

    @GetMapping("/ping")                         // GET http://localhost:8080/ping
    public String ping() {
        return "pong";                           // il testo restituito è la risposta
    }
}
```

```bash
curl http://localhost:8080/ping       # pong
```

- Il controller sta sotto `it.corso.biblioteca`: per questo Spring lo trova
- Nessun `web.xml`, nessun deploy: basta una classe con due annotazioni
- Il valore restituito diventa il corpo della risposta HTTP
- Il tipo restituito può essere un oggetto: in MG5 diventerà JSON

#### Intermedio
Cosa mostra: `application.properties` e un valore letto nel codice con `@Value`.

```properties
# src/main/resources/application.properties
spring.application.name=biblioteca
server.port=8080
logging.level.it.corso=DEBUG
biblioteca.nome=BiblioTech
```

```java
@RestController
public class PingController {

    private final String nomeApplicazione;
    private final String nomeBiblioteca;

    public PingController(@Value("${spring.application.name}") String nomeApplicazione,
                          @Value("${biblioteca.nome:BiblioTech}") String nomeBiblioteca) {   // valore di riserva
        this.nomeApplicazione = nomeApplicazione;
        this.nomeBiblioteca = nomeBiblioteca;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong da " + nomeApplicazione + " (" + nomeBiblioteca + ")";
    }
}
```

- `@Value("${chiave}")` legge una proprietà; `${chiave:valore}` indica il valore di riserva se manca
- `spring.application.name` e `server.port` sono proprietà standard; `biblioteca.nome` l'abbiamo inventata noi
- Cambiando il file, e non il codice Java, cambia il comportamento
- Le proprietà personalizzate in gruppo (`@ConfigurationProperties`) arrivano in MG4

#### Avanzato
Cosa mostra: i tre modi di avviare, la precedenza della configurazione e cosa succede fuori dal package radice.

```bash
# 1. Dall'IDE: Run As → Spring Boot App (oppure dalla Boot Dashboard)

# 2. Da terminale, con il wrapper
./mvnw spring-boot:run

# 3. Come JAR eseguibile
./mvnw clean package
java -jar target/biblioteca-0.0.1-SNAPSHOT.jar

# Precedenza: l'argomento batte la variabile d'ambiente, che batte il file
SERVER_PORT=8082 java -jar target/biblioteca-0.0.1-SNAPSHOT.jar                         # 8082
SERVER_PORT=8082 java -jar target/biblioteca-0.0.1-SNAPSHOT.jar --server.port=9090      # 9090
```

```java
// ❌ Un controller FUORI dal package radice (it.corso.altro, non it.corso.biblioteca)
package it.corso.altro;

@RestController
public class PingFuoriPackage {
    @GetMapping("/ping")
    public String ping() { return "pong"; }
}
// L'applicazione parte senza errori, ma GET /ping risponde 404: Spring non ha mai visto la classe
```

- Per ogni modo di avvio il risultato è lo stesso: cambia solo chi lancia il comando
- L'argomento da riga di comando vince su tutto: è il modo più rapido per cambiare un valore una volta sola
- Una classe fuori dal package radice non è un bean: se è un controller si ottiene un 404, se è una dipendenza un errore all'avvio
- Soluzione: spostare la classe sotto il package radice (oppure `scanBasePackages`, da usare di rado)

```
// Avvio una seconda copia mentre la prima è ancora in esecuzione

Web server failed to start. Port 8080 was already in use.

Action:
Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
```

- Causa più comune: un'altra copia dell'app è ancora in esecuzione, per esempio un avvio precedente dall'IDE mai fermato
- Soluzione 1: fermare l'altra istanza (quadratino rosso nella console dell'IDE, `Ctrl+C` nel terminale)
- Soluzione 2: avviare questa su un'altra porta, con `--server.port=8081`
- Se la porta è occupata da un altro programma e non da una copia dell'app, si cambia porta

```powershell
# PowerShell
.\mvnw spring-boot:run
$env:SERVER_PORT=8082; java -jar target\biblioteca-0.0.1-SNAPSHOT.jar
curl.exe http://localhost:8080/ping       # "curl" da solo, in PowerShell 5.1, è un alias di Invoke-WebRequest

# cmd
mvnw.cmd spring-boot:run
set "SERVER_PORT=8082" && java -jar target\biblioteca-0.0.1-SNAPSHOT.jar
```

- Nelle slide i comandi sono in sintassi bash: su Windows cambiano il wrapper (`mvnw.cmd` o `.\mvnw`), le variabili d'ambiente e `curl`
- In PowerShell la variabile resta impostata finché non si chiude il terminale (in bash il prefisso vale per quel solo comando): si toglie con `Remove-Item Env:SERVER_PORT`
- In `cmd` le virgolette in `set "..."` evitano uno spazio finale nel valore della variabile

### Esercizi

#### Facile
Obiettivo: nel progetto `palestra` creare un controller con `GET /api/orari` che restituisce una stringa con gli orari di apertura. Verificarlo con `curl` o dal browser.

#### Medio
Obiettivo: leggere dal file `application.properties` il nome della palestra (`palestra.nome`) e l'orario di apertura (`palestra.apertura`) e restituirli da un endpoint `GET /api/info`. Aggiungere un valore di riserva per `palestra.apertura`. Poi cambiare `palestra.nome` solo con un argomento `--palestra.nome=...` da riga di comando e verificare il risultato.

#### Difficile
Obiettivo: creare lo stesso progetto in due versioni: prima con i package per layer (`controller`, `service`, `repository`), poi con i package per funzionalità (`iscritti`, `abbonamenti`, `corsi`). Scrivere due righe su pregi e difetti di ciascuna. Poi spostare un controller in un package fuori dal radice, osservare l'effetto e spiegarlo.

---

## Tema 3 — IoC, Dependency Injection e Dependency Inversion

### Spiegazione teorica

- **Il problema**: una classe che crea da sola le sue dipendenze con `new` è **accoppiata** a una implementazione concreta
  - difficile da sostituire (email → SMS)
  - difficile da testare (non si può passare una finta)
- **Inversion of Control (IoC)**: il **controllo** della creazione e del collegamento degli oggetti passa dal tuo codice al **framework** («non chiamarci, ti chiamiamo noi»)
- **Dependency Injection (DI)**: la tecnica con cui Spring applica l'IoC: le dipendenze vengono **passate dall'esterno**, di solito nel costruttore
- Il **container** (`ApplicationContext`) crea gli oggetti, i *bean*, li collega e li tiene pronti per l'uso
- Un **bean** è un normale oggetto Java, creato e gestito da Spring; per ora basta `@Component` (stereotipi, ciclo di vita e scope in MG4)
- **Non tutto è un bean**: Spring gestisce i *collaboratori*, non i dati

| Diventa bean (`@Component` e simili) | Si crea con `new` |
| --- | --- |
| `PrestitoService`, `EmailNotificaService` | `Libro`, `Prestito` (oggetti del dominio) |
| `PrestitoController` | DTO e record che trasportano dati |
| I repository (MG6) | Stringhe, liste, date |

- Regola pratica: un oggetto **condiviso**, con **comportamento** e che **dipende da altri oggetti** → bean; un oggetto che **porta dati**, e ne esistono tanti con valori diversi → `new`
- ⚠️ Un oggetto creato con `new PrestitoService(...)` è un normale oggetto Java: **non è nel container**, Spring non lo conosce e non gli inietta nulla

```
Senza IoC                                  Con IoC / DI
PrestitoService                            Spring (container)
  └─ new EmailNotificaService()              ├─ crea EmailNotificaService
     (sceglie e crea da solo)                ├─ crea PrestitoService
                                             └─ passa la notifica a PrestitoService
```

- **Dependency Inversion** (la «D» di SOLID): *i moduli di alto livello non devono dipendere da quelli di basso livello: entrambi dipendono da astrazioni*
  - `PrestitoService` (alto livello) dipende dall'**interfaccia** `NotificaService`
  - `EmailNotificaService` (basso livello) **implementa** quell'interfaccia
- **SOLID**: cinque principi di progettazione; oggi la «D», le altre le incontreremo nel codice delle prossime mezze giornate
- **Tre modi di iniettare**:

| Tipo | Come | `final` | Testabilità | Quando |
| --- | --- | --- | --- | --- |
| **Costruttore** | Parametri del costruttore | ✅ | ✅ Si passa una finta nel test | **Sempre** (consigliato) |
| Setter | `@Autowired` su un setter | ❌ | ⚠️ | Dipendenze opzionali |
| Campo | `@Autowired` sul campo | ❌ | ❌ Serve Spring o la reflection | Da evitare |

- Con **un solo costruttore** `@Autowired` non serve: Spring lo usa da solo
- **Come sceglie Spring**: guarda il **tipo** del parametro (`NotificaService`) e cerca un bean compatibile: va bene qualsiasi classe che implementa l'interfaccia
  - **un** candidato → lo inietta
  - **zero** candidati → l'applicazione non parte
  - **due o più** → l'applicazione non parte: Spring non sceglie al posto tuo
- Iniezione via costruttore = dipendenze **esplicite**, campi **immutabili**, errore **all'avvio** se manca qualcosa (*fail-fast*)

### Esempi teorici di utilizzo

- **Cambiare fornitore** di un servizio (email, SMS, pagamenti) senza toccare le classi che lo usano
- **Testare la logica di business** con una finta al posto di un servizio esterno: nessuna email vera nei test
- **Ambienti diversi**: in sviluppo una notifica sulla console, in produzione una vera (MG4, con i profili)
- **Lavoro in team**: una persona scrive l'interfaccia e un'altra l'implementazione, in parallelo
- **Aggiungere una funzione** (log, controlli) senza modificare il codice che usa il servizio
- **Bug difficile da trovare**: una dipendenza `new` dentro una classe, che nessun test riesce a sostituire

### Esempi di codice

#### Base
Cosa mostra: lo stesso codice prima e dopo l'IoC, nel progetto BiblioTech.

```java
// ❌ SENZA IoC — service/PrestitoService.java
public class PrestitoService {
    private final NotificaService notifiche = new EmailNotificaService();   // sceglie e crea da solo

    public void registraPrestito(String titolo) {
        notifiche.invia("Prestito registrato: " + titolo);
    }
}
```

```java
// ✅ CON IoC — l'implementazione la sceglie e la crea Spring

// service/NotificaService.java
public interface NotificaService {                          // il service dipende da un'astrazione
    void invia(String messaggio);
}

// service/EmailNotificaService.java
@Component                                                  // "Spring, crea e gestisci tu questa classe"
public class EmailNotificaService implements NotificaService {
    @Override
    public void invia(String messaggio) {
        System.out.println("📧 " + messaggio);
    }
}

// service/PrestitoService.java
@Component
public class PrestitoService {
    private final NotificaService notifiche;

    public PrestitoService(NotificaService notifiche) {      // Spring passa l'implementazione disponibile
        this.notifiche = notifiche;
    }

    public void registraPrestito(String titolo) {
        notifiche.invia("Prestito registrato: " + titolo);
    }
}
```

- `PrestitoService` non contiene più `new`: per passare agli SMS basta un'altra implementazione, senza toccarlo
- Il campo è `final`: la dipendenza non cambia dopo la creazione (come i campi di `Biblioteca` in MG1)
- Spring crea per primo `EmailNotificaService`, poi `PrestitoService`: l'ordine lo ricava lui dai costruttori
- `PrestitoService` conosce solo l'interfaccia: è la Dependency Inversion

```java
// ▶ Per vederlo girare: un CommandLineRunner viene eseguito una volta, subito dopo l'avvio
import org.springframework.boot.CommandLineRunner;

// AvvioDemo.java
@Component
public class AvvioDemo implements CommandLineRunner {
    private final PrestitoService prestitoService;

    public AvvioDemo(PrestitoService prestitoService) {     // anche qui: lo passa Spring
        this.prestitoService = prestitoService;
    }

    @Override
    public void run(String... args) {                       // Spring lo chiama a fine avvio
        prestitoService.registraPrestito("Il nome della rosa");
    }
}
```

```
Started BibliotecaApplication in 1.9 seconds
📧 Prestito registrato: Il nome della rosa
```

- Un `CommandLineRunner` è un bean con un metodo `run`, che Spring Boot chiama una volta a fine avvio
- Serve per provare il codice senza HTTP: è il primo modo di vedere il service in azione
- La catena è Runner → Service → `NotificaService`: ognuno riceve ciò che gli serve nel costruttore

#### Intermedio
Cosa mostra: il service iniettato in un controller, e un test senza Spring con una dipendenza finta.

```java
// controller/PrestitoController.java
@RestController
public class PrestitoController {
    private final PrestitoService prestitoService;

    public PrestitoController(PrestitoService prestitoService) {   // Spring passa il bean del service
        this.prestitoService = prestitoService;
    }

    @GetMapping("/api/prestiti/prova")                             // GET /api/prestiti/prova
    public String prova() {
        prestitoService.registraPrestito("Il nome della rosa");
        return "Prestito registrato";                              // nel log compare 📧 Prestito registrato: ...
    }
}
```

```java
// Test "a mano": nessun Spring, nessuna email vera
class NotificaFinta implements NotificaService {
    final List<String> inviati = new ArrayList<>();

    @Override
    public void invia(String messaggio) {
        inviati.add(messaggio);                                    // registro invece di inviare
    }
}

public static void main(String[] args) {
    NotificaFinta finta = new NotificaFinta();
    PrestitoService servizio = new PrestitoService(finta);          // la dipendenza la passo io, nel costruttore
    servizio.registraPrestito("Il nome della rosa");
    System.out.println(finta.inviati);                              // [Prestito registrato: Il nome della rosa]
}
```

- Controller → Service → `NotificaService`: ogni classe riceve ciò che le serve, nessuna crea l'altra
- Il costruttore è un normale costruttore Java: lo si può chiamare anche senza Spring
- Per questo l'iniezione dal costruttore rende il codice **testabile**: nel test si passa una finta
- In MG8 lo stesso schema si userà con JUnit e Mockito

#### Avanzato
Cosa mostra: gli errori tipici dell'iniezione, e il confronto con setter e campo.

```
// Dimenticato @Component su EmailNotificaService: l'applicazione non parte

Description:
Parameter 0 of constructor in it.corso.biblioteca.service.PrestitoService
required a bean of type 'it.corso.biblioteca.service.NotificaService' that could not be found.

Action:
Consider defining a bean of type 'it.corso.biblioteca.service.NotificaService' in your configuration.
```

```
// Aggiunta una seconda implementazione (SmsNotificaService, anch'essa con @Component): l'applicazione non parte
// (output abbreviato)

Description:
Parameter 0 of constructor in it.corso.biblioteca.service.PrestitoService
required a single bean, but 2 were found:
	- emailNotificaService
	- smsNotificaService

Action:
Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans,
or using @Qualifier to identify the bean that should be consumed
```

```java
// Confronto: gli altri due modi di iniettare (da conoscere, non da imitare)

@Component
public class PrestitoServiceSetter {
    private NotificaService notifiche;                           // non final: potrebbe cambiare

    @Autowired                                                   // setter: la dipendenza arriva dopo la creazione
    public void setNotifiche(NotificaService notifiche) {
        this.notifiche = notifiche;
    }
}

@Component
public class PrestitoServiceCampo {
    @Autowired
    private NotificaService notifiche;                           // ❌ campo privato non final: nel test serve Spring
}
```

- L'errore dice quale parametro di quale costruttore non ha trovato un bean: l'applicazione non parte, e questo è un vantaggio (*fail-fast*)
- Con due implementazioni il messaggio elenca i bean trovati e suggerisce `@Primary` o `@Qualifier`: la soluzione è in MG4
- Con l'iniezione sul campo, un test deve avviare Spring o usare la reflection per passare una finta
- Se manca il tempo si mostra solo il confronto setter/campo, senza provarlo

### Esercizi

#### Facile
Obiettivo: nel progetto `palestra` creare l'interfaccia `PromemoriaService`, una implementazione `EmailPromemoriaService` con `@Component` e un `AbbonamentoService` che la riceve nel costruttore, con un metodo che invia un promemoria di scadenza. Per vederlo girare, creare un `CommandLineRunner` (come `AvvioDemo`) che chiama il metodo all'avvio e verificare l'output nella console.

#### Medio
Obiettivo: scrivere, senza Spring, un piccolo programma (o un metodo `main`) che prova `AbbonamentoService` passandogli una implementazione finta di `PromemoriaService` che memorizza i messaggi in una lista. Verificare che il messaggio venga registrato e spiegare perché la stessa prova sarebbe impossibile se il service creasse il servizio con `new`.

#### Difficile
Obiettivo: provocare di proposito tre errori e leggerne il messaggio: togliere `@Component` all'implementazione, aggiungere una seconda implementazione di `PromemoriaService`, riscrivere `AbbonamentoService` con l'iniezione sul campo. Per ciascuno annotare cosa dice il messaggio (o cosa cambia nella testabilità) e come si risolve. Disegnare infine lo schema delle dipendenze del progetto indicando quali classi dipendono da interfacce.
