### Altinn Proxy for Systema AS

The Altinn Proxy is responsible for securing a X.509 entrance to https:www.altinn.no using a Virksomhetscertifikat.
The secured client within the proxy is accessing resources using the Altinn's REST API.
The client can has access to features defined in Altinn.

Using a Virksomhetscertifikat enables usage of the Altinn-feature of rights delegation, from one organization to another.

For more info, see [usecase](UC.md)


### Prerequisites
* Organisation needs to delegate service *5012/171208 "Elektronisk kontoutskrift tollkreditt og dagsoppgjør"* to a virksomhetsbruker defined by Systema AS, with role:ECKeyRole.

### Features
* Read messages from the organisations inbox; filtered on ServiceOwner SKD, ServiceCode 5012, ServiceEdition 171208 and days after [FIRMALT](https://github.com/SystemaAS/syjservicescommon/blob/master/src/main/no/systema/jservices/common/dao/FirmaltDao.java).AIDATO. AIDATO and AITID is updated on download.
* Download attachment i messages: Automatic polling once every hour and checks if messages exist in www.altinn.no, where AIDATO is greater or equals CreatedDate on message.
* See [DownloadController](https://github.com/SystemaAS/altinn-proxy/blob/master/src/no/systema/altinn/DownloadController.java) for manual downloading.


### Set-up
* Make sure the a valid certificate is defined in Tomcat server.
* Make sure that table: [FIRMALT](https://github.com/SystemaAS/syjservicescommon/blob/master/src/main/no/systema/jservices/common/dao/FirmaltDao.java) is configured with appropriate values.
* Make sure approriate rights are delegate from within www.altinn.se 
* See [DownloadController](https://github.com/SystemaAS/altinn-proxy/blob/master/src/no/systema/altinn/DownloadController.java) for verifying read in organisations inbox.

### Issues
* 2018-03: To retrieve correct service, Skattetaten has a workaround, using different [ServiceCode](https://github.com/SystemaAS/altinn-proxy/blob/master/src/no/systema/altinn/entities/ServiceCode.java) and [ServiceEdition](https://github.com/SystemaAS/altinn-proxy/blob/master/src/no/systema/altinn/entities/ServiceEdition.java) than upfront specified.

* 2018-03: The Attachment object is a bit vague. In the HAL-link; attachment, the element _name_ can be used arbitrary. Now it includes filename without extensions.
  See [ActionsServiceManager](https://github.com/SystemaAS/altinn-proxy/blob/master/src/no/systema/altinn/integration/ActionsServiceManager.java) for workaround. 

			if (attLink.getName().endsWith(".pdf") || attLink.getName().endsWith(".xml")) { 
				writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName());
			} else {
				if (attLink.getName().startsWith("Et"))  {
					writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName()).append(".xml");
				} else {
					writeFile = new StringBuilder(halMessage.getCreatedDate().toString()).append("-").append(attLink.getName()).append(".pdf");
				}
			}  
 
