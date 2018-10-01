package gov.nara.opa.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class EncodingInterceptor extends HandlerInterceptorAdapter {
  //private static OpaLogger logger = OpaLogger.getLogger(EncodingInterceptor.class);
  
  private String characterEncoding = "UTF-8";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
          Object handler) throws Exception {
    //logger.debug(String.format("NARA-1790:Interceptor:Character encoding: %1$s", request.getCharacterEncoding()));
    request.setCharacterEncoding(this.characterEncoding);
    return true;
  }

  public void setCharacterEncoding(String characterEncoding) {
      this.characterEncoding = characterEncoding;
  }
}
