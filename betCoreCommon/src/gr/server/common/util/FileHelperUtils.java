package gr.server.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FileHelperUtils {
	
//	/**
//	 * Reads content from the provided filename
//	 * and returns as stream.
//	 * File with {@value filename} must be in the resources folder.
//	 * 
//	 * @param filename
//	 * @return
//	 */
//	public static String readFromFile(String filename){
//		StringBuilder result = new StringBuilder("");
//		//Get file from resources folder
//		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//		File file = new File(classLoader.getResource(filename).getFile());
//		try {
//			Scanner scanner = new Scanner(file);
//			while (scanner.hasNextLine()) {
//				String line = scanner.nextLine();
//				result.append(line).append("\n");
//			}
//
//			scanner.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		return result.toString();
//		
//	}
	
	public String getFileContents(String uri) throws IOException {

		String output = "";

		InputStream is = getClass().getClassLoader().getResourceAsStream(uri);

		try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(streamReader)) {

			String line = null;
			while ((line = reader.readLine()) != null) {
				output += line;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

}
