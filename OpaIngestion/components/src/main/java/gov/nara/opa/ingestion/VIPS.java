/**
 * Copyright Search Technologies 2014
 */

package gov.nara.opa.ingestion;

import com.google.common.base.Joiner;
import com.searchtechnologies.aspire.framework.utilities.DateTimeUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.logging.ALogger;
import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 *
 * @author pmartinez
 */
public class VIPS {
    /**
     * @param args the command line arguments
     */
    static final long PROCESS_WAIT = DateTimeUtilities.MINUTES(3);
    static final int WATCHDOG_WAIT = DateTimeUtilities.MINUTES(3.5f);
    
    String sourceImgDirectory;
    String destinationImgDirectory;
    final Component component;
    final Settings settings;
    final ALogger logger;
    
    public Boolean destroyed = false;
    
    public VIPS(Component component) throws AspireException{
      this.component = component;
      this.settings = Components.getSettings(component);
      this.logger = (ALogger)this.component;
    }
    
    public VIPS(Component component, File sourceDirectory, File destinationDirectory) throws AspireException
    {
      this(component);
      sourceImgDirectory = sourceDirectory.toString();
      destinationImgDirectory = destinationDirectory.toString();
        
    }
    
    public void createTiles() throws AspireException
    {
    	createTilesUsingExternalProcess();
    }
    
    private void createTilesUsingExternalProcess() throws AspireException{
      int attempNumber = 0;
        
      CommandLine commandLine = new CommandLine("vips");
      commandLine.addArgument("dzsave");
      commandLine.addArgument(sourceImgDirectory);
      commandLine.addArgument(destinationImgDirectory);

      executeVipsCommand(commandLine);

       if (destroyed){
          logger.warn("Tile process had to be destroyed for file: %s", sourceImgDirectory);
          destroyed = false;
          if(executeProcessAgain())
          {
              if(attempNumber < 3)
              {
                  logger.debug("Executing tile process again");
                  createTiles();
                  attempNumber ++;
              }else{
                  logger.error("The tile process could no be finish for file %s", sourceImgDirectory);
              }
          }
      }
    }
    
    public void createThumbnails(int width, int height) throws AspireException{
    	createThumbnailsUsingExternalProcess(width, height);
    }
    
    private void createThumbnailsUsingExternalProcess(int width, int height) throws AspireException
    {
        int attempNumber = 0;
        
        CommandLine commandLine = new CommandLine("vipsthumbnail");
        commandLine.addArgument(sourceImgDirectory);
        commandLine.addArgument("-o");
        commandLine.addArgument(destinationImgDirectory + "[strip]");
        commandLine.addArgument("-s");
        commandLine.addArgument(Integer.toString(width) + "x" + Integer.toString(height));
          
        executeVipsCommand(commandLine);
        
        if (destroyed){
            logger.warn("Thumbnail process had to be destroyed for file: %s", sourceImgDirectory);
            destroyed = false;
            if(executeProcessAgain())
            {
                if(attempNumber < 3)
                {
                    logger.debug("Executing thumbnail process again");
                    createThumbnails(width, height);
                    attempNumber ++;
                }else{
                    logger.error("The thumbnail process could no be finish for file %s", sourceImgDirectory);
                }
            }
        }        
    }    
    
    public void executeVipsCommand(CommandLine commandLine) throws AspireException
    {          
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        DefaultExecutor executor = new DefaultExecutor();
        
        ExecuteWatchdog watchdog = new ExecuteWatchdog(WATCHDOG_WAIT);
        executor.setWatchdog(watchdog);
        
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);    
        
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        executor.execute(commandLine, resultHandler);
        
        try {
          resultHandler.waitFor(PROCESS_WAIT);
        } catch (InterruptedException ex) {
        }
        
        if (!resultHandler.hasResult()){
          String command = getCommand(commandLine);
          throw new AspireException("process timeout", "Timeout waiting for process '%s'", command);
        }
        
        int exitValue = resultHandler.getExitValue();
        
        if (exitValue != 0){          
          String command = getCommand(commandLine);
          throw new AspireException("process failed", "Process '%s' exited with code %d: %s",
            command, exitValue, outputStream.toString());          
        }
      } catch (IOException ex) { 
        throw new AspireException("executor.execute", ex);
        } 
    }
    
    private String getCommand(CommandLine commandLine){
      return Joiner.on(" ").join(commandLine.toStrings());
    }
    
    private boolean executeProcessAgain() throws AspireException
    {
        boolean executeProcess =  false;
        
        try
        {
            File source = new File(sourceImgDirectory);
            File target = new File(destinationImgDirectory);
           
             if (target.exists())
             {
                 BasicFileAttributes sourceAttributes;
                 sourceAttributes = Files.readAttributes(source.toPath(), BasicFileAttributes.class);
                 FileTime sourceModifiedTime = sourceAttributes.lastModifiedTime();
                 
                 BasicFileAttributes targetAttributes = Files.readAttributes(target.toPath(), BasicFileAttributes.class);
                 FileTime targetModifiedTime = targetAttributes.lastModifiedTime();
                 
                 if(sourceModifiedTime.compareTo(targetModifiedTime) > 0) // source file was created after target file
                 {
                     target.deleteOnExit();
                     executeProcess = true;
                 }
             }
             else{
                 executeProcess = true;
             }
             
        }catch(IOException ex){
            throw new AspireException("Vips.executeProcessAgain", ex);
        }
        
        return executeProcess;
    }
}
