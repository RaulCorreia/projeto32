package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class Conn extends Thread implements MyCallBack{
	
	private DataInputStream in; 
    private DataOutputStream out;
    private Socket socket;
    
    private DatagramSocket sendMultiCast;
    private InetAddress group;
    
    private String sign = "";
    private String request = "";
    private String toSave = "";
    
    private int statePing = 0;
    private List<String> listPing;
    private MyCallBack callPing;
    
    public Conn(Socket socket, MyCallBack callPing) throws IOException {
    	
    	System.out.print("Conexao estabelecida ");
    	this.socket = socket;
    	this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());
        
        this.group = InetAddress.getByName("230.0.0.0");
		this.sendMultiCast = new DatagramSocket();
		
		this.listPing = new ArrayList<>();
		
		this.callPing = callPing;
    }
    
 
    public void run() {
    	
    	try {
    		
    		this.out.writeBoolean(true);
        	String nome = this.in.readUTF();
        	this.sign = nome;
        	System.out.println("com: " + nome);
        	
        	CastReceiver receiver = new CastReceiver(this.sign, this);
        	Thread thread = new Thread(receiver);
            thread.start();
        	
        	boolean run = true;
        	ping();
	        while(run) {
				
	        	String opcao = this.in.readUTF();
	        	
	        	switch(opcao) {
	        	
		        	case "soma":{
		        		
		        		int i = this.in.readInt();
		        		int j = this.in.readInt();
		        		String msg = "!soma!" + i + "@" + j;
		        		sendCast(msg);
		        		
		        		this.request = "soma";
		        	    this.toSave =  i+" + " +j+" = ";
		        		
		        	}
		        		break;
		        		
		        		
	        		case "sub":{
		        		
		        		int i = this.in.readInt();
		        		int j = this.in.readInt();
		        		String msg = "!sub!" + i + "@" + j;
		        		sendCast(msg);
		        		
		        		this.request = "sub";
		        	    this.toSave =  i+" - " +j+" = ";
		        		
	        		}
	        			break;
	        			
	        			
	        		case "mult":{
		        		
		        		int i = this.in.readInt();
		        		int j = this.in.readInt();
		        		String msg = "!mult!" + i + "@" + j;
		        		sendCast(msg);
		        		
		        		this.request = "mult";
		        	    this.toSave =  i+" * " +j+" = ";
		        		
	        		}
	        			break;
	        			
	        			
	        		case "div":{
		        		
		        		int i = this.in.readInt();
		        		int j = this.in.readInt();
		        		String msg = "!div!" + i + "@" + j;
		        		sendCast(msg);
		        		
		        		this.request = "div";
		        	    this.toSave =  i+" / " +j+" = ";
		        		
	        		}
	        			break;
	        			
	        			
	        		case "show":{
	        			File file = new File("src/sessions/"+this.sign+".txt"); 
	        			Scanner scan = new Scanner(file); 
	        				  
	        			String registro = "";
        				while (scan.hasNextLine()) {
        					registro += scan.nextLine() + "\n"; 
        				} 
        				
        				this.out.writeUTF(registro);
	        		}
	        			break;
	        			
	        		case "sair":{
	        			
	        			System.out.println("Conex�o finalizada com " + this.sign);
	        			
	        			run = false;
	        			
	        			FileWriter arq = new FileWriter("src/sessions/"+this.sign+".txt");
	        			PrintWriter writer = new PrintWriter(arq);
	        			writer.print("");
	        			writer.close();
	        			
	        			this.socket.close();
						this.in.close(); 
						this.out.close();
	        		}
	        			break;
		                
		        }
		        	
	        }
	        	
			
        
    	} catch (IOException e) {
    		//e.printStackTrace();
    		System.out.println("Conex�o finalizada com " + this.sign);
		}
    	
    	
    	
    }
    
    
    private void ping() {
    	
    	Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run(){
            	
            	
            	if(statePing == 0) {
            		System.out.println("ENVIANDO PING");
            		sendCast("!ping!0");
            		statePing = 1;
            	} else {
            		System.out.println("CHECANDO PONG");
            		callBackRetorno("checkPing", "");
            		statePing = 0;
            	}
            }
        };
        timer.schedule( task, 0, 10000 );
    }
    
    
    private void sendCast(String mensagem) {
    	
    	try {
    		
    		mensagem = "conn!"+this.sign + mensagem;
    		byte[] buf = mensagem.getBytes();
	    	DatagramPacket packet = new DatagramPacket(buf, buf.length, this.group, 4446);
	    	sendMultiCast.send(packet);
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    // Thread para ouvir os multicast
 	//--------------------------------------------------------------------------
 	public class CastReceiver extends Thread {
 		
 		protected MulticastSocket receivMultiCast;
 		protected InetAddress group;
 	    protected String sign;
 	    
 	    protected MyCallBack callback;
 	    
 	    
 	    public CastReceiver(String sign, MyCallBack callback) throws IOException {
 	    	
 	    	this.sign = sign;
 	    	
 	    	this.receivMultiCast = new MulticastSocket(4446);
 	    	this.group = InetAddress.getByName("230.0.0.0");
 	        this.receivMultiCast.joinGroup(this.group);
 	        
 	        
 	        this.callback = callback;
 	        
 	    }
 	    
 	 
 	    public void run() {
 	    	
 	    	try {
 	    		
 	    		while (true) {
 	    			
 	    			byte[] buf = new byte[256];
 	        		DatagramPacket packet = new DatagramPacket(buf, buf.length);
 					this.receivMultiCast.receive(packet);
 					
 	                String received = new String(packet.getData(), 0, packet.getLength());
 	                String split[] = received.split("!");
 	                
 	                if(split[0].equalsIgnoreCase("server") && split[1].equalsIgnoreCase(this.sign)){
 	                	this.callback.callBackRetorno(split[2], split[3]);
 	                }
 	               
 	               
 	            }
 	    		
 	    		
 	    	 } catch (IOException e) {
 				e.printStackTrace();
 	    	 }
 	    	
 	    	
 	    }

 	    
 	}


	@Override
	public void callBackRetorno(String opcao, String result) {
				
		if(opcao.equalsIgnoreCase(this.request)) {
			
			System.out.println(opcao + " resultado: " + result);
			this.request = "received";
			this.toSave += result;
			
			// Salva sess�o no arquivo
			try {
				FileWriter arq = new FileWriter("src/sessions/"+this.sign+".txt", true);
				PrintWriter gravarArq = new PrintWriter(arq);
				gravarArq.println(this.toSave);
				arq.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//----
			
			this.toSave = "";
			
			try {
				this.out.writeUTF(result);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else if(opcao.equalsIgnoreCase("pong")) {
			this.listPing.add(opcao);
			System.out.println("PONG RECEBIDO: " + opcao);
		} else if(opcao.equalsIgnoreCase("checkPing")) {
			
			System.out.println("CONFERINDO PONG");
			if(this.listPing.size() < 3) {
				this.callPing.callBackRetorno("restart", "");
			}
			
			
			this.listPing.clear();
		}
		
		
	}

}
