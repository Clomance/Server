package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientServerChannel {
	DataOutputStream output;
    DataInputStream input;

    ClientServerChannel(Socket socket) throws IOException{
        output = new DataOutputStream(socket.getOutputStream());    // Получение каналов
        input = new DataInputStream(socket.getInputStream());       // связи
    }

    void writeByte(int b) throws IOException{
        output.writeByte(b);
    }

    void writeAll(byte[] bytes) throws IOException{
        output.write(bytes);
    }

    void writeInt(int Int) throws IOException{
        output.writeInt(Int);
    }

    void writeString(String str) throws IOException{
        output.writeByte(str.length());
        output.writeBytes(str);
    }

    void flush() throws IOException{
        output.flush();
    }

    byte readByte() throws IOException{
        return input.readByte();
    }
    
    void readExact(byte[] bytes) throws IOException{
        input.readFully(bytes);
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
}
