/*jshint -W079 */
'use strict';
var opaApp = angular.module('opaApp', ['ngCookies', 'ngResource', 'ngSanitize', 'ngRoute', 'ui.bootstrap', 'opaApiServices', 'opaPublicApiServices', 'kendo.directives', 'nouislider', 'focus-if', 'angular-quill'])
  /**
   * @name facetOrderBy
   * @filter
   * Sort the “Level of Description” category of the Refinements sidebar in reverse order of level, i.e., lowest level on top
   * Other categories are sorted using the angularjs orderBy filter.
   */
  .filter('facetOrderBy', function () {
    return function (array, expression, reverse, facetName) {
      var slice = [].slice;
      var levelOrder = {
        'item': 0,
        'fileunit': 1,
        'series': 2,
        'recordgroup': 3,
        'collection': 4
      };

      var basicCompareReverse = function (a, b) {
        return parseInt(b[expression]) - parseInt(a[expression]);
      };

      var basicCompare = function (a, b) {
        return parseInt(a[expression]) - parseInt(b[expression]);
      };

      //@returns 0 if a == b, positive if a > b and negative if a < b
      var levelCompare = function (a, b) {
        return levelOrder[a.searchEngineName] - levelOrder[b.searchEngineName];
      };

      if (array.length <= 1) {
        return array;
      }
      else if (facetName === 'level') {
        return slice.call(array).sort(levelCompare);
      }
      else if (reverse) {
        return slice.call(array).sort(basicCompareReverse);
      }
      else {
        return slice.call(array).sort(basicCompare);
      }
    };
  })
  /**
   * @name range
   * @filter
   * Fills the 'input' array with integers from 1 to 'total'.
   * The usage is like:
   *  <div ng-repeat="n in [] | range:100"> do something </div>
   **/
  .filter('range', function () {
    return function (input, total) {
      var i;
      total = parseInt(total);
      for (i = 0; i < total; i++) {
        input.push(i);
      }
      return input;
    };
  })

  /**
   * @name cleanTeaser
   * @filter
   * Removes new lines, tabs and duplicated spaces from teaser.
   */
  .filter('cleanTeaser', function () {
    return function (input) {
      var result = input;
      if (input) {
        result = input.replace(/[\n\t]/g, ' ');
        return result.replace(/[ ]+/g, ' '); //remove extra contiguous spaces
      }
      return result;
    };
  })

  /**
   * @name truncateTeaser
   * @filter
   * Cuts the teaser to the @maxLength, looking for the last @separator and appending the @endOfLine string to the end.
   */
  .filter('truncateTeaser', function () {
    var checkForSpans = function (index, lowerCaseInput) {
      var firstSpanStartIndex = lowerCaseInput.indexOf("<span");
      var spanLength = "<span class='searchterm'>".length;
      var closeSpanLength = "</span>".length;
      var spanIndex = 0;
      var startIndex = 0;
      var endIndex = 0;
      var spanIndices = [];

      //make sure no spans are cut
      if (firstSpanStartIndex !== -1 && firstSpanStartIndex < index) {
        while ((spanIndex = lowerCaseInput.indexOf("<span class=", startIndex)) > -1) {
          if (spanIndex <= index) {
            spanIndices.push(spanIndex);
            endIndex = lowerCaseInput.indexOf("</span>", spanIndex);
            index += spanLength + closeSpanLength;
            //Check no span is cut
            if (index < endIndex + closeSpanLength) {
              index = endIndex + closeSpanLength;
            }
            startIndex = endIndex + closeSpanLength;
          } else {
            break;
          }
        }
        if (index > lowerCaseInput.length) {
          index = lowerCaseInput.length;
        }
      }
      return index;
    };

    return function (input, maxLength, separator, endOfLine) {
      var threshold = 10, max = 0, result = input, lowerCaseInput = input.toLowerCase();

      if (input.length) {
        if (input.length <= maxLength || (input.length - maxLength <= threshold)) {
          max = checkForSpans(input.length, lowerCaseInput);
        } else {
          max = checkForSpans(maxLength, lowerCaseInput);
        }

        if (max === input.length) {
          return input;
        }
        result = input.substring(0, max) + endOfLine;
      }
      return result;
    };
  })

  /**
   * Capitalizes the first letter of the input string.
   */.filter('capitalize', function () {
    return function (input) {
      return input.substring(0, 1).toUpperCase() + input.substring(1);
    };
  })

  /**
   * Returns the sliced array from [start, end[
   */.filter('slice', function () {
    return function (arr, start, end) {
      return arr.slice(start, end);
    };
  })
  /**
   * Returns an encoded string, use to encode URL parameters
   * Don't re-encode characters like ' ', ':' and '='
   */.filter('encodeString', function () {
    return function (str) {
      return encodeURIComponent(str).replace(/[!'()*]/g, escape);
    };
  })
  /**
   * Returns an encoded string, use to encode URL parameters
   */.filter('decodeString', function () {
    return function (str) {
      return decodeURIComponent(str).replace(/[+]/g, ' ');
    };
  })
  /**
   * Returns a string with no smart quotes
   */.filter('removeWordCharacters', function () {
    return function (str) {
      return str.replace(/[\u201C\u201D\u201E\u201F]/g, '"')
        .replace(/[\u2018\u2019\u201A\u201B]/g, "'")
        .replace(/\u2026/g, "...")
        .replace(/\[\u0080|\u0093|\u2013|\u2014]/g, "-")
        .replace(/\u02C6/g, "^")
        .replace(/\u2039/g, "<")
        .replace(/\u203A/g, ">")
        .replace(/\u02DC/g, "~")
        .replace(/\u000B/g, "\n")
        .replace(/\u00A0/g, " ");
    };
  })

  /**
   * Returns and string with new lines replaced for spaces
   */.filter('removeNewLines', function () {
    return function (str) {
      return str.replace(/[\n]/g, ' ');
    };
  })
  /**
   * DAS sends invalid dates as 9999 for some metadata fields like Creators, NARAOPA-247 requests to delete
   * such dates from the view
   */.filter('removeInvalidDateFromString', function () {
    return function (str) {
      var result = "";
      if (str && typeof(str) === "string") {
        result = str.replace(/-[ ]*9999/, "-");
        result = result.replace(/9999[ ]*-/, "-");
      }
      return result;
    };
  })
  /**
   * Ellipse a text which has more than a maximun number of characters
   */.filter('truncateEllipse', function () {
    return function (str, maxLength) {
      if (str) {
        var result = str;
        if (result.length > maxLength) {
          result = result.substr(0, maxLength) + "...";
        }
        return result;
      }
    };
  })

  .config(function (datepickerPopupConfig) {
    datepickerPopupConfig.showButtonBar = false;
  })

  .config(function ($routeProvider, $locationProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/partials/home.html',
        controller: 'generalViewCtrl',
        requireLogin: false,
        caseInsensitiveMatch: true,
        reloadOnSearch: false,
        resolve: {
          Announcement: ['AnnouncementService', function (AnnouncementService) {
            var announcement = new AnnouncementService();
            announcement.$get({},
              function (data) {
                if (data.opaResponse.announcement && data.opaResponse.announcement['@text']) {

                  AnnouncementService.text = data.opaResponse.announcement['@text'];
                }
                return '';
              }, function (error) {
                AnnouncementService.text = '';

                return '';
              }
            );
          }],
          BackgroundImage: ['BackgroundImagesService', 'HomeService', '$window', function (BackgroundImagesService, HomeService, $window) {
            var backgroundImage = new BackgroundImagesService();
            backgroundImage.$get({
                naId: $window.localStorage.getItem('home_background_naId'),
                objectId: $window.localStorage.getItem('home_background_objectId')
              },
              function (data) {
                HomeService.image = data.opaResponse['background-image'];
                // Add 'background-size' : 'contain' for better scaling of background images
                HomeService.style = {'background': 'url(' + HomeService.image['@path'] + ') no-repeat center center fixed'};
                $window.localStorage.setItem('home_background_naId', HomeService.image['@naId']);
                $window.localStorage.setItem('home_background_objectId', HomeService.image['@objectId']);
                return '';
              },
              function (error) {
                return null;
              });
          }]
        }
      }).when('/forgotpassword', {
      templateUrl: 'views/partials/login.html',
      controller: 'loginCtrl',
      title: 'Forgot Password',
      requireLogin: false,
      resolve: {
        isLoggedIn: ['$location', 'Auth', function ($location, Auth) {
          if (Auth.isLoggedIn()) {
            $location.url($location.path('/'));
            return true;
          }
          $location.url($location.path());
          return false;
        }]
      },
      caseInsensitiveMatch: true
    }).when('/login', {
      templateUrl: 'views/partials/login.html',
      controller: 'loginCtrl',
      title: 'Login',
      requireLogin: false,
      resolve: {
        isLoggedIn: ['$location', 'Auth', function ($location, Auth) {
          if (Auth.isLoggedIn()) {
            $location.url($location.path('/'));
            return true;
          }
          $location.url($location.path());
          return false;
        }]
      },
      caseInsensitiveMatch: true
    }).when('/resetpassword', {
      templateUrl: 'views/partials/resetPassword.html',
      controller: 'loginCtrl',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/thanks', {
      templateUrl: 'views/partials/passwordReset.html',
      controller: 'loginCtrl',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/registration', {
      templateUrl: 'views/directives/commoncontrols/registration.html',
      controller: 'registrationCtrl',
      title: 'Registration',
      requireLogin: false,
      resolve: {
        isLoggedIn: ['$location', 'Auth', function ($location, Auth) {
          if (Auth.isLoggedIn()) {
            $location.path('/');
            return true;
          }
          return false;
        }]
      },
      caseInsensitiveMatch: true
    }).when('/activation', {
      templateUrl: 'views/directives/commoncontrols/activation.html',
      controller: 'activateCtrl',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/emailverify', {
      requireLogin: false,
      redirectTo: function (routeParams, path, search) {
        if (search.activationCode) {
          return '/?' + 'activationCode=' + search.activationCode + '&emailVerify';
        }
        return '/';
      },
      caseInsensitiveMatch: true
    }).when('/results', {
      templateUrl: 'views/directives/results/resultsView.html',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/mobileFacets', {
      templateUrl: 'views/directives/results/facetsMobile.html',
      controller: 'resultsCtrl',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/advancedsearch', {
      templateUrl: 'views/directives/advancedsearch/advancedSearch.html',
      controller: 'advancedSearchController',
      requireLogin: false,
      caseInsensitiveMatch: true,
      resolve: {
        //Get the list of years ranges for Congressional Records
        CongressList: ['AdvancedSearchService', function (AdvancedSearchService) {
          var congressDates = [];
          var currentYear = new Date().getFullYear();
          var congressIterator = 1;
          var beginYear = AdvancedSearchService.FIRST_CONGRESS_YEAR;
          var endYear = '';

          if (!AdvancedSearchService.congressList) {
            while (beginYear + 2 <= currentYear) {
              beginYear = (congressIterator - 1) * 2 + AdvancedSearchService.FIRST_CONGRESS_YEAR;
              endYear = beginYear + 2 > currentYear ? '' : String(beginYear + 2);
              congressDates.push({
                congressNumber: congressIterator,
                congressYears: String(beginYear) + "-" + endYear
              });
              congressIterator += 1;
            }
            AdvancedSearchService.congressList = {
              congressDates: congressDates,
              maxCongressValue: congressDates.length,
              congressRangeTo: congressDates.length
            };
          }
          return AdvancedSearchService.congressList;
        }]
      }
    }).when('/accounts/:username', {
      templateUrl: 'views/directives/account/accountHome.html',
      controller: 'myAccountCtrl',
      requireLogin: true,
      caseInsensitiveMatch: true,
      isAuthUser: true
    }).when('/accounts/:username/lists', {
      templateUrl: 'views/directives/account/listsHome.html',
      controller: 'myListsCtrl',
      requireLogin: true,
      caseInsensitiveMatch: true,
      isAuthUser: true
    }).when('/accounts/:username/lists/:listname', {
      templateUrl: 'views/directives/account/listsHome.html',
      controller: 'myListsCtrl',
      caseInsensitiveMatch: true
    }).when('/accounts/:username/contributions', {
      templateUrl: 'views/directives/account/contributions.html',
      controller: 'contributionsCtrl',
      caseInsensitiveMatch: true
    }).when('/accounts/:username/exports', {
      templateUrl: 'views/directives/account/exports.html',
      controller: 'exportsCtrl',
      requireLogin: true,
      caseInsensitiveMatch: true,
      isAuthUser: true
    }).when('/accounts/:username/notifications', {
      templateUrl: '../views/directives/account/notifications.html',
      controller: 'notificationCtrl',
      requireLogin: true,
      caseInsensitiveMatch: true,
      isAuthUser: true
    }).when('/search', {
      templateUrl: 'views/partials/generalView.html',
      controller: 'resultsCtrl',
      requireLogin: false,
      resolve: {
        doSearch: ['searchSvc', function (searchSvc) {
          searchSvc.doSearch = true;
          return searchSvc.doSearch;
        }]
      },
      caseInsensitiveMatch: true
    }).when('/moderatorWorkbench', {
      templateUrl: 'views/partials/moderatorWorkbench.html',
      controller: 'moderatorWorkbenchCtrl',
      requireLogin: true,
      requireModerator: true,
      caseInsensitiveMatch: true
    }).when('/administratorWorkbench', {
        templateUrl: 'views/partials/AccountAdministratorWorkbench.html',
        controller: 'administratorWorkbenchCtrl',
        requireLogin: true,
        requireAdmin: true,
        caseInsensitiveMatch: true
      })
      .when('/homePageManager', {
        templateUrl: 'views/partials/HomePageManager.html',
        controller: 'homePageManagerController',
        reloadOnSearch: false,
        requireLogin: true,
        requireModerator: true,
        caseInsensitiveMatch: true
      })
      .when('/id/:naId/comment/:commentId', {
        templateUrl: 'views/partials/contentDetails.html',
        controller: 'contentCtrl',
        requireLogin: false,
        reloadOnSearch: false,
        caseInsensitiveMatch: true
      })
      .when('/id/:naId', {
        templateUrl: 'views/partials/contentDetails.html',
        controller: 'contentCtrl',
        requireLogin: false,
        reloadOnSearch: false,
        caseInsensitiveMatch: true
      }).when('/arcSearch/:oldId', {
      template: '<div></div>',
      controller: 'legacyUrlsCtrl',
      resolve: {
        recordType: function () {
          return 'all';
        }
      },
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/person/:oldId', {
      template: '<div></div>',
      controller: 'legacyUrlsCtrl',
      resolve: {
        recordType: function () {
          return 'person';
        }
      },
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/organization/:oldId', {
      template: '<div></div>',
      controller: 'legacyUrlsCtrl',
      resolve: {
        recordType: function () {
          return 'organization';
        }
      },
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/specific-records-type/:oldId', {
      template: '<div></div>',
      controller: 'legacyUrlsCtrl',
      resolve: {
        recordType: function () {
          return 'specific-records-type';
        }
      },
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/topical-subject/:oldId', {
      template: '<div></div>',
      controller: 'legacyUrlsCtrl',
      resolve: {
        recordType: function () {
          return 'topical-subject';
        }
      },
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/geographic-reference/:oldId', {
      template: '<div></div>',
      controller: 'legacyUrlsCtrl',
      resolve: {
        recordType: function () {
          return 'geographic-reference';
        }
      },
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/id/:naId/:objectIndex/public', {
      templateUrl: 'views/partials/contentDetails.html',
      controller: 'contentCtrl',
      requireLogin: false,
      reloadOnSearch: false,
      caseInsensitiveMatch: true
    }).when('/statistics', {
      templateUrl: 'views/partials/statisticsPage.html',
      controller: 'statisticsCtrl',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/contentNotFound', {
      templateUrl: 'views/partials/contentNotFound.html',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).when('/InteractiveDocumentation', {
      //templateUrl: 'views/partials/interactiveDocumentation.html',
      templateUrl: 'OpaInteractiveDocumentation/index.html',
      //controller: 'interactiveDocumentation',
      requireLogin: false,
      caseInsensitiveMatch: true
    }).otherwise({
      redirectTo: '/contentNotFound',
      title: ''
    });

    // use the HTML5 History API
    $locationProvider.html5Mode(true);
  });

angular.module("template/modal/window.html", []).run(["$templateCache", function ($templateCache) {
  $templateCache.put("template/modal/window.html",
    "<div tabindex=\"-1\" role=\"dialog\" class=\"modal fade\" ng-class=\"{in: animate}\" ng-style=\"{'z-index': 1050 + index*10, display: 'block'}\" ng-click=\"close($event)\">\n" + "    <div class=\"modal-dialog\" ng-class=\"{'modal-xl': size == 'xl', 'modal-sm': size == 'sm', 'modal-lg': size == 'lg'}\"><div class=\"modal-content\" ng-transclude></div></div>\n" + "</div>");
}]);

opaApp.factory('httpInterceptor', function ($q, $rootScope, Auth, ErrorCodesSvc) {
  var numLoadings = 0;
  return {
    request: function (config) {
      if (!(config.params && config.params.noSpinner)) {
        numLoadings++;
        $rootScope.$broadcast("loader_show");
      }
      return config || $q.when(config);
    },
    response: function (response) {
      if (!(response.config.params && response.config.params.noSpinner)) {
        if ((--numLoadings) === 0) {
          $rootScope.$broadcast("loader_hide");
        }
      }
      try {
        if (response.data.opaResponse && Auth.isLoggedIn()) {
          Auth.resetTimeout();
        }
      } catch (exception) {
      }
      return response || $q.when(response);
    },
    responseError: function (response) {
      try {
        if (!(response.config.params && response.config.params.noSpinner)) {
          if (!(--numLoadings)) {
            $rootScope.$broadcast("loader_hide");
          }
        }
      } catch (error) {
        if (!(--numLoadings)) {
          $rootScope.$broadcast("loader_hide");
        }
      }
      try {
        if (response.data.opaResponse && Auth.isLoggedIn()) {
          Auth.resetTimeout();
          if (response.data.opaResponse.error['@code'] === ErrorCodesSvc.NOT_LOGGED_IN) {
            Auth.logout();
          }
        }
      } catch (exception) {
      }
      return $q.reject(response);
    }
  };
});

opaApp.config(function ($httpProvider) {
  $httpProvider.interceptors.push('httpInterceptor');
});


opaApp.run(function ($route, $rootScope, $location, $routeParams, $window, Account, Auth, configServices, OpaUtils, PublicConfigurationService, ReferrerService) {
  var apiConfigService;
  $rootScope.opaUtils = OpaUtils;
  $rootScope.showSearchBar = true;
  $rootScope.revision = configServices.REVISION;

  if (OpaUtils.isOldIE8) {
    OpaUtils.showNotSupportedBrowserDialog();
  }

  var original = $location.path;
  $location.path = function (path, reload) {
    if (reload === false) {
      var lastRoute = $route.current;
      var un = $rootScope.$on('$locationChangeSuccess', function () {
        $route.current = lastRoute;
        un();
      });
    }
    return original.apply($location, [path]);
  };
  $rootScope.$on("$routeChangeSuccess", function (event, current, previous) {
    var isIE8 = OpaUtils.isOldIE;
    var title;
    if (current.$$route && current.$$route.title) {
      title = current.$$route.title;
      if (isIE8) {
        $window.document.title = title;
      }
      else {
        $rootScope.title = title;
      }
    }
    else {
      if ($location.path() === "/search" && $routeParams.q !== null && typeof $routeParams.q !== 'undefined') {
        if ($routeParams.q === "*:*") {
          title = 'The National Archives Catalog Search';
        } else if ($routeParams.q === "") {
          title = 'The National Archives Catalog (without Search)';
        } else {
          title = $routeParams.q + " – Catalog Search";
        }
      }
      else {
        title = 'The National Archives Catalog';
      }

      if (isIE8) {
        $window.document.title = title;
      }
      else {
        $rootScope.title = title;
      }
    }
    //if (current.loadedTemplateUrl === "views/partials/home.html") {
    //  $rootScope.showSearchBar = false;
    //  $rootScope.isHome = true;
    //}
    //else

    if (current.loadedTemplateUrl === "views/directives/advancedsearch/advancedSearch.html") {
      $rootScope.showSearchBar = false;
      $rootScope.isHome = false;
    }
    else if ($location.path() === "/") {
      $rootScope.showSearchBar = false;
      $rootScope.isHome = true;
      if (!$rootScope.$$phase) {
        $rootScope.$apply();
      }
      if (current.loadedTemplateUrl !== "views/partials/home.html") {
        $route.reload();
      }
    } else {
      $rootScope.showSearchBar = true;
      $rootScope.isHome = false;
    }
  });
  $rootScope.$on("$routeChangeStart", function (event, next, current) {
    if (next.params.username && next.isAuthUser && next.params.username !== Auth.userName() && !Auth.justLoggedOut) {
      // When trying to access non authorized account home pages
      event.preventDefault();
      $location.path('/contentNotFound');
      $location.url($location.path());
    }
    else if((next.requireModerator || next.requireAdmin) && Auth.justLoggedOut){
      event.preventDefault();
      $location.path('/');
      $location.url($location.path());
    }
    else if (next.requireModerator) {
      //When the site requires moderator
      //If the user is not moderator or Admin mod => Not authorized, redirect to contentNotFound page.
      if (!Auth.isModerator() && !Auth.isAdminMod()) {
        event.preventDefault();
        $location.path('/contentNotFound');
        $location.url($location.path());
      }
    } else if (next.requireAdmin) {
      //When the site requires admin
      //If the user is not admin or Admin mod => Not authorized, redirect to contentNotFound page.
      if (!Auth.isAdministrator() && !Auth.isAdminMod()) {
        event.preventDefault();
        $location.path('/contentNotFound');
        $location.url($location.path());
      }
    } else if (next !== undefined && next.requireLogin && !Auth.isLoggedIn()) {
      //When Its no logged and the page needs to be logged in.
      event.preventDefault();
      $location.path('/login');
      $location.url($location.path());
    }
    if(Auth.justLoggedOut){
      Auth.justLoggedOut = false;
    }
  });

  /**
   * Keep track of the referrer URL, used to go back after login/registration/logout actions.
   */
  $rootScope.$on("$locationChangeStart", function (event, next, current) {
    ReferrerService.setReferrer(current, next);
  });

  $rootScope.$on("$locationChangeStart", function (event, next, current) {
    //HACK: to avoid redirect when closing the addthis modal window
    var host = window.location.protocol + '//' + window.location.host + '/';
    var isIE8 = window.navigator.userAgent.indexOf('MSIE 8.0') > -1;
    if (next === host && isIE8) {
      event.preventDefault();
      $location.path(current);
    }
  });

  /**
   * Code that creates the event handler for al links, so the UI can display
   * an message when the user access an URL that is not part of NARA
   */
  if (typeof jQuery !== 'undefined') {
    /**
     * Variable that holds the exception URLs that can be accessed without
     * displaying the "Leaving NARA" message
     * @type {*[]}
     */
    var exceptions = [
      '9-11commission.gov',
      '911commission.gov',
      'archives.gov',
      'bushlibrary.tamu.edu',
      'clintonlibrary.gov',
      'eisenhower.archives.gov',
      'fcic.gov',
      'fdrlibrary.marist.edu',
      'federalregister.gov',
      'fordlibrarymuseum.gov',
      'georgewbushlibrary.smu.edu',
      'hoover.archives.gov',
      'jfklibrary.org',
      'jimmycarterlibrary.gov',
      'lbjlibrary.org',
      'nara.gov',
      'nixonlibrary.gov',
      'ofr.gov',
      'ourdocuments.gov',
      'presidentialtimeline.org',
      'reagan.utexas.edu',
      'trumanlibrary.org',
      document.domain,
      configServices.API_LOCATION
    ];

    /**
     * Creates the actual event handler for all 'a' tags
     */
    jQuery('body').on('click', 'a', function (event) {

      /**
       * Element that was clicked
       */
      var el = jQuery(this);

      /**
       * Gets the actual link
       */
      var href = (typeof(el.attr('href')) !== 'undefined' ) ? el.attr('href') : '';

      /**
       * Test for relative URLs
       */
      var internal = href.match(/^(\/id|#|#\/|\/#\/|\/)/);

      var root = href === '/';

      var exporting = href.match(/^\/OpaAPI/);

      /**
       * Code that tests the exceptions
       * @type {boolean}
       */
      var exception = false;
      for (var i = 0; i < exceptions.length; i++) {
        if (href.indexOf(exceptions[i]) > -1) {
          exception = true;
          break;
        }
      }

      /**
       * if is not an internal URL or an exception shows the Modal
       */
      if (!root && !internal && !exception && !exporting && href.length > 0) {
        OpaUtils.showExternalUrlMessageModal(href);
        return false;
      }
    });
  }
  if (Auth.isLoggedIn()) {
    Account.view({username: Auth.userName()}, function () {
    }, function () {
    });
  }

  //Load default API configuration values
  apiConfigService = new PublicConfigurationService();
  apiConfigService.$get({}, function (data) {
    if (data.opaResponse && data.opaResponse.configuration) {
      if (data.opaResponse.configuration.maxPrintResults !== null && typeof data.opaResponse.configuration.maxPrintResults !== 'undefined') {
        configServices.PRINT_LIMIT = parseInt(data.opaResponse.configuration.maxPrintResults);
      }
      if (data.opaResponse.configuration.maxContributionRows !== null && typeof data.opaResponse.configuration.maxContributionRows !== 'undefined') {
        configServices.MAX_CONTRIBUTION_ROWS = parseInt(data.opaResponse.configuration.maxContributionRows);
      }
      if (data.opaResponse.configuration.transcriptionInactivityTime !== null && typeof data.opaResponse.configuration.transcriptionInactivityTime !== 'undefined') {
        configServices.TIMEOUT_TRANSCRIPTION = parseInt(data.opaResponse.configuration.transcriptionInactivityTime) * 60000;
      }
      if (data.opaResponse.configuration.maxSearchResultsPublic !== null && typeof data.opaResponse.configuration.maxSearchResultsPublic !== 'undefined') {
        configServices.PUBLIC_TOP_RESULTS_LIMIT = parseInt(data.opaResponse.configuration.maxSearchResultsPublic);
      }
    }
  }, function (error) {
  });

});
