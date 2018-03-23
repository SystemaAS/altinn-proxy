package no.systema.altinn.entities;

/**
 * Enum for some of possible status on Message in Altinn <br><br>
 * 
 * 
 * @author Fredrik Möller
 * @date 2018-03
 *
 */
public enum Status {
	/**
	 * Arkivert
	 */
	Arkivert("Arkivert"),
	/**
	 * Ulest
	 */
	Ulest("Ulest");
	
	private final String code;

	Status(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
}
