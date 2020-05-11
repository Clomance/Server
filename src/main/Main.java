package main;
import main.ServerThread.ClientThread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Scanner;


public class Main {
	static InetAddress address; // Адрес сервера
	static int port = 8080; // Порт сервера
	
	static ServerThread serverThread; // Серверный поток (текущий поток - консоль)
	static int connectionsLimit = 5; // Максимальное количество подключений
	
	final public static int tokenSize = 2; // Размер токена
	static TokenGenerator tokenGen = new TokenGenerator(); // Генератор токенов

	static Data data = new Data(); // Данные пользователей
	
	static Currency currency = new Currency();
	
	public static void main(String[] args) {
		{
			FileSystem files = new FileSystem();
			if (!files.loadData()) {
				log("FileSystemError");
				return;
			}
			if (!files.loadSettings()) {
				try {
					address = InetAddress.getByName("192.168.0.101");
				} catch (UnknownHostException ignored) {}
			}
		}
		
		log("Обновление курса валют");
		if (currency.update()) {
			log("Обновлёно");
		}
		else {	
			log("Ошибка");
			return;
		}
		
		Scanner scan = new Scanner(System.in);
		String line = null;
		
		log("КОНСОЛЬ СЕРВЕРА");
		
		// Цикл консоли
		while (true) {
			try {
				line = scan.nextLine().trim();
				String[] line_args = line.split(" ");
				int len = line_args.length;
				
				for (int i = 0; i < len; i++) {
					line_args[i] = line_args[i].trim(); // Удаление пробелов по бокам строки
				}
				
				if (len > 0) {
					switch (line_args[0]) {
					
						case "помощь":
							log("   запустить\n   установить\n   инфо\n   остановить \n   показать\n   обновить");
							break;
							
						case "запустить":
							if (len > 1) {
								log("Команда не принимает аргументы");
								continue;
							}
							if (serverThread != null && serverThread.running) {	//
								log("Ошибка: сервер уже запущен");				// Игноривание команды,
								continue;										// если сервер запущен
							}													//
							serverThread = new ServerThread();
							serverThread.start();
							break;
							
						// Настройка
						case "установить":
							if (len > 2) {
								
								if (serverThread != null && serverThread.running) {			//
									log("Ошибка: нельзя изменить, когда сервер запущен");	// Игноривание команды,
									continue;												// если сервер запущен
								}															//
								
								switch (line_args[1]) {
									case "адрес": // Установка адреса сервера
										line = line_args[2].trim();
										address = InetAddress.getByName(line);
										break;
										
									case "порт": // Установка порта сервера
										line = line_args[2].trim();
										port = Integer.parseInt(line, 10);
										break;
										
									case "колВоПодключений": // Установка максимального количества подключений
										line = line_args[2].trim();
										connectionsLimit = Integer.parseInt(line, 10);
										break;
								}
							}
							else {
								log("установить [поле] [значение]\n" + "Поля: адрес, порт, колВоПодключений");
							}
							
							break;
						// Основная инфа о сервере
						case "инфо":
							log("Адрес сервера: " + address.getHostAddress());
							log("Порт сервера: " + port);
							log("Максимальное количество подключений: " + connectionsLimit);
							break;
						
						// Показать определённую информацию сервера
						case "показать":
							if (len > 1) {
								switch (line_args[1]) {
									case "главное":
										
										if (data.len() == 0) {
											log("Нет данных");
											break;
										}
				
										for (int i = 0; i < data.len(); i++) {
											System.out.printf("-> %d\n логин - %s\n пароль - %s\n", i, data.logins.get(i), data.passwords.get(i));
										}
										break;
										
									case "токены":
										if (data.len() == 0) {
											log("Нет данных");
											break;
										}
				
										for (int i = 0; i < data.len(); i++) {
											Integer[] token = data.tokens.get(i);
											System.out.printf("-> %d\n логин - %s\n токен - %d, %d\n", i, data.logins.get(i), token[0], token[1]);
										}
										break;
										
									case "авторов":
										log("<---> Оскар Хисматуллин - основа сервера\n<---> Станислав Короленко - консоль сервера\n<---> Илья Исаев - функции расчёта\n<---> Тимур Шайхинуров - тестирование и отчётная деятельность");
										break;
										
									case "валюту":
										
										break;
										
									default:
										log("Не верный аргумент");
										break;
								}
							}
							else {
								log("показать [аргуменет]\n" 
										+ "аргументы:\n -> главное - логины и пароли\n"
										+ "токены -> токены"
										+ "авторов -> авторы сервера"
										+ "валюта -> текущий курс вылют");
							}
							
							break;
							
						case "обновить":
							if (len < 3) {
								log("обновить [поле]\nполя:\nкурс валют");
								continue;
							}
							if (line_args[1].compareTo("курс") == 0 && line_args[2].compareTo("валют") == 0) {
								log("Обновление валюты");
								if (currency.update()) {
									currency.last_update = Instant.now();
									log("Обновлено");
								}
								else {
									log("Ошибка");
								}
							}
							else {
								log("Не верный аргумент");
							}
							
							break;
							
						case "остановить":
							if (len > 1) {
								log("Команда не принимает аргументы");
								continue;
							}
							log("Завершение...");
							scan.close();
							
							stopServerThread();
							
							FileSystem files = new FileSystem();
							files.save();
							files.saveSettings();
							
							log("Сервер остановлен");
							return;
							
						case "crocoDev":
							log(croco);
							break;
							
						default:
							log("Нет такой команды");
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
			
			log("Закрытие клиентских потоков");
			
			// Перебор всех потоков - передача сообщения о закрытии
	        for (ClientThread client: serverThread.clientThreads) {
	        	log("Закрытие клиентского потока " + client.ID_str);
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
	
	final static String croco = "                            ████████████████████              \r\n" + 
			"                              ██░░░░░░░░░░░░░░░░██████        \r\n" + 
			"                                ████▒▒▓▓██░░░░░░░░░░░░██      \r\n" + 
			"                                          ██▒▒██░░░░░░░░██    \r\n" + 
			"                                                ██░░░░░░██    \r\n" + 
			"                                                ██░░░░░░░░██  \r\n" + 
			"                                                  ██░░░░░░██  \r\n" + 
			"                                                  ██░░░░░░██  \r\n" + 
			"                                                  ░░░░░░  ██  \r\n" + 
			"                                                  ▒▒░░░░░░░░██\r\n" + 
			"                                                    ██░░░░░░██\r\n" + 
			"██████                                              ██░░░░░░██\r\n" + 
			"██░░████            ████████                        ██░░░░░░██\r\n" + 
			"██░░░░░░████████████▒▒░░██░░██                  ████░░░░░░░░██\r\n" + 
			"  ████░░░░░░░░░░░░██░░░░██░░██████          ████░░░░░░░░░░░░▒▒\r\n" + 
			"  ██  ████░░░░░░░░░░████░░░░░░░░░░██████▓▓▒▒░░░░░░░░░░░░░░░░▒▒\r\n" + 
			"      ██  ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██\r\n" + 
			"          ██  ████░░░░░░██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██\r\n" + 
			"              ██  ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██\r\n" + 
			"                      ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██  \r\n" + 
			"                      ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██  \r\n" + 
			"                ██    ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██  \r\n" + 
			"    ██  ██  ██  ██████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░██  \r\n" + 
			"████████████████░░░░░░░░░░██░░░░░░░░██░░░░░░░░░░██░░░░░░░░██  \r\n" + 
			"██░░░░░░░░░░░░░░░░░░░░░░░░██░░░░░░░░██░░░░░░░░░░██░░░░░░░░██  \r\n" + 
			"  ██████████████████████████░░░░░░░░██████████████░░░░░░░░██  \r\n" + 
			"                            ██░░░░██              ██░░░░██    \r\n" + 
			"                        ██████░░░░██          ▒▒████░░░░██    \r\n" + 
			"                      ▓▓░░░░░░░░██          ▒▒░░░░░░░░██      \r\n" + 
			"                      ████████████          ████████████  ";
	
}
