package no.systema.altinn.integration;

import static de.otto.edison.hal.EmbeddedTypeInfo.withEmbedded;
import static de.otto.edison.hal.HalParser.parse;

import java.io.IOException;
import java.util.List;

import de.otto.edison.hal.HalRepresentation;
import no.systema.altinn.entities.AttachmentHalRepresentation;
import no.systema.altinn.entities.MessagesHalRepresentation;

/**
 * Helper class for managing Hal stuff.
 * 
 * @author Fredrik Möller
 * @date 2018-01
 *
 */
public class HalHelper {

	/**
	 * Return Hal representations of Messages
	 * 
	 * @param body
	 * @return List<MessagesHalRepresentation>  messages
	 * @throws IOException
	 */
	public static List<MessagesHalRepresentation> getMessages(String body) throws IOException {
        final HalRepresentation result = parse(body)
                .as(HalRepresentation.class, withEmbedded("messages", MessagesHalRepresentation.class));
        final List<MessagesHalRepresentation> embeddedMessages = result.getEmbedded().getItemsBy("messages", MessagesHalRepresentation.class);
        
        return embeddedMessages;

	}
	
	/**
	 * Return Hal representations of Message
	 * 
	 * @param body
	 * @return MessagesHalRepresentation  messages
	 * @throws IOException
	 */
	public static MessagesHalRepresentation getMessage(String body) throws IOException {
        final MessagesHalRepresentation result = parse(body)
                .as(MessagesHalRepresentation.class);
        
        return result;

	}	

	/**
	 * Return Hal representations of Attachment
	 * 
	 * @param body
	 * @return AttachmentHalRepresentation  messages
	 * @throws IOException
	 */
	public static AttachmentHalRepresentation getAttachment(String body) throws IOException {
        final AttachmentHalRepresentation result = parse(body)
                .as(AttachmentHalRepresentation.class);
        
        return result;

	}		
	
	
	
}
