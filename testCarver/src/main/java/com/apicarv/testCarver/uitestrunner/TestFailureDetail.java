package com.apicarv.testCarver.uitestrunner;

public class TestFailureDetail {
	private final String location;
	private final String shortMessage;
	
	public String getLocation() {
		return location;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	private transient final String fullMessage;

	public TestFailureDetail(String location, String shortMessage, String fullMessage) {
		this.location = location;
		this.shortMessage = shortMessage;
		this.fullMessage = fullMessage;
	}
	
	@Override
	public String toString() {
		return location + " : " + shortMessage;
	}
}
