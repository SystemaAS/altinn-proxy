package no.systema.altinn.proxy;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpEntity;

import no.systema.altinn.entities.ApiKey;
import no.systema.jservices.common.dao.services.FirmaltDaoService;

@PropertySource(value = { "classpath:application-test.properties" })
public class TestJAuthorization {
	private static Logger logger = Logger.getLogger(TestJAuthorization.class.getName());
	Authorization auth = null;
	ApplicationContext context = null;
	ApplicationContext context2 = null;
	FirmaltDaoService firmaltDaoService = null;

	@Before
	public void setUp() throws Exception {
        AbstractApplicationContext  context = new AnnotationConfigApplicationContext(TestAppConfig.class);
        
		context2 = new ClassPathXmlApplicationContext("syjservicescommon-data-service-test.xml");
		firmaltDaoService = (FirmaltDaoService) context.getBean("firmaltDaoService");
        
//        auth = (Authorization) context.getBean("authorization");
        
        auth = new Authorization();
        
        assert firmaltDaoService != null;
//        firmaltDaoService.  jdbstemplate initieras inte!
        
//        logger.info("firmaltDaoService="+firmaltDaoService);
//        auth.setFirmaltDaoService(firmaltDaoService);
        context.close();	
	}

	@Test
	public void testGetHttpEntity() {
		HttpEntity<ApiKey> ent = auth.getHttpEntity();
		logger.info("ent="+ent);
		assertNotNull("checking", ent);
	}	
	
}
