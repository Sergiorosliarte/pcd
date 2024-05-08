package ejercicio3;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase que modela el monitor Gasolinera. este tiene las condiciones de uso de
 * tuneles y surtidores, y se ocupa con ellas de que no se acceda a la misma vez
 * al mismo tunel ni al mismo surtidor.
 */
public class Gasolinera {
	private ReentrantLock l;
	private Condition repostar, pantalla;
	private Condition[] lavado;
	private boolean[] usoSurtidores;
	private int[] colasLavado;
	private boolean[] usoTuneles;
	private boolean usoPantalla;
	private int surtidoresLibres;

	/**
	 * Constructor del monitor Gasolinera. Inicializa los arrays de uso surtidores,
	 * colas de lavado y uso de túneles, así como las condiciones y el cerrojo
	 */
	public Gasolinera() {
		this.l = new ReentrantLock(true);
		this.repostar = l.newCondition();
		this.pantalla = l.newCondition();
		this.lavado = new Condition[5];
		for (int i = 0; i < 5; i++) {
			this.lavado[i] = l.newCondition();
		}
		this.usoSurtidores = new boolean[4];
		for (int i = 0; i < 4; i++) {
			this.usoSurtidores[i] = false;
		}
		this.colasLavado = new int[5];
		for (int i = 0; i < 5; i++) {
			this.colasLavado[i] = 0;
		}
		this.usoTuneles = new boolean[5];
		for (int i = 0; i < 5; i++) {
			this.usoTuneles[i] = false;
		}
		this.surtidoresLibres = 4;
		this.usoPantalla = false;
	}

	/**
	 * Metodo que obtiene un surtidor para repostar: busca un surtidor libre y lo
	 * ocupa, si no hay surtidores libres, espera a que se libere uno y lo ocupa.
	 * 
	 * @return el número de surtidor escogido
	 * @throws InterruptedException
	 */
	public int repostar() {
		l.lock();
		int i = 0;
		try {
			if (surtidoresLibres == 0) {
				repostar.await();
			}

			while (usoSurtidores[i] == true) {
				i++;
			}

			usoSurtidores[i] = true;
			surtidoresLibres--;

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			l.unlock();
		}
		return i;
	}

	/**
	 * Metodo que obtiene un libera para repostar: marca el surtidor como libre y
	 * notifica al siguiente proceso que esté esperando un surtidor.
	 * 
	 * @param surtidor numero de surtidor a liberar
	 */
	public void liberarSurtidor(int surtidor) {
		l.lock();
		try {
			surtidoresLibres++;
			usoSurtidores[surtidor] = false;
			repostar.signal();
		} finally {
			l.unlock();
		}
	}

	/**
	 * Método que consulta el tiempo total de espera en todas las colas de lavado.
	 * 
	 * @return tiempo de espera en cada cola
	 * @throws InterruptedException
	 */
	public int[] consultarEspera() {
		l.lock();
		int[] colas = new int[5];
		try {

			if (usoPantalla == true) {
				pantalla.await();
			}
			usoPantalla = true;
			for (int i = 0; i < colas.length; i++) {
				colas[i] = colasLavado[i];
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			l.unlock();
		}
		return colas;
	}

	/**
	 * Método que simula como un cliente llega a una cola y espera en await() hasta
	 * que sea el primero de la cola. Una vez es el primero marca como ocupado ese
	 * túnel de lavado y procede a lavar su coche.
	 * 
	 * @param cola   cola a la que se quiere incorporar
	 * @param tiempo tiempo de espera
	 * @param id     id del hilo cliente
	 * @throws InterruptedException
	 */
	public void lavar(int cola, int tiempo){
		l.lock();
		try {
			colasLavado[cola] += tiempo;
			usoPantalla = false;
			pantalla.signal();

			if (usoTuneles[cola] == true) {
				lavado[cola].await();
			}
			usoTuneles[cola] = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			l.unlock();
		}
	}

	/**
	 * Método que simula como un coche que estaba lavandose deja el túnel de lavado.
	 * Es llamado cuando se termina de lavar un coche y marca como libre el túnel,
	 * se quita su turno de la cola y se notifica al siguiente de los hilos que
	 * estaban bloqueados en lavar.
	 * 
	 * @param tunel tunel de lavado en uso
	 */
	public void liberarTunel(int tunel, int tiempo) {
		l.lock();
		try {
			usoTuneles[tunel] = false;
			colasLavado[tunel] -= tiempo;
			lavado[tunel].signal();
		} finally {
			l.unlock();
		}
	}

}
