package main;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Data {
	private ReentrantLock lock = new ReentrantLock(); // Флаг для синхронизации
	
	private Vector<String> logins = new Vector<>(); // Логины
	private Vector<String> passwords = new Vector<>(); // Пароли
	private Vector<Integer[]> tokens = new Vector<>(); // Токены
	
//	private Vector<Double> percents = new Vector<>();
//	private Vector<Double> profit = new Vector<>();
//	private Vector<Double> ggg2 = new Vector<>();
//	private Vector<Double> ggg = new Vector<>();
	
	// Авторизация
	int sign_in(String login, String password) {
		for (int i = 0; i < logins.size(); i++) {
			if (logins.get(i).compareTo(login) == 0) {
				if (passwords.get(i).compareTo(password) == 0) {
					return i;
				}
				else {
					return -1;
				}
			}
		}
		return -1;
	}
	
	// Регистрация
	int sign_up(String login, String password, Integer[] token) {
		lock.lock();
		
		for (int i = 0; i < logins.size(); i++) {
			if (logins.get(i).compareTo(login) == 0) {
				return -1;
			}
		}
		int len = logins.size();
		
		logins.add(login);
		passwords.add(password);
		tokens.add(token);
		
		lock.unlock();
		
		return len;
	}
	
	void setToken(int index, Integer[] token) {
		tokens.set(index, token);
	}
	
	// Поиск и проверка токенов
	int searchToken(Integer[] token) {
		for (int i = 0; i < tokens.size(); i++) {
			Integer[] tmp = tokens.get(i);
			int counter = 0;
			for (int j = 0; j < tmp.length; j++) {
				if (tmp[j] != token[j]) {
					break;
				}
				counter ++;
			}
			if (counter == tmp.length) {
				return i;
			}
		}
		return -1;
	}
}
