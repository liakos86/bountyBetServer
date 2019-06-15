package gr.server.mongo.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Executor<T> {

    private TypeToken<T> responseType;

    public Executor(TypeToken<T> responseType) {
        this.responseType = responseType;
    }

    public T execute(String json) {
        return new Gson().fromJson(json, responseType.getType());
    }


}