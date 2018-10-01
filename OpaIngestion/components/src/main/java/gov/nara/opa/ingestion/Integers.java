package gov.nara.opa.ingestion;

/**
 *
 * @author caraya
 */
public class Integers {
  public static boolean tryParse(String value){
    try{  
        Integer.parseInt(value);  
        return true;  
     } catch(NumberFormatException nfe){  
         return false;  
     }  
  }
}
