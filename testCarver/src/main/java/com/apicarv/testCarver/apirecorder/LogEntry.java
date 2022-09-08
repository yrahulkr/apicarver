package com.apicarv.testCarver.apirecorder;

import org.openqa.selenium.devtools.v101.network.model.MonotonicTime;

public class LogEntry extends NetworkEvent{
	private UIAction uiActionBefore;
	private UIAction uiActionAfter;
	private int crawlStateBefore;
	private int crawlStateAfter;
	private String event;
	private int candidateGroup;
	
	public LogEntry(int id) {
		super(id);
		this.timeStamp = System.currentTimeMillis();
	}
	
	public String getEvent() {
		return event;
	}

	MonotonicTime monoTimeStamp;
	
	private final long timeStamp;

	public MonotonicTime getMonoTimeStamp() {
		return monoTimeStamp;
	}

	public void setMonoTimeStamp(MonotonicTime timeStamp) {
		this.monoTimeStamp = timeStamp;
	}


	public UIAction getUiActionBefore() {
		return uiActionBefore;
	}

	public void setUiActionBefore(UIAction uiActionBefore) {
		this.uiActionBefore = uiActionBefore;
	}

	public UIAction getUiActionAfter() {
		return uiActionAfter;
	}

	public void setUiActionAfter(UIAction uiActionAfter) {
		this.uiActionAfter = uiActionAfter;
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

	public void setEvent(String event) {
		this.event = event;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public int getCandidateGroup() {
		return candidateGroup;
	}

	public void setCandidateGroup(int eventGroupNum) {
		this.candidateGroup = eventGroupNum;
	}

	
}
