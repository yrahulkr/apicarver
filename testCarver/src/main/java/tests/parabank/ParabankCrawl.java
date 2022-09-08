package tests.parabank;

import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.Test;

import com.apicarv.testCarver.uitestrunner.CrawljaxStub;

public class ParabankCrawl {

	@Test
	public void testCrawl() throws Exception {
//		String[] args = {"petclinic", "DOM_RTED", "5", "0.0"};
		String[] args = {"parabank", "HYBRID", String.valueOf(UtilsMiner.CRAWL_TIME)};
		CrawljaxStub.main(args);
	}

}
