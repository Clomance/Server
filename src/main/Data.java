package main;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Data {
	private ReentrantLock lock = new ReentrantLock(); // Флаг для синхронизации
	
	Vector<String> logins = new Vector<>(); // Логины
	Vector<String> passwords = new Vector<>(); // Пароли
	Vector<Integer[]> tokens = new Vector<>(); // Токены
	
	Vector<History> history = new Vector<>(); // История запросов
	
	// Количество профилей
	int len() {
		return logins.size();
	}
	
	// Добавиление профиля
	void addProfile(String login, String password, Integer[] token, History history) {
		logins.add(login);
		passwords.add(password);
		tokens.add(token);
		this.history.add(history);
	}
	
	// Авторизация
	int sign_in(String login, String password) {
		lock.lock();
		
		for (int i = 0; i < logins.size(); i++) {
			if (logins.get(i).compareTo(login) == 0) {
				if (passwords.get(i).compareTo(password) == 0) {
					lock.unlock();
					return i;
				}
				else {
					lock.unlock();
					return -1;
				}
			}
		}
		
		lock.unlock();
		return -1;
	}
	
	// Регистрация
	int sign_up(String login, String password, Integer[] token) {
		lock.lock();
		int len = logins.size();
		
		for (int i = 0; i < len; i++) {
			if (logins.get(i).compareTo(login) == 0) {
				lock.unlock();
				return -1;
			}
		}
		
		addProfile(login, password, token, new History(1)); // Создание нового профиля
		
		lock.unlock();
		return len;
	}
	
	// Установка токена
	void setToken(int index, Integer[] token) {
		tokens.set(index, token);
	}
	
	// Поиск и проверка токена
	int searchToken(Integer[] token) {
		lock.lock();
		
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
				lock.unlock();
				return i;
			}
		}
		
		lock.unlock();
		return -1;
	}
	
	History getHistory(int index) {
		return history.get(index);
	}
	
	void addRequest(int index, Request request) {
		history.get(index).add(request);
	}
	
	// Дата
	public static class Date{
		int year;
		int month;
		int day;
		
		Date(int year, int month, int day){
			this.year = year;
			this.month = month;
			this.day = day;
		}
	}
	
	// Запрос
	public static class Request{
		double deposit;
		double percents;
		Date[] period;
		byte capitalization;
		// Доп. поля TODO
		double result;
		
		Request(double deposit, double percents, Date[] period, byte capitalization){
			this.deposit = deposit;
			this.percents = percents;
			this.period = period;
			this.capitalization = capitalization;
			
			result = compute();
		}

		Request(double deposit, double percents, Date[] period, byte capitalization, double result){
			this.deposit = deposit;
			this.percents = percents;
			this.period = period;
			this.capitalization = capitalization;
			
			this.result = result;
		}
		
		// Расчёт
		double compute() {
			
			switch (capitalization) {
				case 0: // Без капитализации
					//
					// Место для формулы TODO
					//
					break;
					
				case 1: // Ежемесячная капитализация
					//
					// Место для формулы TODO
					//
					break;
					
				case 2: // Ежеквартальная капитализация
					//
					// Место для формулы TODO
					//
					break;
				default:
					result = 0.0;
					break;
			}
			return result;
		}
	}
	
	// История запросов
	static class History extends Vector<Request>{
		History(int len) {
			
			this.ensureCapacity(len);
		}
		private static final long serialVersionUID = 1L;
		
	}
	
}
