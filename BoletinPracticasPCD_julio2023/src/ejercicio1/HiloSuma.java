package ejercicio1;

import java.util.Random;

/**
 * Clase que modela el hilo que genera A+A del ejercicio 1 del boletín de
 * prácticas
 */
public class HiloSuma extends Thread {
	/** Matriz que se va a generar en cada iteración.
	 */
	private int[][] A = new int[3][3];

	/**
	 * Método que imprime una matriz de tamaño 3x3 al lado de sí misma con un +
	 * entre las dos.
	 * 
	 * @param m Matriz 3x3 a imprimir
	 */
	private void imprimirAMA() {
		System.out.println("A + A");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.print(A[i][j] + " ");
			}
			if (i == 1)
				System.out.print(" + ");
			else
				System.out.print("    ");
			for (int j = 0; j < 3; j++) {
				System.out.print(A[i][j] + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Método que imprime una matriz de tamaño 3x3 con una cabecera que dice "2A".
	 * 
	 * @param m Matriz 3x3 a imprimir
	 */
	private void imprimirA(int[][] aux) {
		System.out.println("2A");
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.print(aux[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	/**
	 * Método que genera en A una matriz de 3x3 con valores enteros aleatorios entre 0 y
	 * 100
	 * 
	 * @return matriz 3x3 de valores aleatorios entre 0 y 100
	 */
	private void generarMatriz() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++)
				A[i][j] = new Random().nextInt(0, 101);
		}
	}

	/**
	 * Método que recibe una matriz 3x3 y calcula m+m y devuelve el resultado.
	 * 
	 * @param Matriz 3x3 a calcular m+m
	 */
	private int[][] sumaMatriz() {
		int[][] aux = new int[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				aux[i][j] = A[i][j] + A[i][j];
			}
		}
		return aux;
	}

	/**
	 * Método de ejecución del hilo, hace 10 repeticiones del trabajo, primero
	 * vuelve a generar la matriz A y calcula 2A y lo almacena en una variable auxiliar.
	 * Después bloquea el cerrojo para garantizar la exclusión mutua, 
	 * imprime A+A y después imprime 2A. Por último libera el
	 * cerrojo.
	 */
	@Override
	public void run() {
		for (int i = 0; i < 10; i++) {
			generarMatriz();
			int[][] aux = sumaMatriz();
			Ejercicio1.cerrojo.lock();
			try {
				imprimirAMA();
				imprimirA(aux);
			} finally {
				Ejercicio1.cerrojo.unlock();
			}
		}
	}
}
