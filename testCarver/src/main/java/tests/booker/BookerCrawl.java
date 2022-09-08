package tests.booker;

import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.Test;

import com.apicarv.testCarver.uitestrunner.CrawljaxStub;

public class BookerCrawl {

	@Test
	public void testCrawl() throws Exception {
//		String[] args = {"petclinic", "DOM_RTED", "5", "0.0"};
		String[] args = {"booker", "HYBRID", String.valueOf(UtilsMiner.CRAWL_TIME)};
		CrawljaxStub.main(args);
	}

}
