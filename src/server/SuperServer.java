package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class SuperServer implements MyCallBack{
	
    private Thread thread1;
    private Thread thread2;
    private Thread thread3;
    
    private boolean runOther;
    
    private Scanner teclado;
    
	
	public SuperServer() throws IOException, InterruptedException {
		
		this.runOther = true;
		this.teclado = new Scanner(System.in);
		initServer();
		
	}
	
	
	public void initServer() throws IOException, InterruptedException {
		
		GetConn conn = new GetConn(this);
		Thread threadConn = new Thread(conn);
		threadConn.start();
		
		Server serv = new Server();
        this.thread1 = new Thread(serv);
        this.thread1.start();
        
        Server serv2 = new Server();
        this.thread2 = new Thread(serv2);
        this.thread2.start();
        
        Server serv3 = new Server();
        this.thread3 = new Thread(serv3);
        this.thread3.start();
		
		while(true) {
			
	        System.out.println("Derrubar servidor?");
	        String option = teclado.nextLine();
	        
	        if(option.equalsIgnoreCase("sim") ) {
	        	this.thread3.stop();
	        	this.listnerServ();
	        }
	        
		}
	}

	
	public class GetConn extends Thread implements MyCallBack {
		
		private ServerSocket socketServidor;
	    private MyCallBack callPing;
	    
	    public GetConn(MyCallBack callPing) throws IOException {
	    	this.socketServidor = new ServerSocket(12345);
	    	this.callPing = callPing;
	    }
	    
	 
	    public void run() {
	    	
	    	while(true) {
	    	
		    	try {
		    		
		    		Socket socket = this.socketServidor.accept();
					
		    		Conn conn = new Conn(socket, this);
		    		new Thread(conn).start();
		    		
		    	} catch (IOException e) {
		    	}
	    	}
	    }


		@Override
		public void callBackRetorno(String opcao, String result) {
			callPing.callBackRetorno(opcao, result);
		}

	    
	}
	
	
	public void listnerServ() throws InterruptedException {
		System.out.println("Servidor 3 caiu..");
//		TimeUnit.SECONDS.sleep(20);
//		System.out.println("Servidor 3 iniciando");
//		this.runOther = true;
	}


	@Override
	public void callBackRetorno(String opcao, String result) {
		this.runOther = true;
		
		if(this.runOther) {
			
			try {
				
				System.out.println("Servidor 3 iniciando");
				Server serv3 = new Server();
				this.thread3 = new Thread(serv3);
		        this.thread3.start();
		        
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        
	        this.runOther = false;
		}
	}
	
}
