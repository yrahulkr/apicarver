package com.apicarv.testCarver.apirecorder;

public class ResponseEvent extends NetworkEvent {
	
	public ResponseEvent(int id) {
		super(id);
	}

	protected int status; // To be used for responses


	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
