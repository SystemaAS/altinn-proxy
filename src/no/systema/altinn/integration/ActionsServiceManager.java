package no.systema.altinn.integration;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jakewharton.fliptables.FlipTableConverters;

import de.otto.edison.hal.Link;
import no.systema.altinn.entities.ApiKey;
import no.systema.altinn.entities.MessagesHalRepresentation;
import no.systema.altinn.entities.PrettyPrintAttachments;
import no.systema.altinn.entities.ServiceCode;
import no.systema.altinn.entities.ServiceEdition;
import no.systema.altinn.entities.ServiceOwner;
import no.systema.jservices.common.dao.FirmaltDao;

/**
 * The responsible service manager for accessing resources inside www.altinn.no <br>
 * 
 * Implementing part of actions found here: https://www.altinn.no/api/Help <br>
 * 
 * This class is assuming that {@linkplain FirmaltDao} is prepared as a List in {@linkplain Authorization} <br>
 * 
 * @author Fredrik Möller
 * @date 2018-01
 *
 */
@EnableScheduling
@Service("actionsservicemanager")
public class ActionsServiceManager {
	private static Logger logger = Logger.getLogger(ActionsServiceManager.class.getName());
	
	@Autowired
	private Authorization authorization;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}	
	
	@Autowired
	private RestTemplate restTemplate;
	
	/**
	 * Get all messages for orgnr
	 * 
	 * @see {@link ActionsUriBuilder}
	 * @param forceDetails, convenience for troubleshooting, typical use is false.
	 * @return List<MessagesHalRepresentation>
	 */
	public List<MessagesHalRepresentation> getMessages(boolean forceDetails) {
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		authorization.getFirmaltDaoList().forEach(firmalt -> {
			URI uri = ActionsUriBuilder.messages(firmalt.getAihost(), firmalt.getAiorg());
			if (forceDetails) {
				List<MessagesHalRepresentation> messages = getMessages(uri, firmalt);
				messages.forEach((message) -> {
					String self = message.getLinks().getLinksBy("self").get(0).getHref();
					result.add(getMessage(URI.create(self),firmalt));
				});
	
			} else {
				result.addAll(getMessages(uri, firmalt));
			}
		});

		return result;

	}
	
	/**
	 * Get all message for orgnr and specific {@link ServiceOwner}
	 * 
	 * @see {@link ActionsUriBuilder}
	 * @param orgnr
	 * @param serviceOwner
	 * @return List<MessagesHalRepresentation>
	 */
	public List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner) {
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		authorization.getFirmaltDaoList().forEach(firmalt -> {		
			URI uri = ActionsUriBuilder.messages(firmalt.getAihost(),  firmalt.getAiorg(),serviceOwner);
			result.addAll(getMessages(uri,firmalt));
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
	public List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition, FirmaltDao firmalt) {
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		URI uri = ActionsUriBuilder.messages(firmalt.getAihost(),  firmalt.getAiorg(),serviceOwner,serviceCode, serviceEdition);
		result.addAll(getMessages(uri,firmalt));
		
		return result;
		
	}	
	
	/**
	 * Get all message for orgnr and specific {@link ServiceOwner}, {@link ServiceOwner}, {@link ServiceEdition} and yesterday
	 * 
	 * @see {@link ActionsUriBuilder}
	 * @param orgnr
	 * @param serviceOwner
	 * @param serviceCode
	 * @param serviceEdition
	 * @param date
	 * @return List<MessagesHalRepresentation>
	 */
	private List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition, LocalDateTime yesterday, FirmaltDao firmalt) {
		logger.info("About to get message greater than "+yesterday);
		final List<MessagesHalRepresentation> result = new ArrayList<MessagesHalRepresentation>();
		URI uri = ActionsUriBuilder.messages(firmalt.getAihost(), firmalt.getAiorg(), serviceOwner, serviceCode, serviceEdition, yesterday);
		result.addAll(getMessages(uri, firmalt));
		
		return result;
	
	}	
	
	/**
	 * Retrieves all attachment in Melding: Dagsoppgjor, for today <br>
	 * and stores as defined in {@linkplain FirmaltDao}.aipath
	 * 
	 * @param forceAll, convenience for troubleshooting, typical use is false.
	 * @return List of fileNames
	 */
	public List<PrettyPrintAttachments> putDagsobjorAttachmentsToPath(boolean forceAll) {
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		if (forceAll) {
			authorization.getFirmaltDaoList().forEach(firmalt -> {	
				List<MessagesHalRepresentation> dagsobjors = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, firmalt);

				dagsobjors.forEach((message) -> {
					logRecords.addAll(getAttachments(message, firmalt));
				});		
			
			});
		} else {
			authorization.getFirmaltDaoList().forEach(firmalt -> {			
				List<MessagesHalRepresentation> dagsobjors = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, LocalDateTime.now().minusDays(1),firmalt);

				dagsobjors.forEach((message) -> {
					logRecords.addAll(getAttachments(message, firmalt));
				});		
			
			});
		}

		logger.info("Dagsoppgjors attachments are downloaded.");
		logger.info(FlipTableConverters.fromIterable(logRecords, PrettyPrintAttachments.class));
		
		return logRecords;

	}

	@Scheduled(cron="${altinn.file.download.cron.pattern}")
	private List<PrettyPrintAttachments> putDagsobjorAttachmentsToPath() {
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		authorization.getFirmaltDaoList().forEach(firmalt -> {			
			List<MessagesHalRepresentation> dagsobjors = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor, LocalDateTime.now().minusDays(1),firmalt);
		
			dagsobjors.forEach((message) -> {
				logRecords.addAll(getAttachments(message, firmalt));
			});		

		});
		
		logger.info("Scheduled download of Dagsoppgjors attachments is executed.");
		logger.info(FlipTableConverters.fromIterable(logRecords, PrettyPrintAttachments.class));
		
		return logRecords;

	}	
	
	/*
	 * Get all attachments in message, e.i. PDF and XML
	 */
	private List<PrettyPrintAttachments> getAttachments(MessagesHalRepresentation message, FirmaltDao firmalt) {
		List<PrettyPrintAttachments> logRecords = new ArrayList<PrettyPrintAttachments>();
		String self = message.getLinks().getLinksBy("self").get(0).getHref();
		logger.info("self="+self);
		
		URI uri = URI.create(self);
		//Get specific message
		MessagesHalRepresentation halMessage = getMessage(uri, firmalt);
		
		List<Link> attachmentsLink =halMessage.getLinks().getLinksBy("attachment");
		attachmentsLink.forEach((attLink) -> {
			logger.info("attLink="+attLink);
			URI attUri = URI.create(attLink.getHref());
			//Prefix Altinn-name with created_date
			StringBuilder writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName());
			getAttachment(attUri, writeFile.toString(), firmalt);
			PrettyPrintAttachments log = new PrettyPrintAttachments(firmalt.getAiorg(), LocalDateTime.now().toString(),halMessage.getCreatedDate().toString(), writeFile.toString(), ServiceOwner.Skatteetaten.name() );
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

			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entityHeadersOnly, String.class); 

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getMessage for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			}
			logger.info("responseEntity.getBody"+responseEntity.getBody());
	
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

			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entityHeadersOnly, String.class); 

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getMessage for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			}
			logger.info("responseEntity.getBody"+responseEntity.getBody());
	
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
			logger.info("getAttachment, uri=" + uri);

			responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, entityHeadersOnly, byte[].class, "1");

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getAttachment for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			} else {

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
	

//	private List<MetadataHalRepresentation> getMetadata(URI uri){
//		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity();
//		ResponseEntity<String> responseEntity = null;
//		
//		try {
//
//			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entityHeadersOnly, String.class); 
//
//			if (responseEntity.getStatusCode() != HttpStatus.OK) {
//				logger.error("Error in getMessage for " + uri);
//				throw new RuntimeException(responseEntity.getStatusCode().toString());
//			}
//			logger.info("responseEntity.getBody"+responseEntity.getBody());
//	
//	        return HalHelper.getMetadata(responseEntity.getBody());
//
//		} catch (Exception e) {
//			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
//			logger.warn(errMessage, e);
//			throw new RuntimeException(errMessage);
//		}
//		
//	}
	
}
