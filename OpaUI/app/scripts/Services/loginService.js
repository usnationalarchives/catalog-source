opaApp.service("LoginService", function ($modal, Auth, Logout, OpaUtils, $location) {

  this.forgotPasswordModalShown = false;

  this.showForgetPasswordModal = function (fromWorkspace) {
    $modal.open({
      templateUrl: 'views/directives/dialogs/forgotpassworddialog.html',
      controller: ['$scope', '$modalInstance', 'Account', 'fromWorkspace', function ($scope, $modalInstance, Account, fromWorkspace) {
        $scope.fromWorkspace = fromWorkspace;
        $scope.error = "";
        $scope.username = "";
        $scope.resetPassword = function (username) {
          if (!username) {
            $scope.error = 'Please fill in Username';
          } else {
            $scope.error = "";
            var account = new Account();
            account.$requestReset({username: username},
              function (data) {
                $scope.error = "";
                $scope.close();
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
        $scope.close = function () {
          $modalInstance.close();
          if (fromWorkspace) {
            $("#workspaceModal").show();
          }
        };
      }],
      resolve: {
        fromWorkspace: function () {
          return fromWorkspace;
        }
      }
    });
  };

  this.showforgotUsernameModal = function (fromWorkspace) {
    $modal.open({
      templateUrl: 'views/directives/dialogs/forgotusernamedialog.html',
      controller: ['$scope', '$modalInstance', 'Account', 'fromWorkspace', function ($scope, $modalInstance, Account, fromWorkspace) {
        $scope.fromWorkspace = fromWorkspace;
        $scope.error = "";
        $scope.email = "";
        $scope.forgetUsername = function (email) {
          $scope.error = "";
          var account = new Account();
          account.$recover({'email': email},
            function (data) {
              $scope.error = "";
              $scope.close();
            },
            function (error) {
              if (!OpaUtils.checkForAPIError(error)) {
                //check for other errors block
                $scope.error = error.data.opaResponse.error.description;
              }
            }
          );
        };
        $scope.close = function () {
          $modalInstance.close();
          if (fromWorkspace) {
            $("#workspaceModal").show();
          }
        };
      }],
      resolve: {
        fromWorkspace: function () {
          return fromWorkspace;
        }
      }
    });
  };

  this.logout = function () {
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
});
