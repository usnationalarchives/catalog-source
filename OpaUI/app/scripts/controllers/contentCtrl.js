opaApp.controller('contentCtrl', function ($scope, $filter, $location, $rootScope, $log,$timeout, Auth, TagService, exportSvc, visorSvc, ErrorCodesSvc, Content, ContentWithSearchQuery, OpaUtils, ListsService, Lists, $routeParams, $window, searchSvc,commentsService) {
  //Controller Variables
  /**
   * The first function that is run when the controller loads
   */
  var nextNaId = '';
  var prevNaId = '';
  var videoThumbPath;
  var pdfThumbPath;
  var audioThumbPath;
  var docThumbPath;
  var excelThumbPath;
  var presentationThumbPath;
  var defaultThumbPath;
  var objects;
  var objectsImages;
  var paramsObj;
  var MAX_THUMBS;
  var shortContent;

  /**
   * Defines $scope variables for UI bindings
   */
  var initScopeModel = function () {
    //Services
    $scope.Auth = Auth;
    $scope.searchSvc = searchSvc;
    $scope.exportSvc = exportSvc;
    $scope.commentsService = commentsService;

    //Model
    $scope.tagText = "";
    $scope.maxThumbs = MAX_THUMBS;
    $scope.result = {
      objects: {},
      description: ""
    };
    $scope.DDI = false;
    $scope.thumbs = [];
    $scope.docs = [];
    $scope.totalObjects = 0;
    $scope.offlineContent = false;
    $scope.currentIndex = 0;
    $scope.itemTotal = 1;
    $scope.technicalDocs = [];
    $scope.electronicRecords = [];
    $scope.allDocs = false;
    $scope.docVisor = false;
    $scope.objectNotFound = false;
    $scope.ContetDetails = true; //todo: fix spelling in referenced xsls
    $scope.toggle = OpaUtils.toggle;
    //Tags array Variable
    $scope.Tags = [];
    $scope.naId = $routeParams.naId || searchSvc.selectedNaId;
    $scope.isLoggedIn = Auth.isLoggedIn();
    //Variable use to show or hide elements in the UI
    $scope.showSideBar = true;
    //Variable with the text for the label for when the sidebar is collapsible
    $scope.label = "Close";
    //Variable to for the show more/less tags
    $scope.showAllTags = false;
    //Variable used to change the class of angle icon in the mobile header
    $scope.expanded = false;
    //Variable used to show or hide the panel with the meta data (More info) for mobile
    $scope.expandedMoreInfo = false;
    $scope.getDownloadURL = exportSvc.getURL;
    $scope.currentPage = 0;
    $scope.comments = {};
    $scope.commentId = $routeParams.commentId;

    $scope.shortContent = "";
  };

  /**
   * Initializes the controller variables.
   * Instead of images path we use class from glyphicons for file type images
   */
  var initControllerVariables = function () {
    MAX_THUMBS = 30;
    videoThumbPath = 'images/video.svg';
    audioThumbPath = 'images/audio.svg';
    pdfThumbPath = 'filetypes filetypes-pdf';
    docThumbPath = 'filetypes filetypes-doc';
    excelThumbPath = 'filetypes filetypes-xls';
    presentationThumbPath = 'filetypes filetypes-ppt';
    defaultThumbPath = 'glyphicons glyphicons-file';
    objects = [];
    objectsImages = [];
    paramsObj = null;
  };

  /**
   * Forces a close of the contribution workspace
   */
  var closeWorkspace = function () {
    if (!OpaUtils.isOpenWorkspace()) {
      $('.modal-backdrop').remove();
    }
  };

  /**
   * Changes the current media (multi as multimedia as Images, audio and video)
   */
  var changeMediaMulti = function (media, index) {
    $scope.showVisor = true;
    $scope.docVisor = false;
    visorSvc.currentThumbnailURL = null;

    var mediaKey = '@path';
    if ('@renditionBaseUrl' in media) {
      mediaKey = '@url';
    }

    var file = {
      mime: media.file['@mime']
    };
    if (index > $scope.thumbs.length) {
      $scope.loadAll();
    }
    try {
      visorSvc.currentThumbnailURL = OpaUtils.getStoragePath($scope.naId, false, media.file[mediaKey]);
    } catch (e) {
    }
    if (media.imageTiles) {
      file.path = OpaUtils.getStoragePath($scope.naId, false, media.imageTiles[mediaKey]);
      file.ext = 'DZI';
    } else {
      file.path = OpaUtils.getStoragePath($scope.naId, false, media.file[mediaKey]);
      file.ext = OpaUtils.getFileExt(media.file['@path']);
      file.stream = media.file['@stream'];
    }
    var objectId = media['@id'];
    visorSvc.total = objects.length;
    visorSvc.changeMedia($scope.naId, file, index, objectId);
  };

  /**
   * Changes the current doc
   */
  var changeMediaDoc = function (media, index) {
    $scope.showVisor = true;
    $scope.docVisor = true;
    var mediaKey = '@path';
    if ('@renditionBaseUrl' in media) {
      mediaKey = '@url';
    }
    var file = {
      path: OpaUtils.getStoragePath($scope.naId, false, media.file[mediaKey]),
      ext: OpaUtils.getFileExt(media.file['@path']),
      mime: media.file['@mime']
    };
    var objectId = media['@id'];
    visorSvc.total = objects.length;
    visorSvc.changeMedia($scope.naId, file, index, objectId);
  };

  /**
   * updates the selected index
   */
  var updateCurrentIndex = function () {
    $scope.currentIndex = visorSvc.index;
    $scope.itemTotal = visorSvc.total || 1;
  };

  var initVisor = function () {
    //sets the function used to change media from the visor
    visorSvc.media.fullPath = null;
    visorSvc.getMediaFromCtrl = $scope.changeMedia;
    visorSvc.registerIndexObserverCallback(updateCurrentIndex);
  };

  /**
   * Extracts the info for the file
   * @param fileName
   * @returns {{fullName: *, name: (string|*), ext: (string|*)}}
   */
  var getFileInfo = function (fileName) {
    //TODO: check if needed
    return {
      fullName: fileName,
      name: fileName.substr(fileName.lastIndexOf('/') + 1),
      ext: fileName.substr(fileName.lastIndexOf('.') + 1).toLowerCase()
    };
  };

  /**
   * Sets the corresponding thumbnail image
   * and separates the media from the documents
   */
  var setThumbnails = function () {
    objectsImages = [];
    angular.forEach(objects, function (value) {
      var mediaKey = '@path';
      if ('@renditionBaseUrl' in value) {
        mediaKey = '@url';
      }

      if ($.isArray(value.file)) {
        value.file = value.file[0];
      }

      var mime = value.file['@mime'];

      if (/image/.test(mime)) {
        if (value.thumbnail) {
          value.thumbnail.thumbnailFile = OpaUtils.getStoragePath($scope.naId, false, value.thumbnail[mediaKey]);
        }
        else {
          value.thumbnail = {thumbnailFile: defaultThumbPath};
        }
        value.mediaType = 'multi';
        objectsImages.push(value);
      } else if (/pdf/.test(mime)) {
        value.thumbnail = {thumbnailFile: pdfThumbPath};
        value.mediaType = 'doc';
        $scope.docs.push(value);
      } else if (/video/.test(mime)) {
        value.thumbnail = {thumbnailFile: videoThumbPath};
        value.mediaType = 'multi';
        objectsImages.push(value);
      } else if (/audio/.test(mime)) {
        value.thumbnail = {thumbnailFile: audioThumbPath};
        value.mediaType = 'multi';
        objectsImages.push(value);
      } else if (/word/.test(mime) || /write/.test(mime)) {
        value.thumbnail = {thumbnailFile: docThumbPath};
        value.downloadOnly = true;
        value.href = OpaUtils.getStoragePath($scope.naId, false, value.file[mediaKey]);
        value.mediaType = 'doc';
        $scope.docs.push(value);
      } else if (/excel/.test(mime)) {
        value.thumbnail = {thumbnailFile: excelThumbPath};
        value.downloadOnly = true;
        value.href = OpaUtils.getStoragePath($scope.naId, false, value.file[mediaKey]);
        value.mediaType = 'doc';
        $scope.docs.push(value);
      } else if (/powerpoint/.test(mime)) {
        value.thumbnail = {thumbnailFile: presentationThumbPath};
        value.downloadOnly = true;
        value.href = OpaUtils.getStoragePath($scope.naId, false, value.file[mediaKey]);
        value.mediaType = 'doc';
        $scope.docs.push(value);
      } else if (/text/.test(mime)) {
        value.thumbnail = {thumbnailFile: defaultThumbPath};
        value.downloadOnly = true;
        value.href = OpaUtils.getStoragePath($scope.naId, false, value.file[mediaKey]);
        value.mediaType = 'doc';
        $scope.docs.push(value);
      } else {
        value.thumbnail = {thumbnailFile: defaultThumbPath};
        value.downloadOnly = true;
        if (value.file['@path']) {
          value.href = OpaUtils.getStoragePath($scope.naId, false, value.file[mediaKey]);
        }
        else {
          value.href = OpaUtils.getStoragePath($scope.naId, false, value.file['@name']);
        }
        value.mediaType = 'doc';
        $scope.docs.push(value);
      }
    });
  };

  /**
   * Error handler of Content Details $get
   * @param error
   */
  var errorFn = function (error) {
    $location.path('contentNotFound');
    if (!OpaUtils.checkForAPIError(error)) {
      //check for other errors block
      $log.error(error);
    }
  };

  /**
   * Success handler of Content Details $get.
   * @param data
   */
  var successFn = function (data) {
    $scope.noResults = false;
    if (data.opaResponse) {
      if (typeof data.opaResponse['@total'] !== 'undefined' && !isNaN(parseInt(data.opaResponse['@total']))) {
        $scope.totalResults = parseInt(data.opaResponse['@total']);
      } else {
        $scope.totalResults = 0;
      }
      if (data.opaResponse['@nextNaId']) {
        nextNaId = data.opaResponse['@nextNaId'];
      } else {
        nextNaId = "";
      }
      if (data.opaResponse['@prevNaId']) {
        prevNaId = data.opaResponse['@prevNaId'];
      } else {
        prevNaId = "";
      }

      $scope.result = data.opaResponse.content;
      //JQuery Hack to decode html entities from string, for example convert &apos; to '
      document.title = $rootScope.title = $scope.title = $('<textarea />').html(data.opaResponse['@title']).text();

      $scope.shortContent = $('<textarea />').html(data.opaResponse['@shortContent']).text();

      if ($scope.shortContent) {
        $rootScope.metaDescription = $scope.shortContent;
        $rootScope.metaKeywords = $scope.shortContent;
      } else {
        $rootScope.metaDescription = '';
        $rootScope.metaKeywords = '';
      }

      var url = $location.protocol() + '://' + $location.host();
      if (!(($location.port() === 80 && $location.protocol() === 'http') || ($location.port() === 443 && $location.protocol() === 'https'))) {
        url += ':' + $location.port();
      }
      url += $location.url();
      addthis_share.url = url;
      addthis_share.title = $rootScope.title;
      addthis_share.description = $rootScope.metaDescription;

      $scope.opaId = data.opaResponse['@opaId'];
      if ($scope.result && $scope.result.description) {
        $scope.html = $scope.result.description;
      } else if ($scope.result && $scope.result.authorities) {
        $scope.html = $scope.result.authorities;
        $scope.expanded = true;
      } else {
        if (searchSvc.query !== "") {
          retryContentDetail();
        } else {
          $location.path('contentNotFound');
        }
      }
      if (searchSvc.isPreview) {
        searchSvc.isPreview = false;
        $scope.start();
      }
    } else {
      $location.path('contentNotFound');
      $log.error("Invalid content detail: data.opaResponse is missing.");
    }
  };

  var retryContentDetail = function(){
    var contentDetail = new Content();
    searchSvc.query = "";
    searchSvc.searchURL = "";
    $window.sessionStorage.removeItem("sp");
    contentDetail.$get({'naid': $scope.naId}, successFn, errorFn);
  };

  var navigateTo = function (naraId) {
    if (naraId) {
      $window.sessionStorage.setItem("sr", $scope.currentPage - 1);
      $window.sessionStorage.setItem("naId", naraId);
      $location.path("/id/" + naraId);
    } else {
      navigateToCurrentPageNumber();
    }
  };

  var navigateToCurrentPageNumber = function () {
    var contentDetail;
    var spOffset;
    var spRows;
    if (paramsObj && typeof paramsObj.offset !== 'undefined') {
      spOffset = paramsObj.offset;
      spRows = paramsObj.rows;
      paramsObj.rows = 1;
      paramsObj.offset = $scope.currentPage - 1;
      $window.sessionStorage.setItem("sr", paramsObj.offset);
      contentDetail = new ContentWithSearchQuery();
      contentDetail.$get(paramsObj, function (data) {
        if (data.opaResponse && typeof data.opaResponse['@naId'] !== 'undefined' && data.opaResponse['@naId'] !== null) {
          $location.path("/id/" + data.opaResponse['@naId']);
          $window.sessionStorage.setItem("naId", data.opaResponse['@naId']);
        }
      }, errorFn);
      paramsObj.offset = spOffset;
      paramsObj.rows = spRows;
    }
  };

  var init = function () {
    var naIdFromSessionStorage;
    var highlightOff = false;
    var contentDetail;
    var spOffset;
    var spRows;
    var searchParams; //search params used in the search results page
    var historyStateObj = {};

    initControllerVariables();
    initScopeModel();
    closeWorkspace();
    initVisor();

    //If the entry point is the Url and has a commentId, or it is a contribution workspace, load the content detail w/o navigation
    if (!$scope.commentId && !$routeParams.objectIndex && !$routeParams.contributionType && !searchSvc.isPreview) {

      //If present, read the history.state object -> The user hit the browser's Back/Next buttons.
      if (OpaUtils.supports_history_api() && $window.history.state) {
        $.extend(historyStateObj, $window.history.state);
        if (historyStateObj.naId && historyStateObj.naId === $scope.naId) {
          if (typeof historyStateObj.sr !== 'undefined') {
            $window.sessionStorage.setItem("sr", historyStateObj.sr);
          }
          else {
            $window.sessionStorage.removeItem("sr");
          }
          $window.sessionStorage.setItem("naId", historyStateObj.naId);
          delete historyStateObj.naId;
          delete historyStateObj.sr;
          $window.sessionStorage.setItem("sp", angular.toJson(historyStateObj));
        }
      }

      //Compare session storage naId with current URL NaId
      if ($scope.naId) {
        naIdFromSessionStorage = $window.sessionStorage.getItem("naId");
        //If session storage naId is different to the URL's naId then discard the session storage one.
        if (naIdFromSessionStorage && $scope.naId !== naIdFromSessionStorage) {
          $window.sessionStorage.clear();
        }
        else {
          $window.sessionStorage.setItem("naId", $scope.naId);
        }
      }

      //Read the selected result ($routeParams.sr)
      if (typeof $routeParams.sr !== 'undefined') {
        $scope.currentPage = parseInt($routeParams.sr) + 1;
        $window.sessionStorage.setItem("sr", $routeParams.sr);
        $location.search("sr", null);
        $location.replace();
      }
      else {
        $scope.currentPage = parseInt($window.sessionStorage.getItem("sr")) + 1;
      }
      $scope.currentPage = isNaN($scope.currentPage) ? 0 : $scope.currentPage;

      if ($routeParams.sp) {
        searchParams = decodeURIComponent($routeParams.sp);
        $window.sessionStorage.setItem("sp", searchParams);
        $location.search("sp", null);
        $location.replace();
      }
      else {
        searchParams = $window.sessionStorage.getItem("sp");
      }

      paramsObj = searchParams ? angular.fromJson(searchParams) : null;
      if (paramsObj) {
        highlightOff = typeof paramsObj.highlight !== 'undefined' && paramsObj.highlight !== null && paramsObj.highlight === false;
        if (paramsObj.q) {
          searchSvc.setQuery(paramsObj.q);
        }

        //Build searchURL for link of "Back to search results"
        searchSvc.searchURL = $location.protocol() + "://" + $location.host() + ":" + $location.port() + "/search?";
        angular.forEach(paramsObj, function (value, key) {
          searchSvc.searchURL += key + ((typeof value !== 'undefined' && value !== null) ? '=' + value : '') + "&";
        });

        //Save browser history state(IE >= 10)
        if (OpaUtils.supports_history_api()) {
          $.extend(historyStateObj, paramsObj);
          if ($scope.currentPage) {
            historyStateObj.sr = $scope.currentPage - 1;
          }
          historyStateObj.naId = $scope.naId;
          $window.history.replaceState(historyStateObj, document.title, '/id/' + $scope.naId);
        }

        if ($scope.currentPage) {
          if (typeof paramsObj.offset === 'undefined' || paramsObj.offset === null) {
            paramsObj.offset = searchSvc.defaultSearchParams.offset;
          }
          spOffset = paramsObj.offset;
          paramsObj.offset = $scope.currentPage - 1;
          spRows = paramsObj.rows;
          paramsObj.rows = 1;
          contentDetail = new ContentWithSearchQuery();
        }
      }
    }
    $scope.getLists();
    $scope.clearCheckboxes();
    visorSvc.currentTab = 1;

    if (!$scope.currentPage || !paramsObj) {
      contentDetail = new Content();
      if (highlightOff) {
        paramsObj = {'naid': $scope.naId, 'searchTerm': ""};
      }
      else {
        paramsObj = {'naid': $scope.naId, 'searchTerm': searchSvc.getQuery()};
      }
    }

    contentDetail.$get(paramsObj, successFn, errorFn);

    document.body.className = document.body.className.replace("modal-open", "");

    if (typeof paramsObj.offset !== 'undefined' && paramsObj.offset !== null) {
      paramsObj.offset = spOffset;
      paramsObj.rows = spRows;
    }
  };

  //Scope functions

  /**
   * obtains the index from a given object
   * @param object
   * @returns {number|*}
   */
  $scope.objectIndex = function (object) {
    return objects.indexOf(object);
  };

  /**
   * Changes the current media
   * @param index
   */
  $scope.changeMedia = function (index) {
    var media = objects[index];
    if (media.mediaType === 'multi') {
      changeMediaMulti(media, index);
      visorSvc.currentObject = media;
    }
    else if (media.mediaType === 'doc') {
      changeMediaDoc(media, index);
      visorSvc.currentObject = media;
    }
  };

  /**
   * Opens the workspace for the clicked image displaying
   * the contribution tab with the most recent contribution
   * @param index
   */
  $scope.showWorkspace = function (index) {
    $scope.changeMedia(index);
    visorSvc.openWorkspace();
  };

  /**
   * Function to show/hide the sidebar
   * */
  $scope.hideShowSideBar = function () {
    $scope.showSideBar = !$scope.showSideBar;
    if ($scope.label === "Close") {
      $scope.label = "Open";
    } else {
      $scope.label = "Close";
    }
  };

  $scope.toogleHeader = function () {
    $scope.expanded = !$scope.expanded;
  };

  $scope.login = function () {
    $scope.toLogin = true;
  };

  //This function add a new tag
  $scope.addTag = function () {

    if (!$scope.tagText.trim()) {
      OpaUtils.showErrorGlobalNotification("Please enter text for the tag");
    }
    else {
      if (Auth.isLoggedIn()) {
        TagService.addTag($scope.naId, $scope.tagText).then(
          function () {
            $scope.tagText = "";
            $scope.getAllTags();
          },
          function (error) {
            if (!OpaUtils.checkForAPIError(error)) {
              //check for other errors block
//              if (Auth.checkIsNotLoggedIn(error)) {
//                if (Auth.isLoggedIn())
//                  Auth.logout();
//                OpaUtils.showMessageModalLogin();
//              }
//              else
              OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
            }
          }
        );
      }
      else {
        OpaUtils.showMessageModalLogin();
      }
    }
  };

  //This function delete a user tag
  $scope.deleteTag = function (text, tooltipId) {
    TagService.deleteTag($scope.naId, text).then(
      function () {
        $("#" + tooltipId).data("kendoTooltip").destroy();
        $scope.getAllTags();
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
//          if (Auth.checkIsNotLoggedIn(error)) {
//            if (Auth.isLoggedIn()) {
//              Auth.logout();
//            }
//            $location.path("/login");
//          }
        }
      }
    );
  };

  //This function returns all the tags
  $scope.getAllTags = function () {

    TagService.getAllTags($scope.naId).then(
      function (promise) {
        if (promise.opaResponse) {
          //Patch to check if server sent an array or a single object.
          $scope.Tags = angular.isArray(promise.opaResponse.tags.tag) ? promise.opaResponse.tags.tag : [promise.opaResponse.tags.tag];
          TagService.createTooltips($scope.Tags, "tag");
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.NO_TAGS_EXIST)) {
            $scope.Tags = [];
          }
          else {
            $log.error("Alert " + error.data.opaResponse.error.description);
          }
        }
      }
    );
  };

  $scope.checkUserTag = function (tag) {
    return TagService.checkUserTag(tag);
  };

  /**
   * Loads all thumbnails
   */
  $scope.loadAll = function () {
    $scope.maxThumbs = $scope.totalObjects;
    $scope.thumbs = objectsImages;
  };

  /**
   * Loads more thumbnails
   */
  $scope.loadMore = function () {
    $scope.maxThumbs += 30;
    $scope.thumbs = objectsImages.slice(0, $scope.maxThumbs);
  };

  $scope.start = function () {
    var contributionType;
    $scope.loadObjects();
    $scope.getAllTags();
    $scope.comments.naId = $scope.naId;
    if ($routeParams.objectIndex) {
      visorSvc.naId = $scope.naId;
      visorSvc.index = $routeParams.objectIndex - 1;
      if (!$scope.totalObjects || (visorSvc.index > $scope.totalObjects)) {
        $scope.objectNotFound = true;
        visorSvc.index = 0;
      }

      if (!$scope.objectNotFound) {
        visorSvc.notifyIndexObservers();
        if (objects[visorSvc.index]) {
          visorSvc.objectId = objects[visorSvc.index]['@id'];
          $scope.changeMedia(visorSvc.index);
        }
        contributionType = $routeParams.contributionType;
        switch (contributionType) {
          case 'tag':
            visorSvc.currentTab = 1;
            break;
          case 'comment':
            visorSvc.currentTab = 2;
            break;
          case 'transcription':
            visorSvc.currentTab = 3;
            break;
          case 'translation':
            visorSvc.currentTab = 4;
            break;
        }
        if (OpaUtils.isPhone()) {
          visorSvc.loadWorkspace(false, true);
        } else {
          visorSvc.loadWorkspace(false, false);
        }
      }
      else {
        OpaUtils.showMessageModal("Not Found", "Contribution not found");
        $location.search('contributionType', null).path("/id/" + $routeParams.naId, false);
      }
    }
  };

  $scope.getLists = function () {
    if (!Auth.isLoggedIn()) {
      return;
    }
    ListsService.GetAllLists().then(function (promise) {
        $scope.userList = promise.opaResponse.userLists.userList;
      },
      function (error) {
        $scope.userList = {};
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      });
  };

  $scope.createNewList = function () {
    var list = new Lists();
    if (!$scope.newListName) {
      $scope.error = "Please enter a new list name";
      return;
    }
    else if ($scope.newListName.length > 50) {
      $scope.error = "Text size of field '" + $scope.newListName + "' (" + $scope.newListName.length + "]) is greater than field size: 50";
      return;
    }
    $scope.newListName = $filter('removeWordCharacters')($scope.newListName);
    list.$create({'listname': $scope.newListName},
      function (data) {
        if (data.opaResponse) {
          $scope.selectedList = $scope.newListName;
          $scope.addToList();
          $scope.getLists();
          $('#createList').modal('hide');
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          $scope.error = error.data.opaResponse.error.description;
        }
      }
    );
  };

  $scope.addToList = function () {
    var list = new Lists({list: $scope.selectedList});
    if ($scope.opaId) {
      list.$addToList({'what': $scope.opaId},
        function (data) {
          if (data.opaResponse) {
            var message = "Record added to " + data.opaResponse.header.request.listName + " list.";
            OpaUtils.showMessageModal($scope.title, message);
            $("#listscollapse").collapse('hide');
          }
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            OpaUtils.showMessageModal("Cannot add the record to the list", error.data.opaResponse.error.description);
          }
        }
      );
    } else {
      OpaUtils.showMessageModal("Cannot add the record to the list", "The record does not have an OPA id");
      $("#listscollapse").collapse('hide');
    }
  };

  $scope.last = function () {
    return !!($scope.currentPage === 0 || $scope.currentPage >= $scope.totalResults);

  };

  $scope.increasePageNumber = function () {
    if ($scope.last()) {
      return;
    }
    $scope.currentPage = $scope.currentPage + 1;
    if (nextNaId !== $scope.naId) {
      navigateTo(nextNaId);
    }
    else {
      navigateTo("");
    }
  };

  $scope.decreasePageNumber = function () {
    if ($scope.currentPage <= 1) {
      return;
    }
    $scope.currentPage = $scope.currentPage - 1;

    if (prevNaId !== $scope.naId) {
      navigateTo(prevNaId);
    }
    else {
      navigateTo("");
    }
  };

  /**
   * Function that executes a search within the content naId
   */
  $scope.searchWithin = function () {
    var params = {
      'f.ancestorNaIds': $scope.naId,
      sort: "naIdSort asc"
    };
    searchSvc.setQuery("*:*");
    searchSvc.setSearchUrl(params);
  };

  /**
   * Executes a search by the given field
   * @param field
   * @param value
   * @param level
   */
  $scope.searchBy = function (field, value, level) {
    var params = {};
    params[field] = value.toString();
    if (level) {
      params['f.level'] = level;
    }
    params.sort = "naIdSort asc";
    searchSvc.searchWithAdditionalParams(params);
  };

  /**
   * Load the objects from the response in order to display them
   */
  $scope.loadObjects = function () {
    if ($scope.result.objects && !$scope.DDI) {
      if ($.isArray($scope.result.objects.objects.object)) {
        objects = $scope.result.objects.objects.object;
        objects.sort(function (a, b) {
          return parseInt(a['@objectSortNum']) - parseInt(b['@objectSortNum']);
        });
      } else {
        objects.push($scope.result.objects.objects.object);
      }
      setThumbnails();
      $scope.totalObjects = objects.length;
      if (objectsImages.length > 0) {
        $scope.offlineContent = false;
        $scope.docVisor = false;
        $scope.thumbs = objectsImages.slice(0, $scope.maxThumbs);
      } else if ($scope.docs.length > 0) {
        $scope.offlineContent = false;
        $scope.docVisor = true;
      }
      var firstObject = objects[0];
      if (!firstObject.downloadOnly) {
        $scope.changeMedia(0);
      }
    } else {
      $scope.offlineContent = true;

      if ($scope.result.objects && $scope.DDI) {

        $scope.offlineContent = false;
        $scope.technicalDocs = [];
        $scope.electronicRecords = [];
        if ($.isArray($scope.result.objects.objects.object)) {

          $scope.result.objects.objects.object.forEach(function (value, key) {
            var mediaKey = '@path';
            if ('@renditionBaseUrl' in value ){
              mediaKey = '@url';
            }
            value.file['@path'] = OpaUtils.getStoragePath($scope.naId, false, value.file[mediaKey]);
            if (value.technicalMetadata) {
              value.technicalMetadata.fileSizeDisplay = OpaUtils.getPretyFileSize(value.technicalMetadata.size);
            }
            if (value.designator === 'Technical Documentation') {
              $scope.technicalDocs.push(value);
            } else {
              $scope.electronicRecords.push(value);
            }
          });
        } else {
          var mediaKey = '@path';
          if ('@renditionBaseUrl' in $scope.result.objects.objects.object ){
            mediaKey = '@url';
          }
          //Updating the object path url
          $scope.result.objects.objects.object.file['@path'] = OpaUtils.getStoragePath($scope.naId, false, $scope.result.objects.objects.object.file[mediaKey]);

          if ($scope.result.objects.objects.object.designator === 'Technical Documentation' && $scope.result.objects.objects.object.display === 'Y') {
            $scope.technicalDocs.push($scope.result.objects.objects.object);
          } else {
            if ($scope.result.objects.objects.object.display === 'Y') {
              $scope.electronicRecords.push($scope.result.objects.objects.object);
            }
          }
        }
      }
    }
  };

  $scope.exportFromContentDetails = function () {
    exportSvc.naid = $scope.naId;
    $('#exportModal').modal('show');
    exportSvc.cleanUp();
  };

  $scope.printFromContentDetails = function () {
    exportSvc.naid = $scope.naId;
    $('#printModal').modal('show');
    exportSvc.cleanUp();
  };

  $scope.clearCheckboxes = function () {
    searchSvc.cleanUpAccordion();
  };

  $scope.getError = function () {
    return $scope.error;
  };

  $scope.decodeURI = function (uri) {
    return decodeURIComponent(uri);
  };

  /**
   * This function returns the text to display for the link of comments shown in the left side box of content details.
   * @returns {string}
   */
  $scope.getTextGoToComments = function(){
    var text = "";
    if(commentsService.commentsCount > 0){
      text = "View all "+ commentsService.commentsCount + " comments";
    }
    else{
      text = "Add a comment...";
    }
    return text;
  };

  $scope.goToComments = function(){
    var x = document.getElementById("comments");
    x.scrollIntoView({block: "start", behavior: "smooth"});
    $timeout(function () {
      document.getElementById("commentHeaderLink").focus();
    }, 250);
  };


  $scope.$on('ngRepeatFinished', function () {

    //Scroll to the comment if it is specified.
    if ($scope.commentId) {
      var divId = 'c' + $scope.commentId;
      //Check if URL is a persistent comment link
      var x = document.getElementById(divId);
      x.scrollIntoView({block: "start", behavior: "smooth"});
      $('#' + divId).animate({backgroundColor: '#FFFFFF'});
    }
  });

  init();
});
