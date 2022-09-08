package com.apicarv.testCarver.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsAPIRunner {

	private static final Logger LOG = LoggerFactory.getLogger(UtilsAPIRunner.class);

	public static List<String> splitCookie(String cookieString) {
		List<String> returnSplit = new ArrayList<>();
		String[] newLineSplits = cookieString.split("\\R");
		for (String nlSplit : newLineSplits) {
			String[] semicommaSplits = nlSplit.split(";");
			for (String semicommaSplit : semicommaSplits) {
				returnSplit.add(semicommaSplit);
			}
		}

		return returnSplit;
	}

	public static List<NameValuePair> getCookiePairs(String cookieString) {
		List<NameValuePair> returnPairs = new ArrayList<NameValuePair>();
		List<String> splitCookies = splitCookie(cookieString);

		for (String cookie : splitCookies) {
			try {
				String[] splitParam = cookie.split("=");
				returnPairs.add(new BasicNameValuePair(splitParam[0].trim(), splitParam[1].trim()));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return returnPairs;

	}

	/**
	 * Splits URL QUERY or POST data strings
	 * 
	 * @param data
	 * @return
	 */
	public static List<NameValuePair> getPostData(String data) {
		List<NameValuePair> params = new ArrayList<>();

		LOG.debug(data);
		try {
			String[] split = data.split("&");
			for (int i = 0; i < split.length; i++) {
				LOG.debug(split[i]);
				try {

					String[] splitParam = split[i].split("=");
					if (split[i].contains("=") && splitParam.length < 2) {
						params.add(new BasicNameValuePair(splitParam[0], ""));
					} else {
						params.add(new BasicNameValuePair(splitParam[0], splitParam[1]));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			LOG.error("Cannot parse Post Data");
		}
		return params;
	}
	
	
	public static String getNormalizedPathString(String pathItem) {
		String pathMod = pathItem;
		
		try{
			Integer.parseInt(pathItem.charAt(0)+"");
			pathMod = "number" + pathItem;
		}
		catch(NumberFormatException ex) {
			// Its not a number
		}
		
		return pathMod;
	}
}
