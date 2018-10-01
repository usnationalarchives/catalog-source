'use strict';
opaApp.controller('myAccountCtrl', function ($location, $scope, $window, Account, Auth, Contributions, Exports, ListsService, OpaUtils) {
  $scope.Auth = Auth;
  $scope.AccountInformation = {};
  $scope.notifications = {};
  $scope.contributionsStatsHeaders = [['THIS MONTH','@totalMonth'],['THIS YEAR','@totalYear'],['ALL TIME','@total']];

  //Toogle display full name to public option
  $scope.DisplayFullNameToPublic = function () {
    var account = new Account();
    account.$modify({'displayFullName': $scope.AccountInformation.isFullNamePublic},
      function (data) {
        if (data.opaResponse) {
          Auth.displayFullName(data.opaResponse.user.displayFullName);
          OpaUtils.showMessageModal('Information', 'Account Information Updated');
        }
      },
      function () {
        OpaUtils.showMessageModal('Error', 'Unable to Update Account Information, please contact NARA technical support at catalog@nara.gov');
      }
    );
  };

  //Get contributions stats
  var getContributtions = function () {
    var contributions = new Contributions();
    contributions.$getContributions({fullstats: true, username: Auth.userName()},
      function (data) {
        if (data.opaResponse) {
          $scope.contributions = data.opaResponse.contributions;
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  //Get exports summary
  var getExports = function () {
    var exports = new Exports();
    exports.$getSummary({},
      function (data) {
        if (data.opaResponse) {
          $scope.exports = {};
          $scope.exports.pending = data.opaResponse.accountExportsStatusSummary.Pending;
          $scope.exports.complete = data.opaResponse.accountExportsStatusSummary.Complete;
        }
      },
      function (error) {
        $scope.exports = {};
        $scope.exports.pending = 0;
        $scope.exports.complete = 0;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  //Get exports summary
  //TODO: move to service and update bindings
  var getNotifications = function () {
    var account = new Account();
    account.$getNotifications({},
      function (data) {
        if (data.opaResponse) {
          $scope.notifications = data.opaResponse.notifications;
        }
      },
      function () {
        $scope.notifications = 0;
      }
    );
  };

  //Deactivate Account
  $scope.deactivateAccount = function () {
    var account = new Account();
    account.$deactivate({'password': $scope.deactivatePassword},
      function (data) {
        $('#deactivateAccount').removeClass('fade');
        $('#deactivateAccount').modal('hide');
        if (data.opaResponse) {
          if (Auth.isLoggedIn()) {
            Auth.logout();
            $location.url("/login");

          }
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


  //Get User Profile Information
  var getProfileInformation = function () {
    var account = new Account();
    account.$view({},
      function (data) {
        if (data.opaResponse) {
          $scope.AccountInformation.userId = data.opaResponse.user.id;
          $scope.AccountInformation.type = data.opaResponse.user.type;
          $scope.AccountInformation.fullName = data.opaResponse.user.fullName;
          $scope.AccountInformation.isFullNamePublic = data.opaResponse.user.displayFullName;
          $scope.AccountInformation.email = data.opaResponse.user.email;
          $scope.AccountInformation.rights = data.opaResponse.user.rights;
          $scope.AccountInformation.isNaraStaff = data.opaResponse.user.isNaraStaff;
          Auth.setUserInfo($scope.AccountInformation);
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse.error) {
            $scope.error = error.data.opaResponse.error.description;
          }
        }
      }
    );
  };

  //Change password
  $scope.changePassword = function () {
    if ($scope.newPassword !== "" && $scope.oldPassword !== "" && $scope.confirmPassword !== "") {
      var account = new Account();
      if ($scope.confirmPassword === $scope.newPassword) {
        account.$modify({'password': $scope.oldPassword, 'newPassword': $scope.newPassword},
          function (data) {
            if (data.opaResponse) {
              $('#changePassword').modal('hide');
              OpaUtils.showMessageModal('Information', 'Account Information Updated');
              $scope.error = "";
            }
          },
          function (error) {
            if (!OpaUtils.checkForAPIError(error)) {
              //check for other errors block
              $scope.error = error.data.opaResponse.error.description;
            }
          }
        );
      }
      else {
        $scope.error = "Your New Passwords don’t match";
      }
    }
    else {
      $scope.error = "Please fill in all fields";
    }
  };

  $scope.changeEmail = function () {
    if ($scope.newEmail.length > 100) {
      $scope.error = "Text size of field 'email' (" + $scope.newEmail.length + ") is greater than field size: 100 ";
      return;
    }
    if ($scope.newEmail !== '' && $scope.password !== '') {
      var account = new Account();
      account.$modify({'email': $scope.newEmail, 'password': $scope.password},
        function (data) {
          if (data.opaResponse) {
            $('#changeEmail').modal('hide');
            getProfileInformation();
            OpaUtils.showMessageModal('Account Information Updated', 'Email Address Change Pending<br>Check your email for a verification message!');
            $scope.error = "";
          }
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            $scope.error = error.data.opaResponse.error.description;
          }
        }
      );
    }
    else {
      $scope.error = "Please fill in all fields";
    }
  };

  $scope.changeFullName = function () {
    if ($scope.newName === "" && $scope.password === "") {
      $scope.error = "Please fill in all fields";
    }
    else {
      var account = new Account();
      account.$modify({'fullName': $scope.newName, 'password': $scope.password},
        function (data) {
          if (data.opaResponse) {
            $('#changeFullName').modal('hide');
            getProfileInformation();
            Auth.fullName(data.opaResponse.user.fullName);
            OpaUtils.showMessageModal('Information', 'Account Information Updated');
            $scope.error = "";
          }
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            $scope.error = error.data.opaResponse.error.description;
          }
        }
      );
    }
  };

  var getListSummary = function () {
    ListsService.GetAllLists().then(
      function (data) {
        if (data.opaResponse) {
          ListsService.listCount = $scope.listCount = data.opaResponse.userLists.total;
        }
      },
      function () {
        $scope.listCount = 0;
      });
  };

  getContributtions();
  getListSummary();
  getExports();
  getProfileInformation();
  getNotifications();
});
