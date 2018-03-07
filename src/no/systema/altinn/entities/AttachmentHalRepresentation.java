package no.systema.altinn.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.otto.edison.hal.HalRepresentation;

//TODO funkar inte att encoda till, formodlinge ta bort.
public class AttachmentHalRepresentation extends HalRepresentation {

    @JsonProperty("FileName")
	private String fileName;
    @JsonProperty("Data")
    private byte[] data;
    @JsonProperty("AttachmentLink")
    private String attachmentLink;  //AttachmentLink	Uri	Download link for the Attachment.
    @JsonProperty("AttachmentType")
    private String attachmentType; // This is currently unused, but will be used in a future release.

//	Encrypted	Boolean	Flag indicating if this is an encrypted Attachment.
//	SigningLocked	Boolean	Value indicating whether the user should be allowed to change signing selection of the attachment. If both SigningLocked and SingedByDefault are true, then signing is required.
//	SignedByDefault	Boolean	Value indicating whether the service owner wants the attachment to be signed. If both SigningLocked and SingedByDefault are true, then signing is required.	

    public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getAttachmentLink() {
		return attachmentLink;
	}
	public void setAttachmentLink(String attachmentLink) {
		this.attachmentLink = attachmentLink;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public String getAttachmentType() {
		return attachmentType;
	}
	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

    
}
