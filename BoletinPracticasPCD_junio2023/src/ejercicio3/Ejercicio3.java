package ejercicio3;

import java.util.Random;

/** Clase que modela el ejercicio 3, contiene un objeto Gasolinera que actua como monitor y 100 clientes (coches).
 */
public class Ejercicio3 {

	/** Método main, inicializa la gasolinera y crea 100 hilos cliente con una cantidad de gasolina y un lavado aleatorios y los ejecuta.
	 */
	public static void main(String[] args) {
		Gasolinera g = new Gasolinera();
		Cliente[] clientes = new Cliente[100];

		for (int i = 0; i < 100; i++) {
			int gasolina = 0;
			int lavado = new Random().nextInt(0, 3);
			if ((i % 3) != 0) {
				gasolina = new Random().nextInt(20, 51);
			}
			clientes[i] = new Cliente(g, gasolina, lavado);
		}

		for (int i = 0; i < 100; i++) {
			clientes[i].start();
		}

		try {
			for (int i = 0; i < 100; i++) {
				clientes[i].join();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("FIN DEL PROGRAMA");
	}
}
