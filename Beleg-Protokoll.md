# Protokoll Dateitransfer

Nachfolgend ist das Protokoll zum Beleg Dateitransfer beschrieben. Implementieren Sie dieses Protokoll exakt. Zur Belegabnahme muss der von ihnen erstellte Client zu einem beliebigen nach diesem Protokoll erstellten Server korrekt eine Datei übertragen können.

## Protokollbeschreibung
* Nutzung des ersten Paketes für die Übertragung von Dateiinformationen
* Der Server erkennt einen neuen Übertragungswunsch durch die Kennung „Start“, die übertragene Sessionnummer wird dann vom Server übernommen
* Der Client wählt eine Sessionnummer per Zufall.
* Übertragungsprotokoll: 
  * Stop-and-Wait-Protokoll oder Go-Back-N
  * Bei Absendung eines Paketes vom Client wird ein Timeout [[1]](#hinweise) gestartet, welcher mit korrekter Bestätigung durch den Empfänger zurückgesetzt wird.
  * Bei Auslösung des Timeouts wird das Paket erneut gesendet. Dies wird maximal 10 mal wiederholt. Danach erfolgt ein Programmabbruch mit einer Fehlermeldung. 
  * Beachten Sie die Vorgehensweise des Protokolls bzgl. verlorener Daten / ACKs etc.
* Network-Byte-Order:  Big-Endian-Format
* Die Länge eines Datagrams sollte auf die MTU des genutzten Netzes begrenzt werden. (mit max. 1400 Byte ist man auf der sicheren Seite)
* CRC32-Polynom (IEEE-Standard): 0x04C11DB7 (genutzt für die Fehlererkennung im Startpaket und in der Gesamtdatei). 


## Paketaufbau

### Startpaket (Client -> Server)
* 16-Bit Sessionnummer (Wahl per Zufallsgenerator)
*  8-Bit Paketnummer (immer 0)
*  5-Byte Kennung „Start“  als ASCII-Zeichen
* 64-Bit Dateilänge (unsigned integer) (für Dateien > 4 GB)
* 16-Bit (unsigned integer) Länge des Dateinamens  (1-255)
* 0-255 Byte Dateiname als String mit Codierung UTF-8 (erlaubte Zeichen: [a-zA-ZäöüßÄÖÜ0-9_\-\.])
* 32-Bit CRC32 über alle Daten des Startpaketes ab der Startkennung (Sessionnummer und Paketnummer werden nicht einbezogen)

### Datenpakete (Client -> Server)
* 16-Bit-Sessionnummer
*  8-Bit Paketnummer, 1. Datenpaket hat die Nr. 1  (SW/GBN: gerechnet wird mod 256 )
* Daten (max. 1470 Byte)

### letztes Datenpaket (Client -> Server)
* 16-Bit Sessionnummer
*  8-Bit Paketnummer
* 32-Bit CRC32 (Berechnung über die Gesamtdatei)

### Bestätigungspakete (Server -> Client)
* 16-Bit-Sessionnummer
*  8-Bit Bestätigungsnummer für das zu bestätigende Paket  (ACK 0 → Paket Nr. 0 bestätigt)  
*  8-Bit Anzahl der maximal vom Client ohne Bestätigung zu sendenden Pakete (GBN: 1-255)
* 32-Bit CRC32 (Berechnung über die Gesamtdatei am Server, wird nur im **letzten Bestätigungspaket** verschickt)

## Hinweise

1. Sinnvoll ist eine gleitende Anpassung des Timeouts an der Übertragungskanal um den Datendurchsatz bei Paketwiederholungen zu erhöhen, Berechnung siehe z.B. TCP-Protokoll
