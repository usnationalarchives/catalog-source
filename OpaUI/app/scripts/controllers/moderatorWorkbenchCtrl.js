opaApp.controller('moderatorWorkbenchCtrl', function ($scope, $timeout, $modal, $location, $log, ModeratorStream, ModeratorTags, ModeratorService, ModeratorContributions, OpaUtils, TranscriptionService, PaginationSvc, Auth, ErrorCodesSvc,OnlineAvailabilityService,$sce) {

  //UI BINDINGS
  $scope.header = "";
  $scope.transcriptionText = [];

  //GLOBAL VARIABLES
  $scope.tags = [];
  $scope.transcriptions = [];
  $scope.comments = [];
  $scope.moderatorActions = [];
  $scope.ModeratorService = ModeratorService;
  $scope.OpaUtils = OpaUtils;
  $scope.currentTab = '';
  $scope.openedLookup = false;

  //totals
  $scope.totalTags = 0;
  $scope.totalTranscriptions = 0;
  $scope.totalStream = 0;
  $scope.totalFilterTags = 0;
  $scope.totalFilterTranscriptions = 0;
  $scope.totalFilterStream = 0;


  //Pagination Simple Variables
  $scope.currentPage = 1;
  $scope.resultPerPage = 25;
  $scope.offset = 0;
  $scope.totalRecords = 0;
  $scope.offsetEnd = 0;
  $scope.noItems = true;
  $scope.nav = {};
  $scope.nav.targetPage = 1;

  //Online Availability Notification
  $scope.editNotification = false;
  $scope.newNotification = false;

  /**
   * Returns all the totals that are shown in the tab titles
   */
  $scope.getTotals = function () {

    var total = new ModeratorContributions();
    var params = {};

    if (ModeratorService.filterText) {
      params.naId = ModeratorService.filterText;
    }

    total.$get(params,
      function (data) {
        if (ModeratorService.filterText) {
          $scope.totalFilterTags = data.opaResponse.results.totalTags;
          $scope.totalFilterComments = data.opaResponse.results.totalComments;
          $scope.totalFilterTranscriptions = data.opaResponse.results.totalTranscriptions;
          $scope.totalFilterModerator = data.opaResponse.results.totalModerator;
        }
        else {
          $scope.totalTags = data.opaResponse.results.totalTags;
          $scope.totalComments = data.opaResponse.results.totalComments;
          $scope.totalTranscriptions = data.opaResponse.results.totalTranscriptions;
          $scope.totalModerator = data.opaResponse.results.totalModerator;
        }

        $scope.updateOffset();
      },
      function (error) {});
  };


  /** ---------------------------------------------- **/
  /** ---------------- TAGS ------------------------ **/
  /** ---------------------------------------------- **/
    //This function returns all the tags
  $scope.getAllTags = function () {
    var stream = new ModeratorStream();
    var params = {'offset': $scope.offset === -1 ? 0 : $scope.offset, 'rows': $scope.resultPerPage, 'filterType': 'TG'};

    if (ModeratorService.filterText) {
      params.naId = ModeratorService.filterText;
    }

    stream.$get(params,
      function (data) {
        if (data.opaResponse.results.contribution.length === 0) {
          $scope.tags = [];
          $scope.totalRecords = 0;
          $scope.noItems = true;
          $scope.noResults = true;
        }
        else if (data.opaResponse.results.contribution) {
          $scope.noResults = false;
          $scope.tags = data.opaResponse.results.contribution;
          $scope.createTooltips($scope.tags, 'tg');
          $scope.noItems = false;
          $scope.offset = data.opaResponse.results.offset;
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (Auth.checkIsNotLoggedIn(error)) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            $location.path("/login");
          }
          else if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.INVALID_PARAM)) {
            OpaUtils.showMessageModal("Error", "Text size of the filter is too long. Try again with a text smaller than 50 characters");
          }
          else {
            $scope.noItems = true;
            $scope.offset = 0;
            $scope.noResults = true;
          }
        }
      }
    );

  };

  /** ---------------------------------------------- **/
  /** -------------- COMMENTS  --------------------- **/
  /** ---------------------------------------------- **/
    //This function returns all the tags
  $scope.getAllComments = function () {
    var stream = new ModeratorStream();
    var params = {'offset': $scope.offset === -1 ? 0 : $scope.offset, 'rows': $scope.resultPerPage, 'filterType': 'CM'};

    if (ModeratorService.filterText) {
      params.naId = ModeratorService.filterText;
    }

    stream.$get(params,
      function (data) {
        if (data.opaResponse.results.contribution.length === 0) {
          $scope.comments = [];
          $scope.totalRecords = 0;
          $scope.noItems = true;
          $scope.noResults = true;
        }
        else if (data.opaResponse.results.contribution) {
          $scope.noResults = false;
          $scope.comments = data.opaResponse.results.contribution;
          $scope.createTooltips($scope.comments, 'cm');
          $scope.noItems = false;
          $scope.offset = data.opaResponse.results.offset;
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (Auth.checkIsNotLoggedIn(error)) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            $location.path("/login");
          }
          else if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.INVALID_PARAM)) {
            OpaUtils.showMessageModal("Error", "Text size of the filter is too long. Try again with a text smaller than 50 characters");
          }
          else {
            $scope.noItems = true;
            $scope.offset = 0;
            $scope.noResults = true;
          }
        }
      }
    );

  };

  /** ---------------------------------------------- **/
  /** ---------------- TRANSCRIPTIONS -------------- **/
  /** ---------------------------------------------- **/

  /**
   * Get all the transcriptions for moderation.
   * @param naid Filter by naid if it is specified
   */
  $scope.getAllTranscriptions = function () {

    var stream = new ModeratorStream();
    var params = {'offset': $scope.offset === -1 ? 0 : $scope.offset, 'rows': $scope.resultPerPage, 'filterType': 'TR'};

    if (ModeratorService.filterText) {
      params.naId = ModeratorService.filterText;
    }

    stream.$get(params,
      function (data) {
        if (data.opaResponse.results.contribution.length === 0) {
          $scope.transcriptions = [];
          $scope.totalRecords = 0;
          $scope.noItems = true;
          $scope.noResults = true;
        }
        else if (data.opaResponse.results.contribution) {
          $scope.noResults = false;
          $scope.transcriptions = data.opaResponse.results.contribution;
          $scope.createTooltips($scope.transcriptions, 'tr');
          $scope.noItems = false;
          $scope.offset = data.opaResponse.results.offset;
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (Auth.checkIsNotLoggedIn(error)) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            $location.path("/login");
          }
          else if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.INVALID_PARAM)) {
            OpaUtils.showMessageModal("Error", "Text size of the filter is too long. Try again with a text smaller than 50 characters");
          }
          else {
            $scope.noItems = true;
            $scope.offset = 0;
            $scope.noResults = true;
          }
        }
      }
    );
  };

  /**
   * Gets the whole transcription text
   * @param idModerationToolsPanel Html div id of the panel to show
   * @param idTeaserPanel Html div id containing the transcription teaser to hide
   * @param index Transcription index which correspond to
   * @param objectInfo Object containing the naid and object if of the transcription
   */
  $scope.reviewTranscription = function (idModerationToolsPanel, idTeaserPanel, index, objectInfo, version) {

    $scope.transcriptionText[index] = "";

    TranscriptionService.getTranscriptionVersion(objectInfo['@naid'], objectInfo['@id'], version).then(
      function (promise) {
        $scope.transcriptionText[index] = promise.opaResponse.transcription.text;
        $(idTeaserPanel).hide();
        $(idModerationToolsPanel).show();
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          if (Auth.checkIsNotLoggedIn(error)) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            $location.path("/login");
          }
        }
      });
  };

  /**
   * Closes the panel where the whole transcription text is displayed and the remove action.
   * @param idModerationToolsPanel Html div id of the panel to hide
   * @param idTeaserPanel  Html div id containing the transcription teaser to show
   */
  $scope.cancelModeration = function (idModerationToolsPanel, idTeaserPanel) {
    $(idTeaserPanel).show();
    $(idModerationToolsPanel).hide();
  };


  /** ---------------------------------------------- **/
  /** ---------------MODAL VERSIONING -------------- **/
  /** ---------------------------------------------- **/

  /**
   * Opens the Modal with the different version of a given transcription
   * @param transcription The transcription object
   */
  $scope.openVersioningModal = function (transcription) {

    var modal = $modal.open({
      templateUrl: 'views/directives/dialogs/previousVersionTranscriptionDialog.html',
      controller: ['$scope', '$modalInstance', 'transcription', 'OpaUtils', 'TranscriptionService', 'ModeratorService', 'ModeratorTranscriptions', 'Auth', '$location', function ($scope, $modalInstance, transcription, OpaUtils, TranscriptionService, ModeratorService, ModeratorTranscriptions, Auth, $location) {

        $scope.transcription = {};
        $scope.currentNumberVersion = parseInt(transcription.transcription['@version']);
        $scope.previuosTranscription = {};
        $scope.OpaUtils = OpaUtils;
        $scope.naid = transcription.object['@naid'];
        $scope.objId = transcription.object['@id'];
        $scope.title = transcription.title;
        $scope.pageNum = transcription.object['@pageNum'];
        $scope.totalPages = transcription.object['@totalPages'];


        //UI BINDINGS
        $scope.versions = [];
        $scope.reasons = [];
        $scope.ui = {
          versionSelected: '',
          reasonSelected: '',
          additionalComments: ''
        };


        $scope.prepareVersionsSelect = function () {
          var versionTemp = $scope.versions;
          $scope.versions = [];

          angular.forEach(versionTemp, function (value, key) {
            var obj = {};
            obj.text = 'Version ' + value['@num'] + ', ' + value['@action'] + ', ' + OpaUtils.fancyDate(value['@when']);
            obj.value = value['@num'];
            $scope.versions.push(obj);
          });
        };


        /**
         * Gets the last transcription version
         */
        $scope.getTranscriptionCurrentVersion = function () {

          TranscriptionService.getTranscriptionVersion($scope.naid, $scope.objId, $scope.currentNumberVersion).then(
            function (promise) {
              $scope.transcription = promise.opaResponse.transcription;
              $scope.currentNumberVersion = $scope.transcription['@version'];
            },
            function (error) {
              if (Auth.checkIsNotLoggedIn(error)) {
                $modalInstance.close();
                OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                $location.path("/login");
              }
            }
          );
        };

        /**
         * Gets the transcription of a given version
         * @param version
         */
        $scope.getTranscriptionPreviousVersion = function (version) {

          TranscriptionService.getTranscriptionVersion($scope.naid, $scope.objId, version ? version : $scope.ui.versionSelected).then(
            function (promise) {
              $scope.previuosTranscription = promise.opaResponse.transcription;
              // $scope.ui.versionSelected = $scope.previuosTranscription['@version'];
            },
            function (error) {
              if (Auth.checkIsNotLoggedIn(error)) {
                $modalInstance.close();
                OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                $location.path("/login");
              }
            }
          );
        };

        /**
         * Gets all the version of the current transcription
         */
        $scope.getAllVersions = function () {
          TranscriptionService.getTranscriptionAllVersions($scope.naid, $scope.objId).then(
            function (promise) {
              $scope.versions = promise.opaResponse.transcription.version;
              $scope.prepareVersionsSelect();
            },
            function (error) {
              if (Auth.checkIsNotLoggedIn(error)) {
                $modalInstance.close();
                OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                $location.path("/login");
              }
            }
          );
        };

        /**
         * Gets the list of reason for the transcription change
         *
         */
        $scope.getReasons = function () {
          ModeratorService.getReasons().then(
            function (promise) {
              $scope.reasons = ModeratorService.prepareReasonsForSelect(promise.opaResponse.reasons.reason, 'change');
            },
            function (error) {
              $scope.reasons = ModeratorService.prepareReasonsForSelect([], 'change');
            }
          );
        };

        $scope.cancel = function () {
          $modalInstance.close();
        };

        $scope.showNewReasonModal = function () {
          var modal = ModeratorService.showNewReasonModal();

          modal.result.then(
            function (reason) {
              ModeratorService.addReason(reason).then(
                function () {
                  $scope.getReasons();
                }
              );
            }
          );
        };

        /**
         * Restores a previous version of a transcription with the version selected in the version select
         */
        $scope.restoreTranscription = function () {

          if ($scope.ui.reasonSelected) {
            var mt = new ModeratorTranscriptions({'naid': $scope.naid, 'objId': $scope.objId});
            var params = {'versionNumber': $scope.ui.versionSelected, 'reasonId': $scope.ui.reasonSelected};

            if ($scope.ui.additionalComments) {
              params.notes = $scope.ui.additionalComments;
            }
            else {
              params.notes = " ";
            }

            mt.$restore(params,
              function (data) {
                $scope.getTranscriptionPreviousVersion($scope.currentNumberVersion);

                if (data.opaResponse.transcription) {
                  $scope.currentNumberVersion = data.opaResponse.transcription['@version'];
                }

                $scope.getTranscriptionCurrentVersion();

                $scope.getAllVersions();

                //Cleaning old control values
                $scope.ui.versionSelected = '';
                $scope.ui.reasonSelected = '';
                $scope.ui.additionalComments = '';

              },
              function (error) {
                if (!OpaUtils.checkForAPIError(error)) {
                  //check for other errors block
                  if (Auth.checkIsNotLoggedIn(error)) {
                    $modalInstance.close();
                    OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                    $location.path("/login");
                  }
                }
              }
            );
          }
          else {
            OpaUtils.showMessageModal("Error", "A reason must be selected");
          }
        };

        // Check when a previous version is selected
        $scope.$watch('ui.versionSelected', function (newValue, oldValue) {
          if (newValue !== oldValue) {
            $scope.getTranscriptionPreviousVersion($scope.ui.versionSelected);
          }
        });

        // Check when the New reason is selected
        $scope.$watch('ui.reasonSelected', function (newValue) {

          if (newValue === -1) {
            $scope.showNewReasonModal();
            $scope.ui.reasonSelected = '';
          }

        });


        $scope.init = function () {
          $scope.getTranscriptionCurrentVersion();
          $scope.getTranscriptionPreviousVersion($scope.currentNumberVersion - 1);
          $scope.getAllVersions();
          $scope.getReasons();
        };

        $scope.init();


      }],
      size: 'lg',
      resolve: {
        transcription: function () {
          return transcription;
        }
      }
    });

    modal.result.then(
      function (close) {
        $scope.getAllTranscriptions();
      }
    );

  };

  /** ---------------------------------------------- **/
  /** ---------   MODAL REMOVE TRANSCRIPTION ------- **/
  /** ---------------------------------------------- **/

  $scope.openRemoveTranscriptionModal = function (transcription) {

    var modal = $modal.open({
      templateUrl: 'views/directives/dialogs/removeTranscriptionDialog.html',
      controller: ['$scope', '$modalInstance', 'transcription', 'ModeratorService', 'ModeratorTranscriptions', 'Auth', '$location', 'ErrorCodesSvc', 'OpaUtils', function ($scope, $modalInstance, transcription, ModeratorService, ModeratorTranscriptions, Auth, $location, ErrorCodesSvc, OpaUtils) {

        $scope.transcription = transcription;
        $scope.naid = transcription.object['@naid'];
        $scope.objId = transcription.object['@id'];
        $scope.version = transcription.transcription['@version'];
        $scope.ModeratorService = ModeratorService;

        //$scope.reasons = [];
        $scope.ui = {
          reasonSelected: '',
          additionalComments: ''
        };


        $scope.removeTranscription = function () {

          if ($scope.ui.reasonSelected) {

            var mt = new ModeratorTranscriptions({'naid': $scope.naid, 'objId': $scope.objId});
            var params = {
              'versionNumber': $scope.version,
              'reasonId': $scope.ui.reasonSelected,
              'notes': $scope.ui.additionalComments ? $scope.ui.additionalComments : " "
            };

            mt.$remove(params,
              function (data) {
                $scope.cancel();
              },
              function (error) {
                if (!OpaUtils.checkForAPIError(error)) {
                  //check for other errors block
                  if (Auth.checkIsNotLoggedIn(error)) {
                    OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
                    $location.path("/login");
                  }
                  else if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.LOCKED_BY_ANOTHER)) {
                    OpaUtils.showErrorGlobalNotification("Unable to remove the transcription. It is being edited by someone else");
                  }
                }
              }
            );
          }
          else {
            OpaUtils.showErrorGlobalNotification("A reason must be selected");
          }
        };

        $scope.getReasons = function () {
          ModeratorService.getReasons().then(
            function (promise) {
              ModeratorService.reasonsRemove = ModeratorService.prepareReasonsForSelect(promise.opaResponse.reasons.reason, 'remove');
            },
            function (error) {
              ModeratorService.reasonsRemove = ModeratorService.prepareReasonsForSelect([], 'remove');
            }
          );
        };

        $scope.showNewReasonModal = function () {
          var modal = ModeratorService.showNewReasonModal();

          modal.result.then(
            function (reason) {
              ModeratorService.addReason(reason).then(
                function () {
                  $scope.getReasons();
                }
              );
            }
          );
        };

        // Check when the New reason is selected
        $scope.$watch('ui.reasonSelected', function (newValue) {

          if (newValue === -1) {
            $scope.showNewReasonModal();
            $scope.ui.reasonSelected = '';
          }

        });


        $scope.cancel = function () {
          $modalInstance.close();
        };

      }],
      size: 'xs',
      resolve: {
        transcription: function () {
          return transcription;
        }
      }
    });

    modal.result.then(
      function (close) {
        ModeratorService.notifyUpdaters();
      },
      function (dismiss) {}
    );
  };

  /** ---------------------------------------------- **/
  /** ------------ MODERATOR ACTION ---------------- **/
  /** ---------------------------------------------- **/

  $scope.getAllModeratorActions = function () {

    var stream = new ModeratorStream();
    var params = {
      'offset': $scope.offset === -1 ? 0 : $scope.offset,
      'rows': $scope.resultPerPage,
      'filterType': 'Moderator'
    };

    if (ModeratorService.filterText) {
      params.naId = ModeratorService.filterText;
    }

    stream.$get(params,
      function (data) {
        if (data.opaResponse.results.contribution.length === 0) {
          $scope.moderatorActions = [];
          $scope.totalRecords = 0;
          $scope.noItems = true;
          $scope.noResults = true;
        }
        else if (data.opaResponse.results.contribution) {
          $scope.noResults = false;
          $scope.moderatorActions = data.opaResponse.results.contribution;
          $scope.noItems = false;
          $scope.offset = data.opaResponse.results.offset;
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (Auth.checkIsNotLoggedIn(error)) {
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            $location.path("/login");
          }
          else if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.INVALID_PARAM)) {
            OpaUtils.showMessageModal("Error", "Text size of the filter is too long. Try again with a text smaller than 50 characters");
          }
          else {
            $scope.noItems = true;
            $scope.offset = 0;
            $scope.noResults = true;
          }
        }
      }
    );
  };


  /** ---------------------------------------------- **/
  /** ---------    MODAL LOOKUP ID    -------------- **/
  /** ---------------------------------------------- **/

  $scope.openModalLookupID = function () {

    ModeratorService.openedLookup = true;

    var modalL = $modal.open({
      templateUrl: 'views/directives/dialogs/lookupIdDialog.html',
      controller: ['$scope', '$modalInstance', 'Results', 'searchSvc', 'PaginationSvc', 'ModeratorService', function ($scope, $modalInstance, Results, searchSvc, PaginationSvc, ModeratorService) {

        //UI BINDINGS
        $scope.ui = {
          searchText: "",
          resultPerPage: 25
        };
        $scope.noResults = false;
        $scope.errorDescription = "";
        $scope.results = [];
        $scope.searchSvc = searchSvc;

        //Pagination Variables
        $scope.queryTime = "";
        $scope.currentPage = 1;
        $scope.resultPerPage = 25;
        $scope.page1 = 0;
        $scope.page2 = 0;
        $scope.page3 = 0;
        $scope.offset = 0;
        $scope.totalRecords = 0;
        $scope.nav = {};
        $scope.nav.targetPage = 1;

        //Pagination functions

        $scope.firstPageNumber = function () {
          PaginationSvc.firstPageNumber($scope);
          $scope.search();
        };

        $scope.lastPageNumber = function () {
          PaginationSvc.lastPageNumber($scope);
          $scope.search();
        };

        $scope.decreasePageNumber = function () {
          PaginationSvc.decreasePageNumber($scope);
          $scope.search();
        };

        $scope.pageNumber = function (page) {
          PaginationSvc.pageNumber($scope, page);
          $scope.search();
        };

        $scope.increasePageNumber = function () {
          PaginationSvc.increasePageNumber($scope);
          $scope.search();
        };

        $scope.last = function () {
          return PaginationSvc.last($scope);
        };

        $scope.doSearch = function () {
          $scope.offset = 0;
          $scope.search();
        };

        $scope.totalPages = function() {
          return PaginationSvc.getTotalPages($scope);
        };

        $scope.search = function () {

          //Reseting Variables
          $scope.errorDescription = "";
          $scope.noResults = false;

          var resultsService = new Results();
          var params = {'q': $scope.ui.searchText, 'offset': $scope.offset, "rows": $scope.ui.resultPerPage};

          resultsService.$get(params,
            function (data) {
              if (!data.opaResponse) {
                $scope.noResults = true;
              }
              else if (data.opaResponse.results === null || data.opaResponse.results.result === null) {
                $scope.noResults = true;
              }
              else {
                $scope.noResults = false;
                $scope.results = data.opaResponse.results.result;
                $scope.totalRecords = data.opaResponse.results['@total'];
                $scope.queryTime = data.opaResponse.totalTime;

                PaginationSvc.updateOffset($scope);
              }
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {

                $scope.results = [];
                $scope.totalRecords = 0;
                $scope.queryTime = 0;

                if (error && error.data.opaResponse) {
                  if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.INVALID_OFFSET) ||
                    angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.SEARCH_TIMEOUT)) {
                    $scope.errorDescription = error.data.opaResponse.error.description;
                  }
                  else {
                    $scope.noResults = true;
                  }
                }


              }


            }
          );
        };

        $scope.chooseResult = function (naid) {
          ModeratorService.filterText = naid;
          $modalInstance.close();
        };

        $scope.cancel = function () {
          $modalInstance.dismiss();
        };

        // Check when the New reason is selected
        $scope.$watch('ui.resultPerPage', function (newValue, oldVlaue) {

          if (newValue !== oldVlaue) {
            $scope.resultPerPage = newValue;
            $scope.currentPage = 1;
            $scope.offset = 0;
            $scope.search();
          }
        });
      }],
      size: 'lg'
    });

    modalL.result.then(
      function (close) {
        ModeratorService.openedLookup = false;
        ModeratorService.notifyUpdaters();
      },
      function (dismiss) {
        ModeratorService.openedLookup = false;
      }
    );

  };

  /** ---------------------------------------------- **/
  /** ---------- ONLINE  AVAILABILITY -------------- **/
  /** ---------------------------------------------- **/

  $scope.getNotification = function(){

    $scope.notification = null;
    $scope.editNotification = false;

    var notification = new OnlineAvailabilityService({'naId':$scope.notificationNaid});

    notification.$get({},
      function(data){
        $scope.notification = data.opaResponse['online-availability-header'];
        $scope.notification.text  = decodeURIComponent($scope.notification['@header']);
        $scope.notification['@headerSanitazed']  = $sce.trustAsHtml(decodeURIComponent($scope.notification['@header']));
        $scope.notification['@enabled'] = ($scope.notification['@enabled'] === "true");
        if(!$scope.notification.text) {
          OpaUtils.showMessageModal("Information", "Online availability notification for this archival description is not available");
        }
      },
      function(error){
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
        }
      });
  };
  $scope.clearNotificationFilter = function(){
    $scope.notificationNaid = "";
    $scope.notification = undefined;
    $scope.editNotification = false;
  };

  $scope.editOnlineNotification = function(){
    $scope.editNotification = !$scope.editNotification;
  };

  $scope.cancelEditionNotification = function(){
    $scope.editNotification = false;
    $scope.notification.text  = decodeURIComponent($scope.notification['@header']);
  };

  $scope.saveNotification = function(){

    if($scope.notification.text) {
      var notificationCall = new OnlineAvailabilityService({
        'naId': $scope.notificationNaid,
        'header': encodeURIComponent($scope.notification.text)
      });

      notificationCall.$saveNotification({},
        function (data) {
          $scope.editNotification = false;
          $scope.getNotification();
        },
        function (error) {
        });
    }
    else{
      OpaUtils.showMessageModal("Online Availability Warning","Text cannot be empty, please enter valid content");
    }

  };

  $scope.enableNotificationToogle = function(){
    var notificationCall = new OnlineAvailabilityService({'naId':$scope.notificationNaid,'header':encodeURIComponent($scope.notification.text)});

    notificationCall.$saveNotification({'enabled':$scope.notification['@enabled']},
      function (data) {
        $scope.getNotification();
      },
      function (error) {});
  };

  /** ---------------------------------------------- **/
  /** ---------------- GENERAL --------------------- **/
  /** ---------------------------------------------- **/

  /**
   * Gets the proper list of contribution depending on the selected tab.
   */
  var getContributions = function () {

    if (Auth.isLoggedIn()) {
      switch ($scope.currentTab) {
        case 'tg':
          $scope.getAllTags();
          break;

        case 'cm':
          $scope.getAllComments();
          break;

        case 'tr':
          $scope.getAllTranscriptions();
          break;

        case 'ma':
          $scope.getAllModeratorActions();
          break;
      }

      $scope.getTotals();
    }
    else {
      OpaUtils.showMessageModal("Not Logged in", "You must be logged in to stay in this page");
      $location.path("/login");

    }
  };

  /**
   * Gets the list of reasons
   *
   */
  $scope.getReasons = function () {
    ModeratorService.getReasons().then(
      function (promise) {
        ModeratorService.reasonsRemove = ModeratorService.prepareReasonsForSelect(promise.opaResponse.reasons.reason, 'remove');
        ModeratorService.reasonsRestore = ModeratorService.prepareReasonsForSelect(promise.opaResponse.reasons.reason, 'restore');
      },
      function (error) {
        ModeratorService.reasonsRemove = ModeratorService.prepareReasonsForSelect([], 'remove');
        ModeratorService.reasonsRestore = ModeratorService.prepareReasonsForSelect([], 'restore');
      }
    );
  };

  //Select the proper tab and display its content
  $scope.SelectTab = function (value) {
    switch (value) {
      case 'tg':
        $scope.header = "Tags";
        $scope.currentTab = 'tg';
        ModeratorService.filterText = "";
        $location.search('tabType', 'tag');
        break;
      case 'cm':
        $scope.header = "Comments";
        $scope.currentTab = 'cm';
        ModeratorService.filterText = "";
        $location.search('tabType', 'comment');
        break;
      case 'tr':
        $scope.header = "Transcriptions";
        $scope.currentTab = 'tr';
        ModeratorService.filterText = "";
        $location.search('tabType', 'transcription');
        break;

      case 'oa':
        $scope.header = "";
        $scope.currentTab = 'oa';
        $location.search('tabType', 'onlineavailability');
        break;

      case 'ma':
        $scope.header = "Moderator Actions";
        $scope.currentTab = 'ma';
        ModeratorService.filterText = "";
        $location.search('tabType', 'moderator');
        break;
    }
  };

  /**
   * Creates the tooltips over the Date field
   * @param contributions The list of tag or transcriptions
   * @param htmlTag the html tag name where the tooltip will be placed
   */
  $scope.createTooltips = function (contributions, htmlTag) {
    $timeout(function () {
      angular.forEach(contributions, function (tag, index) {

        if ($('#' + htmlTag + index).data("kendoTooltip")) {
          $('#' + htmlTag + index).data("kendoTooltip").destroy();
        }

        $('#' + htmlTag + index).kendoTooltip({
          autoHide: true,
          position: "bottom",
          content: OpaUtils.fancyDateCompleteFormat(tag['@when'])
        });
      });
    }, 500);
  };


  //Pagination functions
  $scope.decreasePageNumber = function () {
    PaginationSvc.decreasePageNumber($scope);
    getContributions();
  };

  $scope.pageNumber = function (page) {
    PaginationSvc.pageNumber($scope, page);
    getContributions();
  };

  $scope.increasePageNumber = function () {
    PaginationSvc.increasePageNumber($scope);
    getContributions();
  };

  $scope.last = function () {
    return PaginationSvc.last($scope);
  };

  $scope.totalPages = function() {
    return PaginationSvc.getTotalPages($scope);
  };

  $scope.firstPageNumber = function () {
    PaginationSvc.firstPageNumber($scope);
    getContributions();
  };

  $scope.lastPageNumber = function () {
    PaginationSvc.lastPageNumber($scope);
    getContributions();
  };

  $scope.updateOffset = function () {

    switch ($scope.currentTab) {
      case 'tg':
        if (ModeratorService.filterText) {
          $scope.totalRecords = $scope.totalFilterTags;
        }
        else {
          $scope.totalRecords = $scope.totalTags;
        }
        break;

      case 'cm':
        if (ModeratorService.filterText) {
          $scope.totalRecords = $scope.totalFilterComments;
        }
        else {
          $scope.totalRecords = $scope.totalComments;
        }
        break;

      case 'tr':
        if (ModeratorService.filterText) {
          $scope.totalRecords = $scope.totalFilterTranscriptions;
        }
        else {
          $scope.totalRecords = $scope.totalTranscriptions;
        }
        break;

      case 'ma':
        if (ModeratorService.filterText) {
          $scope.totalRecords = $scope.totalFilterModerator;
        }
        else {
          $scope.totalRecords = $scope.totalModerator;
        }
        break;

    }
    PaginationSvc.updateOffset($scope);
  };

  $scope.clearFilter = function () {
    ModeratorService.filterText = "";
    $scope.offset = 0;
    getContributions();
  };







  /** ---------------------------------------------- **/
  /** ----------------- WATCHERS-------------------- **/
  /** ---------------------------------------------- **/

    // Check when the results per page is changed and request a new set of results
  $scope.$watch('resultPerPage', function (newValue, oldValue) {
    if (newValue !== oldValue) {
      $scope.offset = 0;
      getContributions();
    }
  });

  $scope.init = function () {

    //read tab type from query string
    var p = $location.search().tabType;
    if (typeof p !== 'undefined') {
      switch (p) {
        case 'tag':
          $scope.SelectTab('tg');
          break;
        case 'comment':
          $scope.SelectTab('cm');
          break;
        case 'transcription':
          $scope.SelectTab('tr');
          break;
        case 'onlineavailability':
          $scope.SelectTab('oa');
          break;
        case 'moderator':
          $scope.SelectTab('ma');
          break;
      }

      if (!ModeratorService.openedLookup) {
        $scope.offset = 0;
        $scope.resultsPerPage = 25;

        getContributions();
      }
    }

    ModeratorService.registerUpdateCallback(getContributions);
    if (!ModeratorService.openedLookup) {
      $scope.getReasons();
    }

  };

  $scope.init();
});
