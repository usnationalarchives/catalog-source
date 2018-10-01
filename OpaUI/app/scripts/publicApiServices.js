var opaPublicApiServices = angular.module('opaPublicApiServices', ['opaAppConfig']);

/*
 * Calls to the API for managing own account and account information in the National Archives Catalog
 */
opaPublicApiServices.factory("AccountPublic", ["$resource", "configServices",function ($resource, configServices, Auth) {
  return $resource(configServices.API_END_POINT + "/users",
    {},
    {
      register: {method: "POST", params: {userType:'standard',userRights:'regular'}},
      userNameSearch: {method: "GET", params: {action:'search'}}
    });
}
]);


/*
 * Calls to the API for creating and listing exports
 */
opaPublicApiServices.factory("ExportsPublic", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT,
    {},
    {
      create: {method: "POST", params: {action:'export','export.bulk':true}, withCredentials: true,
        transformResponse: function(value){
          //var obj = {};
          //obj.response = angular.fromJson(value);
          return angular.fromJson(value);
        }}
    });
}
]);

/*
 * Calls to the API for Import contributions
 */
opaPublicApiServices.factory("BulkImportPublic", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT + "/bulk-imports",
    {},
    {
      create: {method: "POST", params: {}, withCredentials: true}
    });
}
]);


/*
 * Calls to the API for getting results from a search with specific parameters and filters
 */
opaPublicApiServices.factory("ResultsPublic", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT,
    {},
    {
      get: {'method': "GET", params: {action: 'search'}, withCredentials: true},
      contributionsBySearch: {'method': "POST", params: {action: 'search'}, withCredentials: true},
      deleteContributionsBySearch: {'method': "DELETE", params: {action: 'search'}, withCredentials: true},
      getXml: {'method': "GET", params: {action: 'search'}, withCredentials: true,
        transformResponse: function(value){
          var obj = {};
          obj.response = value;
          return obj;
        }}
    });
}
]);


/*
 * Calls to the API for managing tags for a description with a proper NARA id
 */
opaPublicApiServices.factory("TagsPublic", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT + "/id/:naid/tags",
    {naid: '@naid'},
    {
      add: {'method': "POST", 'params': {}, withCredentials: true},
      deleteTag: {'method': "DELETE", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing tags for objects inside a description
 */
opaPublicApiServices.factory("TagsObjectsPublic", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT + "/id/:naid/objects/:objectId/tags",
    {naid: '@naid', objectId: '@objectId'},
    {
      add: {'method': "POST", 'params': {}, withCredentials: true},
      deleteTag: {'method': "DELETE", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing transcriptions for objects inside a description
 */
opaPublicApiServices.factory("TranscriptionPublic", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT + "/id/:naid/objects/:objectId/transcriptions",
    {naid: '@naid', objectId: '@objectId'},
    {
      save: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for login with an already registered user
 */
opaPublicApiServices.factory("Authentication", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.API_END_POINT + "/login",
    {},
    {
      login: {method: "POST", params: {}, withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          var str = [];
          var p;
          for (p in data) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(data[p]));
          }
          return str.join("&");
        }
      }
    });
}
]);


