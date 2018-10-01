'use strict';

opaApp.controller('statisticsCtrl', function ($scope, $window, OpaUtils, searchSvc, statisticsSvc) {

  var result = {};
  $scope.toggle = OpaUtils.toggle;
  $scope.prettyName = OpaUtils.prettyNameObjects;
  $scope.statistics = [];

  var getStatistics = function () {
    var data = {};
    var first;
    result = statisticsSvc.getStatistics();
    result.then(function (results) {
      data = results.opaResponse.statistics;
      angular.forEach(data, function (value, key) {
        first = value.subGroup[0];
        if (first && first.subCounts) {
          if (first.subCounts.itemCount >= 0) {
            value.items = true;
          }
          if (first.subCounts.seriesCount >= 0) {
            value.series = true;
          }
          if (first.subCounts.fileUnitCount >= 0) {
            value.fileUnits = true;
          }
        }
        switch (key) {
          case "collection":
            value.order = 0;
            break;
          case "recordgroup":
            value.order = 1;
            break;
          case "series":
            value.order = 2;
            break;
          case "fileunit":
            value.order = 3;
            break;
          case "item":
            value.order = 4;
            value.subGroup = undefined;
            break;
        }
        value.name = $scope.prettyName(key, false);
        value.key = key;
        $scope.statistics.push(value);
      });
    });
  };

  /**
   * Initializes the controller.
   */
  getStatistics();
});
