package no.systema.altinn.integration;

import static de.otto.edison.hal.HalParser.parse;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	

	//TODO
	public String getXMLRepresentation(MessagesHalRepresentation message) {
		String self = message.getLinks().getLinksBy("self").get(0).getHref();
		
		
		URI uri = URI.create(self);
		HalRepresentation hal = getMessage(uri);
		
		return hal.toString();
	}
	
	
	public void putDagsobjorXMLRepresentationToPath() {
		
		 List<MessagesHalRepresentation> dagsobjors = getMessages(ServiceOwner.Skatteetaten, ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor);
//		 dagsobjors.forEach((message) ->  logger.info("dagsobjor for orgnr"+orgnr+", XML="+getXMLRepresentation(message)));
		 
		//TODO
		
	}
	
	
	/**
	 * Retrieves the PDF representation of dagsoppgjor <br>
	 * and stores as defined in {@linkplain FirmaltDao}.
	 * 
	 * Also Scheduled @Value("#{someBean.someProperty != null ? someBean.someProperty : 'default'}")
	 * 
	 * @return List of fileNames
	 */
	@Scheduled(cron="${altinn.file.download.cron.pattern}")
	public List<String> putDagsobjorPDFRepresentationToPath() {
		List<String> fileNames = new ArrayList<String>();
		List<MessagesHalRepresentation> dagsobjors = getMessages(ServiceOwner.Skatteetaten,ServiceCode.Dagsobjor, ServiceEdition.Dagsobjor);
	
		dagsobjors.forEach((message) -> {
			logger.info("message" + message);
			fileNames.add(getAttachment(message));
		});

		logger.info("Dagsoppgjors PDF's are downloaded.");
		
		return fileNames;

	}
	
	/**
	 * Gets the list of available API-services in Altinn.
	 * 
	 * @see {@link ActionsUriBuilder}
	 * @return List<MetadataHalRepresentation>
	 */
	public List<MetadataHalRepresentation> getMetadata() {
		URI uri = ActionsUriBuilder.metadata(authorization.getHost());
		return getMetadata(uri);
	}

	
	private String getAttachment(MessagesHalRepresentation message) {
		String self = message.getLinks().getLinksBy("self").get(0).getHref();
		logger.info("self="+self);
		
		URI uri = URI.create(self);
		//Get specific message
		MessagesHalRepresentation halMessage = getMessage(uri);
		
		Optional<Link> attachmentLink =halMessage.getLinks().getLinkBy("attachment");
		uri = URI.create(attachmentLink.get().getHref());
		
		//Prefix Altinn-name with created_date
		StringBuilder writeFile = new StringBuilder(halMessage.getCreatedDate()).append("-").append(attachmentLink.get().getName());
		getAttachment(uri, writeFile.toString());
		
		return writeFile.toString();

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
		String filePath = authorization.getFilePath();

		try {
			logger.info("getAttachment, uri=" + uri);

			responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.GET, entityHeadersOnly, byte[].class,"1");

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getAttachment for " + uri);
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			} else {

				ByteArrayInputStream bis = new ByteArrayInputStream(responseEntity.getBody());
				FileOutputStream fos = new FileOutputStream(filePath + writeFile);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = bis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				bis.close();

			}

			logger.info("File: " + filePath + writeFile + " saved on disk.");

		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}

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
	
	/*
	 * Metadata for specific message, e.i. MessagesHalRepresentation
	 */
	private MetadataHalRepresentation getMetadata(MessagesHalRepresentation message){
		HttpEntity<ApiKey> entityHeadersOnly = authorization.getHttpEntity();
		ResponseEntity<String> responseEntity = null;
		
		try {

			Optional<Link> link = message.getLinks().getLinkBy("metadata");
			responseEntity = restTemplate.exchange(link.get().getHref(), HttpMethod.GET, entityHeadersOnly, String.class); 

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				logger.error("Error in getMessage for " + link.get().getHref());
				throw new RuntimeException(responseEntity.getStatusCode().toString());
			}
			logger.info("responseEntity.getBody"+responseEntity.getBody());
	
	        final MetadataHalRepresentation result = parse(responseEntity.getBody())
	                .as(MetadataHalRepresentation.class);
	        
			return result;

		} catch (Exception e) {
			String errMessage = String.format(" request failed: %s", e.getLocalizedMessage());
			logger.warn(errMessage, e);
			throw new RuntimeException(errMessage);
		}
		
	}
	
}
