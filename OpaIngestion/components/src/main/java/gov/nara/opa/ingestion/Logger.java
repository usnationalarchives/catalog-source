package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.logging.ALogger.Severity;
import java.util.concurrent.LinkedBlockingQueue;
import org.w3c.dom.Element;


public class Logger extends ComponentImpl{
  private LinkedBlockingQueue<Message> messages;
  private Thread loggingThread;
  private volatile boolean isClosed;
  
  private class Message{
    private final Severity severity;
    private Throwable cause;
    private final String format;
    private final Object[] args;

    private Message(Severity severity, String format, Object[] args) {
      this.severity = severity;
      this.format = format;
      this.args = args;
    }

    private Message(Severity severity, Throwable cause, String format, Object[] args) {
      this(severity, format, args);
      this.cause = cause;
    } 
  }
  
  @Override
  public void initialize(Element config) throws AspireException {
    messages = new LinkedBlockingQueue<>();
    startLoggingThread();
  }

  @Override
  public void close() {
    isClosed = true;
  }

  private void startLoggingThread() {
    loggingThread = new Thread(
      new Runnable(){
        @Override
        public void run() {          
          while(!isClosed){            
            try {
              Message message = messages.take();
              log(message);
            } catch (InterruptedException ex) {
            }
          }
        }        
      });
    loggingThread.start();
  }
  
  private void log(Message message){
    log(message.severity, message.cause, message.format, message.args);
  }
  
  @Override
  public void info(String format, Object ... args){
    messages.offer(new Message(Severity.INFO, format, args));
  }

  @Override
  public void warn(String format, Object ... args){
    messages.offer(new Message(Severity.WARN, format, args));
  }
  
  @Override
  public void error(Throwable cause, String format, Object ... args){
    messages.offer(new Message(Severity.ERROR, cause, format, args));
  }
}
