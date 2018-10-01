opaApp.service("ListsService", function (Auth, Lists, $log, OpaUtils) {

  this.selectedRecords = [];
  this.selectedAllIndex = [];
  this.listCount = 100;

  //Get list by its name
  this.GetList = function (params) {
    var lists = new Lists();
    //Get user's lists
    var result;
    result = lists.$getList(params,
      function (data) {},
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
    return result;
  };

  //Get all the user's lists
  this.GetAllLists = function () {
    var lists = new Lists();
    //Get user's lists
    var result;
    result = lists.$get({'rows': this.listCount},
      function (data) {
        if (data.opaResponse) {
          result = data.opaResponse.userLists.userList;
          result["@total"] = data.opaResponse.userLists["@total"];
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
    return result;
  };

  //Rename a specific list
  this.RenameList = function (listname, newname) {
    var lists = new Lists();
    //Get user's lists
    var result = lists.$update({'listname': listname, 'newname': newname}, function (data) {},
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      });
    return result;
  };

  //Rename a specific list
  this.DeleteSelectedListItems = function (listname, listItems) {
    var lists = new Lists({'list': listname});
    //Get user's lists
    var result = lists.$deleteItems({'what': listItems}, function (data) {},
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      });
    return result;
  };

  //Delete all the user's lists
  this.DeleteAllLists = function () {
    var lists = new Lists();
    var result;
    result = lists.$deleteLists({},
      function (data) {
        if (!data.opaResponse) {
          $log.info("No data");
        }
        else {
          if (data.opaResponse.header["@status"] === 200) {
            $log.info("deleted successfully");
          }
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
    return result;
  };

  //Delete a list by its name
  this.DeleteList = function (listname) {
    var lists = new Lists({'list': listname});
    var result;
    result = lists.$deleteList({},
      function (data) {
        if (!data.opaResponse) {
          $log.info("No data");
        }
        else {
          if (data.opaResponse.header["@status"] === 200) {
            $log.info("deleted successfully");
          }
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
    return result;
  };
});

