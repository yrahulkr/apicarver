package com.apicarv.testCarver.apirecorder;

import java.lang.reflect.Method;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Strings;

public class UIActionListener implements WebDriverListener {
	private static final Logger LOG = LoggerFactory.getLogger(UIActionListener.class);

	@Override
	public void beforeClick(WebElement element) {
		UIActionLogger.getInstance().logAction(UIAction.ActionType.click, UIAction.ActionTiming.before, getXpath(element));
		System.out.println("Before Click : " + getXpath(element));
	}

	private String getXpath(WebElement element) {
		try {
			return element.getTagName() + ": text :" + element.getText() + " : location :" + element.getLocation().toString();
		}catch(Exception ex) {
			return ""; 
		}
	}

	@Override
	public void afterClick(WebElement element) {
		UIActionLogger.getInstance().logAction(UIAction.ActionType.click, UIAction.ActionTiming.after, getXpath(element));
		System.out.println("After Click : " + getXpath(element));
	}

	@Override
	public void beforeSubmit(WebElement element) {
		UIActionLogger.getInstance().logAction(UIAction.ActionType.submit, UIAction.ActionTiming.before, getXpath(element));

		System.out.println("Before Submit : " + getXpath(element));
	}

	@Override
	public void afterSubmit(WebElement element) {
		UIActionLogger.getInstance().logAction(UIAction.ActionType.submit, UIAction.ActionTiming.after, getXpath(element));

		System.out.println("After Submit : " + getXpath(element));
	}

	@Override
	public void beforeAnyNavigationCall(WebDriver.Navigation navigation, Method method, Object[] args) {
		String actionIdentifier = method.getName() + ":" + Strings.join(" : ", args);
		UIActionLogger.getInstance().logAction(UIAction.ActionType.nav, UIAction.ActionTiming.before, actionIdentifier);

		LOG.debug("Before navigation." + method.getName() + ": with args :" + Strings.join(" : ", args));

	}

	@Override
	public void afterAnyNavigationCall(WebDriver.Navigation navigation, Method method, Object[] args, Object result) {
		String actionIdentifier = method.getName() + ":" + Strings.join(" : ", args);
		UIActionLogger.getInstance().logAction(UIAction.ActionType.nav, UIAction.ActionTiming.before, actionIdentifier);

		LOG.debug("After navigation." + method.getName() + ": with args :" + Strings.join(" : ", args));
	}

	public void beforeGet(WebDriver driver, String url) {
		UIActionLogger.getInstance().logAction(UIAction.ActionType.get, UIAction.ActionTiming.before, url);

		System.out.println("Before get : " + url);
	}

	public void afterGet(WebDriver driver, String url) {
		UIActionLogger.getInstance().logAction(UIAction.ActionType.get, UIAction.ActionTiming.after, url);

		System.out.println("After get : " + url);
	}

}
