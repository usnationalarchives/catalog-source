package gov.nara.opa.architecture.web.valueobject;

import java.util.LinkedHashMap;

import com.searchtechnologies.aspire.services.AspireObject;

/**
 * Classes that implement this interface provide convenience methods for
 * allowing the creation of AspireObjects
 * 
 * @author aolaru
 * @date Jun 6, 2014
 * 
 */
public interface AspireObjectCreator {

  /**
   * Creates a new Aspire object or fetches from cache
   * 
   * @param rootName
   *          The name that will be given to the new AspireObject
   * @param fetchFromCache
   *          If this parameter is true and the AspireObject was created before
   *          (as a result of a prior createAspireObject call, the return value
   *          will be fetched from cache (i.e. a member variable that stores a
   *          reference to the priorly created object)
   * @return The AspireObject
   */
  AspireObject createAspireObject(String rootName, String action,
      boolean fetchFromCache);

  /**
   * To be implemented by all ValueObjects to allow the retrieval of content to
   * be include on AspireObjects when they are created
   * 
   * @return A map of property name/values that the recursion logic in
   *         AspireObjectPopulationHelper will use to create AspireObjects.If a
   *         value is a AspireObjectCreator or a List implementation the
   *         recursion logic will continue the recursion by interrogating
   *         getAspireObjectContent of the AspireObjectCreator or walking
   *         through the objects in the List.
   */
  LinkedHashMap<String, Object> getAspireObjectContent(String action);

}
