package gr.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MockHttpHelper {

	public String mockGetContentWithHeaders(String uri) throws IOException {

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
