package no.systema.altinn.integration;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import no.systema.altinn.entities.ServiceCode;
import no.systema.altinn.entities.ServiceEdition;
import no.systema.altinn.entities.ServiceOwner;

/**
 * Creates URI on https://www.altinn.no/api/Help
 * 
 * @author Fredrik Möller
 * @date 2018
 */
public class ActionsUriBuilder {

	/**
	 * Gets the user profile of the currently authenticated user.
	 * 
	 * @param host
	 * @param orgnr
	 * @return URI, ex. GET {orgno}/profile
	 */
	public static URI profile(String host, int orgnr) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/{who}/profile")
				.build()
				.expand(orgnr)
				.encode();

		return uriComponents.toUri();

	}
	
	/**
	 * Gets the list of available API-services in Altinn.
	 * 
	 * @param host
	 * @return URI, ex. GET metadata
	 */
	public static URI metadata(String host) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/metadata")
				.build()
				.encode();

		return uriComponents.toUri();

	}	
	
	/**
	 * Gets all messages for the given 'who', here orgnr. These can optionally be retrieved in the language specified.
	 * 
	 * @param host
	 * @param orgnr
	 * @return URI, ex. GET {who}/Messages?language={language}
	 */
	public static URI messages(String host, String orgnr) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/{who}/messages")
				.build()
				.expand(orgnr)
				.encode();

		return uriComponents.toUri();

	}

	/**
	 * Gets all messages for the given 'who', here orgnr. These can optionally be retrieved in the language specified.
	 * 
	 * Filtered on Serviceowner, e.g. SKD (=Skatteetaten)
	 * 
	 * @param host
	 * @param orgnr
	 * @param serviceOwneer
	 * @return URI, ex. GET {who}/Messages?language={language}
	 */
	public static URI messages(String host, String orgnr, ServiceOwner serviceOwner) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/{who}/messages")
			    .query("$filter={value}")
			    .buildAndExpand(orgnr, "ServiceOwner eq \'"+serviceOwner+"\'")
				.encode();

		return uriComponents.toUri();

	}
	
	/**
	 * Gets all messages for the given 'who', here orgnr. These can optionally be retrieved in the language specified.
	 * 
	 * Filtered on Serviceowner, e.g. SKD (=Skatteetaten), ServiceCode and ServiceEdition.
	 * 
	 * @param host
	 * @param orgnr
	 * @param serviceOwneer
	 * @return URI, ex. GET {who}/Messages?language={language}
	 */
	public static URI messages(String host, String orgnr, ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition ) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/{who}/messages")
			    .query("$filter={expand1}")
			    .buildAndExpand(orgnr, "ServiceOwner eq \'"+serviceOwner+"\' and ServiceCode eq \'"+serviceCode.getCode()+"\' and ServiceEdition eq "+serviceEdition.getCode())
			    .encode();

		return uriComponents.toUri();

	}

	/**
	 * Gets all messages for the given 'who', here orgnr. These can optionally be retrieved in the language specified.
	 * 
	 * Filtered on Serviceowner, e.g. SKD (=Skatteetaten), ServiceCode, ServiceEdition and CreatedDate (greater than).
	 * 
	 * @param host
	 * @param orgnr
	 * @param serviceOwneer
	 * @param serviceCode
	 * @param serviceEdition
	 * @param createdDate
	 * @return URI, ex. GET {who}/Messages?language={language}
	 */
	public static URI messages(String host, String orgnr, ServiceOwner serviceOwner, ServiceCode serviceCode, ServiceEdition serviceEdition, LocalDateTime createdDate) {
		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/{who}/messages")
			    .query("$filter={expand1}")
			    .buildAndExpand(orgnr, "ServiceOwner eq \'"+serviceOwner+"\' and ServiceCode eq \'"+serviceCode.getCode()+"\' and ServiceEdition eq "+serviceEdition.getCode() +  " and CreatedDate gt datetime\'"+createdDate+"\'")
			    .encode();

		return uriComponents.toUri();

	}	
	
	/**
	 * Get the authentication url.
	 * 
	 * @param host
	 * @param path
	 * @return URI, ex. POST https://tt02.altinn.no/api/authentication/authenticatewithpassword?ForceEIAuthentication
	 */
	public static URI authentication(String host, String path) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path(path)
				.query("ForceEIAuthentication")
				.build()
				.encode();

		return uriComponents.toUri();

	}	
	
	
	/**
	 * Contains all actions related to the authorization roles
	 * 
	 * @param host
	 * @param orgnr
	 * @return URI, ex. GET {who}/authorization/roles?language={language}
	 */
	public static URI roles(String host, int orgnr) {

		UriComponents uriComponents = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(host)
				.path("/api/{who}/authorization/roles")
				.build()
				.expand(orgnr)
				.encode();

		return uriComponents.toUri();

	}	
	
}
