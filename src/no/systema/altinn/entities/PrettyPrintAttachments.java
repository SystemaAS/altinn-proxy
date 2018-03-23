package no.systema.altinn.entities;

/**
 * This class is convenience object for pretty printing info on downloads. <br>
 * 
 * 
 * @author fredrikmoller
 * @date 2018-02
 *
 */
public class PrettyPrintAttachments {
	private String orgnr;
	private String nedlastingsDato;
	private String skapetDato;
	private String filNavn;
	private String serviceEier;
	private String status;

	public PrettyPrintAttachments(String orgnr, String nedlastingsDato, String skapetDato, String filNavn, String serviceEier, String status) {
		this.orgnr = orgnr;
		this.nedlastingsDato = nedlastingsDato;
		this.skapetDato = skapetDato;
		this.filNavn = filNavn;
		this.serviceEier = serviceEier;
		this.status = status;
	}

	public String getOrgnr() {
		return orgnr;
	}
	public void setOrgnr(String orgnr) {
		this.orgnr = orgnr;
	}
	public String getNedlastingsDato() {
		return nedlastingsDato;
	}
	public void setNedlastingsDato(String nedlastingsDato) {
		this.nedlastingsDato = nedlastingsDato;
	}
	public String getSkapetDato() {
		return skapetDato;
	}
	public void setSkapetDato(String skapetDato) {
		this.skapetDato = skapetDato;
	}
	public String getFilNavn() {
		return filNavn;
	}
	public void setFilNavn(String filNavn) {
		this.filNavn = filNavn;
	}
	public String getServiceEier() {
		return serviceEier;
	}
	public void setServiceEier(String serviceEier) {
		this.serviceEier = serviceEier;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}

	
}
