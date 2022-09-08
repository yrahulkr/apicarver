package tests.ecomm;

import com.apicarv.testCarver.uitestrunner.CrawljaxStub;
import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.Test;

public class EcommCrawl {

	@Test
	public void testCrawl() throws Exception {
//		String[] args = {"petclinic", "DOM_RTED", "5", "0.0"};
		String[] args = {"ecomm", "HYBRID", String.valueOf(UtilsMiner.CRAWL_TIME)};
		CrawljaxStub.main(args);
	}

}
