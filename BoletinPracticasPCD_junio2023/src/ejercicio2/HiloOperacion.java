package ejercicio2;

import java.util.Random;

/** Clase que modela los hilos que implementan las operaciones en el ejercicio 2.
 */
public class HiloOperacion extends Thread {
	private int a, b, suma;
	private long id;
	
	/** Constructor de la clase HiloOperacion, obtiene el ID del hilo e inicializa las variables aleatorias.
	 */
	public HiloOperacion() {
		a = 0;
		b = 0;
		suma = 0;
		id = this.getId();
	}

	/**Método de ejecución de los hilos. Repite 30 veces la operación especificada, genera dos números aleatorios
	 * y calcula la suma de ambos, después, según si el resultado MOD 5 = 0 o no y si es par o no elige los paneles
	 * a los que debe acceder, adquiere el semáforo de los paneles que te tocan,
	 * obtiene exclusión mutua para el array que controla que paneles están en uso
	 * y selecciona el que esté disponible de entre los que puede escoger (1, 2-3 o 4-5), crea un String con lo que imprimirá
	 * e imprime todo a través del panel.
	 * 
	 */
	@Override
	public void run() {
		for (int i = 0; i < 30; i++) {
			a = new Random().nextInt(0, 101);
			b = new Random().nextInt(0, 101);
			suma = a + b;

			if ((suma % 5) == 0) {
				try {
					Ejercicio2.uno.acquire();
					Ejercicio2.mutex.acquire();
					Ejercicio2.libre[0] = false;
					Ejercicio2.mutex.release();
					String output = "Hilo con ID ";
					output = output.concat(Long.toString(id)).concat("\n");
					output = output.concat("Números generados ").concat(Integer.toString(a)).concat("   ")
							.concat(Integer.toString(b)).concat("\n");
					output = output.concat("Operación a realizar: suma\n");
					output = output.concat("Resultado: ").concat(Integer.toString(suma)).concat("\n");
					output = output.concat("Fin del hilo ID ").concat(Long.toString(id)).concat("\n");
					Ejercicio2.paneles[0].escribir_mensaje(output);
					Ejercicio2.mutex.acquire();
					Ejercicio2.libre[0] = true;
					Ejercicio2.mutex.release();
					Ejercicio2.uno.release();
				} catch (InterruptedException e) {
					System.out.println("Hilo interrumpido.");
					e.printStackTrace();
				}
			}

			else {
				if ((suma % 2) == 0) {
					try {
						Ejercicio2.cuatrocinco.acquire();
						Ejercicio2.mutex.acquire();
						int p = 0;
						if (Ejercicio2.libre[3] == true) {
							Ejercicio2.libre[3] = false;
							p = 3;
						}
							
						else {
							Ejercicio2.libre[4] = false;
							p = 4;
						}
						Ejercicio2.mutex.release();
						String output = "Hilo con ID ";
						output = output.concat(Long.toString(id)).concat("\n");
						output = output.concat("Números generados ").concat(Integer.toString(a)).concat("   ")
								.concat(Integer.toString(b)).concat("\n");
						output = output.concat("Operación a realizar: suma\n");
						output = output.concat("Resultado: ").concat(Integer.toString(suma)).concat("\n");
						output = output.concat("Fin del hilo ID ").concat(Long.toString(id)).concat("\n");
						Ejercicio2.paneles[p].escribir_mensaje(output);
						Ejercicio2.mutex.acquire();
						Ejercicio2.libre[p] = true;
						Ejercicio2.mutex.release();
						Ejercicio2.cuatrocinco.release();
					} catch (InterruptedException e) {
						System.out.println("Hilo interrumpido.");
						e.printStackTrace();
					}
				}
				
				else {
					try {
						Ejercicio2.dostres.acquire();
						Ejercicio2.mutex.acquire();
						int p = 0;
						if (Ejercicio2.libre[1] == true) {
							Ejercicio2.libre[1] = false;
							p = 1;
						}
							
						else {
							Ejercicio2.libre[2] = false;
							p = 2;
						}
						Ejercicio2.mutex.release();
						String output = "Hilo con ID ";
						output = output.concat(Long.toString(id)).concat("\n");
						output = output.concat("Números generados ").concat(Integer.toString(a)).concat("   ")
								.concat(Integer.toString(b)).concat("\n");
						output = output.concat("Operación a realizar: suma\n");
						output = output.concat("Resultado: ").concat(Integer.toString(suma)).concat("\n");
						output = output.concat("Fin del hilo ID ").concat(Long.toString(id)).concat("\n");
						Ejercicio2.paneles[p].escribir_mensaje(output);
						Ejercicio2.mutex.acquire();
						Ejercicio2.libre[p] = true;
						Ejercicio2.mutex.release();
						Ejercicio2.dostres.release();
					} catch (InterruptedException e) {
						System.out.println("Hilo interrumpido.");
						e.printStackTrace();
					}
				}
			}
		}

	}
}
