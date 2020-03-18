package main;
import static main.Main.tokenSize;
import static main.Main.log;
import static main.Main.connectionsLimit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

// ���� ���������� �� �������� � ���������
enum ClientThreadStatus{
    Waiting,
    Running,
    Shutting
}

// ��������� ����� ������������ �����������
public class ServerThread extends Thread {
    private Vector<Integer> free_threads = null; // ������� ��������� �������
    ClientThread[] clientThreads = null; // ������ ��� ��������� �����������

    private ServerSocket serverSocket = null; // "������" ������� - � ���� ������������ �������
    boolean running = false; // ���� ����� �������� (������ �������): true - ��������, false - ��������

    @Override
    public void run(){
        running = true;
        log("ServerThread started");
        try{
        	log("Binding...");
            
            serverSocket = new ServerSocket(Main.port, 10, Main.ip); // �������� �������, backlog - ������ ������� �������� ��������� �����������
            serverSocket.setSoTimeout(500); // ��������� �������� ����������� ������� (� �������������)

            free_threads = new Vector<>(connectionsLimit);      	//
            clientThreads = new ClientThread[connectionsLimit]; 	//
            for (int i = 0; i < connectionsLimit; i++){         	// �������� �������
                clientThreads[i] = new ClientThread(i);             // ��� ���������
                clientThreads[i].start();                           // �����������
                free_threads.add(i);                                //
            }                                                       //
            
            log("Success");
            log("Waiting for connections");
            // ���� ��������� �����������
            while (running){
            	
                try {
                    Socket client = serverSocket.accept(); // �������� ����������� (��������� �����)
                    log("Connected");

                    try{
                        int last = free_threads.size() - 1;         // ��������� ����������
                        int index = free_threads.remove(last);      // ������ � ��������
                        clientThreads[index].handle(client);        // ��� ���������� ��������
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        client.close(); // ���������� �������, ���� ���������� ������ ���
                    }
                    log("Handled");
                }
                catch (SocketTimeoutException timeout) {
                	continue; // ���������� ��-�� ������� �������� ����������� - ����������� ��������
                }
                catch (IOException e){
                	log(e.toString());
                    break;
                }
            }
        }
        catch(IOException e){ // ������ ��� �������� �������
        	log(e.toString());
        }
        
        try {						//
			serverSocket.close();	// ����������
		} catch (Exception e) {		// �����������
			e.printStackTrace();	// �����������
		}							//
        
        running = false;
        log("ServerThread stopped");
    }
    
    // ���������� ����� - ����� ��� ������ � ������������ ��������
    public class ClientThread extends Thread {
    	private String login;
    	private String password;
    	
        private int ID; // ����� ������ � ������� ������� (ServerThread.clientThreads)
        private String ID_str; // ����� � ���� ������ (��� ��������)

        private Socket socket; // ����� - �������� �� ����������� � �������
        private ClientServerChannel channel;
        
        private ClientThreadStatus status = ClientThreadStatus.Waiting; // ���� ���������� �� �������� � ���������

        byte task;
        Integer[] token = new Integer[tokenSize];; 
        
        ClientThread(int ID){
            this.ID = ID;
            ID_str = String.valueOf(ID);
        }

        @Override
        public void run(){
            log("ClientThread " + ID_str + " is waiting");
            while (status != ClientThreadStatus.Shutting){
            	System.out.print("");
            	if (status == ClientThreadStatus.Running) { //�������� � ������������ ��������
            		 log("ClientThread " + ID_str + " is running");
                     try {
                     	channel = new ClientServerChannel(socket); // �������� ������ ��� ������ �������
                     	task = channel.readByte(); // ��������� ������
                     	
                     	
                     	//									//
                     	// | ��������� � ���������� ����� | //
                     	// v							  v	//
                     	
                     	if (task == 2) { // ������ - ������
                     		
                     		for (int i = 0; i < tokenSize; i++) {	//
                     			token[i] = channel.readInt();		// ��������� �������
                     		}										//
                     		
                     		// ��������� ������ TODO
                     		//
                     		
                     		int index = Main.data.searchToken(token); // ����� �������
                     		
                     		if (index != -1) {
                     			channel.writeByte(1); // �������� �������������� ������
                     			
                     			// ������ TODO
                     		}
                     		else {
                     			channel.writeByte(0); // �������� �������������� ������
                     		}
                     	}
                     	else { // ������ - ����, ����������� (0/1 ��������������)
                     		
                     		token = Main.tokenGen.getTokens(); // ��������� �������
                     		
                     		login = channel.readString();
                     		password = channel.readString();
                     		
                     		int index; // ��������� �����/�����������
                     		
                     		if (task == 0) {
                     			index = Main.data.sign_in(login, password); // ����
                     		}
                     		else {
                     			index = Main.data.sign_up(login, password, token); // �����������
                     		}
                     		
                     		if (index != -1) {
                     			channel.writeByte(1); // �������� �������������� ������
                     			
                     			for (int token: token) {		//
                     				channel.writeInt(token);	// �������� �������
                     			}								//
                     		}
                     		else {
                     			channel.writeByte(0); // �������� �������������� ������
                     		}
                     	}
                     	
                     	channel.flush(); // �������� �������� ���� ������
                     }
                     catch (IOException e){
                         e.printStackTrace();
                     }
                     status = ClientThreadStatus.Waiting; // ������ ��������

                     log("ClientThread " + ID_str + " is waiting");
                     
                     free_threads.add(ID); // ���������� ������ � ������ ���������
            	}
            }
            log("ClientThread " + ID_str + " is shutting down");
        }

        // ��������� ������� � ������ ���������
        void handle(Socket client){
            this.socket = client;
            status = ClientThreadStatus.Running;
        }

        // �������� ������
        void close(){
            log("Closing clientThread " + ID_str);
            status = ClientThreadStatus.Shutting;
            if (socket != null){
                try{
                    socket.close();
                }
                catch(Exception e){
                	log(e.toString());
                }
            }
        }
    }
}