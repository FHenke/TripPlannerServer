package sockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import api.utilities.TimeZoneInfo;
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
	
	public static Request jsonToRequest(String jsonRequest){
		Gson gson = new Gson();
		Request request = gson.fromJson(jsonRequest, Request.class);
		return request;
	}
	
	public static TimeZoneInfo jsonToTimeZoneInfo(String jsonTimeZoneInfo){
		Gson gson = new Gson();
		System.out.println(jsonTimeZoneInfo);
		TimeZoneInfo timeZoneInfo = gson.fromJson(jsonTimeZoneInfo, TimeZoneInfo.class);
		return timeZoneInfo;
	}
}
