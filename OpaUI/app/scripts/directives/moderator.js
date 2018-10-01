'use strict';
opaApp.directive('tagTable', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/tagTable.html'
  };
});

opaApp.directive('tagItem', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/tagItem.html'
  };
});

opaApp.directive('editionTag', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    controller: 'editionModeratorCtrl',
    templateUrl: 'views/directives/moderator/editionTag.html',
    scope: {
      'type': '=',
      'contribution': "=",
      'index': '='
    }
  };
});

opaApp.directive('commentTable', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/commentTable.html'
  };
});

opaApp.directive('commentItem', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/commentItem.html'
  };
});

opaApp.directive('editionComment', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    controller: 'editionModeratorCtrl',
    templateUrl: 'views/directives/moderator/editionComment.html',
    scope: {
      'type': '=',
      'contribution': "=",
      'index': '='
    }
  };
});


opaApp.directive('editionTranscription', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/editionTranscription.html'
  };
});


opaApp.directive('transcriptionTable', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/transcriptionTable.html'
  };
});

opaApp.directive('transcriptionItem', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/transcriptionItem.html'
  };
});


opaApp.directive('moderatorActionTable', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/moderatorActionTable.html'
  };
});

opaApp.directive('moderatorActionItem', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/moderatorActionItem.html'
  };
});


opaApp.directive('onlineAvailabilityTable', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/moderator/onlineAvailabilityTable.html'
  };
});
