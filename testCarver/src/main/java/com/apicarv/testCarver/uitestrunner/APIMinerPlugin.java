package com.apicarv.testCarver.uitestrunner;

import com.apicarv.testCarver.apirecorder.CrawlEvent;
import com.apicarv.testCarver.apirecorder.CrawlStateLogger;
import com.apicarv.testCarver.apirecorder.NetworkEventParser;
import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlerContext;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.plugin.AfterEventFiredPlugin;
import com.crawljax.core.plugin.OnUrlLoadPlugin;
import com.crawljax.core.plugin.PreCrawlingPlugin;
import com.crawljax.core.state.Eventable;
import com.crawljax.stateabstractions.hybrid.HybridStateVertexImpl;
import com.apicarv.testCarver.utils.Settings;

public class APIMinerPlugin implements AfterEventFiredPlugin, OnUrlLoadPlugin, PreCrawlingPlugin {

	/**
	 * 	Access is already set by the time afterEventFired is called
	 */
	@Override
	public void afterEventFired(CrawlerContext context, Eventable eventable) {
		
		synchronized(CrawlStateLogger.getInstance()) {
//			System.out.println(" plugin - " + CrawlStateLogger.getInstance().getCrawlStateBefore());
//			System.out.println(CrawlStateLogger.getInstance().getCrawlStateAfter());			
			CrawlEvent event = new CrawlEvent(eventable.getSourceStateVertex().getId(), eventable.getTargetStateVertex().getId(), eventable.getIdentification().getValue());
			CrawlStateLogger.getInstance().addCrawlEvent(event);
			// Add event to unique event map
			if(context.getFragmentManager()!=null && context.getCurrentState() instanceof HybridStateVertexImpl) {
				CandidateElement candidate = eventable.getSourceStateVertex().getCandidateElement(eventable);
				int candidateGroup = CrawlStateLogger.getInstance().addToCandidateGroups(candidate);
				event.setCandidateGroup(candidateGroup);
			}
			else {
				event.setCandidateGroup(-1);
			}
		}
		
		NetworkEventParser.getInstance().newCrawlEvent();
	}

	@Override
	public void onUrlLoad(CrawlerContext context) {
		synchronized(CrawlStateLogger.getInstance()) {
			if(context.getCurrentState()!=null) {
				CrawlEvent event = new CrawlEvent(-1, context.getCurrentState().getId(), "loadURL");
				event.setCandidateGroup(CrawlStateLogger.LOAD_URL_GROUP);
				CrawlStateLogger.getInstance().addCrawlEvent(event);
			}
			else {
				CrawlEvent event = new CrawlEvent(-1, -1, "FirstloadURL");
				event.setCandidateGroup(CrawlStateLogger.LOAD_URL_GROUP);
				CrawlStateLogger.getInstance().addCrawlEvent(event);
			}
		}
		NetworkEventParser.getInstance().newCrawlEvent();
	}

	@Override
	public void preCrawling(CrawljaxConfiguration config) throws RuntimeException {
		Settings.currResult.addUI_output(config.getOutputDir().getAbsolutePath());
	}
}
