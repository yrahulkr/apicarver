package com.apicarv.testCarver.apirecorder;

import java.util.Arrays;
import java.util.List;

public class GenPipeLine {
	
	/**
	 * https://www.iana.org/assignments/media-types/media-types.xhtml
	 */
	public static String[] EXCLUDED_RESOURCES = { "audio", "image", "video", "model", "example", "text/css", "font",
			"model", "application/octet-stream", "javascript", "font-woff", "font-woff2", "png", "text/js", "woff", "woff2", "application/javascript"};

	public static final String[] INCLUDED_RESOURCES = {"json", "xml", "html"};
	
	public static final String[] IGNORED_HEADERS = { "content-length" };

	public static final String[] USEFUL_HEADERS = { "Set-Cookie", "Cookie" };

	
	public enum FILTER_TYPE{inclusion, exclusion};
	private FILTER_TYPE filterType = FILTER_TYPE.inclusion;
	private boolean combineEvents = false;
	private boolean cleanHeaders = false;
	private boolean excludeResources = false;
	private boolean exportIntermediate = false;
	
	private List<String> excludedResources = null;
	private List<String> ignoredHeaders = null;

	private List<String> includedResources;
	
	
	/**
	 * CombineEvents should always be chosen to be true. 
	 * @param combineEvents
	 * @param cleanHeaders
	 * @param excludeResources
	 */
	public GenPipeLine(boolean combineEvents, boolean cleanHeaders, boolean excludeResources, boolean exportIntermediate, FILTER_TYPE filterType) {
		this.combineEvents = combineEvents;
		this.cleanHeaders = cleanHeaders;
		this.excludeResources = excludeResources;
		this.setExportIntermediate(exportIntermediate);
		this.setFilterType(filterType);
	}
	
	public boolean isCombineEvents() {
		return combineEvents;
	}
	public void setCombineEvents(boolean combineEvents) {
		this.combineEvents = combineEvents;
	}
	public boolean isCleanHeaders() {
		return cleanHeaders;
	}
	public void setCleanHeaders(boolean cleanHeaders) {
		this.cleanHeaders = cleanHeaders;
	}
	public boolean isExcludeResources() {
		return excludeResources;
	}
	public void setExcludeResources(boolean excludeResources) {
		this.excludeResources = excludeResources;
	}
	public List<String> getExcludedResources() {
		if(excludedResources == null) {
			excludedResources = Arrays.asList(EXCLUDED_RESOURCES);
		}
		return excludedResources;
	}
	
	public List<String> getIncludedResources(){
		if(includedResources == null) {
			includedResources = Arrays.asList(INCLUDED_RESOURCES);
		}
		return includedResources;
	}
	
	public void setIncludedResources(List<String> includedResources) {
		this.includedResources = includedResources;
	}
	
	public void setExcludedResources(List<String> excludedResources) {
		this.excludedResources = excludedResources;
	}
	public List<String> getIgnoredHeaders() {
		if(ignoredHeaders == null) {
			ignoredHeaders = Arrays.asList(IGNORED_HEADERS);
		}
		return ignoredHeaders;
	}
	public void setIgnoredHeaders(List<String> ignoredHeaders) {
		this.ignoredHeaders = ignoredHeaders;
	}

	public boolean isExportIntermediate() {
		return exportIntermediate;
	}

	public void setExportIntermediate(boolean exportIntermediate) {
		this.exportIntermediate = exportIntermediate;
	}

	public FILTER_TYPE getFilterType() {
		return filterType;
	}

	public void setFilterType(FILTER_TYPE filterType) {
		this.filterType = filterType;
	}
	
	
}
