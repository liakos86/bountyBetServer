package gr.server.mongo.util;

import java.util.ArrayList;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import gr.server.data.constants.MongoFields;
import gr.server.data.user.model.objects.User;
import gr.server.data.user.model.objects.UserBet;

public class Executor<T> {

    private TypeToken<T> responseType;

    public Executor(TypeToken<T> responseType) {
        this.responseType = responseType;
    }

    public T execute(String json) {
        return new Gson().fromJson(json, responseType.getType());
    }
    
    public void tidy(T object, Document document) {
    	if (object instanceof User){
			((User) object).setMongoId(document.getObjectId(MongoFields.MONGO_ID).toString()); 
			((User) object).setUserBets(new ArrayList<>());
		}else if (object instanceof UserBet){
			 ((UserBet)object).setMongoId(document.getObjectId(MongoFields.MONGO_ID).toString());
		}
    }

}