package ejercicio4;

import messagepassing.MailBox;
import messagepassing.Selector;

/**
 * Clase que modela el proceso admin del ejercicio 4.
 */
public class Admin extends Thread {
	private Selector s;
	private MailBox inicioVotacion, finVotacion;

	/**
	 * Constructor de clase Admin
	 * 
	 * @param buzonInicioVot buzon en el que recibirá un aviso cuando inicie la
	 *                       votacion
	 * @param buzonFinVot    buzon al que mandará un aviso cuando acabe la votacion
	 */
	public Admin(MailBox buzonInicioVot, MailBox buzonFinVot) {
		this.inicioVotacion = buzonInicioVot;
		this.finVotacion = buzonFinVot;

		this.s = new Selector();
		s.addSelectable(buzonInicioVot, false);
	}

	/**
	 * Método que ejecuta la clase Admin. Espera a recibir un aviso cuando inicie la
	 * votación. Después espera un tiempo arbitrario y envía un aviso al servidor
	 * para que termine la votación.
	 */
	@Override
	public void run() {
		while (true) {
			switch (s.selectOrBlock()) {
			case 1:
				String received = (String) inicioVotacion.receive();
				if (received.equals("start"))
					try {
						sleep(3000);
					} catch (Exception e) {
					}
				finVotacion.send("end");
				break;
			}
			break;

		}
	}
}
