package main;
import static main.Main.tokenSize;

import java.util.Random;

public class TokenGenerator {
	Random random = new Random(0);
	
	Integer[] getToken() {
		Integer[] array = new Integer[tokenSize];
		
		for (int i = 0; i < tokenSize; i++) {
			array[i] = random.nextInt();
		}
		
		return array;
	}
}