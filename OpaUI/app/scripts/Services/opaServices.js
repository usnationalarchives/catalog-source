opaApp.service("ErrorCodesSvc", function () {
  this.NO_TAGS_EXIST = "TAG_NOT_FOUND";
  this.COMMENT_NOT_FOUND = "COMMENT_NOT_FOUND";
  this.NOT_LOGGED_IN = "INVALID_CREDENTIALS";
  this.LOCKED_BY_ANOTHER = "LOCKED_BY_ANOTHER";
  this.USER_EXISTS = "USER_EXISTS";
  this.INVALID_EMAIL = "INVALID_EMAIL";
  this.EMAIL_EXISTS = "EMAIL_EXISTS";
  this.INVALID_PATTERN = "INVALID_PATTERN";
  //Error for username field
  this.EXCEEDS_SIZE = "EXCEEDS_SIZE";
  this.MISSING_PARAMETER = "MISSING_PARAMETER";
  this.ROWS_LIMIT_EXCEEDED = "ROWS_LIMIT_EXCEEDED";
  this.INVALID_OFFSET = "INVALID_OFFSET";
  this.INVALID_PARAM = "INVALID_PARAM";
  this.ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
  this.SEARCH_TIMEOUT = "SEARCH_TIMEOUT";
  this.BACKGROUND_IMAGE_NOT_FOUND = "BACKGROUND_IMAGE_NOT_FOUND";
});

opaApp.service("OpaUtils", function ($filter, $injector, configServices, $location) {

  /**
   * Variable to identify when the user navigates from the workspace
   * @type {boolean}
   */
  this.navigating = false;

  /**
   * "Constant" to compare the status response of REST calls
   * @type {number}
   */
  this.NOT_FOUND = 404;
  this.SERVER_ERROR = 500;
  this.SYSTEM_ERROR_MESSAGE = "System error, please contact NARA support at catalog@nara.gov";

  /*
   * Variable to identify when the user has initiated a bulk export
   */
  this.isBulkExport = false;

  /*
   * Variable to identify when the user has clicked the url on the Leaving NARA dialog
   */
  var leavingNara = false;

  /*
   * Flag to determine if account home should be loaded after workspace modal is hidden
   */

  this.showAccountHome = false;

  /**
   * Flag to identify if app is running on IE. This is considered for IE8 and IE9
   *
   */
  this.isOldIE = false;
  this.isOldIE8 = false;
  this.isIE = false;

  this.checkIE = function () {
    // Detecting IE
    if ($('html').is('.ie8, .ie9')) {
      this.isOldIE = true;
    }
    // Detecting IE8
    if ($('html').is('.ie8')) {
      this.isOldIE8 = true;
    }

    //Detecting any IE
    if (navigator.appName == 'Microsoft Internet Explorer' ||  !!(navigator.userAgent.match(/Trident/) || navigator.userAgent.match(/rv 11/)))
    {
      this.isIE = true;
    }
  };
  this.checkIE();


  this.supports_history_api = function() {
    return !!(window.history && history.pushState);
  };

  this.checkForAPIError = function (error) {
    try {
      if (error && (angular.equals(error.status, this.SERVER_ERROR) || !error.data.opaResponse)) {
        this.showMessageModal("We are Sorry!",
          "Something went wrong, please contact <a href='mailto:catalog@nara.gov'>NARA support</a>");
        return true;
      }
      return false;
    } catch(error) {
      return false;
    }
  };

  //Returns the relative date from now
  this.fancyDate = function (date) {
    if(date) {
      var localTime = moment.utc(date).toDate();
      return moment(localTime).fromNow();
    }
  };

  // Return the date in this format MMMM Do, YYYY h:mmm a
  this.fancyDateCompleteFormat = function (date) {
    var localtime = moment.utc(date).toDate();
    return moment(localtime).format('MMMM Do, YYYY h:mm a');
  };

  // Return the date in this format MMM Do, YYYY h:mmm a
  this.fancyDateMMMFormat = function (date) {
    var localtime = moment.utc(date).toDate();
    return moment(localtime).format('MMM Do, YYYY h:mm a');
  };

  /**
   * Show little success notification in the corner of the screen.
   * Read http://notifyjs.com/ for more customization
   * Position: Right bottom
   * @param text Text to display
   */
  this.showSuccessGlobalNotification = function (text) {
    $.notify(
      text,
      {
        position: "right bottom",
        className: 'success'
      }
    );
  };

  /**
   * Show little error notification in the corner of the screen.
   * Read http://notifyjs.com/ for more customization
   * Position: Right bottom
   * @param text Text to display
   */
  this.showErrorGlobalNotification = function (text) {
    $.notify(
      text,
      {
        position: "right bottom",
        className: 'error'
      }
    );
  };

  /**
   * Show little error notification in the position of the element. It's around the element
   * Read http://notifyjs.com/ for more customization
   * @param element The ID of the HTML element
   * @param text Text to display
   * @param position left, right, bottom center
   */
  this.showErrorElementNotification = function (element, text, position) {
    $(element).notify(
      text,
      {
        position: position,
        className: 'error'
      }
    );
  };

  /**
   * Takes the size in bytes
   * and returns it in a better scale
   * @param fileSize
   * @returns {string}
   */
  this.getPretyFileSize = function (fileSize) {
    var size = parseFloat(fileSize);
    var sizeDisplay = '';
    var i;
    var sizes = [
      ' B',
      ' KB',
      ' MB',
      ' GB'
    ];
    if (!isNaN(size)) {
      for (i = 0; i < sizes.length; i++) {
        if (size > 1024) {
          size = size / 1024;
        } else {
          sizeDisplay = Math.round(size * 100) / 100 + sizes[i];
          break;
        }
      }
      if (sizeDisplay) {
        return sizeDisplay;
      }
      return Math.round(size * 100) / 100 + ' TB';
    }
  };

  //This function checks if the workspace modal is open
  this.isOpenWorkspace = function () {
    if ($("#workspaceModal").data('bs.modal')) {
      return $("#workspaceModal").data('bs.modal').isShown;
    }
    else if ($("#workspaceResponsiveModal").data('bs.modal')) {
      return $("#workspaceResponsiveModal").data('bs.modal').isShown;
    }
    else {
      return false;
    }
  };

  //Show a modal with a error message coming from the API
  this.showMessageModal = function (title, message) {
    $injector.get('$modal').open({
      templateUrl: 'views/directives/dialogs/messagedialog.html',
      controller: ['$scope', '$modalInstance', 'title', 'message', function ($scope, $modalInstance, title, message) {

        $scope.title = title;
        $scope.message = message;

        $scope.ok = function () {
          $modalInstance.close();
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
  };

  /**
   * Shows a message when timer is about to finish
   */
  this.showTimerModal = function () {
    var title = 'Ending login session';
    var message = 'You have been inactive in the National Archives Catalog for 30 minutes. If no action is taken, you will be logged out from the Catalog.';
    $injector.get('$modal').open({
      templateUrl: 'views/directives/dialogs/messageDialogTimer.html',
      controller: ['$scope', '$modalInstance',
        function ($scope, $modalInstance) {

          $scope.title = title;
          $scope.message = message;
          $scope.timer = null;
          $scope.interval = null;

          var doLogout = function () {
            try {
              $modalInstance.close();
            } catch (e) {
            }
            $injector.get('LoginService').logout();
            $injector.get('Auth').logout();
            localStorage.setItem("timerModal", "logout");
          };

          var clearTimer = function () {
            try {
              $modalInstance.close();
            } catch (e) {
            }
            if ($scope.interval) {
              clearInterval($scope.interval);
            }
            if ($scope.timer) {
              $injector.get('$timeout').cancel($scope.timer);
            }
          };

          var checkTime = function () {
            var clickAction = localStorage.getItem("timerModal");
            if (clickAction) {
              clearTimer();
              if (clickAction === "login") {
                $scope.login(true);
              }
            } else {
              var endTime = localStorage.getItem("timerFinish");
              if ((new Date()).getTime() <= endTime) {
                var timeRemaining = endTime - (new Date()).getTime();
                //console.log((new Date()).toTimeString() + ": remaining time en callback: " + timeRemaining);
                if ($injector.get('Auth').timer) {
                  $injector.get('$timeout').cancel($injector.get('Auth').timer);
                }
                $injector.get('Auth').timer = $injector.get('$timeout')($injector.get('Auth').timerCallbackRef, timeRemaining);
                clearTimer();
              }
            }
          };

          $scope.login = function (skip) {
            $injector.get('Account').view({username: $injector.get('Auth').userName()},
              function () {
                if (!skip) {
                  $modalInstance.close();
                  if ($scope.timer) {
                    $injector.get('$timeout').cancel($scope.timer);
                    localStorage.setItem("timerModal", "login");
                  }
                }
              },
              function () {
                $modalInstance.close();
              }
            );
          };

          $scope.timeout = function () {
            localStorage.removeItem("timerModal");
            $scope.timer = $injector.get('$timeout')(function () {
              doLogout();
            }, 60000);
            $scope.interval = setInterval(function () {
              checkTime();
            }, 2000);
          };

          $scope.timeout();

          $scope.logout = function () {
            doLogout();
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
  };

  /**
   * Shows a message for external URLs notifying the user
   * @param url
   */
  this.showExternalUrlMessageModal = function (url) {
    if (leavingNara) {
      leavingNara = false;
      return;
    }
    $injector.get('$modal').open({
      templateUrl: 'views/directives/dialogs/messagedialogURL.html',
      controller: ['$scope', '$modalInstance', function ($scope, $modalInstance) {

        /**
         * External URL
         */
        $scope.url = url;

        /**
         * Timer time in seconds for the re-direct
         * @type {number}
         */
        $scope.time = 10;

        $scope.onClick = function () {
          window.open($scope.url);
          clearInterval(counter);
          $modalInstance.close();
          leavingNara = true;
        };

        /**
         * Actual timer
         */
        function timer() {
          $scope.time--;
          if ($scope.time <= 0) {
            /**
             * When the time reaches 0
             * stop the timer and redirect
             */
            clearInterval(counter);
            window.open($scope.url);
            $modalInstance.close();
          }
        }

        /**
         * start the timer
         * @type {number}
         */
        var counter = setInterval(timer, 1000);

        /**
         * If the user clicks the Ok button redirect immediately
         */
        $scope.ok = function () {
          window.open($scope.url);
          clearInterval(counter);
          $modalInstance.close();
        };
      }],
      size: 'sm'
    });
  };

  //Show a modal with a error message coming from the API
  this.showMessageModalLogin = function () {
    var modal = $injector.get('$modal').open({
      templateUrl: 'views/directives/dialogs/messagedialogLogin.html',
      controller: ['$scope', '$modalInstance', function ($scope, $modalInstance) {

        $scope.ok = function () {
          $modalInstance.close();
        };

        $scope.cancel = function () {
          $modalInstance.dismiss();
        };
      }],
      size: 'sm'
    });

    var action = modal.result.then(
      function (close) {
        return false;
      },
      function (dismiss) {
        return true;
      }
    );

    return action;
  };

  //Show a modal with a error message coming from the API
  this.showInfoModal = function (title, message) {
    $injector.get('$modal').open({
      templateUrl: '../../views/directives/dialogs/infoDialog.html',
      controller: ['$scope', '$modalInstance', 'title', 'message', function ($scope, $modalInstance, title, message) {

        $scope.title = title;
        $scope.message = message;

        $scope.ok = function () {
          $modalInstance.close();
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
  };

  /**
   * This dialog is shown when the browser is not supported
   * in this case IE8 and IE9
   */
  this.showNotSupportedBrowserDialog = function(){
    $injector.get('$modal').open({
      templateUrl: 'views/directives/dialogs/notSupportedBrowser.html',
      controller: ['$scope', '$modalInstance', function ($scope, $modalInstance) {

        $scope.continue = function () {
          $modalInstance.close();
        };
      }],
      size: 'sm'
      });
  };

  /**
   * Gets the path to the storage where the images are
   * @param naId
   * @returns {string}
   * @param isDzi
   * @param path
   */
  this.getStoragePath = function (naId, isDzi, path) {
    if (!path) {
      path = isDzi ? '/opa-renditions/image-tiles': '/opa-renditions/thumbnails';
    }
    else if (path.match(/^https?:\/\//)){
      //If the path is absolute, then return it without modification.
      return path;
    }
    return configServices.OPASTORAGE_URL + naId + '/' + path;
  };

  /**
   * Extracts the extension for a file and returns it
   * @param filename
   * @returns {string}
   */
  this.getFileExt = function (filename) {
    return filename.substr(filename.lastIndexOf('.') + 1).toUpperCase();
  };

  /**
   * Toggles the collapse state of a panel
   * @param element to collapse/uncollapse
   * @param link that has the indicator of the collapse state
   * @param parent
   * @param collapse
   */
  this.toggle = function (element, link, parent, collapse) {
    var classValues = $(element).attr('class').split(' ');
    if (collapse) {
      $('.' + collapse).addClass('collapsed');
    }
    if (parent) {
      $(element).collapse({parent: parent}).collapse('toggle');
    } else {
      $(element).collapse('toggle');
    }
    if (classValues.indexOf('in') >= 0) {
      $(link).addClass('collapsed');
    } else {
      $(link).removeClass('collapsed');
    }
  };

  this.getIconFromType = function (iconType) {
    if (iconType) {
      switch (true) {
        case /file-unit/.test(iconType):
          return "images/fileunit.svg";
        case /itemAv/.test(iconType):
          return "images/video.svg";
        case /organization/.test(iconType):
          return "images/glyphicons-264-bank.svg";
        case /person/.test(iconType):
          return "images/glyphicons-4-user.svg";
        case /series/.test(iconType):
          return "images/series.svg";
        case /record-group/.test(iconType):
          return "images/recordgroup.svg";
        case /collection/.test(iconType):
          return "images/collection.svg";
        case /specific-records-type/.test(iconType):
          return "images/specificrecordstype.svg";
        case /topical-subject/.test(iconType):
          return "images/topicalsubject.svg";
        case /geographic-reference/.test(iconType):
          return "images/geographicreference.svg";
        case /item/.test(iconType):
          return "images/item.svg";
        case /audio/.test(iconType):
          return "images/audio.svg";
        case /document/.test(iconType):
        case /text/.test(iconType):
          return "images/text.svg";
        case /video/.test(iconType):
          return "images/video.svg";
        case /web/.test(iconType):
          return "images/web.svg";
        case /other/.test(iconType):
        case /image/.test(iconType):
          break;
        default:
      }
    }
    return "";
  };

  this.prettyNameObjects = function (name, single) {
    var singular = '';
    var plural = '';
    switch (angular.lowercase(name)) {
      case 'recordgroup':
        singular = 'Record Group';
        plural = 'Record Groups';
        break;
      case 'fileunit':
        singular = 'File Unit';
        plural = 'File Units';
        break;
      case 'series':
        singular = 'Series';
        plural = singular;
        break;
      case 'collection':
        singular = 'Collection';
        plural = 'Collections';
        break;
      case 'item':
        singular = 'Item';
        plural = 'Items';
        break;
      case 'itemav':
        singular = 'Item AV';
        plural = 'Items AV';
        break;
      case 'object':
        singular = 'Object';
        plural = 'Objects';
        break;
      case 'organization':
        singular = 'Organization';
        plural = 'Organizations';
        break;
    }
    return single ? singular : plural;
  };

  /**
   * Function to know if the user is in the special page of Interactive Documentation
   * @returns {boolean}
   */
  this.isIntDocsPage = function () {
    var path = $location.path().toUpperCase();
    return path === '/INTERACTIVEDOCUMENTATION';

  };

  /**
   * Function to know if we are in a mobile device
   * @returns {boolean}
   */
  this.isMobileDevice = function () {
    return !!(navigator.userAgent.match(/Android/i) || navigator.userAgent.match(/webOS/i) ||
    navigator.userAgent.match(/iPhone/i) || navigator.userAgent.match(/iPad/i) ||
    navigator.userAgent.match(/iPod/i) || navigator.userAgent.match(/BlackBerry/i) ||
    navigator.userAgent.match(/Windows Phone/i));
  };

  /**
   * Function to know if we are in a iOS device
   * @returns {boolean}
   */
  this.isIosDevice = function () {
    return !!(navigator.userAgent.match(/iPhone/i) ||
    navigator.userAgent.match(/iPad/i) || navigator.userAgent.match(/iPod/i));
  };

  /**
   * Function to know if we are in a phone
   * @returns {boolean}
   */
  this.isPhone = function () {
    return window.innerWidth <= 568;
  };

  /**
   * Function to check is a value is a number
   * @param n value
   * @returns {boolean}
   */
  this.isNumeric = function(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  };
});

opaApp.service("PaginationSvc", function () {

  this.last = function (scope) {
    return scope.currentPage > Math.floor(scope.totalRecords / scope.resultPerPage);
  };

  this.firstPageNumber = function (scope) {
    if (scope.currentPage <= 1) {
      return;
    }
    scope.currentPage = 1;
    scope.offset = 0;
  };

  this.calculateFirstPageOffset = function (scope) {
    return 0;
  };

  this.lastPageNumber = function (scope) {
    if (this.last(scope)) {
      return;
    }
    scope.currentPage = Math.floor(scope.totalRecords / scope.resultPerPage);
    if ((scope.totalRecords % scope.resultPerPage) >= 1) {
      scope.currentPage = scope.currentPage + 1;
    }

    scope.offset = (scope.currentPage - 1) * scope.resultPerPage;
  };

  this.calculateLastPageOffset = function (scope) {
    var lastPageNum = Math.floor(scope.totalRecords / scope.resultPerPage);
    if ((scope.totalRecords % scope.resultPerPage) >= 1) {
      lastPageNum = lastPageNum + 1;
    }
    return (lastPageNum -1) * scope.resultPerPage;
  };

  this.increasePageNumber = function (scope) {
    if (this.last(scope)) {
      return;
    }
    scope.currentPage = scope.currentPage + 1;
    scope.offset = (scope.currentPage - 1) * scope.resultPerPage;
  };

  this.calculateNextPageOffset = function (scope) {
    return ((scope.currentPage + 1) - 1) * scope.resultPerPage;
  };

  this.decreasePageNumber = function (scope) {
    if (scope.currentPage <= 1) {
      return;
    }
    scope.currentPage = scope.currentPage - 1;
    scope.offset = (scope.currentPage - 1) * scope.resultPerPage;
  };

  this.calculatePreviousPageOffset = function (scope) {
    if (scope.currentPage <= 1) {
      return 0;
    }
    return ((scope.currentPage -1) - 1) * scope.resultPerPage;
  };

  this.pageNumber = function (scope, number) {
    var totalPages  =  this.getTotalPages(scope);

    if(number < 1)
    {
      number = 1;
    }

    if(number > totalPages)
    {
      number = totalPages;
    }
    scope.currentPage = number;
    scope.offset = (scope.currentPage - 1) * scope.resultPerPage;
  };

  this.calculateSpecificPageOffset = function (scope, number) {
    var totalPages = this.getTotalPages(scope);

    if(number < 1){
      number = 1;
    }

    if(number > totalPages){
      number = totalPages;
    }
    return (number - 1) * scope.resultPerPage;
  };

  this.getTotalPages = function(scope)
  {
    if(scope.totalRecords === 0){
     return Math.ceil((scope.offset/scope.resultPerPage) + 1);
    }
    else{
      return Math.ceil(scope.totalRecords/scope.resultPerPage);
    }

  };

  this.updateOffset = function (scope) {
    scope.currentPage = Math.floor((scope.offset / scope.resultPerPage) + 1);
    scope.nav.targetPage = scope.currentPage;
    scope.offsetEnd = Math.min(scope.offset + parseInt(scope.resultPerPage), scope.totalRecords);
    scope.page1 = scope.currentPage;

    var totalPages  =  Math.ceil(scope.totalRecords / scope.resultPerPage);
    if (scope.page1 === 1) {
      scope.page2 = 2;
      scope.page3 = 3;

    } else if ( scope.page1 === totalPages) {
      scope.page3 = scope.currentPage;
      scope.page1 = scope.page3 - 2;
      scope.page2 = scope.page3 - 1;
    } else {
      scope.page1 = scope.currentPage - 1;
      scope.page2 = scope.currentPage;
      scope.page3 = scope.currentPage + 1;
    }
    if(scope.page3 > totalPages)
    {
      scope.page3 = 0; //Make it invisible
    }
    if(scope.page2 > totalPages)
    {
      scope.page2 = 0; //Make it invisible
    }
  };

  /**
   *  SIMPLE PAGINATION
   */
/*
  this.updateOffsetSimple = function (scope) {
    scope.offsetEnd = Math.min(scope.offset + parseInt(scope.resultPerPage), scope.totalRecords);
    scope.currentPage = (scope.offset / scope.resultPerPage) + 1;
  };*/
});

opaApp.service("HomeService", function () {
  this.MAX_LENGTH_CITATION = 25;

});

opaApp.service("SearchFilters", function ($filter) {

  this.removeParenthesis = function (text) {
    text = String(text); //In case text gets automatically converted to a number.
    text = text.replace("(", "");
    text = text.replace(")", "");
    return text;
  };

  this.getNameFromValue = function (array, value) {
    var match = $filter('filter')(array, {Value: value});
    if (match.length > 0) {
      return match[0].Name;
    } else {
      return "";
    }
  };

  this.FacetNames = {
    DATA_SOURCE: "Data Source",
    LOCATION: "Location",
    LEVEL: "Level of Description",
    DATE: "Date",
    MATERIAL_TYPE: "Type of Materials",
    //Fallback
    ARCHIVES_WEB: "Archives Web",
    AUDIO_VISUAL_ITEM: "Item Audio/Visual",
    DIGITAL_OBJ: "Digital Objects",
    FILE_FORMAT: "File Format",
    PHOTO: "Photographs and other Graphic Materials",
    PRESIDENTIAL_WEB: "Presidential Web",
    RECORD_GROUP: "Record Group",
    TITLE_ITEM: "Title",
    GEOGRAPHIC_REFERENCES_ITEM: "Geographic References",
    RECORD_GROUP_NUMBER_ITEM: "Record Group Number",
    COLLECTION_IDENTIFIER_ITEM: "Collection Identifier",
    CREATOR_ITEM: "Creator",
    DESCRIPTION_IDENTIFIER_ITEM: "Description Identifier",
    ARCHIVAL_PERSON_OR_ORGANIZATION_NAME_ITEM: "Archival Description Name",
    AUTHORITY_PERSON_OR_ORGANIZATION_NAME_ITEM: "Authority Name",
    TAGS_KEYWORDS: "Description Tags",
    MACHINE_TAGS: "Machine Tags",
    DESCRIPTION_START_YEAR_ITEM: "Description Year: From ",
    DESCRIPTION_END_YEAR_ITEM: "Description Year: To ",
    AUTHORITY_START_YEAR_ITEM: "Authority Year: From ",
    AUTHORITY_END_YEAR_ITEM: "Authority Year: To ",
    ANCESTOR_NAID: "Ancestor NARA-ID",
    TAG_EXACT: "Description Tag",
    OBJ_TAG_EXACT: "Object Tag",
    ALL_AUTHORITIES_IDS: "All Authorities IDs",
    CONTRIBUTOR_ID: "Contributor ID",
    CREATOR_IDS: "Creator IDs",
    SUBJECT_IDS: "Subject IDs",
    DONOR_IDS: "Donor IDs",
    BEGIN_CONGRESS: "Begin Congress",
    END_CONGRESS: "End Congress",
    CONGRESS_RANGE: "Congress Range",
    BEGIN_DATE: "Begin Date",
    END_DATE: "End Date",
    EXACT_DATE: "Exact Date",
    RECURRING_MONTH: "Recurring Date - Month",
    RECURRING_DAY: "Recurring Date - Day",
    OPEN_MONTH: "Anniversary Month",
    OPEN_DAY: "Anniversary Day",
    PERSONAL_REFERENCE_IDS: "Personal Reference IDs",
    RECORD_GROUP_COLLECTION_ID: "Record Group Number / Collection ID"
  };

  this.SourceItems = function () {
    return [
      {
        Name: "Archival Descriptions",
        Value: "descriptions"
      },
      {
        Name: "Archives.gov",
        Value: "archives.gov"
      },
      {
        Name: "Authority Records",
        Value: "authority"
      },
      {
        Name: "Archival Descriptions with Digital Objects",
        Value: "online"
      },
      {
        Name: "Presidential Libraries",
        Value: "presidential"
      },
      {
        Name: "Presidential/Vice Presidential Electronic Records",
        Value: "opt6"
      },
      {
        Name: "Web Pages",
        Value: "archives.gov"
      }
    ];
  };
  this.TypeArchivalMaterialsList = function () {
    return [
      {
        Name: "Architectural and Engineering Drawings",
        Value: "drawings"
      },
      {
        Name: "Artifacts",
        Value: "artifacts"
      },
      {
        Name: "Data Files",
        Value: "datafiles"
      },
      {
        Name: "Maps and Charts",
        Value: "mapsandcharts"
      },
      {
        Name: "Moving Images",
        Value: "movingimages"
      },
      {
        Name: "Photographs and Other Graphic Materials",
        Value: "photographsandgraphics"
      },
      {
        Name: "Sound Recordings",
        Value: "sound"
      },
      {
        Name: "Textual Records",
        Value: "text"
      },
      {
        Name: "Web Pages",
        Value: "web"
      }
    ];
  };
  this.LevelOfDescriptions = function () {
    return [
      {
        Name: "Record Group",
        Value: "recordgroup",
        Tooltip: "Overall grouping of archival records created by major government entity.  Each record group is designated by a record group number."
      },
      {
        Name: "Collection",
        Value: "collection",
        Tooltip: "An accumulation of documents brought together on the basis of a shared characteristic.  Each collection is designated by a collection identifier."
      },
      {
        Name: "Series",
        Value: "series",
        Tooltip: "Archival records created and used together for a specific purpose during a specific time period."
      },
      {
        Name: "File Unit",
        Value: "fileunit",
        Tooltip: "Several archival records within a series that are grouped together, often in a file folder or volume."
      },
      {
        Name: "Item",
        Value: "item",
        Tooltip: "A single record within a group of archival materials.  This also includes audio-visual items."
      }
    ];
  };
  this.FileFormat = function () {
    return [
      {
        Name: "ASCII Text",
        Value: "text/plain"
      },
      {
        Name: "Audio Visual (Real Media Video Stream)",
        Value: "application/vnd.rn-realmedia"
      },
      {
        Name: "Audio Visual File (AVI)",
        Value: "video/x-msvideo"
      },
      {
        Name: "Audio Visual File (MOV)",
        Value: "video/quicktime"
      },
      {
        Name: "Audio Visual File (MP4)",
        Value: "video/mp4"
      },
      {
        Name: "Audio Visual File (WMV)",
        Value: "video/x-ms-wmv"
      },
      {
        Name: "Image (BMP)",
        Value: "image/bmp"
      },
      {
        Name: "Image (GIF)",
        Value: "image/gif"
      },
      {
        Name: "Image (JPG)",
        Value: "image/jpeg"
      },
      {
        Name: "Image (TIFF)",
        Value: "image/tiff"
      },
      {
        Name: "MS Excel Spreadsheet",
        Value: "application/excel"
      },
      {
        Name: "Microsoft PowerPoint Document",
        Value: "application/mspowerpoint"
      },
      {
        Name: "Microsoft Word Document",
        Value: "application/msword"
      },
      {
        Name: "Microsoft Write Document",
        Value: "application/mswrite"
      },
      {
        Name: "Portable Document File (PDF)",
        Value: "application/pdf"
      },
      {
        Name: "Compressed file (ZIP)",
        Value: "application/zip"
      },
      {
        Name: "Sound File (MP3)",
        Value: "audio/mpeg3"
      },
      {
        Name: "Sound File (WAV)",
        Value: "audio/x-wav"
      },
      {
        Name: "World Wide Web Page",
        Value: "text/html"
      }
    ];
  };

  this.Location = function () {
    this.ArchivalMaterialsLocation = [
      {
        Name: "William J. Clinton Library",
        Value: "1"
      },
      {
        Name: "Dwight D. Eisenhower Library",
        Value: "2"
      },
      {
        Name: "Franklin D. Roosevelt Library",
        Value: "3"
      },
      {
        Name: "George Bush Library",
        Value: "4"
      },
      {
        Name: "Gerald R. Ford Library",
        Value: "5"
      },
      {
        Name: "Gerald R. Ford Museum",
        Value: "6"
      },
      {
        Name: "Herbert Hoover Library",
        Value: "7"
      },
      {
        Name: "Harry S. Truman Library",
        Value: "8"
      },
      {
        Name: "Jimmy Carter Library",
        Value: "9"
      },
      {
        Name: "John F. Kennedy Library",
        Value: "10"
      },
      {
        Name: "Lyndon Baines Johnson Library",
        Value: "11"
      },
      {
        Name: "Richard Nixon Library - College Park",
        Value: "12"
      },
      {
        Name: "Ronald Reagan Library",
        Value: "13"
      },
      {
        Name: "National Archives at Boston",
        Value: "14"
      },
      {
        Name: "National Archives at New York",
        Value: "15"
      },
      {
        Name: "National Archives at Philadelphia",
        Value: "17"
      },
      {
        Name: "National Archives at Atlanta",
        Value: "18"
      },
      {
        Name: "National Archives at Chicago",
        Value: "19"
      },
      {
        Name: "National Archives at Kansas City",
        Value: "20"
      },
      {
        Name: "National Archives at Fort Worth",
        Value: "21"
      },
      {
        Name: "National Archives at Denver",
        Value: "22"
      },
      {
        Name: "National Archives at Riverside",
        Value: "23"
      },
      {
        Name: "National Archives at San Francisco",
        Value: "24"
      },
      {
        Name: "National Archives at Anchorage",
        Value: "25"
      },
      {
        Name: "National Archives at Seattle",
        Value: "26"
      },
      {
        Name: "National Personnel Records Center - Civilian Personnel Records",
        Value: "27"
      },
      {
        Name: "National Personnel Records Center - Military Personnel Records",
        Value: "28"
      },
      {
        Name: "National Archives at College Park – Cartographic",
        Value: "29"
      },
      {
        Name: "National Archives at College Park - Motion Pictures",
        Value: "30"
      },
      {
        Name: "National Archives at College Park - Still Pictures",
        Value: "31"
      },
      {
        Name: "National Archives - Washington, DC - Archives I Textual Reference",
        Value: "32"
      },
      {
        Name: "National Archives at College Park - Textual Reference",
        Value: "33"
      },
      {
        Name: "National Archives at College Park – FOIA",
        Value: "34"
      },
      {
        Name: "Center for Legislative Archives",
        Value: "36",
        HasCongressionalRecords: true
      },
      {
        Name: "National Archives at College Park – Electronic Records",
        Value: "37"
      },
      {
        Name: "Library of Congress, Prints and Photographs Division (an affiliated archives)",
        Value: "38"
      },
      {
        Name: "National Park Service, Yellowstone National Park Archives (an affiliated archives)",
        Value: "39"
      },
      {
        Name: "New Mexico Commission of Public Records, State Records Center and Archives (an affiliated archives)",
        Value: "40"
      },
      {
        Name: "Oklahoma Historical Society (an affiliated archives)",
        Value: "41"
      },
      {
        Name: "Pennsylvania Historical and Museum Commission, State Archives (an affiliated archives)",
        Value: "42"
      },
      {
        Name: "United States Military Academy Archives (an affiliated archives)",
        Value: "43"
      },
      {
        Name: "United States Naval Academy, William W. Jeffries Memorial Archives (an affiliated archives)",
        Value: "44"
      },
      {
        Name: "Presidential Materials Division",
        Value: "48",
        HasPresidentialElectronicRecords: "true"

      },
      {
        Name: "National Archives at St. Louis",
        Value: "50"
      },
      {
        Name: "Richard Nixon Library",
        Value: "51"
      },
      {
        Name: "George W. Bush Library",
        Value: "53",
        HasPresidentialElectronicRecords: "true"
      },
      {
        Name: "U.S. Government Printing Office (an affiliated archives)",
        Value: "54"
      },
      {
        Name: "University of North Texas Libraries (an affiliated archives)",
        Value: "57"
      },
      {
        Name: "Barack Obama Presidential Library",
        Value: "53023101"
      }
    ];
    return this.ArchivalMaterialsLocation;
  };
});

opaApp.service("ReferrerService", function ($location) {
  var localhost = window.location.protocol + '//' + window.location.host;
  this._referrer = '';
  this._target = '';
  this._previousReferrer = '';

  this.setReferrer = function(current, next){
    this._previousReferrer = this._referrer;
    this._referrer = encodeURIComponent(current.replace(localhost, ''));
    this._target = encodeURIComponent(next.replace(localhost, ''));
  };

  this.getReferrer = function(){
    return {
      previousReferrer: decodeURIComponent(this._previousReferrer),
      referrer: decodeURIComponent(this._referrer),
      target: decodeURIComponent(this._target)
    };
  };

  this.navigateBack = function(){
    var referrer = this.getReferrer();
    if (referrer.referrer && referrer.target && referrer.referrer !== referrer.target) {
      $location.url(referrer.referrer);
    } else {
      $location.path('/');
    }
  };
  this.navigateForward = function(){
    var referrer = this.getReferrer();
    if (referrer.referrer && referrer.target && referrer.referrer !== referrer.target) {
      $location.url(referrer.target);
    } else {
      $location.path('/');
    }
  };
});
