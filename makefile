CC=javac

main: HLFClienteTCP.class HLFServidorIterativo.class ProcesadorHLF.class

HLFClienteTCP.class: HLFClienteTCP.java
	$(CC) HLFClienteTCP.java

HLFServidorIterativo.class: HLFServidorIterativo.java
	$(CC) HLFServidorIterativo.java

ProcesadorHLF.class: ProcesadorHLF.java
	$(CC) ProcesadorHLF.java

clean:
	rm -rf *.class
