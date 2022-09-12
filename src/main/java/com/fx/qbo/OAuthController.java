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
import com.intuit.ipp.core.IEntity;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Entity;
import com.intuit.ipp.data.Header;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
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

  public void getServiceHandler() {

    try {
      OAuth2PlatformClient client = new OAuth2PlatformClient(oauth2Config);
      BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);
      accessToken = bearerTokenResponse.getAccessToken();
      Config.setProperty(Config.BASE_URL_QBO, baseURL);
      OAuth2Authorizer authorizer = new OAuth2Authorizer(accessToken);
      context = new Context(authorizer, ServiceType.QBO, realmId);
      service = new DataService(context);

    } catch (OAuthException e) {
      Popup OAuthException = new Popup("OAuthException", "Error: Invalid OAuth Request");
      OAuthException.setVisible(true);
      e.printStackTrace();
    } catch (FMSException e) {
      Popup FMSException = new Popup("FMSException", "Error: FMS Exception");
      e.printStackTrace();
    }

  }

  public Customer getCustomer(String name) throws FMSException, OAuthException {
    Customer customer = new Customer();
    Config.setProperty(Config.BASE_URL_QBO, baseURL);
    OAuth2Authorizer authorizer = new OAuth2Authorizer(accessToken);
    context = new Context(authorizer, ServiceType.QBO, realmId);
    service = new DataService(context);

    List<Customer> customers = service.findAll(customer);
    java.util.Iterator itr = customers.iterator();

    while (itr.hasNext()) {
      customer = (Customer) itr.next();
      if (customer.getDisplayName().equals(name)) {
        return customer;
      } else {
        customer = new Customer();
        customer.setDisplayName(name);
      }
    }
    service.add(customer);
    return customer;
  }

  public void postInvoices(ArrayList<Invoice> invoices) {
    for (int i = 0; i < invoices.size(); i++) {
      try {
        service.add(invoices.get(i));
      } catch (FMSException e) {
        Popup FMSException = new Popup("FMSException", "Error: FMS Exception");
        e.printStackTrace();
      }

    }
  }

  public Item getItem(String name, double amount) throws FMSException {
    Item item = new Item();
    Config.setProperty(Config.BASE_URL_QBO, baseURL);
    OAuth2Authorizer authorizer = new OAuth2Authorizer(accessToken);
    context = new Context(authorizer, ServiceType.QBO, realmId);
    service = new DataService(context);
    List<Item> items = service.findAll(item);
    java.util.Iterator itr = items.iterator();

    while (itr.hasNext()) {
      item = (Item) itr.next();


      if (item.getName().equals(name)) {
        return item;
      } else {
        item = new Item();
        item.setName(name);
        item.setUnitPrice(new BigDecimal(amount));
        item.setType(ItemTypeEnum.NON_INVENTORY);
        
      }
    }
    service.add(item);
    return item;
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
