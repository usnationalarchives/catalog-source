'use strict';
opaApp.directive('header', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/header.html',
    controller: 'loginCtrl'
  };
});

opaApp.directive('menuHeader', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/menuHeader.html'
  };
});

opaApp.directive('searchbar', function () {
  return {
    restrict: 'A',
    replace: true,
    templateUrl: 'views/directives/commoncontrols/searchbar.html',
    controller: function ($scope, searchSvc, Auth) {
      $scope.searchSvc = searchSvc;

      //Special case when the name to display is too long and the max screen width is 1024
      // The name is cut and the margin for loginSearchBar should be removed
      var windowWidth = window.innerWidth;
      if(Auth.getDisplayName() && windowWidth <= 1024 && Auth.getDisplayName().length >= 19){
        $('#loginSearchBar').addClass('noMargins');
      }
    }
  };
});


opaApp.directive('multibar', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/multibar.html'
  };
});

opaApp.directive('paging', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/pagination.html'
  };
});

opaApp.directive('pagingDynamic', function() {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/pagination-dynamic.html'
  };
})

opaApp.directive('designatorDescriptionRow', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/designatorAndDescriptionRow.html'
  };
});

opaApp.directive('pagingSimple', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/paginationSimple.html'
  };
});

opaApp.directive('loginPage', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    controller: 'loginCtrl',
    templateUrl: 'views/directives/commoncontrols/loginpage.html'
  };
});

opaApp.directive('registrationPage', function () {
  return {
    restrict: 'A',
    require: 'ngModel',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/registration.html'
  };
});

opaApp.directive('thankyou', function () {
  return {
    restrict: 'A',
    require: 'ngModel',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/thankyou.html'
  };
});

opaApp.directive('passwordReset', function () {
  return {
    restrict: 'A',
    require: 'ngModel',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/partials/passwordReset.html'
  };
});

opaApp.directive('email', function () {
  return {
    require: 'ngModel',
    link: function (scope, elem, attr, ngModel) {
      ngModel.$parsers.unshift(function (value) {
        var domain = value.split("@")[1];
        if (domain === "nara.gov") {
          scope.userNARA = "As a NARA Staff member your full name will be displayed to the public";
          scope.enableNARA = true;
        } else {
          scope.userNARA = "Display full name to public";
          scope.enableNARA = false;
        }
        return value;
      });
    }
  };
});

opaApp.directive('alphanumeric', function () {
  return {
    require: '?ngModel',
    link: function (scope, elem, attr, ngModelCtrl) {
      if (!ngModelCtrl) {
        return;
      }
      ngModelCtrl.$parsers.push(function (input) {
        input = String(input);
        var transformedInput = input.replace(/[^a-zA-Z0-9]+/g, '');
        if (transformedInput !== input) {
          ngModelCtrl.$setViewValue(transformedInput);
          ngModelCtrl.$render();
        }
        return transformedInput;
      });
      elem.bind('keypress', function (event) {
        if (event.keyCode === 32) {
          event.preventDefault();
        }
      });
    }
  };
});

opaApp.directive('specialchars', function () {
  return {
    require: '?ngModel',
    link: function (scope, elem, attr, ngModelCtrl) {
      if (!ngModelCtrl) {
        return;
      }
      ngModelCtrl.$parsers.push(function (input) {
        input = String(input);
        var transformedInput = input.replace(/[^a-zA-Z0-9.,'&\- ]+/g, '');
        if (transformedInput !== input) {
          ngModelCtrl.$setViewValue(transformedInput);
          ngModelCtrl.$render();
        }
        return transformedInput;
      });
    }
  };
});

opaApp.directive('ngEnter', function () {
  return function (scope, element, attrs) {
    element.bind("keydown keypress", function (event) {
      if (event.which === 13) {
        scope.$apply(function () {
          scope.$eval(attrs.ngEnter);
        });

        event.preventDefault();
      }
    });
  };
});

opaApp.directive('autoFillSync', function ($timeout) {
  return {
    require: 'ngModel',
    link: function (scope, elem, attrs, ngModel) {
      var origVal = elem.val();
      $timeout(function () {
        var newVal = elem.val();
        if (ngModel.$pristine && origVal !== newVal) {
          ngModel.$setViewValue(newVal);
        }

      }, 500);

      //Awesome Hack for IE when the browser autofill the password input. removes the gray color class.
      $(elem).focus(function () {
        var ua = window.navigator.userAgent;
        var msie = ua.indexOf("MSIE ");
        if (msie > 0) {
          $(elem).removeClass('placeholdersjs');
        }
      });
    }
  };
});

opaApp.directive('navigationTabs', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/navigationTabs.html',
    controller: function ($scope, $location, searchSvc) {

      $scope.getCounts = function () {
        searchSvc.getTabCounts($location.search(), $scope.createTooltips, $scope.destroyToolTips);
      };
      $scope.setTab = function (id, value) {
        $scope.selectedTab = id;

        $location.search('offset', undefined);

        if($scope.selectedTab === 1){
          $location.search('tabType', undefined);
        }
        else{
          $location.search('tabType', value);
        }
      };
      $scope.getCounts();
    }
  };
});

opaApp.directive('generalView', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/generalView.html'
  };
});

opaApp.directive('multibarActions', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/multibarActions.html'
  };
});

opaApp.directive('multibarActionsCommon', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/multibarActionsCommon.html'
  };
});

opaApp.directive('visor', function () {
  var uniqueId = 1000;
  return {
    restrict: 'A',
    scope: {
      hideContributionsButton: '@visor'
    },
    templateUrl: 'views/directives/commoncontrols/visor.html',
    link: function (scope, elem) {
      uniqueId = uniqueId + 1;
      scope.uniqueId = uniqueId;
      scope.playerId = "mediaplayer" + uniqueId;
      scope.visorId = 'openSeadragonViewer' + uniqueId;
      elem.find('.osVisor').attr('id', scope.visorId);
      scope.sliderId = 'slider' + uniqueId;
      elem.find('.slider-control').attr('id', scope.sliderId);
      elem.find('.mediaplayer').attr('id', scope.playerId);
      elem.find('.fa-minus-square').attr('id', 'zoom-out' + uniqueId);
      elem.find('.fa-plus-square').attr('id', 'zoom-in' + uniqueId);
      elem.find('.fa-arrows').attr('id', 'home' + uniqueId);
      elem.find('.openSeadragonControls').attr('id', 'openSeadragonControls' + uniqueId);
    }
  };
});

opaApp.directive('spinner', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/spinner.html'
  };
});


opaApp.directive("loader", function ($rootScope) {
  return function ($scope, element, attrs) {
    $rootScope.$on("loader_show", function () {
      return element.show();
    });
    return $rootScope.$on("loader_hide", function () {
      return element.hide();
    });
  };
});

opaApp.directive('addthisToolbox', function ($location, shareSvc) {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    template: '<button class="btn btn-link addthis_button_compact"><span class="glyphicons glyphicons-hospital color-orange"></span> <span class="hidden-inline-portrait">Share</span></button>',
    link: function (scope, element, attrs) {
      shareSvc.configureAddThis(element);
    }
  };
});

opaApp.directive("slideElement", function () {
  return {
    // This means the directive can be used as an attribute only. Example <div data-slide-element="variable"> </div>
    restrict: "A",

    // This is the functions that gets executed after Angular has compiled the html
    link: function (scope, element, attrs) {

      // We don't want to abuse on watch but here it is critical to determine if the parameter has changed.
      scope.$watch(attrs.slideElement, function (newValue, oldValue) {

        // This is our logic. If parameter is true slideDown otherwise slideUp.
        // TODO: This should be transformed into css transition or angular animator if IE family supports it
        if (newValue) {
          return element.slideDown();
        } else {
          return element.slideUp();
        }
      });
    }
  };
});


opaApp.directive('resizeParent', ['$window', function ($window) {
  return {
    link: function (scope, el, attrs) {
      var interval; //TODO: review if variable is needed
      var count = 1;
      var setParentHeight = function () {
        var offset = 0;
        if (attrs.offset) {
          offset = parseInt(attrs.offset);
        }
        var newSize = el.outerHeight() + offset;
        if (attrs.min) {
          var min = parseInt(attrs.min);
          if (newSize < min) {
            newSize = min;
          }
        }
        el.parent().height(newSize);
        if (interval && count) {
          count--;
        } else if (interval && !count) {
          clearInterval(interval);
        }
      };
      interval = setInterval(setParentHeight, 1000);

      angular.element($window).on('resize', function () {
        setParentHeight();
        clearInterval(interval);
      });
    }
  };
}]);

opaApp.directive('comments', function () {
  return {
    restrict: 'A',
    scope: false,
    replace: true,
    templateUrl: 'views/directives/commoncontrols/comments.html'
  };
});

opaApp.directive('comment', function (OpaUtils) {
  return {
    restrict: 'A',
    scope: {
      comment: '=',
      isReply: '=',
      refreshComments: '&',
      naId: '=naid',
      parentId: '=',
      parentIndex: '='
    },
    replace: true,
    templateUrl: 'views/directives/commoncontrols/comment.html',
    controller: 'commentController'
  };
});

