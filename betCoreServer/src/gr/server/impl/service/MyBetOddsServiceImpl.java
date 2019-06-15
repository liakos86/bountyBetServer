package gr.server.impl.service;

import gr.server.application.exception.UserExistsException;
import gr.server.data.user.model.User;
import gr.server.data.user.model.UserBet;
import gr.server.def.service.MyBetOddsService;
import gr.server.impl.client.MongoClientHelperImpl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@Path("/betServer")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MyBetOddsServiceImpl 
implements MyBetOddsService {

	
	@Override
	@GET
    @Path("/{id}/myOpenBets")
	public List<UserBet> getMyOpenBets(@PathParam("id") String id) {
		return new MongoClientHelperImpl().getBetsForUser(id);
	}
	
	@Override
	@POST
    @Path("/placeBet")
	@Consumes(MediaType.APPLICATION_JSON)
	public String placeBet(InputStream incomingStream) {
		
		StringBuilder userPredictionBuilder = new StringBuilder();
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingStream));
			String line = null;
			while((line = in.readLine()) != null){
				userPredictionBuilder.append(line);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		UserBet bet = new Gson().fromJson(userPredictionBuilder.toString(),
				new TypeToken<UserBet>() {}.getType());
		bet = new MongoClientHelperImpl().placeBet(bet);
		
		return new Gson().toJson(bet);
		
	}
	
//	@Override
//	@POST
//    @Path("/placeBet/{eventId}/{userId}/{prediction}/{amount}")
//	public Document placeBet(@PathParam("eventId") String eventId, @PathParam(Fields.USER_ID) String userId, @PathParam("prediction") String prediction, @PathParam("amount") Integer amount) {
//		UserPrediction userPrediction = new UserPrediction();
//		userPrediction.setBetAmount(amount);
//		userPrediction.setEventId(eventId);
//		userPrediction.setStatus(BetStatus.PENDING);
//		userPrediction.setUserId(userId);
//		userPrediction.setPrediction(prediction);
//		return new MongoClientHelperImpl().placePrediction(userPrediction);
//		
//	}
	
	@Override
	@POST
    @Path("/createUser")
	@Consumes(MediaType.APPLICATION_JSON)
	public String createUser(InputStream incomingStream) throws UserExistsException {
		
		StringBuilder userBuilder = new StringBuilder();
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingStream));
			String line = null;
			while((line = in.readLine()) != null){
				userBuilder.append(line);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		User user = new Gson().fromJson(userBuilder.toString(),
				new TypeToken<User>() {}.getType());
		
		return new Gson().toJson(new MongoClientHelperImpl().createUser(user));
	}
	
	

//	@Override
//	@GET
//    @Path("/{id}/delete")
//	public Response deletePerson(@PathParam("id") int id) {
//		Response response = new Response();
//		if(persons.get(id) == null){
//			response.setStatus(false);
//			response.setMessage("Person Doesn't Exists");
//			return response;
//		}
//		persons.remove(id);
//		response.setStatus(true);
//		response.setMessage("Person deleted successfully");
//		return response;
//	}
//
	@Override
	@GET
	@Path("/{id}/get")
	public String getUser(@PathParam("id") String id) {
		return new Gson().toJson(new MongoClientHelperImpl().getUser(id));
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public String getLeagues(){
		return  new Gson().toJson(new MongoClientHelperImpl().getLeagues()); 
	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeaderBoard")
	public String getLeaderBoard(){
		return  new Gson().toJson(new MongoClientHelperImpl().retrieveLeaderBoard()); 
	
	}

}
