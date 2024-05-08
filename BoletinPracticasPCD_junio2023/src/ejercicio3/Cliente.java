package ejercicio3;

/**
 * Clase que modela el comportamiento de un coche, cliente de una gasolinera
 */
public class Cliente extends Thread {
	// Tiempo para cada lavado
	private static final int B = 100;
	private static final int N = 125;
	private static final int P = 150;

	private Gasolinera gasolinera;
	// Si se repostará o no.
	private final boolean repostar;

	// litros que se van a repostar.
	private final int litros;
	private final int tiempoRepostaje;

	// lavado es un entero que tendrá 3 valores: 0, 1, 2. Estos indican el lavado,
	// básico, normal o premium respectivamente.
	private final int lavado;
	private final int tiempoLavado;

	/**
	 * Constructor de la clase. Guarda el gasto de combustible (si no repuesta es
	 * 0), el tipo de lavado y la gasolinera a la que irá.
	 * 
	 * @param g      Monitor Gasolinera
	 * @param litros cantidad de gasolina
	 * @param lavado tipo de lavado
	 */
	public Cliente(Gasolinera g, int litros, int lavado) {
		this.gasolinera = g;
		if (litros == 0) {
			this.repostar = false;
			this.litros = litros;
			this.tiempoRepostaje = 0;
		} else {
			this.litros = litros;
			this.tiempoRepostaje = getTiempoRepostaje();
			this.repostar = true;

		}

		this.lavado = lavado;
		this.tiempoLavado = getLavado();
	}

	private int getTiempoRepostaje() {
		return this.litros * 4;
	}

	private int getLavado() {
		switch (lavado) {
		case 0:
			return B;
		case 1:
			return N;
		case 2:
			return P;
		default:
			throw new IllegalArgumentException("Unexpected value: " + lavado);
		}
	}

	/**
	 * Método run, modela la ejecución de un hilo cliente. Primero determina si el
	 * coche va a repostar. Si no es así, obtendrá el tiempo de espera en la cola 5
	 * y se pondrá a la espera en esta. Por otro lado si va a repostar, solicita el
	 * repostaje, simula el tiempo de repostar, libera su surtidor y recibe el
	 * tiempo de espera de cada cola y selecciona la más corta. Después, en ambos
	 * casos se pondrá en la cola escogida a esperar hasta que sea el primero y
	 * cuando lo sea bloqueará ese tunel de lavado y simulará el tiempo de lavado.
	 * Por último libera el tunel y notifica al resto de procesos.
	 */
	@Override
	public void run() {
		if (repostar == true) {
			int cola = -1;
			int min = 100000;
			int surtidor = gasolinera.repostar();
			// Simula el tiempo que tarda el coche en repostar
			try {
				Thread.sleep(tiempoRepostaje);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gasolinera.liberarSurtidor(surtidor);
			int colas[] = gasolinera.consultarEspera();
			for (int i = 0; i < 5; i++) {
				if (colas[i] < min) {
					min = colas[i];
					cola = i;
				}
			}
			System.out.println("Cliente " + this.getId() + " ha sido atendido en el surtidor " + (surtidor + 1)
					+ "\nRepostado " + litros + " euros en un tiempo " + tiempoRepostaje
					+ "\n Tiempo estimado de lavado " + tiempoLavado + "\nSeleccionado túnel " + (cola + 1)
					+ "\nTiempo estimado de espera para el lavado en la cola1=" + colas[0] + ", cola2=" + colas[1]
					+ ", cola3=" + colas[2] + ", cola4=" + colas[3] + ", cola5=" + colas[4]);
			
			gasolinera.lavar(cola, this.tiempoLavado);
			try {
				Thread.sleep(this.tiempoLavado);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gasolinera.liberarTunel(cola,tiempoLavado);
		} else {
			int cola = 4;
			int tiempo = gasolinera.consultarEspera()[4];
			System.out
					.println("Cliente " + this.getId() + " solo quiere lavar el coche\n" + " Tiempo estimado de lavado "
							+ getLavado() + "\nTiempo estimado de espera para el lavado en la cola5=" + tiempo);
			gasolinera.lavar(cola, this.tiempoLavado);
			try {
				Thread.sleep(this.tiempoLavado);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gasolinera.liberarTunel(cola,this.tiempoLavado);
		}

	}

}
