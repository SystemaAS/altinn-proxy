package no.systema.altinn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDate;
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
import no.systema.jservices.common.util.Log4jUtils;
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
	 * @Example: http://gw.systema.no:8080/altinn-proxy/downloadDagsobjor.do?user=FREDRIK&forceAll=false&gtDato=20180101
	 * forceAll=true , removes date-filter on GET attachment.
	 * gtDato=set, filter on CreatedDate in www.altin..no, overrides forceAll=true
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
			//Greater than date
			String gtDato = request.getParameter("gtDato");
			if (StringUtils.hasValue(gtDato)) {
				dagsoppgors = serviceManager.putDagsobjorAttachmentsToPath(Boolean.valueOf(forceAll),getFromCreatedDate(gtDato));
				sb.append("Dagsoppgjors-filer i meldinger fra Skattetaen er nedlasted. Med filter på gtDato (CreatedDate i altinn). \n \n");
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
	 * @Example: http://gw.systema.no:8080/altinn-proxy/readInnboks.do?user=FREDRIK&forceDetails=false&ignoreStatus=false
	 * forceDetails=true is adding more data on using 'self'-link, not recommeded to use.
	 * ignoreStatus=true not including Status Ulest and Lest
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
			
			String ignoreStatus = request.getParameter("ignoreStatus");
			
			List<PrettyPrintMessages> messages = serviceManager.getMessages(Boolean.valueOf(forceDetails), Boolean.valueOf(ignoreStatus));
			
			logger.info("serviceManager.getMessages()");
			logger.info(FlipTableConverters.fromIterable(messages, PrettyPrintMessages.class));

			sb.append("Alle meldinger \n \n");
			
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

	/**
	 * 
	 * @Example: http://gw.systema.no:8080/altinn-proxy/showHistory.do?user=FREDRIK&filename=log4j_altinn-proxy.log
	 * 
	 * @param session
	 * @param request, user 
	 * @return status
	 */	
	@RequestMapping(value="showHistory.do", method={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String showHistory(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		logger.info("showHistory.do...");
		try {
			String user = request.getParameter("user");
			Assert.notNull(user, "user must be delivered."); 

			String userName = bridfDaoService.getUserName(user);
			Assert.notNull(userName, "userName not found in Bridf."); 

			String fileName = request.getParameter("filename");
			Assert.notNull(fileName, "fileName must be delivered."); 			
			
			
			sb.append(Log4jUtils.getLog4jData(fileName));
			
			
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
	
	private LocalDate getFromCreatedDate(String fraDato) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd"); //as defined in Firmalt
		LocalDate fromDate = LocalDate.parse(fraDato, dateFormatter);
		
		return fromDate;
	}	

	@Autowired
	private BridfDaoService bridfDaoService;
	
	@Autowired
	private ActionsServiceManager serviceManager;
	
}
