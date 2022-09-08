package com.apicarv.testCarver.utils;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Comparator;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseComparisonTests {
	@Test
	public void testJsonCompare() throws IOException {
		String json1 = "[{\"name\":\"snake\",\"id\":4},{\"name\":\"bird\",\"id\":5},{\"name\":\"hamster\",\"id\":6}]";
		String json2 = "[{\"id\":1,\"name\":\"RqSbQgqO\"},{\"id\":2,\"name\":\"surgery\"},{\"id\":3,\"name\":\"dentistry\"}]";
		ObjectMapper mapper = new ObjectMapper();
		Comparator<JsonNode> comparator = new Comparator<JsonNode>() {
			@Override
			public int compare(JsonNode o1, JsonNode o2) {
				if(o1.getNodeType() == o2.getNodeType()) {
					return 0;
				}
				return 1;
			}
		};
		JsonNode topNode = mapper.readTree(json1);
		System.out.println(mapper.readTree(json1));
		assertTrue(ResponseComparisonUtils.compareJson(json1, json2));
	}
}
