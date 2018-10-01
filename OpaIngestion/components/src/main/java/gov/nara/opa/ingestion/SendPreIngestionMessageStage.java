/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import com.google.common.base.Strings;
import com.searchtechnologies.aspire.services.*;
import org.w3c.dom.Element;

/**
 * Stage to send email messages when new items appear in pre-ingestion.
 * @author OPA Ingestion Team
 */
public class SendPreIngestionMessageStage extends IngestionStage {
  private static final String MAIL_TO_TAG = "mailSmtpToAddresses";
  private String mailSmtpToAddresses = null;

  Settings settings;
  
  @Override
  public void initialize(Element config) throws AspireException {
    debug("Initializing SendPreIngestionMessageStage");

//    settings = (Settings)this.getComponentDirect("../Ingestion/Settings");
//
//    if (config == null) return;
//    
//
//    mailSmtpToAddresses = getStringFromConfig(config, MAIL_TO_TAG, null);
//    if (Strings.isNullOrEmpty(mailSmtpToAddresses)) {
//      mailSmtpToAddresses = settings.getMailSmtpToAddresses();
//    }
  }
  
  @Override
  public void process(Job job) throws AspireException {
    
    String host = settings.getMailSmtpHost();
    String port = settings.getMailSmtpPort();
    Boolean auth = settings.getMailSmtpAuth();
    Boolean tls = settings.getMailSmtpStartTLSEnable();
    String username = settings.getMailSmtpUsername();
    String pass = settings.getMailSmtpPassword();
    String fromAddress = settings.getMailSmtpFromAddress();
    String toAddresses = mailSmtpToAddresses;
    
    AspireObject doc = job.get();
    
    if (!Strings.isNullOrEmpty(host) && !Strings.isNullOrEmpty(port) &&
        auth != null && tls != null &&
        !Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(pass) &&
        !Strings.isNullOrEmpty(fromAddress) && !Strings.isNullOrEmpty(toAddresses)) {
    
        debug("Settings: %s|%s|%s|%s|%s|%s|%s|%s", host, port, auth, tls, username, pass, fromAddress, toAddresses);
        EmailClient emailClient = new EmailClient(this, host, port, auth, tls, username, pass, fromAddress, toAddresses);
        
        String subject = String.format("Item Added to Staging area: %s", job.getJobId());
        String body = String.format("An item has been added to the staging area (pre-ingestion) for processing: %s", doc.getText("url"));
        emailClient.SendMessage(subject, body);            
    }
    else{
        info("Some attributes needed for email in settings.xml file are not configured");
    }
  }
}
