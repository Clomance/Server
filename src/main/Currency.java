package main;

import java.net.URLConnection;
import java.net.URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Currency {
	double USD = 0.0;
	double EUR = 0.0;
	
	public boolean update() {
		URL url;
		try {
			url = new URL("http://www.cbr.ru/currency_base/daily/");
			URLConnection connection = url.openConnection();
				
			InputStreamReader input = new InputStreamReader(connection.getInputStream());
			
			BufferedReader reader = new BufferedReader(input);
			
			String line = reader.readLine();
			
			while (line != null) {
				if (line.contains("EUR")) {
					
					EUR = readDouble(reader);
				}
				else if(line.contains("USD")) {

					USD = readDouble(reader);

				}
				
				line = reader.readLine();
			}
			return true;
			
		} 
		catch (IOException e) {
			Main.log("Проблемы с соединением");
			return false;
		}
	}
	
	static double readDouble(BufferedReader reader){
		double num = 0.0;
		String line = "";
		try {
			for (int i = 0; i < 3; i++) {
				line = reader.readLine();
			}
			
			line = line.trim();
			
			int len = line.length();
			
			String trim = line.substring(4,len - 5);
			
			String str = trim .replace(",",".");

			num = Double.parseDouble(str);
			
		}
		catch (Exception e) {
			Main.log("Ошибка во время парсинга сайта");
		}
		return num;
	}
}
