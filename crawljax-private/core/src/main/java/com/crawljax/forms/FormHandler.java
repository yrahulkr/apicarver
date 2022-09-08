package com.crawljax.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.xml.xpath.XPathExpressionException;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.condition.eventablecondition.EventableCondition;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.configuration.CrawlRules;
import com.crawljax.core.exception.BrowserConnectionException;
import com.crawljax.core.state.Identification;
import com.crawljax.core.state.Identification.How;
import com.crawljax.util.DomUtils;
import com.crawljax.util.XPathHelper;
import com.google.common.base.Enums;
import com.google.inject.assistedinject.Assisted;

/**
 * Handles form values and fills in the form input elements with random values of the defined
 * values.
 */
public class FormHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(FormHandler.class.getName());

	protected final EmbeddedBrowser browser;

	public static final int RANDOM_STRING_LENGTH = 8;

	protected final FormInputValueHelper formInputValueHelper;

	@Inject
	public FormHandler(@Assisted EmbeddedBrowser browser, CrawlRules config) {
		this.browser = browser;
		this.formInputValueHelper = FormInputValueHelper.getInstance(
				config.getInputSpecification(), config.getFormFillMode());
	}

	/**
	 * Fills in the element with the InputValues for input
	 *
	 * @param element the node element
	 * @param input   the input data
	 */
	protected void setInputElementValue(Node element, FormInput input) {

		LOGGER.debug("INPUTFIELD: {} ({})", input.getIdentification(), input.getType());
		if (element == null || input.getInputValues().isEmpty()) {
			return;
		}
		try {

			switch (input.getType()) {
				case TEXT:
				case TEXTAREA:
				case PASSWORD:
				case INPUT:
				case EMAIL:
				case NUMBER:
					handleText(input);
					break;
				case HIDDEN:
					handleHidden(input);
					break;
				case CHECKBOX:
					handleCheckBoxes(input);
					break;
				case RADIO:
					handleRadioSwitches(input);
					break;
				case SELECT:
					handleSelectBoxes(input);
					break;
				/*case CALENDAR:
					handleCalendarDrag(input);*/
			}

		} catch (ElementNotInteractableException e) {
			LOGGER.warn("Element not visible, input not completed.");
		} catch (BrowserConnectionException e) {
			throw e;
		} catch (RuntimeException e) {
			LOGGER.error("Could not input element values");
			throw e;
		}
	}

	private void handleCalendarDrag(FormInput input){
		System.out.println("handling calendar drag");
		if(input.getInputValues().contains("no")) {
			LOGGER.info("Drag drop cannot be performed for this calendar input {}", input);
			return ;
		}
		List<WebElement> draggableCells = browser.getWebElement(input.getIdentification()).findElements(By.className("rbc-day-bg"));
		Random random = new Random();
		int firstIndex = random.nextInt(draggableCells.size());
		
		int secondIndex = random.nextInt(draggableCells.size());
		
		while(firstIndex==secondIndex) {
			secondIndex = random.nextInt(draggableCells.size());
		}
		
		WebElement a = draggableCells.get(firstIndex);
		WebElement b = draggableCells.get(secondIndex);
		Actions action = new Actions(browser.getWebDriver());
//		action.clickAndHold(firstElement).moveToElement(secondElement)
//		.release(secondElement).build().perform();
//		action.dragAndDrop(firstElement, secondElement).perform();
		/*int x1 = ((Locatable)a).getCoordinates().inViewPort().x;
		int y1 = a.getLocation().y;
		int x2 = b.getLocation().x;
        int y2 = b.getLocation().y;*/
		/*
        
		action.moveToElement(a)
        .pause(Duration.ofSeconds(1))
        .clickAndHold(a)
        .pause(Duration.ofSeconds(1))
        .moveByOffset(x, y)
        .moveToElement(b)
        .moveByOffset(x,y)
        .pause(Duration.ofSeconds(1))
        .release().build().perform();*/
		action.dragAndDrop(a, b).release().perform();
//		action.dragAndDropBy(a, x2-x1, y2-y1).release().perform();;
//		 Robot robot = new Robot();
//		    robot.keyPress(KeyEvent.VK_ESCAPE);
//		    robot.keyRelease(KeyEvent.VK_ESCAPE);
		JavascriptExecutor js = (JavascriptExecutor)browser.getWebDriver();
				js.executeScript("function createEvent(typeOfEvent) {\n" + "var event =document.createEvent(\"CustomEvent\");\n"
				                    + "event.initCustomEvent(typeOfEvent,true, true, null);\n" + "event.dataTransfer = {\n" + "data: {},\n"
				                    + "setData: function (key, value) {\n" + "this.data[key] = value;\n" + "},\n"
				                    + "getData: function (key) {\n" + "return this.data[key];\n" + "}\n" + "};\n" + "return event;\n"
				                    + "}\n" + "\n" + "function dispatchEvent(element, event,transferData) {\n"
				                    + "if (transferData !== undefined) {\n" + "event.dataTransfer = transferData;\n" + "}\n"
				                    + "if (element.dispatchEvent) {\n" + "element.dispatchEvent(event);\n"
				                    + "} else if (element.fireEvent) {\n" + "element.fireEvent(\"on\" + event.type, event);\n" + "}\n"
				                    + "}\n" + "\n" + "function simulateHTML5DragAndDrop(element, destination) {\n"
				                    + "var dragStartEvent =createEvent('dragstart');\n" + "dispatchEvent(element, dragStartEvent);\n"
				                    + "var dropEvent = createEvent('drop');\n"
				                    + "dispatchEvent(destination, dropEvent,dragStartEvent.dataTransfer);\n"
				                    + "var dragEndEvent = createEvent('dragend');\n"
				                    + "dispatchEvent(element, dragEndEvent,dropEvent.dataTransfer);\n" + "}\n" + "\n"
				                    + "var source = arguments[0];\n" + "var destination = arguments[1];\n"
				                    + "simulateHTML5DragAndDrop(source,destination);", a, b);
		LOGGER.info("Drag drop done between {} and {}", a.getLocation(), b.getLocation());
	}

	private void handleCheckBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			boolean check = inputValue.isChecked();

			WebElement inputElement = browser.getWebElement(input.getIdentification());

			if (check && !inputElement.isSelected()) {
				inputElement.click();
			} else if (!check && inputElement.isSelected()) {
				inputElement.click();
			}
		}
	}

	private void resetRadioSwitches(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			if (inputValue.isChecked()) {
				WebElement inputElement = browser.getWebElement(input.getIdentification());
				if(inputElement!=null)
					((JavascriptExecutor)browser.getWebDriver()).executeScript("arguments[0].checked=false", inputElement);
			}
		}
	}
	
	private void handleRadioSwitches(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			if (inputValue.isChecked()) {
				WebElement inputElement = browser.getWebElement(input.getIdentification());
				inputElement.click();
			}
		}
	}

	private void resetSelectBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			WebElement inputElement = browser.getWebElement(input.getIdentification());
			inputElement.sendKeys(inputValue.getValue());
		}
	}
	
	private void handleSelectBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			WebElement inputElement = browser.getWebElement(input.getIdentification());
			inputElement.sendKeys(inputValue.getValue());
		}
	}
	
	/**
	 * Clear the input for given input text
	 * @param input
	 */
	private void resetText(FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if (null == text || text.length() == 0) {
			return;
		}
		WebElement inputElement = browser.getWebElement(input.getIdentification());
		inputElement.clear();
		inputElement.sendKeys(Keys.BACK_SPACE);
//		inputElement.sendKeys(text);
	}

	private void handleText(FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if (null == text || text.length() == 0) {
			return;
		}
		WebElement inputElement = browser.getWebElement(input.getIdentification());
		inputElement.clear();
		inputElement.sendKeys(text);
	}

	/**
	 * Enter information into the hidden input field.
	 *
	 * @param input The input to enter into the hidden field.
	 */
	private void handleHidden(FormInput input) {
		String text = input.getInputValues().iterator().next().getValue();
		if (null == text || text.length() == 0) {
			return;
		}
		WebElement inputElement = browser.getWebElement(input.getIdentification());
		JavascriptExecutor js = (JavascriptExecutor) browser.getWebDriver();
		js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", inputElement,
				"value", text);
	}

	/**
	 * @return all input element in dom
	 */
	private List<Node> getInputElements(Document dom) {
		List<Node> nodes = new ArrayList<>();
		try {
			NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, "//INPUT");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node candidate = nodeList.item(i);
				Node typeAttribute = candidate.getAttributes().getNamedItem("type");
				if (typeAttribute == null
						|| (Enums
						.getIfPresent(FormInput.InputType.class,
								typeAttribute.getNodeValue().toUpperCase())
						.isPresent())) {

					nodes.add(nodeList.item(i));
				}
			}
			nodeList = XPathHelper.evaluateXpathExpression(dom, "//TEXTAREA");
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodes.add(nodeList.item(i));
			}
			nodeList = XPathHelper.evaluateXpathExpression(dom, "//SELECT");
			for (int i = 0; i < nodeList.getLength(); i++) {
				nodes.add(nodeList.item(i));
			}
			
			/*nodeList = XPathHelper.evaluateXpathExpression(dom, "//DIV[@class='rbc-calendar']");
			for (int i = 0; i < nodeList.getLength(); i++) {
				System.out.println("found calendar");
				nodes.add(nodeList.item(i));
			}*/
			return nodes;
		} catch (XPathExpressionException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return nodes;
	}

	/**
	 * @return a list of form inputs.
	 */
	public List<FormInput> getFormInputs() {

		final List<FormInput> formInputs = new ArrayList<>();
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDom());
			List<Node> nodes = getInputElements(dom);
			for (Node node : nodes) {
				FormInput formInput =
						formInputValueHelper.getFormInputWithIndexValue(browser, node, 0);
				if (formInput != null) {
					formInputs.add(formInput);
				}
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return formInputs;
	}
	
	public List<FormInput> resetFormInputs(List<FormInput> formInputs) {
		ArrayList<FormInput> handled = new ArrayList<>();
		FormInput failing = null;
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			for (FormInput input : formInputs) {
				failing = input;
				LOGGER.info("resetting : " + input.getIdentification().getValue());
				resetInputElementValue(formInputValueHelper.getBelongingNode(input, dom), input);
				handled.add(input);
				failing = null;
			}
		} catch (Exception e) {
			LOGGER.error("Could not reset form elements");
			LOGGER.error(e.getMessage());
		}
		if(failing==null) {
			handled.add(new FormInput(null, null));
		}
		else {
			handled.add(failing);
		}
		return handled;
	}

	protected void resetInputElementValue(Node element, FormInput input) {

		LOGGER.debug("INPUTFIELD: {} ({})", input.getIdentification(), input.getType());
		if (element == null || input.getInputValues().isEmpty()) {
			return;
		}
		try {

			switch (input.getType()) {
				case TEXT:
				case TEXTAREA:
				case PASSWORD:
				case INPUT:
				case EMAIL:
					resetText(input);
					break;
//				case HIDDEN:
//					resetHidden(input);
//					break;
				case CHECKBOX:
					LOGGER.info("Resetting checkbox{}", input);
					resetCheckBoxes(input);
					break;
				case RADIO:
					resetRadioSwitches(input);
					break;
//				case SELECT:
//					handleSelectBoxes(input);
			default:
				break;
			}

		} catch (ElementNotInteractableException e) {
			LOGGER.warn("Element not visible, input not completed.");
		} catch (BrowserConnectionException e) {
			throw e;
		} catch (RuntimeException e) {
			LOGGER.error("Could not input element values");
			throw e;
		}
	}


	private void resetCheckBoxes(FormInput input) {
		for (InputValue inputValue : input.getInputValues()) {
			boolean check = inputValue.isChecked();

			WebElement inputElement = browser.getWebElement(input.getIdentification());

			if (check && inputElement.isSelected()) {
				inputElement.click();
			}
//				else if (!check && !inputElement.isSelected()) {
//				inputElement.click();
//			}
		}
	}

	/**
	 * Fills in form/input elements.
	 *
	 * @param formInputs form input list.
	 * @return 
	 */
	public List<FormInput> handleFormElements(List<FormInput> formInputs) {
		ArrayList<FormInput> handled = new ArrayList<>();
		FormInput failing = null;
		try {
			Document dom = DomUtils.asDocument(browser.getStrippedDomWithoutIframeContent());
			for (FormInput input : formInputs) {
				failing = input;
				LOGGER.info("Filling in: " + input.getIdentification().getValue());
				Node belongingNode = formInputValueHelper.getBelongingNode(input, dom);
				setInputElementValue(belongingNode, input);
				if(belongingNode!=null) {
					String xpath = XPathHelper.getSkeletonXpath(belongingNode);
					Identification xpathId = new Identification(How.xpath, xpath);
					FormInput handledInput = new FormInput(input.getType(), xpathId);
					handledInput.inputValues(input.getInputValues());
					handled.add(handledInput);
				}
				else {
					handled.add(input);
				}
//				input.getIdentification().setHow(How.xpath);
//				input.getIdentification().setValue(XPathHelper.getSkeletonXpath(belongingNode));
				failing = null;
			}
		} catch (Exception e) {
			LOGGER.error("Could not handle form elements");
			LOGGER.error(e.getMessage());
		}
		if(failing==null) {
			handled.add(new FormInput(null, null));
		}
		else {
			handled.add(failing);
		}
		return handled;
	}

	/**
	 * @param sourceElement      the form element
	 * @param eventableCondition the belonging eventable condition for sourceElement
	 * @return a list with Candidate elements for the inputs.
	 */
	public List<CandidateElement> getCandidateElementsForInputs(Element sourceElement,
			EventableCondition eventableCondition) {

		return formInputValueHelper.getCandidateElementsForInputs(browser, sourceElement,
				eventableCondition);
	}

}
