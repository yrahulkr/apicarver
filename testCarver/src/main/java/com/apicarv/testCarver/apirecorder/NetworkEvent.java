package com.apicarv.testCarver.apirecorder;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class NetworkEvent {
	private int id;
	protected String requestId;
	protected EventClazz clazz;
	protected MethodClazz method;
	protected String requestUrl;
	protected List<Header> headers;
	protected transient String data;
	protected String postData;
	protected String resourceType;
	private String message;

	public enum EventClazz {
		RequestWillBeSent, RequestWillBeSentExtraInfo, ResponseReceived, ResponseReceivedExtraInfo, DataReceived, LoadingFailed, LoadingFinished, Probe
	}
	public enum MethodClazz {
		GET, POST, PUT, DELETE, OPTIONS, HEAD, UNKNOWN, PATCH, TRACE
	}

	public NetworkEvent(MethodClazz method, String requestUrl, List<Header> response) {
		this.method = method;
		this.requestUrl = requestUrl;
		this.headers = response;
	}

	
	public NetworkEvent(int id) {
		this.id = id;
	}

	public NetworkEvent(MethodClazz method2, String requestUrl2) {
		this.method = method2;
		this.requestUrl = requestUrl2;
	}
	
	public int getId() {
		return id;
	}


	public MethodClazz getMethod() {
		return method;
	}

	public void setMethod(MethodClazz method) {
		this.method = method;
	}
	
	public static MethodClazz getMethodClazz(String methodName) {
		if(methodName == null) {
			return MethodClazz.UNKNOWN;
		}
		switch(methodName.trim().toUpperCase()) {
		case "GET":
			return MethodClazz.GET;
		case "PUT":
			return MethodClazz.PUT;
		case "DELETE":
			return MethodClazz.DELETE;
		case "OPTIONS":
			return MethodClazz.OPTIONS;
		case "head":
			return MethodClazz.HEAD;
		case "POST":
			return MethodClazz.POST;
		case "PATCH":
			return MethodClazz.PATCH;
		case "TRACE":
			return MethodClazz.TRACE;
		default:
			return MethodClazz.UNKNOWN;
		}
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public String getData() {
		return data;
	}

	public EventClazz getClazz() {
		return clazz;
	}

	public void setData(String dataResponse) {
		this.data = dataResponse;
	}

	public void setClazz(EventClazz clazz) {
		this.clazz = clazz;
	}


	public void setPostData(String postData) {
		this.postData = postData;
	}

	public String getPostData() {
		return postData;
	}

	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public static EventClazz getEventClazz(String clazz2) {
		switch (clazz2) {
		case "ResponseReceived":
			return EventClazz.ResponseReceived;
		case "ResponseReceivedExtraInfo":
			return EventClazz.ResponseReceivedExtraInfo;
		case "RequestWillBeSent":
			return EventClazz.RequestWillBeSent;

		case "RequestWillBeSentExtraInfo":
			return EventClazz.RequestWillBeSentExtraInfo;
		case "DataReceived":
			return EventClazz.DataReceived;
		case "LoadingFinished":
			return EventClazz.LoadingFinished;
		case "LoadingFailed":
			return EventClazz.LoadingFailed;
		default:
			return null;
		}
	}

	public String getRequestId() {
		return requestId;
	}
	
	public void transferFields(NetworkEvent other) {
		other.clazz = this.clazz;
		other.data = this.data;		
		other.method = method;
		other.requestId = requestId;
		other.requestUrl = requestUrl;
		other.message = message;
		other.resourceType = resourceType;
		/*
		if(fullResponse!=null) {
			clone.fullResponse = new HashMap<String, Object>();
			for(String key: fullResponse.keySet()) {
				clone.fullResponse.put(key, fullResponse.get(key));
			}
		}*/
		
		if(headers!=null) {
			other.headers = new ArrayList<Header>();
			for(Header existing: headers) {
				other.headers.add(new BasicHeader(existing.getName(), existing.getValue()));
			}
		}
		
		if(postData!=null) {
			/*clone.postData = new ArrayList<>(); 
			for(PostDataEntry entry: postData) {
				clone.postData.add(entry);
			}*/
			other.postData = this.postData;
		}
		
	}
	
	public NetworkEvent clone(int newId) {
		NetworkEvent clone = new NetworkEvent(newId);
		transferFields(clone);;
		return clone;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	@Override
	public String toString() {
		return this.clazz  + " - " + method + " - " + requestUrl;
	}



	public void setMessage(String reasonPhrase) {
		this.message = reasonPhrase;
	}


	public String getMessage() {
		// TODO Auto-generated method stub
		return this.message;
	}
}
