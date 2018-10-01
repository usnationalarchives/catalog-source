package gov.nara.opa.ingestion;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.services.AspireException;
import org.w3c.dom.Element;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contains the values of properties defined in file settings.xml
 */
public class Settings extends ComponentImpl{  
  static final String DOWNLOAD_DIGITAL_OBJECTS_ENABLED = "downloadDigitalObjectsEnabled";
  static final String CREATE_THUMBNAILS_ENABLED = "createThumbnailsEnabled";
  static final String CREATE_ZOOM_IMAGES_ENABLED = "createZoomImagesEnabled";
  static final String CONVERT_JPEG2000_ENABLED = "convertJPEG2000Enabled";  
  static final String AWS_ACCESS_KEY_ID = "awsAccessKeyId";
  static final String AWS_SECRET_KEY = "awsSecretKey";
  static final String SOLR_SERVER_KEY = "solrServer"; 
  static final String API_SERVER_KEY = "apiServer"; 
  static final String SOLR_COMMIT_WITHIN = "solrCommitWithin";
  static final String SEND_MESSAGE_ENABLED = "sendMessageEnabled";
  static final String MAIL_SMTP_HOST = "mailSmtpHost";
  static final String MAIL_SMTP_PORT = "mailSmtpPort";
  static final String MAIL_SMTP_FROM_ADDRESS = "mailSmtpFromAddress";
  static final String MAIL_SMTP_AUTH = "mailSmtpAuth";
  static final String MAIL_SMTP_START_TLS_ENABLE = "mailSmtpStarttlsEnable";
  static final String MAIL_SMTP_USER_NAME = "mailSmtpUsername";
  static final String MAIL_SMTP_PASSWORD = "mailSmtpPassword";
  static final String MAIL_SMTP_TO_ADDRESSES = "mailSmtpToAddresses";
  
  static final String S3_STORAGE_ENABLED = "S3StorageEnabled";
  static final String S3_STORAGE_BUCKET_NAME = "S3StorageBucketName";
  static final String S3_STORAGE_ACCESS_KEY_ID = "S3StorageAccessKeyId";
  static final String S3_STORAGE_SECRET_KEY = "S3StorageSecretKey";
  
  static final String OPA_STORAGE = "OpaStorage";
  
  static final String XML_STORE = "xmlStore";
  private static final String EXTRACT_TEXT_ENABLED = "extractTextEnabled";
  private static final String FORCE_TEXT_EXTRACTION = "forceTextExtraction";

  static final String REINDEX = "reindex";
  static final String GET_OBJECTS_FROM_SOLR = "getObjectsFromSolr";

  static final String DAS_WHITE_LIST = "dasWhiteList";
  static final String DAS_LEAF_WHITE_LIST = "dasLeafWhiteList";

  private boolean downloadDigitalObjectsEnabled;
  private boolean createThumbnailsEnabled;
  private boolean createZoomImagesEnabled;
  private boolean convertJpeg2000Enabled;
  private boolean sendMessageEnabled;
  private String solrServer;    
  private String apiServer;    
  private String awsAccessKeyId;
  private String awsSecretKey;
  private String solrCommitWithin;
  private String mailSmtpHost;
  private String mailSmtpPort;
  private String mailSmtpFromAddress;
  private String fingerprint;
  private boolean mailSmtpAuth;
  private boolean mailSmtpStartTLSEnable;
  private String mailSmtpUsername;
  private String mailSmtpPassword;
  private String mailSmtpToAddresses;
  
  private boolean s3StorageEnabled;
  private String s3StorageBucketName;
  private String s3StorageAccessKeyId;
  private String s3StorageSecretKey;
  
  private String opaStorage;
  private String xmlStore;
  private boolean extractTextEnabled;
  private boolean forcedTextExtract;

  private boolean reindex;
  private boolean getObjectsFromSolr;

  private String dasWhiteList;

  private String dasLeafWhiteList;

  private DataSource dataSource;

  @Override
  public void initialize(Element config) throws AspireException {
    dasLeafWhiteList = dasLeafWhiteList = getStringFromConfig(config, DAS_LEAF_WHITE_LIST, null);
    info("DAS Leaf White List: %s", getDasLeafWhiteList());

    dasWhiteList = getStringFromConfig(config, DAS_WHITE_LIST, null);
    info("DAS White List: %s", getDasWhiteList());

    getObjectsFromSolr = getBooleanFromConfig(config, GET_OBJECTS_FROM_SOLR, false);
    info("Getting Objects XML From Solr: %s", getObjectsFromSolr ? "yes" : "no");

    reindex = getBooleanFromConfig(config, REINDEX, false);
    info("Reindexing: %s", reindex ? "yes" : "no");

    downloadDigitalObjectsEnabled = getBooleanFromConfig(config, DOWNLOAD_DIGITAL_OBJECTS_ENABLED, true);
    info("Download digital objects: %s", downloadDigitalObjectsEnabled ? "yes" : "no");
    
    createThumbnailsEnabled = getBooleanFromConfig(config, CREATE_THUMBNAILS_ENABLED, true);
    info("Create thumbnails: %s", createThumbnailsEnabled ? "yes" : "no");
    
    createZoomImagesEnabled = getBooleanFromConfig(config, CREATE_ZOOM_IMAGES_ENABLED, true);
    info("Create zoom images: %s", createZoomImagesEnabled ? "yes" : "no");
    
    convertJpeg2000Enabled = getBooleanFromConfig(config, CONVERT_JPEG2000_ENABLED, true);
    info("Convert Jpeg2000 images: %s", convertJpeg2000Enabled ? "yes" : "no");

    extractTextEnabled = getBooleanFromConfig(config, EXTRACT_TEXT_ENABLED, true);
    info("Extract text: %s", extractTextEnabled ? "yes" : "no");

    forcedTextExtract = getBooleanFromConfig(config, FORCE_TEXT_EXTRACTION, false);
    info("Force text extraction: %s", forcedTextExtract ? "yes" : "no");
    
    awsAccessKeyId = getStringFromConfig(config, AWS_ACCESS_KEY_ID, null);
    awsSecretKey = getStringFromConfig(config, AWS_SECRET_KEY, null);    
    
    solrServer = getStringFromConfig(config, SOLR_SERVER_KEY, null);
    info("Solr server url: %s", solrServer);
    
    solrCommitWithin = getStringFromConfig(config, SOLR_COMMIT_WITHIN, null);
    info("Solr commit within: %s", getSolrCommitWithin());

    apiServer = getStringFromConfig(config, API_SERVER_KEY, null);
    info("API server url: %s", apiServer);
    
    sendMessageEnabled = getBooleanFromConfig(config, SEND_MESSAGE_ENABLED, false);
    info("Send message when object process end %s", sendMessageEnabled ? "yes": "no");
    
    mailSmtpHost = getStringFromConfig(config, MAIL_SMTP_HOST, null);
    info("Mail SMTP Host %s", mailSmtpHost);
    
    mailSmtpPort = getStringFromConfig(config, MAIL_SMTP_PORT, null);
    info("Mail SMTP Port %s", mailSmtpPort);
    
    mailSmtpFromAddress = getStringFromConfig(config, MAIL_SMTP_FROM_ADDRESS, null);
    info("Mail SMTP From Address %s", mailSmtpFromAddress);
    
    mailSmtpAuth = getBooleanFromConfig(config, MAIL_SMTP_AUTH, true);
    info("Mail SMTP Auth %s", mailSmtpAuth ? "yes": "no");
    
    mailSmtpStartTLSEnable = getBooleanFromConfig(config, MAIL_SMTP_START_TLS_ENABLE, true);
    info("Mail SMTP TLS Enable %s", mailSmtpStartTLSEnable ? "yes": "no");
    
    mailSmtpUsername = getStringFromConfig(config, MAIL_SMTP_USER_NAME, null);
    info("Mail SMTP username %s", mailSmtpUsername);
    
    mailSmtpPassword = getStringFromConfig(config, MAIL_SMTP_PASSWORD, null);
    
    mailSmtpToAddresses = getStringFromConfig(config, MAIL_SMTP_TO_ADDRESSES, null);
    info("Mail SMTP To Address %s", mailSmtpToAddresses);
    
    s3StorageEnabled = getBooleanFromConfig(config, S3_STORAGE_ENABLED, false);
    info("S3 storage enabled: %s", isS3StorageEnabled() ? "yes" : "no");
    
    s3StorageBucketName = getStringFromConfig(config, S3_STORAGE_BUCKET_NAME, null);
    info("S3 storage bucket name: %s", getS3StorageBucketName());
    
    s3StorageAccessKeyId = getStringFromConfig(config, S3_STORAGE_ACCESS_KEY_ID, null);
    
    s3StorageSecretKey = getStringFromConfig(config, S3_STORAGE_SECRET_KEY, null);
    
    opaStorage = getStringFromConfig(config, OPA_STORAGE, null);
    info("OPA Storage: %s", getOpaStorage());

    xmlStore = getStringFromConfig(config, XML_STORE, null);
    info("Xml Store: %s", getXmlStore());
    
    fingerprint = Constants.FINGERPRINT;
    info("Fingerprint: %s", fingerprint);

    initializeDataSource(config);
  }

  private void initializeDataSource(Element config) throws AspireException {
    dataSource = createDataSource(config);
  }

  private DataSource createDataSource(Element config) throws AspireException {
    ComboPooledDataSource cpds = new ComboPooledDataSource();

    try {
      String driverClassName = getStringFromConfig(config, "dbDriverClass", null);
      cpds.setDriverClass(driverClassName); //loads the jdbc driver
    } catch (PropertyVetoException e) {
      throw new AspireException("Pooled data source setDriverClass", e);
    }

    String dbUrl = getStringFromConfig(config, "dbUrl", null);
    cpds.setJdbcUrl(dbUrl);

    String dbUser = getStringFromConfig(config, "dbUser", null);
    cpds.setUser(dbUser);

    String dbPassword = getStringFromConfig(config, "dbPassword", null);
    cpds.setPassword(dbPassword);

    int dbMaxPoolSize = getIntegerFromConfig(config, "dbMaxPoolSize", 15, 0, 64);
    cpds.setMaxPoolSize(dbMaxPoolSize);

    return cpds;
  }

  @Override
  public void close() {
  }
   
  public boolean downloadDigitalObjectsIsEnabled(){
    return downloadDigitalObjectsEnabled;
  }
  
  public boolean createThumbnailsIsEnabled(){
    return createThumbnailsEnabled;
  }
  
  public boolean createZoomImagesIsEnabled(){
    return createZoomImagesEnabled;
  }
  
  public String getAwsAccessKeyId(){
    return awsAccessKeyId;
  }

  public String getAwsSecretKey() {
    return awsSecretKey;
  }
  
  public boolean convertJpeg2000IsEnabled(){
    return convertJpeg2000Enabled;
  }

  public String getSolrServer() {
    return solrServer;
  }

  public String getSolrCommitWithin() {
    return solrCommitWithin;
  }
  
  public String getAPIServer() {
    return apiServer;
  }

  public boolean sendMessageEnabled() {
    return sendMessageEnabled;
  }
  
  public String getMailSmtpHost() {
    return mailSmtpHost;
  }
  
  public String getMailSmtpPort() {
    return mailSmtpPort;
  }
  
  public String getMailSmtpFromAddress() {
    return mailSmtpFromAddress;
  }
  
  public boolean getMailSmtpAuth() {
    return mailSmtpAuth;
  }
  
  public boolean getMailSmtpStartTLSEnable() {
    return mailSmtpStartTLSEnable;
  }
  
  public String getMailSmtpUsername() {
    return mailSmtpUsername;
  }
  
  public String getMailSmtpPassword() {
    return mailSmtpPassword;
  }
  
  public String getMailSmtpToAddresses() {
    return mailSmtpToAddresses;
  }

  public boolean isS3StorageEnabled() {
    return s3StorageEnabled;
  }

  public String getS3StorageBucketName() {
    return s3StorageBucketName;
  }

  public String getS3StorageAccessKeyId() {
    return s3StorageAccessKeyId;
  }

  public String getS3StorageSecretKey() {
    return s3StorageSecretKey;
  }

  public String getOpaStorage() {
    return opaStorage;
  }

  public String getXmlStore() {
    return xmlStore;
  }

  public boolean isExtractTextEnabled() {
    return extractTextEnabled;
  }

  public boolean isForcedTextExtract() {
    return forcedTextExtract;
  }

  public boolean isReindex() {
    return reindex;
  }

  public boolean getObjectsFromSolr() {
    return getObjectsFromSolr;
  }

  public String getDasWhiteList() {
    return dasWhiteList;
  }

  public String getDasLeafWhiteList() {
    return dasLeafWhiteList;
  }

  public Connection getDbConnection() throws SQLException {
    return dataSource.getConnection();
  }
}
