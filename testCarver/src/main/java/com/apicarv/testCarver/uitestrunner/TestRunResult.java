package com.apicarv.testCarver.uitestrunner;

import java.util.ArrayList;
import java.util.List;

public class TestRunResult {

	private final int crawlTime;

	private final List<String>ui_output;

	public TestRunResult(APIMiner.MiningMode miningMode, int crawlTime){
		this.miningMode = miningMode;
		this.crawlTime = crawlTime;
		this.ui_output = new ArrayList<>();
	}

	public List<String> getUi_output(){
		return this.ui_output;
	}

	public void addUI_output(String outDir){
		this.ui_output.add(outDir);
	}

	private APIMiner.MiningMode miningMode;
	private List<TestFailureDetail> testFailureDetails = new ArrayList<TestFailureDetail>();
	private int runCount = -1;
	private boolean failed;

	private long duration;

	public long getDuration() {
		return duration;
	}

	public boolean isFailed() {
		return failed;
	}

	public void addTestResult(TestFailureDetail detail) {
		this.testFailureDetails.add(detail);
	}

	public void setRunTests(int runCount) {
		this.setRunCount(runCount);
	}

	public List<TestFailureDetail> getFailedTests() {
		return this.testFailureDetails;
	}

	public int getRunCount() {
		return runCount;
	}

	public void setRunCount(int runCount) {
		this.runCount = runCount;
	}

	public void setFailed(boolean b) {
		this.failed = b;
	}

	public void setDuration(long l) {
		this.duration = l;
	}

}
