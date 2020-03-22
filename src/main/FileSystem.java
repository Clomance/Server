package main;

import main.Data.Date;
import main.Data.History;
import main.Data.Request;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileSystem {
	
	//Загрузка данных пользователей
	boolean loadData() {
		Main.log("Loading data");
		
		File loginsFile = new File("logins");
		File passwordsFile = new File("passwords");
		File historiesFile = new File("histories");
		
		boolean newFile;
		try {
			newFile = loginsFile.createNewFile();
			
			passwordsFile.createNewFile();
			
			historiesFile.createNewFile();
		} catch (Exception e) {
			Main.log(e.toString());
			return false;
		}
		
		if (newFile) {
			return true;
		}
		
		try {
			DataInputStream loginsStream = new DataInputStream(new FileInputStream(loginsFile));
			DataInputStream passwordsStream = new DataInputStream(new FileInputStream(passwordsFile));
			DataInputStream historiesStream = new DataInputStream(new FileInputStream(historiesFile));
			
			int len = loginsStream.readInt();
			
			for (int i = 0; i < len; i++) {
				String login = readString(loginsStream);
				String password = readString(passwordsStream);
				History history = readHistory(historiesStream);
				Integer[] token = Main.tokenGen.getToken();
				
				Main.data.addProfile(login, password, token, history);
			}
		} 
		catch (EOFException end) {
			
		}
		catch (SecurityException | IOException e) {
			Main.log(e.toString());
			return false;
		}
		
		Main.log("Loaded");
		return true;
	}
	
	void save() {
		Main.log("Saving data");
		try {
			DataOutputStream loginsStream = new DataOutputStream(new FileOutputStream("logins"));
			DataOutputStream passwordsStream = new DataOutputStream(new FileOutputStream("passwords"));
			DataOutputStream historiesStream = new DataOutputStream(new FileOutputStream("histories"));
			
			int len = Main.data.len();
			
			loginsStream.writeInt(len);
			
			for (int i = 0; i < len; i++) {
				writeString(loginsStream, Main.data.logins.get(i));
				writeString(passwordsStream, Main.data.passwords.get(i));
				writeHistory(historiesStream, Main.data.history.get(i));
			}
		} 
		catch (Exception e) {
			Main.log(e.toString());
			Main.log("Saving error");
		}
		Main.log("Saved");
	}
	
	String readString(InputStream file) throws IOException {
		int len = file.read();
		byte[] bytes = new byte[len];
		file.read(bytes, 0, len);
		return new String(bytes);
	}
	
	Date readDate(DataInputStream file) throws IOException{
		int year = file.readInt();
		int month = file.readInt();
		int day = file.readInt();
		return new Date(year, month, day);
	}
	
	Request readRequest(DataInputStream file) throws IOException  {
		double deposit = file.readDouble();
		double percents = file.readDouble();
		
		Date[] date = new Date[2];
		date[0] = readDate(file);
		date[1] = readDate(file);
		
		byte capitalization = file.readByte();
		
		double result = file.readDouble();
		return new Request(deposit, percents, date, capitalization, result);
	}
	
	History readHistory(DataInputStream file) throws IOException  {
		int len = file.readInt();
		History history = new History(len);
		for (int i = 0; i < len; i++) {
			Request e = readRequest(file);
			history.add(e);
		}
		return history;
	}
	
	void writeString(OutputStream file, String str) throws IOException{
		int len = str.length();
		file.write(len);
		file.write(str.getBytes(), 0, len);
	}
	
	void writeDate(DataOutputStream file, Date date) throws IOException{
		file.writeInt(date.year);
		file.writeInt(date.month);
		file.writeInt(date.day);
	}
	
	void writeRequest(DataOutputStream file, Request request) throws IOException{
		file.writeDouble(request.deposit);
		file.writeDouble(request.percents);
		
		writeDate(file, request.period[0]);
		writeDate(file, request.period[1]);
		
		file.writeByte(request.capitalization);
		
		file.writeDouble(request.result);
	}
	
	void writeHistory(DataOutputStream file, History history) throws IOException{
		int len = history.size();
		file.writeInt(len);
		
		for (int i = 0; i < len; i++) {
			writeRequest(file, history.get(i));
		}
	}
}
