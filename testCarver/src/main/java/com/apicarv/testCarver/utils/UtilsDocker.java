package com.apicarv.testCarver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apicarv.testCarver.utils.Settings.SUBJECT;


public class UtilsDocker {
	
	private static final Logger LOG = LoggerFactory.getLogger(UtilsDocker.class);

	
	public static void restartDocker(SUBJECT subject, boolean restart, String covFile) {
		if (subject == null) {
			LOG.error("Not a valid subject : {}", subject);
			return;
		}
		if (subject == SUBJECT.dummy) {
			LOG.info("No docker for " + subject);
			return;
		}

		List<List<String>> commands = new ArrayList<List<String>>();

		Path dockerStopScript = Paths.get(Settings.DOCKER_LOCATION, subject.name(), Settings.DOCKER_STOP_SCRIPT);
		
		if(Files.exists(dockerStopScript)) {
			String timeStamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());

			List<String> dockerStop = new ArrayList<String>();
			dockerStop.add("bash");
			dockerStop.add("-c");
			String dockerStopCmd = covFile!=null? dockerStopScript.toAbsolutePath().toString() +" " + covFile :  dockerStopScript.toAbsolutePath().toString();
			dockerStop.add(dockerStopCmd);
			commands.add(dockerStop);
		}
		else {
			List<String> dockerStop = new ArrayList<String>();
			dockerStop.add("bash");
			dockerStop.add("-c");
			dockerStop.add("docker stop " + subject.name());
			commands.add(dockerStop);

			List<String> dockerRm = new ArrayList<String>();
			dockerRm.add("bash");
			dockerRm.add("-c");
			dockerRm.add("docker rm " + subject.name());
			commands.add(dockerRm);
		}

		
		if (restart) {
			Path dockerScript = Paths.get(Settings.DOCKER_LOCATION, subject.name(), Settings.DOCKER_SCRIPT);
			List<String> runDocker = new ArrayList<String>();
			runDocker.add("bash");
			runDocker.add("-c");
			runDocker.add(dockerScript.toAbsolutePath().toString());
			commands.add(runDocker);
		}

		for (List<String> command : commands) {
			LOG.info("{}", command);
			ProcessBuilder builder = new ProcessBuilder();
			String path = builder.environment().get("PATH");

			path += ":/usr/local/bin"; // For docker executable

			builder.environment().put("PATH", path);

			builder.command(command);
			try {
				Process process = builder.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					LOG.info(line);
				}

				int exitCode = process.waitFor();
				LOG.info("\nExited with error code : " + exitCode);
			} catch (IOException e) {
				LOG.error("Error starting docker {}", e.getMessage());
			} catch (InterruptedException e) {
				LOG.error("Error starting docker {}", e.getMessage());
			}
		}

	}
	
	public static void main(String args[]) {
		restartDocker(SUBJECT.realworld, true, "test/before");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 CloseableHttpClient client = HttpClients.createDefault();
		 
		 
				 
		try {
			ClassicHttpRequest request = new HttpGet("http://localhost:3000/api/tags");
			CloseableHttpResponse response = client.execute(request);
			System.out.println(response.getCode());
			Thread.sleep(100);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		restartDocker(SUBJECT.realworld, false, new File("test/after").getAbsolutePath());
		
	}
}
