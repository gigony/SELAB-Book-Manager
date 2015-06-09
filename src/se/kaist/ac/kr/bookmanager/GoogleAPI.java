package se.kaist.ac.kr.bookmanager;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential.Builder;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityService.GetAccessTokenResult;
import com.google.gdata.client.spreadsheet.SpreadsheetService;

public class GoogleAPI {

  private String APPLICATION_NAME = "sebookmanager-v2";
  public List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/drive", "https://spreadsheets.google.com/feeds", "https://docs.google.com/feeds");

  private SpreadsheetService spreadsheetService;
  //notasecret

  public GoogleAPI() {
  }

  public GoogleAPI(String appName, List<String> scopes) {
    APPLICATION_NAME = appName;
    SCOPES = scopes;
  }

  public SpreadsheetService spreadsheetService() {
    SpreadsheetService service = new SpreadsheetService(APPLICATION_NAME);
//    service.setOAuth2Credentials(generateCredentialForGae(SCOPES));
    service.setOAuth2Credentials(generateCredentialWithP12(SCOPES, false));

    service.setProtocolVersion(SpreadsheetService.Versions.V2);
    service.setConnectTimeout(120 * 1000);
    service.getRequestFactory().setHeader("User-Agent", APPLICATION_NAME); // ADDED
    return service;
  }
  private GoogleCredential generateCredentialWithP12(List<String> scopes, boolean useServiceAccountUser) {
    try {
      Builder builder = new GoogleCredential.Builder().setTransport(getTransport()).setJsonFactory(getJsonFactory())
              .setServiceAccountId(getServiceAccountEmail()).setServiceAccountScopes(scopes)
              .setServiceAccountPrivateKeyFromP12File(getServiceAccountKeyFile());

      if (useServiceAccountUser) {
        builder.setServiceAccountUser(getServiceAccountUser());
      }

      return builder.build();
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getServiceAccountUser() {
    return "ggbae@se.kaist.ac.kr";
  }

  private File getServiceAccountKeyFile() {
    return new File("WEB-INF/SEBookManager.p12");
  }

  private String getServiceAccountEmail() {
    return "1072276152621-vkn1tvglqjvgi1vhhuauuc1sr9eeh9bg@developer.gserviceaccount.com";
  }

  private Credential generateCredentialForGae(List<String> scopes) {
    AppIdentityCredential appIdentityCredential = new AppIdentityCredential.Builder(scopes).build();
    AppIdentityService appIdentityService = appIdentityCredential.getAppIdentityService();
    GetAccessTokenResult accessTokenResult = appIdentityService.getAccessToken(scopes);

    GoogleCredential credential = new GoogleCredential();
    credential.setAccessToken(accessTokenResult.getAccessToken());
    credential.setExpirationTimeMilliseconds(accessTokenResult.getExpirationTime().getTime());

    return credential;
  }
  
  private HttpTransport getTransport() {
    return new NetHttpTransport();
  }

  private JsonFactory getJsonFactory() {
    return new JacksonFactory();
  }

}