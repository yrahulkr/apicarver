package com.apicarv.testCarver.utils;

public class UtilsParser {
	public static String getTestSuiteNameFromWithinType(String withinType) {
		// class clarolineDirectBreakage.TestLoginAdmin -> clarolineDirectBreakage

		if (withinType.contains("main.java")) {
			withinType = withinType.replaceAll("class ", "");
		} else {
			withinType = withinType.replaceAll("class ", "");
		}
		
		withinType = withinType.substring( withinType.indexOf(".") + 1, withinType.length());
		withinType = withinType.substring(0, withinType.indexOf("."));

		return withinType;
	}

}
