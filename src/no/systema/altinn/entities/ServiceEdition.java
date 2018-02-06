package no.systema.altinn.entities;

import no.systema.altinn.integration.ActionsServiceManager;

/**
 * Enum for possible serviceedition in Altinn <br><br>
 * To use in combination with {@linkplain ServiceCode}
 * 
 * For full list @see {@linkplain ActionsServiceManager} getMetadata
 * 
 * @author Fredrik Möller
 * @date 2018-01
 *
 */
public enum ServiceEdition {
	/**
	 * 171208
	 */
	Dagsobjor(171208);
	
	private final int code;

	ServiceEdition(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
	
}
