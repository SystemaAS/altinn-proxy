package no.systema.altinn.integration;

/**
 * Enum for possible serviceowner in Altinn <br><br>
 * 
 * For full list @see {@linkplain ActionsServiceManager} getMetadata
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
