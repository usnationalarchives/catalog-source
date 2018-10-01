'use strict';

opaApp.controller('advancedSearchController', function ($scope, $location, $routeParams, $rootScope, SearchFilters, searchSvc, OpaUtils, AdvancedSearchService, dateService) {

  /**
   * Reload filters applied in brief results page.
   */
  var DATE_MAP = AdvancedSearchService.DATE_MAP;

  /**
   * Options for kendo calendar
   */
  $scope.calendarOptions = {
    format: 'MM/dd/yyyy',
    parseFormats: ['MM-dd-yyyy', 'MM/dd/yyyy', 'MM-dd-yy', 'MM/dd/yy'],
    start: 'decade',
    depth: 'day',
    min: new Date(1700, 0, 1)
  };


  /**
   * The init function initialized the controller state.
   */
  var init = function () {
    initScope();
    loadParameters();
  };

  /**
   * Init the sources to limit the search to.
   * @param changeFn If passed, the function gets called after setting each source.
   */
  var initScopeSources = function (changeFn) {
    $scope.archivalDescriptionSelected = true;
    $scope.onlineMaterialsSelected = true;
    $scope.authorityRecordSelected = false;
    $scope.webPagesSelected = false;

    $scope.SourceItems = AdvancedSearchService.sources; //Set source items.

    //Add the Selected property to each data source
    angular.forEach($scope.SourceItems, function (item, index) {
      switch (item.Value) {
        case AdvancedSearchService.sourceFiltersDictionary.ARCHIVAL_DESCRIPTIONS.value:
          item.Selected = $scope.archivalDescriptionSelected;
          break;
        case AdvancedSearchService.sourceFiltersDictionary.ONLINE_ARCHIVAL_DESCRIPTIONS.value:
          item.Selected = $scope.onlineMaterialsSelected;
          break;
        case AdvancedSearchService.sourceFiltersDictionary.AUTHORITY_RECORDS.value:
          item.Selected = $scope.authorityRecordSelected;
          break;
        case AdvancedSearchService.sourceFiltersDictionary.WEB.value:
          item.Selected = $scope.webPagesSelected;
          break;
        default:
          item.Selected = false;
      }
      if (changeFn) {
        changeFn(index);
      }
    });
  };

  /**
   * Init scope variables that can be reset by the user as well when pushing the reset button.
   */
  var initResettableScopeVariables = function () {
    $scope.whiteHouseDropdown = '0';
    $scope.name = '';
    $scope.userTags = '';
    $scope.titleAdvancedSearch = '';
    $scope.geograficRef = '';
    $scope.recordGroupNumber = '';
    $scope.creator = '';
    $scope.DescriptionId = '';

    $scope.from = '';
    $scope.to = '';
    $scope.cc = '';
    $scope.bcc = '';
    $scope.subject = '';

    $scope.photographer = '';
    $scope.city = '';
    $scope.state = '';
    $scope.country = '';
    $scope.title2 = '';
    $scope.frameNumber = '';
    $scope.rollNumber = '';
    $scope.caption = '';
    $scope.keywords = '';

    $scope.lastName = '';
    $scope.firstName = '';
    $scope.middleInitial = '';
    $scope.visitorLastName = '';
    $scope.visitorFirstName = '';
    $scope.callerLastName = '';
    $scope.callerFirstName = '';

    $scope.caseNumber = '';

    $scope.errorFlag = false;

    //Congress Slider Range
    $scope.enableCongressionalRecords = true;
    $scope.congressRangeFrom = 1;

    //Common calendar model
    $scope.dateRange = {
      currentTab: 'range',
      dayFromValue: 'DD',
      dayToValue: 'DD',
      monthFromValue: 'MM',
      monthToValue: 'MM',
      yearFromValue: '',
      yearToValue: '',
      dayOpenValue: 'DD',
      monthOpenValue: 'MM',
      exactMonthValue: 'MM',
      exactDayValue: 'DD',
      exactYearValue: ''
    };

  };

  /**
   * Initializes scope variables
   */
  var initScope = function () {

    $scope.AdvancedSearchService = AdvancedSearchService;
    $scope.sections = AdvancedSearchService.sections;
    $scope.toolTips = AdvancedSearchService.toolTips;

    initScopeSources();
    initResettableScopeVariables();

    $scope.enablePresidentialElectronicRecords = false;
    $scope.searchTerm = searchSvc.getQuery();
    $scope.source = true;

    $scope.TypeArchivalMaterialsList = SearchFilters.TypeArchivalMaterialsList(); //Setting drop down values for Archival Descriptions.
    $scope.levelOfDescriptions = SearchFilters.LevelOfDescriptions(); //Setting drop down values for level of descriptions
    $scope.fileFormat = SearchFilters.FileFormat(); //Setting drop down values for file format
    $scope.ArchivalMaterialsLocation = SearchFilters.Location(); //Setting drop down values for Archival Materials Location

    //Congress Slider Range
    $scope.minCongressValue = 1;
    $scope.maxCongressValue = 999999;
    $scope.congressRangeTo = 1;
    $scope.yearsCongressBegin = '';
    $scope.yearsCongressEnd = '';

    //Retrieve the range of congress years
    if (AdvancedSearchService.congressList) {
      $scope.congressDates = AdvancedSearchService.congressList.congressDates;
      $scope.maxCongressValue = AdvancedSearchService.congressList.maxCongressValue;
      $scope.congressRangeTo = AdvancedSearchService.congressList.congressRangeTo;
    }

    //Logic to enable/disable the 'Presidential/Vice Presidential Electronic Records Fields' depending on the locations selected
    $scope.$watch('ArchivalMaterialsLocation.selectedValues', function (newValue, oldValue) {
      var i;
      var enablePresidentialElectronicRecords = false;
      var enableCongressionalRecords = false;

      if (newValue && newValue.length) {
        for (i = 0; i < newValue.length; i++) {
          if (!enablePresidentialElectronicRecords) {
            enablePresidentialElectronicRecords =false;
          }
          if (!enableCongressionalRecords) {
            enableCongressionalRecords = $.inArray(newValue[i], AdvancedSearchService.locationsWithCongressionalRecords) !== -1;
          }
          if (enablePresidentialElectronicRecords && enableCongressionalRecords) {
            break;
          }
        }
      }
      else {
        enablePresidentialElectronicRecords = false;
        enableCongressionalRecords = true;
      }
      $scope.enableCongressionalRecords = enableCongressionalRecords;
      $scope.enablePresidentialElectronicRecords = false;
    });

    $scope.$watch('congressRangeFrom', function () {
      if ($scope.congressDates) {
        if ($scope.congressRangeFrom && $scope.congressDates.length > 0 && parseInt($scope.congressRangeFrom) <= $scope.maxCongressValue) {
          $scope.yearsCongressBegin = ($scope.congressDates[parseInt($scope.congressRangeFrom) - 1]).congressYears;
        }
      }
    });

    $scope.$watch('congressRangeTo', function () {
      if ($scope.congressDates) {
        if ($scope.congressRangeTo && $scope.congressDates.length > 0 && parseInt($scope.congressRangeTo) <= $scope.maxCongressValue) {
          $scope.yearsCongressEnd = ($scope.congressDates[parseInt($scope.congressRangeTo) - 1]).congressYears;
        }
      }
    });
  };

  /**
   * Check that calendars has a valid date
   */
  $scope.validateCalendars = function () {

  };

  /**
   * Parse dateArray of strings from URL and saves it into the same array as numbers. Returns null if date is invalid
   * @param dateArray
   * @returns {Function}
   */
  var parseDateFromUrl = function (dateArray) {
    var validDate = true;

    if (dateArray && dateArray.length === DATE_MAP.VALID_LENGTH) {
      dateArray[DATE_MAP.YEAR] = parseInt(dateArray[DATE_MAP.YEAR]); //year
      dateArray[DATE_MAP.MONTH] = parseInt(dateArray[DATE_MAP.MONTH]); //month
      dateArray[DATE_MAP.DAY] = parseInt(dateArray[DATE_MAP.DAY]); //day

      //Check that date is complete, and has acceptable values
      if (isNaN(dateArray[DATE_MAP.YEAR]) || isNaN(dateArray[DATE_MAP.MONTH]) || isNaN(dateArray[DATE_MAP.DAY]) ||
        dateArray[DATE_MAP.MONTH] < 0 || dateArray[DATE_MAP.MONTH] > 12 || dateArray[DATE_MAP.DAY] < 0 || dateArray[DATE_MAP.DAY] > 31) {

        validDate = false;
      }
    }
    else {
      validDate = false;
    }

    if (!validDate) {
      dateArray = null;
    }
    return dateArray;
  };

  var loadParameters = function () {
    var congressNumbers;
    var oldScopeFilters = [];
    var descriptionId;
    var creator;
    var recordGroupNumber;
    var geographicRef;
    var titleAdvancedSearch;
    var authorityToCalendar, authorityFromCalendar;
    var descriptionToCalendar, descriptionFromCalendar;
    var tags;
    var authorityPOName, descriptionPOName;
    var beginCongress, endCongress;
    var recurringDateDay, recurringDateMonth;
    var beginDate, endDate;
    var exactDate;


    if (searchSvc.filterParams) {
      descriptionPOName = searchSvc.filterParams['f.descriptionPOName'];
      authorityPOName = searchSvc.filterParams['f.authorityPOName'];
      if(searchSvc.filterParams['f.machineTags']) {
        tags = searchSvc.filterParams['f.machineTags'];
      } else {
        tags = searchSvc.filterParams['f.tagsKeywordsAdv'];
      }
      descriptionFromCalendar = searchSvc.filterParams['f.descriptionStartYear'];
      descriptionToCalendar = searchSvc.filterParams['f.descriptionEndYear'];
      authorityFromCalendar = searchSvc.filterParams['f.authorityStartYear'];
      authorityToCalendar = searchSvc.filterParams['f.authorityEndYear'];
      titleAdvancedSearch = searchSvc.filterParams['f.allTitles'];
      geographicRef = searchSvc.filterParams['f.geographicReferences'];
      recordGroupNumber = searchSvc.filterParams['f.recordGroupNoCollectionId'];
      creator = searchSvc.filterParams['f.creators'];
      descriptionId = searchSvc.filterParams['f.descriptionIdentifier'];
      recurringDateDay = searchSvc.filterParams['f.recurringDateDay'];
      recurringDateMonth = searchSvc.filterParams['f.recurringDateMonth'];
      beginDate = searchSvc.filterParams['f.beginDate'];
      endDate = searchSvc.filterParams['f.endDate'];
      exactDate = searchSvc.filterParams['f.exactDate'];

      //Load sources selected
      if (searchSvc.filterParams['f.oldScope']) {
        oldScopeFilters = SearchFilters.removeParenthesis(searchSvc.filterParams['f.oldScope']).split(' or ');
        //$scope.onlineMaterialsSelected = $.inArray(AdvancedSearchService.sourceFiltersDictionary.ONLINE_ARCHIVAL_DESCRIPTIONS.value, oldScopeFilters) !== -1;
        angular.forEach($scope.SourceItems, function (source, index) {
          source.Selected = $.inArray(source.Value, oldScopeFilters) !== -1;
          switch (source.Name) {
            /*case AdvancedSearchService.sourceFiltersDictionary.ARCHIVAL_DESCRIPTIONS.name:
             source.Selected = source.Selected || $scope.onlineMaterialsSelected;
             break;*/
            case AdvancedSearchService.sourceFiltersDictionary.WEB.name:
              source.Selected = source.Selected ||
                $.inArray(AdvancedSearchService.sourceFiltersDictionary.WEB.value, oldScopeFilters) !== -1 ||
                $.inArray(AdvancedSearchService.sourceFiltersDictionary.PRESIDENTIAL.value, oldScopeFilters) !== -1;
              break;
          }
          $scope.selectedSourceChange(index);
        });
      }

      //Load multi options
      AdvancedSearchService.loadOptions($scope.levelOfDescriptions, 'f.level');
      AdvancedSearchService.loadOptions($scope.ArchivalMaterialsLocation, 'f.locationIds');
      AdvancedSearchService.loadOptions($scope.fileFormat, 'f.fileFormat');
      AdvancedSearchService.loadOptions($scope.TypeArchivalMaterialsList, 'f.materialsType');

      //Congress cannot be 0
      if (searchSvc.filterParams['f.congressRange']) {
        congressNumbers = searchSvc.filterParams['f.congressRange'].split('-');
        if (congressNumbers.length === 2) {
          beginCongress = parseInt(congressNumbers[0]);
          endCongress = parseInt(congressNumbers[1]);
        }
      }
    }
    //load Person or Organization
    if (descriptionPOName) {
      $scope.name = descriptionPOName;
    }
    else if (authorityPOName) {
      $scope.name = authorityPOName;
    }

    if (tags) {
      $scope.userTags = tags;
    }

    if (titleAdvancedSearch) {
      $scope.titleAdvancedSearch = titleAdvancedSearch;
    }

    if (geographicRef) {
      $scope.geograficRef = geographicRef;
    }

    if (recordGroupNumber) {
      $scope.recordGroupNumber = recordGroupNumber;
    }

    if (creator) {
      $scope.creator = creator;
    }

    if (descriptionId) {
      $scope.DescriptionId = descriptionId;
    }

    if (beginCongress && !(isNaN(beginCongress)) && endCongress && !(isNaN(endCongress)) && beginCongress <= endCongress &&
      beginCongress >= $scope.minCongressValue && beginCongress <= $scope.maxCongressValue &&
      endCongress >= $scope.minCongressValue && endCongress <= $scope.maxCongressValue) {

      $scope.congressRangeFrom = beginCongress;
      $scope.congressRangeTo = endCongress;
    }

    if (recurringDateDay || recurringDateMonth) {
      $scope.dateRange.currentTab = 'open';
      recurringDateDay = parseInt(recurringDateDay);
      recurringDateMonth = parseInt(recurringDateMonth);
      if (!isNaN(recurringDateDay) && recurringDateDay > 0 && recurringDateDay <= 31) {
        $scope.dateRange.dayOpenValue = recurringDateDay;
      }
      if (!isNaN(recurringDateMonth) && recurringDateMonth > 0 && recurringDateMonth <= 12) {
        $scope.dateRange.monthOpenValue = recurringDateMonth;
      }
      dateService.validateDates($scope.dateRange);
    }
    else if (beginDate || endDate) {
      $scope.dateRange.currentTab = 'range';
      if (beginDate) {
        beginDate = beginDate.split('-');
        beginDate = parseDateFromUrl(beginDate);
        if (beginDate !== null) {
          $scope.dateRange.dayFromValue = beginDate[DATE_MAP.DAY];
          $scope.dateRange.monthFromValue = beginDate[DATE_MAP.MONTH];
          $scope.dateRange.yearFromValue = beginDate[DATE_MAP.YEAR];
        }
      }
      if (endDate) {
        endDate = endDate.split('-');
        endDate = parseDateFromUrl(endDate);
        if (endDate !== null) {
          $scope.dateRange.dayToValue = endDate[DATE_MAP.DAY];
          $scope.dateRange.monthToValue = endDate[DATE_MAP.MONTH];
          $scope.dateRange.yearToValue = endDate[DATE_MAP.YEAR];
        }
      }
      dateService.validateDates($scope.dateRange);
    }
    else if (exactDate) {
      $scope.dateRange.currentTab = 'exact';
      exactDate = exactDate.split('-');
      exactDate = parseDateFromUrl(exactDate);
      if (exactDate !== null) {
        $scope.dateRange.exactDayValue = exactDate[DATE_MAP.DAY];
        $scope.dateRange.exactMonthValue = exactDate[DATE_MAP.MONTH];
        $scope.dateRange.exactYearValue = exactDate[DATE_MAP.YEAR];
      }
      dateService.validateDates($scope.dateRange);
    }
  };


  /**
   * Syncs the model variables with the selected/deselected source checkbox
   * @param {Number} index Index of the source selected/deselected in the SourceItems array.
   */
  $scope.selectedSourceChange = function (index) {
    var source = $scope.SourceItems[index];
    switch (source.Name) {
      case AdvancedSearchService.sourceFiltersDictionary.ARCHIVAL_DESCRIPTIONS.name:
        $scope.archivalDescriptionSelected = source.Selected;
        break;
      case AdvancedSearchService.sourceFiltersDictionary.ONLINE_ARCHIVAL_DESCRIPTIONS.name:
        $scope.onlineMaterialsSelected = source.Selected;
        break;
      case AdvancedSearchService.sourceFiltersDictionary.AUTHORITY_RECORDS.name:
        $scope.authorityRecordSelected = source.Selected;
        break;
      case AdvancedSearchService.sourceFiltersDictionary.WEB.name:
        $scope.webPagesSelected = source.Selected;
        break;
    }


    //Reset date controls when only authority records is selected
    if($scope.showSection($scope.sections.ONLY_AUTHORITY)){
      $scope.dateRange.exactDayValue =  'DD';
      $scope.dateRange.exactMonthValue = 'MM';
      $scope.dateRange.exactYearValue = 'YYYY';

      $scope.dateRange.dayOpenValue =  'DD';
      $scope.dateRange.monthOpenValue = 'MM';

      $scope.dateRange.currentTab = 'range';
    }
  };

  /**
   * Specify if a section should be visible or not.
   * @param section from $scope.sections
   * @returns {boolean}
   */
  $scope.showSection = function (section) {
    var show = false;
    switch (section) {
      case $scope.sections.AUTHORITY:
        show = !$scope.archivalDescriptionSelected && $scope.authorityRecordSelected && !$scope.webPagesSelected;
        break;
      case $scope.sections.DESCRIPTIONS:
        show = ($scope.archivalDescriptionSelected || $scope.onlineMaterialsSelected) && !$scope.authorityRecordSelected && !$scope.webPagesSelected;
        break;
      case $scope.sections.PRESIDENTIAL:
        show = false;
        break;
      case $scope.sections.CONGRESS:
        //TODO: logic for Congressional terms
        show = true;
        break;
      case $scope.sections.WEB:
        show = $scope.webPagesSelected;
        break;
      case $scope.sections.COMMON:
        show = !$scope.webPagesSelected;
        break;
      case $scope.sections.ONLY_AUTHORITY:
        show = !$scope.archivalDescriptionSelected && !$scope.onlineMaterialsSelected && $scope.authorityRecordSelected;
    }
    return show;
  };

  /**
   * Pass search term to the basic search page.
   */
  $scope.passSearchStringToBasicSearch = function () {
    searchSvc.setQuery($scope.searchTerm);
  };

  /**
   * Reset values previously selected
   */
  $scope.reset = function () {
    $scope.TypeArchivalMaterialsList.selectedValues = null;
    $scope.levelOfDescriptions.selectedValues = null;
    $scope.fileFormat.selectedValues = null;
    $scope.ArchivalMaterialsLocation.selectedValues = null;
    $scope.searchTerm = '';

    $rootScope.$broadcast('resetDaysList');

    $scope.congressRangeTo = $scope.maxCongressValue;
    $scope.yearsCongressBegin = ($scope.congressDates[$scope.minCongressValue - 1]).congressYears;
    $scope.yearsCongressEnd = ($scope.congressDates[$scope.maxCongressValue - 1]).congressYears;

    initResettableScopeVariables();

    initScopeSources($scope.selectedSourceChange);

    $('.k-datepicker input').val('');
    $('.k-widget.k-tooltip-validation').hide();

    $scope.advancedSearchForm.$setPristine(true);
  };

  /**
   * Set filters to execute search
   */
  $scope.setSearchUrl = function () {
    var endCongress;
    var beginCongress;
    var filterString;
    var selectedSources = [];
    var searchParams = {};
    var range;
    $scope.errorFlag = false;

    searchSvc.filterParams = null;

    //Reads search term and if it is not null then set the value of the proper textbox.
    if ($scope.searchTerm) {
      searchSvc.setQuery($scope.searchTerm);
      searchParams.q = $scope.searchTerm;
    }
    else {
      //if the user did not type any search term the default is *:* which means that will search over the whole collection.
      searchSvc.setQuery('*:*');
      searchParams.q = '*:*';
    }

    //Get the selected sources
    if (!($scope.archivalDescriptionSelected && $scope.onlineMaterialsSelected && $scope.authorityRecordSelected && $scope.webPagesSelected)) {
      /*The “Archival Materials Online” checkbox
       (covered by requirement 1.1.1.2 and parametric search on data-specific search fields)
       is used to indicate whether the user wants to search only
       those descriptions that have digitized content; it is checked by default.*/
      if ($scope.archivalDescriptionSelected) {
        selectedSources.push(AdvancedSearchService.sourceFiltersDictionary.ARCHIVAL_DESCRIPTIONS.value);
        selectedSources.push(AdvancedSearchService.sourceFiltersDictionary.ONLINE_ARCHIVAL_DESCRIPTIONS.value);
      } else if ($scope.onlineMaterialsSelected) {
        selectedSources.push(AdvancedSearchService.sourceFiltersDictionary.ONLINE_ARCHIVAL_DESCRIPTIONS.value);
      }
      if ($scope.authorityRecordSelected) {
        selectedSources.push(AdvancedSearchService.sourceFiltersDictionary.AUTHORITY_RECORDS.value);
      }
      if ($scope.webPagesSelected) {
        selectedSources.push(AdvancedSearchService.sourceFiltersDictionary.WEB.value);
       // selectedSources.push(AdvancedSearchService.sourceFiltersDictionary.PRESIDENTIAL.value);
      }
      searchParams['f.oldScope'] = searchSvc.arrayToUrlString(selectedSources, ' or ', true);
    }

    //Get Archival Descriptions filters
    if ($scope.showSection($scope.sections.DESCRIPTIONS)) {

      //Setting Type of Archival Descriptions
      filterString = searchSvc.arrayToUrlString($scope.TypeArchivalMaterialsList.selectedValues, ' or ', true);
      if (filterString) {
        searchParams['f.materialsType'] = filterString;
      }
      //Setting level
      filterString = searchSvc.arrayToUrlString($scope.levelOfDescriptions.selectedValues, ' or ', true);
      if (filterString) {
        searchParams['f.level'] = filterString;
      }

      //Setting fileFormat
      filterString = searchSvc.arrayToUrlString($scope.fileFormat.selectedValues, ' or ', true);
      if (filterString) {
        searchParams['f.fileFormat'] = filterString;
      }

      //Setting Location
      filterString = searchSvc.arrayToUrlString($scope.ArchivalMaterialsLocation.selectedValues, ' or ', true);
      if (filterString) {
        searchParams['f.locationIds'] = filterString;
      }
      filterString = null;

      //Setting Title
      if ($scope.titleAdvancedSearch) {
        searchParams['f.allTitles'] = $scope.titleAdvancedSearch;
      }

      //Setting Creator
      if ($scope.creator) {
        searchParams['f.creators'] = $scope.creator;
      }

      //Setting Geographic Reference
      if ($scope.geograficRef) {
        searchParams['f.geographicReferences'] = $scope.geograficRef;
      }

      //Setting Record Group Number / Collection Id
      if ($scope.recordGroupNumber) {
        searchParams['f.recordGroupNoCollectionId'] = $scope.recordGroupNumber;
      }

      //Setting Description Identifier
      if ($scope.DescriptionId) {
        searchParams['f.descriptionIdentifier'] = $scope.DescriptionId;
      }
    }

    if ($scope.showSection($scope.sections.COMMON)) {
      if ($scope.name) {
        //Setting Person/Organization Name
        if ($scope.archivalDescriptionSelected && !$scope.authorityRecordSelected) {
          searchParams['f.descriptionPOName'] = $scope.name;
        }
        else if ($scope.authorityRecordSelected && !$scope.archivalDescriptionSelected) {
          searchParams['f.authorityPOName'] = $scope.name;
        }
        else {
          searchParams['f.descriptionPOName'] = $scope.name;
          searchParams['f.authorityPOName'] = $scope.name;
        }
      }
      if ($scope.userTags) {
        //Search Engine has issues with '='
        if($scope.userTags.indexOf(':') !== -1){
          searchParams['f.machineTags'] = $scope.userTags.replace(/[=]/g, ' ');
        } else {
          searchParams['f.tagsKeywordsAdv'] = $scope.userTags.replace(/[=]/g, ' ');
        }
      }

      if ($scope.enableCongressionalRecords) {
        beginCongress = parseInt($scope.congressRangeFrom);
        endCongress = parseInt($scope.congressRangeTo);
        if (beginCongress !== $scope.minCongressValue || endCongress !== $scope.maxCongressValue) {
          if (beginCongress > endCongress) {
            $scope.errorFlag = true;
            if (OpaUtils.isMobileDevice()) {
              OpaUtils.showErrorGlobalNotification('Congress Range: Invalid range value');
            }
            else {
              OpaUtils.showErrorGlobalNotification('Congress Range: Begin Congress must be lower than or equal to End Congress');
            }
          }
          searchParams['f.congressRange'] = $scope.congressRangeFrom + '-' + $scope.congressRangeTo;
        }
      }

      if (!$scope.dateRange.errorFlag) {
        if ($scope.dateRange.currentTab === 'range') {
          if ($scope.dateRange.dayFromValue !== 'DD' && $scope.dateRange.monthFromValue === 'MM') {
            OpaUtils.showErrorGlobalNotification('Begin Month must be set');
            return;
          }
          if ($scope.dateRange.dayToValue !== 'DD' && $scope.dateRange.monthToValue === 'MM') {
            OpaUtils.showErrorGlobalNotification('End Month must be set');
            return;
          }
          range = dateService.getRangeDates($scope.dateRange.yearFromValue,
            $scope.dateRange.monthFromValue,
            $scope.dateRange.dayFromValue,
            $scope.dateRange.yearToValue,
            $scope.dateRange.monthToValue,
            $scope.dateRange.dayToValue);

          if (range[0] !== 0) {
            searchParams['f.beginDate'] = moment(range[0]).format('YYYY-MM-DD');
          }
          if (range[1] !== 0) {
            searchParams['f.endDate'] = moment(range[1]).format('YYYY-MM-DD');
          }
        }
        else if ($scope.dateRange.currentTab === 'exact' && !$scope.showSection($scope.sections.ONLY_AUTHORITY)) {
          range = dateService.getRangeDates($scope.dateRange.exactYearValue,
            $scope.dateRange.exactMonthValue,
            $scope.dateRange.exactDayValue);

          if (range[0] !== 0) {
            searchParams['f.exactDate'] = moment(range[0]).format('YYYY-MM-DD');
          }
        }
        else if ($scope.dateRange.currentTab === 'open') {
          if (($scope.dateRange.monthOpenValue !== 'MM' || $scope.dateRange.dayOpenValue !== 'DD') && !$scope.showSection($scope.sections.ONLY_AUTHORITY)) {
            if ($scope.dateRange.monthOpenValue !== 'MM' && $scope.dateRange.monthOpenValue) {
              searchParams['f.recurringDateMonth'] = '00'.substr(0, 2 - $scope.dateRange.monthOpenValue.length) + $scope.dateRange.monthOpenValue;
            }
            if ($scope.dateRange.dayOpenValue !== 'DD' && $scope.dateRange.dayOpenValue) {
              searchParams['f.recurringDateDay'] = '00'.substr(0, 2 - $scope.dateRange.dayOpenValue.length) + $scope.dateRange.dayOpenValue;
            }
          }
        }
      }
      else {
        $scope.errorFlag = true;
        $scope.dateRange.errors.forEach(function (value) {
          OpaUtils.showErrorGlobalNotification(value);
        });
      }
    }

    //TODO: Get Presidential/Vice Presidential Electronic Records Fields

    if (!$scope.errorFlag) {
      searchParams.SearchType = 'advanced';
      $location.search(searchParams);
      $location.path('/search');
    }
  };

  /**
   * This function checks the inputs values from the congressional field ranges
   * @param begin Flag that indicates if the value to check is the begin or the end.
   */
  $scope.changeCongressValue = function (begin) {

    if (begin) {
      if ($scope.congressRangeFrom.length >= 1) {
        if (!OpaUtils.isNumeric($scope.congressRangeFrom) || parseInt($scope.congressRangeFrom) <= 0 || parseInt($scope.congressRangeFrom) > $scope.maxCongressValue) {
          $scope.congressRangeFrom = $scope.minCongressValue;
        }
        $scope.yearsCongressBegin = ($scope.congressDates[parseInt($scope.congressRangeFrom) - 1]).congressYears;
      }
    }
    else if (!begin) {
      if ($scope.congressRangeTo.length >= 1) {
        if (!OpaUtils.isNumeric($scope.congressRangeTo) || parseInt($scope.congressRangeTo) <= 0 || parseInt($scope.congressRangeTo) > $scope.maxCongressValue) {
          $scope.congressRangeTo = $scope.maxCongressValue;
        }
        $scope.yearsCongressEnd = ($scope.congressDates[parseInt($scope.congressRangeTo) - 1]).congressYears;
      }
    }
  };

  //Initialize the controller
  init();
});
