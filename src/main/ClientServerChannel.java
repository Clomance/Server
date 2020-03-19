package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;

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
    	output.writeInt(date.month);
    	output.writeInt(date.day);
    }
    
    void writeRequest(Data.Request request) throws IOException{
    	output.writeDouble(request.deposit);
    	output.writeDouble(request.percents);
    	
    	writeDate(request.period[0]);
    	writeDate(request.period[1]);
    	
    	output.writeDouble(request.result);
    }
    
    void writeHistory(Data.History history) throws IOException{
    	output.writeInt(history.size());
    	for (Data.Request request: history) {
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
    
    Data.Date readDate() throws IOException{
    	int year = input.readInt();
    	int month = input.readInt();
    	int day = input.readInt();
    	
    	return new Data.Date(year, month, day);
    }
    
    Data.Request readRequest() throws  IOException{
    	double deposit = input.readDouble();
    	double percents = input.readDouble();
    	
    	Data.Date[] period = new Data.Date[2];
    	
    	period[0] = readDate();
    	period[1] = readDate();
    	
    	return  new Data.Request(deposit, percents, period);
  
    }
}
