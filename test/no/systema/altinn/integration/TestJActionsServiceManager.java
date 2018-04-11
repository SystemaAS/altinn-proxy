//package no.systema.altinn.integration;
//
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Arrays;
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.function.Predicate;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import no.systema.altinn.entities.MessagesHalRepresentation;
//import no.systema.altinn.entities.PrettyPrintMessages;
//import no.systema.altinn.entities.ServiceCode;
//import no.systema.altinn.entities.ServiceEdition;
//import no.systema.altinn.entities.ServiceOwner;
//import no.systema.altinn.entities.Status;
//import no.systema.jservices.common.dao.FirmaltDao;
//import no.systema.jservices.common.dao.services.FirmaltDaoService;
//import no.systema.jservices.common.util.DateTimeManager;
//
//public class TestJActionsServiceManager {
//
////	ActionsServiceManager serviceManager = null;
//	FirmaltDaoService firmaltDaoService = null;
//	
//	String BAREKSTAD = "810514442";
//	String KIRKENES = "910021451";
//	
////	   @Autowired
////	    public void setProductService(ActionsServiceManager serviceManager) {
////	        this.serviceManager = serviceManager;
////	    }
//	
//	FirmaltDaoService fs;
//	
//	
//	ActionsServiceManager serviceManager;
//	
//	@Before
//	public void setUp() throws Exception {
//        ApplicationContext commonContext = new ClassPathXmlApplicationContext("syjservicescommon-data-service-test.xml");
//        firmaltDaoService = (FirmaltDaoService) commonContext.getBean("firmaltDaoService");
////        serviceManager = new ActionsServiceManager();
////        serviceManager.setFirmaltDaoService(firmaltDaoService);
//	}
//
//	
//	@Test
//	public final void testGetMessages() {
//		int orgnr = 810514442;    //810514442, 910021451
//		List<PrettyPrintMessages> result = serviceManager.getMessages(false, false);
//		
//		System.out.println("result.size="+result.size());
//		
//		assertNotNull(result); 
//	}
//	
//	@Test
//	public final void testGetDagsobjor() {
//		
//		serviceManager.putDagsobjorAttachmentsToPath(false, null);
////		result2.forEach((message) ->  System.out.println("message from "+ServiceOwner.Skatteetaten+":"+message));
//		
//	}
//	
//	@Test
//	public final void testGetDownloadDatoCheck() {
//		FirmaltDao dao = new FirmaltDao();
//		dao.setAidato(20180219);
//		//boolean isDownloaded = serviceManager.isDownloadedToday(dao);
//		boolean isDownloaded = isDownloadedToday(dao);
//		//Code ripped due to bean-loading issues
//		
//		
//		Assert.assertFalse("Should not be set as downloaded.", isDownloaded);
//		
//	}		
//	
//	boolean isDownloadedToday(FirmaltDao firmalt) {
//		//Sanity check
//		if (firmalt.getAidato() == 0) {
//			throw new RuntimeException("FIRMALT.aidato not set!");
//		}
//		DateTimeManager dtm = new DateTimeManager();
//		String nowString = dtm.getCurrentDate_ISO();
//		int now = Integer.valueOf(nowString);
//		
//		System.out.println("nowString="+nowString);
//		System.out.println("now="+now);
//		System.out.println("firmalt.getAidato()="+firmalt.getAidato());
//		
//		if (firmalt.getAidato() < now) {
//			return false;
//		} else {
//			return true;
//		}
//
//	}
//	
//	@Test
//	public final void testDatoCheck() {
//		List<FirmaltDao> firmaltDaoList = firmaltDaoService.get();
//
//		DateTimeManager dtm = new DateTimeManager();
//		String nowString = dtm.getCurrentDate_ISO();
//		int now = Integer.valueOf(nowString);		
//		
////		firmaltDaoList.forEach(dao -> System.out.println(ReflectionToStringBuilder.toString(dao)));
//		firmaltDaoList.forEach(dao -> {
//			System.out.println("aidato=" + dao.getAidato());
//			int dager=  now - dao.getAidato();
//			System.out.println("dager=" + dager);
//			
//		});		
//		
//	}	
//	
//	@Test
//	public final void testAidatoToLocalDate() {
//		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd"); 
//		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");  //as defined in Firmalt.
//	
//		int aidato = 20180220;
//		int aitid = 3;
//		String aidatoString = String.valueOf(aidato);
//		
//		String padded = String.format("%06d", aitid);		
//		
//		assertTrue("Should be padded.",padded.length() == 6);
//
//
//		LocalDate fromDate = LocalDate.parse(aidatoString, dateFormatter);
//		LocalTime fromTime = LocalTime.parse(padded, timeFormatter);
//		
//		System.out.println("aidato"+aidato+", fromDate="+fromDate);
//		LocalDateTime ldt = LocalDateTime.of(fromDate, fromTime);
//		System.out.println("padded"+padded+", fromTime="+fromTime);
//
//
//	}
//
//	@Test
//	public final void testIntDateToLocalDate() {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//	    String nowString = "20161109";
//	    int now = Integer.valueOf(nowString);
//
//	    LocalDate formatDateTime = LocalDate.parse(nowString, formatter);
//
//	    System.out.println("Before : " + nowString);
//
//	    System.out.println("After : " + formatDateTime);
//
//	    System.out.println("After : " + formatDateTime.format(formatter));
//
//	}	
//	
//	@Test
//	public final void findInList() {
//		final List<String> DHL_ORGNR = Arrays.asList("936972403", "972871400", "971959266", "972211222");
//		String orgnr = "123";
//		Predicate<String> filterPredicate = e -> e == orgnr;
//		Consumer<String> printConsumer = e -> System.out.println("e=" + e);
//		DHL_ORGNR.stream().filter(filterPredicate).forEach(printConsumer);
//
//	}
//	
//	
//	static void printJsonView(List<MessagesHalRepresentation> messages)  {
//	   final ObjectMapper mapper = new ObjectMapper();
//	    
//	   messages.forEach((message) ->  {
//		try {
//			System.out.println(mapper.writeValueAsString(message));
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	});	
//	}
//	
//	
//}
