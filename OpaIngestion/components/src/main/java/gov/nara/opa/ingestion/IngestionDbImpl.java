package gov.nara.opa.ingestion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.framework.utilities.DateTimeUtilities;
import com.searchtechnologies.aspire.services.AspireException;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.mapdb.BTreeMap;
import org.w3c.dom.Element;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import static gov.nara.opa.ingestion.ThreadUtils.sleep;

public class IngestionDbImpl extends ComponentImpl implements IngestionDb{
  public static final String DB_FILE_TAG = "dbFile";
  public static final String RECORDS_KEY = "Records";
    public static final String MD5_KEY = "md5";
    public static final String FIRST_INGEST_DATE_KEY = "firstIngestDate";

  private final Gson gson = new Gson();
  private final Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
  
  private DB db;

    private BTreeMap<Integer, String> records;

  @Override
  public void initialize(Element config) throws AspireException {
    String dbFile = getStringFromConfig(config, DB_FILE_TAG, null);
    
    createDirs(dbFile);
    initializeDb(dbFile);
  }

  @Override
  public void close() {
    try {
      debug("Attempting to close Ingestion db");
      if (db != null) {
        db.commit();
        db.close();
        if (db.isClosed()) {
          info("Ingestion db closed.");
        } else {
          error("Ingestion db could not be closed.");
        }
      }
    }catch(Exception e){
      error("Ingestion db error: "+e.getMessage());
    }

  }

  private void createDirs(String file) throws AspireException {
    try {
      Files.createDirectories(Paths.get(file).getParent());
    } catch (IOException ex) {
      throw new AspireException("create-parent-directories", ex);
    }
  }

  private void initializeDb(String file) {
    File dbFile = new File(file);
    db = DBMaker
        .newFileDB(dbFile)
        .mmapFileEnableIfSupported()
        .mmapFileCleanerHackDisable()
        .asyncWriteEnable()
        .asyncWriteQueueSize(100000)
        .make();

    records = db.getTreeMap(RECORDS_KEY);
    
    info("Initialized ingestion db.");
  }  

  @Override
  public void removeRecord(Integer key) {
    records.remove(key);
  }

    @Override
    public String getMD5(Integer key){
        return records.containsKey(key) ? getRecord(key).get(MD5_KEY) : null;
    }

    @Override
    public void setMD5(Integer key, String md5){
        Map<String, String> record = getRecord(key);
        record.put(MD5_KEY, md5);
        records.put(key, toJson(record));
    }

    @Override
    public String getFirstIngestDate(Integer key) {
        return records.containsKey(key) ? getRecord(key).get(FIRST_INGEST_DATE_KEY) : null;
    }

    @Override
    public void setFirstIngestDate(Integer key, String firstIngestDate) {
        Map<String, String> record = getRecord(key);
        record.put(FIRST_INGEST_DATE_KEY, firstIngestDate);
        records.put(key, toJson(record));
    }

    private Map<String, String> getRecord(Integer key) {
        return records.containsKey(key) ? jsonToMap(records.get(key)) : new HashMap<String, String>();
    }

    private Map<String, String> jsonToMap(String json) {
      return gson.fromJson(json, stringStringMap);
    }

    private String toJson(Map<String, String> map){
      return gson.toJson(map, stringStringMap);
    }
}
