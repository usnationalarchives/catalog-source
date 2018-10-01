opaApp.controller('editionModeratorCtrl', function ($scope, $timeout, $modal, ModeratorService, ModeratorStream, ModeratorTags, ModeratorTagsObjects, OpaUtils, ModeratorTranscriptions, Auth,ModeratorComments, ModeratorCommentsObjects, ModeratorOnlineAvailability) {

  //UI BINDINGS
  $scope.ui = {
    reasonSelected: ""
  };
  $scope.additionalComments = "";
  $scope.ModeratorService = ModeratorService;

  $scope.showNewReasonModal = function () {
    var modal = ModeratorService.showNewReasonModal();

    modal.result.then(
      function (reason) {
        $scope.addReason(reason);
      }
    );
  };

  // Check when the New reason is selected
  $scope.$watch('ui.reasonSelected', function (newValue) {
    if (angular.equals(newValue, -1)) {
      $scope.showNewReasonModal();
      $scope.ui.reasonSelected = '';
    }

  });

  $scope.addReason = function (reason) {
    ModeratorService.addReason(reason).then(
      function () {
        $scope.getReasons();
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {

        }
      }
    );

  };

  $scope.getReasons = function () {

    ModeratorService.getReasons().then(
      function (promise) {
        ModeratorService.reasonsRemove = ModeratorService.prepareReasonsForSelect(promise.opaResponse.reasons.reason, 'remove');
        ModeratorService.reasonsRestore = ModeratorService.prepareReasonsForSelect(promise.opaResponse.reasons.reason, 'restore');
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          ModeratorService.reasonsRemove = ModeratorService.prepareReasonsForSelect([], 'remove');
          ModeratorService.reasonsRestore = ModeratorService.prepareReasonsForSelect([], 'restore');
        }
      }
    );

  };

  $scope.confirmModeration = function () {
    var mt;
    var params = {};
    if ($scope.ui.reasonSelected !== "") {
      if (angular.equals($scope.contribution['@type'], 'tag')) {
        var tag;
        if ($scope.contribution.description['@id']) {
          tag = new ModeratorTagsObjects({
            'naid': $scope.contribution.description['@naid'],
            'objectId': $scope.contribution.description['@id']
          });
        }
        else {
          tag = new ModeratorTags({'naid': $scope.contribution.description['@naid']});
        }

        params = {
          'reasonId': $scope.ui.reasonSelected,
          'text': $scope.contribution.tag['@text'],
          'notes': $scope.additionalComments ? $scope.additionalComments : "."
        };

        // REMOVE/RESTORE TAGS
        if (angular.equals($scope.contribution['@action'], 'ADD') || angular.equals($scope.contribution['@action'], 'RESTORE')) {
          tag.$remove(params,
            function (data) {
              OpaUtils.showSuccessGlobalNotification("Tag Removed");
              ModeratorService.notifyUpdaters();
              $("#moderationTag" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                //check for other errors block
                if (Auth.checkIsNotLoggedIn(error)) {
                  OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                  $location.path("/login");
                }
              }
            });
        }
        else if (angular.equals($scope.contribution['@action'], 'DELETE') || angular.equals($scope.contribution['@action'], 'REMOVE')) {
          tag.$restore(params,
            function (data) {
              OpaUtils.showSuccessGlobalNotification("Tag Restored");
              ModeratorService.notifyUpdaters();
              $("#moderationTag" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                //check for other errors block
                if (Auth.checkIsNotLoggedIn(error)) {
                  OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                  $location.path("/login");
                }
              }
            });
        }
      }
      // REMOVE/RESTORE COMMENTS
      else if (angular.equals($scope.contribution['@type'], 'comment')) {
        var cm;

        if ($scope.contribution.description['@id']) {
          cm = new ModeratorCommentsObjects({
            'naid': $scope.contribution.description['@naid'],
            'commentId': $scope.contribution['@transId'],
            'objectId': $scope.contribution.description['@id']
          });
        }
        else {
          cm = new ModeratorComments({
            'naid': $scope.contribution.description['@naid'],
            'commentId': $scope.contribution['@transId']
          });
        }

        params = {
          'reasonId': $scope.ui.reasonSelected,
          'notes': $scope.additionalComments ? $scope.additionalComments : " "
        };

        if (angular.equals($scope.contribution['@action'], 'NEW') || angular.equals($scope.contribution['@action'], 'EDIT') || angular.equals($scope.contribution['@action'], 'REPLY') || angular.equals($scope.contribution['@action'], 'RESTORE')) {
          cm.$remove(params,
            function (data) {
              OpaUtils.showSuccessGlobalNotification("Comment Removed");
              ModeratorService.notifyUpdaters();
              $("#moderationActionEdition" + $scope.index).collapse('hide');
            },
            function(error){
              if (!OpaUtils.checkForAPIError(error)) {
                OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
              }
            });
        }
        else if (angular.equals($scope.contribution['@action'], 'REMOVE')) {
          cm.$restore(params,
            function (data) {
              OpaUtils.showSuccessGlobalNotification("Comment Restored");
              ModeratorService.notifyUpdaters();
              $("#moderationActionEdition" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                //check for other errors block
                OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
              }
            }
          );
        }
      }
      // REMOVE/RESTORE TRANSCRIPTIONS
      else if (angular.equals($scope.contribution['@type'], 'transcription')) {
        mt = new ModeratorTranscriptions({
          'naid': $scope.contribution.object['@naid'],
          'objId': $scope.contribution.object['@id']
        });
        params = {
          'versionNumber': $scope.contribution.transcription['@version'],
          'reasonId': $scope.ui.reasonSelected,
          'notes': $scope.additionalComments ? $scope.additionalComments : " "
        };

        if (angular.equals($scope.contribution['@action'], 'RESTORE')) {
          mt.$remove(params,
            function (data) {
              OpaUtils.showSuccessGlobalNotification("Transcription Removed");
              ModeratorService.notifyUpdaters();
              $("#moderationActionEdition" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                if (Auth.checkIsNotLoggedIn(error)) {
                  OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                  $location.path("/login");
                }
              }
            }
          );

        }
        else if (angular.equals($scope.contribution['@action'], 'REMOVE')) {

          mt.$restore(params,
            function (data) {
              OpaUtils.showSuccessGlobalNotification("Transcription Restored");
              ModeratorService.notifyUpdaters();
              $("#moderationActionEdition" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                //check for other errors block
                if (Auth.checkIsNotLoggedIn(error)) {
                  OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                  $location.path("/login");
                }
              }
            }
          );
        }
      }
      //REMOVE/RESTORE ANNOUNCEMENTS
      else if (angular.equals($scope.contribution['@type'], 'announcement')) {
        mt = new ModeratorOnlineAvailability({
          'naid': $scope.contribution.description['@naid']
        });
        params = {
          'reasonId': $scope.ui.reasonSelected,
          'notes': $scope.additionalComments ? $scope.additionalComments : " "
        };

        if (angular.equals($scope.contribution['@action'], 'UPDATE') || angular.equals($scope.contribution['@action'], 'RESTORE')) {
          mt.$remove(params,
            function (data) {
              ModeratorService.notifyUpdaters();
              $("#moderationActionEdition" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
              }
            }
          );
        }
        else if (angular.equals($scope.contribution['@action'], 'REMOVE')) {
          mt.$restore(params,
            function (data) {
              ModeratorService.notifyUpdaters();
              $("#moderationActionEdition" + $scope.index).collapse('hide');
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                //check for other errors block
              }
            }
          );
        }
      }
    }

    else {
      OpaUtils.showErrorGlobalNotification("A reason must be selected");
    }
  };

  $scope.cleanForm = function () {
    $scope.ui.reasonSelected = "";
    $scope.additionalComments = "";

    if (angular.equals($scope.type, 'Tag')) {
      $("#moderationTag" + $scope.index).collapse('hide');
    }
    else if(angular.equals($scope.type, 'Comment')) {
      $("#moderationComment" + $scope.index).collapse('hide');
    }
    else if (angular.equals($scope.type, 'ModeratorAction')) {
      $("#moderationActionEdition" + $scope.index).collapse('hide');
    }
  };
});
