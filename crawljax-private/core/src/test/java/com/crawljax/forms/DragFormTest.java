package com.crawljax.forms;

import static com.crawljax.browser.matchers.StateFlowGraphMatchers.hasStates;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.crawljax.core.CrawlSession;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.test.BaseCrawler;

public class DragFormTest {
	@Test
	public void whenHiddenElementsOfItShouldNotCrawl() {
		CrawlSession crawl = new BaseCrawler("dragdrop-elements").crawl();
		StateFlowGraph stateFlowGraph = crawl.getStateFlowGraph();

		int expectedStates = 2;
		assertThat(stateFlowGraph, hasStates(expectedStates));
	}
}
