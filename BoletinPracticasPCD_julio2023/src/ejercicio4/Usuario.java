package ejercicio4;

import java.util.Random;

import messagepassing.MailBox;
import messagepassing.Selector;

/**
 * Clase que modela un usuario en la ejecución del ejercicio 4.
 */
public class Usuario extends Thread {
	/** Tiempo que espera un usuario a la respuesta del servidor.
	 */
	private static int T = 15000;
	
	/** Buzon de entrada del usuario. Aquí recibirá respuestas del servidor.
	 */
	private MailBox entrada;
	private Selector s;
	
	/** Buzon de solicitudes de registro del servidor.
	 */
	private MailBox servidorRegistro;
	
	/** Buzon de solicitudes de voto del servidor.
	 */
	private MailBox servidorVoto;
	
	/** Buzon de solicitudes de consulta del servidor.
	 */
	private MailBox servidorCandidatos;
	
	/** Id del usuario, sirve para que el servidor identifique el buzon de usuario
	 * al que debe mandar su respuesta.
	 */
	private int id;
	
	/** Token de votación del usuario.
	 */
	private int token;
	
	//Valor que indica si se ha producido el registro.
	private boolean registrado;
	
	// Valor que indica si se va a votar (true) o se abstiene o no puede votar (false).
	private boolean quiereVotar;
	
	/** Candidato al que se ha decidido votar.
	 */
	private String candidate;

	/**
	 * Constructor de clase. Recibe el buzon de un servidor y un numero
	 * identificador.
	 * 
	 * @param buzon buzon donde el usuario recibe respuestas
	 * @param id Identificador del usuario
	 * @param registro buzon al que se envían peticiones de registro
	 * @param voto buzon al que se envían peticiones de voto
	 * @param candidatos buzon al que se envían peticiones de consulta
	 * @param id identificador del hilo usuario
	 */
	public Usuario(MailBox buzon, MailBox registro, MailBox voto, MailBox candidatos, int id) {
		this.entrada = buzon;
		this.s = new Selector();
		s.addSelectable(entrada, false);
		
		this.servidorRegistro = registro;
		this.servidorVoto = voto;
		this.servidorCandidatos = candidatos;
		this.id = id;
		this.registrado = false;
		this.quiereVotar = false;
	}

	/**
	 * Método que elige un candidato al azar. Si el identificador del usuario es
	 * multiplo de 5 devuelve una abstención. Sino devuelve un candidato aleatorio.
	 * Se elige abstenerse si es múltiplo de 5 porque hay cerca de 120 de estos números
	 * entre 1 y 120, así que más o menos un cuarto de los votantes se abstendrá.
	 * 
	 * @param numero número de candidatos a elegir
	 * @return número de candidato
	 */
	private int selectCandidate(int numero) {
		if (this.id % 5 == 0)
			return 60000;
		else
			return new Random().nextInt(1, numero);
	}

	/**
	 * Accede a la exclusión mutua de la pantalla e imprime por pantalla una cadena
	 * según el código que reciba. 0-registro aceptado. 1-registro denegado.
	 * 2-abstencion. 3-voto correcto. 4-Voto denegado. Para acceder a la exclusión mutua
	 * se bloquea en el buzon mutex para recibir el testigo de la pantalla y cuando lo recibe
	 * (porque otro lo ha liberado) imprime lo que necesite. Después devuelve el testigo al buzon.
	 * 
	 * @param cod codigo de la cadena que se debe imprimir.
	 */
	private void imprimir(int cod) {
		String o = (String)Ejercicio4.mutex.receive();
		switch (cod) {
		case 0:
			System.out.println("Soy el usuario " + (this.id + 1)
					+ " y me he registrado correctamente, y este es mi token: " + this.token);
			break;
		case 1:
			System.out.println("Soy el usuario " + (this.id + 1) + " y no me he podido registrar");
			break;
		case 2:
			System.out.println("Soy el usuario " + (this.id + 1) + " y he decidido abstenerme de votar");
			break;
		case 3:
			System.out.println("Soy el usuario " + (this.id + 1) + " y he votado a " + this.candidate);
			break;
		case 4:
			System.out.println("Soy el usuario " + (this.id + 1) + " y no han admitido mi voto");
			break;
		}
		Ejercicio4.mutex.send(o);
	}

	/**
	 * Método run. Modela el comportamiento de un hilo usuario. Primero intenta registrarse
	 * en el servidor. Si no lo consigue informa por pantalla y termina la ejecución.
	 * Si lo consigue pide al servidor la lista de candidatos. En caso de que no reciba
	 * respuesta acaba la ejecución, sino llama a selectCandidate para elegir a quien
	 * votar. Si decide abstenerse informa por pantalla y termina la ejecución, sino
	 * pide votar al servidor. Tanto si recibe por respuesta que no tiene la capacidad
	 * de votar (un error), bien porque la votación esté cerrada o porque no tuviera
	 * derecho a voto, como si no recibe respuesta indica que su voto no ha sido aceptado.
	 * Si es aceptado lo imprime por pantalla.
	 */
	@Override
	public void run() {
		//Manda una solicitud de registro y espera una respuesta.
		String solicitudRegistro = Integer.toString(id).concat(":usuario").concat(Integer.toString(id));
		servidorRegistro.send(solicitudRegistro);
		switch (s.selectOrTimeout(T)){
		case 1:
			//Si la recibe comprueba qué ha recibido.
			String respuestaRegistro = (String) entrada.receive();
			String[] mensaje1 = respuestaRegistro.split(":");
			switch (mensaje1[0]) {
			case "error":
				//Si es un error indica que el registro ha fracasado.
				imprimir(1);
				break;
			case "token":
				//En caso de haberse registrado guarda su token y lo indica por pantalla.
				this.token = Integer.parseInt(mensaje1[1]);
				this.registrado = true;
				imprimir(0);
				break;
			}
			break;
		case 0:
			//En caso de no recibir nada indica un registro fallido.
			imprimir(1);
			break;
		}
		//Si se ha registrado intenta votar.
		if (this.registrado) {
			//Solicita al servidor la lista de candidatos.
			String solicitudCandidatos = Integer.toString(id);
			servidorCandidatos.send(solicitudCandidatos);
			switch (s.selectOrTimeout(T)){
			case 1:
				//Si el servidor se la devuelve elige al candidato al que votará (o si se abstiene)
				String respuestaCandidatos = (String) entrada.receive();
				String[] mensaje2 = respuestaCandidatos.split(":");
				int voto = selectCandidate(Integer.parseInt(mensaje2[0])+1);
				//En caso de abstención informa por pantalla.
				if (voto == 60000)
					imprimir(2);
				else {
					//Si vota guarda el candidato.
					candidate = mensaje2[voto];
					quiereVotar = true;
				}
				break;
			case 0:
				//Si no recibe respuesta terminará la ejecución.
				break;
			}
			//En caso de querer votar entra en este if.
			if (this.quiereVotar) {
				//Manda su solicitud de voto al servidor.
				String solicitudVoto = Integer.toString(id).concat(":usuario").concat(Integer.toString(id))
						.concat(":").concat(Integer.toString(token)).concat(":").concat(this.candidate);
				servidorVoto.send(solicitudVoto);
				switch (s.selectOrTimeout(T)){
				case 1:
					//En caso de recibir respuesta comprueba lo que ha llegado:
					String respuestaVoto = (String) entrada.receive();
					String[] mensaje3 = respuestaVoto.split(":");
					switch (mensaje3[0]) {
					case "error":
						//Si es un error informa de que su voto no se ha admitido.
						imprimir(4);
						break;
					case "confirmado":
						//En caso contrario informa de su voto.
						imprimir(3);
					}
					break;
				case 0:
					//Si no recibe nada indica que su voto no se ha admitido.
					imprimir(4);
					break;
				}
			}
		}
	}

}