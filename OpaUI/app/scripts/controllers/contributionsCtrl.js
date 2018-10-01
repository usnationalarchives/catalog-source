opaApp.controller('contributionsCtrl', function ($scope, $location, $routeParams, $log,$window, Auth, Contributions, OpaUtils, PaginationSvc, configServices) {

  /*
   * UI Binding variable
   */
  $scope.selectedTab = 1;
  $scope.resultPerPage = 25;
  $scope.count = {};
  $scope.selectedTag = null;
  $scope.currentPage = 1;
  $scope.offset = 0;
  $scope.OpaUtils = OpaUtils;
  $scope.rowsTags = 200;
  $scope.totalDistinctTags = 0;
  $scope.hostname = $window.location.protocol + '//' + $window.location.host;
  $scope.nav = {};
  $scope.nav.targetPage = 1;
  $scope.fullnameToDisplay = "";

  /*
   * Variables for sorting
   */
  $scope.predicate = 'tag ASC';
  $scope.predicateTop = 'tag';
  $scope.reverseTop = false;
  $scope.reverse = true;
  $scope.sortby = 'Alphabetically';
  $scope.tableSortby = 'date';

  // Pagination variables
  $scope.page1 = 0;
  $scope.page2 = 0;
  $scope.page3 = 0;

  var username;
  var firstime = true;

  var init = function () {
    var type;
    if (!('contributionType' in $location.search())) {
      $location.search('contributionType', 'tags');
    }
    type = $location.search().contributionType;
    switch (type) {
      case 'tags':
        $scope.selectedTab = 1;
        break;
      case 'comments':
        $scope.selectedTab = 2;
        break;
      case 'transcriptions':
        $scope.selectedTab = 3;
        break;
      case 'translations':
        $scope.selectedTab = 4;
        break;
    }
    username = $routeParams.username;
    $scope.setTab($scope.selectedTab, type);
    $scope.count.tags = 0;
    $scope.count.comments = 0;
    $scope.count.transcriptions = 0;
    $scope.count.translations = 0;
    $scope.rowsTags = 200;
    $scope.tags = [];
    $window.sessionStorage.clear();

  };

  var loadContributions = function (type) {
    var contributions = new Contributions();
    contributions.$getSummary({username: username, fullstats: false},
      function (data) {
        if (data.opaResponse.contributions) {
          $scope.count.tags = data.opaResponse.contributions.tags['@total'];
          $scope.count.transcriptions = data.opaResponse.contributions.transcriptions['@total'];
          $scope.count.comments = data.opaResponse.contributions.comments['@total'];
          if (data.opaResponse.contributions['@DisplayFullNameFlag'] === 'true') {
            $scope.fullnameToDisplay = data.opaResponse.contributions['@userFullName'];
            if (data.opaResponse.contributions['@isNaraStaff'] === 'true') {
              $scope.fullnameToDisplay = $scope.fullnameToDisplay + ' (NARA Staff)';
            }
          }
          else {
            $scope.fullnameToDisplay = username;
          }
          $scope.reverse = true;
          switch (type) {
            case 'tags':
              contributions.$getTags({username: username, rows: $scope.rowsTags, offset: 0, sort: $scope.predicate},
                function (data) {
                  $scope.totalDistinctTags = data.opaResponse.tags.total;
                  if (data.opaResponse.tags.tag) {
                    $scope.tags = data.opaResponse.tags.tag;
                  }
                },
                function (error) {
                  if (!OpaUtils.checkForAPIError(error)) {
                    //check for other errors block
                    $scope.tags = [];
                  }
                }
              );
              break;
            case 'transcriptions':
              getTranscriptions();
              break;

            case 'comments':
              getComments();
              break;
          }
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  $scope.increaseOffset = function () {
    $scope.rowsTags += 200;
    if ($scope.totalDistinctTags > $scope.tags.length) {
      loadContributions('tags');
    }
  };

  $scope.selectTag = function (tag, count) {
    if (tag && count) {
      $scope.selectedTag = tag;
      $scope.totalTitlesTag = $scope.totalRecords = count;
      $scope.offset = 0;
      $scope.currentPage = 1;
    }
    getTags();
  };

  $scope.sortTags = function (sortby) {
    if (sortby === "Alphabetically") {
      $scope.predicate = 'tag DESC';
      $scope.predicateTop = 'tag';
      $scope.reverseTop = false;
    } else {
      $scope.predicate = 'count DESC';
      $scope.predicateTop = 'count';
      $scope.reverseTop = true;
    }
    if ($scope.totalDistinctTags > $scope.tags.length) {
      loadContributions('tags');
    }
  };

  $scope.setRows = function (resultPerPage) {
    $scope.resultPerPage = resultPerPage;
    $scope.currentPage = 1;
    $scope.offset = 0;
    if ($scope.selectedTab === 1) {
      $scope.selectTag();
    }
    else {
      loadContributions($location.search().contributionType);
    }
  };

  $scope.sortTable = function (param) {
    var func;
    if (param === 'date') {
      switch ($scope.selectedTab) {
        case 1:
          func = getTags;
          break;
        case 2:
          func = getComments;
          break;
        case 3:
          func = getTranscriptions;
          break;
      }
    }
    if (param === $scope.tableSortby) {
      $scope.reverse = !$scope.reverse;
    }
    else {
      $scope.reverse = true;
    }
    $scope.tableSortby = param;
    func();
  };

  $scope.setTab = function (index, query) {
    if (!firstime) {
      $location.search('contributionType', query).path($location.path(), false);
    }
    $scope.filtertext = null;
    //$scope.textToFilterBy = null;
    $scope.selectedTab = index;
    $scope.selectedTag = null;
    $scope.currentPage = 1;
    $scope.offset = 0;
    $scope.tableSortby = 'date';
    $scope.titles = null;
    $scope.totalRecords = null;
    $scope.rows = null;
    $scope.rowsTags = 200;
    $scope.tags = [];
    $scope.predicate = 'tag ASC';
    $scope.predicateTop = 'tag';
    $scope.reverseTop = false;
    $scope.reverse = true;
    $scope.sortby = 'Alphabetically';
    $scope.showMore = false;
    firstime = false;
    loadContributions(query);
  };

  $scope.usersContributions = function () {
    if (Auth.isLoggedIn() && Auth.userName() === $routeParams.username) {
      return "My ";
    } else {
      return ($scope.fullnameToDisplay || $routeParams.username) + "'s ";
    }
  };

  $scope.myModifications = function (fullname) {
    if (Auth.isLoggedIn() && Auth.userName() === $routeParams.username) {
      return "you";
    } else {
      return (fullname || $routeParams.username);
    }
  };

  var getTranscriptions = function () {
    var contributions = new Contributions();
    contributions.$getTranscriptions({
        username: username,
        title: $scope.filtertext,
        offset: $scope.offset,
        rows: Math.min($scope.resultPerPage, configServices.MAX_CONTRIBUTION_ROWS),
        descOrder: $scope.reverse
      },
      function (data) {
        if (data.opaResponse.titles) {
          $scope.titles = data.opaResponse.titles.title;
          $scope.totalRecords = data.opaResponse.titles.total;
          if ($scope.resultPerPage > configServices.MAX_CONTRIBUTION_ROWS) {
            OpaUtils.showErrorGlobalNotification("For performance considerations, only " + configServices.MAX_CONTRIBUTION_ROWS + " transcriptions are displayed.");
          }
        }
        $scope.updateOffset();
      },
      function (error) {
        $scope.ready = true;
        $scope.totalRecords = 0;
        $scope.titles = null;
        $scope.currentPage = 1;
        $scope.offset = 0;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  var getTags = function () {
    var contributions = new Contributions();
    $scope.ready = false;
    contributions.$getTaggedTitles({
        username: username,
        title: $scope.filtertext,
        tagtext: $scope.selectedTag,
        offset: $scope.offset,
        rows: $scope.resultPerPage,
        descOrder: $scope.reverse
      },
      function (data) {
        if (data.opaResponse.titles) {
          if ('@total' in data.opaResponse.titles) {
            $scope.totalRecords = parseInt(data.opaResponse.titles['@total']);
          }
          else {
            $scope.totalRecords = parseInt(data.opaResponse.titles.total);
          }
          if (!$scope.filtertext) {
            $scope.totalRecords = $scope.totalTitlesTag;
          }
          $scope.titles = data.opaResponse.titles.title;
          $scope.ready = true;
        }
        $scope.updateOffset();
      },
      function (error) {
        $scope.ready = true;
        $scope.titles = null;
        $scope.totalRecords = 0;
        $scope.currentPage = 1;
        $scope.offset = 0;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  var getComments = function () {
    var contributions = new Contributions();
    var params = {
      username: username,
      offset: $scope.offset,
      rows: $scope.resultPerPage,
      descOrder: $scope.reverse,
      title: $scope.filtertext
    };

    $scope.ready = false;
    contributions.$getComments(params,
      function (data) {
        if (data.opaResponse.comments) {
          if ('@total' in data.opaResponse.comments) {
            $scope.totalRecords = parseInt(data.opaResponse.comments['@total']);
          }
          else {
            $scope.totalRecords = parseInt(data.opaResponse.comments.total);
          }
          $scope.titles = data.opaResponse.comments.comment;
          $scope.ready = true;
        }
        $scope.updateOffset();
      },
      function (error) {
        $scope.ready = true;
        $scope.titles = null;
        $scope.totalRecords = 0;
        $scope.currentPage = 1;
        $scope.offset = 0;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  $scope.filter = function (filtertext) {
    if (!filtertext) {
      $scope.filtertext = null;
    }
    else {
      $scope.filtertext = filtertext;
    }
    $scope.offset = 0;
    $scope.currentPage = 1;
    switch ($scope.selectedTab) {
      case 1:
        getTags(filtertext);
        break;
      case 2:
        getComments(filtertext);
        break;
      case 3:
        getTranscriptions(filtertext);
        break;
    }
  };


  /***New Pagintation functions
   *
   */

  $scope.pageNumber = function (page) {
    PaginationSvc.pageNumber($scope, page);
    updateContent();
  };

  $scope.last = function () {
    return PaginationSvc.last($scope);
  };

  $scope.totalPages = function () {
    return PaginationSvc.getTotalPages($scope);
  };

  $scope.firstPageNumber = function () {
    PaginationSvc.firstPageNumber($scope);
    updateContent();
  };

  $scope.lastPageNumber = function () {
    PaginationSvc.lastPageNumber($scope);
    updateContent();
  };

  $scope.updateOffset = function () {
    PaginationSvc.updateOffset($scope);
  };

  /***************************************************************/


  $scope.decreasePageNumber = function () {
    PaginationSvc.decreasePageNumber($scope);
    updateContent();
  };

  $scope.increasePageNumber = function () {
    PaginationSvc.increasePageNumber($scope);
    updateContent();
  };

  var updateContent = function () {

    if ($scope.selectedTab === 1) {
      $scope.selectTag();
    }
    else {
      loadContributions($location.search().contributionType);
    }
   $scope.nav.targetPage = $scope.currentPage;
  };

  $scope.getUrl = function (title, contribution) {
    var url = '#/id/' + title.naId;
    if (title.objectId) {
      url += '/' + title.pageNum + '/public?contributionType=' + contribution;
    }
    return url;
  };

  $scope.getImage = function (title) {
    if (title.objectId) {
      return title.pageNum + '/' + title.totalPages;
    }
    /*
     * NARA-1653
     * If window resolution is too small don't show the whole Description text, just Desc.
     */
    if (OpaUtils.isPhone()) {
      return "Desc.";
    }
    return "Description";
  };

  $scope.getOffset = function () {
    if ((parseInt($scope.resultPerPage) + $scope.offset) < $scope.totalRecords) {
      return parseInt($scope.resultPerPage) + $scope.offset;
    }
    return $scope.totalRecords;
  };

  init();

});
