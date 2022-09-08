package com.apicarv.testCarver.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.google.gson.Gson;
import com.apicarv.testCarver.apirunner.APIResponse;
public class ReportGenerator {
	
	private static final String TEMPLATE_FILE = "Report.html.vm";

	String reportFile;
	public ReportGenerator(String reportFile) {
		this.reportFile = reportFile;
	}

	public void generateReport(List<APIResponse> responses) throws IOException {
		copyHTMLReport(responses, reportFile, TEMPLATE_FILE);
	}
	
	private void copyHTMLReport(List<APIResponse> responses, String fileName, String templateFile) throws IOException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.NullLogChute");
		engine.setProperty("resource.loader", "file");
		engine.setProperty("file.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
		VelocityContext context = new VelocityContext();
		String json = new Gson().toJson(responses);
		context.put("diff_json", json.replace("\\", "\\\\").replace("`", "\\`"));
		Template template = engine.getTemplate(templateFile);
		File f = new File(fileName);
		FileWriter writer = new FileWriter(f);
		template.merge(context, writer);
		writer.flush();
		writer.close();
	}
}
