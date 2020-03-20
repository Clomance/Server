package main;

import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Data {
	private ReentrantLock lock = new ReentrantLock(); // Флаг для синхронизации
	
	private Vector<String> logins = new Vector<>(); // Логины
	private Vector<String> passwords = new Vector<>(); // Пароли
	private Vector<Integer[]> tokens = new Vector<>(); // Токены
	
	private Vector<History> history = new Vector<>(); // История запросов
	
	
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
		
		for (int i = 0; i < logins.size(); i++) {
			if (logins.get(i).compareTo(login) == 0) {
				lock.unlock();
				return -1;
			}
		}
		int len = logins.size();
		
		logins.add(login);			//
		passwords.add(password);	// Создание нового
		tokens.add(token);			// профиля
		history.add(new History()); //
		
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
		boolean capitalization;
		// Доп. поля TODO
		double result;
		
		Request(double deposit, double percents, Date[] period, boolean capitalization){
			this.deposit = deposit;
			this.percents = percents;
			this.period = period;
			this.capitalization = capitalization;
			
			result = compute();
		}
		
		// Расчёт
		double compute() {
			result = 0.0;
			//
			// Место для формулы TODO
			//
			return result;
		}
	}
	
	// История запросов
	static class History extends Vector<Request>{
		private static final long serialVersionUID = 1L;
		
	}
	
}
