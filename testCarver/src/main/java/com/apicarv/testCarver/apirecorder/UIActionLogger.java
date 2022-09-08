package com.apicarv.testCarver.apirecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UIActionLogger {
	private List<UIAction> uiActions;
	private UIAction latestBeforeAction;
	private UIAction latestAfterAction;

	private static UIActionLogger uiActionLogger = new UIActionLogger();

	AtomicInteger atomicId = new AtomicInteger(0);

	public static UIActionLogger getInstance() {
		return uiActionLogger;
	}

	private UIActionLogger() {
		uiActions = new ArrayList<>();
		latestBeforeAction = null;
		latestAfterAction = null;
	}

	public List<UIAction> getUiActions() {
		return uiActions;
	}

	public void setUiActions(List<UIAction> uiActions) {
		this.uiActions = uiActions;
	}

	public UIAction getLatestBeforeAction() {
		return latestBeforeAction;
	}

	public UIAction getLatestAfterAction() {
		return latestAfterAction;
	}

	public void setLatestAfterAction(UIAction latestAfterAction) {
		this.latestAfterAction = latestAfterAction;
	}

	public void setLatestBeforeAction(UIAction latestBeforeAction) {
		this.latestBeforeAction = latestBeforeAction;
	}

	
	/**
	 * Use {@link #logAction(UIAction.ActionType, UIAction.ActionTiming, String)}
	 * @return
	 */
	public UIAction logAction() {
		this.latestBeforeAction = null;
		this.latestAfterAction = null;
		return new UIAction(atomicId.getAndIncrement());
	}

	/**
	 * Creates a new {@link #UIAction} and maintans the latest after and before UI action logs to associate API calls
	 * @param actionType
	 * @param actionTiming
	 * @param actionIdentifier
	 * @return
	 */
	public UIAction logAction(UIAction.ActionType actionType, UIAction.ActionTiming actionTiming, String actionIdentifier) {
		UIAction newAction = new UIAction(atomicId.getAndIncrement(), actionType, actionTiming, actionIdentifier);
		uiActions.add(newAction);
		switch (actionTiming) {
		case before:
			latestBeforeAction = newAction;
			break;
		case after:
			latestAfterAction = newAction;
			break;
		case during:
			break;
		case unknown:

		default:
			latestBeforeAction = null;
			latestAfterAction = null;
			break;
		}
		
		return newAction;
	}
	
	public void reset() {
		uiActionLogger = new UIActionLogger();
	}
}
