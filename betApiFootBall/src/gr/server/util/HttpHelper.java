package gr.server.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import gr.server.data.constants.SportScoreApiConstants;

public class HttpHelper {
	
	public String fetchGetContent(String uri) throws IOException {
        URL url = new URL(uri);
        final int OK = 200;
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        int responseCode = connection.getResponseCode();
        if (responseCode == OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response2 = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response2.append(inputLine);
            }
            in.close();
            return response2.toString();
        }
        return null;
    }
	
	public String fetchGetContentWithHeaders(String uri) throws IOException {
		
        URL url = new URL(uri);
        final int OK = 200;
        
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty(SportScoreApiConstants.RAPID_API_HEADER_HOST_KEY, SportScoreApiConstants.RAPID_API_HEADER_HOST_VALUE);
        connection.addRequestProperty(SportScoreApiConstants.RAPID_API_HEADER_KEY, SportScoreApiConstants.RAPID_API_HEADER_VALUE);
        int responseCode = connection.getResponseCode();
        if (responseCode == OK) {
        	
        	InputStream decompressedStream = StreamHelper.decompressStream(connection.getInputStream());
        	
            BufferedReader in = new BufferedReader(new InputStreamReader(decompressedStream));
            String inputLine;
            StringBuffer response2 = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response2.append(inputLine);
            }
            in.close();
            return response2.toString();
        }
        return null;
    }
	
}
