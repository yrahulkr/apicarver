package com.apicarv.testCarver.apirecorder;

public class UIAction {
	private final int id;
	public enum ActionType{
		click, submit, sendkeys, hover, jsAction, get, unknown, nav
	}
	public enum ActionTiming{
		before, after, during, unknown
	}
	
	private ActionType actionType;
	private ActionTiming actionTiming;
	/**
	 * URL for get events and Element identifier for others
	 */
	private String actionIdentifier;

	public UIAction(int id) {
		this.id = id;
	}
	
	public UIAction(int id, ActionType actionType, ActionTiming actionTiming, String actionIdentifier) {
		this.id = id;
		this.actionIdentifier = actionIdentifier;
		this.actionType = actionType;
		this.actionTiming = actionTiming;
	}
	
	public int getId() {
		return id;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public String getActionIdentifier() {
		return actionIdentifier;
	}

	public void setActionIdentifier(String actionIdentifier) {
		this.actionIdentifier = actionIdentifier;
	}

	public ActionTiming getActionTiming() {
		return actionTiming;
	}

	public void setActionTiming(ActionTiming actionTiming) {
		this.actionTiming = actionTiming;
	}
}
