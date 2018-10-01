package gov.nara.opa.ingestion;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.logging.ALogger;


/**
 * Send Email with a message.
 */
public class EmailClient {
    final Component component;
    final ALogger logger;
    private String host;
    private String port;
    private boolean useAuth;
    private boolean enableTLS;
    private String username;
    private String password;
    private String fromAddress;
    
    private HashSet<String> toAddresses;
    private final Properties properties;

    public EmailClient(Component component, String host, String port, boolean useAuth, boolean enableTLS, String username, String pass, String fromAddress, String toAddresses){
      this.component = component;
      this.logger = (ALogger)this.component;
      properties = System.getProperties();
      setHost(host);
      setPort(port);
      setUseAuth(useAuth);
      setEnableTLS(enableTLS);
      setUsername(username);
      setPassword(pass);
      setFromAddress(fromAddress);
      setToAddresses(toAddresses);
  }
  
  private boolean getEnableTLS() {
    return enableTLS;
  }

  private void setEnableTLS(boolean enableTLS) {
    this.enableTLS = enableTLS;
  }

  private boolean getUseAuth() {
    return useAuth;
  }

  private void setUseAuth(boolean useAuth) {
    this.useAuth = useAuth;
  }
  
  private String getHost() {
    return host;
  }

  private void setHost(String host) {
    this.host = host;
  }

  private String getUsername() {
    return username;
  }

  private void setUsername(String username) {
    this.username = username;
  }

  private String getPassword() {
    return password;
  }

  private void setPassword(String password) {
    this.password = password;
  }
  
  private String getPort() {
    return port;
  }

  private void setPort(String port) {
    this.port = port;
  }

  private String getFromAddress() {
    return fromAddress;
  }

  private void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  private HashSet<String> getToAddresses() {
    if (toAddresses == null) {
      toAddresses = new HashSet<>();
    }
    return toAddresses;
  }
  
  private void setToAddresses(String toAddresses) {
      String[] addresses = toAddresses.split(";");
      getToAddresses().clear();
      getToAddresses().addAll(Arrays.asList(addresses));
  }
  
  /**
   * Sends a message using the configuration in the account.properties file. The
   * destination accounts must be set before callting this method.
   * 
   * @param subject
   *          The email subject
   * @param body
   *          The email body as html
     * @throws com.searchtechnologies.aspire.services.AspireException
   */
  public void SendMessage(String subject, String body) throws AspireException {
    properties.setProperty("mail.smtp.host", getHost());
    properties.setProperty("mail.smtp.auth", Boolean.toString(getUseAuth()));
    properties.setProperty("mail.smtp.starttls.enable", Boolean.toString(getEnableTLS()));
    properties.setProperty("mail.smtp.port", getPort());

    Session session;

    if (getUseAuth() == false) {
      session = Session.getInstance(properties);
    } else {
      session = Session.getInstance(properties, new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(getUsername(), getPassword());
        }
      });
    }

    MimeMessage message = new MimeMessage(session);

    try {
      message.setFrom(new InternetAddress(getFromAddress()));

      if (toAddresses.size() <= 0) {
        throw new AspireException("Email Client", "Must provide at least one destination address");
      }

      for (String toAddress : toAddresses) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
            toAddress));
      }

      message.setSubject(subject);

      ByteArrayDataSource rawData= new ByteArrayDataSource(body.getBytes(), "text/html");
      DataHandler dh= new DataHandler(rawData);
      message.setDataHandler(dh);
  
      Thread.currentThread().setContextClassLoader(com.sun.mail.smtp.SMTPTransport.class.getClassLoader());
      
      Transport.send(message);

    } catch (AddressException e) {
      logger.error(e, "Email Client - AddressException: %s", e.getLocalizedMessage());
      throw new AspireException("Email Client", e.getMessage());
    } catch (MessagingException e) {
      logger.error(e, "Email Client - MessagingException: %s", e.getLocalizedMessage());
      throw new AspireException("Email Client", e.getMessage());
    }
  }
}
