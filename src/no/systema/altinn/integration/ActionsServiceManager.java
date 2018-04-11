package no.systema.altinn.integration;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jakewharton.fliptables.FlipTableConverters;

import de.otto.edison.hal.Link;
import no.systema.altinn.entities.ApiKey;
import no.systema.altinn.entities.MessagesHalRepresentation;
import no.systema.altinn.entities.PrettyPrintAttachments;
import no.systema.altinn.entities.PrettyPrintMessages;
import no.systema.altinn.entities.ServiceCode;
import no.systema.altinn.entities.ServiceEdition;
import no.systema.altinn.entities.ServiceOwner;
import no.systema.altinn.entities.Status;
import no.systema.jservices.common.dao.FirmaltDao;
import no.systema.jservices.common.dao.services.FirmaltDaoService;
import no.systema.jservices.common.util.DateTimeManager;

/**
 * The responsible service manager for accessing resources inside www.altinn.no <br>
 * 
 * Implementing part of actions found here: https://www.altinn.no/api/Help <br>
 * 
 * 
 * @author Fredrik Möller
 * @date 2018-01
 *
 */
//@EnableScheduling
@Service("actionsservicemanager")
public class ActionsServiceManager {
	private static Logger logger = Logger.getLogger(ActionsServiceManager.class);
	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd"); //as defined in Firmalt
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");  //as defined in Firmalt.
	
	@Autowired
	private Authorization authorization;
	
	@Autowired
	private FirmaltDaoService firmaltDaoService;

	@Value("${altinn.access.use.proxy}")
    String useProxy;	
	
    @Value("${altinn.access.proxy.host}")
    String proxyHost;

    @Value("${altinn.access.proxy.port}")
    String port;	
	
    @PostConstruct 
    public void init(){
    	LocalDateTime now = LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss SS");
        
        now.format(formatter);
    	
      logger.info("ActionsServiceManager has been initialized, time="+now.format(formatter));
    }
    
    
    
	private RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

//		logger.info("useProxy="+useProxy);
//		logger.debug("proxyHost="+proxyHost+", port="+port);
		
		if (Boolean.valueOf(useProxy)) {
	        int portNr = -1;
	        try {
	            portNr = Integer.parseInt(port);
	        } catch (NumberFormatException e) {
	            logger.error("Unable to parse the proxy port number");
	            throw new RuntimeException("Unable to parse the proxy port number", e);
	        }

	        InetSocketAddress address = new InetSocketAddress(proxyHost,portNr);
	        Proxy proxy = new Proxy(Proxy.Type.HTTP,address);
	        requestFactory.setProxy(proxy);			
			
	    	logger.debug("Proxy set to: "+ proxyHost + ":"+portNr);	
		} else {
    		requestFactory.setProxy(Proxy.NO_PROXY);	  
    		logger.debug("NO_PROXY set. ");			
		}
	    
	    return new RestTemplate(requestFactory);		
		
	}	
	
	/**
	 * Get all messages for orgnr
	 * 
	 * Executed manually, from DownloadController.
	 * 
	 * @see {@link ActionsUriBuilder}
	 * @param forceDetails -  convenience for troubleshooting, typical use is false.
	 * @param ignoreStatus -  adding real filter on fromDate = 10.
	 * @return List<PrettyPrintMessages>
	 */
	public List<PrettyPrintMessages> getMessages(boolean forceDetails, boolean ignoreStatus) {
		final List<PrettyPrintMessages> result = new ArrayList<PrettyPrintMessages>();
		List<FirmaltDao> firmaltDaoList =null;

		try {
			firmaltDaoList = getSaneFirmaltRecords();
		} catch (Exception e) {
			logger.fatal("Something wrong with FIRMALT, aborting.");
			return result;
		}
		
		firmaltDaoList.forEach(firmalt -> {
			URI uri = ActionsUriBuilder.messages(firmalt.getAihost(), firmalt.getAiorg());
			if (forceDetails) {
				List<MessagesHalRepresentation> messages = getMessages(uri, firmalt);

				messages.forEach((message) -> {
					String self = message.getLinks().getLinksBy("self").get(0).getHref();
					MessagesHalRepresentation halMessage = getMessage(URI.create(self),firmalt);
					PrettyPrintMessages log = new PrettyPrintMessages(firmalt.getAiorg(), LocalDate.now().toString(),halMessage.getCreatedDate().toString(), 
							halMessage.getSubject(), halMessage.getServiceOwner(), halMessage.getServiceCode(), halMessage.getServiceEdition(), halMessage.getStatus() );

					result.add(log);
				});
	
			} else {
				if (ignoreStatus) {
				
					logger.info("ignoreStatus, get all messages...");
					List<MessagesHalRepresentation> messages = getMessages(uri, firmalt);
	
					messages.forEach((message) -> {
						PrettyPrintMessages log = new PrettyPrintMessages(firmalt.getAiorg(), LocalDate.now().toString(),message.getCreatedDate().toString(), 
								message.getSubject(), message.getServiceOwner(), message.getServiceCode(), message.getServiceEdition(), message.getStatus()  );
	
						result.add(log);
					});	
				} else {
					
					logger.info("NOT ignoreStatus, get all messages with real filter, minus 10 days...");
					LocalDate fromDate = getLatestDownloadDate(firmalt).minusDays(10);				
					
					List<MessagesHalRepresentation> dagsobjors = new ArrayList<MessagesHalRepresentation>();
					
					List<MessagesHalRepresentation> dagsobjorsUlest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, fromDate,firmalt, Status.Ulest);
					dagsobjors.addAll(dagsobjorsUlest);
					logger.info("dagsobjorsUlest: On fromDate="+fromDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Ulest.getCode() );
					
					List<MessagesHalRepresentation> dagsobjorsLest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, fromDate,firmalt, Status.Lest);
					dagsobjors.addAll(dagsobjorsLest);
					logger.info("dagsobjorsLest: On fromDate="+fromDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Lest.getCode() );
					
					/** 2018_03-02
					 * Det har også blitt oppdaget en feil i oppsettet for enkelttjeneste for den nye ordningen for dagsoppgjør. Denne feilen berører kun de som ønsker å tildele enkeltpersoner enkelttjenester i Altinn. 
					* For å løse dette søk opp 4125/150602 "Brev til etterskuddspliktige" og velg denne. I tillegg er det laget en ny enkelttjeneste som er riktig 5012/171208 "Elektronisk kontoutskrift tollkreditt og dagsoppgjør" som vil være gyldig i løpet av 3-4 uker. Tildel denne samtidig og den vil automatisk bli tatt i bruk når den nye tjenesten er klar.
					* Har en rolle som "Regnskapsmedarbeider" vil en uansett ha tilgang til å laste ned PDF- og e2b-fil fra Altinn og vil ikke bli berørt av endringen.
					 */
					//TODO: To be removed when 5012/171208 is working. Planned to work  2018-03/2018-04
					List<MessagesHalRepresentation> dagsobjorsFIXUlest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.DagsobjorFIX, ServiceEdition.DagsobjorFIX, fromDate,firmalt, Status.Ulest);
					dagsobjors.addAll(dagsobjorsFIXUlest);
					logger.info("dagsobjorsFIXUlest On fromDate="+fromDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Ulest.getCode() );
					
					List<MessagesHalRepresentation> dagsobjorsFIXLest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.DagsobjorFIX, ServiceEdition.DagsobjorFIX, fromDate,firmalt, Status.Lest);
					dagsobjors.addAll(dagsobjorsFIXLest);
					logger.info("dagsobjorsFIXLest On fromDate="+fromDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Lest.getCode() );
					
					dagsobjors.forEach((message) -> {
						PrettyPrintMessages log = new PrettyPrintMessages(firmalt.getAiorg(), LocalDate.now().toString(),message.getCreatedDate().toString(), 
								message.getSubject(), message.getServiceOwner(), message.getServiceCode(), message.getServiceEdition(), message.getStatus()  );
	
						result.add(log);
					});						
					
				}
			
			}
		});

		return result;

	}
	
	/**
	 * Get all message for orgnr and specific {@link ServiceOwner}, {@link ServiceOwner}, {@link ServiceEdition}
	 * 
	 * @see {@link ActionsUriBuilder}
	 * @param orgnr
	 * @param serviceOwner
	 * @param serviceCode
	 * @param serviceEdition
	 * @return List<MessagesHalRepresentation>
	 */
	private List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition, FirmaltDao firmalt) {
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		URI uri = ActionsUriBuilder.messages(firmalt.getAihost(),  firmalt.getAiorg(),serviceOwner,serviceCode, serviceEdition);
		result.addAll(getMessages(uri,firmalt));
		
		return result;
		
	}	
	/*
	 * Adding CreatedDate to Odata-filter.
	 * 
	 * Only executed manually
	 */
	private List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition, LocalDate createdDate, FirmaltDao firmalt) {
		logger.info("About to get message greater than "+createdDate+ " for orgnr:"+firmalt.getAiorg());
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		URI uri = ActionsUriBuilder.messages(firmalt.getAihost(), firmalt.getAiorg(), serviceOwner, serviceCode, serviceEdition, createdDate);

		result.addAll(getMessages(uri, firmalt));
		
		return result;
	
	}
	/*
	 * Exclude messages, typically with Status "Ulest" or "Lest".
	 */
	private List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition, LocalDate latestDownloadDate, FirmaltDao firmalt, Status status) {
		logger.info("About to get message greater than "+latestDownloadDate+ " for orgnr:"+firmalt.getAiorg()+ ", and Status:"+status.getCode());
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		URI uri = ActionsUriBuilder.messages(firmalt.getAihost(), firmalt.getAiorg(), serviceOwner, serviceCode, serviceEdition, latestDownloadDate, status);

		result.addAll(getMessages(uri, firmalt));
		
		return result;
	
	}		
	
	
	/**
	 * Retrieves all attachment for ServiceOwner.Skatteetaten, ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, for today and stores as defined in {@linkplain FirmaltDao}.aipath
	 * 
	 * Note: this is not using Status-filter.
	 * 
	 * Executed manually
	 * 
	 * @param forceAll removes filter day, convenience for troubleshooting manually, typical use is false.
	 * @param gtDato 
	 * @return List of fileNames
	 */
	public List<PrettyPrintAttachments> putDagsobjorAttachmentsToPath(boolean forceAll, LocalDate gtDato) {
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		List<FirmaltDao> firmaltDaoList =null;

		try {
			firmaltDaoList = getSaneFirmaltRecords();
		} catch (Exception e) {
			logger.fatal("Something wrong with FIRMALT, aborting.");
			return logRecords;
		}		

		
		firmaltDaoList.forEach(firmalt -> {
			if (gtDato != null || forceAll) {
				List<MessagesHalRepresentation> dagsobjors = null;
				if (gtDato != null) {
					logger.info("Orgnr:"+firmalt.getAiorg()+ ", downloading fraDato-filtered messages from "+gtDato+", from Skatteeten on Dagsoppgjor");
					logger.info("fraDato="+gtDato);
					dagsobjors = getMessages(ServiceOwner.Skatteetaten, ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, gtDato, firmalt);

					/** 2018_03-02
					 * Det har også blitt oppdaget en feil i oppsettet for enkelttjeneste for den nye ordningen for dagsoppgjør. Denne feilen berører kun de som ønsker å tildele enkeltpersoner enkelttjenester i Altinn. 
					* For å løse dette søk opp 4125/150602 "Brev til etterskuddspliktige" og velg denne. I tillegg er det laget en ny enkelttjeneste som er riktig 5012/171208 "Elektronisk kontoutskrift tollkreditt og dagsoppgjør" som vil være gyldig i løpet av 3-4 uker. Tildel denne samtidig og den vil automatisk bli tatt i bruk når den nye tjenesten er klar.
					* Har en rolle som "Regnskapsmedarbeider" vil en uansett ha tilgang til å laste ned PDF- og e2b-fil fra Altinn og vil ikke bli berørt av endringen.
					 */
					//TODO: To be removed when 5012/171208 is working. Planned to work  2018-03/2018-04
					List<MessagesHalRepresentation> dagsobjorsFIX = getMessages(ServiceOwner.Skatteetaten,ServiceCode.DagsobjorFIX, ServiceEdition.DagsobjorFIX, gtDato,firmalt);
					logger.info(dagsobjorsFIX.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.DagsobjorFIX.getCode()+", ServiceEdition="+ServiceEdition.DagsobjorFIX.getCode());
					dagsobjors.addAll(dagsobjorsFIX);					
					
				
				} else {  //forceAll
					logger.info("Orgnr:"+firmalt.getAiorg()+ ", downloading all messages from Skatteeten on Dagsoppgjor");
					dagsobjors = getMessages(ServiceOwner.Skatteetaten, ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, firmalt);

					/** 2018_03-02
					 * Det har også blitt oppdaget en feil i oppsettet for enkelttjeneste for den nye ordningen for dagsoppgjør. Denne feilen berører kun de som ønsker å tildele enkeltpersoner enkelttjenester i Altinn. 
					* For å løse dette søk opp 4125/150602 "Brev til etterskuddspliktige" og velg denne. I tillegg er det laget en ny enkelttjeneste som er riktig 5012/171208 "Elektronisk kontoutskrift tollkreditt og dagsoppgjør" som vil være gyldig i løpet av 3-4 uker. Tildel denne samtidig og den vil automatisk bli tatt i bruk når den nye tjenesten er klar.
					* Har en rolle som "Regnskapsmedarbeider" vil en uansett ha tilgang til å laste ned PDF- og e2b-fil fra Altinn og vil ikke bli berørt av endringen.
					 */
					//TODO: To be removed when 5012/171208 is working. Planned to work  2018-03/2018-04
					List<MessagesHalRepresentation> dagsobjorsFIX = getMessages(ServiceOwner.Skatteetaten,ServiceCode.DagsobjorFIX, ServiceEdition.DagsobjorFIX, firmalt);
					logger.info(dagsobjorsFIX.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.DagsobjorFIX.getCode()+", ServiceEdition="+ServiceEdition.DagsobjorFIX.getCode());
					dagsobjors.addAll(dagsobjorsFIX);						
				
				}

				
				dagsobjors.forEach((message) -> {
					logRecords.addAll(getAttachments(message, firmalt));
				});
				
				if (!dagsobjors.isEmpty()) {
					updateDownloadDato(firmalt);
				}
				logger.info("Orgnr:"+firmalt.getAiorg()+ ", " +dagsobjors.size()+" dagsoppgjor downloaded, with "+logRecords.size()+" attachments.");
			} else {
				logger.info("Orgnr:"+firmalt.getAiorg()+ ", downloading if not downloaded today.");
				if (!isDownloadedToday(firmalt)) {
					logRecords.addAll(getDagsoppgjor(firmalt));
				}
				logger.info("Orgnr:"+firmalt.getAiorg()+ " with "+logRecords.size()+" attachments.");

			}
		});

		logger.info("putDagsobjorAttachmentsToPath executed, with forceAll="+forceAll+", fraDato="+gtDato);
		logger.info(FlipTableConverters.fromIterable(logRecords, PrettyPrintAttachments.class));
		
		return logRecords;

	}


//	@Scheduled(cron="${altinn.file.download.cron.pattern}")
//	@Scheduled(cron="0 */5 * * * *")  //5 min
	public void putDagsobjorAttachmentsToPath() {
		logger.debug("::putDagsobjorAttachmentsToPath (Started by Scheduler)::");
		LocalDateTime now = LocalDateTime.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss SS");
        
       logger.info("putDagsobjorAttachmentsToPath() start running, time="+now.format(formatter));
		
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		List<FirmaltDao> firmaltDaoList = null;

		try {
			firmaltDaoList = getSaneFirmaltRecords();
		} catch (Exception e) {
			logger.fatal("Something wrong with FIRMALT, Throwing exception.");
			throw e;
		}
		
		firmaltDaoList.forEach(firmalt -> {
			logger.info("::Started by Scheduler::Actual values in FIRMALT="+ReflectionToStringBuilder.toString(firmalt));
			logger.info("::Started by Scheduler:: :Get messages for orgnnr:"+firmalt.getAiorg());
			if (!isDownloadedToday(firmalt)) {
				logRecords.addAll(getDagsoppgjor(firmalt));	

				logger.info("::Started by Scheduler:: download of Dagsoppgjors attachments is executed.");
				logger.info(FlipTableConverters.fromIterable(logRecords, PrettyPrintAttachments.class));
			} else {
				logger.info("::Started by Scheduler:: orgnnr:"+firmalt.getAiorg() +" Already downloaded today.");
			}
			
		});
		
	}	
	
	private List<PrettyPrintAttachments> getDagsoppgjor(FirmaltDao firmalt) {
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		LocalDate latestDownloadDate = getLatestDownloadDate(firmalt);
		
		List<MessagesHalRepresentation> dagsobjors = new ArrayList<MessagesHalRepresentation>();
		
		List<MessagesHalRepresentation> dagsobjorsUlest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, latestDownloadDate,firmalt, Status.Ulest);
		dagsobjors.addAll(dagsobjorsUlest);
		logger.info("dagsobjorsUlest: On createdDate="+latestDownloadDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Ulest.getCode() );
		
		List<MessagesHalRepresentation> dagsobjorsLest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, latestDownloadDate,firmalt, Status.Lest);
		dagsobjors.addAll(dagsobjorsLest);
		logger.info("dagsobjorsLest: On createdDate="+latestDownloadDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Lest.getCode() );
		
		/** 2018_03-02
		 * Det har også blitt oppdaget en feil i oppsettet for enkelttjeneste for den nye ordningen for dagsoppgjør. Denne feilen berører kun de som ønsker å tildele enkeltpersoner enkelttjenester i Altinn. 
		* For å løse dette søk opp 4125/150602 "Brev til etterskuddspliktige" og velg denne. I tillegg er det laget en ny enkelttjeneste som er riktig 5012/171208 "Elektronisk kontoutskrift tollkreditt og dagsoppgjør" som vil være gyldig i løpet av 3-4 uker. Tildel denne samtidig og den vil automatisk bli tatt i bruk når den nye tjenesten er klar.
		* Har en rolle som "Regnskapsmedarbeider" vil en uansett ha tilgang til å laste ned PDF- og e2b-fil fra Altinn og vil ikke bli berørt av endringen.
		 */
		//TODO: To be removed when 5012/171208 is working. Planned to work  2018-03/2018-04
		List<MessagesHalRepresentation> dagsobjorsFIXUlest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.DagsobjorFIX, ServiceEdition.DagsobjorFIX, latestDownloadDate,firmalt, Status.Ulest);
		dagsobjors.addAll(dagsobjorsFIXUlest);
		logger.info("dagsobjorsFIXUlest On createdDate="+latestDownloadDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Ulest.getCode() );
		
		List<MessagesHalRepresentation> dagsobjorsFIXLest = getMessages(ServiceOwner.Skatteetaten,ServiceCode.DagsobjorFIX, ServiceEdition.DagsobjorFIX, latestDownloadDate,firmalt, Status.Lest);
		dagsobjors.addAll(dagsobjorsFIXLest);
		logger.info("dagsobjorsFIXLest On createdDate="+latestDownloadDate +", " + dagsobjors.size() +" messages found on ServiceOwner="+ServiceOwner.Skatteetaten.getCode()+", ServiceCode="+ServiceCode.Dagsobjor.getCode()+", ServiceEdition="+ServiceEdition.Dagsobjor.getCode()+", Status="+Status.Lest.getCode() );
		
		dagsobjors.forEach((message) -> {
			logRecords.addAll(getAttachments(message, firmalt));
		});					

		if (!dagsobjors.isEmpty()) {
			updateDownloadDato(firmalt);
		}
		logger.info("Orgnr:"+firmalt.getAiorg()+ ", " +dagsobjors.size()+" Dagsoppgjor downloaded, with "+logRecords.size()+" attachments.");
	
		return logRecords;
		
	}
	
	
	private LocalDate getLatestDownloadDate(FirmaltDao firmalt) {
		int aidato;
		String aidatoString;
		if (firmalt.getAidato() == 0) {
			throw new RuntimeException("FIRMALT.aidato not set!");
		} else {
			aidato = firmalt.getAidato();
			aidatoString = String.valueOf(aidato);
		}

		LocalDate fromDate = LocalDate.parse(aidatoString, dateFormatter);
		
		return  fromDate;

	}	
	
	
	
	private void updateDownloadDato(FirmaltDao firmalt) {
		LocalDateTime now = LocalDateTime.now();
		String nowDate = now.format(dateFormatter);
		int aidato = Integer.valueOf(nowDate);
		
		String nowTime = now.format(timeFormatter);
		int aitid = Integer.valueOf(nowTime);		
		
		firmalt.setAidato(aidato);
		firmalt.setAitid(aitid);
		firmaltDaoService.updateAiDatoAndAiTid(firmalt);
		
		logger.info("Orgnr:"+firmalt.getAiorg()+" updated; aidato="+firmalt.getAidato()+", aitid="+firmalt.getAitid());
		
	}

	private boolean isDownloadedToday(FirmaltDao firmalt) {
		//Sanity check
		if (firmalt.getAidato() == 0) {
			throw new RuntimeException("FIRMALT.aidato not set!");
		}
		
		if (firmalt.getAidato() < getCurrentDate() ) {
			logger.info("Files for orgnr:"+firmalt.getAiorg()+" not downloaded today. About to executed download...");
			return false;
		} else {
			logger.info("Files for orgnr:"+firmalt.getAiorg()+" downloaded today.");
			return true;
		}

	}	
	
	private int getCurrentDate() {
		DateTimeManager dtm = new DateTimeManager();
		String nowString = dtm.getCurrentDate_ISO();
		int now = Integer.valueOf(nowString);
		
		return now;
	}
	
	/*
	 * Get all attachments in message, e.i. PDF and XML
	 */
	private List<PrettyPrintAttachments> getAttachments(MessagesHalRepresentation message, FirmaltDao firmalt) {
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		String self = message.getLinks().getLinksBy("self").get(0).getHref();
		
		URI uri = URI.create(self);
		//Get specific message
		MessagesHalRepresentation halMessage = getMessage(uri, firmalt);
		
		List<Link> attachmentsLink =halMessage.getLinks().getLinksBy("attachment");
		
		attachmentsLink.forEach((attLink) -> {
			URI attUri = URI.create(attLink.getHref());
			//Prefix Altinn-name with created_date
			StringBuilder writeFile;
			if (attLink.getName().endsWith(".pdf") || attLink.getName().endsWith(".xml")) { 
				writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName());
			} else {
				/*2018-03: Could be lead to problem in future if xml name is changed.
				 * be aware....
				 */
				if (attLink.getName().contains("xml"))  {
					writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName()).append(".xml");
				} else {
					writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName()).append(".pdf");
				}
			}
			getAttachment(attUri, writeFile.toString(), firmalt);
			PrettyPrintAttachments log = new PrettyPrintAttachments(firmalt.getAiorg(), LocalDateTime.now().toString(),halMessage.getCreatedDate().toString(), writeFile.toString(), halMessage.getServiceOwner(), halMessage.getStatus() );
			logRecords.add(log);
			
		});
		
		return logRecords;

	}	

	/*
	 * FirmaltDao as param is her due to late fix in model. (logically not really neede.)
	 */
	private List<MessagesHalRepresentation> getMessages(URI uri, FirmaltDao firmaltDao){
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity(firmaltDao);
		ResponseEntity<String> responseEntity = null;
		
		try {

			responseEntity = restTemplate().exchange(uri, HttpMethod.GET, entityHeadersOnly, String.class); 

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getMessage for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			}
			logger.debug("getMessages:responseEntity.getBody"+responseEntity.getBody());
	
	        return HalHelper.getMessages(responseEntity.getBody());
	        
		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}
		
	}	
	
	/*
	 * FirmaltDao as param is her due to late fix in model. (logically not really needed.)
	 */
	private MessagesHalRepresentation getMessage(URI uri, FirmaltDao firmaltDao){
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity(firmaltDao);
		ResponseEntity<String> responseEntity = null;
		
		try {

			responseEntity = restTemplate().exchange(uri, HttpMethod.GET, entityHeadersOnly, String.class); 

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getMessage for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			}
			logger.debug("getMessage:responseEntity.getBody"+responseEntity.getBody());
	
	        return HalHelper.getMessage(responseEntity.getBody());
	        
		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}
		
	}	

	/*
	 * FirmaltDao as param is her due to late fix in model. (logically not really needed.)
	 */
	private void getAttachment(URI uri, String writeFile, FirmaltDao firmaltDao) {
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntityFileDownload(firmaltDao);
		ResponseEntity<byte[]> responseEntity = null;

		try {
			logger.debug("getAttachment, uri=" + uri);

			responseEntity = restTemplate().exchange(uri.toString(), HttpMethod.GET, entityHeadersOnly, byte[].class, "1");

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getAttachment for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			} else {
				logger.debug("getAttachment:responseEntity.getBody"+responseEntity.getBody());
				writeToFile(writeFile, responseEntity, firmaltDao);

			}

		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}

	}

	
	/*
	 * For test
	 * FileOutputStream fos = new FileOutputStream("/usr/local/Cellar/tomcat/8.0.33/libexec/webapps/altinn-proxy/WEB-INF/resources/files/" + writeFile);
	 */
	private void writeToFile(String writeFile, ResponseEntity<byte[]> responseEntity, FirmaltDao firmaltDao) throws FileNotFoundException, IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(responseEntity.getBody());
		FileOutputStream fos = new FileOutputStream(firmaltDao.getAipath() + writeFile);
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = bis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}

		fos.close();
		bis.close();

		logger.info("File: " + firmaltDao.getAipath() + writeFile + " saved on disk.");

	}
	
	
	/**
	 * @return List<FirmaltDao> with all params set.
	 */
	private List<FirmaltDao> getSaneFirmaltRecords()  {
		List<FirmaltDao> firmaltDaoList =firmaltDaoService.get();
		
		if (firmaltDaoList.size() == 0) {
			throw new RuntimeException("Cannot find any records in firmalt, based on keys: aifirm, aiorg");
		}
		
		
		firmaltDaoList.forEach((firmalt) -> {
			try {
				firmalt.hasSaneValues();
				logger.info("Firmalt record seems ok, record="+ReflectionToStringBuilder.toString(firmalt));
			} catch (RuntimeException e) {
				logger.error("Firmalt record has suspicious values, record="+ReflectionToStringBuilder.toString(firmalt), e);
				throw e;
			}
		} ); 
		
		
		return firmaltDaoList;
	}
	
}
