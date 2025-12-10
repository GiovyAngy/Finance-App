# Finance App

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.4-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue)
![SQLite](https://img.shields.io/badge/SQLite-3.41.2.1-orange)

Desktop-/Webanwendung zur Verwaltung von Einnahmen, Ausgaben und persönlichen Budgets.  

---

## **Systemanforderungen**

- **JDK 17** (empfohlen, kompatibel mit Spring Boot 3.1.4)  
- **Maven 3.8+**  
- IDE: **IntelliJ IDEA** (Community oder Ultimate)  
- Browser zum Testen der Anwendung (Chrome, Firefox, Edge)

---

## **Installation und Einrichtung**

### 1. Projekt klonen
Klonen Sie das Repository oder laden Sie das Projekt als ZIP herunter und entpacken Sie es in Ihrem bevorzugten Ordner:

```bash
git clone -> https://github.com/GiovyAngy/Finance-App.git
```

### 2. IntelliJ konfigurieren
1. IntelliJ IDEA → **File → Open** → Projektordner auswählen.
2. **File → Project Structure → Project**:
   - **Project SDK → JDK 17** auswählen  
   - **Project language level → 17** einstellen
3. Maven konfigurieren:
   - **Settings → Build, Execution, Deployment → Build Tools → Maven → Importing → JDK for importer → JDK 17**  
   - **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE → JDK 17**

### 3. Maven-Abhängigkeiten aktualisieren
- Rechts im IntelliJ-Paneel **Maven → Reimport All** klicken  
- Oder im Terminal in der Projekt-Wurzel:

```bash
mvn clean install
```

### 4. Server-Port konfigurieren (optional)
- Standardport ändern (Standard ist 8080):
  - Datei: `src/main/resources/application.properties`
  - Folgende Zeile hinzufügen:

```
server.port=8081
```

- Die Anwendung ist dann erreichbar unter: `http://localhost:8081`

### 5. Anwendung starten
- Datei `FinanceApplication.java` → Rechtsklick → **Run 'FinanceApplication.main()'**
- Die Konsole zeigt die Spring Boot-Startmeldungen an.

---

## **Funktionen der Anwendung**

1. **Monatliche Einnahmen** eintragen  
2. **Einzelausgaben** eintragen  
3. **Monats- oder Jahresbudget** für ein Ziel festlegen  
4. Daten in **Excel** exportieren/importieren  
5. **Diagramme anzeigen**:
   - X/Y-Diagramm für Budget und Kontostand  
   - Kreisdiagramm für Prozentanteil von Einnahmen/Ausgaben  
   - Aufteilung nach Kategorien

---

## **Datenbank**
- Verwendete Datenbank: **SQLite**  
- DB-Datei: `finance.db` im Projektordner (wird automatisch erstellt)  
- Verbindung wird über Spring Boot JDBC verwaltet

---

## **Wichtige Abhängigkeiten**
- Spring Boot 3.1.4  
- SLF4J 2.0.9  
- Logback 1.4.11  
- SQLite JDBC 3.41.2.1  
- Apache POI 5.2.3 (Excel Import/Export)
- iText 7.2.5 (PDF Import/Export)

---

## **Hinweise**
- Übersetze die Texte der Buttons in den HTML-Dateien, alles andere funktioniert mit den geänderten Variablennamen.  
- Verwende unbedingt **JDK 17**, sonst treten SLF4J/Logback-Fehler auf.

---

## **Support / Kontakt**
Für Probleme oder Fragen bitte ein Issue im Repository öffnen oder den Projektautor [kontaktieren]([https://github.com/GiovyAngy](https://giovyangy.github.io/Lebenlauf/index.html#kontakt)).
