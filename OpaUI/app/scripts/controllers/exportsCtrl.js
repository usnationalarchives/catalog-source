'use strict';
opaApp.controller('exportsCtrl', function ($scope, exportSvc, Exports, Auth, OpaUtils, configServices, $timeout) {

  $scope.Auth = Auth;
  $scope.OpaUtils = OpaUtils;
  $scope.configServices = configServices;

  var getExports = function () {
    var exports = new Exports();
    exports.$get({},
      function (data) {
        if (data.opaResponse && data.opaResponse.accountExports) {
          $scope.exports = data.opaResponse.accountExports.accountExport;
        }
        $timeout(function () {
          angular.forEach($scope.exports, function (elem, index) {
            $('#deleteExport' + index).kendoTooltip({
              autoHide: true,
              position: "bottom",
              callout: false,
              content: "Remove this download"
            });
          });
        }, 500);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          $scope.exports = null;
        }
      }
    );
  };

  $scope.deleteExport = function (id) {
    var exports = new Exports();
    exports.$deleteExport({auth: 'auth', 'exportId': id},
      function () {
        OpaUtils.showSuccessGlobalNotification("This download has been removed");
        getExports();
      }, function (error) {});
  };

  $scope.progress = function (percentage) {
    return Math.floor(percentage / 20);
  };

  getExports();
});
