'use strict';
opaApp.controller('calendarCtrl', function ($scope) {
  $scope.init = function (viewMode) {
    if (viewMode === 'years') {
      $scope.var1 = {
        var1: '2014',
        viewMode: "years",
        minViewMode: "years",
        format: "yyyy"
      };
    }
    else if (viewMode === 'months') {
      $scope.var1 = {
        var1: '2014',
        viewMode: "years",
        minViewMode: "months",
        format: "MM/yyyy"
      };
    } else {
      $scope.var1 = {
        var1: '2014',
        viewMode: "years",
        minViewMode: "days",
        format: "MM/dd/yyyy"
      };
    }
  };
});
