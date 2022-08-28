package com.fx.qbo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.management.Query;

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
  private List<Scope> scopes = new ArrayList<Scope>();

  private OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(clientId, clientSecret)
      .callDiscoveryAPI(Environment.SANDBOX)
      .buildConfig();

  public void setOAuthUrl() throws InvalidRequestException, IOException, OAuthException {

    String csrf = oauth2Config.generateCSRFToken();

    scopes.add(Scope.Accounting);

    url = oauth2Config.prepareUrl(scopes, redirectUri, csrf);

  }

  public void request() {
    // Prepare OAuth2PlatformClient
    OAuth2PlatformClient client = new OAuth2PlatformClient(oauth2Config);

    // Get the bearer token (OAuth2 tokens)

    try {
      BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

      accessToken = bearerTokenResponse.getAccessToken();

      refreshToken = bearerTokenResponse.getRefreshToken();

      OAuth2Authorizer authorizer = new OAuth2Authorizer(refreshToken);

      Context context = new Context(authorizer, ServiceType.QBO, realmId);

      DataService service = new DataService(context);

      Customer customer = new Customer();
      customer.setDisplayName("bruh");
      customer.setGivenName("bruh sandwich");

      Customer resultCustomer = service.add(customer);

    } catch (FMSException e1) {
      Popup FMSException = new Popup("FMSException", "Error: Invalid authorization request");
      FMSException.setVisible(true);
      e1.printStackTrace();
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
