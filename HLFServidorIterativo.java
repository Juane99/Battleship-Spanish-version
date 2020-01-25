//
// HLFServidorIterativo
// (CC) Juan Emilio Martinez, 2019
//

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HLFServidorIterativo {

	public static void main(String[] args) {
		ServerSocket serverSocket;
		// Puerto de escucha
		int port=8888;
		
		int num_hebra = 0;

		
		try {
			// Abrimos el socket en modo pasivo, escuchando el en puerto indicado por "port"
			//////////////////////////////////////////////////
			serverSocket=new ServerSocket(port);
			//////////////////////////////////////////////////
			
			// Mientras ... siempre!
			do {
				Socket socket_cliente1 = null;
				Socket socket_cliente2 = null;
				
				try{
					// Aceptamos una nueva conexión con accept()
					/////////////////////////////////////////////////
					socket_cliente1=serverSocket.accept();
					socket_cliente2=serverSocket.accept();
					//////////////////////////////////////////////////
					
					// Creamos un objeto de la clase ProcesadorYodafy, pasándole como 
					// argumento el nuevo socket, para que realice el procesamiento
					// Este esquema permite que se puedan usar hebras más fácilmente.
				} catch (IOException e){
					System.out.println("Error: no se pudo aceptar la conexion solicitada");
				}
				ProcesadorHLF procesador=new ProcesadorHLF(socket_cliente1,socket_cliente2,num_hebra++);
				procesador.start();
				
			} while (true);
			
		} catch (IOException e) {
			System.err.println("Error al escuchar en el puerto "+port);
		}

	}

}
