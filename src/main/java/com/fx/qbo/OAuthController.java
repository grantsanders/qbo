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

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.web.util.UriBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Header;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.SalesItemLineDetail;
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
  private static String baseURL = "https://sandbox-quickbooks.api.intuit.com/v3/company";
  private static Context context;
  private static DataService service;
  

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
    Config.setProperty(Config.BASE_URL_QBO, baseURL);

  }

  public void getTokens() {

    try {
      OAuth2PlatformClient client = new OAuth2PlatformClient(oauth2Config);
      BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);
      accessToken = bearerTokenResponse.getAccessToken();
      Config.setProperty(Config.BASE_URL_QBO, baseURL);
      OAuth2Authorizer authorizer = new OAuth2Authorizer(accessToken);


      context = new Context(authorizer, ServiceType.QBO, realmId);

      service = new DataService(context);

      String sql = "Select * from Customer startposition 1 maxresults 1";

      Customer customer = new Customer();

      customer.setDisplayName("dataservice test");

      // com.fx.qbo.Invoice invoice = new com.fx.qbo.Invoice();

      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      System.out.println(gson.toJson(customer));

      QueryResult queryResult = service.executeQuery(sql);

      System.out.println(queryResult.toString());

    } catch (OAuthException e) {
      Popup OAuthException = new Popup("OAuthException", "Error: Invalid OAuth Request");
      OAuthException.setVisible(true);
      e.printStackTrace();
    } catch (FMSException e) {
      // TODO Auto-generated catch block
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
