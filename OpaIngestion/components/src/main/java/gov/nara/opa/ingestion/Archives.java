package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;

/**
 * This class contains utility methods for archive files.
 */
public class Archives {
  public static boolean isArchive(String fileName){
    return isCompressedTarArchive(fileName) ||
      isZipArchive(fileName);
  }
  
  public static boolean isCompressedTarArchive(String fileName){
    return fileName.endsWith(".tar.gz") || fileName.endsWith(".tar.bz2");
  }

  public static boolean isZipArchive(String fileName) {
    return fileName.endsWith(".zip") || fileName.endsWith(".7z");
  }

  public static ArchiveInputStream createArchiveInputStream(File file) throws AspireException{
    BufferedInputStream inputStream;
    try {
      inputStream = new BufferedInputStream(new FileInputStream(file));
    } catch (FileNotFoundException e) {
      throw new AspireException("file not found", e);
    }

    String name = file.getName();

    String compressorName = null;
    String uncompressedFileName = null;

    if (GzipUtils.isCompressedFilename(name)){
      compressorName = CompressorStreamFactory.GZIP;
      uncompressedFileName = GzipUtils.getUncompressedFilename(name);
    } else if (BZip2Utils.isCompressedFilename(name)){
      compressorName = CompressorStreamFactory.BZIP2;
      uncompressedFileName = BZip2Utils.getUncompressedFilename(name);
    }

    InputStream compressorInputStream;

    try {
      compressorInputStream = compressorName == null
              ? inputStream
              : new CompressorStreamFactory().createCompressorInputStream(compressorName, inputStream);
    } catch (CompressorException ex) {
      throw new AspireException("could-not-create-compressor-input-stream", ex);
    }

    String extension = uncompressedFileName != null
            ? FilenameUtils.getExtension(uncompressedFileName)
            : FilenameUtils.getExtension(name);

    String archiverName;

    switch(extension){
      case ArchiveStreamFactory.TAR:
        archiverName = ArchiveStreamFactory.TAR;
        break;
      case ArchiveStreamFactory.ZIP:
        archiverName = ArchiveStreamFactory.ZIP;
        break;
      case ArchiveStreamFactory.SEVEN_Z:
        archiverName = ArchiveStreamFactory.SEVEN_Z;
        break;
      default:
        archiverName = null;
    }

    if (archiverName == null){
      throw new AspireException("Archive type not supported", name);
    }

    try {
      return new ArchiveStreamFactory().createArchiveInputStream(archiverName, compressorInputStream);
    } catch (ArchiveException ex) {
      throw new AspireException("could-not-create-archive-input-stream", ex);
    }
  }
}
