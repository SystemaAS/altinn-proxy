package no.systema.altinn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import no.systema.altinn.integration.ActionsServiceManager;
import no.systema.jservices.common.dao.services.BridfDaoService;

@Controller
public class DownloadController {
	private static Logger logger = Logger.getLogger(DownloadController.class.getName());

	/**
	 * Entrance for accessing info in secure www.altinn.no, using .P12 certificate
	 * 
	 * @Example: http://gw.systema.no:8080/altinn-proxy/downloadDagsobjor.do?user=FREDRIK
	 * 
	 * @param session
	 * @param request, user 
	 * @return status
	 */	
	@RequestMapping(value="downloadDagsobjor.do", method={RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String download(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();

		logger.info("downloadDagsobjor.do...");
		try {
			String user = request.getParameter("user");
			Assert.notNull(user, "user must be delivered."); 

			String userName = bridfDaoService.getUserName(user);
			Assert.notNull(userName, "userName not found in Bridf."); 
			
			
			serviceManager.putDagsobjorPDFRepresentationToPath();
			
			logger.info("serviceManager.putDagsobjorPDFRepresentationToPath() executed...");
			
			
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
	
	@Autowired
	private BridfDaoService bridfDaoService;
	
	@Autowired
	private ActionsServiceManager serviceManager;
	
}
