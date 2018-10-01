'use strict';
opaApp.directive('customCalendar', function () {
  return {
    restrict: 'A',
    require: 'ngModel',
    controller: 'calendarCtrl',
    link: function (scope, element, attrs, ngModelCtrl) {
      element.datetimepicker({
        format: scope.var1.format,
        viewMode: scope.var1.viewMode,
        minViewMode: scope.var1.minViewMode,
        pickTime: false
      }).on('changeDate', function (e) {
        ngModelCtrl.$setViewValue(e.date);
        scope.$apply();
      });
    }
  };
});