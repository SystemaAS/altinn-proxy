***Altinn Proxy for Systema AS***

The Altinn Proxy is responsible for securing a X.509 entrance to https:www.altinn.no using a Virksomhetscertifikat.
The secured client within the proxy is accessing resources using the Altinn's REST API.
The client can has access to features defined in Altinn.

Using a Virksomhetscertifikat enables usage of the Altinn-feature of rights delegation, from one organization to another.

For more detailed info, see [usecase](UC.md)


## Prerequsities
* Organisation needs to delegate service *RF-1331 Søknad om dagsoppgjør* to a virksomhetsbruker defined by Systema AS, with role:ECKeyRole.

## Features
* Read messages from the organisation innbox; filtered on ServiceOwner SKD, ServiceCode 5012 and ServiceEdition 171208. 
* Download attachment i messages: automatic and manually.


## Set-up
* Make sure the a valid certificate is defined in Tomcat server.
* Make sure that table: [FIRMALT](https://github.com/SystemaAS/syjservicescommon/blob/master/src/main/no/systema/jservices/common/dao/FirmaltDao.java) is configured with appropriate values.


