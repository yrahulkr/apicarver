package com.apicarv.testCarver.utils;

public class DomUtils {
	public static String getStrippedHTML(String html) {
		String strippedStr;

		// remove line breaks
		strippedStr = html.replaceAll("[\\t\\n\\x0B\\f\\r]", "");

		// remove just before and after elements spaces
		strippedStr = strippedStr.replaceAll(">[ ]*", ">");
		strippedStr = strippedStr.replaceAll("[ ]*<", "<");

		return strippedStr;
	}

}
