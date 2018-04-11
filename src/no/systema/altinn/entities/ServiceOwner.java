package no.systema.altinn.entities;

import no.systema.altinn.integration.ActionsServiceManager;

/**
 * Enum for possible serviceowner in Altinn <br><br>
 * 
 * 
 * @author Fredrik Möller
 * @date 2018-01
 *
 */
public enum ServiceOwner {
	/**
	 * SKD
	 */
	Skatteetaten("SKD"),
	/**
	 * ASF
	 */
	Samlesider("ASF");
	
	private final String code;

	ServiceOwner(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
}
