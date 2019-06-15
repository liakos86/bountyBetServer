package gr.server.client;

import gr.server.data.api.model.league.League;
import gr.server.impl.client.ApiFootballClient;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.junit.Test;

public class TestApiFootballClient {

	@Test
	public void test() throws IOException, ParseException {
				List<League> leagues = ApiFootballClient.getLeagues();
	}
	
}
