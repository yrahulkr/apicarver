package com.apicarv.testCarver.uitestrunner;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import com.apicarv.testCarver.utils.Settings;

public class TestCaseExecutionListener extends RunListener {

    private Description failedTestDescription;
    private String failedTestTrace;
    private boolean testFailed = false;
    
    private JUnitCore core = null;

    public TestCaseExecutionListener(){
    }

    @Override
    public void testStarted(Description description) {
    	System.out.println(description.getMethodName());
    }

    @Override
    public void testFailure(Failure failure) {
        if(!testFailed){
            testFailed = true;
            this.failedTestDescription = failure.getDescription();
            this.failedTestTrace = failure.getTrace();
        }
        
        System.out.println(failure.getMessage().split(":")[0]);
        System.err.println(failure.getDescription().getMethodName() + " : " + failure.getException().toString());
        String failureLine = "";
        String failureClass = failure.getException().getStackTrace()[0].getMethodName();
        StackTraceElement[] trace = failure.getException().getStackTrace();
        for(int i = 0; i< failure.getException().getStackTrace().length; i++) {
        	StackTraceElement element = failure.getException().getStackTrace()[i];
        	if (element.getMethodName().equalsIgnoreCase(failure.getDescription().getMethodName())) {
        		System.out.println(element.getClassName());
        		String [] split = element.getClassName().split("\\.");
        		String failureClassName = split[split.length-1];
        		failureLine = failureClassName + "_" + element.getLineNumber();
        		break;
        	}
        }
        System.out.println(failureClass + " : " + failureLine);
        String fullMessage  = failure.getException().toString();
        String shortMessage = fullMessage.split("\n")[0];
        shortMessage = shortMessage.replaceAll("\"", "'");
        shortMessage = shortMessage.replaceAll("\\{", "<");
        shortMessage = shortMessage.replaceAll("\\}", ">");
        shortMessage = shortMessage.replaceAll("\\[", "<");
        shortMessage = shortMessage.replaceAll("\\]", ">");
        TestFailureDetail detail = new TestFailureDetail(failureLine, shortMessage, fullMessage);
        
        Settings.currResult.addTestResult(detail);

		Field field;
		try {
			System.out.println("Requesting to stop");
			field = JUnitCore.class.getDeclaredField("notifier");
			field.setAccessible(true);
			RunNotifier runNotifier = (RunNotifier) field.get(core);
			runNotifier.pleaseStop();
			System.out.println("Asked to stop");
		} catch (NoSuchFieldException | SecurityException e) {
			System.out.println("No Such Filed | security exception");
		} catch (IllegalArgumentException e) {
			System.out.println("Illegal argumnet");
		} catch (IllegalAccessException e) {
			System.out.println("Illegal Access exception");
		} catch(Exception ex) {
			System.out.println("Other exception " + ex.getMessage());
		}
      
    }

    public Optional<Description> getFailedTestDescription() {
        if(this.failedTestDescription != null)
            return Optional.of(this.failedTestDescription);
        return Optional.empty();
    }

    public Optional<String> getFailedTestTrace() {
        if(this.failedTestTrace != null)
            return Optional.of(this.failedTestTrace);
        return Optional.empty();
    }

	public void setCore(JUnitCore core2) {
		core = core2;
	}
	
	@Override
	public void testFinished(Description description) throws Exception {
//		DriverProvider.getInstance().closeDevtools();
//		NetworkEventParser.getInstance().exportJson();
		super.testFinished(description);
		
	}
	
	@Override
	public void testRunFinished(Result result) {
		Settings.currResult.setRunTests(result.getRunCount());
	}
}
