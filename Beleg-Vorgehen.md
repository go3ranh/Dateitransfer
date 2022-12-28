# Vorschläge zur Bearbeitung des Belegs


## Vorgehen

* Weichen Sie von den gegebenen Klassen und Schnittstellen nur mit guter Begründung ab.
* Es ist hilfreich für die Programmierung, vorab ein Zustandsdiagramm des Protokolls anzufertigen.
* Testen Sie zuerst die Übertragung ohne Paketverluste und ohne Verzögerung, wenn dies funktioniert, können Sie mit Paketverlusten testen. 
* Testen Sie zunächst nur die Übertragung einer kurzen Datei mit Testdaten z.B. nur 5 Byte (0 ,1, 2, 3, 4). Wenn dies funktioniert können Sie mit Daten über mehrere Pakete weitermachen. 
* Ein kritischer Punkt ist der Überlauf der Paketnummer über 255. 
* Beim Debuggen mit Paketverlusten kann es sinnvoll sein, den Zufallsgenerator immer mit gleichen Werten zu starten (Seed).


## weitere Hinweise

1. Sinnvoll ist eine gleitende Anpassung des Timeouts an der Übertragungskanal um den Datendurchsatz bei Paketwiederholungen zu erhöhen, Berechnung siehe z.B. TCP-Protokoll

2. In Java ist die Klasse java.nio.ByteBuffer für Low-Level-Operationen gut nutzbar.  
   Außerdem interessant: Google Guava: https://github.com/google/guava

3. Die Länge des Datenfeldes kann über die abfragbare UDP-Paketlänge ermittelt werden.

4. Implementierungsdetails für CRC32: gespiegeltes Polynom, Initialisierung des Registers mit 0xffffffff, Berechnung des Endwerts XOR 0xffffffff  
Test:  Codierung der ASCII-Folge 123456789  muss die CRC cbf43926  ergeben, siehe  (https://crccalc.com/) .
Java-Klassen:  http://introcs.cs.princeton.edu/java/61data/CRC32.java.html

5. Für Stringhandling in Java siehe z.B. Klassen DataInput.readUTF  bzw. DataInputStream.readUTF
Diese Klassen weichen zwar in drei Punkten vom UTF-8-Standard ab, welche aber für die Belegaufgabe unkritisch seien sollten, siehe:
 http://docs.oracle.com/javase/6/docs/api/java/io/DataInput.html#modified-utf-8  
Alternative: fileName.getBytes(StandardCharsets.UTF_8) und fileName = new String(byteFileName, StandardCharsets.UTF_8)


6. Nützlich sind die Javaklassen: ByteArrayInputStream, CheckedInputStream, DataInputStream

7. Für Informationen zu Java-Bitmanipulationen siehe Vorlesungsfolien oder z.B: http://sys.cs.rice.edu/course/comp314/10/p2/javabits.html


8. Whether a value in an int is signed or unsigned depends on how the bits are interpreted - java interprets bits as a signed value (doesn't have unsigned primitives).
If you have an int that you want to interpret as an unsigned value (e.g. you read an int from a DataInputStream that you know contains an unsigned value) then you can do the following trick.  

`int fourBytesIJustRead = someObject.getInt();`  
`long unsignedValue = fourBytesIJustRead & 0xffffffffl;`
