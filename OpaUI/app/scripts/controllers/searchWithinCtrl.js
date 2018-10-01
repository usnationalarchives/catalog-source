'use strict';

opaApp.controller('searchWithinCtrl', function ($scope, searchSvc) {
  $scope.query = searchSvc.getQuery();
  $scope.searchWithinObject = searchSvc.searchWithinObject;
  $scope.results = [];
  $scope.resultReady = false;
  $scope.isSearchWithin = true;

  searchSvc.searchWithinCallback = function () {
    $scope.results = [];
    $scope.results.push($scope.searchWithinObject.searchWithin);
    $scope.resultReady = true;
  };

  $scope.showBox = function () {
    return !($.isEmptyObject($scope.searchWithinObject.searchWithin) || !$scope.searchWithinObject.show);
  };

  $scope.close = function () {
    $scope.searchWithinObject.show = false;
  };
});
