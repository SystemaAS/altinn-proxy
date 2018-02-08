package no.systema.altinn.integration;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
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

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import no.systema.altinn.entities.ApiKey;
import no.systema.altinn.entities.MessagesHalRepresentation;
import no.systema.altinn.entities.MetadataHalRepresentation;
import no.systema.altinn.entities.ServiceCode;
import no.systema.altinn.entities.ServiceEdition;
import no.systema.altinn.entities.ServiceOwner;
import no.systema.jservices.common.dao.FirmaltDao;

/**
 * The responsible service manager for accessing resources inside www.altinn.no
 * 
 * Implementing part of actions found here: https://www.altinn.no/api/Help
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
	 * @param orgnr
	 * @return List<MessagesHalRepresentation>
	 */
	public List<MessagesHalRepresentation> getMessages() {
		URI uri = ActionsUriBuilder.messages(authorization.getHost(), authorization.getOrgnr());
		return getMessages(uri);
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
		URI uri = ActionsUriBuilder.messages(authorization.getHost(), authorization.getOrgnr(),serviceOwner);
		return getMessages(uri);
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
	public List<MessagesHalRepresentation> getMessages(ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition) {
		URI uri = ActionsUriBuilder.messages(authorization.getHost(), authorization.getOrgnr(),serviceOwner, serviceCode, serviceEdition);
		return getMessages(uri);
	}	
	
	/**
	 * Retrieves all attachment in Melding: Dagsoppgjor <br>
	 * and stores as defined in {@linkplain FirmaltDao}.aipath
	 * 
	 * @return List of fileNames
	 */
	@Scheduled(cron="${altinn.file.download.cron.pattern}")
	public List<String> putDagsobjorAttachmentsToPath() {
		List<String> fileNames = new ArrayList<String>();
		List<MessagesHalRepresentation> dagsobjors = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor);
	
		dagsobjors.forEach((message) -> {
			logger.info("message" + message);
			fileNames.addAll(getAttachments(message));
		});

		logger.info("Dagsoppgjors attachments are downloaded.");
		
		return fileNames;

	}
	
	/*
	 * Get all attachments in message, e.i. PDF and XML
	 */
	private List<String> getAttachments(MessagesHalRepresentation message) {
		List<String> fileNames = new ArrayList<String>();
		String self = message.getLinks().getLinksBy("self").get(0).getHref();
		logger.info("self="+self);
		
		URI uri = URI.create(self);
		//Get specific message
		MessagesHalRepresentation halMessage = getMessage(uri);
		
		List<Link> attachmentsLink =halMessage.getLinks().getLinksBy("attachment");
		attachmentsLink.forEach((attLink) -> {
			logger.info("attLink="+attLink);
			URI attUri = URI.create(attLink.getHref());
			//Prefix Altinn-name with created_date
			StringBuilder writeFile = new StringBuilder(halMessage.getCreatedDate()).append("-").append(attLink.getName());
			getAttachment(attUri, writeFile.toString());
			fileNames.add(writeFile.toString());
			
		});
		
		return fileNames;

	}	

	private List<MessagesHalRepresentation> getMessages(URI uri){
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity();
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
	
	private MessagesHalRepresentation getMessage(URI uri){
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity();
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

	private void getAttachment(URI uri, String writeFile) {
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntityFileDownload();
		ResponseEntity<byte[]> responseEntity = null;

		try {
			logger.info("getAttachment, uri=" + uri);

			responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, entityHeadersOnly, byte[].class, "1");

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getAttachment for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			} else {

				writeToFile(writeFile, responseEntity);

			}

		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}

	}

	private void writeToFile(String writeFile, ResponseEntity<byte[]> responseEntity) throws FileNotFoundException, IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(responseEntity.getBody());
		FileOutputStream fos = new FileOutputStream(authorization.getFilePath() + writeFile);
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = bis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}

		fos.close();
		bis.close();

		logger.info("File: " + authorization.getFilePath() + writeFile + " saved on disk.");

	}
	

	private List<MetadataHalRepresentation> getMetadata(URI uri){
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity();
		ResponseEntity<String> responseEntity = null;
		
		try {

			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entityHeadersOnly, String.class); 

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getMessage for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			}
			logger.info("responseEntity.getBody"+responseEntity.getBody());
	
	        return HalHelper.getMetadata(responseEntity.getBody());

		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}
		
	}
	
}
