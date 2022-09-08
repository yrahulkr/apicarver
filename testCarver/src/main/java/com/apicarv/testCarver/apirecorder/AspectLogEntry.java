package com.apicarv.testCarver.apirecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class AspectLogEntry extends NetworkEvent{
	private Map<String, Object> fullResponse;
	
	public AspectLogEntry(MethodClazz method, String requestUrl, Map<String, Object> fullResponse) {
		super(method, requestUrl);
		this.fullResponse = fullResponse;
	}
	
	public AspectLogEntry(int id) {
		super(id);
	}

	public Map<String, Object> getFullResponse() {
		return fullResponse;
	}

	public void setFullResponse(Map<String, Object> fullResponse) {
		this.fullResponse = fullResponse;
	}
	
	public AspectLogEntry clone() {
		AspectLogEntry clone = new AspectLogEntry(0);
		clone.clazz = this.clazz;
		clone.data = this.data;		
		clone.requestId = requestId;
		clone.requestUrl = requestUrl;
		clone.method = method;
		
		if(fullResponse!=null) {
			clone.fullResponse = new HashMap<String, Object>();
			for(String key: fullResponse.keySet()) {
				clone.fullResponse.put(key, fullResponse.get(key));
			}
		}
		
		if(headers!=null) {
			clone.headers = new ArrayList<Header>();
			for(Header existing: headers) {
				clone.headers.add(new BasicHeader(existing.getName(), existing.getValue()));
			}
		}
		
		if(postData!=null) {
			/*clone.postData = new ArrayList<>(); 
			for(PostDataEntry entry: postData) {
				clone.postData.add(entry);
			}*/
			clone.postData = this.postData;
		}
		
		return clone;
	}
}
