package com.apicarv.testCarver.uitestrunner;

import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(TestCaseExecutor.class);

	public void execute(RunListener runListener, String className
//			, SUBJECT subject 
//			, boolean newSuite
			){
//		if (subject != null) {
//			resetApp(subject);
//		}
		JUnitCore core = new JUnitCore();
		core.addListener(runListener);
		((TestCaseExecutionListener)runListener).setCore(core);

		try {
			core.run(Class.forName(className));
		} catch (ClassNotFoundException e) {
			LOG.error("Error running class {}", className);
			LOG.error("Error running class {}", e.getMessage());

		}catch(Exception ex) {
			LOG.error("Exception occured during test run");
			ex.printStackTrace();
		}
		finally {
			
		}
//		if (subject != null) {
//			// Stop docker after test run
//			restartDocker(subject, false);
//		}
	}
	
//	private static void resetApp(SUBJECT subject) {
//		switch (subject) {
//		case addressbook:
//			tests.addressbook.ResetAppState.reset();
//			break;
//		case claroline:
//			tests.claroline.ResetAppState.reset();
//			break;
//		case collabtive:
//			tests.collabtive.ResetAppState.reset();
//			break;
//		case mantisbt:
//			tests.mantisbt.ResetAppState.reset();
//			break;
//		case mrbs:
//			tests.mrbs.ResetAppState.reset();
//			break;
//		case ppma:
//			tests.ppma.ResetAppState.reset();
//			break;
//		default:
//			LOG.error("Could not reset {}", subject);
//			return;
//		}
//	}


//	public static void restartDocker(SUBJECT subject, boolean restart) {
//		if (subject == null) {
//			LOG.error("Not a valid subject : {}", subject);
//			return;
//		}
//		if (subject == SUBJECT.dummy) {
//			LOG.info("No docker for " + subject);
//			return;
//		}
//
//		List<List<String>> commands = new ArrayList<List<String>>();
//		List<String> dockerStop = new ArrayList<String>();
//		dockerStop.add("bash");
//		dockerStop.add("-c");
//		dockerStop.add("docker stop " + subject.name());
//		commands.add(dockerStop);
//
//		List<String> dockerRm = new ArrayList<String>();
//		dockerRm.add("bash");
//		dockerRm.add("-c");
//		dockerRm.add("docker rm " + subject.name());
//		commands.add(dockerRm);
//
//		if (restart) {
//			Path dockerScript = Paths.get(Settings.DOCKER_LOCATION, subject.name(), Settings.DOCKER_SCRIPT);
//			List<String> runDocker = new ArrayList<String>();
//			runDocker.add(dockerScript.toAbsolutePath().toString());
//			commands.add(runDocker);
//		}
//
//		for (List<String> command : commands) {
//			LOG.info("{}", command);
//			ProcessBuilder builder = new ProcessBuilder();
//			String path = builder.environment().get("PATH");
//
//			path += ":/usr/local/bin"; // For docker executable
//
//			builder.environment().put("PATH", path);
//
//			builder.command(command);
//			try {
//				Process process = builder.start();
//				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//				String line;
//				while ((line = reader.readLine()) != null) {
//					LOG.info(line);
//				}
//
//				int exitCode = process.waitFor();
//				LOG.info("\nExited with error code : " + exitCode);
//			} catch (IOException e) {
//				LOG.error("Error starting docker {}", e.getMessage());
//			} catch (InterruptedException e) {
//				LOG.error("Error starting docker {}", e.getMessage());
//			}
//		}
//
//	}

}
