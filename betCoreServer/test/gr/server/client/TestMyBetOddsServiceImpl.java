package gr.server.client;

import gr.server.data.api.model.league.Country;
import gr.server.data.user.model.User;
import gr.server.impl.service.MyBetOddsServiceImpl;
import gr.server.util.HttpHelper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestMyBetOddsServiceImpl {
	
	MyBetOddsServiceImpl service = new MyBetOddsServiceImpl();
	
	//@Test
	public void testGetCountries() throws IOException{
		
		String countriesUrl = "http://localhost:8080/betServer/ws/betServer/getCountries";
		String countries = new HttpHelper().fetchGetContent(countriesUrl);
		List<Country> countriesList = new Gson().fromJson(countries, new TypeToken<List<Country>>() {
        }.getType());
		System.out.println(countriesList.get(0).getCountryName());
	}
	
	//@Test
	public void test() throws ParseException{
		String f = "yyyy-MM-dd HH:mm";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(f);
		String ff = "2018-04-22";
		String h = " 15:00";
		
		Date parse = simpleDateFormat.parse(ff+h);
		
		System.out.println(parse.after(new Date()));
		 
	}
	
	@Test
	public void testGetUser(){
		String userId = "5ce46508830a771083e0e5a2";
		String userString = service.getUser(userId);
		User user = new Gson().fromJson(userString, new TypeToken<User>(){}.getType());
		System.out.println(user.getUsername());
	}

}
