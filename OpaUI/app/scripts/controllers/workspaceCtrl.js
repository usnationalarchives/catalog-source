opaApp.controller('workspaceCtrl', function ($scope, $routeParams, $window, $timeout, $modal, $location, $filter, Auth, TagService, Transcription, /*Translation, Languages,*/ visorSvc, ErrorCodesSvc, configServices, OpaUtils, LoginService, shareSvc, commentsService) {
  // UI BINDINGS
  var updateTranscriptionObjectInfo;
  $scope.tagTextWorkspace = "";
  $scope.tags = [];
  $scope.tagsCount = 0;
  var tagsLastModified = 0;
  $scope.commentsCount = 0;
  $scope.contributors = [];
  $scope.transcription = {};
  $scope.transcription.text = "";
  $scope.transcription.lockedTooltip = null;
  $scope.transcription.tooltipPosition = "center";
  var transcriptionsLastModified = 0;
  // $scope.translationText = "";
  //$scope.selectedTranslationLanguage = "";

  $scope.comments = [];
  var commentsLastModified = 0;
  $scope.lastModified = "";
  $scope.fancyLastModified = "";
  $scope.visorSvc = visorSvc;
  $scope.title = "";
  $scope.lastSaveDate = "";
  $scope.Auth = Auth;
  $scope.isWorkspace = true;
  $scope.LoginService = LoginService;
  $scope.loginOut = false;
  var initAlreadyCalled = false;

  //Global variables
  $scope.showLoginScreen = false;
  $scope.transcriptionLocked = false;
  $scope.transcriptionLockedByMe = false;
  // $scope.translationEditModeOn = false;
  $scope.lastSavedTranscriptionText = "";
  // $scope.lastSavedTranslationText = "";
  $scope.timerInactivityAdvise = null;
  $scope.timerInactivity = null;
  $scope.currentObjectId = null;
  $scope.showAllContributors = false;
  $scope.showAddCommentScreen = true;
  $scope.naId = $routeParams.naId;

  $scope.commentsService = commentsService;

  //Show the login screen panel
  $scope.showLogin = function () {
    $scope.showLoginScreen = true;
    $timeout(function() {
      document.getElementById("InputUsername").focus();
      document.getElementById("cancelLogin").focus();
    }, 0, false);
  };

  //Hide the login screen panel
  $scope.hideLogin = function () {
    $scope.showLoginScreen = false;
  };

  //Set the current selected tab
  $scope.setTab = function (index, query) {
    visorSvc.currentTab = index;
    $scope.lastSaveDate = "";
    $location.search('contributionType', query).path($location.path(), false);

    updateThis();
    watchTranscriptionTooltip();
  };

  $scope.logoutw = function () {
    $scope.loginOut = true;
    //Check if the user is editing the transcription and ask if wants to saves
    if (!angular.equals($filter('removeWordCharacters')($scope.transcription.text), $scope.lastSavedTranscriptionText)) {
      $scope.showUnsavedTranscriptionModificationsDialog(null, null, false);
    }
    else if ($scope.transcriptionLockedByMe) {
      $scope.unlockTranscription();
    }
    else {
      LoginService.logout();
      $scope.hideLogin();
    }
  };

  $scope.createTooltips = function () {
    $timeout(function () {
      $('#lastModified').kendoTooltip({
        autoHide: true,
        position: "bottom",
        content: "<div>" + $scope.lastModified + "</div>"
      });
    }, 1500);

    //
  };

  // --------------  TAGS ----------------------
  //This function add a new tag for a given object
  $scope.addTagWorkspace = function () {

    if (!$scope.tagTextWorkspace.trim()) {
      OpaUtils.showErrorGlobalNotification("Please enter text for the tag");
    }
    else {
      TagService.addTagObject($routeParams.naId, $scope.currentObjectId, $scope.tagTextWorkspace, visorSvc.index + 1).then(
        function () {
          $scope.tagTextWorkspace = "";
          $scope.getAllTagsObjectWorkspace();
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
          }
        }
      );
    }
  };

  //This function delete a user tag for a given object
  $scope.deleteTagWorkspace = function (text, tooltipId) {
    TagService.deleteTagObject($routeParams.naId, $scope.currentObjectId, text).then(
      function () {
        $("#" + tooltipId).data("kendoTooltip").destroy();
        $scope.getAllTagsObjectWorkspace();
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
        }
      }
    );
  };


  //This function get all the tags for a given object id. The current object id.
  $scope.getAllTagsObjectWorkspace = function () {
    TagService.getAllTagsObject($routeParams.naId, $scope.currentObjectId).then(
      function (promise) {
        //Patch to check if server sent an array or a single object.
        $scope.tags = angular.isArray(promise.opaResponse.tags.tag) ? promise.opaResponse.tags.tag : [promise.opaResponse.tags.tag];
        tagsLastModified = new Date(promise.opaResponse.tags['@lastModified']);
        $scope.tagsCount = parseInt(promise.opaResponse.tags['@total']);
        TagService.createTooltips($scope.tags, "tagw");
        if (visorSvc.orderTabsByDate) {
          changeTab();
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse && angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.NO_TAGS_EXIST)) {
            $scope.tags = [];
            $scope.tagsCount = 0;
            tagsLastModified = -1;
            if (visorSvc.orderTabsByDate) {
              changeTab();
            }
          }
          else if (error.data.opaResponse) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
          }
        }
      }
    );
  };

  $scope.checkUserTagWorkspace = function (tag) {
    return TagService.checkUserTag(tag) && Auth.isLoggedIn();
  };


  // ------------- COMMENTS ----------------------------
  $scope.showAddComment = function () {
    $scope.showAddCommentScreen = false;
  };

  $scope.getAllCommentsObjectWorkspace = function () {

    commentsService.workspaceCommentsCount = 0;

    commentsService.getCommentsForObject($routeParams.naId, $scope.currentObjectId).then(
      function (data) {
        $scope.comments = data.opaResponse.comments;
        if ($scope.comments.comment && $scope.comments.comment.length > 0) {
          $scope.showAddCommentScreen = false;
          commentsService.workspaceCommentsCount = parseInt($scope.comments['@total']) + parseInt($scope.comments['@replies']);
          commentsLastModified = new Date($scope.comments['@lastModified']);
          if (visorSvc.orderTabsByDate) {
            changeTab();
          }
        }
        else {
          $scope.showAddCommentScreen = true;
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse && angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.COMMENT_NOT_FOUND)) {
            $scope.comments = [];
            $scope.showAddCommentScreen = true;
            $scope.commentsCount = 0;
            commentsLastModified = -1;
            if (visorSvc.orderTabsByDate) {
              changeTab();
            }

          }
          else if (error.data.opaResponse) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
          }
        }
      });
  };

  $scope.addComment = function () {
    if($scope.commentText) {
      commentsService.addObjectComment($routeParams.naId, $scope.currentObjectId, $scope.commentText, visorSvc.index + 1).then(
        function () {
          $scope.getAllCommentsObjectWorkspace();
          $scope.commentText = '';
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
          }
        }
      );
    }
    else{
      OpaUtils.showErrorGlobalNotification("Please enter a valid comment");
    }
  };

  $scope.refreshComments = function () {
    $scope.getAllCommentsObjectWorkspace();
  };

  $scope.run = false;


  $scope.$on('ngRepeatFinished', function () {
    if (!$scope.run) {
      if ($scope.currentCommentId) {
        var divId = 'c' + $scope.currentCommentId;
        //Check if URL is a persistent comment link
        var x = document.getElementById(divId);
        x.scrollIntoView({block: "start", behavior: "smooth"});
        $('#' + divId).animate({backgroundColor: '#FFFFFF'});
        $scope.run = true;
      }
    }
  });

  /**
   * This function checks if the current link is a reply sharing link on content detail, which by default hides replies.
   * This function make the reply visible.
   * @param comment
   * @returns {boolean}
   */

  $scope.checkSharing = function (comment) {
    if ($scope.currentCommentId === comment['@id']) {
      comment.hide = false;
      return false;
    }
    return true;
  };


  //---------------- Translations ----------------------

  /* $scope.editTranslation = function () {
   $scope.lockTranslation();
   $scope.startTranscriptionInactivityTimerCountDown();
   };

   $scope.cancelEditionTranslation = function () {
   if (!angular.equals($filter('removeWordCharacters')($scope.translationText), $scope.lastSavedTranslationText)) {
   $scope.showUnsavedTranslationModificationsDialog();
   }
   else {
   $scope.unlockTranslation();
   }

   $scope.lastSaveDate = "";
   $scope.cancelInactivityTimerCountDown();
   };

   $scope.getTranslation = function () {
   var translation = new Translation({'naid': $routeParams.naId, 'objectId': $scope.currentObjectId});
   var params = {};

   translation.$get(params,
   function (data) {
   $scope.contributors = $scope.processContributors(data.opaResponse.translation.users.user);
   $scope.translationText = data.opaResponse.translation.text;
   $scope.lastSavedTranslationText = data.opaResponse.translation.text;
   $scope.fancyLastModified = OpaUtils.fancyDate(data.opaResponse.translation['@lastModified']);
   $scope.lastModified = OpaUtils.fancyDateCompleteFormat(data.opaResponse.translation['@lastModified']);
   $scope.checkIfTranslationLocked(data.opaResponse.translation);
   $scope.createTooltips();
   },
   function (error) {
   $scope.contributors = [];
   $scope.translationText = "";
   $scope.lastSavedTranslationText = "";
   $scope.fancyLastModified = "";
   $scope.lastModified = "";
   if (!OpaUtils.checkForAPIError(error)) {
   //check for other errors block
   if (error.data.opaResponse)
   $scope.checkIfTranslationLocked(error.data.opaResponse.translation);
   }
   $scope.createTooltips();
   });
   };

   $scope.lockTranslation = function () {
   var translation = new Translation({'naid': $routeParams.naId, 'objectId': $scope.currentObjectId});
   var params = {'action': 'lock'};

   translation.$action(params,
   function () {
   $scope.translationEditModeOn = true;
   $scope.loginOut = false;
   },
   function (error) {
   if (!OpaUtils.checkForAPIError(error)) {
   //check for other errors block
   if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.LOCKED_BY_ANOTHER)) {
   OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
   $scope.getTranslation();
   }
   }
   });
   };

   */
  /**
   * Unlock the translation
   * @param objId
   * @param closeModal Flag to close the modal after unlock the translation
   */
  /*
   $scope.unlockTranslation = function (objId, closeModal) {
   var objectId = null;
   if (objId) {
   objectId = objId;
   }
   else {
   objectId = $scope.currentObjectId;
   }
   var translation = new Translation({'naid': $routeParams.naId, 'objectId': objectId});
   var params = {'action': 'unlock'};

   translation.$action(params,
   function (data) {
   $scope.translationEditModeOn = false;

   if ($scope.loginOut) {
   LoginService.logout();
   }

   if (closeModal)
   $scope.closeModals();
   },
   function (error) {
   if (!OpaUtils.checkForAPIError(error)) {
   //check for other errors block
   }
   });
   };

   $scope.saveTranslation = function () {

   if (!angular.equals($filter('removeWordCharacters')($scope.translationText), $scope.lastSavedTranslationText)) {

   var translation = new Translation({'naid': $routeParams.naId, 'objectId': $scope.currentObjectId});
   var params = {
   'action': 'saveAndRelock',
   'text': $filter('removeWordCharacters')($scope.translationText),
   'languageCode': $scope.selectedTranslationLanguage.code,
   'pageNum': visorSvc.index + 1
   };

   translation.$action(params,
   function () {
   $scope.lastSavedTranslationText = $filter('removeWordCharacters')($scope.translationText);
   $scope.cancelInactivityTimerCountDown();
   $scope.startTranslationInactivityTimerCountDown();
   $scope.lastSaveDate = OpaUtils.fancyDateCompleteFormat(new Date());
   OpaUtils.showSuccessGlobalNotification("Your work has been saved");

   },
   function (error) {
   if (!OpaUtils.checkForAPIError(error)) {
   //check for other errors block
   try{
   OpaUtils.showErrorGlobalNotification(error.data.opaResponse.error.description);
   }
   catch (exception) {
   OpaUtils.showErrorGlobalNotification("There was an error saving your translation");
   }
   }
   }
   );
   }
   };

   */
  /**
   * Save and unlock the translation.
   * @param transText Used when there is a transition and the user wants to save the previous translation
   * @param objId Used when there is a transition and the user wants to save the previous translation
   * @param closeModal Flag to close the modal after unlock the translation
   */
  /*
   $scope.saveUnlockTranslation = function (transText, objId, closeModal) {
   var objectId = null;
   var translationText = "";

   if (objId) {
   objectId = objId;
   }
   else {
   objectId = $scope.currentObjectId;
   }

   if (transText) {
   translationText = transText;
   }
   else {
   translationText = $scope.translationText;
   }

   var translation = new Translation({'naid': $routeParams.naId, 'objectId': objectId});
   var params = {
   'action': 'saveAndUnlock',
   'text': $filter('removeWordCharacters')(translationText),
   'pageNum': visorSvc.index + 1
   };

   translation.$action(params,
   function (data) {
   $scope.lastSavedTranslationText = $filter('removeWordCharacters')($scope.translationText);
   $scope.translationEditModeOn = false;
   $scope.lastSaveDate = "";

   if ($scope.loginOut) {
   LoginService.logout();
   }

   if (closeModal) {
   $scope.closeModals();
   }
   },
   function (error) {
   if (!OpaUtils.checkForAPIError(error)) {
   //check for other errors block
   OpaUtils.showErrorGlobalNotification(error.data.opaResponse.error.description);
   }
   }
   );
   };

   //This function checks if the current translation is locked.
   // If it is lock, the edit button is set to disable and a tooltip is created.
   $scope.checkIfTranslationLocked = function (translation) {
   if (angular.equals(translation.isLocked, "true")) {

   if (!angular.equals(translation.lockedBy['@id'], Auth.userName())) {
   $('#editButton').attr('disabled', 'disabled');

   $timeout(function () {
   var username = translation.lockedBy['@id'];
   if (translation.lockedBy['@isNaraStaff'] === 'true') {
   username = translation.lockedBy['@fullName'] + " (NARA Staff)";
   }

   $('#editArea').kendoTooltip({
   autoHide: true,
   position: "top",
   content: "<div>Locked by user <a href='#/accounts/" + translation.lockedBy['@id'] +
   "/contributions?contributionType=translations' target='_blank'>" + username + "</a> for editing</div>"
   });
   }, 500);
   }
   else {
   $('#editButton').removeAttr('disabled');
   $scope.translationEditModeOn = true;
   }
   }
   else {
   $('#editButton').removeAttr('disabled');

   if ($('#editArea').data("kendoTooltip")) {
   $('#editArea').data("kendoTooltip").destroy();
   }
   }
   };

   //This function start de timer for the inactivity timer countdown.
   //The amount of time is set in the config.js
   //5 minutes after the timer finish an advise is shown to the user.
   $scope.startTranslationInactivityTimerCountDown = function () {

   $scope.timerInactivityAdvise = $timeout(function () {
   var tooltip = $('#actionButtonsTranslation').kendoTooltip({
   position: "top",
   content: "<div>The lock for editing will be released in 5 minutes <br /> due to your inactivity.<strong> Please save your changes.</strong></div>"
   }).data("kendoTooltip");

   tooltip.show($("#actionButtonsTranslation"));
   }, configServices.TIMEOUT_TRANSCRIPTION - 300000);

   $scope.timerInactivity = $timeout(function () {
   $("#actionButtonsTranslation").data("kendoTooltip").destroy();
   $scope.unlockTranslation();
   $scope.getTranslation();
   }, configServices.TIMEOUT_TRANSCRIPTION);
   };

   $scope.showUnsavedTranslationModificationsDialog = function (translationText, objId) {

   var message = "There are unsaved translation modifications. Do you want to save them now?";
   var title = "Alert";

   var modal = $modal.open({
   templateUrl: 'views/directives/dialogs/yesnomessagedialog.html',
   controller: ['$scope', '$modalInstance', 'title', 'message', function ($scope, $modalInstance, title, message) {

   $scope.title = title;
   $scope.message = message;

   $scope.yes = function () {
   $modalInstance.close();
   };

   $scope.no = function () {
   $modalInstance.dismiss();
   };
   }],
   size: 'sm',
   resolve: {
   title: function () {
   return title;
   },
   message: function () {
   return message;
   }
   }
   });

   modal.result.then(function () {
   $scope.saveUnlockTranslation(translationText, objId, true);
   }, function () {
   $scope.unlockTranslation(objId, true);
   });
   };

   $scope.$watch('translationEditModeOn', function (newValue, oldValue) {
   if (newValue === false && oldValue === true) {
   $scope.getTranslation();
   }
   });

   var updateTranslationObjectInfo = function () {
   //Check changes before move to another object
   if ($scope.translationEditModeOn) {
   if (!angular.equals($filter('removeWordCharacters')($scope.translationText), $scope.lastSavedTranslationText)) {
   $scope.showUnsavedTranslationModificationsDialog($scope.translationText, $scope.currentObjectId);
   }
   else {
   $scope.unlockTranslation();
   }

   $scope.translationEditModeOn = false;
   }

   $scope.lastSaveDate = "";
   $scope.currentObjectId = visorSvc.objectId;
   $scope.getAllTagsObjectWorkspace();
   $scope.getAllCommentsObjectWorkspace();
   $scope.getTranslation();
   };*/

  // --------------  TRANSCRIPTION ----------------------

  $scope.editTranscription = function () {
    if (!$scope.transcriptionLockedByMe) {
      $scope.transcriptionLockedByMe = true;
      $scope.lockTranscription();
      $scope.startTranscriptionInactivityTimerCountDown();
    }
  };

  $scope.cancelEditionTranscription = function () {
    if (!angular.equals($filter('removeWordCharacters')($scope.transcription.text), $scope.lastSavedTranscriptionText)) {
      $scope.showUnsavedTranscriptionModificationsDialog(null, null, false);
    }
    else {
      $scope.unlockTranscription();
    }

    $scope.lastSaveDate = "";
    $scope.cancelInactivityTimerCountDown();
  };

  $scope.getTranscription = function () {
    var transcription = new Transcription({'naid': $routeParams.naId, 'objectId': $scope.currentObjectId});
    var params = {};

    $scope.transcription.lockedTooltip = null;
    transcription.$get(params,
      function (data) {
        $scope.contributors = $scope.processContributors(data.opaResponse.transcription.users.user);
        $scope.transcription.text = data.opaResponse.transcription.text;
        $scope.lastSavedTranscriptionText = data.opaResponse.transcription.text;
        $scope.fancyLastModified = OpaUtils.fancyDate(data.opaResponse.transcription['@lastModified']);
        $scope.lastModified = OpaUtils.fancyDateCompleteFormat(data.opaResponse.transcription['@lastModified']);
        transcriptionsLastModified = new Date(data.opaResponse.transcription['@lastModified']);
        $scope.checkIfTranscriptionLocked(data.opaResponse.transcription);
        $scope.createTooltips();
        if (visorSvc.orderTabsByDate) {
          changeTab();
        }
      },
      function (error) {
        $scope.contributors = [];
        $scope.transcription.text = "";
        $scope.lastSavedTranscriptionText = "";
        $scope.fancyLastModified = "";
        $scope.lastModified = "";
        transcriptionsLastModified = -1;
        if (visorSvc.orderTabsByDate) {
          changeTab();
        }
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse) {
            $scope.checkIfTranscriptionLocked(error.data.opaResponse.transcription);
          }
        }
        $scope.createTooltips();
      });
  };

  $scope.lockTranscription = function () {
    var transcription = new Transcription({'naid': $routeParams.naId, 'objectId': $scope.currentObjectId});
    var params = {'action': 'lock'};

    transcription.$action(params,
      function () {
        $scope.transcriptionLocked = true;
        $scope.transcriptionLockedByMe = true;
        $scope.loginOut = false;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          $scope.transcriptionLocked = false;
          $scope.transcriptionLockedByMe = false;
          //check for other errors block
          if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.LOCKED_BY_ANOTHER)) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            $scope.getTranscription();
          }
        }
      });
  };

  /**
   * Unlock the transcription
   * @param objId
   * @param closeModal Flag to close the modal after unlock the transcription
   */
  $scope.unlockTranscription = function (objId, closeModal) {
    var objectId = null;
    if (objId) {
      objectId = objId;
    }
    else {
      objectId = $scope.currentObjectId;
    }
    var transcription = new Transcription({'naid': $routeParams.naId, 'objectId': objectId});
    var params = {'action': 'unlock'};

    transcription.$action(params,
      function (data) {
        $scope.transcriptionLocked = false;
        $scope.transcriptionLockedByMe = false;

        if ($scope.loginOut) {
          LoginService.logout();
        }

        if (closeModal) {
          $scope.closeModals();
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showErrorGlobalNotification("Cannot unlock object");
          if (closeModal) {
            $scope.closeModals();
          }
        }
      });
  };

  $scope.saveTranscription = function () {

    if (!angular.equals($filter('removeWordCharacters')($scope.transcription.text), $scope.lastSavedTranscriptionText)) {

      var transcription = new Transcription({
        'naid': $routeParams.naId,
        'objectId': $scope.currentObjectId,
        'text': $filter('removeWordCharacters')($scope.transcription.text)
      });
      var params = {
        'action': 'saveAndRelock',
        'pageNum': visorSvc.index + 1
      };

      transcription.$action(params,
        function () {
          $scope.lastSavedTranscriptionText = $filter('removeWordCharacters')($scope.transcription.text);
          $scope.cancelInactivityTimerCountDown();
          $scope.startTranscriptionInactivityTimerCountDown();
          $scope.lastSaveDate = OpaUtils.fancyDateCompleteFormat(new Date());
          OpaUtils.showSuccessGlobalNotification("Your work has been saved");

        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            try {
              OpaUtils.showErrorGlobalNotification(error.data.opaResponse.error.description);
            }
            catch (exception) {
              OpaUtils.showErrorGlobalNotification("There was an error saving your transcription");
            }
          }
        }
      );
    }
  };

  /**
   * Save and unlock the transcription.
   * @param transText Used when there is a transition and the user wants to save the previous transcription
   * @param objId Used when there is a transition and the user wants to save the previous transcription
   * @param closeModal Flag to close the modal after unlock the transcription
   */
  $scope.saveUnlockTranscription = function (transText, objId, closeModal) {
    var objectId = null;
    var transcriptionText = "";

    if (objId) {
      objectId = objId;
    }
    else {
      objectId = $scope.currentObjectId;
    }

    if (transText) {
      transcriptionText = transText;
    }
    else {
      transcriptionText = $scope.transcription.text;
    }

    var transcription = new Transcription({
      'naid': $routeParams.naId,
      'objectId': objectId,
      'text': $filter('removeWordCharacters')(transcriptionText)
    });
    var params = {
      'action': 'saveAndUnlock',
      'pageNum': visorSvc.index + 1
    };

    transcription.$action(params,
      function (data) {
        $scope.lastSavedTranscriptionText = $filter('removeWordCharacters')($scope.transcription.text);
        $scope.transcriptionLocked = false;
        $scope.transcriptionLockedByMe = false;
        $scope.lastSaveDate = "";

        if ($scope.loginOut) {
          LoginService.logout();
        }

        if (closeModal) {
          $scope.closeModals();
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          OpaUtils.showErrorGlobalNotification(error.data.opaResponse.error.description);
          OpaUtils.showErrorGlobalNotification("Your work could not be saved");
          if (closeModal) {
            $scope.closeModals();
          }
        }
      }
    );
  };


  //This function reads the authors list and creates a single array with the user names or full names for NARA staff people
  $scope.processContributors = function (contributors) {

    var arrayTemp = [];
    angular.forEach(contributors, function (contributor, index) {
      var objectTemp = {};
      var name = "";
      if (contributor['@isNaraStaff'] === "true") {
        name = contributor['@fullName'] + " (NARA Staff)";
      }
      else if (contributor['@displayFullName'] === "true") {
        name = contributor['@fullName'];
      }
      else {
        name = contributor['@id'];
      }

      objectTemp.name = name;
      objectTemp.username = contributor['@id'];
      objectTemp.lastModified = contributor['@lastModified'];

      arrayTemp.push(objectTemp);
    });

    return arrayTemp;
  };

  //This function checks if the current transcription is locked.
  // If it is lock, the edit button is set to disable and a tooltip is created.
  $scope.checkIfTranscriptionLocked = function (transcription) {
    if (angular.equals(transcription.isLocked, "true")) {
      $scope.transcriptionLocked = true;

      if (!angular.equals(transcription.lockedBy['@id'], Auth.userName())) {

        $scope.transcriptionLockedByMe = false;
        var username = transcription.lockedBy['@id'];
        if (transcription.lockedBy['@isNaraStaff'] === 'true') {
          username = transcription.lockedBy['@fullName'] + " (NARA Staff)";
        }
        $scope.transcription.lockedTooltip = "<div>Locked by user <a href='#/accounts/" + transcription.lockedBy['@id'] +
          "/contributions?contributionType=transcriptions' target='_blank'>" + username + "</a> for editing</div>";
        if (!$("#transcription-locked-tooltip").data("kendoTooltip"))
        var tooltip = $('#transcription-locked-tooltip').kendoTooltip({
          position: $scope.transcription.tooltipPosition,
          content: $scope.transcription.lockedTooltip
        }).data("kendoTooltip");
        $("#transcription-locked-tooltip").data("kendoTooltip").options.content = $scope.transcription.lockedTooltip;
        $("#transcription-locked-tooltip").data("kendoTooltip").refresh();
      }
      else {
        $scope.transcriptionLockedByMe = true;
      }
    }
    else {
      $scope.transcription.lockedTooltip = null;
      $scope.transcriptionLocked = false;
      $scope.transcriptionLockedByMe = false;
      if ($("#transcription-locked-tooltip").data("kendoTooltip")) {
        $("#transcription-locked-tooltip").data("kendoTooltip").destroy();
      }
    }
  };

  //This function start de timer for the inactivity timer countdown.
  //The amount of time is set in the config.js
  //5 minutes after the timer finish an advise is shown to the user.
  $scope.startTranscriptionInactivityTimerCountDown = function () {

    $scope.timerInactivityAdvise = $timeout(function () {
      var tooltip = $('#actionButtonsTranscription').kendoTooltip({
        position: "top",
        content: "<div>The lock for editing will be released in 5 minutes <br /> due to your inactivity.<strong> Please save your changes.</strong></div>"
      }).data("kendoTooltip");

      tooltip.show($("#actionButtonsTranscription"));
    }, configServices.TIMEOUT_TRANSCRIPTION - 300000);

    $scope.timerInactivity = $timeout(function () {
      $("#actionButtonsTranscription").data("kendoTooltip").destroy();
      $scope.unlockTranscription();
      $scope.getTranscription();
    }, configServices.TIMEOUT_TRANSCRIPTION);
  };

  //This function is call every time the user saves the transcription.
  //Cancels the timers and destroys the tooltip in case it exist.
  $scope.cancelInactivityTimerCountDown = function () {
    $timeout.cancel($scope.timerInactivityAdvise);
    $timeout.cancel($scope.timerInactivity);
    if ($("#saveArea").data("kendoTooltip")) {
      $("#saveArea").data("kendoTooltip").destroy();
    }
  };

  $scope.showUnsavedTranscriptionModificationsDialog = function (transcriptionText, objId, closeWorkspace) {

    var message = "There are unsaved transcription modifications. Do you want to save them now?";
    var title = "Alert";

    var modal = $modal.open({
      templateUrl: 'views/directives/dialogs/yesnomessagedialog.html',
      controller: ['$scope', '$modalInstance', 'title', 'message', function ($scope, $modalInstance, title, message) {

        $scope.title = title;
        $scope.message = message;

        $scope.yes = function () {
          $modalInstance.close();
        };

        $scope.no = function () {
          $modalInstance.dismiss();
        };
      }],
      size: 'sm',
      resolve: {
        title: function () {
          return title;
        },
        message: function () {
          return message;
        }
      }
    });

    modal.result.then(function () {
      $scope.saveUnlockTranscription(transcriptionText, objId, closeWorkspace);
    }, function () {
      $scope.unlockTranscription(objId, closeWorkspace);
    });
  };

  // Check when the results per page is changed and request a new set of results
  $scope.$watch('transcriptionLocked', function (newValue, oldValue) {
    if (newValue === false && oldValue === true) {
      $scope.getTranscription();
    }
  });

  updateTranscriptionObjectInfo = function () {
    //Check changes before move to another object
    if ($scope.transcriptionLockedByMe) {
      if (!angular.equals($filter('removeWordCharacters')($scope.transcription.text), $scope.lastSavedTranscriptionText)) {
        $scope.showUnsavedTranscriptionModificationsDialog($scope.transcription.text, $scope.currentObjectId, false);
      }
      else {
        $scope.unlockTranscription();
      }
    }

    $scope.lastSaveDate = "";
    $scope.currentObjectId = visorSvc.objectId;
    $scope.getAllTagsObjectWorkspace();
    $scope.getAllCommentsObjectWorkspace();
    $scope.getTranscription();
  };

  /**
   * This function is called to load the last contribution made in the current object.
   * If it was a comment, the comments tab is displayed when workspace is shown.
   */
  var changeTab = function () {
    if (tagsLastModified !== 0 && commentsLastModified !== 0 && transcriptionsLastModified !== 0) {
      var dates = [tagsLastModified, commentsLastModified, transcriptionsLastModified];
      var minDate = dates[0];
      var index = 1;
      for (var i = 1; i < dates.length; i++) {
        if (minDate < dates[i]) {
          minDate = dates[i];
          index = i + 1;
        }
      }
      var tab = 'tag';
      switch (index) {
        case 2:
          tab = 'comment';
          break;
        case 3:
          tab = 'transcription';
          break;
        // case 4:
        // tab = 'translation';
        // break;
      }
      $scope.setTab(index, tab);
    }
  };

  /**
   * SHARE UTILS
   */

  Element.prototype.remove = function () {
    this.parentElement.removeChild(this);
  };

  NodeList.prototype.remove = HTMLCollection.prototype.remove = function () {
    for (var i = 0, len = this.length; i < len; i++) {
      if (this[i] && this[i].parentElement) {
        this[i].parentElement.removeChild(this[i]);
      }
    }
  };


  //This function
  var updateThis = function () {
    //Remove share button from DOM
    document.getElementById("workspaceAddthis").remove();

    //Create Button
    var share = document.createElement('button');
    share.setAttribute('id', 'workspaceAddthis');
    share.setAttribute('class', 'addthis_button_compact btn btn-link');

    //Create Share image and text
    var shareIcon = document.createElement('span');
    shareIcon.setAttribute('class', 'glyphicons glyphicons-hospital color-orange');
    share.appendChild(shareIcon);

    var shareText = document.createElement('span');
    shareText.setAttribute('class', 'hidden-inline-portrait');
    var node = document.createTextNode("  Share");
    shareText.appendChild(node);
    share.appendChild(shareText);

    var root = document.getElementById("workspaceShare");
    root.appendChild(share);

    shareSvc.configureAddThis("#workspaceAddthis");
  };

  $scope.init = function () {
    if (!initAlreadyCalled) {
      initAlreadyCalled = true;
      visorSvc.registerObserverCallback(updateTranscriptionObjectInfo);
      //  visorSvc.registerObserverCallback(updateTranslationObjectInfo);
      visorSvc.registerObserverCallback(updateThis);

      var queryString = $location.search();
      if (queryString.num) {
        $scope.currentCommentId = queryString.num;
      }
      watchTranscriptionTooltip();
      // $scope.loadAllLanguages();
    }
  };

  $scope.closeWorkspace = function () {
    if (visorSvc.showLogin) {
      visorSvc.showLogin = false;
      return;
    }
    if ($scope.transcriptionLockedByMe) {
      $scope.cancelInactivityTimerCountDown();
    }

    //Check if the user is editing the transcription and ask if wants to saves
    if (!angular.equals($filter('removeWordCharacters')($scope.transcription.text), $scope.lastSavedTranscriptionText)) {
      $scope.showUnsavedTranscriptionModificationsDialog(null, null, true);
    }
    else {
      if ($scope.transcriptionLockedByMe) {
        $scope.unlockTranscription(0, true);
      }
      else {
        $scope.closeModals();
      }
    }
  };

  $scope.closeModals = function () {
    $('#workspaceModal').modal('hide');
    $('#workspaceResponsiveModal').modal('hide');
  };


  $('#workspaceModal').on('show.bs.modal', function () {
    $scope.init();
    OpaUtils.navigating = false;
    if ($scope.title.length > 125) {
      $(this).find('.modal-header').css({
        height: '10%'
      });
      $(this).find('.modal-body').css({
        height: '90%'
      });
    }
  });

  $('#workspaceModal').on('shown.bs.modal', function () {
    addthis.init();
    addthis.button($("#workspaceAddthis").get(), {}, {url: $location.absUrl(), title: addthis_share.title});
  });

  $('#workspaceResponsiveModal').on('shown.bs.modal', function () {
    addthis.init();
    addthis.button($("#workspaceResponsiveAddthis").get(), {}, {url: $location.absUrl(), title: addthis_share.title});
  });

  $('#workspaceModal').on('hide.bs.modal', function (e) {
    $location.search('contributionType', null).path("/id/" + $routeParams.naId, false);
    if (window.location.hash) {
      window.location.hash = "";
    }
  });

  $('#workspaceModal').on('hidden.bs.modal', function (e) {
    if (OpaUtils.navigating) {
      if (OpaUtils.showAccountHome) {
        OpaUtils.showAccountHome = false;
        $location.path("/accounts/" + Auth.userName());
      }
    }
    else {
      $window.location.assign($location.absUrl());
    }
  });

  $('#workspaceResponsiveModal').on('hide.bs.modal', function (e) {
    $location.search('contributionType', null).path("/id/" + $routeParams.naId, false);
    if (window.location.hash) {
      window.location.hash = "";
    }
  });

  $('#workspaceResponsiveModal').on('hidden.bs.modal', function (e) {
    if (OpaUtils.navigating) {
      if (OpaUtils.showAccountHome) {
        OpaUtils.showAccountHome = false;
        $location.path("/accounts/" + Auth.userName());
      }
    }
    else {
      $window.location.assign($location.absUrl());
    }
  });

  $('#workspaceResponsiveModal').on('show.bs.modal', function () {
    addthis_share.url = $location.absUrl();
    $scope.init();
    OpaUtils.navigating = false;
  });


  /* $scope.loadAllLanguages = function () {
   var languages = new Languages();
   var params = {};

   $scope.languages = {
   data: [
   {
   isoCode:"AAR",
   language:"Afar",
   country:"1"
   },
   {
   isoCode:"ABK",
   language:"Abkhazian",
   country:"1"
   },
   {
   isoCode:"ACE",
   language:"Achinese",
   country:"2"
   }],
   group: {field: "hasTranslation"}
   };

   languages.$get(params,
   function (data) {
   var languages = data.opaResponse.languages.languages;

   for (var language in data.languages) {

   language.hasTranslation = false;
   }

   $scope.languages = languages;
   },
   function (error) {

   if (!OpaUtils.checkForAPIError(error)) {
   //check for other errors block
   }

   });
   };*/

  var watchTranscriptionTooltip = function() {
    if ($("#transcription-locked-tooltip").data("kendoTooltip")) {
      if (!$scope.transcription.lockedTooltip) {
        $("#transcription-locked-tooltip").data("kendoTooltip").destroy();
        return;
      }
    }
    setTimeout(watchTranscriptionTooltip, 100);
  }
});
