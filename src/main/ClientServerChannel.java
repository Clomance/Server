package main;
import main.Data.Request;
import main.Data.Date;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientServerChannel {
	private DataOutputStream output;
    private DataInputStream input;

    ClientServerChannel(Socket socket) throws IOException{
        output = new DataOutputStream(socket.getOutputStream());    // Получение каналов
        input = new DataInputStream(socket.getInputStream());       // связи
    }

    // Отправка  данных //
    // | 			  | //
    // v			  v //
    void writeByte(int b) throws IOException{
        output.writeByte(b);
    }
    
    void writeInt(int Int) throws IOException{
        output.writeInt(Int);
    }
    
    void writeDouble(double d) throws IOException{
    	output.writeDouble(d);
    }

    void writeString(String str) throws IOException{
        output.writeByte(str.length());
        output.writeBytes(str);
    }
    
    void writeDate(Data.Date date) throws IOException{
    	output.writeInt(date.year);
    	output.writeByte(date.month);
    	output.writeByte(date.day);
    }
    
    void writeRequest(Request request) throws IOException{
    	output.writeDouble(request.deposit);
    	output.writeDouble(request.percents);
    	
    	writeDate(request.period[0]);
    	writeDate(request.period[1]);
    	
    	output.writeByte(request.capitalization);
    	
    	output.writeByte(request.currency);
    	
    	output.writeDouble(request.result);
    }
    
    void writeHistory(Data.History history) throws IOException{
    	output.writeInt(history.size());
    	for (Request request: history) {
    		writeRequest(request);
    	}
    }

    void flush() throws IOException{
        output.flush();
    }

    
    // Получение данных //
    // | 			  | //
    // v			  v //
    byte readByte() throws IOException{
        return input.readByte();
    }

    Boolean readBoolean() throws IOException{
        return input.readBoolean();
    }

    int readInt() throws IOException{
        return input.readInt();
    }
    
    String readString() throws IOException{
    	int len = input.readByte();
    	byte[] bytes = new byte[len];
    	input.readFully(bytes);
    	String str = new String(bytes);
    	return str;
    }
    
    Date readDate() throws IOException{
    	int year = input.readInt();
    	byte month = input.readByte();
    	byte day = input.readByte();
    	
    	return new Date(year, month, day);
    }
    
    Request readRequest() throws  IOException{
    	double deposit = input.readDouble();
    	double percents = input.readDouble();
    	
    	Date[] period = new Date[] {readDate(), readDate()};
    	
    	byte capitalization = input.readByte();
    	
    	byte currency = input.readByte();
    	
    	return  new Request(deposit, percents, period, capitalization, currency);
  
    }
}
