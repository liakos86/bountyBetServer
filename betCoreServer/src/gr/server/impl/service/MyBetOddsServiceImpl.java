package gr.server.impl.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.application.exception.UserExistsException;
import gr.server.data.bet.enums.BetStatus;
import gr.server.data.bet.enums.PredictionStatus;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;
import gr.server.data.user.model.objects.UserPrediction;
import gr.server.def.service.MyBetOddsService;
import gr.server.impl.client.MongoClientHelperImpl;


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
	
//	@Override
//	@POST
//    @Path("/placeBet")
//	@Consumes(MediaType.APPLICATION_JSON)
//	public String placeBet(InputStream incomingStream) {
//		
//		StringBuilder userPredictionBuilder = new StringBuilder();
//		try{
//			BufferedReader in = new BufferedReader(new InputStreamReader(incomingStream));
//			String line = null;
//			while((line = in.readLine()) != null){
//				userPredictionBuilder.append(line);
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		
//		UserBet bet = new Gson().fromJson(userPredictionBuilder.toString(),
//				new TypeToken<UserBet>() {}.getType());
//		bet = new MongoClientHelperImpl().placeBet(bet);
//		
//		return new Gson().toJson(bet);
//		
//	}
	
	@Override
	@POST
    @Path("/placeBet")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response placeBet(String userBet) {
		UserBet newBet =  new Gson().fromJson(userBet,
				new TypeToken<UserBet>() {}.getType());
		
		/**
		 * TODO: validations
		 */
		
		newBet.setBetStatus(BetStatus.PENDING);
		for (UserPrediction betPrediction: newBet.getPredictions()) {
			betPrediction.setPredictionStatus(PredictionStatus.PENDING);
		}
		
		newBet = new MongoClientHelperImpl().placeBet(newBet);
		
		return Response.ok(new Gson().toJson(newBet)).build();		
	}
	
//	@Override
//	@POST
//    @Path("/placeBet/{userId}/{predictions}/{amount}")
//	public UserBet placeBet(@PathParam(Fields.USER_ID) String userId, @PathParam("predictions") List<UserPrediction> predictions, @PathParam("amount") double amount) {
//		UserBet userBet = new UserBet();
//		userBet.setBetAmount(amount);
//		userBet.setBetStatus(BetStatus.PENDING.getCode());
//		userBet.setMongoUserId(userId);
//		userBet.getPredictions().addAll(predictions);
//		return new MongoClientHelperImpl().placeBet(userBet);
//	}
	
//	@Override
//	@POST
//    @Path("/createUser")
//	@Consumes(MediaType.APPLICATION_JSON)
//	public String createUser(InputStream incomingStream) throws UserExistsException {
//		
//		StringBuilder userBuilder = new StringBuilder();
//		try{
//			BufferedReader in = new BufferedReader(new InputStreamReader(incomingStream));
//			String line = null;
//			while((line = in.readLine()) != null){
//				userBuilder.append(line);
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		
//		User user = new Gson().fromJson(userBuilder.toString(),
//				new TypeToken<User>() {}.getType());
//		
//		return new Gson().toJson(new MongoClientHelperImpl().createUser(user));
//	}
	
	@Override
	@POST
    @Path("/createUser")
	@Consumes("application/json; charset=UTF-8")
	@Produces("application/json; charset=UTF-8")
	public Response createUser(String user) throws UserExistsException {
		User newUser = new Gson().fromJson(user, new TypeToken<User>() {}.getType());
		newUser = new MongoClientHelperImpl().createUser(newUser);
		return Response.ok(new Gson().toJson(newUser)).build();	
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
	@Produces(MediaType.APPLICATION_JSON)
	@Path("getUser/{id}")
	public Response getUser(@PathParam("id") String id) {
		return Response.ok(new Gson().toJson(new MongoClientHelperImpl().getUser(id))).build();	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeagues")
	public String getLeagues(){
		return  new Gson().toJson(new MongoClientHelperImpl().getMongoLeagues()); 
	
	}
	
	@Override
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getLeaderBoard")
	public String getLeaderBoard(){
		return  new Gson().toJson(new MongoClientHelperImpl().retrieveLeaderBoard()); 
	
	}

}
