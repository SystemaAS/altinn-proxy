package no.systema.altinn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jakewharton.fliptables.FlipTableConverters;

import no.systema.altinn.entities.PrettyPrintAttachments;
import no.systema.altinn.entities.PrettyPrintMessages;
import no.systema.altinn.integration.ActionsServiceManager;
import no.systema.jservices.common.dao.FirmaltDao;
import no.systema.jservices.common.dao.services.BridfDaoService;
import no.systema.jservices.common.util.StringUtils;
/**
 * This controller is mainly for troubleshooting. <br>
 * 
 * It adresses the "real" implementation of download dagsoppgjor, here doing in manually.
 * 
 * 
 * @author fredrikmoller
 * @date 2018-02
 *
 */
@Controller
public class DownloadController {
	private static Logger logger = Logger.getLogger(DownloadController.class.getName());

	/**
	 * 
	 * Files are downloaded into path define in {@linkplain FirmaltDao}.aipath
	 * 
	 * Note: No state-handling of messages in Altinn!
	 * 
	 * @Example: http://gw.systema.no:8080/altinn-proxy/downloadDagsobjor.do?user=FREDRIK&forceAll=false&fraDato=20180101
	 * forceAll=true , removes date-filter on GET attachment.
	 * fraDato=set, filter on CreatedDate in www.altin..no, overrides forceAll=true
	 * 
	 * @param session
	 * @param request, user 
	 * @return status
	 */	
	@RequestMapping(value="downloadDagsobjor.do", method={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String download(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		List<PrettyPrintAttachments> dagsoppgors = null;

		logger.info("downloadDagsobjor.do...");
		try {
			String user = request.getParameter("user");
			Assert.notNull(user, "user must be delivered."); 

			String userName = bridfDaoService.getUserName(user);
			Assert.notNull(userName, "userName not found in Bridf."); 
			
			//Ignoring date-filter
			String forceAll = request.getParameter("forceAll");
			//Filter on date
			String fraDato = request.getParameter("fraDato");
			if (StringUtils.hasValue(fraDato)) {
				dagsoppgors = serviceManager.putDagsobjorAttachmentsToPath(Boolean.valueOf(forceAll),getFromCreatedDate(fraDato));
				sb.append("Dagsoppgjors-filer i meldinger fra Skattetaen er nedlasted. Med filter på fraDato (CreatedDate i altinn). \n \n");
			} else {
				dagsoppgors = serviceManager.putDagsobjorAttachmentsToPath(Boolean.valueOf(forceAll), null);
				if (StringUtils.hasValue(forceAll) && Boolean.valueOf(forceAll).booleanValue()) {
					sb.append("Dagsoppgjors-filer i meldinger fra Skattetaen er nedlasted. Uten filter! \n \n");
				} else {
					sb.append("Dagsoppgjors-filer i meldinger fra Skattetaen er nedlasted. Fra idag. \n \n");
				}

			}
			
			sb.append("Path till filer finnes i fil:FIRMALT og felt: AIPATH \n \n");
			
			sb.append(FlipTableConverters.fromIterable(dagsoppgors, PrettyPrintAttachments.class));
			
		} catch (Exception e) {
			// write std.output error output
			e.printStackTrace();
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			return "ERROR [JsonResponseOutputterController]" + writer.toString();
		}

		session.invalidate();
		return sb.toString();

	}

	/**
	 * 
	 * Read all meldinger i virksomhet(er)s innboks(er)
	 * 
	 * 
	 * @Example: http://gw.systema.no:8080/altinn-proxy/readInnboks.do?user=FREDRIK&forceDetails=false
	 * forceDetails=true is adding more data on using 'self'-link, not recommeded to use.
	 * 
	 * @param session
	 * @param request, user 
	 * @return status
	 */	
	@RequestMapping(value="readInnboks.do", method={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String readInnboks(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		logger.info("readInnboks.do...");
		try {
			String user = request.getParameter("user");
			Assert.notNull(user, "user must be delivered."); 

			String userName = bridfDaoService.getUserName(user);
			Assert.notNull(userName, "userName not found in Bridf."); 
			
			String forceDetails = request.getParameter("forceDetails");
			
			List<PrettyPrintMessages> messages = serviceManager.getMessages(Boolean.valueOf(forceDetails));
			
			logger.info("serviceManager.getMessages()");
			logger.info(FlipTableConverters.fromIterable(messages, PrettyPrintMessages.class));

			sb.append("Alle meldinger i innboksen. \n \n");
			
			sb.append("Meldinger:\n");
			sb.append(FlipTableConverters.fromIterable(messages, PrettyPrintMessages.class));
			
		} catch (Exception e) {
			// write std.output error output
			e.printStackTrace();
			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			return "ERROR [JsonResponseOutputterController]" + writer.toString();
		}

		session.invalidate();
		return sb.toString();

	}

	private LocalDateTime getFromCreatedDate(String fraDato) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd"); //as defined in Firmalt
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");  //as defined in Firmalt.

		String fraTime = "000000";
		LocalDate fromDate = LocalDate.parse(fraDato, dateFormatter);
		LocalTime fromTime = LocalTime.parse(fraTime, timeFormatter);
		
		return  LocalDateTime.of(fromDate, fromTime);
	}	
	
	@Autowired
	private BridfDaoService bridfDaoService;
	
	@Autowired
	private ActionsServiceManager serviceManager;
	
}
