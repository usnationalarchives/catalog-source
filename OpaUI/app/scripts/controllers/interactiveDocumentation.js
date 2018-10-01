'use strict';

opaApp.controller('interactiveDocumentation', function ($scope, configServices, Authentication, ResultsPublic, ExportsPublic, BulkImportPublic, TagsPublic, TagsObjectsPublic, TranscriptionPublic, Auth, OpaUtils, AccountPublic, $timeout) {

  $scope.configServices = configServices;

  $scope.registerURL = "{SERVERAPI}/users?userName={userName}&fullName={fullName}&displayFullName={display}&email={email}&password={password}&userType=standard&userRights=regular";
  $scope.loginURL = "{SERVERAPI}/login?user={userName}&password={password}";
  $scope.usernameSearchURL = "{SERVERAPI}/users?action=search&id={userName}";
  $scope.searchURL = "{SERVERAPI}?action=search";
  $scope.bulkExportURL = "{SERVERAPI}?action=export&export.bulk=true";
  $scope.exportImageTilesURL = "{SERVERAPI}/id/{naId}/objects/{objectId}/image-tiles";
  $scope.bulkImportURL = "{SERVERAPI}/bulk-imports?entity={contribution}&content={text}";
  $scope.contributionTagURL = "{SERVERAPI}/id/{naId}/tags?text={text}";
  $scope.contributionTagObjectURL = "{SERVERAPI}/id/{naId}/objects/{objectId}/tags?text={text}";
  $scope.contributionTranscriptionURL = "{SERVERAPI}/id/{naId}/objects/{objectId}/transcriptions?text={text}&action=saveAndUnlock";
  $scope.bulkExportURL = configServices.API_END_POINT + "/exports/noauth/files/";

  //GLOBAL UI variables
  $scope.apicallR = "";
  $scope.lifeMode = "false";
  $scope.onlyLiveMode = false;
  $scope.errorResponse = false;
  $scope.alertLiveMode = "Right now you are connected to the live system. Be aware that any call done here affect real data. Be careful with your tests.";

  //REGISTRATION VARIABLES
  $scope.displayFullnameInput = false;
  $scope.fullNameInput = "";
  $scope.emailInput = "";
  $scope.isNaraStaff = false;

  //SEARCH VARIABLES
  $scope.fieldNamesSearch = [
    {group: '', value: 'Select one'},
    {group: 'description', value: 'description.fileUnit'},
    {group: 'description', value: 'description.fileUnit.dataControlGroup'},
    {group: 'description.series', value: 'description.series.title'},
    {group: 'description.series', value: 'description.series.recordHistory'},
    {group: 'description.series', value: 'description.series.descriptionAuthorArray.descriptionAuthor.authorName'},
    {group: 'description.item', value: 'description.item'},
    {group: 'objects', value: 'objects.object'},
    {group: 'objects', value: 'objects.object.technicalMetadata.name'}
  ];

  //SEARCH VARIABLES
  $scope.resultFieldsSearch = [
    {group: 'description', value: 'description'},
    {group: 'description', value: 'description.naId'},
    {group: 'description', value: 'description.title'},
    {group: 'description', value: 'description.item'},
    {group: 'description', value: 'description.series'},
    {group: 'description', value: 'description.fileUnit'},
    {group: 'description', value: 'description.itemAv'},
    {group: 'description', value: 'description.collection'},
    {group: 'description', value: 'description.localIdentifier'},
    {group: 'description', value: 'description.recordGroup'},
    {group: 'description', value: 'description.recordHistory'},
    {group: 'description', value: 'description.productionDateArray'},
    {group: 'description', value: 'description.broadcastDateArray'},
    {group: 'description', value: 'description.coverageDates'},
    {group: 'description', value: 'description.inclusiveDates'},

    {group: 'person', value: 'person.birthDate'},
    {group: 'person', value: 'person.deathDate'},

    {group: 'organization', value: 'organization.termName'},
    {group: 'organization', value: 'organization.naId'},
    {group: 'organization', value: 'organization.establishDate'},
    {group: 'organization', value: 'organization.abolishDate'},

    {group: 'authority', value: 'authority'},
    {group: 'authority', value: 'authority.person'},
    {group: 'authority', value: 'authority.organization'},
    {group: 'authority', value: 'authority.specificRecordsType'},
    {group: 'authority', value: 'authority.topicalSubject'},

    {group: 'publicContributions', value: 'publicContributions'},
    {group: 'publicContributions', value: 'publicContributions.transcription'},
    {group: 'publicContributions', value: 'publicContributions.tags'},

    {group: 'objects', value: 'objects'},
    {group: 'objects', value: 'objects.object'},
    {group: 'objects', value: 'objects.object.file'},
    {group: 'objects', value: 'objects.object.thumbnail'},
    {group: 'objects', value: 'objects.object.imageTiles'},
    {group: 'objects', value: 'objects.object.technicalMetadata'},
    {group: 'objects', value: 'objects.object.publicContributions'},
    {group: 'objects', value: 'objects.object.publicContributions.tags'},
    {group: 'objects', value: 'objects.object.publicContributions.transcription'},

  ];


  $scope.sortOptions = [
    {group: '', value: 'Select one'},
    {group: 'Description', value: 'description.localIdentifier'},
    {group: 'Description', value: 'description.naId'},
    {group: 'Description', value: 'description.title'},
    {group: 'Description', value: 'description.recordHistory.created.dateTime'},
    {group: 'Description', value: 'description.recordHistory.changed.modification.dateTime'},
    {group: 'Description', value: 'description.productionDateArray.proposableQualifiableDate.logicalDate'},
    {group: 'Description', value: 'description.broadcastDateArray.proposableQualifiableDate.logicalDate'},
    {group: 'Description', value: 'description.releaseDateArray.proposableQualifiableDate.logicalDate'},
    {group: 'Description', value: 'description.coverageDates.coverageStartDate.logicalDate'},
    {group: 'Description', value: 'description.coverageDates.coverageEndDate.logicalDate'},
    {group: 'Description', value: 'description.inclusiveDates.inclusiveStartDate.logicalDate'},
    {group: 'Description', value: 'description.inclusiveDates.inclusiveStartDate.logicalDate'},
    {group: 'Person', value: 'person.termName'},
    {group: 'Person', value: 'person.naId'},
    {group: 'Person', value: 'person.birthDate'},
    {group: 'Person', value: 'person.deathDate'},
    {group: 'Organization', value: 'organization.termName'},
    {group: 'Organization', value: 'organization.naId'},
    {group: 'Organization', value: 'organization.establishDate'},
    {group: 'Organization', value: 'organization.abolishDate'},
    {group: 'topicalSubject', value: 'topicalSubject.termName'},
    {group: 'topicalSubject', value: 'topicalSubject.naId'},
    {group: 'geographicSubject', value: 'geographicSubject.termName'},
    {group: 'geographicSubject', value: 'geographicSubject.naId'},
    {group: 'specificRecordsType', value: 'specificRecordsType.termName'},
    {group: 'specificRecordsType', value: 'specificRecordsType.naId'}
  ];

  $scope.resultTypesList = [
    'holding',
    'recordGroup',
    'collection',
    'series',
    'fileUnit',
    'item',
    'object',
    'authority',
    'topicalSubject',
    'specific-records-type',
    'archivesWeb',
    'presidentialWeb'
  ];


  $scope.fieldNameSelectSearch = $scope.fieldNamesSearch[0];
  $scope.intFieldInputSearch = "";
  $scope.dateFieldInputSearch = "";
  $scope.sortSelectSearch = $scope.sortOptions[0];
  $scope.sortTypeSelectSearch = "asc";
  $scope.formatSearch = "json";
  $scope.resultTypeSearch = [];
  $scope.resultFieldInputSearch = [];
  $scope.rowsInputSearch = 20;
  $scope.offsetInputSearch = 0;


  //BULK EXPORT VARIABLES
  $scope.typeExport = "brief";
  $scope.formatExport = "json";
  $scope.imageTilesExport = false;
  $scope.whatExport = [];
  $scope.contentBulkExport = [];
  $scope.exportLinkDownload = "";
  $scope.exporId = "";

  //BULK IMPORT VARIABLES
  $scope.entitySelectImport = "";
  $scope.contentBulkImport = "";

  //CONTRIBUTIONS VARIABLES
  $scope.metadataContribution = "";
  $scope.actionContribution = 0;
  $scope.contentContributions = "";

  //CONTRIBUTION USING NAID

  $scope.contributionSelect = '';
  $scope.naidContribution = "";
  $scope.objectContribution = "";
  $scope.contentContribution = "";

  $scope.setLiveMode = function () {
    if (!Auth.isLoggedIn()) {
      OpaUtils.showMessageModalLogin().then(
        function (cancel) {
          if (cancel) {
            $scope.lifeMode = "false";
          }
        }
      );
    }
    else {
      $scope.lifeMode = "true";
    }
  };

  $scope.setMockMode = function () {
    $scope.lifeMode = "false";

  };

  //Check is it user is currently logged in.
  //if(Auth.isLoggedIn()) {
  //  $scope.lifeMode = 1;
  //}

  $scope.toggle = function (element) {
    $(element).collapse('toggle');
  };

  /*REGISTRATION*/
  $scope.tryUserRegistration = function (formValid) {
    $scope.responseTextArea = "";
    $scope.apicall = "";
    $scope.onlyLiveMode = false;
    $scope.errorResponse = false;
    $scope.action = "POST";

    if (formValid) {
      var tempCall = $scope.registerURL;

      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall.replace("{userName}", $scope.userNameInput);
      tempCall = tempCall.replace("{fullName}", $scope.fullNameInput);
      tempCall = tempCall.replace("{display}", $scope.displayFullnameInput);
      tempCall = tempCall.replace("{email}", $scope.emailInput);
      tempCall = tempCall.replace("{password}", $scope.passwordInput);
      $scope.apicall = tempCall;


      if ($scope.lifeMode === "false") {
        var isNaraStaff = false;
        var currentTime = new Date().toISOString();
        var apiResponse = '';

        if ($scope.checkNaraStaff($scope.emailInput)) {
          isNaraStaff = true;
        }
        apiResponse = apiResponse + '{"opaResponse":{"header":{"@status":"200","time":"' + currentTime;
        apiResponse = apiResponse + '",';
        apiResponse = apiResponse + '"request":{"@action":"registerUserAccount","@path":"/OpaAPI/api/v1/users","format":"json","pretty":true,';
        apiResponse = apiResponse + '"userName":"' + $scope.userNameInput + '",';
        apiResponse = apiResponse + '"userType":"standard","userRights":"regular","email":"' +
          $scope.emailInput + '","fullName":"' + $scope.fullNameInput + '",';
        apiResponse = apiResponse + '"displayFullName":' + $scope.displayFullnameInput + '}},';
        apiResponse = apiResponse + '"user":{"internalId":"INTERNAL-ID","id":"' + $scope.userNameInput + '",';
        apiResponse = apiResponse + '"type":"standard","rights":"regular","fullName":"' + $scope.fullNameInput +
          '","email":"' + $scope.emailInput + '",';
        apiResponse = apiResponse + '"displayFullName":' + $scope.displayFullnameInput + ',"status":"active","hasNote":false,"isNaraStaff":"' +
          isNaraStaff + '","accountCreatedTs":"' + currentTime + '"}}}';

        $scope.responseTextArea = JSON.stringify(jQuery.parseJSON(apiResponse), null, " ");

      }
      //CALL TO API
      else {
        var params = {};
        params.userName = $scope.userNameInput;
        if ($scope.fullNameInput) {
          params.fullName = $scope.fullNameInput;
        }
        params.displayFullName = $scope.displayFullnameInput;
        params.email = $scope.emailInput;
        params.password = $scope.passwordInput;

        var register = new AccountPublic();
        register.$register(params,
          function (data) {
            $scope.responseTextArea = JSON.stringify(data, null, " ");
          },
          function (error) {
            $scope.errorResponse = true;
            $scope.responseTextArea = JSON.stringify(error.data, null, " ");
          }
        );
      }

      $("#responseRegistration").collapse('show');
    }
  };

  $scope.checkNaraStaff = function () {
    try {
      if ($scope.emailInput.indexOf("@nara.gov") > -1) {
        $scope.isNaraStaff = true;
        $scope.displayFullnameInput = true;
        return true;
      }
      else {
        $scope.isNaraStaff = false;
        return false;
      }
    }
    catch (e) {
    }
  };

  $scope.resetRegistration = function () {
    $scope.displayFullnameInput = false;
    $scope.fullNameInput = "";
    $("#responseRegistration").collapse('hide');
  };


  /*LOGIN*/
  $scope.userLogin = function (formValid) {
    $scope.responseTextArea = "";
    $scope.apicall = "";
    $scope.onlyLiveMode = true;
    $scope.errorResponse = false;
    $scope.action = "POST";

    if (formValid) {
      var tempCall = $scope.loginURL;
      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall.replace("{userName}", $scope.userNameInputLogin);
      tempCall = tempCall.replace("{password}", $scope.passwordInputLogin);
      $scope.apicall = tempCall;

      //Call to live system
      Authentication.login({}, {'user': $scope.userNameInputLogin, 'password': $scope.passwordInputLogin},
        function (data) {
          $scope.responseTextArea = JSON.stringify(data, null, " ");
        },
        function (error) {
          $scope.errorResponse = true;
          $scope.responseTextArea = JSON.stringify(error.data, null, " ");
        }
      );

      $("#responsepanelLogin").collapse('show');

    }
  };

  $scope.resetLogin = function () {
    $("#responsepanelLogin").collapse('hide');
  };


  /*USERNAME SEARCH*/
  $scope.usernameSearch = function (formValid) {
    $scope.responseTextArea = "";
    $scope.apicallR = "";
    $scope.onlyLiveMode = true;
    $scope.errorResponse = false;
    $scope.action = "GET";

    if (formValid) {
      var tempCall = $scope.usernameSearchURL;
      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall.replace("{userName}", $scope.username);
      $scope.apicall = tempCall;

      //CALL TO PUBLIC API
      var searchUser = new AccountPublic();
      searchUser.$userNameSearch({id: $scope.username},
        function (data) {
          $scope.responseTextArea = JSON.stringify(data, null, " ");
        },
        function (error) {
          $scope.errorResponse = true;
          $scope.responseTextArea = JSON.stringify(error.data, null, " ");
        }
      );

      $("#responsepanelUsernameSearch").collapse('show');
    }
  };

  $scope.resetUsernameSearch = function () {
    $("#responsepanelUsernameSearch").collapse('hide');
  };


  /* SEARCH */
  $scope.searchApi = function (formValid) {
    $scope.responseTextArea = "";
    $scope.apicallR = "";
    $scope.onlyLiveMode = true;
    $scope.errorResponse = false;
    $scope.action = "GET";
    if (formValid) {

      var params = $scope.getSearchParameters();
      params.format = $scope.formatSearch;

      var tempCall = $scope.searchURL;
      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall + "&" + jQuery.param(params);
      $scope.apicall = tempCall;

      var query = new ResultsPublic();

      if ($scope.formatSearch === 'json') {
        query.$get(params,
          function (data) {
            $scope.responseTextArea = JSON.stringify(data, null, " ");
          },
          function (error) {
            $scope.errorResponse = true;
            $scope.responseTextArea = JSON.stringify(error.data, null, " ");
          });
      }
      else {
        query.$getXml(params,
          function (data) {
            $scope.responseTextArea = data.response;
          },
          function (error) {
            $scope.errorResponse = true;
            $scope.responseTextArea = error.data.response;
          });
      }

      $("#responsepanelSearch").collapse('show');
    }
  };

  /***
   * Prepare an object with the parameters for search
   * @returns An object with the parameters
   */
  $scope.getSearchParameters = function () {
    var params = {};

    if ($scope.qInputSearch) {
      params.q = $scope.qInputSearch;
    }
    if ($scope.naidInputSearch) {
      params.naIds = $scope.naidInputSearch;
    }
    if ($scope.objectInputSearch) {
      params.objectIds = $scope.objectInputSearch;
    }

    if ($scope.fieldNameSelectSearch !== $scope.fieldNamesSearch[0]) {
      params[$scope.fieldNameSelectSearch.value] = $scope.fieldNameInputValueSearch;
    }

    if ($scope.intFieldInputSearch) {
      params[$scope.intFieldInputSearch] = $scope.intFieldInput2Search;
    }

    if ($scope.dateFieldInputSearch) {
      params[$scope.dateFieldInputSearch] = $scope.dateFieldInput2Search;
    }

    if ($scope.sortSelectSearch !== $scope.sortOptions[0]) {
      params.sort = $scope.sortSelectSearch.value + " " + $scope.sortTypeSelectSearch;
    }

    if ($scope.resultTypeSearch.length > 0) {
      params.resultTypes = $scope.resultTypeSearch.toString();
    }

    if ($scope.machineTagsInputSearch) {
      params.machineTags = $scope.machineTagsInputSearch;
    }

    if ($scope.resultFieldInputSearch.length > 0) {
      params.resultFields = '';
      for (var i = 0; i < $scope.resultFieldInputSearch.length; i++) {
        if (i > 0) {
          params.resultFields += ',';
        }
        params.resultFields += $scope.resultFieldInputSearch[i].value;
      }
    }

    params.rows = $scope.rowsInputSearch;
    params.offset = $scope.offsetInputSearch;

    return params;
  };

  $scope.resetSearch = function () {
    $scope.naidInputSearch = "";
    $scope.fieldNameSelectSearch = $scope.fieldNamesSearch[0];
    $scope.intFieldInputSearch = "";
    $scope.dateFieldInputSearch = "";
    $scope.sortSelectSearch = $scope.sortOptions[0];
    $scope.sortTypeSelectSearch = "asc";
    $scope.formatSearch = "json";
    $scope.resultTypeSearch = [];
    $scope.resultFieldInputSearch = [];
    $scope.rowsInputSearch = 20;
    $scope.offsetInputSearch = 0;
    $("#responsepanelSearch").collapse('hide');
  };

  // BULK EXPORT
  $scope.bulkExportApi = function (validForm) {
    $scope.responseTextArea = "";
    $scope.apicallR = "";
    $scope.onlyLiveMode = true;
    $scope.errorResponse = false;
    $scope.action = "POST";

    if (validForm) {

      var tempCall = '';

      var params = $scope.getSearchParameters();

      if ($scope.whatExport.length > 0) {
        params['export.what'] = $scope.whatExport.toString();
      }
      if ($scope.typeExport) {
        params['export.type'] = $scope.typeExport;
      }
      if ($scope.contentBulkExport.length > 0) {
        params['export.bulk.content'] = $scope.contentBulkExport.toString();
      }
      if ($scope.formatExport) {
        params['export.format'] = $scope.formatExport;
      }

      if ($scope.imageTilesExport) {
        tempCall = $scope.exportImageTilesURL;
        tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
        tempCall = tempCall.replace("{naId}", $scope.naidInputSearch);
        tempCall = tempCall.replace("{objectId}", $scope.objectInputSearch);
      }
      else {
        tempCall = $scope.bulkExportURL;
        tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
        tempCall = tempCall + "&" + jQuery.param(params);
      }

      $scope.apicall = tempCall;

      if (!$scope.imageTilesExport) {
        var exports = new ExportsPublic();
        exports.$create(params,
          function (data) {
            $scope.exporId = data.opaResponse.accountExport.bulkExportId;
            $scope.exportLinkDownload = $scope.bulkExportURL + $scope.exporId;
            $scope.responseTextArea = JSON.stringify(data, null, " ");
          },
          function (error) {
            $scope.errorResponse = true;
            $scope.responseTextArea = JSON.stringify(error.data, null, " ");
          });
      }

      $("#responsePanelBulkExport").collapse('show');
    }
  };

  $scope.checkWhatExport = function () {
    if ($.inArray('metadata', $scope.whatExport) > -1) {
      return true;
    }
    return false;
  };

  $scope.resetBulkExport = function () {
    $scope.naidInputSearch = "";
    $scope.fieldNameSelectSearch = $scope.fieldNamesSearch[0];
    $scope.intFieldInputSearch = "";
    $scope.dateFieldInputSearch = "";
    $scope.sortSelectSearch = $scope.sortOptions[0];
    $scope.sortTypeSelectSearch = "asc";
    $scope.resultTypeSearch = [];
    $scope.rowsInputSearch = 20;
    $scope.offsetInputSearch = 0;

    $scope.typeExport = "brief";
    $scope.formatExport = "json";
    $scope.imageTilesExport = false;
    $scope.whatExport = [];
    $scope.contentBulkExport = [];
    $scope.exportLinkDownload = "";
    $scope.exporId = "";


    $("#responsePanelBulkExport").collapse('hide');
  };


  $scope.bulkImport = function (validForm) {
    var tempCall;
    var apiResponse;
    var currentTime;
    var parseData;
    var params = {};

    $scope.responseTextArea = "";
    $scope.apicallR = "";
    $scope.errorResponse = false;
    $scope.action = "POST";

    if (validForm) {

      tempCall = $scope.bulkImportURL;
      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall.replace("{contribution}", $scope.entitySelectImport);
      tempCall = tempCall.replace("{text}", $scope.contentBulkImport.trim().replace(/\r?\n|\r/g, ''));
      $scope.apicall = tempCall;

      //console.log($scope.contentBulkImport.replace(/\r?\n|\r/g, ""));

      if ($scope.lifeMode === "false") {
        try {
          //var json = JSON.stringify($scope.contentBulkImport.replace(/\r?\n|\r/g, ""));
          parseData = jQuery.parseJSON($scope.contentBulkImport.replace(/\r?\n|\r/g, ""));
          apiResponse = '{"opaResponse": {"header": {"@status": "200", "time": "' + new Date().toISOString() +
            '","request": { "@action": "createImport","@path": "api/v1/bulk-imports",';
          apiResponse += '"@Entity": "' + $scope.entitySelectImport + '" }}, "results": "success" }}';
          $scope.responseTextArea = JSON.stringify(jQuery.parseJSON(apiResponse), null, " ");
        }
        catch (e) {
          currentTime = new Date().toISOString();
          apiResponse = '';
          apiResponse += '{"opaResponse":{"header":{"@status":"400","time":"' + currentTime;
          apiResponse += '",';
          apiResponse += '"error":{"@code":"Error","description":"' + 'Enter Valid JSON Data' + '","format":"json","pretty":true}}}}';
          $scope.responseTextArea = JSON.stringify(jQuery.parseJSON(apiResponse), null, " ");
        }
      }
      else {
        params.entity = $scope.entitySelectImport;
        params.content = $scope.contentBulkImport.replace(/\r?\n|\r/g, '');

        var bulkImport = new BulkImportPublic();
        bulkImport.$create(params,
          function (data) {
            $scope.responseTextArea = JSON.stringify(data, null, " ");
          },
          function (error) {
            $scope.errorResponse = true;
            $scope.responseTextArea = JSON.stringify(error.data, null, " ");
          });
      }
      $("#responsePanelBulkImport").collapse('show');
    }
  };

  $scope.setContributionText = function () {

    if ($scope.entitySelectImport === "tag") {
      $scope.contentBulkImport = "{ \"tags\": [\n" +
        "  {\"naId\":[naId here], \"text\":\"Truman Post President\" },\n" +
        "  { \"naId\":[naId here], \"objectId\":[objectId here], \"text\":\"nara:president=Truman\" }]\n" +
        "}";
    }
    else if ($scope.entitySelectImport === "transcription") {

      $scope.contentBulkImport = "{ \"transcriptions\": [\n" +
        "  { \"naId\":[naId here], \"objectId\":[objectId here], \"text\":\"The Honorable Harry S. Truman My dear Mr. President: It is an honor forme, as our party's candidate for President of the United States. . \" },\n" +
        "  { \"naId\":[naId here], \"objectId\":[objectId here], \"text\":\"GEORGE McGOVERN United States Senate Washington, D.C. 20510 AIRMAIL\" }]\n" +
        "}";
    }
    else {
      $scope.contentBulkImport = "";
    }
  };

  $scope.resetBulkImport = function () {
    $scope.entitySelectImport = "";
    $scope.contentBulkImport = "";

    $("#responsePanelBulkImport").collapse('hide');
  };

  //CONTRIBUTIONS BY SEARCH METHOD

  $scope.contributionsBySearch = function (formValid) {
    $scope.responseTextArea = "";
    $scope.apicallR = "";
    $scope.errorResponse = false;
    $scope.action = "POST";
    //$scope.objectInputSearch = '';
    //$scope.naidInputSearch = '';

    if (formValid) {

      var params = $scope.getSearchParameters();
      params[$scope.metadataContribution] = $scope.contentContributions;

      var tempCall = $scope.searchURL;
      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall + "&" + jQuery.param(params);
      $scope.apicall = tempCall;

      if ($scope.lifeMode === "false") {
        var currentTime = new Date().toISOString();
        var apiResponse = '{"opaResponse": {"header": {"@status": "200", "time": "' + new Date().toISOString() + '","request": {';

        switch ($scope.metadataContribution) {
          case 'tag':
            if ($scope.actionContribution === 0) {
              $scope.action = "POST";
              apiResponse += '"@action": "saveSearch","@path": "api/v1","format": "json","pretty":true,"pageNum":0,';
              if ($scope.naidInputSearch) {
                apiResponse += '"naId":"' + $scope.naidInputSearch + '",';
              }
              else {
                apiResponse += '"naId":"some NAID",';
              }
              if ($scope.objectInputSearch) {
                apiResponse += '"objectId": "' + $scope.objectInputSearch + '",';
              }
            }
            else {
              $scope.action = "DELETE";
              apiResponse += '"@action": "deleteSearchTag","@path": "api/v1","format": "json","pretty":true,"pageNum":0,';
            }

            var tagsInfo = $scope.contentContributions.split(",");
            var tagText = '"tagText":[';
            var tags = '"tags": {"@total": "' + tagsInfo.length + '", "tag": [';
            for (var i = 0; i < tagsInfo.length; i++) {
              if (i !== 0) {
                tagText += ',';
                tags += ',';
              }
              tagText += '"' + tagsInfo[i] + '"';

              tags += '{ "@text":"' + tagsInfo[i] + '","@user": "YOUR-USERNAME","@displayFullName":"false","@isNaraStaff": "false"}';
            }

            tagText += ']}},';
            tags += ']}},"$resolved": true}';

            apiResponse += tagText + tags;

            break;
          case 'transcription':
            $scope.action = "POST";
            apiResponse += '"@path": "api/v1/id/' + $scope.naidInputSearch + "/objects/" + $scope.objectInputSearch + '/transcriptions",';
            apiResponse += '"@action":"saveAndUnlock","apiType":"api", "naId":"' + $scope.naidInputSearch + '", "objectId":"' + $scope.objectInputSearch + '",';
            apiResponse += '"accountId":"YOUR-ACCOUNT-ID","action":"saveAndUnlock", "pageNum":"1","format":"json", "pretty":true }},';
            apiResponse += '"transcription":{ "@lastModified":"' + currentTime + '", "@pageNumber":"1","isLocked":"false","@accountId":"YOUR-ACCOUNT-ID",';
            apiResponse += '"@userName":"YOUR-USERNAME","@fullName":"YOUR-FULLNAME","@displayFullName":"false","@isAuthoritative":"false",';
            apiResponse += '"@version":"VERSION-NUMBER","users":{ "@total":"No. of users used", "user":[ {"@id":"YOUR-ID","@fullName":"YOUR-FULL-NAME",';
            apiResponse += '"@displayFullName":"false",';
            apiResponse += '"@isNaraStaff":"false", "@lastModified":"' + currentTime + '"},{"@id":"ID","@fullName":"FULL-NAME","@displayFullName":"false",';
            apiResponse += '"@isNaraStaff":"false", "@lastModified":"LAST-MODIFIED-TIME"} ]},"text":"' + $scope.contentContributions + '"	 }  }}';
            break;
        }

        $scope.responseTextArea = JSON.stringify(jQuery.parseJSON(apiResponse), null, " ");
      }
      else {
        var result = new ResultsPublic();

        if ($scope.actionContribution === '2') {
          $scope.action = "DELETE";
          result.$deleteContributionsBySearch(params,
            function (data) {
              $scope.responseTextArea = JSON.stringify(data, null, " ");
            },
            function (error) {
              $scope.errorResponse = true;
              $scope.responseTextArea = JSON.stringify(error.data, null, " ");
            });
        }
        else {
          $scope.action = "POST";
          result.$contributionsBySearch(params,
            function (data) {
              $scope.responseTextArea = JSON.stringify(data, null, " ");
            },
            function (error) {
              $scope.errorResponse = true;
              $scope.responseTextArea = JSON.stringify(error.data, null, " ");
            });
        }
      }

      $("#responsePanelContributionSearch").collapse('show');
    }
  };

  $scope.changeActionContribution = function () {
    if ($scope.metadataContribution === 'transcription') {
      $scope.actionContribution = 1;
    }
    else if ($scope.metadataContribution === 'tag') {
      $scope.actionContribution = 0;
    }
    else {
      $scope.actionContribution = "";
    }
  };

  $scope.resetContributionsSearch = function () {
    $scope.naidInputSearch = "";
    $scope.metadataContribution = "";
    $scope.actionContribution = 0;
    $scope.contentContributions = "";

    $("#responsePanelContributionSearch").collapse('hide');
  };


  //CONTRIBUTION USING NAID

  $scope.contributionNaid = function (formValid) {
    $scope.responseTextArea = "";
    $scope.apicallR = "";
    $scope.errorResponse = false;
    $scope.action = "POST";

    if (formValid) {
      var tempCall = '';
      if ($scope.contributionSelect === 'tag') {
        if ($scope.naidContribution && $scope.objectContribution) {
          tempCall = $scope.contributionTagObjectURL;
        }
        else {
          tempCall = $scope.contributionTagURL;
        }
      }
      else {
        tempCall = $scope.contributionTranscriptionURL;
      }

      tempCall = tempCall.replace("{SERVERAPI}", configServices.API_END_POINT);
      tempCall = tempCall.replace("{text}", $scope.contentContribution);
      tempCall = tempCall.replace("{naId}", $scope.naidContribution);
      tempCall = tempCall.replace("{objectId}", $scope.objectContribution);
      $scope.apicall = tempCall;

      if ($scope.lifeMode === "false") {
        var currentTime = new Date().toISOString();
        var apiResponse = '{"opaResponse": {"header": {"@status": "200", "time": "' + currentTime + '","request": {';

        if ($scope.contributionSelect === 'tag') {
          if ($scope.actionContribution === 0) {
            $scope.action = "POST";
            var text = "";
            var tagsInfo = $scope.contentContribution.split(",");
            apiResponse += '"@action": "save","@path": "api/v1/id/';

            if ($scope.objectContribution) {
              apiResponse += $scope.naidContribution + "/objects/" + $scope.objectContribution + '/tags", "format": "json",' + ' "pretty": true, "naId": "';
              apiResponse += $scope.naidContribution + '","objectId": "' + $scope.objectContribution + '", "pageNum": 1, "tagText":[';
            }
            else {
              apiResponse += $scope.naidContribution + '/tags", "format": "json",' + ' "pretty": true, "naId": "' + $scope.naidContribution + '", "pageNum" : 0, "tagText":[';
            }
            var tags = '"tags": {"@total": "' + tagsInfo.length + '", "tag": [';
            for (var i = 0; i < tagsInfo.length; i++) {
              if (i !== 0) {
                text += ',';
                tags += ',';
              }
              text += '"' + tagsInfo[i] + '"';

              if ($scope.objectContribution) {
                tags += '{ "@text":"' + tagsInfo[i] + '"' + ',"@pageNum":"1"';
              }
              else {
                tags += '{ "@text":"' + tagsInfo[i] + '"';
              }
              tags += ',"@user":' + '"YOUR-USERNAME"' + ',"@displayFullName": "false","@isNaraStaff": "false","@created":"' + currentTime + '"}';
            }

            text += ']}},';
            tags += ']}}}';
            apiResponse += text + tags;
          }
          else {
            $scope.action = "DELETE";
            apiResponse += '"@action": "deleteTag","@path": "api/v1/id/' + $scope.naidContribution;

            if ($scope.objectContribution) {
              apiResponse += "/objects/" + $scope.objectContribution;
            }
            apiResponse += $scope.naidContribution + '/tags", "format": "json",' + ' "pretty": true, "naId": "' + $scope.naidContribution + '",';
            if ($scope.objectContribution) {
              apiResponse += '"objectId": "' + $scope.objectContribution + '",';
            }
            apiResponse += '"tagText":"' + $scope.contentContribution + '"}},"tag": { "@text":"' + $scope.contentContribution + '"';
            apiResponse += ',"@user":' + '"YOUR-USERNAME"' + ' ,"@displayFullName": "false","@isNaraStaff": "false","@deleted":"' + currentTime + '"}}}';
          }
        }
        /*Transcription*/
        else {
          $scope.action = "PUT";
          apiResponse += '"@path": "api/v1/id/' + $scope.naidContribution + "/objects/" + $scope.objectContribution + '/transcriptions",';
          apiResponse += '"@action":"saveAndUnlock","apiType":"api", "naId":"' + $scope.naidContribution + '", "objectId":"' + $scope.objectContribution + '",';
          apiResponse += '"accountId":"YOUR-ACCOUNT-ID","action":"saveAndUnlock", "pageNum":"1","format":"json", "pretty":true }},';
          apiResponse += '"transcription":{ "@lastModified":"' + currentTime + '", "@pageNumber":"1","isLocked":"false","@accountId":"YOUR-ACCOUNT-ID",';
          apiResponse += '"@userName":"YOUR-USERNAME","@fullName":"YOUR-FULLNAME","@displayFullName":"false","@isAuthoritative":"false",';
          apiResponse += '"@version":"VERSION-NUMBER","users":{ "@total":"No. of users used", "user":[ {"@id":"YOUR-ID","@fullName":"YOUR-FULL-NAME",';
          apiResponse += '"@displayFullName":"false",';
          apiResponse += '"@isNaraStaff":"false", "@lastModified":"' + currentTime + '"},{"@id":"ID","@fullName":"FULL-NAME","@displayFullName":"false",';
          apiResponse += '"@isNaraStaff":"false", "@lastModified":"LAST-MODIFIED-TIME"} ]},"text":"' + $scope.contentContribution + '"	 }  }}';

        }
        $scope.responseTextArea = JSON.stringify(jQuery.parseJSON(apiResponse), null, " ");
      }
      /*Call to API*/
      else {
        var params = {};
        params.text = $scope.contentContribution;

        if ($scope.contributionSelect === 'tag') {
          var tagS;
          if ($scope.objectContribution) {
            tagS = new TagsObjectsPublic({'naid': $scope.naidContribution, 'objectId': $scope.objectContribution});
          }
          else {
            tagS = new TagsPublic({'naid': $scope.naidContribution});
          }
          //Create a tag
          if ($scope.actionContribution === 0) {
            $scope.action = "POST";
            tagS.$add(params,
              function (data) {
                $scope.responseTextArea = JSON.stringify(data, null, " ");
              },
              function (error) {
                $scope.errorResponse = true;
                $scope.responseTextArea = JSON.stringify(error.data, null, " ");
              });
          }
          else {
            //Delete a tag
            $scope.action = "DELETE";
            tagS.$deleteTag(params,
              function (data) {
                $scope.responseTextArea = JSON.stringify(data, null, " ");
              },
              function (error) {
                $scope.errorResponse = true;
                $scope.responseTextArea = JSON.stringify(error.data, null, " ");
              });
          }
        }
        else {
          $scope.action = "PUT";
          var transcriptionS = new TranscriptionPublic({
            'naid': $scope.naidContribution,
            'objectId': $scope.objectContribution
          });
          transcriptionS.$save(params,
            function (data) {
              $scope.responseTextArea = JSON.stringify(data, null, " ");
            },
            function (error) {
              $scope.errorResponse = true;
              $scope.responseTextArea = JSON.stringify(error.data, null, " ");
            });
        }
      }

      $("#responsePanelContributionNaid").collapse('show');
    }
  };

  $scope.setContributionActionChange = function () {
    if ($scope.contributionSelect === 'transcription') {
      $scope.actionContribution = 1;
    }
    else if ($scope.contributionSelect === 'tag') {
      $scope.actionContribution = 0;
    }
    else {
      $scope.actionContribution = '';
    }
  };

  $scope.resetContributionNaid = function () {
    $scope.contributionSelect = '';
    $scope.actionContribution = '';
    $scope.naidContribution = "";
    $scope.objectContribution = "";
    $scope.contentContribution = "";

    $("#responsePanelContributionNaid").collapse('hide');
  };

  function hasHtml5Validation() {
    return typeof document.createElement('input').checkValidity === 'function';
  }

  if (hasHtml5Validation()) {
    $('.validate-form').submit(function (e) {
      if (!this.checkValidity()) {
        e.preventDefault();
        $(this).addClass('invalid');
        $('#status').html('invalid');
      } else {
        $(this).removeClass('invalid');
        $('#status').html('submitted');
      }
    });
  }


  $(document).ready(function () {
    /**The mean of the following code is fix a bug with the collaping panel on bootstrap. When the panel is hidden and
     * reset button is pressed (which hides the panel),the panel is shown.
     */
    $timeout(function () {
      $('#responseRegistration').collapse('show');
      $("#responsepanelLogin").collapse('show');
      $('#responsepanelUsernameSearch').collapse('show');
      $('#responsepanelSearch').collapse('show');
      $('#responsePanelBulkExport').collapse('show');
      $('#responsePanelBulkImport').collapse('show');
      $('#responsePanelContributionSearch').collapse('show');
      $('#responsePanelContributionNaid').collapse('show');
    }, 1000);

    $timeout(function () {

      $('#responseRegistration').collapse('hide');
      $("#responsepanelLogin").collapse('hide');
      $('#responsepanelUsernameSearch').collapse('hide');
      $('#responsepanelSearch').collapse('hide');
      $('#responsePanelBulkExport').collapse('hide');
      $('#responsePanelBulkImport').collapse('hide');
      $('#responsePanelContributionSearch').collapse('hide');
      $('#responsePanelContributionNaid').collapse('hide');
    }, 2000);
  });
});
