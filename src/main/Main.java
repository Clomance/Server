package main;
import main.ServerThread.ClientThread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
	static InetAddress address; // Адрес сервера
	static int port = 8080; // Порт сервера
	
	static ServerThread serverThread; // Серверный поток (текущий поток - консоль)
	static int connectionsLimit = 5; // Максимальное количество подключений
	
	final public static int tokenSize = 2; // Размер токена
	static TokenGenerator tokenGen = new TokenGenerator(); // Генератор токенов

	static Data data = new Data(); // Данные пользователей
	
	
	public static void main(String[] args) {
		{
			FileSystem files = new FileSystem();
			if (!files.loadData()) {
				log("FileSystemError");
				return;
			}
		}
		
		try {
			address = InetAddress.getByName("192.168.0.101");
		} catch (UnknownHostException ignored) {}
		
		Scanner scan = new Scanner(System.in);
		String line = null;
		
		log("Server Console");
		
		// Цикл консоли
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
							if (serverThread != null && serverThread.running) {	//
								log("Error: server is already running");		// Игноривание команды,
								continue;										// если сервер запущен
							}													//
							serverThread = new ServerThread();
							serverThread.start();
							break;
							
						// Настройка
						case "set":
							if (len > 1) {
								
								if (serverThread != null && serverThread.running) {	//
									log("Error: server is running");				// Игноривание команды,
									continue;										// если сервер запущен
								}													//
								
								switch (line_args[1]) {
									case "ip":
										
										log("Enter ip (without port)");
										line = scan.nextLine().trim();
										address = InetAddress.getByName(line);
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
						// Основная инфа о сервере
						case "info":
							log("Server address: " + address.getHostAddress());
							log("Server port: " + port);
							log("Limit of connections: " + connectionsLimit);
							break;
						
						// Показать определённую информацию сервера
						case "show":
							if (len > 1) {
								switch (line_args[1]) {
									case "mains":
										if (data.len() == 0) {
											log("No data");
											break;
										}
				
										for (int i = 0; i < data.len(); i++) {
											System.out.printf("-> %d\n logins - %s\n password - %s\n", i, data.logins.get(i), data.passwords.get(i));
										}
										break;
									case "tokens":
										if (data.len() == 0) {
											log("No data");
											break;
										}
				
										for (int i = 0; i < data.len(); i++) {
											Integer[] token = data.tokens.get(i);
											System.out.printf("-> %d\n logins - %s\n token - %d, %d\n", i, data.logins.get(i), token[0], token[1]);
										}
										break;
									default:
										log("No such argumant");
										break;
								}
							}
							else {
								log("show [arg]\n" + "args:\n -> mains - logins and passwords");
							}
							
							break;
						case "stop":
							log("Stopping...");
							scan.close();
							
							stopServerThread();
							
							FileSystem files = new FileSystem();
							files.save();
							
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
	
	// Остановка всех потоков
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
			
			// Перебор всех потоков - передача сообщения о закрытии
	        for (ClientThread client: serverThread.clientThreads) {
	        	log("Closing clientThread " + client.ID_str);
	        	client.status = ClientThreadStatus.Shutting; // Установка флага в закрытие потока
	        	
	        	if (client.socket != null){
	                try{
	                	client.socket.close();
	                }
	                catch(Exception e){
	                	log(e.toString());
	                }
	            }
	        }
	        
	        // Цикл ожидания закрытия потоков
	        for (ClientThread client: serverThread.clientThreads) {
	        	try {
	                client.join(); // Ожидание завершения потока
	                log("Closed\n");
	            }
	            catch (InterruptedException e) {
	                log(e.toString());
	            }
	        }
		}
	}
	
	// Вывод в консоль
	public static void log(String str) {
		System.out.println(str);
	}
}
