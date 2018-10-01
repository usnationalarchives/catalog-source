'use strict';

opaApp.directive('advancedsearch', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/advancedsearch/advancedSearch.html'
  };
});

opaApp.directive('archivalDescriptions', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/advancedsearch/archivalDescriptions.html',
    link: function postLink(scope, iElement, iAttrs) {
      // Trigger when number of children changes, including by directives like ng-repeat, ng-options, etc.
      var watch = scope.$watch(function () {
        return iElement.children().length;
      }, function () {
        // Wait for templates to render
        scope.$evalAsync(function () {
          // Finally, directives are evaluated and templates are rendered here
          var opt;
          var i;
          //Add thumbnails to the Level of Descriptions options.
          for (i = 0; i < scope.levelOfDescriptions.length; i++) {
            if (scope.levelOfDescriptions[i].Tooltip) {
              opt = $(iElement.find('#levelOfDescriptions option')[i]);
              opt.attr("title", scope.levelOfDescriptions[i].Tooltip);
              opt.html(opt.html() + '<span class="sr-only">' + scope.levelOfDescriptions[i].Tooltip + '</span>');
              opt = null;
            }

          }
        });
      });
    }
  };
});

opaApp.directive('presidentialViceElectronicRecords', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/advancedsearch/PresidentialViceElectronicRecords.html'
  };
});


opaApp.directive('rangeDatesAdvanceSearch', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/advancedsearch/rangeDates.html',
    controller: 'calendarSearchCtrl',
    scope: {
      dateRange: '=',
      disabled: '=',
      authorityRecordSelected: "="
    },
    link: function postLink(scope, iElement) {
      var years;
      years = $(iElement.find('.calendarKendo'));
      years.attr("maxlength", 4);
    }
  };
});
