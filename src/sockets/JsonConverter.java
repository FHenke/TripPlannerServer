package sockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import utilities.Connection;
import utilities.Request;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

public class JsonConverter {

	
	public static String getJson(LinkedBlockingQueue<Connection> connectionList){
		
		Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new GsonUTCDateAdapter()).create();
        Type type = new TypeToken<LinkedBlockingQueue<Connection>>() {}.getType();
        String json = gson.toJson(connectionList, type);
		return json;
	}
	
	public static Request jasonToRequest(String jsonRequest){
		Gson gson = new Gson();
		/*GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setDateFormat("yyyy-mm-dd HH:MM");*/
		Request request = gson.fromJson(jsonRequest, Request.class);
		return request;
	}
}
