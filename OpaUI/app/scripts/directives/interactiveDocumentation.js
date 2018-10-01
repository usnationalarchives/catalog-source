'use strict';
opaApp.directive('userRegistrationApi', function () {
  return {
    restrict: 'A',
    transclude: false,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/userRegistration.html'
  };
});

opaApp.directive('loginApi', function () {
  return {
    restrict: 'A',
    transclude: false,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/login.html'
  };
});

opaApp.directive('usernameSearchApi', function () {
  return {
    restrict: 'A',
    transclude: false,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/usernameSearch.html'
  };
});


opaApp.directive('searchApi', function () {
  return {
    restrict: 'A',
    transclude: false,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/search.html'
  };
});

opaApp.directive('searchForm', function () {
  return {
    restrict: 'A',
    transclude: false,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/searchForm.html'
  };
});


opaApp.directive('responsePanel', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/responsePanel.html',
    scope: {
      action: "=",
      call: "=",
      response: "=",
      lifeMode: "=",
      onlyLiveMode: "=",
      errorResponse: "=",
      exportLinkDownload: "=",
      exporId: "="
    }
  };
});

opaApp.directive('bulkExportApi', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/bulkExport.html'
  };
});

opaApp.directive('bulkImportApi', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/bulkImport.html'
  };
});


opaApp.directive('contributionsApi', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/contributions.html'
  };
});


opaApp.directive('contributionsApiNaid', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/contributionsNaId.html'
  };
});


opaApp.directive('modeApi', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/interactivedocumentation/modes.html',
    scope: {
      id : '@'
    }
  };
});
