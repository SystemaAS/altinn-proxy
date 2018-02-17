package no.systema.altinn.integration;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.systema.altinn.entities.MessagesHalRepresentation;
import no.systema.altinn.entities.ServiceCode;
import no.systema.altinn.entities.ServiceEdition;
import no.systema.altinn.entities.ServiceOwner;
import no.systema.jservices.common.dao.FirmaltDao;
import no.systema.jservices.common.dao.services.FirmaltDaoService;

@PropertySource(value = { "classpath:application-test.properties" })
public class TestJActionsServiceManager {

	ActionsServiceManager serviceManager = null;
	FirmaltDaoService firmaltDaoService = null;
	
	String BAREKSTAD = "810514442";
	String KIRKENES = "910021451";
	
	
	@Before
	public void setUp() throws Exception {
        AbstractApplicationContext  context = new AnnotationConfigApplicationContext(TestAppConfig.class);
        serviceManager = (ActionsServiceManager) context.getBean("actionsservicemanager");
        firmaltDaoService = (FirmaltDaoService) context.getBean("firmaltDaoService");
        context.close();			
		
	}

	//Från Erlend
//	Testorganisasjon: 810514442      BAREKSTAD OG YTTERVÅG REGNSKAP  (systema)
//
//	Sertifikatpassord: KRw16s7XVQuyA3ed
//
//	Daglig leder: 20015001543           ANTONIO MALIK            
//
//	 
//
//	Testorganisasjon2: 910021451 KIRKENES OG AUSTBØ (KUNDE)
//
//	Daglig leder 2: 06117701547 Rolf Bjørn	
	
	//Från Mats
//	Vedlagt ligger innloggingsinformasjon til Altinn hvor testdata er lagt inn.
//
//	 
//
//	Fødselsnummer for innlogging:
//
//	06117701547
//
//	 
//
//	Organisasjonsnummer:
//
//	910021451
//
//	 
//
//	Navn på organisasjon:
//
//	Kirkenes og Austbø	
	
	
	
	@Test
	public final void testGetMessages() {
		int orgnr = 810514442;    //810514442, 910021451
		List<MessagesHalRepresentation> result = serviceManager.getMessages(false);
		
		System.out.println("result.size="+result.size());
		
		assertNotNull(result); 
	}
	
	@Test
	public final void testGetMessagesForServiceOwner_Samlesider() {
		int orgnr = 810514442;    //810514442, 910021451

		List<MessagesHalRepresentation> result = serviceManager.getMessages(ServiceOwner.Samlesider);
		result.forEach((message) ->  System.out.println("message from "+ServiceOwner.Samlesider+":"+message));
		
		assertNotNull(result); 
	}	
	
	@Test
	public final void testGetMessagesForServiceOwner_ServiceCode_ServiceEdition() {
		int orgnr = 810514442;    //810514442, 910021451

		List<FirmaltDao> firmaltList = firmaltDaoService.get();
		assertNotNull(firmaltList);
		
		List<MessagesHalRepresentation> result2 = serviceManager.getMessages(ServiceOwner.Skatteetaten, ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, firmaltList.get(0));
//		result2.forEach((message) ->  System.out.println("message from "+ServiceOwner.Skatteetaten+":"+message));
		
		printJsonView(result2);
		
	}
	
	@Test
	public final void testGetDagsobjor() {
		
		serviceManager.putDagsobjorAttachmentsToPath(false);
//		result2.forEach((message) ->  System.out.println("message from "+ServiceOwner.Skatteetaten+":"+message));
		
	}	

	static void printJsonView(List<MessagesHalRepresentation> messages)  {
	   final ObjectMapper mapper = new ObjectMapper();
	    
	   messages.forEach((message) ->  {
		try {
			System.out.println(mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	});	
	}
	
	
}
