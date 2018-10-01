package gov.nara.opa.api.validation.common;

import gov.nara.opa.api.validation.common.propertyeditor.OpaArrayListPropertyEditor;

import java.util.ArrayList;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalBindingInitializer {
  @InitBinder
  public void registerCustomEditors(WebDataBinder binder, WebRequest request) {
    binder.registerCustomEditor(ArrayList.class,
        new OpaArrayListPropertyEditor());
  }
}