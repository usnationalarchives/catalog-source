'use strict';

opaApp.controller('generalViewCtrl', function ($scope, $rootScope, $location, $routeParams, $window, searchSvc,  OpaUtils, AnnouncementService, HomeService, VerifyEmailService,Auth) {
  //Commented because NARA-1592
  //searchSvc.setQuery("");

  $scope.AnnouncementService = AnnouncementService;
  $scope.HomeService = HomeService;
  $scope.searchSvc = searchSvc;

  $rootScope.isHome = true;

  $scope.search= function(){
    $location.search('q',searchSvc.query);
    if (!searchSvc.query) {
      searchSvc.query = '';
    }
    $location.path("/search");
  };

  var init = function(){
    var accountVerify;
    if($routeParams.activationCode && $routeParams.emailVerify) {
      accountVerify = new VerifyEmailService();
      accountVerify.$verifyEmailChange({activationCode: $routeParams.activationCode},
        function (data) {
          Auth.setUserInfo(data.opaResponse.user);
          OpaUtils.showMessageModal('Account Information Updated', 'Email address change successful!');
        }, function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            OpaUtils.showMessageModal('Account Information Updated', 'There was an error verifying your email change.');
          }
        });
    }
    $location.search({});
    $location.replace();
  };
  init();
});
