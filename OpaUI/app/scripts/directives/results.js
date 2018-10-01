'use strict';
opaApp.directive('result', function ($log, $filter, searchSvc, $compile, $location, $window) {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/result.html',

    compile: function (element, attrs) {
      return function (scope, element, attrs, controller) {

        var valueIndex;
        var newRow;
        var creatorIndex;
        var cleanTitle;
        var cleanTeaser = "";
        var resultJson = scope.result;
        var singleResultRow = "";
        var lineItem = {};
        var subIndex = 0;
        var classTemp = "";
        var isWeb = false;
        var lineItemToDisplay = "";
        var sortedMetaData = [];
        var sortedTitleLine = [];
        var selectedResult = 0;

        if (resultJson) {
          //check if this is a web result
          if (resultJson.tabType && resultJson.tabType.indexOf("web") !== -1) {
            isWeb = true;
          }

          selectedResult = Number(scope.offset);
          if(!isNaN(selectedResult)){
            selectedResult += Number(scope.$index);
          } else {
            selectedResult = Number(scope.$index);
          }

          singleResultRow += "<div class='descriptionResult col-lg-11 col-sm-10 col-xs-8'>";
          //Read title line
          if (resultJson.briefResults.titleLine) {
            sortedTitleLine = $filter('orderBy')(resultJson.briefResults.titleLine, '+order', false);
            singleResultRow += "<div>";
            if (sortedTitleLine.length) {
              for (subIndex = 0; subIndex < sortedTitleLine.length; subIndex++) {
                lineItemToDisplay = "";
                lineItem = sortedTitleLine[subIndex];
                classTemp = scope.checkVisibility(lineItem.priority);
                if (subIndex > 0) {
                  lineItemToDisplay += "<span class='" + classTemp + "'>, </span>";
                }
                //Set the class of the line item
                singleResultRow += "<span ";
                classTemp += lineItem.name === 'title' ? " titleResult" : " dateResult";
                if (classTemp) {
                  singleResultRow += "class='" + classTemp + "'";
                }
                singleResultRow += ">";

                if (lineItem.label) {
                  lineItemToDisplay += lineItem.label + ": ";
                }

                if (lineItem.name === 'title') {
                  cleanTitle = $filter('removeInvalidDateFromString')(lineItem.value);
                  if (isWeb) {
                    lineItemToDisplay += "<a ng-click=\"count('" + resultJson.opaId + "')\" href='" + resultJson.contentDetailUrl + "' target='_blank'>" + cleanTitle + "</a>";
                  }
                  else if (attrs.searchwithin) {
                    lineItemToDisplay += "<a href='/id/" + resultJson.naId + "'>" + cleanTitle + "</a>";
                  }
                  else {
                    lineItemToDisplay += "<a href='/id/" + resultJson.naId + "?" +
                      "&sp=" + encodeURIComponent($window.sessionStorage.getItem("sp")).replace(/'/g, "&apos;").replace(/"/g, '&quot;') +
                      "&sr=" + selectedResult +
                      "'>" + cleanTitle + "</a>";
                  }
                }
                else if (lineItem.name === 'date') {
                  lineItemToDisplay += $filter('removeInvalidDateFromString')(lineItem.value);
                }
                else {
                  lineItemToDisplay += lineItem.value;
                }
                singleResultRow += lineItemToDisplay;
                singleResultRow += "</span>";
              }
            }
            else if (!isWeb) {
              singleResultRow += "<a href='/id/" + resultJson.naId + "?" +
                "&sp=" + encodeURIComponent($window.sessionStorage.getItem("sp")).replace(/'/g, "&apos;").replace(/"/g, '&quot;') +
                "&sr=" + selectedResult +
                "'>" + resultJson.naId + "</a>";
            }
            singleResultRow += "</div>";
          }

          if (resultJson.hierachy) {
            singleResultRow += "<div class=\"record-hierarchy\">" + resultJson.hierachy + "</div>";
          }

          //webarea is a special case
          if (isWeb) {
            for (subIndex = 0; subIndex < resultJson.briefResults.metadataArea.length; subIndex++) {
              if (resultJson.briefResults.metadataArea[subIndex].name &&
                resultJson.briefResults.metadataArea[subIndex].name === 'webArea') {
                singleResultRow += "<div class='webArea " + scope.checkVisibility(resultJson.briefResults.metadataArea[subIndex].priority) + "'>" +
                  resultJson.briefResults.metadataArea[subIndex].value + "</div>";
                break;
              }
            }
          }

          //TODO: load teaser size from config maybe?
          cleanTeaser = $filter('cleanTeaser')(resultJson.teaser);
          var teaserChunk = "<div class='summaryResult hidden-xs hidden-sm hidden-md'>" + $filter('truncateTeaser')(cleanTeaser, 240, ' ', '...') + "</div>";

          var recordFrom = "";

          var resultDetail = "";

          //Read brief results metadata array
          if (resultJson.briefResults.metadataArea) {
            sortedMetaData = $filter('orderBy')(resultJson.briefResults.metadataArea, '+order', false);
            resultDetail += "<div class='resultDetail'>";
            for (subIndex = 0; subIndex < sortedMetaData.length; subIndex++) {
              lineItemToDisplay = "";
              lineItem = sortedMetaData[subIndex];
              classTemp = scope.checkVisibility(lineItem.priority);
              if (lineItem.name === "webArea") {
                continue;
              }
              if (lineItem.name !== "creators" && lineItem.name !== "recordGroupNumber" &&
                lineItem.name !== "collectionIdentifier" && lineItem.name !== "seriesTitle") {
                lineItemToDisplay += "<span class='" + classTemp + "'>";
                if (lineItem.order > 1) {
                  lineItemToDisplay += " ";
                }

                lineItemToDisplay += "<span class='bold'>";
                if (lineItem.label) {
                  lineItemToDisplay += lineItem.label;
                  lineItemToDisplay += ":</span>&nbsp";
                }
                if ($.type(lineItem.value) === "array" && lineItem.value.length > 0) {
                  lineItemToDisplay += lineItem.value[0];
                  for (valueIndex = 1; valueIndex < lineItem.value.length; valueIndex++) {
                    lineItemToDisplay += ", " + lineItem.value[valueIndex];
                  }
                } else {
                  lineItemToDisplay += lineItem.value;
                }
                lineItemToDisplay += "</span>&nbsp";
              }
              else if (lineItem.name === "recordGroupNumber" ||
                lineItem.name === "collectionIdentifier" || lineItem.name === "seriesTitle") {
                if (lineItem.name === "seriesTitle") {
                  recordFrom += "<div><span class='record-from'>" + lineItem.value + "</span></div>";
                } else {
                  recordFrom += "<div><span class='record-from'>" + lineItem.label + ": " + lineItem.value + "</span></div>";
                }
              }
              //Creators
              else {
                lineItemToDisplay += "<div><span class='" + classTemp + "'>";
                lineItemToDisplay += "<span class='bold'>";
                if (lineItem.label) {
                  lineItemToDisplay += lineItem.value.length > 1 ? "Creators:" : "Creator:";
                }
                lineItemToDisplay += "</span>&nbsp";
                lineItemToDisplay += $filter('removeInvalidDateFromString')(lineItem.value[0]);
                for (creatorIndex = 1; creatorIndex < lineItem.value.length; creatorIndex++) {
                  lineItemToDisplay += "; " + $filter('removeInvalidDateFromString')(lineItem.value[creatorIndex]);
                }
                lineItemToDisplay += "</span></div>";
              }
              resultDetail += lineItemToDisplay;
            }
            resultDetail += "</div>";
          }

          singleResultRow += recordFrom;
          singleResultRow += teaserChunk;
          singleResultRow += resultDetail;

          singleResultRow += "</div>";
          newRow = $compile(singleResultRow)(scope);
          element.append(newRow);
        }
      };
    }
  };
});

opaApp.directive('resultLookup', function ($log, $filter, searchSvc) {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/resultLookup.html',

    compile: function (element, attrs) {
      return function (scope, element, attrs, controller) {
        /*$log.info(new Date().getTime() + ": Drawing naraId: " + scope.result.naId + " scope: " + scope.$id);*/
        var newRow;
        var creatorIndex;
        var resultJson = scope.result;
        var singleResultRow = "";
        var lineItem = {};
        var subIndex = 0;
        var lineItemToDisplay = "";
        var sortedMetaData = [];
        var sortedTitleLine = [];
        var date = "";

        if (resultJson) {
          //check if this is a web result
          if (resultJson.tabType && resultJson.tabType.indexOf("web") !== -1) {
            $(element).empty();
            return;
          }

          singleResultRow += "<div class='descriptionResult col-xs-8'>";
          //Read title line
          if (resultJson.briefResults.titleLine) {
            sortedTitleLine = $filter('orderBy')(resultJson.briefResults.titleLine, '+order', false);
            singleResultRow += "<div>";
            for (subIndex = 0; subIndex < sortedTitleLine.length; subIndex++) {
              lineItemToDisplay = "";
              lineItem = sortedTitleLine[subIndex];
//                            classTemp = scope.checkVisibility(lineItem.priority);
//                            if (subIndex > 0)
//                                lineItemToDisplay += "<span class='"+classTemp+"'>, </span>";

              if (lineItem.name === 'date') {
                date = lineItem.value;
                continue;
              }
              else {
                //Set the class of the line item
                singleResultRow += "<span class='titleResult'>";

                if (lineItem.label) {
                  lineItemToDisplay += lineItem.label + ": ";
                }

                if (lineItem.name === 'title') {
                  lineItemToDisplay += "<a href='#/id/" + resultJson.naId + "?q=" + $filter('encodeString')(searchSvc.getQuery()) + "' target='_blank'>" + lineItem.value + "</a>";
                }
                else {
                  lineItemToDisplay += lineItem.value;
                }
                singleResultRow += lineItemToDisplay;
                singleResultRow += "</span>";
              }
            }
            singleResultRow += "</div>";
          }

          //Read brief results metadata array
          if (resultJson.briefResults.metadataArea) {
            sortedMetaData = $filter('orderBy')(resultJson.briefResults.metadataArea, '+order', false);
            singleResultRow += "<div class='resultDetail'>";
            for (subIndex = 0; subIndex < sortedMetaData.length; subIndex++) {
              lineItemToDisplay = "";
              lineItem = sortedMetaData[subIndex];
//                            if(lineItem.name === "webArea"){
//                                continue;
//                            }
              if (lineItem.name !== "creators") {
                if (lineItem.order > 1) {
                  lineItemToDisplay += ", ";
                }

                lineItemToDisplay += "<span class='bold'>";
                if (lineItem.label) {
                  lineItemToDisplay += lineItem.label;
                  lineItemToDisplay += ":</span>&nbsp";
                }
                lineItemToDisplay += lineItem.value;
              }
              //Creators
              else {
                lineItemToDisplay += "<div class='resultDetail'>";
                lineItemToDisplay += "<span class='bold'>";
                if (lineItem.label) {
                  lineItemToDisplay += lineItem.value.length > 1 ? "Creators:" : "Creator:";
                }
                lineItemToDisplay += "</span>&nbsp";
                lineItemToDisplay += lineItem.value[0];
                for (creatorIndex = 1; creatorIndex < lineItem.value.length; creatorIndex++) {
                  lineItemToDisplay += ", " + lineItem.value[creatorIndex];
                }
                lineItemToDisplay += "</div>";
              }
              singleResultRow += lineItemToDisplay;
            }
            singleResultRow += "</div>";
          }

          singleResultRow += "</div>";

          if (date !== "") {
            singleResultRow += "<div class='dateLookupResult col-xs-2'>";
            singleResultRow += date;
            singleResultRow += "</div>";
          }


          newRow = $(singleResultRow);
          element.append(newRow);
        }
      };
    }
  };
});

opaApp.directive('resultWebGroup', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/resultWebGroup.html'
  };
});

opaApp.directive('resultWeb', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/resultWeb.html'
  };
});

opaApp.directive('resultsView', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/resultsView.html'
  };
});

opaApp.directive('noResults', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/noResults.html'
  };
});

opaApp.directive('facets', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/facets.html'
  };
});

opaApp.directive('stickyFacetFilters', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/stickyFacetFilters.html'
  };
});

opaApp.directive('searchWithin', function ($compile) {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/results/searchWithin.html',
    link: function (scope, ele, attrs) {
      scope.$watch(attrs.content, function (result) {
        if (result) {
          var element = $compile('<div data-result data-searchwithin="true" ng-repeat="result in results"></div>')(scope);
          $('.result-directive').replaceWith(element);
        }
      });
    }
  };
});
