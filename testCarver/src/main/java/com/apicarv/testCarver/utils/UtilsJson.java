package com.apicarv.testCarver.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.apicarv.testCarver.apirunner.APIResponse;

public class UtilsJson {

	public static void exportFile(String htmlPath, String json) {
		FileWriter writer;
		try {
			writer = new FileWriter(new File(htmlPath));
			writer.write(json);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Can read a jsonfile with List of entries of NetworkEvent.Class
	 * 
	 * @param jsonFile
	 * @return
	 */
	public static List<LogEntry> importNetworkEventLog(String jsonFile) {
		Gson gson = new GsonBuilder().registerTypeAdapter(Header.class, new JsonDeserializer<Header>() {

			@Override
			public Header deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return new BasicHeader(json.getAsJsonObject().get("name").getAsString(),
						json.getAsJsonObject().get("value").getAsString());
			}
		}).create();

		Type typeOfObjectsList = new TypeToken<List<LogEntry>>() {
		}.getType();

		List<LogEntry> allLogEntries = null;
		try {
			allLogEntries = gson.fromJson(new FileReader(jsonFile), typeOfObjectsList);

		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allLogEntries;
	}

	/**
	 * Can read a jsonfile with List of entries of NetworkEvent.Class
	 * 
	 * @param jsonFile
	 * @return
	 */
	public static List<NetworkEvent> importGeneratedEvents(String jsonFile) {
		Gson gson = new GsonBuilder().registerTypeAdapter(Header.class, new JsonDeserializer<Header>() {

			@Override
			public Header deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return new BasicHeader(json.getAsJsonObject().get("name").getAsString(),
						json.getAsJsonObject().get("value").getAsString());
			}
		}).create();

		Type typeOfObjectsList = new TypeToken<List<NetworkEvent>>() {
		}.getType();

		List<NetworkEvent> allLogEntries = null;
		try {
			allLogEntries = gson.fromJson(new FileReader(jsonFile), typeOfObjectsList);

		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allLogEntries;
	}

	/**
	 * 
	 * @param jsonFile
	 * @return
	 */
	public static List<APIResponse> importAPIResponses(String jsonFile) {
		Gson gson = getAPIResponseDeserializer();

		Type typeOfObjectsList = new TypeToken<List<APIResponse>>() {
		}.getType();

		List<APIResponse> allLogEntries = null;
		try {
			allLogEntries = gson.fromJson(new FileReader(jsonFile), typeOfObjectsList);

		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allLogEntries;

	}

	public static List<APIResponse> importAPIResponsesFromString(String json) {
		Gson gson = getAPIResponseDeserializer();

		Type typeOfObjectsList = new TypeToken<List<APIResponse>>() {
		}.getType();

		List<APIResponse> allLogEntries = null;
		try {
			allLogEntries = gson.fromJson(json, typeOfObjectsList);

		} catch (JsonIOException | JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allLogEntries;

	}

	private static Gson getAPIResponseDeserializer() {
		Gson gson = new GsonBuilder().registerTypeAdapter(Header.class, new JsonDeserializer<Header>() {

			@Override
			public Header deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return new BasicHeader(json.getAsJsonObject().get("name").getAsString(),
						json.getAsJsonObject().get("value").getAsString());
			}
		}).create();
		return gson;
	}

}
