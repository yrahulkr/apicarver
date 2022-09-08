package com.apicarv.testCarver.apirecorder;

public class CrawlEvent {
	private int crawlStateBefore;
	private int crawlStateAfter;
	private String event;
	private final long timeStamp;
	private int candidateGroup;
	
	public CrawlEvent(int crawlStateBefore, int crawlStateAfter, String event) {
		this.crawlStateAfter = crawlStateAfter;
		this.crawlStateBefore = crawlStateBefore;
		this.event = event;
		this.timeStamp = System.currentTimeMillis();
	}
	
	public String getEvent() {
		return event;
	}
	public int getCrawlStateBefore() {
		return crawlStateBefore;
	}

	public void setCrawlStateBefore(int crawlStateBefore) {
		this.crawlStateBefore = crawlStateBefore;
	}

	public int getCrawlStateAfter() {
		return crawlStateAfter;
	}

	public void setCrawlStateAfter(int crawlStateAfter) {
		this.crawlStateAfter = crawlStateAfter;
	}

	public void setEvent(String value) {
		this.event = value;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public int getCandidateGroup() {
		return candidateGroup;
	}

	public void setCandidateGroup(int candidateGroup) {
		this.candidateGroup = candidateGroup;
	}
}
