'use strict';
opaApp.controller('homePageManagerController', function ($location, $routeParams, $scope, Auth, OpaUtils, ErrorCodesSvc, ModeratorAnnouncementService, ModeratorBackgroundImagesService) {
  $scope.AccountRights = Auth.getAccountRights();
  $scope.Images = [];
  $scope.TAB_VALUES = {
    ANNOUNCEMENT: 'announcement',
    BACKGROUND_IMAGES: 'backgroundImages'
  };
  var imagesService = new ModeratorBackgroundImagesService();

  $scope.getAnnouncement = function () {
    var announcementsService = new ModeratorAnnouncementService();
    announcementsService.$get({},
      function (data) {
        if (data.opaResponse.announcement && data.opaResponse.announcement['@text']) {
          $scope.announcementText = data.opaResponse.announcement['@text'];
          $scope.enableAnnouncement = data.opaResponse.announcement['@enabled'] === 'true';
        }
      }, function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  $scope.saveAnnouncement = function () {
    var announcementsService = new ModeratorAnnouncementService();
    announcementsService.$save({text: $scope.announcementText, enabled: $scope.enableAnnouncement},
      function (data) {},
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse.error && error.data.opaResponse.error.description) {
            OpaUtils.showMessageModal("Unable to save announcement", error.data.opaResponse.error.description);
          }
          else {
            OpaUtils.showMessageModal("Something went wrong...", "Unable to save announcement");
          }
        }
      }
    );
  };

  $scope.clearBackgroundImageForm = function () {
    $scope.ImageParentNaId = '';
    $scope.ImageObjectId = '';
  };

  $scope.addImage = function () {
    if ($scope.ImageParentNaId && $scope.ImageParentNaId) {
      imagesService.$add({
        naId: $scope.ImageParentNaId,
        objectId: $scope.ImageObjectId
      }, function (data) {
        getImages();
        $scope.clearBackgroundImageForm();
      }, function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          if (error.data.opaResponse.error && error.data.opaResponse.error.description) {
            OpaUtils.showMessageModal("Unable to Add background image", error.data.opaResponse.error.description);
          }
          else {
            OpaUtils.showMessageModal("Something went wrong...", "Unable to Add background image");
          }
        }
      });
    }
  };

  $scope.removeImage = function (index) {

    if (index < $scope.Images.length) {
      imagesService.$remove({
        naId: $scope.Images[index]['@naId'],
        objectId: $scope.Images[index]['@objectId']
      }, function (data) {
        getImages();
      }, function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse.error && error.data.opaResponse.error.description) {
            OpaUtils.showMessageModal("Unable to Remove background image", error.data.opaResponse.error.description);
          }
          else {
            OpaUtils.showMessageModal("Something went wrong...", "Unable to Remove background image");
          }
        }
      });
    }
  };

  var getImages = function () {
    var imagesService = new ModeratorBackgroundImagesService();
    imagesService.$get({},
      function (data) {
        var i;
        $scope.Images = [];
        if (data.opaResponse['background-images'] && data.opaResponse['background-images'].images &&
          data.opaResponse['background-images'].images.length) {
          for (i = 0; i < data.opaResponse['background-images'].images.length; i++) {
            $scope.Images.push(data.opaResponse['background-images'].images[i]);
          }
        }
        else if (data.opaResponse['background-image']) {
          $scope.Images.push(data.opaResponse['background-image']);
        }
      },
      function (error) {
        $scope.Images = [];
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (!angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.BACKGROUND_IMAGE_NOT_FOUND)) {
            if (error.data.opaResponse.error && error.data.opaResponse.error.description) {

              OpaUtils.showMessageModal("Unable to load background images", error.data.opaResponse.error.description);
            }
            else {
              OpaUtils.showMessageModal("Something went wrong...", "Unable to load background images");
            }
          }
        }
      });
  };

  $scope.setTab = function (tab) {
    if (tab && tab === $scope.TAB_VALUES.BACKGROUND_IMAGES) {
      $scope.tabType = $scope.TAB_VALUES.BACKGROUND_IMAGES;
      getImages();
    }
    else {
      $scope.tabType = $scope.TAB_VALUES.ANNOUNCEMENT;
      $scope.getAnnouncement();
    }
    $location.search('tabType', $scope.tabType);
  };

  //init the controller
  (function () {
    $scope.tabType = $routeParams.tabType ? $routeParams.tabType : $scope.TAB_VALUES.ANNOUNCEMENT;

    if ($scope.AccountRights === 'accountAdminMod' ||
      $scope.AccountRights === 'accountAdmin' || $scope.AccountRights === 'moderator') {

      if ($scope.tabType === 'announcement') {
        $scope.getAnnouncement();
      }
      else {
        getImages();
      }
    }
  })();
});

