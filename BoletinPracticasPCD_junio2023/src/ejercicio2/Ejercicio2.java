package ejercicio2;

import java.util.concurrent.Semaphore;

/** Clase que modela el ejercicio 2, contiene un array de booleanos que determina que paneles están siendo usados,
 * los semáforos que permiten el acceso a los grupos de paneles (1, 2-3, 4-5) y otro semáforo que garantiza la
 * exclusión mutua al acceder al array de booleanos. También contiene los 5 paneles.
 */
public class Ejercicio2 {
	
	public static boolean [] libre;
	public static Semaphore uno = new Semaphore(1);
	public static Semaphore dostres = new Semaphore(2);
	public static Semaphore cuatrocinco = new Semaphore(2);
	public static Semaphore mutex = new Semaphore(1);
	public static Panel [] paneles;
	
	/** Método main del ejercicio 2, genera 50 objetos HiloOperacion que ejecutarán su código
	 * y los ejecuta.
	 */
	public static void main(String[] args) {
		
		libre = new boolean[5];
		for (int i=0; i<5; i++)
			libre[i] = true;
		
		paneles = new Panel[5];
		for (int i = 0; i < 5; i++) {
			String nombre = "Panel ";
			nombre = nombre.concat(Integer.toString(i+1)) ;
			paneles[i] = new Panel(nombre,250, 250);
		}
		Thread[] hilos = new HiloOperacion [50];
		for (int i = 0; i < 50; i++) {
			hilos[i] = new HiloOperacion();
		}
		
		for (int i = 0; i < 50; i++) {
			hilos[i].start();
		}
		
		try {
			for (int i = 0; i < 50; i++) {
				hilos[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("FIN DEL PROGRAMA");
		
	}
}
