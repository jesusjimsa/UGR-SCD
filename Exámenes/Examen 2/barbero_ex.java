import monitor.*;
import java.util.Random;

class Barberia extends AbstractMonitor{
	private boolean pelando;
	private int esperando;
	private int melenudos;
	private final int num_sillas = 5;

	private Condition barbero = makeCondition();
	private Condition clientes = makeCondition();

	public Barberia(){
		pelando = false;	//Al principio no se está pelando a nadie
		esperando = 0;		//Al principio no hay clientes esperando
		melenudos = 0;
	}

	//Invcado por los clientes para cortarse el pelo
	public void cortarPelo(int num_cliente){
		enter();
		
		esperando++;

		if(pelando){
			clientes.await();
		}

		pelando = true;

		System.out.println("El cliente " + num_cliente + " ha comenzado a pelarse\n");
		melenudos += 1;

		if(melenudos == 3){
			System.out.println("Estoy harto de pelar a esta panda de melenudos piojosos\n");
		}

		barbero.signal();

		leave();
	}

	//Invocado por el barbero para esperar (si procede) a un nuevo cliente y sentarlo para el corte
	public void siguienteCliente(){
		enter();

		if(esperando == 0){
			System.out.println("El barbero esta durmiendo\n");
			barbero.await();
			melenudos = 0;
		}
		
		if(esperando > num_sillas){
			esperando = 5;
			System.out.println("Se ha marchado un cliente sin pelarse porque estaba lleno\n");
		}
		else{
			esperando--;
			pelando = true;
		}

		

		leave();
	}

	//Invocado por el barbero para indicar que ha terminado de cortar el pelo
	public void finCliente(){
		enter();

		System.out.println("El cliente ha terminado de pelarse\n");

		pelando = false;

		clientes.signal();

		leave();
	}
}

class aux{
	static Random genAlea = new Random() ;

	static void dormir_max(int milisecsMax){
    	try{
			Thread.sleep( genAlea.nextInt( milisecsMax ) ) ;
    	}

		catch(InterruptedException e){
			System.err.println("sleep interumpido en 'aux.dormir_max()'");
		}
	}
}

class Cliente implements Runnable{
	public Thread thr;
	private int num_cliente;
	private Barberia barberia;

	public Cliente(Barberia la_barberia, int n_cliente){
		barberia = la_barberia;
		num_cliente = n_cliente;
		thr = new Thread(this, "Cliente " + n_cliente);
	}

	public void run (){
		while (true){
			barberia.cortarPelo(num_cliente);	//El cliente espera (si procede) y se corta el pelo
			aux.dormir_max(2000);				//El cliente está fuera de la barberia un tiempo
		}
	}
}

class Barbero implements Runnable{
	public Thread thr;
	private Barberia barberia;

	public Barbero(Barberia la_barberia){
		barberia = la_barberia;
		thr = new Thread(this, "Barbero");
	}

	public void run(){
		while (true){
			barberia.siguienteCliente();
			aux.dormir_max(2500);	//El barbero está cortando el pelo
			barberia.finCliente();
		}
	}
}

class barbero_ex{
	public static void main(String[] args){
		Barberia barberia = new Barberia();
		Barbero barbero = new Barbero(barberia);
		Cliente clientes[] = new Cliente[8];

		for(int i = 0; i < 8; i++){
			clientes[i] = new Cliente(barberia, i);
		}

		barbero.thr.start();

		for(int i = 0; i < 8; i++){
			clientes[i].thr.start();
		}
	}
}
