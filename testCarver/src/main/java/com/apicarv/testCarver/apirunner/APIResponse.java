package com.apicarv.testCarver.apirunner;

import java.util.ArrayList;
import java.util.List;

import com.apicarv.testCarver.apirecorder.LogEntry;
import com.apicarv.testCarver.apirecorder.NetworkEvent;
import com.apicarv.testCarver.apirecorder.ResponseEvent;

public class APIResponse {

	public enum Status {
		SUCCESS, FAILURE, SKIPPED, UNKNOWN
	}

	//TODO: Find an alternate name for checkpoint
	/**
	 * checkpoint operations - (POST, DELETE): other operations can be performed multiple times with the same result.
	 *  Requests that set-cookie can also be checkpoints
	 * @author apicarv
	 *
	 */
	public enum CheckPointType{
		OPERATION, COOKIE, BOTH, NONE
	}
	
	int id;
	Status status;
	CheckPointType checkPoint = CheckPointType.NONE;
	public CheckPointType getCheckPoint() {
		return checkPoint;
	}

	String message;
	NetworkEvent request;
	ResponseEvent response;
	
	private long duration;
	
	public APIResponse(int id) {
		this.id  = id;
	}

	public NetworkEvent getRequest() {
		return request;
	}

	public void setRequest(NetworkEvent request) {
		this.request = request;
	}

	public ResponseEvent getResponse() {
		return response;
	}

	public void setResponse(ResponseEvent response) {
		this.response = response;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getId() {
		return this.id;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public List<APIResponse> buildAPIResponses(int id, List<LogEntry> logEntries) {
		List<APIResponse> list = new ArrayList<APIResponse>();
		
		
		
		return list;
	}

	public void addCheckPoint(CheckPointType newCheck) {
		if(checkPoint == null){
			checkPoint = newCheck;
			return;
		}

		switch(checkPoint) {
		case NONE:
			checkPoint = newCheck;
			return;
		case BOTH: 
			//Nothing to do. 
			return;
		default:
			break;
		}
		
		if(checkPoint != newCheck) {
			checkPoint = CheckPointType.BOTH;
			return;
		}
		else {
			//ALready has been set.
			return;
		}
	}

	@Override
	public String toString() {
		String returnString = "" + status + " - ";
		if(request!=null){
			returnString += request.getClazz() + " - " + request.getMethod() + " - " + request.getRequestUrl();
		}
		if(response!=null){
			returnString += " - " + response.getStatus();
		}
		return returnString;
	}

}
