package gov.nara.opa.api.services.impl.user.lists;

import gov.nara.opa.api.dataaccess.user.lists.UserListDao;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.lists.AddToUserListService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AddToUserListServiceImpl implements AddToUserListService {
  private static OpaLogger logger = OpaLogger
      .getLogger(AddToUserListServiceImpl.class);

  SecureRandom random;

  @Autowired
  private UserListDao userListDao;

  @Autowired
  private ViewUserListService viewUserListService;

  @Autowired
  private ConfigurationService configurationService;

  public UserListItem getListItem(int listId, String opaId) {

    try {
      // Check for duplicate list
      List<UserListItem> dupList;
      dupList = userListDao.selectListItem(listId, opaId);
      if (dupList.size() > 0) {
        return dupList.get(0);
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return null;
  }

  @Override
  public List<String> getOpaIdsToAddToList(int listId, String[] opaIds) {
    List<String> opaIdsToAddToList = new ArrayList<String>();

    try {
      opaIdsToAddToList = userListDao.checkListForOpaIds(listId, opaIds);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return opaIdsToAddToList;
  }

  @Override
  public List<UserListItem> getNonDuplicateListOpaIds(int listId,
      String[] opaIds) {
    List<UserListItem> nonDuplicateListOpaIds = new ArrayList<UserListItem>();

    try {
      nonDuplicateListOpaIds = userListDao.checkListForDuplicateOpaIds(listId,
          opaIds);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return nonDuplicateListOpaIds;
  }

  @Override
  public int batchAddOpaIdsToList(int listId, List<String> opaIdsToAddToList) {
    int toalIdsAddedToList = 0;
    try {
      toalIdsAddedToList = userListDao.batchAddToUserList(listId,
          opaIdsToAddToList);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return toalIdsAddedToList;
  }

}
