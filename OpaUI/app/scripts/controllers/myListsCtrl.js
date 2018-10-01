opaApp.controller('myListsCtrl', function ($filter, $scope, $location, $routeParams, $timeout, Auth, exportSvc, ListsService, OpaUtils) {

  $scope.rowsPerPage = 25;
  $scope.currentPage = 1;
  $scope.showNoElementsMessage = false;
  $scope.pageSelected = false;
  $scope.ListsService = ListsService;

  $scope.array_lists = {}; //TODO: check if array_lists is needed.
  $scope.showSuccessAlert = false;
  $scope.showErrorAlert = false;
  $scope.OpaId = false;

  $scope.exportSvc = exportSvc;

  ListsService.selectedRecords = [];
  ListsService.selectedAllIndex = [];

  var isPublic = false;

  /*
   * Function to get all the lists for the current user
   * It populates the $scope.userList array with the lists returned by API
   */
  var getAllLists = function () {
    ListsService.GetAllLists().then(function (promise) {
      if (promise.opaResponse) {
        $scope.userList = promise.opaResponse.userLists.userList;
      }
    }, function (error) {
      $scope.userList = {};
      if (!OpaUtils.checkForAPIError(error)) {
        //check for other errors block
      }
    });
  };

  /*
   * Function to delete all the list for the current user
   */
  $scope.deleteAllLists = function () {
    ListsService.DeleteAllLists().then(function () {
      $scope.ListContent = {};
      $scope.selectedList = null;
      $("#deleteListsConfirmation").modal('hide');
    }, function (error) {
      if (!OpaUtils.checkForAPIError(error)) {
        //check for other errors block
      }
    });

    $timeout(function () {
      $location.path('/accounts/' + Auth.userName() + '/lists/', false);
      getAllLists();
    }, 500);
  };

  /*
   * Function to load the list selected by the user, it will set pagination
   * variables to its default
   */
  $scope.selectList = function (listName, dontLoad) {
    $scope.currentPage = 1;
    $scope.offset = 0;
    if (dontLoad) {
      $location.path('/accounts/' + Auth.userName() + '/lists/' + listName, false);
    }
    else {
      $location.path('/accounts/' + Auth.userName() + '/lists/' + listName, true);
    }
  };

  /*
   * Function to load a list
   */
  var loadListView = function (listName) {
    $scope.pageSelected = false;
    $scope.showNoElementsMessage = false;
    $scope.selectedList = listName;
    var params = {};
    params.list = listName;
    params.rows = $scope.rowsPerPage;
    params.offset = $scope.offset;
    isPublic = false;
    if (Auth.userName() !== $routeParams.username) {
      params.username = $routeParams.username;
      isPublic = true;
    }
    ListsService.GetList(params).then(function (promise) {
      if (promise.opaResponse) {
        $scope.array_lists = {};
        $scope.selectedOpaId = false;
        $scope.totalRecordsinList = promise.opaResponse.results["@total"];
        $scope.ListContent = promise.opaResponse.results.result;
        managePagination(promise.opaResponse);
      }
    }, function (error) {
      $scope.array_lists = {};
      $scope.selectedOpaId = false;
      $scope.ListContent = {};
      $scope.showNoElementsMessage = true;
      $scope.totalRecordsinList = 0;
      if (!OpaUtils.checkForAPIError(error)) {
        //check for other errors block
      }
    });
  };

  $scope.DeleteCurrentList = function () {
    ListsService.DeleteList($scope.selectedList).then(function (promise) {
      if (!promise.opaResponse) {
      }
      else {
        if (promise.opaResponse.header["@status"] === '200') {
          $scope.showSuccessAlert = true;
          $scope.successTextAlert = "The list was deleted successfully";
          $timeout(function () {
            $location.path('/accounts/' + Auth.userName() + '/lists/', false);
            $scope.showSuccessAlert = false;
          }, 3000);
        }
      }
      getAllLists();
      $scope.selectedList = null;
      $('#deleteListConfirmation').modal('hide');
    }, function (error) {
      $('#deleteListConfirmation').modal('hide');
      $scope.showErrorAlert = true;
      $scope.errorTextAlert = error.data.opaResponse.error.description;
      $scope.selectedList = null;
      $timeout(function () {
        $scope.showErrorAlert = false;
      }, 3000);
      getAllLists();
      if (!OpaUtils.checkForAPIError(error)) {
        //check for other errors block
      }
    });
  };

  $scope.RenameCurrentList = function (newname) {
    if (newname) {
      //var regex = new RegExp('[|&;$%?@"<>()+,]');
      //if (regex.test(newname)) {
      //  OpaUtils.showErrorElementNotification("#newname", "Special characters cannot be used in list name", "bottom center");
      //  return;
      //}
      newname = $filter('removeWordCharacters')(newname);
      ListsService.RenameList($scope.selectedList, newname).then(function () {
        $('#renameListConfirmation').modal('hide');
        $scope.selectedList = newname;
        $scope.selectList($scope.selectedList, true);
        getAllLists();
        loadListView($scope.selectedList);
        OpaUtils.showSuccessGlobalNotification("The list was renamed successfully");
        /* To change the url shared using addthis a page reload is needed */
        $timeout(function () {
          window.location.href = $location.absUrl();
        }, 1600);
      }, function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          OpaUtils.showErrorElementNotification("#newname", error.data.opaResponse.error.description, "bottom center");
        }
      });
    }
    else {
      OpaUtils.showErrorElementNotification("#newname", "Please enter a new list name", "bottom center");
    }
  };

  $scope.RemoveSelectedRecords = function () {
    var items = ListsService.selectedRecords.join(',');
    if (ListsService.selectedall) {
      items = 'all';
    }
    if (items) {
      ListsService.DeleteSelectedListItems($scope.selectedList, items).then(function (promise) {
        $('#deleteRecordsConfirmation').modal('hide');
        getAllLists();
        $scope.offset = 0;
        loadListView($scope.selectedList);
        $scope.showSuccessAlert = true;
        $scope.successTextAlert = "The records were successfully removed";
        $scope.OpaId = false;
        $timeout(function () {
          $scope.showSuccessAlert = false;
        }, 3000);
        ListsService.selectedRecords = [];
      }, function (error) {
        $('#deleteRecordsConfirmation').modal('hide');
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          getAllLists();
          loadListView($scope.selectedList);
          $scope.showErrorAlert = true;
          $scope.errorTextAlert = error.data.opaResponse.error.description;
          $timeout(function () {
            $scope.showErrorAlert = false;
          }, 3000);
        }
      });
    }
  };

  $scope.createUrlListItem = function (naid) {
    var url = $location.protocol() + "://" + $location.host();
    return url + "/" + "id" + "/" + naid;
  };


  // switch flag
  $scope.switchBool = function (value) {
    $scope[value] = !$scope[value];
  };

  $scope.selectOnPage = function () {

    angular.forEach($scope.ListContent, function (record) {
      var idx;
      if (ListsService.selectedAllIndex[$scope.currentPage - 1]) {
        $scope.toggleSelection(record.opaId, true);
      }
      else {
        idx = ListsService.selectedRecords.indexOf(record.opaId);
        if (idx > -1) {
          ListsService.selectedRecords.splice(idx, 1);
        }
      }
    });
  };

  $scope.toggleSelection = function (opaId, all) {
    var idx = ListsService.selectedRecords.indexOf(opaId);
    if (idx > -1) {
      if (!all) {
        ListsService.selectedRecords.splice(idx, 1);
        ListsService.selectedAllIndex[$scope.currentPage - 1] = false;
      }
    }
    else {
      ListsService.selectedRecords.push(opaId);
    }
  };

  var managePagination = function (opaResponse) {
    $scope.total = opaResponse.results["@total"];
    $scope.rows = opaResponse.results.rows;
    $scope.offset = Number(opaResponse.results["@offset"]);
    $scope.bottomLimit = $scope.offset + 1;
    var tempTopLimit = $scope.offset + Number($scope.rows);
    if (Number(tempTopLimit) > Number($scope.total)) {
      $scope.topLimit = $scope.total;
    }
    else {
      $scope.topLimit = tempTopLimit;
    }
  };

  $scope.last = function () {
    return $scope.currentPage >= Math.ceil($scope.total / $scope.rowsPerPage);
  };

  $scope.increasePageNumber = function () {
    if ($scope.last()) {
      return;
    }
    $scope.currentPage = $scope.currentPage + 1;
    $scope.offset = ($scope.currentPage - 1) * $scope.rowsPerPage;
    loadListView($scope.selectedList);
  };

  $scope.decreasePageNumber = function () {
    if ($scope.currentPage <= 1) {
      return;
    }
    $scope.currentPage = $scope.currentPage - 1;
    $scope.offset = ($scope.currentPage - 1) * $scope.rowsPerPage;
    loadListView($scope.selectedList);
  };

  // Check when the results per page is changed and request a new set of results
  $scope.setRows = function (value) {
    if (typeof $scope.selectedList !== 'undefined' && $scope.selectedList !== "") {
      $scope.offset = 0;
      $scope.currentPage = 1;
      $scope.rowsPerPage = value;
      loadListView($scope.selectedList);
    }
  };

  $scope.showPrintModal = function () {
    $('#printModal').modal('show');
    exportSvc.naid = null;
    exportSvc.cleanUp();
    exportSvc.enableLists(true);
  };

  $scope.showPrintListModal = function () {
    if ($scope.selectedList && !$scope.totalRecordsinList) {
      OpaUtils.showMessageModal("Print error", "You can not print an empty list");
      return;
    }
    $('#printModal').modal('show');
    exportSvc.naid = null;
    exportSvc.cleanUp();
    exportSvc.selectedList = $scope.selectedList;
    exportSvc.userName = $routeParams.username || null;
    exportSvc.recordsInList = $scope.totalRecordsinList || 0;
    exportSvc.enableLists(true);
  };

  $scope.isViewedbyPublic = function () {
    return isPublic;
  };

  $scope.generateURL = function(item) {
    if (item.naId) {
      return "#/id/" + item.naId;
    }
    return item.contentDetailUrl;
  };

  if (Auth.isLoggedIn() && Auth.userName() === $routeParams.username) {
    getAllLists();
  }

  if ($routeParams.listname) {
    $timeout(function () {
      loadListView($routeParams.listname);
    }, 100);
  }

  /**
   * Check if the type of metadata object is a record From type.
   * @param metadata
   * @returns {boolean}
   */
  $scope.isRecordFrom = function(metadata){
    switch (metadata.name){
      case 'recordGroupNumber':
      case 'seriesTitle':
        return true;
      default :
        return false;
    }
  }
});
