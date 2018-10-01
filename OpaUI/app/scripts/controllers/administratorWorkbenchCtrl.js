opaApp.controller('administratorWorkbenchCtrl', function ($scope, $location, $filter, $http, $log, Administrator, ManageAccounts, OpaUtils, ErrorCodesSvc, Auth, AdministratorReasons, AdministratorService, PaginationSvc) {
  $scope.userNARA = "Display full name to public";
  $scope.invalid = false;
  $scope.status = 0;
  $scope.sortBy = 0;
  $scope.userType = "";
  $scope.typeFilter = 0;
  $scope.removalReason = 0;
  $scope.showCreateForm = true;
  $scope.currentTab = '';
  $scope.showNote = false;
  $scope.currentUser = null;
  $scope.currentEmail = null;
  $scope.users = [];
  $scope.notes = [];
  $scope.reason = [];

  $scope.currentPage= 1;
  $scope.nav={};
  $scope.nav.targetPage =1;
  $scope.totalRecords= 0;
  $scope.resultPerPage=25;
  $scope.offset=0;
  $scope.offsetEnd=0;
  $scope.responseTotal=0;

  $scope.usernameFilter = "";
  $scope.fullNameFilter = "";
  $scope.emailFilter = "";
  $scope.notFoundTerms = "";


  // Pagination variables
  $scope.page1 = 0;
  $scope.page2 = 0;
  $scope.page3 = 0;

  $scope.statuses = [
    {value: 'active', text: 'active'},
    {value: 'inactive', text: 'inactive'}
  ];

  $scope.usertypes = [
    {value: 'standard_regular', text: 'Registered User'},
    {value: 'power_regular', text: 'Power User'},
    {value: 'power_moderator', text: 'Moderator'},
    {value: 'power_accountAdmin', text: 'Administrator'},
    {value: 'power_accountAdminMod', text: 'Administrator/Moderator'}
  ];

  $scope.showFancyDate = function (date) {
    return OpaUtils.fancyDate(date);
  };

  $scope.showNewReasonModal = function () {
    var modal = AdministratorService.showNewReasonModal();

    modal.result.then(
      function (reason) {
        $scope.addReason(reason);
      }
    );
  };


  $scope.addReason = function (reason) {
    AdministratorService.addReason(reason).then(
      function () {
        $scope.getReasons();
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  $scope.clickOnCreateNewReason = function (index) {
    if ($scope.reason[index] === -1) {
      $scope.showNewReasonModal();
      $scope.reason[index] = "";
    }


  };

  $scope.fullNameRequired = function (data) {
    if (typeof data === 'undefined' || data === "" || data === null) {
      return "Please fill in fullname";
    }
  };

  /**
   * Return Display value for user type.
   * @param type
   * @param rights
   * @returns {string}
   * @constructor
   */
  $scope.GetUserType = function (type, rights) {
    var result = '';
    switch (type) {
      case 'standard':
        if (rights === 'regular') {
          result = 'Registered User';
        }
        break;
      case 'power':
        switch (rights) {
          case 'regular':
            result = 'Power User';
            break;
          case 'moderator':
            result = 'Moderator';
            break;
          case 'accountAdmin':
            result = 'Account Administrator';
            break;
          case 'accountAdminMod':
            result = 'Account Administrator/Moderator';
            break;
        }
        break;
    }
    return result;
  };

  /**
   * Returns the proper display name for the user type, since the backend is playing with
   * two different fields: usertType and  rights but the UI must show only one dropdown.
   * @return {string}
   */
  $scope.GetUserTypeDisplay = function (userType) {
    var typeRightsArray = userType.split(":", 2);
    var type = typeRightsArray[0];
    var rights = typeRightsArray[1];
    return $scope.GetUserType(type, rights);
  };

  //
  $scope.goToTab = function (value) {
    if (value === 'register') {
      $scope.currentTab = 'register';
    }
    else {
      if (value === 'manage') {
        $scope.currentTab = 'manage';
      }
    }
  };

  /**
   * Create a new user.
   * @param isValid
   * @returns {*}
   */
  $scope.registerUser = function (isValid) {
    var displayFull = 0;
    var administrator = new Administrator();
    var userName = $scope.userName;
    var password = Math.random().toString(36).substring(2,12);
    var email = $scope.email;
    var fullName = $scope.fullName;
    var displayFullName = $scope.displayFullName;
    var userType = "";
    var userRights = "";
    var userTypeRights = $scope.userType.split(":", 2);
    var result = {};

    $scope.userNameError = "";
    $scope.userEmailError = "";
    $scope.userTypeError = "";
    $scope.generalError = "";
    $scope.fullNameError = "";


    if ($scope.fullName.length < 1 || $scope.fullName.length > 100) {
      $scope.fullNameError = "The length of text field 'Full Name' must be between 1 and 100 characters";
      return;
    }

    if ($scope.email.length > 100) {
      $scope.userEmailError = "The length of text field 'Email' must be less than 100 characters";
      return;
    }

    if ($scope.createAccountForm.fullName.$pristine || !$scope.createAccountForm.fullName) {
      displayFull = 0;
    }
    else {
      displayFull = 1;
    }
    if ($scope.enableNARA) {
      displayFull = 1;
    }
    if (displayFull && ($scope.createAccountForm.fullName.$pristine || typeof $scope.createAccountForm.fullName === 'undefined' || $scope.userType === "")) {
      isValid = false;
    }
    $scope.invalid = !isValid;
    $scope.generalError = "Please fill in all fields to register";
    if (!isValid) {
      if (!$scope.createAccountForm.email.$valid && $scope.createAccountForm.email.$dirty) {
        $scope.userEmailError = "Invalid email - Please enter a valid email to register";
        $scope.invalid = false;
      }
      return;
    }

    if (userTypeRights.length === 2) {
      userType = userTypeRights[0];
      userRights = userTypeRights[1];
      result = administrator.$register({
          'userName': userName,
          'password': password,
          'email': email,
          'userType': userType,
          'userRights': userRights,
          'fullName': fullName,
          'displayFullName': displayFullName
        },
        function (data) {
          $scope.userNameError = "";
          $scope.userEmailError = "";
          $scope.userTypeError = "";
          $scope.generalError = "";
          $scope.fullNameError = "";
          $scope.showCreateForm = false;
          $scope.userID = data.opaResponse.user.internalId;
        },
        function (error) {
          $scope.userNameError = "";
          $scope.userEmailError = "";
          $scope.userTypeError = "";
          $scope.generalError = "";
          $scope.fullNameError = "";

          if (!OpaUtils.checkForAPIError(error)) {

            //Show proper error message depending on the error code.
            switch (error.data.opaResponse.error['@code']) {
              case ErrorCodesSvc.USER_EXISTS:
                $scope.userNameError = error.data.opaResponse.error.description;
                break;
              case ErrorCodesSvc.EMAIL_EXISTS:
                $scope.userEmailError = error.data.opaResponse.error.description;
                break;
              case ErrorCodesSvc.INVALID_PATTERN:
                $scope.userTypeError = error.data.opaResponse.error.description;
                break;
              case ErrorCodesSvc.MISSING_PARAMETER:
                $scope.fullNameError = error.data.opaResponse.error.description;
                break;
              case ErrorCodesSvc.EXCEEDS_SIZE:
                $scope.userNameError = error.data.opaResponse.error.description;
                break;
              default:
                $scope.generalError = error.data.opaResponse.error.description;
                break;
            }
          }
        }
      );
    }
    else {
      $scope.generalError = 'Please select user type';
    }
    return result;
  };

  //Search accounts
  $scope.searchAccounts = function (keepPagination) {

    var administrator = new Administrator();
    var userId = $scope.userId;
    var userName = $scope.usernameFilter;
    var fullName = $scope.fullNameFilter;
    var email = $scope.emailFilter;
    var userType = $scope.typeFilter;
    var status = $scope.status;
    var userRights;
    var userTypeRights;

    if (!keepPagination) {
      $scope.currentPage = 1;
      $scope.offset = 0;
      $scope.currentPage = 1;
    }

    if (Math.floor(status) === 0) {
      status = null;
    }
    if (userType === "0" || userType === 0) {
      userType = null;
    }
    else if ($scope.typeFilter !== null && typeof $scope.typeFilter !== 'undefined') {
      userTypeRights = $scope.typeFilter.split(":", 2);
      if (userTypeRights.length === 2) {
        userType = userTypeRights[0];
        userRights = userTypeRights[1];
      }
    }

    var params = {'offset': $scope.offset, 'rows': $scope.resultPerPage};
    if (!$scope.sortBy) {
      $scope.sortBy = "internalId";
    }
    params.sort = $scope.sortBy + ' asc';
    //Add parameters to filter params if the user selected the filters.
    if (userName) {
      params.id = userName;
    }

    if (fullName) {
      params.fullName = fullName;
    }

    if (email) {
      params.email = email;
    }

    if (userType) {
      params.userType = userType;
    }

    if (userRights) {
      params.userRights = userRights;
    }

    if (userId) {
      params.internalId = userId;
    }

    if (status !== 0) {
      params.status = status;
    }

    //Request to the api the list of accounts that matches the filters applied by the user.
    return administrator.$search(params, function (data) {
        $scope.users = data.opaResponse.users.user;
        if (typeof $scope.users !== 'undefined' && $scope.users) {
          $scope.responseTotal = data.opaResponse.users['@total'];
          $scope.totalRecords = data.opaResponse.users['@searchTotal'];
          $scope.showNoResultsMessage = false;
          angular.forEach($scope.users, function (item, index) {
            //This variable is used for sort
            item.userRights = item.type + '_' + item.rights;
            $scope.reason[index] = "";
            AdministratorService.getNotes(item.id).then(
              function (promise) {
                item.reasons = promise.opaResponse.notes.note;
              },
              function (error) {
                if (!OpaUtils.checkForAPIError(error)) {
                  //check for other errors block
                }
              }
            );
          });
          PaginationSvc.updateOffset($scope);
        }
        else {
          $scope.showNoResultsMessage = true;
        }
      },
      function (error) {
        $scope.users = [];
        $scope.showNoResultsMessage = true;
        $scope.notFoundTerms = "";

        console.log(params);

        var searchTerms = $.map(params, function(value, index) {
          return [value];
        });

        for(var i = 3; i < searchTerms.length; i++){
          if(i !== 3) {
            $scope.notFoundTerms += ", ";
          }
          if(searchTerms[i] !== null) {
            $scope.notFoundTerms += searchTerms[i];
          }
          else {
            if((i+1) === searchTerms.length){
              $scope.notFoundTerms = $scope.notFoundTerms.slice(0, -2);
            }
          }
        }

        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  //request reset user password
  $scope.requestResetPassword = function () {

    var manageAccounts = new ManageAccounts({'accountOwner': $scope.currentUser});
    manageAccounts.$requestpasswordreset({},
      function (data) {
        var message = "A link to reset their password will be sent to the following user account:" + $scope.currentUser + " " + $scope.currentEmail;
        $('#resetPassword').modal('hide');
        OpaUtils.showMessageModal('Reset Password', message);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          $scope.errorMessage = error.data.opaResponse.error.description;
        }
      });
  };

  //Get reasons
  $scope.getReasons = function () {
    var administrator = new Administrator();
    administrator.$viewReasons({},
      function (data) {
        $scope.reasons = AdministratorService.prepareReasonsForSelect(data.opaResponse.reasons.reason);
      },
      function (error) {
        $scope.users = [];
        $scope.reasons = [];
        $scope.reasons = AdministratorService.prepareReasonsForSelect($scope.reasons);
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };


  /**
   * Save function called when a user is edited. First check is there is missing information. Then makes the editions
   * @param user User object
   * @param index Index of the ng-repeat
   * @param additionalComments
   */
  $scope.submitUserChanges = function (user, index, additionalComments) {
    if ($scope.checkingFormModifications(user, index, additionalComments)) {
      $scope.modifyAccount(user, index, additionalComments);

    }
  };


  /**
   * Check form info before change the user information
   * @param user
   * @param index
   * @param additionalComments
   * @returns {boolean}
   */
  $scope.checkingFormModifications = function (user, index, additionalComments) {
    //Checking that a reason was selected
    if (!$scope.reason[index]) {
      OpaUtils.showErrorElementNotification("#saveEdition" + index, "Please select a Reason", "left");
      return false;
    }
    //This Case is when a user account is reactivated
    else if (angular.equals("inactive", user.oldStatus) && angular.equals("active", getUserStatus(user.id)) && !additionalComments) {
      OpaUtils.showErrorElementNotification("#saveEdition" + index, "Additional comments are required when reactivating a user", "left");
      return false;
    }
    return true;

  };

  //Function that allows to modify a specific account.
  $scope.modifyAccount = function (user, index, additionalComments) {

    $scope.errorEditingAccount = false;
    $scope.editingErrorMessage = "";
    additionalComments = additionalComments === "" ? null : additionalComments;
    var reason = $scope.reason[index];
    //reset reason selected value
    $scope.reason[index] = "";
    var userType;
    var userRights;

    //userType=standard&userRights=regular
    var manageAccounts = new ManageAccounts({'accountOwner': user.id});
    var params = {'fullName': user.fullName, 'email': user.email, 'reasonId': reason};
    var manageAccountsSuccess = function (result){
      $scope.searchAccounts();
      OpaUtils.showSuccessGlobalNotification("Success");
    };
    var manageAccountsError = function (error) {
      $scope.searchAccounts();
      if (!OpaUtils.checkForAPIError(error)) {
        //check for other errors block
        OpaUtils.showErrorGlobalNotification(error.data.opaResponse.error.description);
      }
    };

    if (additionalComments) {
      params.notes = additionalComments;
    }

    switch (user.userRights) {
      case 'standard_regular':
        userType = 'standard';
        userRights = 'regular';
        break;
      case 'power_regular':
        userType = 'power';
        userRights = 'regular';
        break;
      case 'power_moderator':
        userType = 'power';
        userRights = 'moderator';
        break;
      case 'power_accountAdmin':
        userType = 'power';
        userRights = 'accountAdmin';
        break;
      case 'power_accountAdminMod':
        userType = 'power';
        userRights = 'accountAdminMod';
        break;
    }
    if (userType && userRights) {
      params.userType = userType;
      params.userRights = userRights;
    }
    manageAccounts.$modify(params,
      function (result) {
        var ma = new ManageAccounts({'accountOwner': user.id});

        //Review if the user changed the account status.
        if (result.opaResponse.user.status !== user.status) {
          if (user.status === 'active') {
            ma.$reactivateAccount(
              {'reasonId': reason, 'notes': additionalComments}, manageAccountsSuccess, manageAccountsError);
          }
          else {
            ma.$deactivateAccount(
              {'reasonId': reason, 'notes': additionalComments}, manageAccountsSuccess, manageAccountsError);
          }
        }
        else {
          manageAccountsSuccess();
        }
        if (result.opaResponse.user.id === Auth.userName()) {
          var userAccount = {};
          userAccount.userName = result.opaResponse.user.id;
          userAccount.fullName = result.opaResponse.user.fullName;
          userAccount.displayFullName = result.opaResponse.user.displayFullName;
          userAccount.isNaraStaff = result.opaResponse.user.isNaraStaff;
          userAccount.rights = result.opaResponse.user.rights;
          userAccount.bulkMaxRecords = result.opaResponse.user.bulkMaxRecords;
          Auth.setUserInfo(userAccount);
        }
      }, manageAccountsError);
  };

  //Reset values for create account form
  $scope.clearCreateAccountForm = function () {
    $scope.userType = "";
    $scope.userName = null;
    $scope.fullName = null;
    $scope.displayFullName = null;
    $scope.email = null;
    $scope.userNameError = "";
    $scope.userEmailError = "";
    $scope.userTypeError = "";
    $scope.generalError = "";
    $scope.fullNameError = "";
    $scope.invalid = false;
    $scope.enableNARA = false;
    $scope.userNARA = "Display full name to public";
    $scope.createAccountForm.$setPristine();
  };

  //Perform the create another account action, first thing is to clear the form.
  $scope.createAnotherAccount = function () {
    $scope.clearCreateAccountForm();
    $scope.userNameError = "";
    $scope.userEmailError = "";
    $scope.userTypeError = "";
    $scope.generalError = "";
    $scope.showCreateForm = true;
  };

  $scope.setCurrentUser = function (user, email) {
    $scope.currentUser = user;
    $scope.currentEmail = email;
    $scope.errorMessage = "";
  };


  /**
   * Search in the list of user the status of a user given his id
   * @param id User id
   */
  var getUserStatus = function (id) {
    var status;
    var index;

    if(id) {
      for (index = 0; index < $scope.users.length; index++) {
        if (angular.equals($scope.users[index].id, id)) {
          status = $scope.users[index].status;
          break;
        }
      }
    }
    return status;
  };


  //Select the proper tab and display its content
  $scope.SelectTab = function (value) {
    switch (value) {
      case 'register':
        $scope.selectedTab = 'register';
        $location.search('tabType', 'register');
        break;
      case 'manage':
        $scope.selectedTab = 'manage';
        $location.search('tabType', 'manage');
        break;
    }
  };

  /*PAGINATION FUNCTIONS*/

  $scope.decreasePageNumber = function () {
    if ($scope.currentPage > 1) {
      PaginationSvc.decreasePageNumber($scope);
      $scope.searchAccounts(true);
      PaginationSvc.updateOffset($scope);
    }
  };

  $scope.pageNumber = function (currentPage) {
    PaginationSvc.pageNumber($scope, currentPage);
    $scope.searchAccounts(true);
    PaginationSvc.updateOffset($scope);
  };

  $scope.firstPageNumber = function () {
    PaginationSvc.firstPageNumber($scope);
    $scope.searchAccounts(true);
    PaginationSvc.updateOffset($scope);
  };

  $scope.lastPageNumber = function () {
    PaginationSvc.lastPageNumber($scope);
    $scope.searchAccounts(true);
    PaginationSvc.updateOffset($scope);
  };

  $scope.last = function () {
    /*if ($scope.pagination.responseTotal < $scope.pagination.resultPerPage){
     return true;
     }*/
    //TODO: get the last from the service
    return PaginationSvc.last($scope);
  };

  $scope.increasePageNumber = function () {
    if (!$scope.last()) {
      PaginationSvc.increasePageNumber($scope);
      $scope.searchAccounts(true);
      PaginationSvc.updateOffset($scope);
    }
  };

  $scope.totalPages = function()
  {
    return PaginationSvc.getTotalPages($scope);
  };

  /* END PAGINATION FUNCTIONS*/

  $scope.init = function () {

    //read tab type from query string
    var p = $location.search().tabType;
    if (typeof p !== 'undefined') {
      switch (p) {
        case 'register':
          $scope.SelectTab('register');
          break;
        case 'manage':
          $scope.SelectTab('manage');
          break;
      }
    }
    else {
      $scope.SelectTab('register');
    }
    $scope.getReasons();
  };
  $scope.init();

  $scope.$watch('resultPerPage', function (newValue, oldValue) {
    if (newValue !== oldValue) {
      $scope.searchAccounts(false);
    }
  });

  $scope.$watch('sortBy', function (newValue, oldValue) {
    if (newValue !== oldValue) {
      $scope.searchAccounts(false);
    }
  });
});
