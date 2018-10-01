package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Email client implementation
 */
@Component
public class EmailClient {
  private static OpaLogger logger = OpaLogger.getLogger(EmailClient.class);

  @Value("${mail.smtp.host}")
  private String host;

  @Value("${mail.smtp.port}")
  private String port;

  @Value("${mail.smtp.auth}")
  private String useAuth;

  @Value("${mail.smtp.starttls.enable}")
  private String enableTLS;

  @Value("${mail.smtp.username}")
  private String username;

  @Value("${mail.smtp.password}")
  private String password;

  @Value("${mail.smtp.fromaddress}")
  private String fromAddress;

  private HashSet<String> toAddresses;
  private Properties properties;

  public String getEnableTLS() {
    return (enableTLS == null || enableTLS.isEmpty() ? "false" : enableTLS);
  }

  public void setEnableTLS(String enableTLS) {
    this.enableTLS = enableTLS;
  }

  public String getUseAuth() {
    return (useAuth == null || useAuth.isEmpty() ? "false" : useAuth);
  }

  public void setUseAuth(String useAuth) {
    this.useAuth = useAuth;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFromAddress() {
    return fromAddress;
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }

  public HashSet<String> getToAddresses() {
    if (toAddresses == null) {
      toAddresses = new HashSet<String>();
    }
    return toAddresses;
  }

  public EmailClient() {
    properties = System.getProperties();

  }

  /**
   * Sends a message using the configuration in the account.properties file. The
   * destination accounts must be set before calling this method.
   * 
   * @param subject
   *          The email subject
   * @param body
   *          The email body as html
   */
  public void SendMessage(String subject, String body) {
    properties.setProperty("mail.smtp.host", host);
    properties.setProperty("mail.smtp.auth", getUseAuth());
    properties.setProperty("mail.smtp.starttls.enable", getEnableTLS());
    properties.setProperty("mail.smtp.port", port);

    Session session;

    if (getUseAuth().equals("false")) {
      session = Session.getInstance(properties);
    } else {
      session = Session.getInstance(properties, new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(username, password);
        }
      });
    }

    MimeMessage message = new MimeMessage(session);

    try {
      message.setFrom(new InternetAddress(fromAddress));

      if (toAddresses.size() <= 0) {
        throw new InvalidParameterException(
            "Must provide at least one destination address");
      }

      for (String toAddress : toAddresses) {
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(
            toAddress));
      }

      message.setSubject(subject);

      message.setContent(body, "text/html");

      Transport.send(message);

    } catch (AddressException e) {
      logger.error(e.getMessage(), e);
      throw new OpaRuntimeException(e);
    } catch (MessagingException e) {
      logger.error(e.getMessage(), e);
      throw new OpaRuntimeException(e);
    }

  }
}