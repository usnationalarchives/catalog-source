'use strict';

opaApp.controller('activateCtrl', function ($scope, Account, $location, OpaUtils, ReferrerService) {
  $scope.visible = false;
  $scope.showPwdSet = false;
  $scope.returnUrl = '';
  $scope.returnText = '';
  $scope.cancelLink = '/';

  $scope.activation = function () {
    var account = new Account();
    var params = {'activationCode': $location.search().activationCode};
    //Angular bug replace + to %2b instead of %20 -> https://github.com/angular/angular.js/issues/3042
    $scope.returnUrl = $location.search().returnUrl;
    $scope.returnText = $location.search().returnText;

    if ($scope.returnUrl) {
      $scope.returnUrl = $scope.returnUrl.replace(/\+/g, ' ');
    }
    if ($scope.returnText) {
      $scope.returnText = $scope.returnText.replace(/\+/g, ' ');
    }

    if ($scope.showPwdSet) {
      params.showPwdSet = $location.search().showPwdSet;
    }

    account.$activate(params,
      function (data) {
        $scope.visible = true;

        if(data.opaResponse && data.opaResponse.user && data.opaResponse.user.referringUrl){
          ReferrerService.setReferrer(decodeURIComponent(data.opaResponse.user.referringUrl), $location.absUrl());
          $scope.cancelLink = ReferrerService.getReferrer().referrer;
        }

        if ($scope.showPwdSet && data.opaResponse.activationCode && !($scope.returnUrl && $scope.returnText)) {
          var params = {
            'resetCode': data.opaResponse.activationCode,
            'showPwdSet': 1,
            'userName': data.opaResponse.user.id
          };
          $location.search(params);
          $location.path("/resetpassword");
        }
      },
      function (error) {
        $location.path("/");
        $location.search({});
        $scope.visible = false;
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };

  if ($location.search().showPwdSet) {
    $scope.showPwdSet = true;
  }
  $scope.activation();
});
