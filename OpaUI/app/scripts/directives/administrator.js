'use strict';

opaApp.directive('adminItem', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/administrator/adminItem.html'
  };
});

