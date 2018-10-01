package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;

public final class Digests {

  public static byte[] digest(String path) throws AspireException{
    byte[] bytes = getBytes(path);            
    return digest(bytes);
  }
  
  private static byte[] getBytes(String path) throws AspireException{
    Path recordFile = Paths.get(path);
    
    byte[] bytes = null;
    
    try {
      bytes = Files.readAllBytes(recordFile);
    } catch (IOException ex) {
      Exceptions.throwAspireException("gov.nara.opa.ingestion.Digests.could-not-get-file-bytes", ex);
    }
    return bytes;
  }
  
  private static byte[] digest(byte[] bytes) throws AspireException{
    byte[] digest = null;
    
    try {
      digest = MessageDigest.getInstance("MD5").digest(bytes);
    } catch (NoSuchAlgorithmException ex) {
      Exceptions.throwAspireException("gov.nara.opa.ingestion.Digests.could-not-get-digest", ex);
    }
    
    return digest;
  }
  
  public static byte[] getMD5Hash(InputStream is) throws AspireException{
    try (DigestInputStream md5Stream = new DigestInputStream(is, MessageDigest.getInstance("MD5"))) {      
      return md5Stream.getMessageDigest().digest();
    } catch (NoSuchAlgorithmException | IOException ex) {
      throw Exceptions.createAspireException("could-not-get-md5-hash", ex);
    }
  }
    
  public static boolean hashesEqual(byte[] first, byte[] second){
    return first != null && second != null && MessageDigest.isEqual(first, second);
  }
  
  public static boolean md5Equal(File first, File second) throws AspireException{
    return md5Hex(first).equals(md5Hex(second));
  }
  
  public static boolean md5Equal(File first, String second) throws AspireException{
    return md5Hex(first).equals(second);
  }
  
  public static byte[] md5(InputStream data) throws AspireException{
    try {
      return DigestUtils.md5(data);
    } catch (IOException ex) {
      throw new AspireException("DigestUtils.md5Hex", ex);
    }
  }

  public static byte[] md5(Path file) throws AspireException{
    try (InputStream inputStream = Files.newInputStream(file)) {
      return DigestUtils.md5(inputStream);
    } catch (IOException ex) {
      throw new AspireException("DigestUtils.md5Hex", ex);
    }
  }
  
  public static byte[] md5(String data) throws AspireException{
      return DigestUtils.md5(data);
  }
  
  public static String md5Hex(Path file) throws AspireException{
    try (InputStream inputStream = Files.newInputStream(file)) {
      return DigestUtils.md5Hex(inputStream);
    } catch (IOException ex) {
      throw new AspireException("DigestUtils.md5Hex", ex);
    }
  }
  
  public static String md5Hex(File file) throws AspireException{
    try (InputStream inputStream = new FileInputStream(file)) {
      return DigestUtils.md5Hex(inputStream);
    } catch (IOException ex) {
      throw new AspireException("DigestUtils.md5Hex", ex);
    }
  }
}
