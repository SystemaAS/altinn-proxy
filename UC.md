#### Menneske:

*UC 1:*
- VAD:Installera .p12-cerificate for organisation Systema AS på www.altinn.no
- HVORDAN: https://www.altinn.no/hjelp/innlogging/alternativ-innlogging-i-altinn/virksomhetssertifikat/
- VEM: Systema AS

*UC 2 :*
- VAD- Skapa virksomhetsbruker med ECKeyRole
- HVORDAN: Logga in i Systema AS med .p12-certificate lag Lag ny bruker
- VEM: Systema AS

*UC 3:*
- VAD: Kunde trenger gi rettighet - RF-1331 Søknad om dagsoppgjør - till organisation Systema AS
- HVORDAN: Daglig leder logger in (valfri metod. ex. mobil inlogging) og delegerar rettigheter.
- VEM: Kunde

#### IT-system:
*UC 4*:
- VAD: Authenticering :
altinn.authenticationUrl=/api/authentication/authenticatewithpassword
altinn.host=www.altinn.no
- HVORDAN: Göra POST på url för att få Cookie tilbake. 
Viktig HTTPHeader-info:
1. API-nøkkel= Bestillt i Systema AS namn
2. userName=virksomhetsbruker skapad i UC 2 ovan.
3. userPassword

*UC 5*:
- VAD: Hent dagsoppgjør-meldinger med filter  ServiceOwner=SKD, ServiceCode=5012, ServiceEdition.171208
- HVORDAN: Gör GET på kundes organisationnummer 
Viktig HTTPHeader-info:
1. API-nøkkel= Bestillt i Systema AS namn
2. userName=virksomhetsbruker skapad i UC 2 ovan.
3. userPassword
4. Set-Cookie, fra UC 4, se ovan.

*UC 6*:
- VAD: Hent specifikt message for info om var attachment ligger.
- HVORDAN: Göra GET på link:self

*UC 7*:
- VAD: Hent attachment, kan vare i PDF och/eller XML-format.
- HVORDAN: Gör GET på link: attachment i specifikt message, se UC 6.

