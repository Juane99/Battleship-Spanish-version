//
// ProcesadorHLF
// (CC) Juan Emilio Martinez, 2019
//

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap;


//Creacion de la clase pair

class Pair<U, V>
{
	public final U first;   	
	public final V second;  	

	private Pair(U first, V second)
	{
		this.first = first;
		this.second = second;
	}
	
	public static <U, V> Pair <U, V> of(U a, V b)
	{
		return new Pair<>(a, b);
	}
	
	public U getFirst(){
		return first;
	}
	
	public V getSecond(){
		return second;
	}
}

public class ProcesadorHLF extends Thread{
	// Referencia a un socket para enviar/recibir las peticiones/respuestas
	private Socket cliente1, cliente2;
	// stream de lectura (por aquí se recibe lo que envía el cliente)
	private InputStream inputcliente1, inputcliente2;
	// stream de escritura (por aquí se envía los datos al cliente)
	private OutputStream outputcliente1, outputcliente2;
	//Numero de hebra que ejecuta
	private int num_hebra;
	//Fila y columna para actualizar tablero
	private int fila_cliente1 = 0, fila_cliente2 = 0;
	private int fila_eleccion_cliente1 = 0, fila_eleccion_cliente2 = 0;
	private int columna_cliente1 = 0, columna_cliente2 = 0;
	private int columna_eleccion_cliente1 = 0, columna_eleccion_cliente2 = 0;
	private int eleccion = 0;
	
	//Array de posibilidades
	private ArrayList<Pair> posibilidades = new ArrayList<Pair>();
	Pair<Integer, Integer> p1;
	Pair<Integer, Integer> p2;
	Pair<Integer, Integer> p3;
	Pair<Integer, Integer> p4;
	
	//Inicializar flujos
	private PrintWriter outReadCliente1;
	private PrintWriter outReadCliente2;
				
	private BufferedReader inReadCliente1;
	private BufferedReader inReadCliente2;
	
	//Cuadricula juego
	private int cuadricula = 8;
	private char[][] tablero_jugador1 = new char[cuadricula][cuadricula];
	private char[][] tablero_jugador2 = new char[cuadricula][cuadricula];
	private static ArrayList<Character> naves_restantes1 = new ArrayList<>();
	private static ArrayList<Character> naves_restantes2 = new ArrayList<>();
	private static boolean fin = false;
	private static boolean tocado = false;
	private static boolean hundido = false;
	private static char nave_tocada;
	private static int ganador;
	
	// Para que la respuesta sea siempre diferente, usamos un generador de números aleatorios.
	private Random random;
	private int cliente_inicial;
	
	public ProcesadorHLF(Socket cliente1,Socket cliente2, int n_hebra) {
		this.cliente1=cliente1;
		this.cliente2=cliente2;
		random=new Random();
		num_hebra = n_hebra;		
	}
	
	private void inicializaArrays(){
		
		//Inicializamos el tablero
		for (int i=0; i<this.cuadricula; i++){
			for (int j=0; j<this.cuadricula; j++){
				this.tablero_jugador1[i][j] = '_';
				this.tablero_jugador2[i][j] = '_';
			}
		}
		//Rellenamos los arrays de naves con las que le quedan a los jugadores
		naves_restantes1.add('L');
		naves_restantes2.add('L');
				
		for (int i=0; i<3; i++){
			naves_restantes1.add('B');
			naves_restantes2.add('B');
		}
				
		for (int i=0; i<4; i++){
			naves_restantes1.add('S');
			naves_restantes2.add('S');
		}
				
		for (int i=0; i<5; i++){
			naves_restantes1.add('P');
			naves_restantes2.add('P');
		}
	}
	
	//FLUJOS
	private void inicializaFlujos(){
		
		try{
	
			inputcliente1=cliente1.getInputStream();
			inputcliente2=cliente2.getInputStream();
					
			outputcliente1=cliente1.getOutputStream();
			outputcliente2=cliente2.getOutputStream();
					
			outReadCliente1 = new PrintWriter(outputcliente1,true);
			outReadCliente2 = new PrintWriter(outputcliente2,true);
					
			inReadCliente1 = new BufferedReader(new InputStreamReader(inputcliente1));
			inReadCliente2 = new BufferedReader(new InputStreamReader(inputcliente2));
		
		}catch(IOException e){
			System.err.println("Error al inicializar los flujos");
		}
		
	}
		
	// Aquí es donde se realiza el procesamiento realmente:
	public void run(){
		inicializaArrays();
		inicializaFlujos();
		
		String recibido1,recibido2;
		
		try {
			
			//Le enviamos el tablero a los jugadores
			enviaTablero(1);
			enviaTablero(2);
			
			/*****************LANCHA******************/
			
			fila_cliente1 = Integer.parseInt(inReadCliente1.readLine());
			columna_cliente1 = Integer.parseInt(inReadCliente1.readLine());
					
			System.out.println("Recibida fila: " + fila_cliente1 + " y columna: " + columna_cliente1 + " del cliente 1");
			
			//Actualizamos el tablero1 con los valores de la lancha
			
			tablero_jugador1[fila_cliente1][columna_cliente1] = 'L';
			
			fila_cliente2 = Integer.parseInt(inReadCliente2.readLine());
			columna_cliente2 = Integer.parseInt(inReadCliente2.readLine());
					
			System.out.println("Recibida fila: " + fila_cliente2 + " y columna: " + columna_cliente2 + " del cliente 2");
			
			//Actualizamos el tablero2 con los valores de la lancha
			
			tablero_jugador2[fila_cliente2][columna_cliente2] = 'L';
			
			//Le volvemos a enviar el tablero a los jugadores
			enviaTablero(1);
			enviaTablero(2);
			
			/*****************BUQUE******************/
			
			colocaNave(2,'B');
			enviaTablero(1);
			enviaTablero(2);		
			
			/*****************SUBMARINO******************/
			
			colocaNave(3,'S');
			enviaTablero(1);
			enviaTablero(2);	
			
			/*****************PORTAAVIONES******************/
			
			colocaNave(4,'P');
			enviaTablero(1);
			enviaTablero(2);	
			
			//Eleccion del cliente que empieza
			
			cliente_inicial = random.nextInt(1);
			
			if (cliente_inicial == 0){
				outReadCliente1.println(0);
				outReadCliente2.println(1);
			}
			else{
				outReadCliente2.println(1);
				outReadCliente1.println(0);
			}
			
			//Desarrollo del juego
			
			while (fin == false){
			
				if (cliente_inicial == 0){
					
					outReadCliente1.println(4); //Disparando
					//Empieza cliente 1
					
					enviaTablero(1);
					
					fila_cliente1 = Integer.parseInt(inReadCliente1.readLine());
					columna_cliente1 = Integer.parseInt(inReadCliente1.readLine());
						
					System.out.println("Recibida fila: " + fila_cliente1 + " y columna: " + columna_cliente1 + " del cliente 1");
					
					if (tablero_jugador2[fila_cliente1][columna_cliente1] != '_'){
						tocado = true;
						nave_tocada = tablero_jugador2[fila_cliente1][columna_cliente1];
						outReadCliente1.println(100);
						
						naves_restantes2.remove(naves_restantes2.indexOf(tablero_jugador2[fila_cliente1][columna_cliente1]));
						
						if (!naves_restantes2.contains(tablero_jugador2[fila_cliente1][columna_cliente1])){
							hundido = true;
							outReadCliente1.println(200);
							outReadCliente1.println(tablero_jugador2[fila_cliente1][columna_cliente1]);
						}
						else{
							hundido = false;
							outReadCliente1.println(300);
						}
							
						tablero_jugador2[fila_cliente1][columna_cliente1] = 'X';

					}
					else{
						tocado = false;
						outReadCliente1.println(400);
					}
						
					//Recibe el disparo el cliente 2
						
					outReadCliente2.println(18); //Recibiendo
					
					enviaTablero(2);
					
					if (tocado){
						outReadCliente2.println(121);
						outReadCliente2.println(nave_tocada);
						
						if (hundido)
							outReadCliente2.println(99);
						else
							outReadCliente2.println(98);
					}
					else
						outReadCliente2.println(120);
						
					//Comprobamos si se ha acabado el juego
					
					if (naves_restantes2.size() == 0){
						fin = true;
						outReadCliente1.println(70);
						outReadCliente2.println(70);
						ganador = 0;
					}
					else{
						outReadCliente1.println(69);
						outReadCliente2.println(69);
					}
						
					if (fin == false){	
					
						//Sigue cliente 2
						outReadCliente2.println(4); //Disparando
						enviaTablero(2);
						
						fila_cliente2 = Integer.parseInt(inReadCliente2.readLine());
						columna_cliente2 = Integer.parseInt(inReadCliente2.readLine());
							
						System.out.println("Recibida fila: " + fila_cliente2 + " y columna: " + columna_cliente2 + " del cliente 2");
						
						if (tablero_jugador1[fila_cliente2][columna_cliente2] != '_'){
							tocado = true;
							nave_tocada = tablero_jugador1[fila_cliente2][columna_cliente2];
							outReadCliente2.println(100);
							
							naves_restantes1.remove(naves_restantes1.indexOf(tablero_jugador1[fila_cliente2][columna_cliente2]));
							
							if (!naves_restantes1.contains(tablero_jugador1[fila_cliente2][columna_cliente2])){
								hundido = true;
								outReadCliente2.println(200);
								outReadCliente2.println(tablero_jugador1[fila_cliente2][columna_cliente2]);
							}
							else{
								hundido = false;
								outReadCliente2.println(300);
							}
								
							tablero_jugador1[fila_cliente2][columna_cliente2] = 'X';

						}
						else{
							tocado = false;
							outReadCliente2.println(400);
						}
						
							
						//Recibe el disparo el cliente 1
							
						outReadCliente1.println(18); //Recibiendo
						
						enviaTablero(1);
						
						if (tocado){
							outReadCliente1.println(121);
							outReadCliente1.println(nave_tocada);
							
							if (hundido)
								outReadCliente1.println(99);
							else
								outReadCliente1.println(98);
						}
						else
							outReadCliente1.println(120);
							
						
						if (naves_restantes1.size() == 0){
							fin = true;
							ganador = 1;
							outReadCliente1.println(70);
							outReadCliente2.println(70);
						}
						else{
							outReadCliente1.println(69);
							outReadCliente2.println(69);
						}
					}
						
						
				}
				else{
					
					//Empieza cliente 2
					outReadCliente2.println(4); //Disparando
					enviaTablero(2);
					
					
					fila_cliente2 = Integer.parseInt(inReadCliente2.readLine());
					columna_cliente2 = Integer.parseInt(inReadCliente2.readLine());
						
					System.out.println("Recibida fila: " + fila_cliente2 + " y columna: " + columna_cliente2 + " del cliente 2");
					
					if (tablero_jugador1[fila_cliente2][columna_cliente2] != '_'){
						tocado = true;
						nave_tocada = tablero_jugador1[fila_cliente2][columna_cliente2];
						outReadCliente2.println(100);
						
						naves_restantes1.remove(naves_restantes1.indexOf(tablero_jugador1[fila_cliente2][columna_cliente2]));
						
						if (!naves_restantes1.contains(tablero_jugador1[fila_cliente2][columna_cliente2])){
							hundido = true;
							outReadCliente2.println(200);
							outReadCliente2.println(tablero_jugador1[fila_cliente2][columna_cliente2]);
						}
						else{
							hundido = false;
							outReadCliente2.println(300);
						}
							
						tablero_jugador1[fila_cliente2][columna_cliente2] = 'X';

					}
					else{
						tocado = false;
						outReadCliente2.println(400);
					}
					
						
					//Recibe el disparo el cliente 1
						
					outReadCliente1.println(18); //Recibiendo
					
					enviaTablero(1);
					
					if (tocado){
						outReadCliente1.println(121);
						outReadCliente1.println(nave_tocada);
						
						if (hundido)
							outReadCliente1.println(99);
						else
							outReadCliente1.println(98);
					}
					else
						outReadCliente1.println(120);
						
					if (naves_restantes1.size() == 0){
						fin = true;
						ganador = 1;
						outReadCliente1.println(70);
						outReadCliente2.println(70);
					}
					else{
						outReadCliente1.println(69);
						outReadCliente2.println(69);
					}
					
					if (fin == false){	
					
						//Dispara el cliente 1
						outReadCliente1.println(4); //Disparando
						enviaTablero(1);
						
						fila_cliente1 = Integer.parseInt(inReadCliente1.readLine());
						columna_cliente1 = Integer.parseInt(inReadCliente1.readLine());
							
						System.out.println("Recibida fila: " + fila_cliente1 + " y columna: " + columna_cliente1 + " del cliente 1");
						
						if (tablero_jugador2[fila_cliente1][columna_cliente1] != '_'){
							tocado = true;
							nave_tocada = tablero_jugador2[fila_cliente1][columna_cliente1];
							outReadCliente1.println(100);
							
							naves_restantes2.remove(naves_restantes2.indexOf(tablero_jugador2[fila_cliente1][columna_cliente1]));
							
							if (!naves_restantes2.contains(tablero_jugador2[fila_cliente1][columna_cliente1])){
								hundido = true;
								outReadCliente1.println(200);
								outReadCliente1.println(tablero_jugador2[fila_cliente1][columna_cliente1]);
							}
							else{
								hundido = false;
								outReadCliente1.println(300);
							}
								
							tablero_jugador2[fila_cliente1][columna_cliente1] = 'X';

						}
						else{
							tocado = false;
							outReadCliente1.println(400);
						}
						
							
						//Recibe el disparo el cliente 2
							
						outReadCliente2.println(18); //Recibiendo
						
						enviaTablero(2);
						
						if (tocado){
							outReadCliente2.println(121);
							outReadCliente2.println(nave_tocada);
							
							if (hundido)
								outReadCliente2.println(99);
							else
								outReadCliente2.println(98);
						}
						else
							outReadCliente2.println(120);
							
						if (naves_restantes2.size() == 0){
							fin = true;
							ganador = 0;
							outReadCliente1.println(70);
							outReadCliente2.println(70);
						}
						else{
							outReadCliente1.println(69);
							outReadCliente2.println(69);
						}
					}
					
				}
			}
			
			//Seleccion del ganador
			
			if (ganador == 0){
				outReadCliente1.println(777);
				outReadCliente2.println(666);
			}
			else{
				outReadCliente1.println(666);
				outReadCliente2.println(777);
			}
			
		} catch (IOException e) {
			System.err.println("Error al colocar la nave");
		}

	}
	
	//Enviar tablero a jugadores
	
	private void enviaTablero(int num_escritor){
		for (int i=0; i<cuadricula; i++){
			for (int j=0; j<cuadricula; j++){
				if (num_escritor == 1)
					outReadCliente1.print(tablero_jugador1[i][j] + " ");
				else
					outReadCliente2.print(tablero_jugador2[i][j] + " ");
			}
			if (num_escritor == 1)
				outReadCliente1.println();
			else
				outReadCliente2.println();
		}
	}

	
	//Metodo para colocar nuevas naves en el tablero
	
	private void colocaNave(int longitud, char letra_nave){
		
		try{
			fila_cliente1 = Integer.parseInt(inReadCliente1.readLine());
			columna_cliente1 = Integer.parseInt(inReadCliente1.readLine());
					
			System.out.println("Recibida fila: " + fila_cliente1 + " y columna: " + columna_cliente1 + " del cliente 1");
			
			if (fila_cliente1+longitud <= 7 && !hayObstaculo(1,fila_cliente1, columna_cliente1, longitud, 1)){ 
				p1=Pair.of(fila_cliente1+longitud,columna_cliente1);
				posibilidades.add(p1);
			}
			
			if (fila_cliente1-longitud >= 0 && !hayObstaculo(1,fila_cliente1, columna_cliente1, -longitud, 1)){
				p2=Pair.of(fila_cliente1-longitud,columna_cliente1);
				posibilidades.add(p2);
			}
			
			if (columna_cliente1+longitud <= 7 && !hayObstaculo(1,fila_cliente1, columna_cliente1, longitud, 2)){
				p3=Pair.of(fila_cliente1, columna_cliente1+longitud);
				posibilidades.add(p3);
			}
			
			if (columna_cliente1-longitud >= 0 && !hayObstaculo(1,fila_cliente1, columna_cliente1, -longitud, 2)){
				p4=Pair.of(fila_cliente1, columna_cliente1-longitud);
				posibilidades.add(p4);
			}
			
			//Le mandamos el tamaño de las opciones para que sepa cuantas tiene que recibir
			outReadCliente1.println(posibilidades.size());
			
			for (int i=0; i<posibilidades.size(); i++){
				outReadCliente1.println(posibilidades.get(i).getFirst());
				outReadCliente1.println(posibilidades.get(i).getSecond());
			}
				
			eleccion = Integer.parseInt(inReadCliente1.readLine());
					
			//Rellenamos la matriz con los valores obtenidos
			
			fila_eleccion_cliente1 = Integer.parseInt(inReadCliente1.readLine());
			columna_eleccion_cliente1 = Integer.parseInt(inReadCliente1.readLine());
			
			System.out.println("Recibida eleccion: " + eleccion + " del cliente 1");
			System.out.println("Cliente 1 elige fila: " + fila_eleccion_cliente1 + " y columna: " + columna_eleccion_cliente1);			
			
			if (fila_eleccion_cliente1 == fila_cliente1){
				if (columna_eleccion_cliente1 < columna_cliente1){
					
					for (int i=columna_eleccion_cliente1; i <= columna_cliente1; i++){
						tablero_jugador1[fila_eleccion_cliente1][i] = letra_nave;
					}
				}
				else{
					for (int i=columna_cliente1; i <= columna_eleccion_cliente1; i++){
						tablero_jugador1[fila_eleccion_cliente1][i] = letra_nave;
					}
				}
			}
			else{
				if (fila_eleccion_cliente1 < fila_cliente1){
					
					for (int i=fila_eleccion_cliente1; i <= fila_cliente1; i++){
						tablero_jugador1[i][columna_eleccion_cliente1] = letra_nave;
					}
				}
				else{
					for (int i=fila_cliente1; i <= fila_eleccion_cliente1; i++){
						tablero_jugador1[i][columna_eleccion_cliente1] = letra_nave;
					}
				}
			}
			
			
			posibilidades.clear();
			
			fila_cliente2 = Integer.parseInt(inReadCliente2.readLine());
			columna_cliente2 = Integer.parseInt(inReadCliente2.readLine());
		
			System.out.println("Recibida fila: " + fila_cliente2 + " y columna: " + columna_cliente2 + " del cliente 2");
			
			if (fila_cliente2+longitud <= 7 && !hayObstaculo(2,fila_cliente2, columna_cliente2, longitud, 1)){ 
				p1=Pair.of(fila_cliente2+longitud,columna_cliente2);
				posibilidades.add(p1);
			}
			
			if (fila_cliente2-longitud >= 0 && !hayObstaculo(2,fila_cliente2, columna_cliente2, -longitud, 1)){
				p2=Pair.of(fila_cliente2-longitud,columna_cliente2);
				posibilidades.add(p2);
			}
			
			if (columna_cliente2+longitud <= 7 && !hayObstaculo(2,fila_cliente2, columna_cliente2, longitud, 2)){
				p3=Pair.of(fila_cliente2, columna_cliente2+longitud);
				posibilidades.add(p3);
			}
			
			if (columna_cliente2-longitud >= 0 && !hayObstaculo(2,fila_cliente2, columna_cliente2, -longitud, 2)){
				p4=Pair.of(fila_cliente2, columna_cliente2-longitud);
				posibilidades.add(p4);
			}
			
			//Le mandamos el tamaño de las opciones para que sepa cuantas tiene que recibir
			outReadCliente2.println(posibilidades.size());
			
			for (int i=0; i<posibilidades.size(); i++){
				outReadCliente2.println(posibilidades.get(i).getFirst());
				outReadCliente2.println(posibilidades.get(i).getSecond());
			}
			
			eleccion = Integer.parseInt(inReadCliente2.readLine());
					
			//Rellenamos la matriz con los valores obtenidos
			
			fila_eleccion_cliente2 = Integer.parseInt(inReadCliente2.readLine());
			columna_eleccion_cliente2 = Integer.parseInt(inReadCliente2.readLine());
			
			System.out.println("Recibida eleccion: " + eleccion + " del cliente 2");
			System.out.println("Cliente 2 elige fila: " + fila_eleccion_cliente2 + " y columna: " + columna_eleccion_cliente2);
			
			if (fila_eleccion_cliente2 == fila_cliente2){
				if (columna_eleccion_cliente2 < columna_cliente2){
					
					for (int i=columna_eleccion_cliente2; i <= columna_cliente2; i++){
						tablero_jugador2[fila_eleccion_cliente2][i] = letra_nave;
					}
				}
				else{
					for (int i=columna_cliente2; i <= columna_eleccion_cliente2; i++){
						tablero_jugador2[fila_eleccion_cliente2][i] = letra_nave;
					}
				}
			}
			else{
				if (fila_eleccion_cliente2 < fila_cliente2){
					
					for (int i=fila_eleccion_cliente2; i <= fila_cliente2; i++){
						tablero_jugador2[i][columna_eleccion_cliente2] = letra_nave;
					}
				}
				else{
					for (int i=fila_cliente2; i <= fila_eleccion_cliente2; i++){
						tablero_jugador2[i][columna_eleccion_cliente2] = letra_nave;
					}
				}
			}
			
			posibilidades.clear();
			
		}catch (IOException e) {
			System.err.println("Error al obtener los flujos de entrada/salida.");
		}
	}
	
	//Metodo para evitar colisiones a la hora de colocar barcos
	
	public boolean hayObstaculo(int jugador, int fila_ini, int columna_ini, int longi, int principal){
		
		boolean hay = false;
		int min = 0, max = 0;
		
		if (jugador == 1){
		
			if (principal == 1){
				min = Math.min(fila_ini,fila_ini+longi);
				max = Math.max(fila_ini,fila_ini+longi);
				for (int i=min; i <= max ; i++){
					
					if (tablero_jugador1[i][columna_ini] != '_')
						hay = true;
				}
			}
			else{
				min = Math.min(columna_ini,columna_ini+longi);
				max = Math.max(columna_ini,columna_ini+longi);
				
				for (int i=min; i <= max; i++){
					
					if (tablero_jugador1[fila_ini][i] != '_')
						hay = true;
				}
			}
		}
		else{
			if (principal == 1){
				min = Math.min(fila_ini,fila_ini+longi);
				max = Math.max(fila_ini,fila_ini+longi);
				for (int i=min; i <= max ; i++){
					
					if (tablero_jugador2[i][columna_ini] != '_')
						hay = true;
				}
			}
			else{
				min = Math.min(columna_ini,columna_ini+longi);
				max = Math.max(columna_ini,columna_ini+longi);
				
				for (int i=min; i <= max; i++){
					
					if (tablero_jugador2[fila_ini][i] != '_')
						hay = true;
				}
			}
		}
		
		return hay;
	}
}
