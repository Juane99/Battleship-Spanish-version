//
// HLFClienteTCP
// (CC) Juan Emilio Martinez, 2019
//

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.ArrayList;

public class HLFClienteTCP {

	private static String recepcion;
		
	//Variable fila y columna para cada nave
	private static int fila = 0, columna = 0;
		
	// Nombre del host donde se ejecuta el servidor:
	private static String host="localhost";
	// Puerto en el que espera el servidor:
	private static int port=8888;
		
	// Socket para la conexión TCP
	private static Socket socketServicio=null;
	
	//Juego		
	private static String tablero;
	private static int turno;
	private static boolean fin = false;
	private static boolean disparando;
	private static char[][] tablero_enemigo = new char[8][8];
		
	//Para leer enteros
	private static Scanner reader = new Scanner(System.in);
		
	//Posibilidades
	private static int num_posibilidades = 0;
	private static int pos_izda = 0, pos_dcha = 0;
	private static int eleccion;
	private static ArrayList<Integer> elecciones = new ArrayList<>();
	
	//Flujos
	private static PrintWriter outPrinter;
	private static BufferedReader inReader;

	public static void iniciaFlujos(){
		try{
	
			outPrinter = new PrintWriter(socketServicio.getOutputStream(),true);
			inReader = new BufferedReader( new InputStreamReader(socketServicio.getInputStream()));
			
		}catch (IOException e) {
			System.err.println("Error al crear los flujos de entrada/salida");
		}
	}


	public static void main(String[] args) {
		
		try {
			// Creamos un socket que se conecte a "hist" y "port":
			//////////////////////////////////////////////////////
			socketServicio = new Socket(host,port);
			//////////////////////////////////////////////////////			
			
			iniciaFlujos();
			
			//Inicializamos el tablero enemigo
	
			for (int i=0; i<8; i++){
				for (int j=0; j<8; j++){
					tablero_enemigo[i][j] = '_';
				}
			}
			
			System.out.println("¡Hola!, este es tu tablero inicial");
			
			enviaTableroAliado();
			
			System.out.println();
			
			/*****************LANCHA******************/
			
			System.out.println("A continuación, introduzca la posición, en formato FILA COLUMNA, de su LANCHA (1 unidad) ");
			fila = reader.nextInt();
			columna = reader.nextInt();
			
			//Le enviamos la fila y la columna al procesador
			
			outPrinter.println(fila);
			outPrinter.println(columna);
			
			
			enviaTableroAliado();
			
			/*****************BUQUE******************/
			
			System.out.println("A continuación introduzca la posición, en formato FILA COLUMNA, de uno de los extremos de su BUQUE (3 unidades) ");
			colocaNaveClie();
			
			/*****************SUBMARINO******************/
			
			System.out.println("A continuación introduzca la posición, en formato FILA COLUMNA, de uno de los extremos de su SUBMARINO (4 unidades) ");
			colocaNaveClie();
			
			/*****************PORTAAVIONES******************/
			
			System.out.println("A continuación introduzca la posición, en formato FILA COLUMNA, de uno de los extremos de su PORTAAVIONES (5 unidades) ");
			colocaNaveClie();
			System.out.println("¡Hemos terminado de colocar las naves!");
			
			
			//ELECCION DE QUIEN EMPIEZA LA PARTIDA
			turno = Integer.parseInt(inReader.readLine());
			
			if (turno == 0)
				System.out.println("¡Enhorabuena!, la aleatoriedad ha decidido que empieces tu");
			else
				System.out.println("La aleatoriedad ha decidido que empiece tu contrincante, suerte la proxima vez");


			//DESARROLLO DE LA PARTIDA
			
			while (fin == false){ //Mientras no se acabe el juego
				
			
				if (Integer.parseInt(inReader.readLine()) == 4)
					disparando = true;
				else
					disparando = false;
					
				enviaTableroAliado();
				System.out.println();
				enviaTableroEnemigo();	
					
				if (disparando){
					
					System.out.println("Indique la fila y la columna donde desee disparar");
								
					fila = reader.nextInt();
					columna = reader.nextInt();
			
					outPrinter.println(fila);
					outPrinter.println(columna);
					
					if (Integer.parseInt(inReader.readLine()) == 100){
						System.out.println("¡TOCADO!");
						
						if (Integer.parseInt(inReader.readLine()) == 200){
							
							switch((inReader.readLine()).charAt(0)){
								case 'L':
									System.out.println("Has hundido la lancha");
									break;
								case 'P':
									System.out.println("Has hundido el portaaviones");
									break;
								case 'B':
									System.out.println("Has hundido el buque");
									break;
								case 'S':
									System.out.println("Has hundido el submarino");
									break;
							}
						}
						tablero_enemigo[fila][columna] = 'X';
					}
					else{
						System.out.println("¡AGUA!");
						tablero_enemigo[fila][columna] = 'A';
					}
					
				}
				else{
					
					
					if (Integer.parseInt(inReader.readLine()) != 120){
						switch((inReader.readLine()).charAt(0)){
							case 'L':
								System.out.println("¡Le han dado a tu lancha!");
								break;
							case 'P':
								System.out.println("¡Le han dado a tu portaaviones!");
								break;
							case 'B':
								System.out.println("¡Le han dado a tu buque!");
								break;
							case 'S':
								System.out.println("¡Le han dado a tu submarino!");
								break;
						}
						
						
						if (Integer.parseInt(inReader.readLine()) == 99)
							System.out.println("¡Y LO HAN HUNDIDO!");
					}
					else
						System.out.println("Tu contrincante ha fallado el disparo");
						
					
				}
				
				//Comprobar si finaliza el bucle
				
				if (Integer.parseInt(inReader.readLine()) == 70)
					fin = true;
			}
			
			//Comprobamos quien ha ganado
			
			System.out.println();
			
			if (Integer.parseInt(inReader.readLine()) == 777)
				System.out.println("¡ENHORABUENA!, has ganado la partida");
			else
				System.out.println("Has perdido... Suerte la proxima vez");
				
		
			// Una vez terminado el servicio, cerramos el socket (automáticamente se cierran
			// el inpuStream  y el outputStream)
			//////////////////////////////////////////////////////
			socketServicio.close();
			//////////////////////////////////////////////////////
			
			// Excepciones:
		} catch (UnknownHostException e) {
			System.err.println("Error: Nombre de host no encontrado.");
		} catch (IOException e) {
			System.err.println("Error de entrada/salida al abrir el socket.");
		}
		
	}
	
	public static void colocaNaveClie(){
		try{
			fila = reader.nextInt();
			columna = reader.nextInt();
			
			outPrinter.println(fila);
			outPrinter.println(columna);
			
			num_posibilidades = Integer.parseInt(inReader.readLine());
			
			//Recibimos las diferentes posibilidades
			
			System.out.println("El otro extremo de la nave puede ser: ");
			
			elecciones.clear();
				
			for (int i=0; i<num_posibilidades; i++){
				pos_izda = Integer.parseInt(inReader.readLine());
				pos_dcha = Integer.parseInt(inReader.readLine());
				elecciones.add(pos_izda);
				elecciones.add(pos_dcha);
					
				System.out.println(i + ". Posicion: (" + pos_izda + " , " + pos_dcha + ")");
			}
				
			//Elegimos la opcion correspondiente y la mandamos
			eleccion = reader.nextInt();
			outPrinter.println(eleccion);
			
			outPrinter.println(elecciones.get(2*eleccion));
			outPrinter.println(elecciones.get(2*eleccion+1));
			
			//Volvemos a imprimir el tablero 
			
			enviaTableroAliado();
			
			elecciones.clear();
			
		}catch (IOException e) {
			System.err.println("Error al colocar la nave");
		}
	}
	
	private static void enviaTableroAliado(){
		try{
			System.out.println("TABLERO ALIADO");
			
			for (int i=0; i<8; i++){
				tablero = inReader.readLine();
				System.out.println(tablero);
			}
		}catch (IOException e) {
			System.err.println("Error al enviar el tablero");
		}
	}
	
	private static void enviaTableroEnemigo(){
		System.out.println("TABLERO ENEMIGO");
		
		for (int i=0; i<8; i++){
			for (int j=0; j<8; j++){
				System.out.print(tablero_enemigo[i][j] + " ");
			}
			System.out.println();
		}
	}
}
