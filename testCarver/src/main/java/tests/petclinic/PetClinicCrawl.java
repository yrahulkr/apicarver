package tests.petclinic;

import com.apicarv.testCarver.uitestrunner.CrawljaxStub;
import com.apicarv.testCarver.utils.UtilsMiner;
import org.junit.Test;

public class PetClinicCrawl {

	@Test
	public void testCrawl() throws Exception {
//		String[] args = {"petclinic", "DOM_RTED", "5", "0.0"};
		String[] args = {"petclinic", "HYBRID", String.valueOf(UtilsMiner.CRAWL_TIME)};
		CrawljaxStub.main(args);
	}

}
