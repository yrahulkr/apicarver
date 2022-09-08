package com.apicarv.testCarver.apirecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawljax.core.CandidateElement;
import com.crawljax.fragmentation.Fragment.FragmentComparision;

public class CrawlStateLogger {
	List<CrawlEvent> eventList = new ArrayList<>();
	
	Map<Integer, List<CandidateElement>> candidateGroups = new HashMap<>();
	public static final int LOAD_URL_GROUP = 0;
	AtomicInteger nextGroupNum = new AtomicInteger(1);
	
	private static CrawlStateLogger crawlStateLogger = new CrawlStateLogger();

	public static CrawlStateLogger getInstance() {
		return crawlStateLogger;
	}

	private CrawlStateLogger() {
		CrawlEvent dummy = new CrawlEvent(-2, -1, "dummy");
		eventList.add(dummy);
	}
	
	public void addCrawlEvent(CrawlEvent event) {
		eventList.add(event);
	}
	
	public List<CrawlEvent> getEventList(){
		return eventList;
	}
	
	public CrawlEvent getLatestEvent() {
		if(eventList ==null || eventList.isEmpty()) {
			return null;
		}
		return eventList.get(eventList.size()-1);
	}
	
	public void reset() {
		crawlStateLogger = new CrawlStateLogger();
	}

	public int addToCandidateGroups(CandidateElement candidate) {
		if(candidate.getDuplicateAccess() != 0 || candidate.getEquivalentAccess() != 0) {
			for(Integer candidateGroup: candidateGroups.keySet()) {
				CandidateElement groupRep = candidateGroups.get(candidateGroup).get(0);
				FragmentComparision fragCompare = candidate.getClosestFragment().compare(groupRep.getClosestFragment()); 
				if(fragCompare == FragmentComparision.EQUAL || fragCompare == FragmentComparision.EQUIVALENT) {
					// Compare the relative xpaths
					List<CandidateElement> equiv = groupRep.getClosestFragment().getEquivalentCandidate(candidate, candidate.getClosestFragment());
					if(equiv == null || equiv.isEmpty()) {
						// No equivalent candidate
						continue;
					}
					if(equiv.equals(groupRep)) {
						// Is the equivalent cnadidate
						candidateGroups.get(candidateGroup).add(candidate);
						return candidateGroup;
					}
				}
			}
		}
		// Did not find equivalent candidate. Add a new group.
		int groupNum = nextGroupNum.getAndIncrement();
		candidateGroups.put(groupNum, new ArrayList<>());
		candidateGroups.get(groupNum).add(candidate);
		return groupNum;
	}

	
}
