package com.apicarv.testCarver.openAPIGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicarv.testCarver.apirecorder.NetworkEvent.MethodClazz;
import com.apicarv.testCarver.utils.ResponseComparisonUtils;

public class URLNode {
	private static final Logger LOG = LoggerFactory.getLogger(URLNode.class);

	int pathIndex;
	String requestId;
	List<Integer> payloads = new ArrayList<>();
	List<Integer> candidateGroups = new ArrayList<>();
	private MethodClazz method;
	String payloadToCompare;
	String responseType;
	int stateBefore;
	int stateAfter;
	String event;
	List<Integer> visits = new ArrayList<>();
	String parentPath;
	private int id;
	
	public URLNode(String pathItem) {
		this.pathItem = pathItem;
		this.id = 0;
		this.requestId = "0000000";
		this.pathIndex = -1;
		this.parentPath = "";
		this.responseType = "root";
		this.setMethod(MethodClazz.UNKNOWN);
	}
	
	public URLNode(int id, String pathItem, int pathIndex, String parentPath) {
		this.id = id;
		this.pathIndex = pathIndex;
		this.pathItem = pathItem;
		this.parentPath = parentPath;
	}
	
	String pathItem;

	private int var = -1;

	public boolean isLeaf = false;

	
	public int getVar() {
		return var;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPathItem() {
		return pathItem;
	}
	public void setPathItem(String pathItem) {
		this.pathItem = pathItem;
	}
	public int getPathIndex() {
		return pathIndex;
	}
	public void setPathIndex(int pathIndex) {
		this.pathIndex = pathIndex;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public List<Integer> getPayloads() {
		return payloads;
	}
	public void setPayloads(List<Integer> payloads) {
		this.payloads = payloads;
	}
	public String getPayloadToCompare() {
		return payloadToCompare;
	}
	public void setPayloadToCompare(String payloadToCompare) {
		this.payloadToCompare = payloadToCompare;
	}
	public String getType() {
		return responseType;
	}
	public void setType(String type) {
		this.responseType = type;
	}
	public int getStateBefore() {
		return stateBefore;
	}
	public void setStateBefore(int stateBefore) {
		this.stateBefore = stateBefore;
	}
	public int getStateAfter() {
		return stateAfter;
	}
	public void setStateAfter(int stateAfter) {
		this.stateAfter = stateAfter;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public List<Integer> getVisits() {
		return visits;
	}
	public void setVisits(List<Integer> visits) {
		this.visits = visits;
	}
	public String getParentPath() {
		return parentPath;
	}
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hashCode(pathItem + pathIndex);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof URLNode) {
			URLNode that = (URLNode) other;
			if(!pathItem.equalsIgnoreCase(that.pathItem) || pathIndex != that.pathIndex) {
				LOG.debug("false 1 : " + pathItem + " - " + that.pathItem);
				return false;
			}
			
			// The path to this node is the same as the other node being compared
			if(parentPath.equalsIgnoreCase(that.parentPath)) {
				LOG.debug("True 1 : " + pathItem + " - " + that.pathItem);
				return true;
			}
			else if(this.isLeaf && that.isLeaf){
				if(payloadToCompare == null || that.payloadToCompare == null) {
					LOG.debug("false 2 : " + pathItem + " - " + that.pathItem);
					return false;
				}
				else {
					try {
						boolean payLoadSimilar =  ResponseComparisonUtils.comparePayloads(payloadToCompare, that.payloadToCompare, responseType);
						if(payLoadSimilar) {
							LOG.info("True 2: Similar payload: {}- {}, {}-{}", pathItem, parentPath, that.pathItem, that.parentPath);
							return true;
						}
						else {
							LOG.info("False 3: Different payload: {}- {}, {}-{}", pathItem, parentPath, that.pathItem, that.parentPath);
							return false;
						}
//						return payLoadSimilar;
					} catch (IOException e) {
						e.printStackTrace();
						LOG.info("False 4: Error comparing payload : {}- {}, {}-{}", pathItem, parentPath, that.pathItem, that.parentPath);
						return false;
					}
				}
			}
			else {
				// Cannot compare non-leaf nodes when they have different hierarchy
				LOG.debug("false 5 : " + pathItem + " - " + that.pathItem);
				return false;
			}
		}
		System.out.println("Something wrong - " + pathItem);
		return false;
	}
	public int getId() {
		// TODO Auto-generated method stub
		return id;
	}

	public void setVar(int variable) {
		this.var = variable;
	}

	public MethodClazz getMethod() {
		return method;
	}

	public void setMethod(MethodClazz methodClazz) {
		this.method = methodClazz;
	}
	
	@Override
	public String toString() {
		return id + "-" + requestId + "-" + pathItem + "-" + parentPath;
	}
}
