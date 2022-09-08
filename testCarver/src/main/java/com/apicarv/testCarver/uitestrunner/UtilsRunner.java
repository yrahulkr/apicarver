package com.apicarv.testCarver.uitestrunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.apicarv.testCarver.Main;
import com.apicarv.testCarver.utils.Settings;
import com.apicarv.testCarver.utils.Settings.SUBJECT;

public class UtilsRunner {

	static long startTime;
	static long stopTime;
	static long elapsedTime;

	public static Result runTestSuite(Class<?> testSuite) {
		Result result = JUnitCore.runClasses(testSuite);
		return result;
	}
	
	public static Optional<Description> executeTests(SUBJECT subject) {
		TestCaseExecutor executor = new TestCaseExecutor();
		TestCaseExecutionListener listener = new TestCaseExecutionListener();
		
		
		String className = "tests." + subject.name() + ".TestSuite";
		
		executor.execute(listener, className);
		
		return listener.getFailedTestDescription();
	}

	/**
	 * Run a single JUnit test case or an entire test suite if a runner class is
	 * specified
	 * 
	 * @param testSuite
	 * @param testCase
	 */
	public static void runTest(String testSuite, String testCase) {

		/* build the class runner. */
		String classRunner = testSuite + "." + testCase;

		/* run the test programmatically. */
		Result result = null;
		try {
			System.out.println("[LOG]\tRunning test " + classRunner);
			startTime = System.currentTimeMillis();
			result = JUnitCore.runClasses(Class.forName(classRunner));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		/* if the test failed, save the exception. */
		if (!result.wasSuccessful()) {

			System.out.println("[LOG]\tTest " + classRunner + " failed, saving the exception");

			/* for each breakage, save the exception on the filesystem. */
			for (Failure fail : result.getFailures()) {
				System.out.println(fail);
//				EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
//
//				String path = Settings.testingTestSuiteVisualTraceExecutionFolder + UtilsRepair.capitalizeFirstLetter(ea.getFailedTest())
//						+ Settings.JAVA_EXT;
//				String jsonPath = UtilsParser.toJsonPath(path);
//
//				UtilsParser.serializeException(ea, jsonPath);
			}
		} else {
			System.out.println("[LOG]\tTest " + classRunner + " passed");
		}

		stopTime = System.currentTimeMillis();
		elapsedTime = stopTime - startTime;
	}

	public static Object runMethod(Class<?> clazz, Object inst, String methodName) {

		Object result = null;

		try {

			Method[] allMethods = clazz.getDeclaredMethods();
			for (Method m : allMethods) {
				if (m.getName().equalsIgnoreCase(methodName)) {
					m.setAccessible(true);
					result = m.invoke(inst, null);
				}
			}

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void cleanup(Class<?> clazz, Object inst) {
		runMethod(clazz, inst, "tearDown");
	}
	
	public static void main(String args[]) {
		String subject = "xwiki";
		Settings.currResult = new TestRunResult(APIMiner.MiningMode.CRAWLING, 10);
		// start docker
//		TestCaseExecutor.restartDocker(subject, true);
//		
//		try {
//			Thread.sleep(10000); // Wait after restarting docker
//		} catch (InterruptedException e) {
//				System.err.println("Error sleeping after docker restart " + e.getMessage());
//		}
		try {
			Optional<Description> result = UtilsRunner.executeTests(Main.getSubject(subject));
			System.out.println("failed : " + result.isPresent());
			System.out.println(Settings.currResult.getFailedTests());
		}catch(Exception ex) {
			ex.printStackTrace();
		}
//		TestCaseExecutor.restartDocker(subject, false);
		System.exit(0);
	}

}
