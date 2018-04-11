//package no.systema.altinn.integration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
//import org.springframework.test.context.ContextConfiguration;
//
//import no.systema.jservices.common.dao.services.FirmaltDaoService;
//import no.systema.jservices.common.dao.services.FirmaltDaoServiceImpl;
//
////@ComponentScan(basePackages = "no.systema.altinn.integration")
////@PropertySource(value = { "classpath:application-test.properties" })
////@ContextConfiguration(locations = {"classpath:syjservicescommon-data-service.xml"})
//public class TestAppConfig {
//
//	@Bean
//	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//		return new PropertySourcesPlaceholderConfigurer();
//	}
//
//}