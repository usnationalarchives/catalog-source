opaApp.service("TagService", function ($filter, $timeout, Auth, OpaUtils, Tags, TagsObjects) {


  //This function add a new tag
  this.addTag = function (naid, text) {

    var tag = new Tags({'naid': naid, 'text': $filter('removeWordCharacters')(text.trim())});
    var tags;
    tags = tag.$add({},
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );

    return tags;
  };

  //This function add a new tag
  this.deleteTag = function (naid, text) {

    var tag = new Tags({'naid': naid});
    var params;
    var tags;

    params = {text: $filter('removeWordCharacters')(text.trim())};

    tags = tag.$deleteTag(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );

    return tags;
  };

  //This function returns all the tags
  this.getAllTags = function (naid) {
    var tag = new Tags({'naid': naid});
    var params = {};
    var tags;

    tags = tag.$getAllTags(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );
    return tags;
  };


  //OBJECT TAGS
  //This function add a new tag
  this.addTagObject = function (naid, objectid, text, pageNum) {

    var tag = new TagsObjects({'naid': naid, 'objectId': objectid, 'text': $filter('removeWordCharacters')(text.trim())});
    var params = {};
    var tags;

    params = {pageNum: pageNum};

    tags = tag.$add(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );
    return tags;
  };

  //This function add a new tag
  this.deleteTagObject = function (naid, objectid, text) {

    var tag = new TagsObjects({'naid': naid, 'objectId': objectid});
    var params;
    var tags;

    params = {text: $filter('removeWordCharacters')(text.trim())};

    tags = tag.$deleteTag(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );

    return tags;
  };

//This function returns all the tags for an specific object
  this.getAllTagsObject = function (naid, objectid) {
    var tagObject = new TagsObjects({'naid': naid, 'objectId': objectid});
    var params = {};
    var tags;

    tags = tagObject.$getAllTags(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );
    return tags;
  };


//This function creates the tooltip for each tag
  this.createTooltips = function (tags, tagsName) {

    $timeout(function () {

      angular.forEach(tags, function (tag, index) {
        var tagId = "#" + tagsName + index;
        var name = "";
        var date = OpaUtils.fancyDate(tag['@created']);
        if (tag['@isNaraStaff'] === "true") {
          name = tag['@fullName'] ? tag['@fullName'] : tag['@user'];
          name += " (NARA Staff)";
        }
        else if (tag['@displayFullName'] === "true" && tag['@fullName']) {
          name = tag['@fullName'];
        }
        else {
          name = tag['@user'];
        }

        $(tagId).kendoTooltip({
          autoHide: true,
          content: "<div><a target='_blank' href='#/accounts/" + tag['@user'] + "/contributions?contributionType=tags''>" + name + "</a> <span>" + date + "</span></div>"
        });
      });
    }, 500);
  };

  //Check is a tag was created by the current user
  //In the UI is highlighted
  this.checkUserTag = function (tag) {
    return Auth.userName() === tag['@user'];
  };
});
