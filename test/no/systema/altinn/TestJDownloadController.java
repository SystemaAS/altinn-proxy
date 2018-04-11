//package no.systema.altinn;
//
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.fail;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import no.systema.altinn.integration.TestAppConfig;
//import no.systema.altinn.integration.TestAppConfig2;
//
////TODO dependency injection funkar inte
////@RunWith(SpringJUnit4ClassRunner.class)
////@ContextConfiguration(classes = {TestAppConfig.class, TestAppConfig2.class})
//public class TestJDownloadController {
//
//	DownloadController downloadController = null;
//	
////	@Before
//	public void setUp() throws Exception {
//		downloadController = new DownloadController();
//	
//	}
//
//	@Test
//	public void test() {	
//		String res=downloadController.readInnboks(null, null);
//		
//		assertNotNull(res);
//		
//		fail("Not yet implemented");
//	}
//
//}
