package no.systema.altinn.entities;

import no.systema.altinn.integration.ActionsServiceManager;

/**
 * Enum for possible servicecode in Altinn <br><br>
 * To use in combination with {@link ServiceEdition}
 * 
 * 
 * @author Fredrik Möller
 * @date 2018-01
 *
 */
public enum ServiceCode {
	/**
	 * 5012
	 */
	Dagsobjor("5012"), 
	/* 2018_03-02
	* Det har også blitt oppdaget en feil i oppsettet for enkelttjeneste for den nye ordningen for dagsoppgjør. Denne feilen berører kun de som ønsker å tildele enkeltpersoner enkelttjenester i Altinn. 
	* For å løse dette søk opp 4125/150602 "Brev til etterskuddspliktige" og velg denne. I tillegg er det laget en ny enkelttjeneste som er riktig 5012/171208 "Elektronisk kontoutskrift tollkreditt og dagsoppgjør" som vil være gyldig i løpet av 3-4 uker. Tildel denne samtidig og den vil automatisk bli tatt i bruk når den nye tjenesten er klar.
	* Har en rolle som "Regnskapsmedarbeider" vil en uansett ha tilgang til å laste ned PDF- og e2b-fil fra Altinn og vil ikke bli berørt av endringen.
	*/
	/**
	 * 4125
	 */	
	DagsobjorFIX("4125") ;
	
	private final String code;

	ServiceCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
}
