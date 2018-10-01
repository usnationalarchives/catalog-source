'use strict';

//Directive that can be used as a fallback when the ngSource fails to load an image
opaApp.directive('errSrc', function () {
  return {
    link: function (scope, element, attrs) {
      element.bind('error', function () {
        element.attr('src', attrs.errSrc);
      });
    }
  };
});
