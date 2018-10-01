opaApp.service("searchSvc", function (OpaUtils, $location, $rootScope, $routeParams, configServices, ListsService, Results, SearchFilters) {

  /**
   * Object containing the selected tab
   * so it can be modified outside the directive
   * @type {{selectedTab: number}}
   */
  this.tabs = {
    selectedTab: 1
  };

  this.query = "";
  this.getQuery = function () {
    return this.query;
  };
  this.setQuery = function (value) {
    this.query = value.replace(/”/, '"').replace(/“/, '"');
    this.searchParams.tabType = 'all';
  };
  this.showSearchBar = true;
  this.showStickyFilters = false;
  this.tabsInfo = {
    tabsCount: []
  };

  /**
   * local variable so it can be used
   * in asynchronous methods
   * @type {{tabsCount: Array}|*}
   */
  var tabsInfo = this.tabsInfo;
  this.results = [];
  this.facets = [];
  this.thesaurus = {};
  this.thesaurusCategories = ['related', 'broader', 'narrower'];
  this.additionalFilters = [];
  this.doSearch = false;
  this.searchParams = {
    'rows': (configServices.RESULTS_PER_PAGE && configServices.RESULTS_PER_PAGE.length > 0) ? configServices.RESULTS_PER_PAGE[0] : 20,
    'offset': 0,
    'tabType': 'all',
    'facet': 'true',
    'facet.fields': "oldScope,level,materialsType,fileFormat,locationIds,dateRangeFacet",
    'highlight': true,
    'sort': "relevance"
  };

  this.defaultSearchParams = jQuery.extend(true, {}, this.searchParams);

  /**
   * NARA-1571
   * params to override the search params
   * in order to get only the tabCounts
   * @type {{facet.fields: string, rows: number}}
   */
  var tabsCountParams = {
    'facet.fields': 'tabType',
    rows: 0,
    noSpinner: true
  };

  /**
   * NARA-1571
   * variable to know when to update the counts
   * (update counts only one time per search)
   * @type {boolean}
   */
  this.updateTabCounts = true;

  /**
   * get the tab counts
   * @param params
   * @param callback
   * @param destroy
   */
  this.getTabCounts = function (params, callback, destroy) {
    var apiParams = {};
    if (this.updateTabCounts) {
      var resultsService = new Results();
      apiParams = angular.extend({}, params, tabsCountParams);
      this.mergeLocations(apiParams);
      apiParams.tabType = 'all';
      apiParams.offset = 0;
      apiParams.facet = true;
      if (apiParams.q) {
        resultsService.$get(apiParams, function (data) {
          destroy();
          data.opaResponse.facets.field.forEach(function (element) {
            if (element['@name'] === 'tabType') {
              tabsInfo.tabsCount = element.v;
              callback();
            }
          });
        });
      }
    }
    this.updateTabCounts = false;
  };


  this.limitExceeded = false;
  this.lastOffset = 0;
  this.lastoffsetEnd = 0;
  this.lasttotalRecords = 0;
  this.lastqueryTime = 0;

  this.sortOptions = {
    relevance: {text: "Relevance", selected: true},
    titleSort: {text: "Title", selected: false},
    naIdSort: {text: "National Archives Identifier", selected: false},
    localId: {text: "Local Identifier", selected: false},
    hmsEntryNumberSort: {text: "HMS Entry Number", selected: false}
  };

  /**
   * Variable to know if the content controller
   * is working in mode preview
   * @type {boolean}
   */
  this.isPreview = false;

  this.getUrlParams = function () {
    try {
      this.query = this.query.replace(/”/, '"');
      this.query = this.query.replace(/“/, '"');
    } catch (e) {
    }
    return {
      'q': this.query,
      'rows': this.searchParams.rows,
      'offset': this.searchParams.offset,
      'tabType': this.searchParams.tabType,
      'facet': this.searchParams.facet,
      'facet.fields': this.searchParams['facet.fields'],
      'highlight': this.searchParams.highlight ? 'true' : 'false'
    };
  };

  this.checkIfIsDefaultSearchParam = function (param, value) {
    return (angular.equals(this.defaultSearchParams[param], value));
  };

  /* Checkbox on results page handling*/
  this.filterParams = null;
  this.selectedResults = [];
  this.topResults = null;
  var showCheckboxes = false;
  this.collpaseTimes = 0;
  this.panel = false;
  this.naIds = [];
  this.searchURL = null;

  this.searchWithinCallback = function () {
  };

  this.searchWithinObject = {
    searchWithin: {},
    show: true
  };

  this.setSearchWithinObject = function (searchWithin) {
    this.searchWithinObject.searchWithin = searchWithin;
    this.searchWithinCallback();
  };

  /**
   *
   * @param newValue boolean specifying whether the checkboxes should be displayed.
   * @returns {boolean}
   */
  this.activeCheckboxes = function (newValue) {
    if (newValue !== null && typeof newValue !== 'undefined') {
      showCheckboxes = newValue;
    }
    return showCheckboxes;
  };

  this.getThumbnail = function (result, isSearchWithin) {
    var thumbnail = "";
    result.usePreview = false;

    if (result.tabType && !isSearchWithin &&
      (result.tabType.indexOf("audio") !== -1 || result.tabType.indexOf("video") !== -1)) {
      result.usePreview = true;
    }

    if(/^http/.test(result.thumbnailFile)){
       thumbnail = result.thumbnailFile;
       // check to see that it's a new (TO4) image path
       if (thumbnail.includes('catalogmedia/')) {
         result.usePreview = true;
       }
    } else if (result.naId && result.thumbnailFile) {
      thumbnail = OpaUtils.getStoragePath(result.naId) + "/" + result.thumbnailFile.substr(result.thumbnailFile.lastIndexOf('/') + 1);
      result.usePreview = true;
    }
    else {
      thumbnail = OpaUtils.getIconFromType(result.iconType);
      if (!result.isOnline && result.iconType === "nara/itemAv") {
        return "images/notavailable.svg";
      }
      if (!thumbnail) {
        if (result.tabType && !isSearchWithin) {
          if (result.tabType.indexOf("online") === -1) {
            return "images/notavailable.svg";
          }
          else if (result.tabType.indexOf("web") !== -1) {
            return "images/web.svg";
          }
        }
        thumbnail = "images/file.svg";
      }
    }
    return thumbnail;
  };

  /**
   * setSearchURL gets called by the search bar when the user submits a new search.
   */
  this.setSearchUrl = function (params) {
    if (params) {
      params.q = undefined;
    }
    if (!$location.search().tabType) {
      this.searchParams.tabType = 'all';
    }
    $location.search($.extend({q: this.getQuery()}, params));
    $location.path("/search");
    $rootScope.isHome = false;
    this.cleanUpAccordion();
  };


  /**
   * Executes a search with the given query (if any), the given field and value are added to the url parameters
   * @param field
   * @param value
   * @param query [optional] if the query is not supplied, then a search for anything ( *:* )  is executed.
   */
  this.searchBy = function (field, value, query) {
    var params = {};
    if (!query) {
      query = "*:*";
    }
    params[field] = value.toString();
    this.searchWithAdditionalParams(params, query);
  };

  /**
   * Executes a search with the given query and appends the params to the URL params
   * @param params
   * @param query [optional] if the query is not supplied, then a search for anything ( *:* )  is executed.
   */
  this.searchWithAdditionalParams = function (params, query) {
    if (!query) {
      query = "*:*";
    }
    this.setQuery(query.toString());
    this.setSearchUrl(params);
  };

  this.cleanUpAccordion = function () {
    showCheckboxes = false;
    ListsService.selectedRecords = [];
    ListsService.selectedAllIndex = [];
    this.panel = false;
    this.selectedResults = [];
    this.allSelected = false;
    this.topResults = null;
    this.selectedTop = false;
    this.collpaseTimes = 0;
  };

  /**
   * Generates the filters string for the URl, if more than one filter is applied, then add parentheses around them
   * and separate them with ' or '
   * @param array filters to be converted to string
   * @returns {*} filters string for the URL
   */
  this.urlFiltersArrayToString = function (array) {
    return this.arrayToUrlString(array, " or ", true);
  };

  this.arrayToUrlString = function (array, separator, wrapWithParenthesis) {
    var result = null;
    if (array && array.length > 0) {
      if (array.length === 1) {
        result = array[0];
      }
      else {
        if (wrapWithParenthesis) {
          result = "(" + array.join(separator) + ")";
        } else {
          result = array.join(separator);
        }
      }
    }
    return result;
  };

  /**
   * NARAOPA-395: Merge locations 20 and 59
   * @param {object }urlParams
   * @return {boolean} Informing the caller if the function had to change the current URL.
   */
  this.mergeLocations = function (urlParams) {
    var filterArray = [];
    var i;
    var LOCATION_MERGED = '20';
    var LOCATION_TO_MERGE_FROM = '59';
    var locationMergedFound = false;
    var locationToMergeFromIndex = -1;
    var removeLocationFromURL = function (array, locationIndex) {
      array.splice(locationIndex, 1);
      $location.search('f.locationIds', this.urlFiltersArrayToString(array));
      $location.replace();
    };
    var causedRedirect = false;
    if (urlParams['f.locationIds']) {
      filterArray = this.getURLParamValues('locationIds', 'f.', '', ' or ');
      for (i = 0; i < filterArray.length; i++) {
        if (filterArray[i] === LOCATION_MERGED) {
          locationMergedFound = true;
        } else if (filterArray[i] === LOCATION_TO_MERGE_FROM) {
          locationToMergeFromIndex = i;
        }
      }
      if (locationMergedFound && locationToMergeFromIndex === -1) {
        filterArray.push(LOCATION_TO_MERGE_FROM);
        urlParams['f.locationIds'] = this.urlFiltersArrayToString(filterArray);
      } else if (!locationMergedFound && locationToMergeFromIndex !== -1) {
        filterArray.push(LOCATION_MERGED);
        urlParams['f.locationIds'] = this.urlFiltersArrayToString(filterArray);
        removeLocationFromURL.apply(this, [filterArray, locationToMergeFromIndex]);
        causedRedirect = true;
      } else if (locationMergedFound && locationToMergeFromIndex !== -1) {
        removeLocationFromURL.apply(this, [filterArray, locationToMergeFromIndex]);
        causedRedirect = true;
      }
    }
    return causedRedirect;
  };
  /**
   * Parses the URL to see if the paramName is present, and then returns an array with the values assigned to it.
   * For instance, if the url has a param like 'f.materialsType=(text or photographsandgraphics)',
   * then 'f.' is the paramPrefix, 'materialsType' is the paramName, there is no paramSuffix and the separator is ' or ';
   * for this example the return value is the array: ['text', 'photographsandgraphics'].
   * @param paramName
   * @param paramPrefix
   * @param paramSuffix
   * @param separator
   * @returns {Array} Array of values applied to paramName
   */
  this.getURLParamValues = function (paramName, paramPrefix, paramSuffix, separator) {
    var currentFilters = $location.search()[paramPrefix + paramName + paramSuffix];
    var currentFiltersArray = [];
    if (currentFilters) {
      currentFilters = SearchFilters.removeParenthesis(currentFilters);
      currentFiltersArray = currentFilters.split(separator);
    }
    return currentFiltersArray;
  };

  /**
   * doSearch gets called by the search bar when the user submits a new search.
   */
  this.search = function () {
    var currentLocation = $location.path();
    var searchLocation = /^\/search/;
    var params = {};
    $location.search('tabType', null);

    if (searchLocation.exec(currentLocation)) {
      //Only persist the highlight and the f.ancestorNaIds (search within) if present, otherwise do a new search.
      if ($routeParams['f.ancestorNaIds']) {
        params['f.ancestorNaIds'] = $routeParams['f.ancestorNaIds'];
      }
      if ($routeParams.highlight) {
        params.highlight = $routeParams.highlight;
      }
      this.setSearchUrl(params);
    } else {
      this.setSearchUrl();
    }
  };

  /**
   * Share url parameters between simple searh page/Results and Advance Search page.
   */
  this.passURLParams = function () {
    this.filterParams = $location.search();
  };
});
