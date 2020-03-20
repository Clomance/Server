package main;
import main.Data.Request;

import static main.Main.tokenSize;
import static main.Main.log;
import static main.Main.connectionsLimit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;


// Флаг работы клиентских потоков (ClientThread)
enum ClientThreadStatus{
    Waiting,
    Running,
    Shutting
}

// Серверный поток, отвечает за обработку 
// подключений и передачу управления клиентским потокам (ClientThread)
public class ServerThread extends Thread {
    private Vector<Integer> free_threads = null; // Массив свободных потоков 
    ClientThread[] clientThreads = null; // Массив клиентских потоков

    private ServerSocket serverSocket = null; // Канал, к которому подключаются клиенты
    boolean running = false; // Флаг работы сервера: true - запущен, false - отключён
    
    Socket client;
    
    @Override
    public void run(){
        running = true;
        log("ServerThread started");
        try{
        	log("Binding...");
            
            serverSocket = new ServerSocket(Main.port, 10, Main.address); // Создание сервера, backlog - очередь ожидающий обработки клиентов
            serverSocket.setSoTimeout(500); // Установка времени ожидания подключения (в миллисекундах)

            free_threads = new Vector<>(connectionsLimit);      	//
            clientThreads = new ClientThread[connectionsLimit]; 	//
            for (int i = 0; i < connectionsLimit; i++){         	// Создание
                clientThreads[i] = new ClientThread(i);             // клиентских
                clientThreads[i].start();                           // потоков
                free_threads.add(i);                                //
            }                                                       //
            
            log("Success");
            log("Waiting for connections");
            
            // Цикл обработки подключений
            while (running){
            	
                try {
                    client = serverSocket.accept(); // Ожидание подключений
                    log("Connected");

                    try{
                        int last = free_threads.size() - 1;		// Выбор свободного
                        int index = free_threads.remove(last);  // потока и передача
                        clientThreads[index].handle(client);    // ему управления
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        client.close(); // Нет свободных потоков
                    }
                    log("Handled");
                }
                catch (SocketTimeoutException timeout) {
                	continue; // Ошибка - кончилось время ожидания подключения - продолжение ожидания
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
			serverSocket.close();	// Закрытие
		} catch (Exception e) {		// канала для
			e.printStackTrace();	// подключений
		}							//
        
        running = false;
        log("ServerThread stopped");
    }
    
    // Клиентский поток - обрабатывает запросы клиента
    public class ClientThread extends Thread {
    	private String login; // Логин клиента
    	private String password; // Пароль клиента
    	private int index; // Нормер профиля в базе данных
    	
        private int ID; // Номер в массиве потоков (ServerThread.clientThreads)
        String ID_str; // Номер в виде строки (для удобства)

        Socket socket; // Канал - к нему подключается клиент
        private ClientServerChannel channel; // Канал для передачи данных
        
        ClientThreadStatus status = ClientThreadStatus.Waiting; // Флаг работы

        private byte task; // Задача
        private Integer[] token = new Integer[tokenSize]; // Токен
        
        ClientThread(int ID){
            this.ID = ID;
            ID_str = String.valueOf(ID);
        }

        @Override
        public void run(){
            log("ClientThread " + ID_str + " is waiting");
            while (status != ClientThreadStatus.Shutting){
            	
            	System.out.print(""); // Чтобы оптимизация не сломала цикл
            	
            	if (status == ClientThreadStatus.Running) { // Выполнение задачи
            		
            		 log("ClientThread " + ID_str + " is running");
            		 
                     try {
                     	channel = new ClientServerChannel(socket); // Создание канала
                     	task = channel.readByte(); // Получение задачи
                     	
                     	//									//
                     	// | Обработка и выполнение задач | //
                     	// v							  v	//
                     	
                     	switch (task) {
                     		case 0: // Вход
                     			
                     			token = Main.tokenGen.getToken(); // Генерация токена
                     			
                     			login = channel.readString();		// Получение логина
                         		password = channel.readString();	// и пароля
                         		
                         		index = Main.data.sign_in(login, password); // Вход
                         		
                         		if (index == -1) {
                         			channel.writeByte(0); // Отправка отрицательного ответа
                         		}
                         		else {
                         			channel.writeByte(1); // Отправка положительного ответа
                         			
                             		for (int token: token) {		//
                         				channel.writeInt(token);	// Отправка токена
                         			}								//
                             		
                             		Data.History history = Main.data.getHistory(index);	// Оправка истории
                         			channel.writeHistory(history);						//
                         		}
                         		
                     			break;
                     			
                     		case 1: // Регистрация
                     			
                     			token = Main.tokenGen.getToken(); // Генерация токена
                     			
                     			login = channel.readString();		// Получение логина
                         		password = channel.readString();	// и пароля
                         		
                         		index = Main.data.sign_up(login, password, token); // Регистрация
                         		
                         		if (index == -1) {
                         			channel.writeByte(0); // Отправка отрицательного ответа
                         		}
                         		else {
                         			channel.writeByte(1); // Отправка положительного ответа
                         			
                         			for (int token: token) {		//
                         				channel.writeInt(token);	// Отправка токена
                         			}								//
                         		}
                         		
                     			break;
                     			
                     		case 2: // Расчёт
                     			
                     			for (int i = 0; i < tokenSize; i++) {	//
                         			token[i] = channel.readInt();		// Получение токенов
                         		}										//
                         		
                         		Request request = channel.readRequest(); // Получение данных для расчёта
                         		
                         		int index = Main.data.searchToken(token); // Проверка токена
                         		
                         		if (index == -1) {
                         			channel.writeByte(0); // Отправка отрицательного ответа
                         		}
                         		else {
                         			channel.writeByte(1); // Отправка положительного ответа
                         			
                         			channel.writeDouble(request.result); // Отпаравка результата
                         			
                         			Main.data.addRequest(index, request); // Сохранение запроса в истории
                         		}
                     			break;
                     			
                     		default:
                     			break;
                     	}
                     	
                     	channel.flush(); // Ожидания отправки данных
                     }
                     catch (IOException e){
                         e.printStackTrace();
                     }
                     status = ClientThreadStatus.Waiting; // Флаг ожидания

                     log("ClientThread " + ID_str + " is waiting");
                     
                     free_threads.add(ID); // Добавление текущего потока в массив свободных
            	}
            }
            log("ClientThread " + ID_str + " is shutting down");
        }

        // Получение управления клиентом
        void handle(Socket client){
            this.socket = client;
            status = ClientThreadStatus.Running;
        }
    }
}