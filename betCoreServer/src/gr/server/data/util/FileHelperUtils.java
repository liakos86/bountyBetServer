package gr.server.data.util;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileHelperUtils {
	
	/**
	 * Reads content from the provided filename
	 * and returns as stream.
	 * File with {@value filename} must be in the resources folder.
	 * 
	 * @param filename
	 * @return
	 */
	public static String readFromFile(String filename){
		StringBuilder result = new StringBuilder("");
		//Get file from resources folder
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File file = new File(classLoader.getResource(filename).getFile());
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}

			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result.toString();
		
	}

}
