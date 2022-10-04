package gr.server.data.global.helper.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import gr.server.application.RestApplication;
import gr.server.data.api.model.league.Leagues;
import gr.server.impl.client.MockApiClient;

public class MockApiDataFetchHelper {

	public static void fetchLeagues() {
		try {
			 Leagues leaguesFromFile = MockApiClient.getLeaguesFromFile();
			 leaguesFromFile.getData().forEach(l -> RestApplication.LEAGUES.put(l.getId(), l));
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			e.printStackTrace();
			System.out.println("LEAGUES ERROR");
		}
	}

}
