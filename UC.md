#### Menneske:

*UC 1:*
- VAD:Installer .p12-cerificate for organisasjon Systema AS på www.altinn.no
- HVORDAN: https://www.altinn.no/hjelp/innlogging/alternativ-innlogging-i-altinn/virksomhetssertifikat/
- VEM: Systema AS

*UC 2 :*
- VAD- Skapa virksomhetsbruker med ECKeyRole
- HVORDAN: Logg in i Systema AS med .p12-certificate lag og Lag ny bruker
- VEM: Systema AS

*UC 3:*
- VAD: Kunde trenger gi rettighet - RF-1331 Søknad om dagsoppgjør - til organisasjon Systema AS
- HVORDAN: Daglig leder logger in (valfri metode. ex. mobil inlogging) og delegerar rettigheter.
- VEM: Kunde

#### IT-system:
*UC 4*:
- VAD: Authenticering :
altinn.authenticationUrl=/api/authentication/authenticatewithpassword
altinn.host=www.altinn.no
- HVORDAN: Göra POST på url for og få Cookie tilbake. 
Viktig HTTPHeader-info:
1. API-nøkkel= Bestillt i Systema AS namn
2. userName=virksomhetsbruker laget i UC 2 ovan.
3. userPassword

*UC 5*:
- VAD: Hent dagsoppgjør-meldinger med filter  ServiceOwner=SKD, ServiceCode=5012, ServiceEdition.171208
- HVORDAN: Gøre GET på kundes organisasjonsnummer 
Viktig HTTPHeader-info:
1. API-nøkkel= Bestillt i Systema AS namn
2. userName=virksomhetsbruker skapad i UC 2 ovan.
3. userPassword
4. Set-Cookie, fra UC 4, se ovan.

*UC 6*:
- VAD: Hent spesifikt message for info om var attachment ligger.
- HVORDAN: Gøre GET på link:self

*UC 7*:
- VAD: Hent attachment, kan vare i PDF og/eller XML-format.
- HVORDAN: Gøre GET på link: attachment i spesifikt message, se UC 6.