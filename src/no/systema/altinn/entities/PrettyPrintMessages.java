package no.systema.altinn.entities;

/**
 * This class is convenience object for pretty printing info on messages. <br>
 * 
 * 
 * @author fredrikmoller
 * @date 2018-02
 *
 */
public class PrettyPrintMessages {
	private String orgnr;
	private String nedlastingsDato;
	private String skapetDato;
	private String subject;
	private String serviceEier;
	private String serviceKode;
	private int serviceUtgave;

	public PrettyPrintMessages(String orgnr, String nedlastingsDato, String skapetDato, String subject, String serviceEier, String serviceKode, int serviceUtgave) {
		this.orgnr = orgnr;
		this.nedlastingsDato = nedlastingsDato;
		this.skapetDato = skapetDato;
		this.subject = subject;
		this.serviceEier = serviceEier;
		this.serviceKode = serviceKode;
		this.serviceUtgave = serviceUtgave;
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

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getServiceEier() {
		return serviceEier;
	}

	public void setServiceEier(String serviceEier) {
		this.serviceEier = serviceEier;
	}

	public String getServiceKode() {
		return serviceKode;
	}

	public void setServiceKode(String serviceKode) {
		this.serviceKode = serviceKode;
	}

	public int getServiceUtgave() {
		return serviceUtgave;
	}

	public void setServiceUtgave(int serviceUtgave) {
		this.serviceUtgave = serviceUtgave;
	}

}
