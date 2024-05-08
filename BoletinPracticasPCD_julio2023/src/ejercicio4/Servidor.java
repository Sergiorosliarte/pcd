package ejercicio4;

import java.util.HashMap;

import messagepassing.MailBox;
import messagepassing.Selector;

/** Clase que modela un objeto del tipo servidor. Recibe peticiones en sus
 * distintos buzones y las procesa, devolviendo respuestas a los usuarios.
 */
public class Servidor extends Thread {
	/** Tiempo que espera un usuario a la respuesta del servidor.
	 */
	private static int T = 15000;
	
	/** Selector para espera selectiva de mensajes a los distintos buzones.
	 */
	private Selector s;
	
	/** Buzon al que se envía un testigo cuando empieza la votación.
	 */
	private MailBox inicioVotacion;
	/** Buzon donde se recibe un testigo cuando acaba la votación.
	 */
	private MailBox finVotacion;
	
	/** Buzon al que llegan solicitudes de registro.
	 */
	private MailBox registro;
	/** Buzon al que llegan solicitudes de voto.
	 */
	private MailBox votar;
	/** Buzon al que llegan solicitudes de consulta.
	 */
	private MailBox consultarCandidatos;
	
	/** Lista de candidatos.
	 */
	private String[] candidatos;
	
	/** Votos contablilizados para cada candidato.
	 */
	private int[] votos;
	
	/** Valor que indica si la votación está abierta.
	 */
	private boolean votando;
	/** Mapa de usuarios con sus tokens.
	 */
	private HashMap<String, Integer> usuarios;
	/** Último token emitido. Sirve para mandarlos en secuencia.
	 */
	private int ultimoToken;
	/** Valor que indica si el usuario [i] a votado.
	 */
	private boolean[] votado;
	
	/** Constructor de clase. Recibe los buzones y la lista de candidatos y los guarda
	 * en los atributos correctos.
	 * 
	 * @param inicioVotacion
	 * @param finVotacion
	 * @param registro
	 * @param votar
	 * @param consultarCandidatos
	 * @param candidatos
	 */
	public Servidor(MailBox inicioVotacion, MailBox finVotacion, MailBox registro,
			MailBox votar,  MailBox consultarCandidatos, String[] candidatos) {

		this.inicioVotacion = inicioVotacion;
		this.finVotacion = finVotacion;
		this.registro = registro;
		this.votar = votar;
		this.consultarCandidatos = consultarCandidatos;

		this.s = new Selector();
		s.addSelectable(finVotacion, false);
		s.addSelectable(registro, false);
		s.addSelectable(votar, false);
		s.addSelectable(consultarCandidatos, false);

		this.candidatos = candidatos;
		this.usuarios = new HashMap<String, Integer>();
		this.ultimoToken = 1;
		this.votando = false;
		this.votos = new int[candidatos.length];
		for (int i = 0; i < candidatos.length; i++)
			votos[i] = 0;
		this.votado = new boolean[120];
	}
	
	/** Método run, modela una ejecución de un servidor. Mientras que no se marque el fin 
	 * realiza una espera selectiva con timeout. Hay 4 casos en el select. 1 es una recepción
	 * del testigo de fin de votación. Llega al buzon finVotacion de parte del Admin y en este
	 * caso se cierra la votación y se marca el final del bucle principal. El caso 2 es solicitudes
	 * de registro. En este caso comprueba que no haya pasado el límite de 100 usuarios y que el
	 * usuario no esté registrado y le envía su token. El caso 3 es solicitud de voto. Recibe
	 * una cadena con el id de usuario, su nombre registrado, su token y el nombre del candidato
	 * al que vota. Comprueba si la votación está abierta, si está registrado y si los tokens cuadran
	 * y almacena el voto. El caso 4 es una solicitud de consulta de candidatos. Simplemente guarda
	 * la lista en una cadena formateada y la envía al usuario. Si no se recibe ningún mensaje en tiempo T
	 * el select irá al caso 0 y finalizará la votación y pondrá fin al bucle. Después del bucle se contabilizan
	 * los votos y se imprime por pantalla el resultado final.
	 */
	@Override
	public void run() {
		boolean end = false;
		//Mientras que no se acabe la votación:
		while (!end) {
			//Espera selectiva com timeout.
			switch (s.selectOrTimeout(T)) {
			case 0:
				//Tiemout: se cierra la votación.
				votando = false;
				end = true;
			case 1:
				// Se recibe la orden de detener la votación por parte del admin.
				String fin = (String) finVotacion.receive();
				if (fin.equals("end")) {
					this.votando = false;
					end = true;
				}
				 break;
			case 2:
				//Se recibe una peticion de registro, formato: (id:nombre).
				String reg = (String) registro.receive();
				String [] mensaje1 = reg.split(":");
				int id =Integer.parseInt(mensaje1[0]);
				String nombre = mensaje1[1];
				//Si ya está registrado ese nombre se da un error.
				if (usuarios.containsKey(nombre)) {
					Ejercicio4.buzonUsuarios[id].send("error:ya esta registrado");
				}
				//Si ya hay 100 registrados se da un error.
				else if (ultimoToken == 101) {
					Ejercicio4.buzonUsuarios[id].send("error:limite alcanzado");
				}
				//Si no se registra, se actualiza el token y se informa al usuario.
				else {
					usuarios.put(nombre, ultimoToken);
					Ejercicio4.buzonUsuarios[id].send("token:".concat(Integer.toString(ultimoToken)));
					
					//Si ya se han registrado 50 se informa al admin y comienza la votación.
					if (ultimoToken == 51) {
						votando = true;
						inicioVotacion.send("start");
					}
					ultimoToken++;

				}
				break;
			case 3:
				//Se recibe un voto. Formato: (id:nombre:token:candidato).
				String vot = (String) votar.receive();
				String [] mensaje2 = vot.split(":");
				//Si la votación está cerrada da error.
				if (votando == false) {
					Ejercicio4.buzonUsuarios[Integer.parseInt(mensaje2[0])].send("error:votacion cerrada");
				}
				//Si no, si está registrado, no ha votado y cuadra el token contabliliza el voto y marca al usuario como votado.
				else if (usuarios.containsKey(mensaje2[1]) && usuarios.get(mensaje2[1]).equals(Integer.parseInt(mensaje2[2]))
						&& !votado[Integer.parseInt(mensaje2[0])]) {
					Ejercicio4.buzonUsuarios[Integer.parseInt(mensaje2[0])].send("confirmado:voto");
					String voto = mensaje2[3];
					//Busca el candidato votado y suma un voto al mismo
					for (int i = 0; i < candidatos.length; i++) {
						if (voto.equals(candidatos[i]))
							votos[i]++;
					}
					votado[Integer.parseInt(mensaje2[0])] = true;
				}
				//Si no, da error
				else {
					Ejercicio4.buzonUsuarios[Integer.parseInt(mensaje2[0])].send("error:no puede votar");
				}
				break;
			case 4:
				//Solicitud de consulta de candidatos. Formato: (id).
				//Envía al usuario una cadena que informa de cuantos candidatos hay y todos sus nombres.
				String can = (String) consultarCandidatos.receive();
				int iden = Integer.parseInt(can);
				String cands = Integer.toString(candidatos.length);
				for (int i = 0; i < candidatos.length; i++) {
					cands = cands.concat(":").concat(candidatos[i]);
				}
				Ejercicio4.buzonUsuarios[iden].send(cands);
				break;
			}
		}
		/* Cuando acaba la votación pide la exclusión mutua de la pantalla,
			calcula el ganador de la votación y cuantos votos hay. Si hay menos de 25
			indica que la votación no es válida, si no, nombra al ganador*/ 
		String o = (String) Ejercicio4.mutex.receive();
		System.out.println("Fin de la votación");
		int votosTotales = 0;
		int ganador = 0;
		for (int i = 0; i < votos.length; i++) {
			votosTotales += votos[i];
			if (votos[i] > votos[ganador])
				ganador = i;
		}
		if (votosTotales < 25)
			System.out.println("Votación no valida");
		else System.out.println("Ganador: " + candidatos[ganador]);
		//Por último libera la exclusión mutua.
		Ejercicio4.mutex.send(o);
	}

}
