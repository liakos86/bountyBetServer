package gr.server.util;



import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

import gr.server.data.user.model.objects.UserBet;

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
	
	
    public String fetchPostContent(String uri, UserBet postObject) throws IOException {
        URL url = new URL(uri);
        final int OK = 200;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/json");
		
		String json = new Gson().toJson(postObject);
		OutputStream out = new BufferedOutputStream(connection.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.write(json);
		writer.flush();
		writer.close();
		out.close();
        
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
	

}
