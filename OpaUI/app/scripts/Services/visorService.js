opaApp.service("visorSvc", function ($sce, $location) {
  this.MOBILE_NOT_SUPPORTED = [/avi/, /wmv/];
  this.NOT_SUPPORTED = [/avi/];

  this.wmvPlayers = [];
  this.isWorkspace = false;
  this.currentWMV = "";

  /**
   * this method is binded to the controler that has the media sources
   */
  this.getMediaFromCtrl = function () {
  };

  /**
   * numeric Id for the current object
   * @type {number}
   */
  this.objectId = 0;

  /**
   * variable to know if we're displaying videos or images
   * @type {boolean}
   */
  this.isVideo = false;

  /**
   * variable to know if we're displaying audio
   * @type {boolean}
   */
  this.isAudio = false;

  /**
   * variable to know if we're displaying a PDF
   * @type {boolean}
   */
  this.isPdf = false;

  /**
   * variable to know if we're displaying a wmv
   * @type {boolean}
   */
  this.isWmv = false;

  /**
   * Variable to know if this is a format not supported in mobile
   * @type {boolean}
   */
  this.notFormMobile = false;

  /**
   * Variable to know if this is a unsupported file
   * @type {boolean}
   */
  this.notSupported = false;

  /**
   * Current tab selected on workspace
   */
  this.currentTab = -1;

  /**
   * Current NARA id
   */
  this.naId = null;

  /**
   * Current media object
   */
  this.currentObject = null;

  /**
   * object that contains the media information
   * @type {{path: string, name: string, type: string}}
   */
  this.media = {
    /*path: "",
     name: "",
     type: "",
     fullPath: ''*/
  };

  /**
   * returns the full path of the media
   * @returns {string}
   */
  this.getMediaSource = function () {
    return this.media.fullPath;
  };

  /**
   * returns the full path of the media
   * @returns {string}
   */
  this.getMediaStreamSource = function () {
    return this.media.streamPath;
  };

  /**
   * list of the methods to update the media in the different directives
   * @type {Array}
   */
  var observerCallbacks = [];

  /**
   * list of the methods to update the index in the different directives
   * @type {Array}
   */
  var indexObserverCallbacks = [];

  /*
   * Selected thumbnail URL to retrieve full image
   */
  this.currentThumbnailURL = null;

  /**
   * register an media update callback
   * @param callback
   */
  this.registerObserverCallback = function (callback) {
    observerCallbacks.push(callback);
  };

  /**
   * register an index update callback
   * @param callback
   */
  this.registerIndexObserverCallback = function (callback) {
    indexObserverCallbacks.push(callback);
  };

  /**
   * Executes the media update methods
   */
  this.notifyObservers = function () {
    angular.forEach(observerCallbacks, function (callback) {
      callback();
    });
  };

  /**
   * Executes the index update methods
   */
  this.notifyIndexObservers = function () {
    angular.forEach(indexObserverCallbacks, function (callback) {
      callback();
    });
  };

  /**
   *
   *
   */

  this.openWorkspace = function () {
    this.loadWorkspace(true, false);
    this.orderTabsByDate = true;
  };

  /**
   * Checks if this is a wmv and removes the player from the content details
   * and opens it in the workspace
   */
  this.loadWorkspace = function (fromClick, fromMobile) {
    if (fromClick) {
      //Check if comments url is active
      var newUrl = $location.path();
      if(newUrl.indexOf("comment") !== -1){
        newUrl = newUrl.substring(0, newUrl.indexOf("comment") - 1);
      }
      newUrl += '/' + (this.index + 1) + '/public';
      var tab = 'tag';
      switch (this.currentTab) {
        case 2:
          tab = 'comment';
          break;
        case 3:
          tab = 'transcription';
          break;
      }
      $location.search('contributionType', tab).path(newUrl, false);
    }

    this.removeWMP();
    this.isWorkspace = true;
    if (fromMobile) {
      // Preventing to close the modal click outside the modal or with the ESC key
      $('#workspaceResponsiveModal').modal({
        backdrop: 'static',
        keyboard: false
      });
      $('#workspaceResponsiveModal').modal('show');
    }
    else {
      // Preventing to close the modal click outside the modal or with the ESC key
      $('#workspaceModal').modal({
        backdrop: 'static',
        keyboard: false
      });
      $('#workspaceModal').modal('show');
    }
    this.notifyObservers();
    this.orderTabsByDate = false;
  };

  /**
   * Changes the info to the selected media
   * @param naId
   * @param file
   * @param index
   * @param objectId
   */
  this.changeMedia = function (naId, file, index, objectId) {
    this.isVideo = false;
    this.isPdf = false;
    this.isAudio = false;
    this.notSupported = false;
    var notFormMobile = false;

    this.MOBILE_NOT_SUPPORTED.forEach(function (type) {
      if (type.test(file.mime)) {
        notFormMobile = true;
      }
    });

    this.notFormMobile = notFormMobile;

    if (/image/.test(file.mime)) {
      this.media.fullPath = file.path;

    } else if (/pdf/.test(file.mime)) {
      this.currentThumbnailURL = file.path;
      this.isPdf = true;
      this.media.fullPath = file.path;
    } else if (/audio/.test(file.mime)) {
      this.isAudio = true;
      this.media.fullPath = $sce.trustAsResourceUrl(file.path);
      this.media.streamPath = $sce.trustAsResourceUrl(file.stream);
    } else if (/video/.test(file.mime)) {
      this.isVideo = true;
      this.media.fullPath = $sce.trustAsResourceUrl(file.path);
      this.media.streamPath = $sce.trustAsResourceUrl(file.stream);
      this.isWmv = file.ext === 'WMV';
    }
    var notSupported = false;
    this.NOT_SUPPORTED.forEach(function (type) {
      if (type.test(file.mime)) {
        notSupported = true;
      }
    });
    this.notSupported = notSupported;
    this.objectId = objectId;
    this.index = index;
    this.notifyObservers();
    this.notifyIndexObservers();
  };

  this.changeIndex = function (index, total) {
    this.index = index;
    this.total = total;
    this.notifyIndexObservers();
  };

  this.removeWMP = function () {
    var index;
    for (index = 0; index < this.wmvPlayers.length; ++index) {
      $(this.wmvPlayers[index]).remove();
    }
    this.wmvPlayers = [];
  };

  this.showLogin = false;

  this.toggleLogin = function (param) {
    this.showLogin = param;
  };
});
