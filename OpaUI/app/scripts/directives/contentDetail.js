'use strict';
opaApp.directive('electronicRecords', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/contentdetails/electronicRecords.html'
  };
});

opaApp.directive('tags', function () {
  return {
    restrict: 'A',
    transclude: false,
    replace: true,
    scope: {
      listType: '@tags',
      addTag: '=',
      tagText: '=',
      Tags: '=tagList',
      isLoggedIn: '=',
      showAllTags: '=',
      checkUserTag: '=',
      deleteTag: '='
    },
    templateUrl: 'views/directives/contentdetails/tags.html'
  };
});

opaApp.directive('contentDetails', function ($compile) {
  return {
    restrict: 'A',
    replace: true,
    link: function (scope, ele, attrs) {
      scope.$watch(attrs.content,
        function (html) {
          if (html) {
            ele.html(html);
            var element = $compile(ele.contents())(scope);
            element.ready(function () {
              scope.DDI = DDI;
              scope.start();
            });
          }
        });
      scope.$watch(function () {
        return ele.children().length;
      }, function () {
          // Wait for templates to render
          scope.$evalAsync(function () {
              //scope.scrollToComment();
            }
          );
        }
      );
    }
  };
});
