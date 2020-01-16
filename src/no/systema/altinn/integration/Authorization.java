package no.systema.altinn.integration;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import no.systema.altinn.entities.ApiKey;
import no.systema.jservices.common.dao.FirmaltDao;

@Service("authorization")
public class Authorization {
	private static Logger logger = Logger.getLogger(Authorization.class.getName());
	
	@Autowired
	private CertificateManager certificateManager;

    @Value("${altinn.access.use.proxy}")
    boolean useProxy;	
	
    @Value("${altinn.access.proxy.host}")
    String proxyHost;

    @Value("${altinn.access.proxy.port}")
    String port;		
	
	/**
	 * Configures ClientHttpRequestFactory to provide client certificate for two way https connection.<br>
	 * Support TLS-versions: TLSv1, TLSv1.1
	 * 
	 * @param firmaltDao
	 * @return the ClientHttpRequestFactory with configured SSLContext
	 */
	private ClientHttpRequestFactory getRequestFactory(FirmaltDao firmaltDao) { 
		//String[] TLS_PROTOCOLS = {"TLSv1", "TLSv1.1" /*, "TLSv1.2"*/}; // Comment in TLSv1.2 to fail : bug in altinn or java that fails TLS handshake most of the time, but not always
		String[] TLS_PROTOCOLS = {"TLSv1.2"}; // Comment in TLSv1.2 to fail : bug in altinn or java that fails TLS handshake most of the time, but not always
		String[] CIPHER_SUITES = null; // {"TLS_RSA_WITH_AES_128_GCM_SHA256"};

		char[] password = firmaltDao.getAipwd().toCharArray();

		HttpComponentsClientHttpRequestFactory requestFactory;
		HttpClient httpClient;
		
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(certificateManager.loadCertificate(), password);

			/*
			 * Determines whether the certificate chain can be trusted without consulting the trust manager
			 * configured in the actual SSL context. This method can be used to override the standard JSSE
			 * certificate verification process.
			 * <p>
			 * Please note that, if this method returns {@code false}, the trust manager configured
			 * in the actual SSL context can still clear the certificate as trusted.
			 *
			 * @param chain the peer certificate chain
			 * @param authType the authentication type based on the client certificate
			 * @return {@code true} if the certificate can be trusted without verification by
			 *   the trust manager, {@code false} otherwise.
			 * @throws CertificateException thrown if the certificate is not trusted or invalid.
			 */	
			TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;

			SSLContext sslContext = SSLContexts.custom()
					.loadKeyMaterial(keyStore, password)
					.loadTrustMaterial(null, acceptingTrustStrategy)
					.build();


			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, TLS_PROTOCOLS, CIPHER_SUITES,
					new DefaultHostnameVerifier());

			requestFactory = new HttpComponentsClientHttpRequestFactory();

			logger.info("useProxy="+useProxy);
			logger.debug("proxyHost="+proxyHost+", port="+port);
			
			if (Boolean.valueOf(useProxy)) {
		        int portNr = -1;
		        try {
		            portNr = Integer.parseInt(port);
		        } catch (NumberFormatException e) {
		            logger.error("Unable to parse the proxy port number");
		            throw new RuntimeException("Unable to parse the proxy port number", e);
		        }

				httpClient = HttpClients.custom()
						.setSSLSocketFactory(sslSocketFactory)
						.setProxy(new HttpHost(proxyHost, portNr, "http"))
						.build();	
				
				logger.debug("Proxy set to: "+ proxyHost + ":"+portNr);
			} else {
				httpClient = HttpClients.custom()
					.setSSLSocketFactory(sslSocketFactory)
					.build();
				
	    		logger.debug("No proxy set. ");		
			}			
			
			requestFactory.setHttpClient(httpClient);
			
		} catch (KeyManagementException | UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.error("ERROR: loading certificate!",e);
			throw new RuntimeException(e);
		}

		return requestFactory;

	}

	/**
	 * Get a prepared HTTPHeader.
	 * 
	 * @param firmaltDao
	 * @return HttpEntity<ApiKey>
	 */
    public HttpEntity<ApiKey> getHttpEntity(FirmaltDao firmaltDao)  {
        RestTemplate restTemplate = new RestTemplate(getRequestFactory(firmaltDao));
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        
		ApiKey apiKeyDto = new ApiKey();		
		apiKeyDto.setUserName(firmaltDao.getAiuser());
		apiKeyDto.setUserPassword(firmaltDao.getAiupwd());

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/hal+json");
		headers.add(HttpHeaders.ACCEPT, "application/hal+json");
		headers.add(HttpHeaders.HOST, firmaltDao.getAihost());
		headers.add("ApiKey", firmaltDao.getAiapi());

		HttpEntity<ApiKey> entity = new HttpEntity<ApiKey>(apiKeyDto, headers);
		URI authUri = ActionsUriBuilder.authentication(firmaltDao.getAihost(), firmaltDao.getAiauur());
		
		ResponseEntity<byte[]> response = restTemplate.exchange(authUri, HttpMethod.POST, entity, byte[].class);			
		logger.debug("response="+response);
		
		List<String> setCookieList = response.getHeaders().get(HttpHeaders.SET_COOKIE);
		String cookie = null;
		try {
			cookie = setCookieList.get(0); 
		} catch (Exception e) {
			logger.error("Could not get Cookie from "+firmaltDao.getAihost(), e);
			throw new RuntimeException("Could not get Cookie from "+firmaltDao.getAihost(), e);
		}
		headers.add(HttpHeaders.COOKIE, cookie);
		HttpEntity<ApiKey> entityHeadersOnly = new HttpEntity<ApiKey>( headers);		
		
		return entityHeadersOnly;

    }   
 
	/**
	 * Get a prepared HTTPHeader for filedownload
	 * 
	 * @param firmaltDao
	 * @return HttpEntity<ApiKey>
	 */
    public HttpEntity<ApiKey> getHttpEntityFileDownload(FirmaltDao firmaltDao)  {
        RestTemplate restTemplate = new RestTemplate(getRequestFactory(firmaltDao));
		restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        
		ApiKey apiKeyDto = new ApiKey();		
		apiKeyDto.setUserName(firmaltDao.getAiuser());
		apiKeyDto.setUserPassword(firmaltDao.getAiupwd());

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/hal+json");
		headers.add(HttpHeaders.ACCEPT, "application/octet-stream");  
		headers.add(HttpHeaders.HOST, firmaltDao.getAihost());
		headers.add("ApiKey", firmaltDao.getAiapi());

		HttpEntity<ApiKey> entity = new HttpEntity<ApiKey>(apiKeyDto, headers);
		URI authUri = ActionsUriBuilder.authentication(firmaltDao.getAihost(), firmaltDao.getAiauur());

		ResponseEntity<byte[]> response = restTemplate.exchange(authUri, HttpMethod.POST, entity, byte[].class);			
		logger.debug("response="+response);
		
		List<String> setCookieList = response.getHeaders().get(HttpHeaders.SET_COOKIE);
		String cookie = null;
		try {
			cookie = setCookieList.get(0); 
		} catch (Exception e) {
			logger.error("Could not get Cookie from "+firmaltDao.getAihost(), e);
			throw new RuntimeException("Could not get Cookie from "+firmaltDao.getAihost(), e);
		}
		headers.add(HttpHeaders.COOKIE, cookie);
		HttpEntity<ApiKey> entityHeadersOnly = new HttpEntity<ApiKey>( headers);		
		
		return entityHeadersOnly;

    }

}
