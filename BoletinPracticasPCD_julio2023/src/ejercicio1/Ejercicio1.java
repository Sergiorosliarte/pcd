package ejercicio1;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase que modela el código principal del primer ejercicio del boletín de prácticas.
 */
public class Ejercicio1 {
	//Cerrojo para implementar la exclusión mutua por espera ocupada.
	public static Lock cerrojo;
	
	/**
	 * Método principal, crea un hilo suma y un hilo pontencia y los ejecuta.
	 */
	public static void main(String[] args) {

		cerrojo = new ReentrantLock();
		Thread pot = new HiloPotencia();
		Thread sum = new HiloSuma();
		
		pot.start();
		sum.start();
		try {
			pot.join();
			sum.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("FIN DEL PROGRAMA");
	}
}
