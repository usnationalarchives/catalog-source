'use strict';
opaApp.directive('listItem', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/account/listItem.html'
  };
});

opaApp.directive('contributionsCommon', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/account/contributionsCommon.html'
  };
});
