var opaApiServices = angular.module('opaApiServices', ['opaAppConfig']);

/*
 * Calls to the API for managing own account and account information in the National Archives Catalog
 */
opaApiServices.factory("Account", ["$resource", "configServices", "Auth", function ($resource, configServices, Auth) {
  return $resource(configServices.IAPI_END_POINT + "/accounts/:action/:username",
    {},
    {
      activate: {method: "GET", params: {action: 'verifyemail', format: 'json', pretty: 1}},
      deactivate: {method: "PUT", params: {action: 'deactivate', username: Auth.userName}, withCredentials: true},
      deleteNotifications: {method: "DELETE", params: {action: 'notifications'}, withCredentials: true},
      getNotifications: {method: "GET", params: {action: 'notifications'}, withCredentials: true},
      modify: {method: "POST", params: {action: 'modify', username: Auth.userName}, withCredentials: true},
      recover: {method: "GET", params: {action: 'forgotname'}},
      register: {
        method: "POST",
        params: {action: 'register'},
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          var str = [];
          var p;
          for (p in data) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(data[p]));
          }
          return str.join("&");
        }
      },
      requestReset: {method: "POST", params: {action: 'requestpasswordreset'}},
      reset: {method: "POST", params: {action: 'setnewpassword'}},
      summary: {method: "GET", params: {action: 'summary', username: Auth.userName}, withCredentials: true},
      view: {method: "GET", params: {action: 'profile', username: Auth.userName}, withCredentials: true},
      resendverification: {method: "GET", params: {action: 'resendverification'}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for actions that require administrator permissions
 */
opaApiServices.factory("Administrator", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/administrator/accounts/:action",
    {},
    {
      register: {method: "POST", params: {action: 'register'}, withCredentials: true},
      search: {method: "GET", params: {action: 'search'}, withCredentials: true},
      viewReasons: {method: "GET", params: {action: 'reasons'}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing accounts as an administrator
 */
opaApiServices.factory("ManageAccounts", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/administrator/accounts/:action/:accountOwner",
    {accountOwner: "@accountOwner"},
    {
      modify: {method: "POST", params: {action: 'modify'}, withCredentials: true},
      reactivateAccount: {method: "PUT", params: {action: 'reactivate'}, withCredentials: true},
      deactivateAccount: {method: "PUT", params: {action: 'deactivate'}, withCredentials: true},
      requestpasswordreset: {method: "POST", params: {action: 'requestpasswordreset'}, withCredentials: true},
      viewAccountNotes: {method: "GET", params: {action: 'notes'}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for listing contributions from a specified user
 */
opaApiServices.factory("Contributions", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/contributions/:action/:titles",
    {},
    {
      getContributions: {method: "GET", params: {action: 'summary'}, withCredentials: true},
      getTags: {method: "GET", params: {action: 'tags'}, withCredentials: true},
      getComments: {method: "GET", params: {action: 'comments'}, withCredentials: true},
      getTranscriptions: {method: "GET", params: {action: 'transcriptions', titles: 'titles'}, withCredentials: true},
      getSummary: {method: "GET", params: {action: 'summary'}, withCredentials: true},
      getTaggedTitles: {method: "GET", params: {action: 'tags', titles: 'titles'}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for creatin and listing exports
 */
opaApiServices.factory("Exports", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/exports/:auth/:action",
    {},
    {
      create: {
        method: "POST",
        params: {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          var str = [];
          var p;
          for (p in data) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(data[p]));
          }
          return str.join("&");
        }
      },
      getSummary: {method: "GET", params: {auth: 'auth', action: 'summarystatus'}, withCredentials: true},
      get: {method: "GET", params: {auth: 'auth'}, withCredentials: true},
      deleteExport: {method: "DELETE", params: {auth: 'auth'}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for printing
 */
opaApiServices.factory("Print", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/exports/:auth",
    {},
    {
      create: {
        method: "POST",
        params: {},
        withCredentials: true,
        isArray: true,
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

/*
 * Call to the API for logout within an actual session
 */
opaApiServices.factory("Logout", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/logout",
    {},
    {
      logout: {method: "POST", isArray: false, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for getting results from a search with specific parameters and filters
 */
opaApiServices.factory("Results", ["$resource", "configServices", function ($resource, configServices) {
  //NARAOPA-395: Merge locations 20 and 59, and remove location 35
  var processLocations = function (data, headers) {
    var LOCATION_TO_EXCLUDE = '35';
    var LOCATION_MERGED = '20';
    var LOCATION_TO_MERGE = '59';
    var response = angular.fromJson(data);
    var index = 0;
    var subIndex = 0;
    var facet = {};
    var locationMergedIndex = -1;
    var locationToMergeIndex = -1;
    var locationToExcludeIndex = -1;

    if (response && response.opaResponse && response.opaResponse.facets &&
      response.opaResponse.facets.field && response.opaResponse.facets.field.length) {
      for (index = 0; index < response.opaResponse.facets.field.length; index++) {
        if (response.opaResponse.facets.field[index]['@name'] === 'locationIds') {
          facet = response.opaResponse.facets.field[index];
          for (subIndex = 0; subIndex < facet.v.length; subIndex++) {
            if (facet.v[subIndex]['@name'] === LOCATION_MERGED) {
              locationMergedIndex = subIndex;
            } else if (facet.v[subIndex]['@name'] === LOCATION_TO_MERGE) {
              locationToMergeIndex = subIndex;
            } else if (facet.v[subIndex]['@name'] === LOCATION_TO_EXCLUDE) {
              locationToExcludeIndex = subIndex;
            }
          }
          if (locationMergedIndex !== -1 && locationToMergeIndex !== -1) {
            facet.v[locationMergedIndex]['@count'] = String(parseInt(facet.v[locationMergedIndex]['@count']) + parseInt(facet.v[locationToMergeIndex]['@count']));
            facet.v.splice(locationToMergeIndex, 1);
            if (locationToExcludeIndex !== -1 && locationToMergeIndex < locationToExcludeIndex) {
              locationToExcludeIndex -= 1;
            }
          } else if (locationMergedIndex === -1 && locationToMergeIndex !== -1) {
            facet.v[locationToMergeIndex]['@name'] = LOCATION_MERGED;
          }
          if (locationToExcludeIndex !== -1) {
            facet.v.splice(locationToExcludeIndex, 1);
          }
          break;
        }
      }
    }
    return response;
  };

  return $resource(configServices.IAPI_END_POINT,
    {},
    {
      get: {'method': "GET", params: {action: 'search'}, withCredentials: true, transformResponse: processLocations},
      getXml: {
        'method': "GET",
        params: {action: 'search'},
        withCredentials: true,
        transformResponse: function (value) {
          var obj = {};
          obj.response = value;
          return obj;
        }
      },
      searchWithin: {method: 'GET', params: {action: 'searchWithin'}, transformResponse: processLocations},
      searchTag: {method: 'GET', params: {action: 'searchTag'}, transformResponse: processLocations}
    });
}
]);
/*
 * Calls to the API for managing and listing lists for an specific logged in user
 */
opaApiServices.factory("Lists", ["$resource", "Auth", "configServices", function ($resource, Auth, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/lists/:operation/:list",
    {list: "@list"},
    {
      addToList: {
        method: "POST",
        params: {operation: 'add'},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          var str = [];
          var p;
          for (p in data) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(data[p]));
          }
          return str.join("&");
        }
      },
      addTopResultToList: {method: "POST", params: {operation: 'add'}, withCredentials: true},
      create: {method: "POST", params: {operation: 'create'}, withCredentials: true},
      get: {method: "GET", params: {operation: 'view'}, withCredentials: true},
      getList: {method: "GET", params: {operation: 'viewentries', username: Auth.userName}, withCredentials: true},
      deleteLists: {method: "DELETE", params: {operation: 'deleteall'}, withCredentials: true},
      deleteList: {method: "DELETE", params: {operation: 'delete'}, withCredentials: true},
      deleteItems: {method: "DELETE", params: {operation: 'delete'}, withCredentials: true},
      update: {method: "PUT", params: {operation: 'rename'}, withCredentials: true}
    });
}]);

/*
 * Calls to the API for managing tags for a description with a proper NARA id
 */
opaApiServices.factory("Tags", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/id/:naid/tags",
    {naid: '@naid'},
    {
      add: {
        'method': "POST",
        'params': {},
        withCredentials: true,
        'headers': {'Content-Type': 'application/x-www-form-urlencoded'},
        'transformRequest': function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      deleteTag: {'method': "DELETE", 'params': {}, withCredentials: true},
      getAllTags: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing tags for objects inside a description
 */
opaApiServices.factory("TagsObjects", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/id/:naid/objects/:objectId/tags",
    {naid: '@naid', objectId: '@objectId'},
    {
      add: {
        'method': "POST",
        'params': {},
        withCredentials: true,
        'headers': {'Content-Type': 'application/x-www-form-urlencoded'},
        'transformRequest': function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      deleteTag: {'method': "DELETE", 'params': {}, withCredentials: true},
      getAllTags: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing comments for objects inside a description
 */
opaApiServices.factory("CommentsObjectService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/id/:naid/objects/:objectId/comments/:commentId/:replyId",
    {naid: '@naid', objectId: '@objectId', replyId: '@replyId',commentId: '@commentId'},
    {
      add: {
        'method': "POST",
        'params': {},
        'withCredentials': true,
        'headers': {'Content-Type': 'application/x-www-form-urlencoded'},
        'transformRequest': function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      get: {'method': "GET", 'params': {}, withCredentials: true},
      delete: {'method': "DELETE", 'params': {}, withCredentials: true},
      update: {
        'method': "PUT",
        'params': {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      }
    });
}
]);


/*
 * Calls to the API for managing transcriptions for objects inside a description
 */
opaApiServices.factory("Transcription", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/id/:naid/objects/:objectId/transcriptions",
    {naid: '@naid', objectId: '@objectId'},
    {
      action: {
        'method': "PUT",
        'params': {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      get: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for getting information from content details based on a NARA id
 */
opaApiServices.factory("Content", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/id/:naid",
    {naid: '@naid'},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

opaApiServices.factory("ContentWithSearchQuery", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT,
    {},
    {
      get: {'method': "GET", 'params': {action: 'contentDetail'}, withCredentials: true}
    });
}
]);
/*
 * Call to the API for getting stream for moderators
 */
opaApiServices.factory("ModeratorStream", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/stream",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for getting/setting home page announcements for moderators
 */
opaApiServices.factory("ModeratorAnnouncementService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/announcements",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true},
      save: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for getting/setting home page background images for moderators/administrators
 */
opaApiServices.factory("ModeratorBackgroundImagesService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/background-image",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true},
      add: {'method': "POST", 'params': {}, withCredentials: true},
      remove: {'method': "DELETE", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for getting contributions for moderators
 */
opaApiServices.factory("ModeratorContributions", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/contributionTotals",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing tags inside an object as a moderator
 */
opaApiServices.factory("ModeratorTagsObjects", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/tags/id/:naid/objects/:objectId",
    {naid: '@naid', objectId: '@objectId'},
    {
      remove: {'method': "DELETE", 'params': {}, withCredentials: true},
      restore: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing tags inside a description as a moderator
 */
opaApiServices.factory("ModeratorTags", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/tags/id/:naid",
    {naid: '@naid'},
    {
      remove: {'method': "DELETE", 'params': {}, withCredentials: true},
      restore: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing comments inside a description as a moderator
 */
opaApiServices.factory("ModeratorComments", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/comments/id/:naid/:commentId",
    {naid: '@naid', commentId: '@commentId'},
    {
      remove: {'method': "DELETE", 'params': {}, withCredentials: true},
      restore: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing comments inside a object as a moderator
 */
opaApiServices.factory("ModeratorCommentsObjects", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/comments/id/:naid/objects/:objectId/:commentId",
    {naid: '@naid', objectId: '@objectId', commentId: '@commentId'},
    {
      remove: {'method': "DELETE", 'params': {}, withCredentials: true},
      restore: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);


/*
 * Calls to the API for adding and listing reasons for Moderator to report a contribution
 */
opaApiServices.factory("ModeratorReasons", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/contributions/reasons",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true},
      add: {'method': "POST", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for adding and listing Administrator reasons
 */
opaApiServices.factory("AdministratorReasons", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/administrator/accounts/reasons",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true},
      add: {'method': "POST", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for managing transcriptions inside an object as a moderator
 */
opaApiServices.factory("ModeratorTranscriptions", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/transcriptions/id/:naid/objects/:objId",
    {naid: '@naid', objId: '@objId'},
    {
      remove: {'method': "DELETE", 'params': {}, withCredentials: true},
      restore: {'method': "PUT", 'params': {}, withCredentials: true},
      getVersion: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/**
 * Call to the API to get the statistics
 */
opaApiServices.factory("Statistics", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/statistics",
    {},
    {
      getStatistics: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

opaApiServices.factory("LogController", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/logclick",
    {},
    {
      logClick: {'method': "POST", 'params': {}, withCredentials: true}
    });
}
]);

opaApiServices.factory("LegacyUrls", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/urlmapping/:recordType/:id",
    {},
    {
      getNewNaId: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

opaApiServices.factory("PublicConfigurationService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/public/configuration",
    {},
    {
      get: {'method': "GET", 'params': {}}
    });
}
]);

opaApiServices.service('CommentsService', ['$resource', 'configServices', function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/id/:naId/comments/:commentId/:replyId",
    {naId: '@naId', commentId: '@commentId', replyId: '@replyId'},
    {
      getComments: {'method': "GET", 'params': {}},
      addComment: {
        'method': "POST",
        'params': {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      deleteComment: {'method': "DELETE", 'params': {}, withCredentials: true},
      updateComment: {
        'method': "PUT",
        'params': {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      addCommentReply: {
        'method': "POST",
        'params': {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      updateCommentReply: {
        'method': "PUT",
        'params': {},
        withCredentials: true,
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          if (data.text) {
            return (encodeURIComponent('text') + "=" + encodeURIComponent(data.text));
          }
        }
      },
      deleteCommentReply: {'method': "DELETE", 'params': {}, withCredentials: true}
    });
}]);


/*
 * Call to the API for getting/setting home page for public announcements
 */
opaApiServices.factory("AnnouncementService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/announcements",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Call to the API for getting the home page background image
 */
opaApiServices.factory("BackgroundImagesService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/background-image",
    {},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true}
    });
}
]);

/*
 * Calls to the API for managing own account and account information in the National Archives Catalog
 */
opaApiServices.factory("VerifyEmailService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/accounts/verifyemailchange",
    {},
    {verifyEmailChange: {method: "POST", params: {format: 'json', pretty: 1}}}
  );
}
]);

/*
 * Call to the API for manage Online Availability Notifications
 */
opaApiServices.factory("OnlineAvailabilityService", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/online-availability/:naId",
    {naId: '@naId'},
    {
      get: {'method': "GET", 'params': {}, withCredentials: true},
      enable: {'method': "POST", 'params': {}, withCredentials: true},
      saveNotification: {
        'method': "POST",
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        'params': {},
        withCredentials: true,
        transformRequest: function (data) {
          if (data.header) {
            return ('header' + "=" + encodeURIComponent(data.header));
          }
        }
      }
    });
}
]);

/*
 * Call to the API for managing transcriptions inside an object as a moderator
 */
opaApiServices.factory("ModeratorOnlineAvailability", ["$resource", "configServices", function ($resource, configServices) {
  return $resource(configServices.IAPI_END_POINT + "/moderator/online-availability/:naid",
    {naid: '@naid'},
    {
      remove: {'method': "DELETE", 'params': {}, withCredentials: true},
      restore: {'method': "PUT", 'params': {}, withCredentials: true}
    });
}
]);

