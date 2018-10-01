'use strict';

opaApp.controller('loginCtrl', function ($scope, $rootScope, $timeout, $location, $routeParams, $window, Account, Auth, Authentication, exportSvc, LoginService, Logout, OpaUtils, searchSvc, visorSvc, ErrorCodesSvc, ReferrerService) {

  $scope.error = "";
  $scope.user = {};
  $scope.Auth = Auth;
  $scope.showPwdSet = false;

  /**
   * This function is used by the menu links in order to hide the menu bar when navigate through pages.
   */
  $scope.hidemenu = function () {
    if ($rootScope.isHome) {
      $('#menuBarHome').collapse('hide');
    }
    else {
      $('#menuBar').collapse('hide');
    }
  };

  $scope.goHomeClear =  function (){
    searchSvc.query ='';
    $location.path("/");
  };

  $scope.searchFocus = function () {
    if($location.url().indexOf("advancedsearch") > -1){
      document.getElementById('searchterm').focus();
      document.getElementById("searchterm").select();
    }
    else{
      document.getElementById('inputSearch').focus();
      document.getElementById("inputSearch").select();
    }

    if(document.getElementById("menuResponsiveParent")){
      if (hasClass(document.getElementById("menuResponsiveParent"), "in")) {
        $('#menuResponsiveParent').collapse('hide');
      }
    }

  };

  $scope.contentFocus = function () {
    if (document.getElementById('contentOPA')) {
      document.getElementById('contentOPA').focus();
    }

    if (hasClass(document.getElementById("menuResponsiveParent"), "in")) {
      $('#menuResponsiveParent').collapse('hide');
    }
  };


  /*
   * After a successful login, user should be redirect to the previous page
   */
  $scope.goBack = function () {
    if (OpaUtils.isOpenWorkspace()) {
      visorSvc.toggleLogin(false);
    }
    else {
      ReferrerService.navigateBack();
    }
  };

  /**
   * Forgot Password
   * Open Forgot password dialog
   */
  if ($location.url().indexOf("forgotpassword") > -1 && !LoginService.forgotPasswordModalShown) {
    LoginService.forgotPasswordModalShown = true;
    LoginService.showForgetPasswordModal();
  }

  /*
   * Function for logging in an user, and also handle where to redirect an
   * user after logged in.
   */
  $scope.login = function () {
    $scope.error = "";
    if ($("#InputPassword").val() && !$scope.user.password) {
      $scope.user.password = $("#InputPassword").val();
    }
    if (!$scope.user.username || !$scope.user.password) {
      $scope.error = "Please fill in all fields to login";
      return;
    }
    Authentication.login({}, {'user': $scope.user.username, 'password': $scope.user.password},
      function (data) {
        Account.view({username: data.opaResponse.user.id}).$promise.then(function (account) {
          $scope.user.username = null;
          $scope.user.password = null;
          var userAccount = {};
          userAccount.userName = account.opaResponse.user.id;
          userAccount.fullName = account.opaResponse.user.fullName;
          userAccount.displayFullName = account.opaResponse.user.displayFullName;
          userAccount.isNaraStaff = account.opaResponse.user.isNaraStaff;
          userAccount.rights = account.opaResponse.user.rights;
          userAccount.timeout = data.opaResponse.user.timeout;
          userAccount.searchMaxRecords = data.opaResponse.user.searchMaxRecords;
          Auth.setUserInfo(userAccount);
          visorSvc.toggleLogin(false);

          if ((Auth.isModerator() || Auth.isAdministrator() || Auth.isAdminMod()) && !OpaUtils.isBulkExport) {
            if (OpaUtils.isOpenWorkspace()) {
              OpaUtils.navigating = true;
              OpaUtils.showAccountHome = true;
              $("#workspaceModal").modal('hide');
              $("#workspaceResponsiveModal").modal('hide');
              setTimeout(function () {
                $location.path("/accounts/" + Auth.userName());
              }, 1500);
            }
            else {
              $location.path("/accounts/" + Auth.userName());
            }
          }
          //For the cases teh user reset password a new user is created by an Admin. After login the user will be redirected to home page.
          else if($location.url().indexOf("thanks") !== -1){
            $location.path("/");
          }
          else if (!OpaUtils.isOpenWorkspace() && !OpaUtils.isBulkExport) {
            ReferrerService.navigateBack();
          }
          if (OpaUtils.isBulkExport) {
            $("#bulkExportModal").modal('hide');
            exportSvc.createExport(exportSvc.format);
            OpaUtils.isBulkExport = false;
          }
        });
      }, function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          if (error.data.opaResponse) {
            $scope.error = error.data.opaResponse.error.description;
            if (angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.ACCOUNT_LOCKED)) {
              $scope.error += " The account will be blocked within the next 15 minutes.";
            }
          }
          else {
            $scope.error = "Invalid credentials";
          }
        }
      });
  };

  /*
   * Function for setting a new password with a reset code
   */
  $scope.setnewpassword = function () {
    if ($scope.password.length < 8) {
      $scope.error = "Invalid Password – Password must contain a minimum of 8 characters";
      return;
    } else {
      if ($scope.password !== $scope.repassword) {
        $scope.error = "Passwords don't match";
        return;
      }
    }
    var account = new Account();
    account.$reset({
        username: $location.search().userName,
        'password': $scope.password,
        'verificationPassword': $scope.repassword,
        'resetCode': $location.search().resetCode
      },
      function () {
        $scope.error = "";
        $location.path("/thanks");
        $location.search({});
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          $scope.error = error.data.opaResponse.error.description;
        }
      }
    );
  };

  /*
   * Function for logging out the user from the API side and also
   * clear the cookies stored in browser.
   */
  $scope.logout = function () {
    var url = $location.url();
    Logout.logout({},
      function () {
        Auth.justLoggedOut = true;
        Auth.logout();
        $location.url(url);
      }, function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      });
  };

  //This button is hidden on the workspace modal
  $scope.showCancelButton = function () {
    return OpaUtils.isOpenWorkspace();
  };

  $scope.showforgotUsernameModal = function () {
    if (OpaUtils.isOpenWorkspace()) {
      OpaUtils.navigating = true;
      $("#workspaceModal").hide();
      LoginService.showforgotUsernameModal(true);
    }
    else {
      LoginService.showforgotUsernameModal(false);
    }
    if (OpaUtils.isBulkExport) {
      $("#bulkExportModal").modal('hide');
    }
  };

  $scope.showForgetPasswordModal = function () {
    if (OpaUtils.isOpenWorkspace()) {
      OpaUtils.navigating = true;
      $("#workspaceModal").hide();
      LoginService.showForgetPasswordModal(true);
    }
    else {
      LoginService.showForgetPasswordModal(false);
    }
    if (OpaUtils.isBulkExport) {
      $("#bulkExportModal").modal('hide');
    }
  };

  $scope.showRegistration = function () {
    if (OpaUtils.isOpenWorkspace()) {
      OpaUtils.navigating = true;
      $("#workspaceModal").modal('hide');
      $("#workspaceResponsiveModal").modal('hide');
    }
    if (OpaUtils.isBulkExport) {
      $("#bulkExportModal").modal('hide');
      searchSvc.cleanUpAccordion();
    }
    $timeout(function () {
      $location.path("/registration");
      $location.url($location.path());
    }, 500);
  };

  if ($location.search().showPwdSet) {
    $scope.showPwdSet = true;
  }
});


opaApp.controller('registrationCtrl', function ($scope, $window, Account, OpaUtils, ReferrerService) {
  $scope.userNARA = "Display full name to public";
  $scope.enableNARA = false;
  $scope.invalid = false;
  $scope.error = "";
  $scope.thankyou = false;
  var displayfull = 0;

  $scope.register = function (isValid) {
    var referralObj;
    var newAccount = new Account();
    if ($scope.registrationForm.fullnamePublic.$pristine || !$scope.account.fullnamePublic) {
      displayfull = 0;
    }
    else {
      displayfull = 1;
    }
    if ($scope.enableNARA) {
      displayfull = 1;
    }
    if (displayfull === 1 && ($scope.registrationForm.fullName.$pristine || typeof $scope.account.fullname === 'undefined')) {
      isValid = false;
    }
    $scope.invalid = !isValid;
    $scope.error = "Please fill in all fields to register";
    $window.scrollTo(0, 200);
    /* Force browser to show error */
    if (!isValid) {
      if (!$scope.registrationForm.email.$valid && $scope.registrationForm.email.$dirty) {
        $scope.error = "Invalid email - Please enter a valid email to register";
      }
      $window.scrollTo(0, 200);
      /* Force browser to show error */
      return;
    }
    if ($scope.account.password.length < 8) {
      $scope.invalid = true;
      $scope.error = "Invalid Password – Password must contain a minimum of 8 characters";
      $window.scrollTo(0, 200);
      /* Force browser to show error */
      return;
    } else if ($scope.account.password !== $scope.account.repassword) {
      $scope.invalid = true;
      $scope.error = "Passwords don't match";
      $window.scrollTo(0, 200);
      /* Force browser to show error */
      return;
    }
    //NARA-2453 After activating the new user account and then logging in,
    //the user was not redirected back to where they clicked on the Register link
    referralObj = ReferrerService.getReferrer();
    if (referralObj.referrer === '/login' || referralObj.referrer === '/registration' || !referralObj.referrer) {
      if  (referralObj.previousReferrer && referralObj.previousReferrer !== '/login' && referralObj.previousReferrer !== '/registration') {
        referralObj.referrer = referralObj.previousReferrer;
      } else {
        referralObj.referrer = '/';
      }
    }

    newAccount.userName = $scope.account.username;
    newAccount.password = $scope.account.password;
    newAccount.email = $scope.account.email;
    newAccount.fullName = $scope.account.fullname;
    Account.register({}, {
        'userName': newAccount.userName,
        'password': newAccount.password,
        'email': newAccount.email,
        'fullName': newAccount.fullName,
        'displayFullName': displayfull,
        'userType': 'standard',
        referringUrl: encodeURIComponent($window.location.protocol + '//' + $window.location.host + referralObj.referrer)
      },
      function () {
        $scope.thankyou = true;
      },
      function (error) {
        $scope.error = "";
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          $scope.invalid = true;
          $scope.error = error.data.opaResponse.error.description;
          $window.scrollTo(0, 200);
          /* Force browser to show error */
        }
      }
    );
  };

  $scope.resendVerificationEmail = function () {
    var account = new Account();
    account.$resendverification({userName: $scope.account.username},
      function () {},
      function (error) {
        $scope.invalid = true;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          $scope.error = error.data.opaResponse.error.description;
          $window.scrollTo(0, 200);
          /* Force browser to show error */
        }
      }
    );
  };

  /*
   * After a successful login, user should be redirect to the previous page
   */
  $scope.goBack = function () {
    ReferrerService.navigateBack();
  };
});
