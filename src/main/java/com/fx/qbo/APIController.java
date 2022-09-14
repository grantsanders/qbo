package com.fx.qbo;

import java.io.IOException;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemTypeEnum;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.util.Config;
import com.intuit.oauth2.client.OAuth2PlatformClient;

import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.InvalidRequestException;
import com.intuit.oauth2.exception.OAuthException;

public class APIController {

  public APIController() {
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
      context.setMinorVersion("55");
      service = new DataService(context);

    } catch (OAuthException e) {
      Popup OAuthException = new Popup("OAuthException", "Error: Invalid OAuth Request");
      OAuthException.setVisible(true);
      e.printStackTrace();
    } catch (FMSException e) {
      Popup FMSException = new Popup("FMSException", "Error: FMS Exception");
      FMSException.setVisible(true);
      e.printStackTrace();
    }

  }

  public Customer getCustomer(String name) throws FMSException, OAuthException {
    Customer customer = new Customer();

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
        FMSException.setVisible(true);
        e.printStackTrace();

      }
    }
  }

  public Item createNewItem(Item item) {
    try {

      service.add(item);

    } catch (FMSException e) {

      Popup FMSException = new Popup("FMSException", "Error: FMS Exception");
      FMSException.setVisible(true);
      e.printStackTrace();

    }

    return item;
  }

  public Item updateItem(Item item) {
    item.setSparse(true);
    try {
      item.setSparse(true);
      item = service.update(item);
    } catch (FMSException e) {

      e.printStackTrace();
    }
    return item;
  }

  public List<Item> getItemList() {
    Item item = new Item();

    List<Item> items;
    try {
      items = service.findAll(item);
      return items;

    } catch (FMSException e) {
      e.printStackTrace();
    }

    // if somehow this fails
    return null;
  }

  public Item getItem(String name, double amount) throws FMSException {

    Item item = new Item();
    List<Item> items = service.findAll(item);
    java.util.Iterator itr = items.iterator();

    while (itr.hasNext()) {
      item = (Item) itr.next();
      com.intuit.ipp.data.ReferenceType ref = new com.intuit.ipp.data.ReferenceType();
      ref.setValue("91");

      if (item.getName().equals(name)) {

        item.setUnitPrice(new BigDecimal(amount));
        item.setType(ItemTypeEnum.NON_INVENTORY);
        item.setIncomeAccountRef(ref);
        service.update(item);
        return item;

      } else {

        item = new Item();
        item.setName(name);
        item.setUnitPrice(new BigDecimal(amount));
        item.setType(ItemTypeEnum.NON_INVENTORY);
        item.setIncomeAccountRef(ref);

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
