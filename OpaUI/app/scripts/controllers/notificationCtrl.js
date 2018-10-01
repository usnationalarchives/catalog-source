'use strict';
opaApp.controller('notificationCtrl', function ($location, $scope, $log, OpaUtils, Account, Auth) {

  $scope.Account = Account;
  $scope.OpaUtils = OpaUtils;
  $scope.notifications = 0; //UI bound variable
  var allNotificationsArray = 0;
  var newNotifications = 0;

  var numberOfNotifications = 0; //Get all notifications

  $scope.getNotificationMoment = function (dateUtc) {
    if (dateUtc) {
      return OpaUtils.fancyDate(dateUtc);
    }
  };

  $scope.getNotificationAction = function (type, appliedTo) {
    var action = "";
    var element = "";

    switch (appliedTo){
      case 'tags':
        element = "Tag";
        break;
      case 'transcriptions':
        element = "Your Transcription";
        break;
      case 'comments':
        element = "Your Comment";
        break;
    }

    switch (type) {
      case "ADD":
        action = "Added ";
        break;
      case "UPDATE":
        action = "Edited ";
        break;
      case "REMOVE":
        action = "Removed ";
        break;
      case "DELETE":
        action = "Deleted ";
        break;
      case "RESTORE":
        action = "Restored ";
        break;
      case "REPLY":
        action = "Replied ";
        break;
      default:
        return type;
    }
    return action + element;
  };

  $scope.navigateToAccount = function () {
    $location.path("/accounts/" + Auth.userName());
  };

  //Get exports summary
  //TODO: move to service and update bindings
  /**
   * Gets the notifications of the current user
   * @param numberOfNotifications Number of notifications to return, if numberOfNotifications = 0,
   *                              then it retrieved all the notifications.
   */
  var getNotifications = function (numberOfNotifications) {
    var account = new Account();
    var params = {};

    if (numberOfNotifications) {
      params = {rows: numberOfNotifications};
    }

    account.$getNotifications(params,
      function (data) {
        if (data.opaResponse) {
          newNotifications = data.opaResponse.notifications.totalNew;
          allNotificationsArray = data.opaResponse.notifications.notification;

          if (allNotificationsArray && newNotifications && allNotificationsArray.length > 0) {
            $scope.notifications = allNotificationsArray.slice(0, newNotifications);
            OpaUtils.showSuccessGlobalNotification(newNotifications + " new notifications");
          }

          //Clears the new notifications, because they were fetched
          if (allNotificationsArray && newNotifications) {
            account.$deleteNotifications({},
              function (data) {},
              function (error) {
                if (!OpaUtils.checkForAPIError(error)) {
                  //check for other errors block
                  if (error.status !== OpaUtils.NOT_FOUND) {
                    $log.error(error.description);
                  }
                }
              }
            );
          }
        }
      },
      function (error) {
        $scope.notifications = 0;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.status === OpaUtils.NOT_FOUND) {
            OpaUtils.showSuccessGlobalNotification("You don't have new notifications");
          }
        }
      }
    );
  };


  $scope.getNotificationUser = function (event) {
    var userName = '';
    if (event) {
      if (event['@isNaraStaff'] === 'true' || event['@displayNameFlag'] === 'true') {
        userName = event['@otherFullName'];
        if (event['@isNaraStaff'] === 'true') {
          userName += " (NARA Staff)";
        }
      } else {
        userName = event['@otherUser'];
      }
    }
    return {user: userName, isNaraStaff: event['@isNaraStaff'] === 'true'};
  };

  $scope.getAllNotifications = function () {
    $scope.showAllNotifications = true;
    $scope.notifications = allNotificationsArray.slice(0);
  };

  $scope.clearNotifications = function () {
    $scope.notifications = 0;
    $scope.showAllNotifications = false;
  };

  $scope.getUrl = function (naId, pageNumber, appliedTo) {
    var contribution = appliedTo === 'tags' || appliedTo === 'tag' ? "tag" : "transcription";
    var url = '/id/' + naId;
    if (parseInt(pageNumber)) {
      url += '/' + pageNumber + '/public?contributionType=' + contribution;
    }
    return url;
  };

  $scope.getImage = function (pageNumber, totalPages) {
    if (pageNumber !== "0"){
      return pageNumber + "/" + totalPages;
    } else {
      return "Description";
    }
  };

  var init = function () {
    getNotifications(numberOfNotifications);
  };

  init();

});
