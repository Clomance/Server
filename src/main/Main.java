package main;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import main.ServerThread.ClientThread;

public class Main {
	static InetAddress ip; // ����� �������
	static int port = 8080; // ���� �������
	
	static ServerThread serverThread; // ������� ����� ������� (������� ����� - �������)
	static int connectionsLimit = 5; // ������������ ������������� ���������� �����������
	
	final public static int tokenSize = 2; // ������ �������
	static TokenGenerator tokenGen = new TokenGenerator(); // ��������� �������

	static Data data = new Data(); // ������ �������������
	
	
	public static void main(String[] args) {
		try {
			ip = InetAddress.getByName("192.168.0.100");
		} catch (UnknownHostException ignored) {}
		
		Scanner scan = new Scanner(System.in);
		String line = null;
		
		log("Server Console");
		while (true) {
			try {
				line = scan.nextLine().trim();
				String[] line_args = line.split(" ");
				int len = line_args.length;
				
				if (len > 0) {
					switch (line_args[0]) {
					
						case "help":
							log("   start\n   set\n   info\n   stop");
							break;
							
						case "start":
							if (serverThread != null && serverThread.running) {						//
								log("Error: server is already running");	// ������������� �������
								continue;									// ��� ���������� �������
							}												//
							serverThread = new ServerThread();
							serverThread.start();
							break;
							
						// ��������� 
						case "set":
							if (len > 1) {
								
								if (serverThread != null && serverThread.running) {				//
									log("Error: server is running");	// ������������� �������
									continue;							// ��� ���������� �������
								}										//
								
								switch (line_args[1]) {
									case "ip":
										
										log("Enter ip (without port)");
										line = scan.nextLine().trim();
										ip = InetAddress.getByName(line);
										break;
										
									case "port":
										
										log("Enter port");
										line = scan.nextLine().trim();
										port = Integer.parseInt(line, 10);
										break;
										
									case "conAmount":
										
										log("Enter limit of connections");
										line = scan.nextLine().trim();
										connectionsLimit = Integer.parseInt(line, 10);
										break;
								}
							}
							else {
								log("set [arg]\n" + "args: ip, port, conAmount");
							}
							
							break;
							
						case "info":
							log("Server address: " + ip.getHostAddress());
							log("Server port: " + port);
							log("Limit of connections: " + connectionsLimit);
							break;
							
						case "stop":
							log("Stopping...");
							scan.close();
							
							stopServerThread();
							
							log("Stopped");
							return;
							
						default:
							log("No such command");
							break;
					}
				}
			}
			catch (NumberFormatException | UnknownHostException e) {
				log(e.toString());
			}
		}
	}
	
	// ��������� ���� �������
	private static void stopServerThread() {
		if (serverThread != null) {
			serverThread.running = false; 

			try {
				serverThread.join();
			}
			catch (InterruptedException e) {
				log(e.toString());
			}
			
			log("Closing clientThreads...");
			
	        for (ClientThread client: serverThread.clientThreads) { 	//
	        	log("Waiting to close");								//
	            client.close();											// ��������
	            try {													// ����
	                client.join();										// �������
	                log("Closed\n");									// ��������
	            } catch (InterruptedException e) {						// �����������
	                log(e.toString());									// (ClientThread)
	            }														//
	        }															//
		}
	}
	
	// ����� � �������
	public static void log(String str) {
		System.out.println(str);
	}
}
