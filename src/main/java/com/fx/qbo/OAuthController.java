package com.fx.qbo;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import javax.management.Query;
import javax.swing.text.html.HTMLDocument.Iterator;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.web.util.UriBuilder;

import com.google.gson.Gson;
import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Header;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.services.QueryResult;
import com.intuit.ipp.util.Config;
import com.intuit.oauth2.client.OAuth2PlatformClient;

import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.InvalidRequestException;
import com.intuit.oauth2.exception.OAuthException;

public class OAuthController {

  public OAuthController() {
  }

  private static String clientId = "ABUUUFqGcAlG9vL09YJqvxYs4H0MZ1OQAp5Obj1PjWpDiVUSv5";
  private static String clientSecret = "jeVET4dMqTElbxnxQX5BZ3uLW1CUHWlEtiUkXAWs";
  private static String redirectUri = "http://localhost:8080/oauth2redirect";
  private static String authCode = "code";
  private static String url = "";
  private static String accessToken = "";
  private static String refreshToken = "";
  private static String realmId = "";
  private static String baseURL = "https://sandbox-quickbooks.api.intuit.com";


  private OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(clientId, clientSecret)
      .callDiscoveryAPI(Environment.SANDBOX)
      .buildConfig();

  public void setOAuthUrl() throws InvalidRequestException, IOException, OAuthException {

    String csrf = oauth2Config.generateCSRFToken();
    List<Scope> scopes = new ArrayList<Scope>();

    scopes.add(Scope.Accounting);
    scopes.add(Scope.OpenId);
    scopes.add(Scope.Email);
    scopes.add(Scope.Address);
    scopes.add(Scope.Phone);
    scopes.add(Scope.Profile);


    url = oauth2Config.prepareUrl(scopes, redirectUri, csrf);

  }

  public void request() {
    // Prepare OAuth2PlatformClient
    OAuth2PlatformClient client = new OAuth2PlatformClient(oauth2Config);
    // Get the bearer token (OAuth2 tokens)

    try {
      BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

      accessToken = bearerTokenResponse.getAccessToken();

      // refreshToken = bearerTokenResponse.getRefreshToken();

      Customer customer = new Customer();
      customer.setDisplayName("bruh");

      URIBuilder builder = new URIBuilder() 
      .setScheme("https")
      .setHost("sandbox-quickbooks.api.intuit.com")
      .setPath("/v3/company/" + realmId + "/query")
      .setParameter("query", "select * from customer where displayname = 'bruh'");
      String query = builder.toString();      

      HttpClient HTTPClient = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(query))
          .header("Authorization", "Bearer " + accessToken)
          .header("content_type", "application/json")
          .header("Accept", "application/json")
          .GET()
          .build();
      HTTPClient.sendAsync(request, BodyHandlers.ofString())
          .thenApply(HttpResponse::body)
          .thenAccept(System.out::println)
          .join();


    } catch (OAuthException e) {
      Popup OAuthException = new Popup("OAuthException", "Error: Invalid OAuth Request");
      OAuthException.setVisible(true);
      e.printStackTrace();
    }

  }

  public void setAuthCode(String code) {
    authCode = code;
  }

  public String getUrl() {
    return url;
  }

  public String getClientId() {
    return clientId;
  }

  public void setRealmId(String id) {
    realmId = id;
  }
}
