package tests.realworld;

import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.Test;

import com.apicarv.testCarver.uitestrunner.CrawljaxStub;

public class RealworldCrawl {

	@Test
	public void testCrawl() throws Exception {
		String[] args = {"realworld", "HYBRID", String.valueOf(UtilsMiner.CRAWL_TIME)};
		CrawljaxStub.main(args);
	}

}
