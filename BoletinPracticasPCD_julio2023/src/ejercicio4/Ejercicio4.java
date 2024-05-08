package ejercicio4;

import messagepassing.MailBox;

/** Ejercicio 4. Programa principal.
 */
public class Ejercicio4 {
	public static MailBox mutex = new MailBox(1);
	public static MailBox[] buzonUsuarios = new MailBox[120];
	public static MailBox buzonInicioVot = new MailBox(1); //en este recibe el admin y manda el servidor
	public static MailBox buzonFinVot = new MailBox(1); // en este manda el admin y recibe el servidor
	public static MailBox buzonRegistros = new MailBox();
	public static MailBox buzonVotos = new MailBox();
	public static MailBox buzonCandidatos = new MailBox();
	
	/** Main. Inicializa el administrador y el servidor. Genera los candidatos. 
	 * Crea 120 usuarios y  ejecuta todos los hilos.
	 */
	public static void main(String[] args) {
		mutex.send("testigo");
		Admin admin = new Admin(buzonInicioVot,buzonFinVot);
		String candidatos[] = new String[5];
		for (int i = 0; i < candidatos.length; i++) {
			candidatos[i] = "candidato ".concat(Integer.toString(i));
			
		}
		Servidor server = new Servidor(buzonInicioVot, buzonFinVot, buzonRegistros, buzonVotos, buzonCandidatos, candidatos);
		admin.start();
		server.start();
		for (int i = 0; i < 120; i++) {
			buzonUsuarios[i] = new MailBox();
		}
		Usuario[] usuarios = new Usuario[120];
		for (int i = 0; i < 120; i++) {
			usuarios[i] = new Usuario(buzonUsuarios[i], buzonRegistros, buzonVotos, buzonCandidatos, i);
		}
		
		for (int i = 0; i < 120; i++) {
			usuarios[i].start();
		}
		
		for (int i = 0; i < 120; i++) {
			try {
				usuarios[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			admin.join();
			server.join();
		} catch (Exception e) {
		}
	}
}
