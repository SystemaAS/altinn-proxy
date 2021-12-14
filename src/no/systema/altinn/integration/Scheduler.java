//package no.systema.altinn.integration;
//
//import java.time.LocalDateTime;
//
//import org.apache.logging.log4j.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//@Configuration
//@EnableScheduling
//public class Scheduler {
//	private static Logger logger = LogManager.getLogger(Scheduler.class);
//
//	@Autowired
//	private ActionsServiceManager serviceManager;
//
// @Scheduled(cron="${altinn.file.download.cron.pattern}")
//	public void runDownload() {
//		LocalDateTime now = LocalDateTime.now();
//		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
//				.ofPattern("yyyy-MM-dd HH:mm:ss SS");
//		logger.info("::Scheduler::runDownload() about to execute, time=" + now.format(formatter));
//
//		serviceManager.putDagsobjorAttachmentsToPath();
//
//	}
//
//}
