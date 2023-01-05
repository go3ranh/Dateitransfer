# Vorschläge zur Bearbeitung des Belegs


## Vorgehen

* Weichen Sie von den gegebenen Klassen und Schnittstellen nur mit guter Begründung ab.
* Es ist hilfreich für die Programmierung, vorab ein Zustandsdiagramm des Protokolls anzufertigen.
* Testen Sie zuerst die Übertragung ohne Paketverluste und ohne Verzögerung, wenn dies funktioniert, können Sie mit Paketverlusten testen. 
* Testen Sie zunächst nur die Übertragung einer kurzen Datei mit Testdaten z.B. nur 5 Byte (0 ,1, 2, 3, 4). Wenn dies funktioniert können Sie mit Daten über mehrere Pakete weitermachen. 
* Ein kritischer Punkt ist der Überlauf der Paketnummer über 255. 
* Beim Debuggen mit Paketverlusten kann es sinnvoll sein, den Zufallsgenerator immer mit gleichen Werten zu starten (Seed).
* Die Länge des Datenfeldes kann über die abfragbare UDP-Paketlänge ermittelt werden.

## weitere Hinweise

### Timeouts
* Sinnvoll ist eine gleitende Anpassung des Timeouts an der Übertragungskanal um den Datendurchsatz bei Paketwiederholungen zu erhöhen, Berechnung siehe z.B. TCP-Protokoll

### CRC-Berechnung
* Parameter für CRC32 (IEEE): Polynom 0x04C11DB7, gespiegeltes Polynom, Initialisierung des Registers mit 0xffffffff, Berechnung des Endwerts XOR 0xffffffff  
* Test:  Codierung der ASCII-Folge 123456789  muss die CRC cbf43926  ergeben, siehe  (https://crccalc.com/) .
* verschiedene Implementierungen:  http://introcs.cs.princeton.edu/java/61data/CRC32.java.html
* Java-Klassen: CRC32(), CheckedInputStream()

### Strings
* für Stringhandling in Java siehe z.B. Klassen DataInput.readUTF  bzw. DataInputStream.readUTF
* diese Klassen weichen zwar in drei Punkten vom UTF-8-Standard ab, welche aber für die Belegaufgabe unkritisch seien sollten, siehe:
 http://docs.oracle.com/javase/6/docs/api/java/io/DataInput.html#modified-utf-8  
* Alternative: fileName.getBytes(StandardCharsets.UTF_8) und fileName = new String(byteFileName, StandardCharsets.UTF_8)

### Bitmanipulation in Java
* siehe Vorlesungsfolien "Java und Sockets" oder z.B: http://sys.cs.rice.edu/course/comp314/10/p2/javabits.html

### Zahlendarstellung
* Ob ein Wert in einem int vorzeichenbehaftet oder vorzeichenlos ist, hängt davon ab, wie die Bits interpretiert werden - Java interpretiert Bits als vorzeichenbehaftete Werte. 
* Wenn man ein int hat, welcher als vorzeichenloser Wert interpretiert werden soll (z.B. wenn man ein int aus einem DataInputStream liest, von dem man weiß, dass es einen vorzeichenlosen Wert enthält), kann man folgendermaßen vorgehen:

`int fourBytesIJustRead = someObject.getInt();`  
`long unsignedValue = fourBytesIJustRead & 0xffffffffl;`

### nützliche Java-Klassen
* ByteArrayInputStream, DataInputStream
* Low-Level-Operationen: java.nio.ByteBuffer
* Google Guava: https://github.com/google/guava
