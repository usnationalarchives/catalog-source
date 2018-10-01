"use strict";

opaApp.controller('legacyUrlsCtrl', function ($scope, $location, recordType, $routeParams, LegacyUrls) {
  var legacyUrls = new LegacyUrls();
  legacyUrls.$getNewNaId({'recordType': recordType, 'id': $routeParams.oldId}, function (response) {
    var totalResults, i;
    var q = [];

    if(response.opaResponse){

      totalResults = parseInt(response.opaResponse.naIds['@total']);

      if(isNaN(totalResults)) {
        totalResults = 0;
      }

      if(totalResults === 1) {
        $location.path('/id/' + response.opaResponse.naIds.naId[0]['@naId']);
      }
      else if(totalResults > 0) {
        for(i = 0; i < totalResults; i++){
          q.push(response.opaResponse.naIds.naId[i]['@naId']);
        }
        q = q.join(" or ");
        $location.search('q', q);
        $location.path('/search/');
      }
      else {
        $location.path('/contentNotFound');
      }
    }
  }, function (error) {
    $location.path('/contentNotFound');
  });
});
