package main;
import static main.Main.tokenSize;
import static main.Main.log;
import static main.Main.connectionsLimit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

// Флаг отвечающий за ожидание и обработку
enum ClientThreadStatus{
    Waiting,
    Running,
    Shutting
}

// Серверный потом обрабатывает подключения
public class ServerThread extends Thread {
    private Vector<Integer> free_threads = null; // Индексы свободных потоков
    ClientThread[] clientThreads = null; // Потоки для обработки подключений

    private ServerSocket serverSocket = null; // "Гнездо" сервера - к нему подключаются клиенты
    boolean running = false; // Флаг цикла обрабоки (работы сервера): true - работает, false - выключен

    @Override
    public void run(){
        running = true;
        log("ServerThread started");
        try{
        	log("Binding...");
            
            serverSocket = new ServerSocket(Main.port, 10, Main.ip); // Создание сервера, backlog - размер очереди ожидания обработки подключения
            serverSocket.setSoTimeout(500); // Установка ожидания подключения клиента (в миллисекундах)

            free_threads = new Vector<>(connectionsLimit);      	//
            clientThreads = new ClientThread[connectionsLimit]; 	//
            for (int i = 0; i < connectionsLimit; i++){         	// Создание потоков
                clientThreads[i] = new ClientThread(i);             // для обработки
                clientThreads[i].start();                           // подключений
                free_threads.add(i);                                //
            }                                                       //
            
            log("Success");
            log("Waiting for connections");
            // Цикл обработки подключений
            while (running){
            	
                try {
                    Socket client = serverSocket.accept(); // Ожидание подключений (блокирует поток)
                    log("Connected");

                    try{
                        int last = free_threads.size() - 1;         // Получение свободного
                        int index = free_threads.remove(last);      // потока и передача
                        clientThreads[index].handle(client);        // ему управления клиентом
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        client.close(); // Отключение клиента, если свободного потока нет
                    }
                    log("Handled");
                }
                catch (SocketTimeoutException timeout) {
                	continue; // Исключение из-за долгого ожидания подключений - продолжение ожидания
                }
                catch (IOException e){
                	log(e.toString());
                    break;
                }
            }
        }
        catch(IOException e){ // Ошибка при создании сервера
        	log(e.toString());
        }
        
        try {						//
			serverSocket.close();	// Отключение
		} catch (Exception e) {		// возможности
			e.printStackTrace();	// подключений
		}							//
        
        running = false;
        log("ServerThread stopped");
    }
    
    // Клиентский поток - поток для работы с подключённым клиентом
    public class ClientThread extends Thread {
    	private String login;
    	private String password;
    	
        private int ID; // номер потока в массиве потоков (ServerThread.clientThreads)
        private String ID_str; // номер в виде строки (для удобства)

        private Socket socket; // гнёздо - отвечает за подключение к серверу
        private ClientServerChannel channel;
        
        private ClientThreadStatus status = ClientThreadStatus.Waiting; // флаг отвечающий за ожидание и обработку

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
            	if (status == ClientThreadStatus.Running) { //Действия с подключённым клиентом
            		 log("ClientThread " + ID_str + " is running");
                     try {
                     	channel = new ClientServerChannel(socket); // Создание канала для обмена данными
                     	task = channel.readByte(); // Получение задачи
                     	
                     	
                     	//									//
                     	// | Обработка и выполнение задач | //
                     	// v							  v	//
                     	
                     	if (task == 2) { // Задача - расчёи
                     		
                     		for (int i = 0; i < tokenSize; i++) {	//
                     			token[i] = channel.readInt();		// Получение токенов
                     		}										//
                     		
                     		// Получение данных TODO
                     		//
                     		
                     		int index = Main.data.searchToken(token); // Поиск токенов
                     		
                     		if (index != -1) {
                     			channel.writeByte(1); // Отправка положительного ответа
                     			
                     			// Расчёт TODO
                     		}
                     		else {
                     			channel.writeByte(0); // Отправка отрицательного ответа
                     		}
                     	}
                     	else { // Задачи - вход, регистрация (0/1 соответственно)
                     		
                     		token = Main.tokenGen.getTokens(); // Генерация токенов
                     		
                     		login = channel.readString();
                     		password = channel.readString();
                     		
                     		int index; // Результат входа/регистрации
                     		
                     		if (task == 0) {
                     			index = Main.data.sign_in(login, password); // Вход
                     		}
                     		else {
                     			index = Main.data.sign_up(login, password, token); // Регистрация
                     		}
                     		
                     		if (index != -1) {
                     			channel.writeByte(1); // Отправка положительного ответа
                     			
                     			for (int token: token) {		//
                     				channel.writeInt(token);	// Отправка токенов
                     			}								//
                     		}
                     		else {
                     			channel.writeByte(0); // Отправка отрицательного ответа
                     		}
                     	}
                     	
                     	channel.flush(); // Ожидание отправки всех данных
                     }
                     catch (IOException e){
                         e.printStackTrace();
                     }
                     status = ClientThreadStatus.Waiting; // Статус ожидания

                     log("ClientThread " + ID_str + " is waiting");
                     
                     free_threads.add(ID); // Добавление потока в массив свободных
            	}
            }
            log("ClientThread " + ID_str + " is shutting down");
        }

        // Получение клиента и запуск обработки
        void handle(Socket client){
            this.socket = client;
            status = ClientThreadStatus.Running;
        }

        // Закрытие потока
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