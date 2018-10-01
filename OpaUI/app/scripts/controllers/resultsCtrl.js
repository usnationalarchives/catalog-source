'use strict';

opaApp.controller('resultsCtrl', function ($scope, $location, $route, $routeParams, $filter, $rootScope, $log, $timeout, $modal, $window, $cookieStore, Results, Lists, Auth, OpaUtils, searchSvc, ListsService, ErrorCodesSvc, SearchFilters, exportSvc, logControllerSvc, configServices, PaginationSvc, legacyUrlMappingService, visorSvc) {
  var termsAppliedArray = [];
  var selectedTab = $location.search().tabType; //Sets the selected tab from the url

  //Services
  $scope.searchSvc = searchSvc;
  $scope.Auth = Auth;
  $scope.exportSvc = exportSvc;
  $scope.configServices = configServices;
  $scope.OpaUtils = OpaUtils;

  //UI BINDING
  $scope.searchTermsText = "";
  $scope.resultPerPage = searchSvc.searchParams.rows;
  $scope.currentPage = 1;
  $scope.nav = {};
  $scope.nav.targetPage = 1;
  $scope.page1 = 0;
  $scope.page2 = 0;
  $scope.page3 = 0;
  $scope.showAllFacets = true;
  $scope.didYouMean = "";
  searchSvc.thesaurus = "";
  $scope.selectedTerm = 0;
  $scope.exportSvc = exportSvc;
  $scope.checkboxes = searchSvc.activeCheckboxes;
  $scope.onlyWebResults = false;

  //GLOBAL VARIABLES
  $scope.offset = parseInt(searchSvc.searchParams.offset);
  $scope.offsetEnd = 0;
  $scope.totalRecords = 0;
  $scope.searchFinished = false;
  $scope.sort = "relevance";
  $scope.sortDirection = "asc";
  $scope.queryTime = 0;

  //Web pages grouping
  $scope.webPages = false;


  //Bindings to the export Service variables
  $scope.getDownloadURL = exportSvc.getURL;
  $scope.thumbnails = exportSvc.thumbnails;
  $scope.descriptions = exportSvc.descriptions;
  $scope.contributions = exportSvc.contributions;
  $scope.exportFormat = exportSvc.exportFormat;
  $scope.format = exportSvc.format;


  /**
   * This function sets the proper ccs class to show/hide contend based on their priority value
   * @param priority
   *  1: Always visible
   *  2: Hidden in mobiles
   *  3: Hidden in mobiles and tablets
   *  4: Hidden in mobile, tablets and small desktops
   * @returns {string} The bootstrap class with the visibility of this priority
   */
  $scope.checkVisibility = function (priority) {
    var classString = '';

    switch (priority) {
      case 1:
        break;
      case 2:
        classString = 'hidden-inline-xs';
        break;
      case 3:
        classString = 'hidden-inline-xs hidden-inline-sm';
        break;
      case 4:
        classString = 'hidden-inline-xs hidden-inline-sm hidden-inline-md';
        break;
    }
    return classString;
  };


  /* Facet functions */
  /**
   * This function maps the name from the facet json into its correct presentation name.
   * @param value Original facet/filter name.
   * @param parentFacet Facet name that contains the value.
   * @returns {string} - the correct display name for the value.
   */
  $scope.prettyPrintFacet = function (value, parentFacet) {
    var name = "";
    var lowerCaseValue = value.toLowerCase();

    if (parentFacet && value !== parentFacet) {
      switch (parentFacet) {
        case "locationIds":
          name = SearchFilters.getNameFromValue(SearchFilters.Location(), lowerCaseValue);
          break;
        case "oldScope":
          name = SearchFilters.getNameFromValue(SearchFilters.SourceItems(), lowerCaseValue);
          break;
        case "materialsType":
          name = SearchFilters.getNameFromValue(SearchFilters.TypeArchivalMaterialsList(), lowerCaseValue);
          break;
        case "level":
          name = SearchFilters.getNameFromValue(SearchFilters.LevelOfDescriptions(), lowerCaseValue);
          break;
        case "fileFormat":
          name = SearchFilters.getNameFromValue(SearchFilters.FileFormat(), lowerCaseValue);
          break;
        default:
          name = "";
      }
      if (name !== "") {
        return name;
      }
    }

    //Fallback values
    switch (value) {
      case "oldScope":
        return SearchFilters.FacetNames.DATA_SOURCE;
      case "locationIds":
        return SearchFilters.FacetNames.LOCATION;
      case "level":
        return SearchFilters.FacetNames.LEVEL;
      case "fileFormat":
        return SearchFilters.FacetNames.FILE_FORMAT;
      case "dateRangeFacet":
        return SearchFilters.FacetNames.DATE;
      case "recordgroup":
        return SearchFilters.FacetNames.RECORD_GROUP;
      case "materialsType":
        return SearchFilters.FacetNames.MATERIAL_TYPE;
      case "archivesweb":
        return SearchFilters.FacetNames.ARCHIVES_WEB;
      case "presidentialweb":
        return SearchFilters.FacetNames.PRESIDENTIAL_WEB;
      case "itemav":
        return SearchFilters.FacetNames.AUDIO_VISUAL_ITEM;
      case "allTitles":
        return SearchFilters.FacetNames.TITLE_ITEM;
      case "geographicReferences":
        return SearchFilters.FacetNames.GEOGRAPHIC_REFERENCES_ITEM;
      case "recordGroupNumber":
        return SearchFilters.FacetNames.RECORD_GROUP_NUMBER_ITEM;
      case "collectionIdentifier":
        return SearchFilters.FacetNames.COLLECTION_IDENTIFIER_ITEM;
      case "creators":
        return SearchFilters.FacetNames.CREATOR_ITEM;
      case "descriptionIdentifier":
        return SearchFilters.FacetNames.DESCRIPTION_IDENTIFIER_ITEM;
      case "descriptionPOName":
        return SearchFilters.FacetNames.ARCHIVAL_PERSON_OR_ORGANIZATION_NAME_ITEM;
      case "authorityPOName":
        return SearchFilters.FacetNames.AUTHORITY_PERSON_OR_ORGANIZATION_NAME_ITEM;
      case "tagsKeywordsAdv":
        return SearchFilters.FacetNames.TAGS_KEYWORDS;
      case "machineTags":
        return SearchFilters.FacetNames.MACHINE_TAGS;
      case "descriptionStartYear":
        return SearchFilters.FacetNames.DESCRIPTION_START_YEAR_ITEM;
      case "descriptionEndYear":
        return SearchFilters.FacetNames.DESCRIPTION_END_YEAR_ITEM;
      case "authorityStartYear":
        return SearchFilters.FacetNames.AUTHORITY_START_YEAR_ITEM;
      case "authorityEndYear":
        return SearchFilters.FacetNames.AUTHORITY_END_YEAR_ITEM;
      case "ancestorNaIds":
        return SearchFilters.FacetNames.ANCESTOR_NAID;
      case "tagsExact":
        return SearchFilters.FacetNames.TAG_EXACT;
      case "allAuthorityIds":
        return SearchFilters.FacetNames.ALL_AUTHORITIES_IDS;
      case "contributorIds":
        return SearchFilters.FacetNames.CONTRIBUTOR_ID;
      case "creatorIds":
        return SearchFilters.FacetNames.CREATOR_IDS;
      case "subjectIds":
        return SearchFilters.FacetNames.SUBJECT_IDS;
      case "donorIds":
        return SearchFilters.FacetNames.DONOR_IDS;
      case "personalReferenceIds":
        return SearchFilters.FacetNames.PERSONAL_REFERENCE_IDS;
      case "beginCongress":
        return SearchFilters.FacetNames.BEGIN_CONGRESS;
      case "endCongress":
        return SearchFilters.FacetNames.END_CONGRESS;
      case "congressRange":
        return SearchFilters.FacetNames.CONGRESS_RANGE;
      case "beginDate":
        return SearchFilters.FacetNames.BEGIN_DATE;
      case "endDate":
        return SearchFilters.FacetNames.END_DATE;
      case "exactDate":
        return SearchFilters.FacetNames.EXACT_DATE;
      case "recurringDateMonth":
        return SearchFilters.FacetNames.RECURRING_MONTH;
      case "recurringDateDay":
        return SearchFilters.FacetNames.RECURRING_DAY;
      case "openDateMonth":
        return SearchFilters.FacetNames.OPEN_MONTH;
      case "openDateDay":
        return SearchFilters.FacetNames.OPEN_DAY;
      case "recordGroupNoCollectionId":
        return SearchFilters.FacetNames.RECORD_GROUP_COLLECTION_ID;

      default:
        return value;
    }
  };

  /**
   * Function that clears the facets from the view.
   */
  $scope.clearFacets = function () {
    searchSvc.showStickyFilters = false;
    searchSvc.facets = [];
    searchSvc.additionalFilters = [];
  };

  /**
   * Function that changes the route to the search page.
   */
  $scope.returnToSearch = function () {
    $location.path("/search");
  };

  /**
   * In small resolution this function switches the view to the mobile facets page.
   * Comunication between the search page and mobile facets is done thru URL parameters.
   */
  $scope.showFacetsMobile = function () {
    $location.path("/mobileFacets");
  };

  /**
   * Apply action on the mobile facets view, it sets the url parameters with the selected facet values
   * and then it changes the path search.
   */
  $scope.applyMobileFacets = function () {
    for (var i = 0; i < searchSvc.facets.length; i++) {
      for (var j = 0; j < searchSvc.facets[i].v.length; j++) {
        if (searchSvc.facets[i].v[j].isApplied) {
          $scope.applyFacetFilter(searchSvc.facets[i].searchEngineName, searchSvc.facets[i].v[j].searchEngineName, searchSvc.facets[i]);
        } else {
          $scope.removeFilter(searchSvc.facets[i].searchEngineName, searchSvc.facets[i].v[j]);
        }
      }
    }
    $scope.returnToSearch();
  };

  /**
   * Toggles the showAllFacets variable in the view.
   */
  $scope.toggleShowAll = function () {
    $scope.showAllFacets = !$scope.showAllFacets;
  };

  /**
   * Clears all the selected facets from the view and URL.
   */
  $scope.clearAllFacets = function () {
    angular.forEach($routeParams, function (value, key) {
      if (key.indexOf("f.") > -1) {
        delete($routeParams[key]);
      }
    });
    $location.search($routeParams);
  };

  /**
   * Parse additional filters that come from advanced search page and build a structure that can be displayed in the stickyFacetFilters view.
   */
  var getStickyFilters = function () {
    var tempFilters = [];
    var urlFiltersArray = [];
    var filterFound = false;

    angular.forEach($location.search(), function (value, key) {
      if (key.indexOf("f.") > -1) {
        filterFound = true;
        key = key.replace("f.", "");
        urlFiltersArray = getURLFilters(key);
        angular.forEach(urlFiltersArray, function (entry) {
          tempFilters.push({
            searchEngineName: entry,
            displayName: $scope.prettyPrintFacet(entry, key)
          });
        });
        searchSvc.additionalFilters.push({
          searchEngineName: key,
          displayName: $scope.prettyPrintFacet(key),
          v: tempFilters.slice(0),
          expandFilters: false
        });
        tempFilters = [];
      }
    });
    if (filterFound) {
      searchSvc.showStickyFilters = true;
      $scope.showAllFacets = true;
    }
  };

  /**
   * This function reads the facets retrieved from results API response and filters out
   * any empty facet, it also processes the facets for an easier display in the facets directives.
   * @param facets facets array that comes in the OpaResponse of the search API call.
   */
  $scope.getFacets = function (facets) {

    var tempFilters = [];
    var includeFacet = false;
    var urlFiltersArray = [];
    var isDateFacet = false;
    var searchEngineName = "";
    searchSvc.facets = [];

    //Clean attributes names and remove empty facets and filters. Also get the location names from the locationIds
    facets.field.forEach(function (element, index) {
      urlFiltersArray = getURLFilters(element['@name']);
      if (element['@name'] === 'tabType') {
        searchSvc.tabsCount = element.v;
      }
      else {
        if (element['@name'] === 'dateRangeFacet') {
          isDateFacet = true;
        }
        element.v.forEach(function (innerElement, subIndex) {
          if (innerElement['@count'] !== '0') {
            includeFacet = true;
            searchEngineName = isDateFacet ? '"' + innerElement['@name'] + '"' : innerElement['@name'];
            tempFilters.push({
              searchEngineName: searchEngineName,
              displayName: $scope.prettyPrintFacet(innerElement['@name'], element['@name']),
              count: parseInt(innerElement['@count']),
              isApplied: $.inArray(searchEngineName.toLowerCase(), urlFiltersArray) !== -1
            });
            if (tempFilters[tempFilters.length - 1].isApplied) {
              searchSvc.showStickyFilters = true;
            }
          }
        });
        //Exclude any facet that has no items in its filters.
        if (includeFacet) {
          searchSvc.facets.push({
            searchEngineName: element['@name'],
            displayName: $scope.prettyPrintFacet(element['@name'], element['@name']),
            v: tempFilters.slice(0),
            expandFilters: false
          });
        }
      }
      includeFacet = false;
      tempFilters = [];
    });
  };
  /**
   * Controls if a facet group expands/collpases its filters in the left facets pane
   * @param facetIndex Facet index from the facets array
   */
  $scope.toggleExpandedFilters = function (facetIndex) {
    searchSvc.facets[facetIndex].expandFilters = !searchSvc.facets[facetIndex].expandFilters;
  };

  /**
   *
   * @param isExpanded
   * @returns {string}
   */
  $scope.expandFiltersLabel = function (isExpanded) {
    if (isExpanded) {
      return "show less...";
    }
    return "show more...";
  };

  /**
   * Removes the applied filter from the URL and updates the isApplied status in the facets array. By
   * modifiying the URL, the search is executed again.
   * @param facetName Name of the facet that contains the filter
   * @param filter reference to the filter object inside the facets array
   */
  $scope.removeFilter = function (facetName, filter) {
    var newFilters = null;
    var urlFiltersArray = getURLFilters(facetName);
    var indexOfFilter = $.inArray(filter.searchEngineName, urlFiltersArray);

    //filter.isApplied = false;
    if (indexOfFilter !== -1) {
      urlFiltersArray.splice(indexOfFilter, 1);
      if (urlFiltersArray.length > 0) {
        newFilters = searchSvc.urlFiltersArrayToString(urlFiltersArray);
      }
      $location.search("f." + facetName, newFilters);
    }
  };

  /**
   * Applies a filter, it modifies the URL, which causes a new search to be executed.
   * If the filter is already applied it will remove it.
   * @param facetName
   * @param filterName
   * @param facet object
   */
  $scope.applyFacetFilter = function (facetName, filterName, value) {
    if (value.isApplied) {
      $scope.removeFilter(facetName, value);
    }
    else {
      var currentFiltersArray = getURLFilters(facetName);
      var indexOfFilter = $.inArray(filterName, currentFiltersArray);

      if (indexOfFilter === -1) {
        currentFiltersArray.push(filterName);
        $location.search("f." + facetName, searchSvc.urlFiltersArrayToString(currentFiltersArray));
        $location.replace();
      }
    }
  };

  /**
   * This function reads all the applied filters from the URL for a specific facet, and returns them inside an array.
   * @param filterName name of the facet to look for in the URL
   * @returns {Array} array of applied filters for the facet.
   */
  var getURLFilters = function (filterName) {
    var array = searchSvc.getURLParamValues(filterName, "f.", "", " or ");
    array.forEach(function (element, index) {
      array[index] = element.toLowerCase();
    });
    return array;
  };
  /*End of Facet functions*/

  $scope.PassSearchStringToBasicSearch = function () {
    searchSvc.setQuery($scope.searchterm);
  };

  /**
   * Removes filters that are comming from advanced search page and don't match any of the facets filters.
   * @param facet
   * @param filter
   */
  $scope.removeAdditionalFilter = function (facet, filter) {
    filter.isApplied = false;
    $location.search(facet, null);
  };

  /**
   * This function binds the sorting menu.
   * @param value
   */
  $scope.setSort = function (value, navigate) {
    $scope.sort = value;

    searchSvc.sortOptions.relevance.selected = false;
    searchSvc.sortOptions.titleSort.selected = false;
    searchSvc.sortOptions.naIdSort.selected = false;
    searchSvc.sortOptions.localId.selected = false;
    searchSvc.sortOptions.hmsEntryNumberSort.selected = false;

    switch (value) {
      case "relevance":
        searchSvc.sortOptions.relevance.selected = true;
        if (navigate) {
          $location.search('sort', null);
        }
        $scope.firstPageNumber();
        return;
      case "titleSort":
        searchSvc.sortOptions.titleSort.selected = true;
        break;
      case "naIdSort":
        searchSvc.sortOptions.naIdSort.selected = true;
        break;
      case "localId":
        searchSvc.sortOptions.localId.selected = true;
        break;
      case "hmsEntryNumberSort":
        searchSvc.sortOptions.hmsEntryNumberSort.selected = true;
        break;
      default:
    }
    if (navigate) {
      $location.search('sort', $scope.sort + " " + $scope.sortDirection);
    }
    $scope.firstPageNumber();
  };

  $scope.last = function () {
    return PaginationSvc.last($scope);
  };

  $scope.firstPageNumber = function () {
    PaginationSvc.firstPageNumber($scope);
    if (searchSvc.checkIfIsDefaultSearchParam('offset', $scope.offset)) {
      $location.search('offset', undefined);
    }
    searchSvc.allSelected = false;
  };

  $scope.firstPageUrl = function () {
    return $scope.currentPage <= 1 ? "" : $location.url().replace(/&offset=(\d*)/,'');
  }

  $scope.lastPageNumber = function () {
    PaginationSvc.lastPageNumber($scope);
    $location.search('offset', $scope.offset);
    searchSvc.allSelected = false;
  };

  $scope.lastPageUrl = function () {
    var currentUrl = $location.url();
    var newOffset = PaginationSvc.calculateLastPageOffset($scope);
    if (currentUrl.includes('&offset')) {
      return PaginationSvc.last($scope) ? "#" : currentUrl.replace(/&offset=(\d*)/,'&offset='+newOffset);
    }
    return PaginationSvc.last($scope) ? "#" : $location.url()+"&offset="+newOffset;
  }

  $scope.increasePageNumber = function () {
    PaginationSvc.increasePageNumber($scope);
    $location.search('offset', $scope.offset);
    searchSvc.allSelected = false;
  };

  $scope.nextPageUrl = function () {
    var currentUrl = $location.url();
    var newOffset = PaginationSvc.calculateNextPageOffset($scope);
    if (currentUrl.includes('&offset')) {
      return PaginationSvc.last($scope) ? "#" : currentUrl.replace(/&offset=(\d*)/,'&offset='+newOffset);
    }
    return PaginationSvc.last($scope) ? "#" : $location.url()+"&offset="+newOffset;
  }

  $scope.decreasePageNumber = function () {
    PaginationSvc.decreasePageNumber($scope);
    if ($scope.currentPage >= 1) {
      $location.search('offset', $scope.offset);
    }
    searchSvc.allSelected = false;
  };

  $scope.previousPageUrl = function () {
    var currentUrl = $location.url();
    var newOffset = PaginationSvc.calculatePreviousPageOffset($scope);
    if (currentUrl.includes('&offset')) {
      return $scope.currentPage <= 1 ? "#" : currentUrl.replace(/&offset=(\d*)/,'&offset='+newOffset);
    }
    return $scope.currentPage <= 1 ? "#" : $location.url()+"&offset="+newOffset;
  }

  $scope.pageNumber = function (number) {
    if (!searchSvc.limitExceeded || ((number - 1) * $scope.resultPerPage) <= Auth.getRowsQueryLimit()) {
      /*  $scope.currentPage = number;
       $scope.offset = ($scope.currentPage - 1) * $scope.resultPerPage;
       */
      PaginationSvc.pageNumber($scope, number);
      $location.search('offset', $scope.offset);
      searchSvc.allSelected = false;
    }
  };

  $scope.pageNumberUrl = function (number) {
    var currentUrl = $location.url();
    var newOffset = PaginationSvc.calculateSpecificPageOffset($scope, number);
    if (currentUrl.includes('&offset')) {
      return currentUrl.replace(/&offset=(\d*)/,'&offset='+newOffset);
    }
    return $location.url()+"&offset="+newOffset;
  }

  $scope.updateOffset = function () {
    PaginationSvc.updateOffset($scope);
  };

  $scope.setResultsPerPage = function (value) {
    $scope.resultPerPage = value;
  };

  /**
   * This function reads the search parameters from the url, updated the query parameter and returns a copy of the parameters.
   * @params  Output array to copy the parameters to.
   * */
  $scope.getCurrentUrlParams = function (params) {
    angular.copy($location.search(), params);
    searchSvc.setQuery(params.q ? params.q : "");
    return params;
  };

  /**
   * Clear search term textbox when new search link is clicked.
   */
  $scope.clearSearchField = function () {
    searchSvc.setQuery("");
    $scope.clearCheckboxes();
  };

  /**
   * For the query expansion dialog, it it updates the isSelected member of all the sugestions
   * from a specific category for a selected term.
   *
   * @param termSet object that contains all the suggestions of a specific category for a term, for instance:
   *
   * If the selected term is ship, and the catergory narrower, then:
   *
   * termSet = {isSelected = true, //this maps to the 'Select all Terms' in the UI
     *            val = [{value = 'Cargo Ships', isSlected = false},{value = 'Warships', isSelected = false},...]
     *            }
   * As a result of this function with this example, all the isSelected values inside val, will be set to true.
   *
   */
  $scope.selectAllExpandedTerms = function (termSet) {
    var k = 0;
    if (termSet && termSet.val && termSet.val.length > 0) {
      for (; k < termSet.val.length; k++) {
        termSet.val[k].isSelected = termSet.isSelected;
      }
    }
  };

  $scope.getSelectedTerms = function (term, category) {
    var allSelected = true, tempArray = [], tempResultArray = [];

    if (term[category] && term[category].val) {
      if (term[category].isSelected) {
        termsAppliedArray.push(category);
      }
      else {
        angular.forEach(term[category].val, function (expandedTerm) {
          if (expandedTerm.isSelected) {
            tempArray.push(expandedTerm.value);
          } else {
            allSelected = false;
          }
        });
        if (allSelected) {
          termsAppliedArray.push(category);
        } else {
          tempResultArray = termsAppliedArray.concat(tempArray);
          termsAppliedArray = tempResultArray;
        }
      }
    }
  };

  var clearUrlParams = function (prefix) {
    for (var name in $routeParams) {
      if (name.indexOf(prefix) === 0) {
        $log.info("clearUrlParams true");
        delete $routeParams[name];
      }
    }
  };

  $scope.searchExpandedTerms = function () {
    var urlParamPrefix = "thesaurus.terms.";
    var expandedQueriesArray = {};
    termsAppliedArray = [];

    clearUrlParams(urlParamPrefix);

    if (searchSvc.thesaurus && searchSvc.thesaurus.length) {
      angular.forEach(searchSvc.thesaurus, function (term) {
        //termsApplied.push({name: term['@name'], values: []});
        angular.forEach(searchSvc.thesaurusCategories, function (category) {
          $scope.getSelectedTerms(term, category, termsAppliedArray);
        });
        if (termsAppliedArray.length > 0) {
          expandedQueriesArray[urlParamPrefix + term['@name']] = termsAppliedArray.join("|");
        }
        termsAppliedArray = [];
      });
    }
    $('#expandedQuery').removeClass('fade');
    $('#expandedQuery').modal('hide');
    angular.extend($routeParams, expandedQueriesArray);
    $location.search($routeParams);
  };

  var setThesaurusTermForView = function (term, category, urlTerms) {
    var isSelected = false, categorySelected = false;

    //Check if the category itself is selected
    if (urlTerms.indexOf(category) !== -1) {
      categorySelected = true;
    }

    if (term[category] && term[category].val) {
      term[category].isSelected = categorySelected;
      for (var
             k = 0; k < term[category].val.length; k++) {
        if (categorySelected || urlTerms.indexOf(term[category].val[k]) !== -1) {
          isSelected = true;
        }
        term[category].val[k] = {value: term[category].val[k], isSelected: isSelected};
        isSelected = false;
      }
    }
  };

  /**
   * Process the data from the search response
   * $scope.noResults is defined inside this function to avoid the noresults page from showing while waiting for
   * search results.
   * @param data
   */
  var processResults = function (data) {

    $scope.onlyWebResults = false;

    if (!data.opaResponse) {
      $scope.offset = -1;
      $scope.noResults = true;
    }
    else {
      if (data.opaResponse.searchWithin) {
        if ($.isArray(data.opaResponse.searchWithin)) { //TODO: remove this when the API is working correctly
          searchSvc.setSearchWithinObject(data.opaResponse.searchWithin[0]);
        } else {
          searchSvc.setSearchWithinObject(data.opaResponse.searchWithin);
        }
      } else {
        searchSvc.searchWithinObject.searchWithin = {};
        searchSvc.searchWithinObject.show = true;
      }

      if (data.opaResponse.spellingResults && data.opaResponse.spellingResults['@total'] > 0) {
        //TODO: handle multiple search terms in the suggestion.
        $scope.didYouMean = data.opaResponse.spellingResults.spellingResult[0];
        for (var c = 1; c < data.opaResponse.spellingResults.spellingResult.length; c++) {
          $scope.didYouMean += ', ' + data.opaResponse.spellingResults.spellingResult[c];
        }
      }
      if (data.opaResponse.thesaurus && data.opaResponse.thesaurus.term &&
        data.opaResponse.thesaurus.term.length > 0) {
        searchSvc.thesaurus = data.opaResponse.thesaurus.term;
        var urlTerms = [];

        angular.forEach(searchSvc.thesaurus, function (term) {
          urlTerms = searchSvc.getURLParamValues(term['@name'], 'thesaurus.terms.', '', '|');
          setThesaurusTermForView(term, 'related', urlTerms);
          setThesaurusTermForView(term, 'broader', urlTerms);
          setThesaurusTermForView(term, 'narrower', urlTerms);
        });
      }
      if (data.opaResponse.totalTime) {
        $scope.queryTime = data.opaResponse.totalTime;
      }

      if (!data.opaResponse.results || !data.opaResponse.results.result) {
        if (data.opaResponse.webPages) {
          $scope.noResults = false;
          $scope.onlyWebResults = true;
          $scope.offset = 0;
          $scope.totalRecords = data.opaResponse.webPages['@total'];
          $scope.offsetEnd = Math.min(data.opaResponse.webPages['@rows'], $scope.totalRecords);
        } else {
          $scope.noResults = true;
          $scope.offset = -1;
          $scope.onlyWebResults = false;
        }
      }
      else {
        $scope.noResults = false;
        searchSvc.results = data.opaResponse.results.result;
        $scope.totalRecords = data.opaResponse.results['@total'];
        $scope.offsetEnd = Math.min($scope.offset + parseInt($scope.resultPerPage), $scope.totalRecords);
        Auth.setRowsQueryLimit(data.opaResponse.results['@maxRowsForUser']);
        var max = Math.ceil($scope.totalRecords / parseInt($scope.resultPerPage));
        if ($scope.page1 > max) {
          $scope.page1 = 0;
        }
        if ($scope.page2 > max) {
          $scope.page2 = 0;
        }
        if ($scope.page3 > max) {
          $scope.page3 = 0;
        }
      }
      //Facets
      if (data.opaResponse.facets) {
        $scope.getFacets(data.opaResponse.facets);
        $scope.createTooltips();
      }

      //Other sticky filters
      searchSvc.additionalFilters = [];
      getStickyFilters();

      //Web Grouping
      if (data.opaResponse.webPages) {
        $scope.webPages = data.opaResponse.webPages.result;
      }
    }
    $scope.searchFinished = true;
    if (searchSvc.selectedTop) {
      $scope.selectTop(true);
    }

    $scope.updateOffset();
    $scope.checkAllSelected();
  };

  /**
   * Error handler for Results API calls
   * @param error Error object
   */
  var searchError = function (error) {
    if (!OpaUtils.checkForAPIError(error)) {
      //check for other errors block
      try {
        if (error && error.data.opaResponse &&
          angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.INVALID_OFFSET)) {
          searchSvc.limitExceeded = true;
          $scope.searchFinished = true;
        }
        else {
          $scope.noResults = true;
          if (error.data.opaResponse) {
            if (!angular.equals(error.data.opaResponse.error['@code'], ErrorCodesSvc.MISSING_PARAMETER)) {
              OpaUtils.showMessageModal("Something went wrong while searching",
                error.data.opaResponse.error.description + ", please contact <a href='mailto:catalog@nara.gov'>NARA support</a>");
            }
          }
          $scope.searchFinished = true;
          $scope.offset = -1;
        }
      } catch (error) {
        OpaUtils.showMessageModal("Search timeout", "Your search timed out");
        $scope.noResults = true;
        $scope.searchFinished = true;
        $scope.offset = -1;
      }
    }
  };

  /**
   * This function calls the search service that calls the API.
   * When the results are back, they are checked in order to hide invalid results.
   * Also it sets all the UI variables and fill the results array with the results from the API
   * */
  $scope.search = function () {
    var resultsService = new Results();
    var searchParams = {}, urlParams = {};
    searchSvc.limitExceeded = false;
    searchSvc.results = [];
    searchSvc.thesaurus = "";
    $scope.clearFacets();
    searchSvc.updateTabCounts = true;

    if (!searchSvc.getQuery()) {
      $scope.noResults = true;
      $scope.searchFinished = true;
    }
    else {
      //Fill in the missing default params if any, but keep the params from the url; keep any incoming
      //parameter in case the user pasted a url.
      searchParams = searchSvc.getUrlParams();
      urlParams = $scope.getCurrentUrlParams(urlParams);
      //NARAOPA-395: Merge locations 20 and 59
      if (searchSvc.mergeLocations(urlParams)) {
        //mergeLocations may update the URL, if it did, then cancel the current search and let the URL to be reprocessed.
        return;
      }
      $.extend(searchParams, urlParams);
      clearCDVariables();
      saveSearchParams(urlParams);
      $scope.queryTime = 0;
      if (searchParams['f.ancestorNaIds']) {
        resultsService.$searchWithin(searchParams, processResults, searchError);
      } else if (searchParams.filter) {
        resultsService.$searchTag(searchParams, processResults, searchError);
      } else {
        resultsService.$get(searchParams, processResults, searchError);
      }
    }
  };

  /**
   * Check when the results per page is changed and request a new set of results
   */
  $scope.resultPerPageChange = function () {
    if (searchSvc.checkIfIsDefaultSearchParam('rows', $scope.resultPerPage)) {
      $routeParams.rows = undefined;
    }
    else {
      $routeParams.rows = $scope.resultPerPage;
    }

    var offset;
    if ($scope.resultPerPage >= $scope.totalRecords) {
      offset = 0;
    }
    else {
      offset = ($scope.currentPage - 1) * $scope.resultPerPage;
      if (offset > $scope.totalRecords) {
        offset = 0;
      }
    }
    if (searchSvc.checkIfIsDefaultSearchParam('offset', $scope.resultPerPage)) {
      $routeParams.offset = undefined;
    }
    else {
      $routeParams.offset = offset;
    }
    $location.search($routeParams);
  };

  /**Check if a result record is valid
   * Checks:
   *  - Contains title
   *  - briefResults is an array
   * Return TRUE is valid. FALSE is invalid
   */
  $scope.isValidResult = function (result) {
    if (!result.briefResults) {
      return false;
    }
    else if (!result.briefResults.titleLine) {
      return false;
    }
    else if (!result.briefResults.metadataArea) {
      return false;
    }
    return true;
  };

  /**
   * Sets the internal variables using the url parameters
   */
  $scope.setUrlParams = function () {

    if ($routeParams.q) {
      searchSvc.setQuery($routeParams.q);
    }

    if ($routeParams.rows) {
      $scope.resultPerPage = parseInt($routeParams.rows);
    }
    else {
      // Setting the default value
      $scope.resultPerPage = searchSvc.defaultSearchParams.rows;
    }

    if ($routeParams.offset) {
      $scope.offset = parseInt($routeParams.offset);
    }
    else {
      // Setting the default value
      $scope.offset = searchSvc.defaultSearchParams.offset;
    }

    if ($routeParams.tabType) {
      $scope.tabType = searchSvc.searchParams.tabType = $routeParams.tabType;
    }

    if ($routeParams.highlight) {
      searchSvc.searchParams.highlight = $routeParams.highlight;
    }

    if ($routeParams.sort) {
      var sortValue = $routeParams.sort.substr(0, $routeParams.sort.indexOf(" "));
      if (sortValue) {
        $scope.setSort(sortValue, false);
      }
    } else {
      // Setting the default value
      $scope.setSort(searchSvc.defaultSearchParams.sort, false);
    }

    if ($routeParams.facet === 'true') {
      searchSvc.searchParams.facets = $routeParams.facet;
      if ($routeParams.facet.fields) {
        searchSvc.searchParams['facet.fields'] = $routeParams.facet.fields;
      }
    }
  };

  $scope.getLists = function () {
    if (!Auth.isLoggedIn()) {
      return;
    }
    ListsService.GetAllLists().then(function (promise) {
        $scope.userList = promise.opaResponse.userLists.userList;
      },
      function (error) {
        $scope.userList = {};
        if (!OpaUtils.checkForAPIError(error)) {
        }
      });
  };

  /**
   * Executes the search using the Did You Mean Term
   */
  $scope.searchDidYouMean = function (term) {
    $location.search('q', term);
  };

  $scope.showCheckboxes = function () {
    searchSvc.collpaseTimes += 1;
    searchSvc.activeCheckboxes(true);

    if (!$scope.$$phase) {
      $rootScope.$apply();
    }
  };

  $scope.setPanel = function (panel) {
    searchSvc.panel = panel;
  };

  $scope.clearCheckboxes = function () {
    searchSvc.collpaseTimes -= 1;
    if (searchSvc.collpaseTimes <= 0) {
      searchSvc.collpaseTimes = 0;
      searchSvc.activeCheckboxes(false);
      searchSvc.panel = false;
      searchSvc.selectedList = 0;
      searchSvc.selectedResults = [];
      searchSvc.allSelected = false;
      searchSvc.selectedTop = false;
      searchSvc.topResults = null;
      exportSvc.cleanUp();
      if (!$scope.$$phase) {
        $rootScope.$apply();
      }
    }
  };

  $scope.checkAllSelected = function () {
    for (var i = 0; i < searchSvc.results.length; i++) {
      var result = searchSvc.results[i];
      var idx = searchSvc.selectedResults.indexOf(result.opaId);
      if (idx === -1) {
        searchSvc.allSelected = false;
        return;
      }
    }

    searchSvc.allSelected = true;
  };

  $scope.selectOnPage = function () {
    searchSvc.allSelected = !searchSvc.allSelected;
    searchSvc.selectedTop = false;
    searchSvc.topResults = null;
    angular.forEach(searchSvc.results, function (result) {
      if (searchSvc.allSelected) {
        $scope.toggleSelection(result.opaId, true);
      }
      else {
        var idx = searchSvc.selectedResults.indexOf(result.opaId);
        if (idx > -1) {
          searchSvc.selectedResults.splice(idx, 1);
        }
      }
    });
  };

  $scope.toggleSelection = function (opaId, all) {
    var idx = searchSvc.selectedResults.indexOf(opaId);
    if (idx > -1) {
      if (!all) {
        searchSvc.allSelected = false;
        searchSvc.selectedTop = false;
        searchSvc.topResults = null;
        searchSvc.selectedResults.splice(idx, 1);
      }
    }
    else {
      searchSvc.selectedResults.push(opaId);
    }
    if (!all) {
      $scope.checkAllSelected();
    }
  };

  $scope.selectTop = function (skip) {
    searchSvc.allSelected = true;
    if (!skip) {
      searchSvc.selectedTop = !searchSvc.selectedTop;
    }
    if (searchSvc.selectedTop) {
      searchSvc.topResults = Auth.topResults();
    }
    else {
      searchSvc.topResults = null;
    }
    if (searchSvc.selectedTop) {
      angular.forEach(searchSvc.results, function (result) {
        $scope.toggleSelection(result.opaId, true);
      });
    }
    else {
      searchSvc.selectedResults = [];
      this.checkAllSelected();
    }

    //else {
    //  searchSvc.allSelected = false;
    //  var idx = searchSvc.selectedResults.indexOf(result.opaId);
    //  searchSvc.selectedTop = false;
    //  if (idx > -1) {
    //    searchSvc.selectedResults.splice(idx, 1);
    //  }
    //}
    // });
  };

  $scope.createNewList = function (newListName) {
    var list = new Lists();
    if (!newListName) {
      $scope.error = "Please enter a new list name";
      return;
    }
    else if (newListName.length > 50) {
      $scope.error = "Text size of field '" + newListName + "' (" + newListName.length + "]) is greater than field size: 50";
      return;
    }
    newListName = $filter('removeWordCharacters')(newListName);
    list.$create({'listname': newListName},
      function (data) {
        if (data.opaResponse) {
          searchSvc.selectedList = newListName;
          $scope.addToList();
          $scope.getLists();
          $('#createList').modal('hide');
        }
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          $scope.error = error.data.opaResponse.error.description;
        }
      }
    );
  };

  $scope.addToList = function () {
    var searchparams = {};
    var naIds = [];
    var payload = {};
    if (searchSvc.topResults) {
      searchparams.tabType = searchSvc.searchParams.tabType;
      searchparams.q = searchSvc.query;
      searchparams.offset = searchSvc.searchParams.offset;
      searchparams.rows = searchSvc.topResults;
      var additionalFilters = {};
      searchSvc.additionalFilters.forEach(function (filter) {
        filter.v.forEach(function (value) {
          if (!additionalFilters['f.' + filter.searchEngineName]) {
            additionalFilters['f.' + filter.searchEngineName] = [];
          }
          additionalFilters['f.' + filter.searchEngineName].push(value.searchEngineName);
        });
      });
      for (var key in additionalFilters) {
        if (additionalFilters[key].length > 1) {
          additionalFilters[key] = additionalFilters[key].join(" or ");
          additionalFilters[key] = '(' + additionalFilters[key] + ')';
        }
        searchparams[key] = additionalFilters[key];
      }
      searchparams.action = 'addToListFromSearch';
    } else {
      searchSvc.selectedResults.forEach(function (id) {
        naIds.push(id);
      });
      naIds = naIds.join(",");
      payload['what'] = naIds;
    }
    if (searchSvc.selectedList && naIds) {
      //var list = new Lists({list: searchSvc.selectedList});
      searchparams['list'] = searchSvc.selectedList;
      Lists.addToList(searchparams, payload,
        function (data) {
          if (data.opaResponse) {
            var requestIds = parseInt(searchSvc.topResults || searchSvc.selectedResults.length);
            var itemsAdded = 1;
            if (data.opaResponse.ListItems) {
              itemsAdded = data.opaResponse.ListItems;
            }
            var message = "Added " + itemsAdded + " record(s) to " + data.opaResponse.header.request.listName + " list. ";
            if (((requestIds - itemsAdded) !== 0) && (requestIds <= $scope.totalRecords)) {
              message = message.concat((requestIds - itemsAdded) + " record(s) are already in " + data.opaResponse.header.request.listName + " list");
            }
            OpaUtils.showInfoModal("Selected " + requestIds + " records", message);
            searchSvc.collpaseTimes = 1;
            $scope.clearCheckboxes();
          }
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            OpaUtils.showMessageModal("Can not add items to list", error.data.opaResponse.error.description);
          }
        }
      );
    } else {
      if (!searchSvc.selectedList) {
        OpaUtils.showMessageModal("Can not add items to list", "You must select at least one item and one list");
      }
      if (searchSvc.selectedList && !naIds) {
        searchSvc.collpaseTimes = 1;
        $scope.clearCheckboxes();
      }
    }
  };

  var showPreviewModal = function (title, naId) {
    var modal = $modal.open({
      templateUrl: 'views/directives/dialogs/preview.html',
      controller: ['$scope', '$modalInstance', '$location', function ($scope, $modalInstance, $location) {

        $scope.title = title;

        $scope.link = '/id/' + naId;

        $scope.close = function () {
          $modalInstance.close();
        };

        $scope.goToDescription = function () {
          $location.url($scope.link);
          $scope.close();
        };

      }],
      size: 'xl',
      resolve: {
        title: function () {
          return title;
        },
        naId: function () {
          return naId;
        }
      }
    });

    modal.result.then(
      function (close) {
        $scope.killWMVPlayer();
      },
      function (dismiss) {
        $scope.killWMVPlayer();
      }
    );

    //Removing classes fade and in from modal. Video player was not visible on Firefox because of that.
    $timeout(function () {
      $(document.getElementById("previewModal").parentElement.parentElement.parentElement).removeClass("fade in");
    }, 2000);
  };

  $scope.raisePreviewModal = function (result) {
    if (result.usePreview) {
      searchSvc.selectedNaId = result.naId;
      searchSvc.isPreview = true;
      var title = '';
      if ($.isArray(result.briefResults.titleLine)) {
        title = result.briefResults.titleLine[0].value.trim();
      } else {
        title = result.briefResults.titleLine.value.trim();
      }
      showPreviewModal(title, result.naId);
    }
  };

  $scope.killWMVPlayer = function () {
    if (OpaUtils.isIE && visorSvc.currentWMV && document.getElementById(visorSvc.currentWMV)) {
      document.getElementById(visorSvc.currentWMV).Stop();
      visorSvc.currentWMV = "";
    }
  };

  $scope.destroyToolTips = function () {
    var allToolTips = ['all', 'online', 'image', 'document', 'video', 'audio', 'web'];
    angular.forEach(allToolTips, function (tab) {
      if ($('#' + tab).data("kendoTooltip")) {
        $('#' + tab).data("kendoTooltip").destroy();
      }
    });
  };

  /**
   * Creates the tooltips for the tabs and if a name is provided
   * it will show it inmediatly
   */
  $scope.createTooltips = function () {

    var allToolTips = ['all', 'online', 'web', 'document', 'image', 'video'];
    var totalResuls = [{title: "All", count: 0},
      {title: "Available Online", count: 0},
      {title: "Web Pages", count: 0},
      {title: "Documents", count: 0},
      {title: "Images", count: 0},
      {title: "Videos", count: 0}];

    if (!OpaUtils.isMobileDevice()) {

      var tooltipObj = {
        tooltip: undefined,
        target: ''
      };

      $scope.destroyToolTips();

      angular.forEach(searchSvc.tabsInfo.tabsCount, function (tabCount) {
        switch (tabCount['@name']) {
          case 'all':
            totalResuls[0].count = tabCount['@count'];
            break;

          case 'online':
            totalResuls[1].count = tabCount['@count'];
            break;

          case 'web':
            totalResuls[2].count = tabCount['@count'];
            break;

          case 'document':
            totalResuls[3].count = tabCount['@count'];
            break;

          case 'image':
            totalResuls[4].count = tabCount['@count'];
            break;

          case 'video':
            totalResuls[5].count = tabCount['@count'];
            break;
        }
      });

      angular.forEach(allToolTips, function (tabCount, index) {
        $('#' + tabCount).kendoTooltip({
          autoHide: true,
          position: "bottom",
          content: "<div>" + totalResuls[index].title + ": " + totalResuls[index].count + " results</div>"
        });
      });

      if (tooltipObj.target) {
        $(tooltipObj.target).data("kendoTooltip").show();
      }
    }
  };

  /**
   * "Expanded Queries"/ Select the currently expanded term in the Expanded Queries dialog.
   * @param term
   */
  $scope.selectTerm = function (term) {
    $scope.selectedTerm = term;
  };

  $scope.showExportModal = function () {
    if (searchSvc.selectedResults.length <= 0 && !exportSvc.naid && !searchSvc.topResults) {
      OpaUtils.showMessageModal("Export error", "You must select records to export");
      return;
    }
    $('#exportModal').modal('show');
    exportSvc.naid = null;
    exportSvc.cleanUp();
  };

  $scope.showPrintModal = function () {
    if (searchSvc.selectedResults.length <= 0 && !exportSvc.naid && !searchSvc.topResults) {
      OpaUtils.showMessageModal("Print error", "You must select records to print");
      return;
    }
    $('#printModal').modal('show');
    exportSvc.naid = null;
    exportSvc.cleanUp();
  };

  $scope.hidePaging = function () {
    var moreThanOnePage = $scope.page2 !== 0;
    return !$scope.searchFinished || $scope.noResults || $scope.onlyWebResults || (!moreThanOnePage && !searchSvc.limitExceeded);
  };

  $scope.hidePagingBotom = function () {
    var moreThanOnePage = $scope.page2 !== 0;
    return !$scope.searchFinished || $scope.noResults || $scope.onlyWebResults || (!moreThanOnePage && !OpaUtils.isMobileDevice());
  };

  $scope.setTabType = function (tab) {
    $location.search('tabType', tab);
    searchSvc.searchParams.tabType = tab;
  };

  $scope.setHighlight = function (isOn) {
    searchSvc.searchParams.highlight = isOn;
    if (searchSvc.checkIfIsDefaultSearchParam('highlight', isOn)) {
      $routeParams.highlight = undefined;
    }
    else {
      $routeParams.highlight = 'false';
    }
    $location.search($routeParams);
  };

  $scope.getError = function () {
    return $scope.error;
  };

  $scope.count = function (url) {
    logControllerSvc.logClick(url);
  };


  $scope.totalPages = function () {
    return PaginationSvc.getTotalPages($scope);
  };

  /**
   * Check if the result thumbnail show the hover preview icon hovering.
   * @param result
   * @returns {boolean}
   */
  $scope.showPlusSign = function (result) {
    return !!(result.thumbnailFile || (result.tabType.indexOf("audio") !== -1) || (result.tabType.indexOf("video") !== -1));
  };

  /**
   * Saves the current searchUrl in the browser cookieStore to be used later by Content Details.
   */
  var saveSearchParams = function (params) {
    $window.sessionStorage.setItem("sp", angular.toJson(params));
  };

  var clearCDVariables = function () {
    $window.sessionStorage.removeItem("sp");
    $window.sessionStorage.removeItem("sr");
    $window.sessionStorage.removeItem("naId");
  };

  /**
   * Initializes the controller.
   */
  (function () {
    var mappedParams = {};
    var specialEncodingSpaceCase = '%2520'; //NARA-2427

    if (typeof selectedTab === 'undefined') {
      $scope.selectedTab = 1;
      $scope.tabType = searchSvc.searchParams.tabType;
    }
    else {
      switch (selectedTab) {
        case 'all':
          $scope.selectedTab = 1;
          $scope.tabType = "all";
          break;
        case 'online':
          $scope.selectedTab = 2;
          $scope.tabType = "online";
          break;
        case 'web':
          $scope.selectedTab = 3;
          $scope.tabType = "web";
          break;
        case 'document':
          $scope.selectedTab = 4;
          $scope.tabType = "document";
          break;
        case 'image':
          $scope.selectedTab = 5;
          $scope.tabType = "image";
          break;
        case 'video':
          $scope.selectedTab = 6;
          $scope.tabType = "video";
          break;
      }
    }

    if (searchSvc.doSearch) {
      mappedParams = legacyUrlMappingService.mapLegacyParams();
      if (!$.isEmptyObject(mappedParams)) {
        if (mappedParams.redirectToAdvSearch) {
          $location.search({});
          $location.path('advancedsearch');
        } else {
          $location.search(mappedParams);
        }
        $location.replace();
        return; //cancel search and reload.
      }
      //NARA-2427:
      if ($routeParams.q.indexOf(specialEncodingSpaceCase) !== -1) {
        $location.search('q', $routeParams.q.replace(specialEncodingSpaceCase, ' '));
        $location.replace();
        return; //cancel search and reload.
      }
      $scope.setUrlParams();
      if ($location.path() === "/search") {
        $scope.searchTermsText = searchSvc.getQuery();
        $scope.updateOffset();
        $scope.search();
        $scope.getLists();
        $scope.isSearch = true;
        searchSvc.doSearch = false;
      } else {
        $scope.isSearch = false;
      }
    }
    // Checking if it is showing on mobile screen
    // The following does not work on IE.So first check is it not running on IE
    if (!OpaUtils.isOldIE && OpaUtils.isMobileDevice()) {
      $scope.destroyToolTips();
    }
  })();
});
