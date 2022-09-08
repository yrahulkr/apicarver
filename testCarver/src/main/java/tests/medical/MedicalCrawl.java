package tests.medical;

import com.apicarv.testCarver.uitestrunner.CrawljaxStub;
import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.Test;

public class MedicalCrawl {

	@Test
	public void crawl() throws Exception {
		String URL = "http://localhost:3000";
		CrawljaxStub.run(new MedicalCrawlingRules(), "medical", "HYBRID", UtilsMiner.CRAWL_TIME, -1, -1, URL);
	}

//	@Test
//	public void crawlShop() throws Exception{
//		String URL = "http://localhost:3000";
//		CrawljaxStub.run(new MedicalDoctorCrawlingRules(), "medical", "HYBRID", UtilsMiner.CRAWL_TIME, -1, -1, URL);
//	}
}
