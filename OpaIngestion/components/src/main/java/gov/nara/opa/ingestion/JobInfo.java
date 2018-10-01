package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import org.apache.commons.io.FilenameUtils;

public final class JobInfo {
  private File file;
  private String filename;
  private String recordType;
  private Lock objectsXmlLock;
  private String objectId;
  private AspireObject digitalObject;
  private OpaStorage opaStorage;
  private AspireObject record;
  private Integer naid;
  private boolean processingAnnotations;
  private boolean isPrimary;
  private AspireObject fileNode;
  private Queue<File> filesCopiedFromPreIngestion;
  private Map<String, AspireObject> digitalObjects;
  private String description;
  private Map<String, Integer> prevSortNums;
  private Map<String, Integer> sortNums;
  private boolean objectSortChanged;
  private String mimeType;
  private boolean isNewerRecord;
  
  private JobInfo parent;
  private AtomicInteger subJobOutstandingCount;
  private AtomicInteger subJobFailedCount;
  private Job job;
  private AspireObject jobData;
  private Job parentJob;
  
  private AspireObject recordFromXmlStore;

  private final ArrayList<AspireObject> objectContents = new ArrayList<>();
  private File contentFile;
  
  private final AspireObjectFactory aspireObjectFactory = new AspireObjectFactory();
  private String name;
  private boolean forceFeed;

  private boolean extractTextEnabled;
  private boolean forcedTextExtract;

  private File metaDataFile;
  private File extractedTextFile;
  private File extractedPaginatedTextFile;

  private Long objectFileSize;

  private AspireObject previousObjectsXML;

  private boolean isLegacyObject;
  private URL objectSourceURL;

  private File TMDContentFile;

  private boolean doTMDRegeneration;

  private boolean doExtractText;


    public void close(){
      file = null;
      objectsXmlLock = null;
      digitalObject = null;
      opaStorage = null;
      record = null;
      fileNode = null;

      if (filesCopiedFromPreIngestion != null){
          filesCopiedFromPreIngestion.clear();
      }

      if (digitalObjects != null){
          digitalObjects.clear();
      }

      if (prevSortNums != null) {
        prevSortNums.clear();
      }

      if (sortNums != null) {
        sortNums.clear();
      }

      parent = null;
      subJobOutstandingCount = null;
      subJobFailedCount = null;
      job = null;
      jobData = null;
      parentJob = null;

      objectContents.clear();
      
      if (contentFile != null && contentFile.exists()){
        contentFile.delete();
      }

      if (metaDataFile != null && metaDataFile.exists()){
        metaDataFile.delete();
      }

      if (extractedTextFile != null && extractedTextFile.exists()){
        extractedTextFile.delete();
      }

      if (extractedPaginatedTextFile != null && extractedPaginatedTextFile.exists()){
        extractedPaginatedTextFile.delete();
      }

      if (TMDContentFile != null && TMDContentFile.exists()) {
        TMDContentFile.delete();
      }

      previousObjectsXML = null;
      objectSourceURL = null;

      metaDataFile = null;
      extractedTextFile = null;
      extractedPaginatedTextFile = null;
      TMDContentFile = null;

      objectFileSize = null;
  }

  public JobInfo(){

  }

  public String getName() {
    return name;
  }

  public JobInfo(JobInfo parent, AspireObject digitalObject) throws AspireException{
    setNAID(parent.getNAID());
    setDigitalObject(digitalObject);
    setOpaStorage(parent.getOpaStorage());
    setObjectsXmlLock(parent.getObjectsXmlLock());
  }

  public File getFile(){
    return file;
  }

  public void setFile(File value){
    file = value;
  }

  public String getFileName(){
    return filename;
  }

  public void setFileName(String value){
    filename = value;
  }

  public Integer getNAID(){
    return naid;
  }

  public void setNAID(String value){
    naid = new Integer(value);
  }

  public void setNAID(Integer value){
    naid = value;
  }

  public boolean isArchivalDescription(){
    return Records.ARCHIVAL_DESCRIPTION_TAGS.contains(recordType);
  }

  public boolean isAuthorityRecord(){
    return Records.AUTHORITY_RECORD_TAGS.contains(recordType);
  }
  
  public String getRecordMD5() throws AspireException {
	  return Digests.md5Hex(getFile());
  }

  public String getRecordType(){
    return recordType;
  }

  public void setRecordType(String value){
    recordType = value;
  }

  public Lock getObjectsXmlLock(){
    return objectsXmlLock;
  }

  public void setObjectsXmlLock(Lock value){
    objectsXmlLock = value;
  }

  public String getObjectId(){
    return objectId != null ? objectId : (objectId = createObjectId());
  }

  private String createObjectId(){
    return digitalObject != null ? digitalObject.getAttribute("id") : null;
  }

  /**
   * This method is intended to support Unit Tests and should not be used for
   * any other scenario.
   */
  void setObjectId(String value){
    objectId = value;
  }

  public AspireObject getDigitalObject(){
    return digitalObject;
  }

  public void setDigitalObject(AspireObject value) throws AspireException{
    digitalObject = value;

    fileNode = digitalObject.get("file");
    name = FilenameUtils.getName(fileNode.getAttribute("path"));
    isPrimary = "primary".equals(fileNode.getAttribute("type"));
    mimeType = fileNode.getAttribute("mime");
  }

  public OpaStorage getOpaStorage(){
    return opaStorage;
  }

  public void setOpaStorage(OpaStorage value){
     opaStorage = value;
  }

  public AspireObject getRecord(){
    return record;
  }

  public void setRecord(AspireObject value){
    record = value;
  }

  public boolean isProcessingAnnotations(){
    return processingAnnotations;
  }

  public void setIsProcessingAnnotations(boolean value){
    processingAnnotations = value;
  }

  public boolean isPrimary(){
    return isPrimary;
  }

  public AspireObject getRecordFromXmlStore() throws AspireException{
    return recordFromXmlStore;
  }

  public void setRecordFromXmlStore(AspireObject recordFromXmlStore) {
    this.recordFromXmlStore = recordFromXmlStore;
  }

  public Queue<File> getFilesCopiedFromPreIngestion() {
    return filesCopiedFromPreIngestion;
  }

  public void setFilesCopiedFromPreIngestion(Queue<File> filesCopiedFromPreIngestion) {
    this.filesCopiedFromPreIngestion = filesCopiedFromPreIngestion;
  }

  public Map<String, AspireObject> getDigitalObjects() {
    return digitalObjects;
  }

  public Map<String, AspireObject> createDigitalObjects() {
    return (this.digitalObjects = new HashMap<>());
  }

  public Map<String, Integer> getPrevSortNums() {
    return prevSortNums;
  }

  public Map<String, Integer> createPrevSortNums() {
    return (this.prevSortNums = new HashMap<>());
  }

  public Map<String, Integer> getSortNums() {
    return sortNums;
  }

  public Map<String, Integer> createSortNums() {
    return (this.sortNums = new HashMap<>());
  }

  public boolean getObjectSortChanged() {
    return this.objectSortChanged;
  }

  public void setObjectSortChanged(boolean objectSortChanged) {
    this.objectSortChanged = objectSortChanged;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public InputStream createInputStream() throws AspireException {
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException ex) {
      throw new AspireException("input stream from file", ex);
    }
  }

  public JobInfo getParent() {
    return parent;
  }

  public void setParent(JobInfo parent) {
    this.parent = parent;
  }

  public AtomicInteger getSubJobOutstandingCount() {
    return subJobOutstandingCount;
  }

  public void setSubJobOutstandingCount(AtomicInteger subJobOutstandingCount) {
    this.subJobOutstandingCount = subJobOutstandingCount;
  }

  public AtomicInteger getSubJobFailedCount() {
    return subJobFailedCount;
  }

  public void setSubJobFailedCount(AtomicInteger subJobFailedCount) {
    this.subJobFailedCount = subJobFailedCount;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public AspireObject getJobData() {
    return jobData;
  }

  public void setJobData(AspireObject jobData) {
    this.jobData = jobData;
  }

  public Job getParentJob() {
    return parentJob;
  }

  public void setParentJob(Job parentJob) {
    this.parentJob = parentJob;
  }

  public boolean isImage(){
    if (fileNode == null)
      return false;

    String type = fileNode.getAttribute("type");
    String mime = fileNode.getAttribute("mime");
    return "primary".equals(type)
      && mime != null
      && mime.contains("image");
  }

  public boolean isPDF(){
    if (fileNode == null)
      return false;

    String type = fileNode.getAttribute("type");
    String mime = fileNode.getAttribute("mime");
    return "primary".equals(type)
      && mime != null
      && mime.contains("pdf");
  }

  public ArrayList<AspireObject> getObjectContents() {
    return objectContents;
  }

  public boolean isTextExtractSource(){
    if (fileNode == null)
      return false;

    String type = fileNode.getAttribute("type");
    String mime = fileNode.getAttribute("mime");
    return
      "primary".equals(type)
      && mime != null
      &&  (
            mime.equals("application/pdf") ||
            mime.equals("text/plain") ||
            mime.equals("text/html") ||
            mime.equals("video/x-ms-wmv") ||
            mime.equals("video/mp4") ||
            mime.equals("video/avi") ||
            mime.equals("video/x-msvideo") ||
            mime.equals("application/vnd.rn-realmedia") ||
            mime.equals("video/quicktime") ||
            mime.equals("application/mswrite") ||
            mime.equals("application/excel") ||
            mime.equals("application/mspowerpoint") ||
            mime.equals("application/msword")
          );
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean isJPEG2000Image(){
    return getJPEG2000FileElement() != null;
  }
  
  private AspireObject getJPEG2000FileElement(){
    for (AspireObject child : digitalObject.getChildren(true)){
      if (child.getName().equals("file") && "image/jp2".equals(child.getAttribute("mime"))){
        return child;
      }
    }
    
    return null;
  }
     
  public String getPathToContent() throws AspireException{
    String path = fileNode.getAttribute("path");
    return opaStorage.getFullPathInLive(stripLeadingSlash(path));
  }
  
  public String getPathToJPEG2000Image() throws AspireException{
    AspireObject fileElement = getJPEG2000FileElement();
    if (fileElement == null){
      return null;
    }
    
    String path = fileElement.getAttribute("path");
    return opaStorage.getFullPathInLive(stripLeadingSlash(path));
  }
  
  private String stripLeadingSlash(String value){
    return value.startsWith("/") ? value.substring(1) : value;
  }
  
  String getPathToThumbnail() throws AspireException {
    String relativePath = 
            OpaStorage.OPA_RENDITIONS + "/" + 
            OpaStorage.THUMBNAILS + "/" +
            getName() + "-thumb.jpg";
    return opaStorage.getFullPathInLive(relativePath);
  }

  String getPathToDeepZoomImage() throws AspireException {
    String relativePath = 
            OpaStorage.OPA_RENDITIONS + "/" + 
            OpaStorage.IMAGE_TILES + "/" +
            getName() + ".dzi";
    return opaStorage.getFullPathInLive(relativePath);
  }

  String getPathToDeepZoomFilesDir() throws AspireException {
    String relativePath = 
            OpaStorage.OPA_RENDITIONS + "/" + 
            OpaStorage.IMAGE_TILES + "/" +
            getName() + "_files";
    return opaStorage.getFullPathInLive(relativePath);
  }

  public String getPathToExtractedText() throws AspireException {
    String relativePath = 
            OpaStorage.OPA_RENDITIONS + "/" + 
            OpaStorage.EXTRACTED_TEXT + "/" +
            getName() + ".txt";
    return opaStorage.getFullPathInLive(relativePath);
  }

  public String getLegacyPathToExtractedText() throws AspireException {
    String relativePath =
            OpaStorage.OPA_RENDITIONS + "/" +
                    OpaStorage.EXTRACTED_TEXT + "/" +
                    getName() + ".txt";
    return opaStorage.getLegacyLiveBaseKey()+"/"+relativePath;
  }

  public String getPathToPaginatedText() throws AspireException {
    String relativePath =
            OpaStorage.OPA_RENDITIONS + "/" +
                    OpaStorage.EXTRACTED_TEXT + "/" +
                    getName() + ".paginated.txt";
    return opaStorage.getFullPathInLive(relativePath);
  }

  public String getLegacyPathToPaginatedText() throws AspireException {
    String relativePath =
            OpaStorage.OPA_RENDITIONS + "/" +
                    OpaStorage.EXTRACTED_TEXT + "/" +
                    getName() + ".paginated.txt";
    return opaStorage.getLegacyLiveBaseKey()+"/"+relativePath;
  }

  public File getContentFile() throws AspireException {
    return contentFile != null ? contentFile : (contentFile = opaStorage.getFile(getPathToContent()));
  }

  void saveRecord() throws AspireException, IOException {
      File file = File.createTempFile("record", ".xml");
      getRecord().writeXml(file, AspireObject.PRETTY);

    String pathToRecordInXmlStore = getPathToRecordInXmlStore(naid);

    boolean fileHasChanged = !Digests.md5Hex(file).equals(opaStorage.md5Hex(pathToRecordInXmlStore));

    if (fileHasChanged){
      opaStorage.saveFile(file, pathToRecordInXmlStore);
    }

      file.delete();
  }

  /**
    Use the legacy objects.xml/description.xml path if that file exists.
   Otherwise, use the new xmlstore/full/nn/nnnn/naid path.

   */
  String getPathToObjectsXml() throws AspireException {
    return opaStorage.getLegacyLiveBaseKey()+"/"+OpaStorage.OBJECTS_XML;
  }

  String getPathToDescriptionXml() throws AspireException {
    return opaStorage.getLegacyLiveBaseKey()+"/"+OpaStorage.DESCRIPTION_XML;
  }

  AspireObject getRecordInXmlStore() throws AspireException {
    return getRecordInXmlStore(naid);
  }

  AspireObject getRecordInXmlStore(Integer naId) throws AspireException {
    File file = getRecordFileInXmlStore(naId);
    AspireObject record = null;
    
    if (file != null && file.exists()){
      try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)){
        record = AspireObject.createFromXML(reader);
      } catch (Exception ex) {
        throw new AspireException("AspireObject.createFromXML", ex, "Failed to load file %s", file);
      }

      file.delete();
    }
    
    return record;
    
  }

  public String getPathToRecordInXmlStore(Integer naId) throws AspireException {
    StringBuilder sb = new StringBuilder(50);
    sb.append("full");
    sb.append(File.separatorChar);
    sb.append(NAIDDirectories.getLevel1(naId));
    sb.append(File.separatorChar);
    sb.append(NAIDDirectories.getLevel2(naId));
    sb.append(File.separatorChar);
    sb.append(naId);
    sb.append(".xml");
    
    return opaStorage.getFullPathInXmlStore(sb.toString());
  }
  
  public File getRecordFileInXmlStore(Integer naId) throws AspireException{
    String pathToRecordInXmlStore = getPathToRecordInXmlStore(naId);
    return opaStorage.getFile(pathToRecordInXmlStore);
  }


  void deleteOpaIP() throws AspireException {
    opaStorage.deleteFiles(getPathToRecordInXmlStore(naid));
  }

    public void setForceFeed(boolean forceFeed) {
        this.forceFeed = forceFeed;
    }

    public boolean isForceFeed() {
        return forceFeed;
    }

  public boolean isExtractTextEnabled() {
    return extractTextEnabled;
  }

  public void setExtractTextEnabled(boolean extractTextEnabled) {
    this.extractTextEnabled = extractTextEnabled;
  }

  public boolean isForcedTextExtract() {
    return forcedTextExtract;
  }

  public void setForcedTextExtract(boolean forcedTextExtract) {
    this.forcedTextExtract = forcedTextExtract;
  }

  public File getMetaDataFile() {
    return metaDataFile;
  }

  public void setMetaDataFile(File metaDataFile) {
    this.metaDataFile = metaDataFile;
  }

  public File getExtractedTextFile() {
    return extractedTextFile;
  }

  public void setExtractedTextFile(File extractedTextFile) {
    this.extractedTextFile = extractedTextFile;
  }

  public File getExtractedPaginatedTextFile() {
    return extractedPaginatedTextFile;
  }

  public void setExtractedPaginatedTextFile(File extractedPaginatedTextFile) {
    this.extractedPaginatedTextFile = extractedPaginatedTextFile;
  }

  public Long getObjectFileSize() {
    return objectFileSize;
  }

  public void setObjectFileSize(Long objectFileSize) {
    this.objectFileSize = objectFileSize;
  }

  public AspireObject getPreviousObjectsXML() {
    return previousObjectsXML;
  }

  public void setPreviousObjectsXML(AspireObject previousObjectsXML) {
    this.previousObjectsXML = previousObjectsXML;
  }

  public boolean isLegacyObject() {
    return isLegacyObject;
  }

  public void setLegacyObject(boolean legacyObject) {
    isLegacyObject = legacyObject;
  }

  public URL getObjectSourceURL() {
    return objectSourceURL;
  }

  public void setObjectSourceURL(URL objectSourceURL) {
    this.objectSourceURL = objectSourceURL;
  }

  public File getTMDContentFile() {
    return TMDContentFile;
  }

  public void setTMDContentFile(File TMDContentFile) {
    this.TMDContentFile = TMDContentFile;
  }

  public boolean isDoTMDRegeneration() {
    return doTMDRegeneration;
  }

  public void setDoTMDRegeneration(boolean doTMDRegeneration) {
    this.doTMDRegeneration = doTMDRegeneration;
  }

  public boolean isDoExtractText() {
    return doExtractText;
  }

  public void setDoExtractText(boolean doExtractText) {
    this.doExtractText = doExtractText;
  }
}
