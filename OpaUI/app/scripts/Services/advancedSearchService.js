opaApp.service('AdvancedSearchService', function ($log, $location, ErrorCodesSvc, OpaUtils, searchSvc, SearchFilters) {

  /**
   * Congressional Records list for the range slider.
   */
  this.congressList = null;
  this.FIRST_CONGRESS_YEAR = 1789;

  /**
   * Mapping for date array read from URL
   */
  this.DATE_MAP = {
    YEAR : 0,
    MONTH : 1,
    DAY : 2,
    VALID_LENGTH : 3
  };

  /**
   * Advanced search page tooltips
   */
  this.toolTips = {
    SEARCH_TERM: 'Searches keywords in all fields of archival descriptions, authority records, and web pages.',
    PERSON_OR_ORGANIZATION_NAME: 'Searches for the names of people and organizations in descriptions and authority records.',
    TAGS: 'Searches user-contributed tags in archival descriptions, digital content, and authority records.',
    RECORD_GROUP_NUMBER_COLLECTION_ID: 'Searches archival descriptions that are part of the specified record group number or collection identifier.',
    DATES: 'Searches the date fields of archival descriptions.',
    LIMIT_SEARCH: 'Limits the search to the data source selected.',
    ARCHIVAL_DESCRIPTIONS: 'Limits search to descriptions of archival records. Descriptions may or may not have archival materials online.',
    AUTHORITY_RECORDS: 'Limits search to authority records including people and organizations.',
    WEB_PAGES: 'Limits search to National Archives web pages including Archives.gov and presidential libraries pages.',
    TYPE_OF_ARCHIVAL_MATERIALS: 'Limits search of archival descriptions to specific record types. By default, all record types are searched.',
    LEVEL_OF_DESCRIPTIONS: 'Limits search of archival descriptions to specific levels of the archival hierarchy: record groups/collections, series, file units, and items. By default, all levels are searched.',
    RECORD_GROUP: 'Overall grouping of archival records created by major government entity.  Each record group is designated by a record group number.',
    COLLECTION: 'An accumulation of documents brought together on the basis of a shared characteristic.  Each collection is designated by a collection identifier.',
    SERIES: 'Archival records created and used together for a specific purpose during a specific time period.',
    FILE_UNIT: 'Several archival records within a series that are grouped together, often in a file folder or volume.',
    ITEM: 'A single record within a group of archival materials.  This also includes audio-visual items.',
    FILE_FORMAT_OF_ARCHIVAL_DESCRIPTIONS: 'Limits search of archival descriptions based on the file formats of online archival materials. By default, all file formats are searched.',
    LOCATION_OF_ARCHIVAL_MATERIALS: 'Limits search of archival descriptions based on National Archives locations. By default, all locations are searched.',
    TITLE: 'Searches keywords in the title of archival descriptions.',
    GEOGRAPHIC_REFERENCES: 'Searches keywords in geographic references of archival descriptions.',
    CREATOR: 'Searches keywords in the creator of archival descriptions.',
    DESCRIPTION_IDENTIFIER: 'Searches archival descriptions that reference a particular description identifier, including National Archives Identifier.',
    CONGRESSIONAL_RECORDS: 'These search fields apply only to Congressional records held by the Center for Legislative Archives.',
    CONGRESS_RANGE: 'Limits search to a single Congress or range of Congresses. By default all Congresses are searched.',
    BEGIN_CONGRESS: 'Enter a begin Congress number to narrow search to a single Congress or range of Congresses.',
    END_CONGRESS: 'Enter an end Congress number to narrow search to a single Congress or range of Congresses',
    CONGRESS_SLIDER: 'Slide the right and left circles to narrow the search to a single Congress or range of Congresses.',
    PRESIDENTIAL_ELECTRONIC_RECORDS: 'These search fields apply only to electronic records held by the George W. Bush Library and the Presidential Materials Division.',
    FOIA_CASE_NUMBER: 'Enter a FOIA Case Number assigned to a particular Presidential/Vice Presidential Electronic Records FOIA request.',
    TYPE_OF_RECORD: 'Filter Presidential and Vice Presidential electronic records by type of record. Options include email, photographs, and White House Worker and Visitor Entry System (WAVES) records.',
    WH_EMAILS_DATE: 'Searches the date fields of emails.',
    WH_EMAIL_FROM: 'Searches the name of email senders.',
    WH_EMAIL_TO: 'Searches the name(s) of email recipients.',
    WH_EMAIL_CC: 'Searches the name(s) of individual(s) copied on emails.',
    WH_EMAIL_BCC: 'Searches the name(s) of individual(s) blind copied on emails.',
    WH_EMAIL_SUBJECT: 'Searches the email subject line.',
    WH_PHOTO_DATE: 'Searches the date of a photograph.',
    WH_PHOTO_PHOTOGRAPHER: 'Searches the name of the photographer who took the photograph.',
    WH_PHOTO_CITY: 'Searches the city in which the photograph was taken.',
    WH_PHOTO_STATE: 'Searches the state in which the photograph was taken.',
    WH_PHOTO_COUNTRY: 'The country in which the photograph was taken.',
    WH_PHOTO_TITLE: 'Searches the title of the event being photographed.',
    WH_PHOTO_FRAME_NUMBER: 'Searches the number assigned to the unique frame. Combined with the Roll Number, it identifies the photograph.',
    WH_PHOTO_ROLL_NUMBER: 'Searches the number assigned to the roll of film that contained the photograph. Together with the Frame Number, it identifies the photograph.',
    WH_PHOTO_CAPTION: 'Searches the caption that the White House staff assigned to the photograph or roll of film.',
    WH_PHOTO_KEYWORDS: 'Searches the keywords assigned to the photograph by White House staff.',
    WH_WORKER_ARRIVAL_DATE_RANGE: 'Searches the date range for the date of a visitor’s arrival.',
    WH_WORKER_DEPARTURE_DATE_RANGE: 'Searches the date range for the date of a visitor’s departure.',
    WH_WORKER_LAST_NAME: 'Searches the last name of White House visitor.',
    WH_WORKER_FIRST_NAME: 'Searches the first name of White House visitor.',
    WH_WORKER_MIDDLE_INITIAL: 'Searches the middle initial of White House visitor.',
    WH_WORKER_VISITEE_LAST_NAME: 'Searches the last name of person being visited.',
    WH_WORKER_VISITEE_FIRST_NAME: 'Searches the first name of person being visited.',
    WH_WORKER_CALLER_LAST_NAME: 'Searches the last name of the White House staff member who scheduled the visit in WAVES.',
    WH_WORKER_CALLER_FIRST_NAME: 'Searches the first name of the White House staff member who scheduled the visit in WAVES.'
  };

  /**
   * Source filters name and search engine values.
   */
  this.sourceFiltersDictionary = {
    ARCHIVAL_DESCRIPTIONS: {name: "Archival Descriptions", value: "descriptions"},
    AUTHORITY_RECORDS: {name: "Authority Records", value: "authority"},
    ONLINE_ARCHIVAL_DESCRIPTIONS: {name: "Archival Materials Online", value: "online"},
    WEB: {name: "Web Pages", value: "archives.gov"},
    PRESIDENTIAL: {name: "Presidential Web Pages", value: "presidential"}
  };

  /**
   * TODO: check if this can be replaced with sourceFiltersDictionary
   * Sources used in template
   */
  this.sources = [
    {
      Name: "Archival Descriptions",
      Value: "descriptions",
      ToolTip: "Limits search to descriptions of archival records. Descriptions may or may not have archival materials online."
    },
    {
      Name: "Archival Materials Online",
      Value: "online",
      ToolTip: 'Limits search to only online archival materials, including digitized records and electronic records. Uncheck to search all archival descriptions.'
    },
    {
      Name: "Authority Records",
      Value: "authority",
      ToolTip: "Limits search to authority records including people and organizations."
    },
    {
      Name: "Web Pages",
      Value: "archives.gov",
      ToolTip: "Limits search to National Archives web pages including Archives.gov and presidential libraries pages."
    }
  ];

  /**
   * Array that contains the locations that have the attribute: HasPresidentialElectronicRecords = true;
   */
  this.locationsWithPresidentialElectronicRecords = (function () {
    var locationsArray = [];
    angular.forEach(SearchFilters.Location(), function (location) {
      if (location.HasPresidentialElectronicRecords) {
        locationsArray.push(location.Value);
      }
    });
    return locationsArray;
  })();

  /**
   * Array that contains the locations that have the attribute: HasCongressionalRecords = true;
   */
  this.locationsWithCongressionalRecords = (function () {
    var locationsArray = [];
    angular.forEach(SearchFilters.Location(), function (location) {
      if (location.HasCongressionalRecords) {
        locationsArray.push(location.Value);
      }
    });
    return locationsArray;
  })();

  //
  /**
   * Section "constants"
   * @type {{COMMON: string,
   *         DESCRIPTIONS: string,
   *         AUTHORITY: string,
   *         WEB: string,
   *         PRESIDENTIAL: string,
   *         CONGRESS: string}}
   */
  this.sections = {
    COMMON: "Common search fields",
    DESCRIPTIONS: "Archival Descriptions",
    AUTHORITY: "Authority Records",
    WEB: "Web Pages",
    PRESIDENTIAL: "Presidential Libraries",
    CONGRESS: "Congressional dates",
    ONLY_AUTHORITY: "ONLY_AUTHORITY"
  };

  /**
   * Select options from filter parameters
   * @param {*} optionsObject
   * @param {string} filter
   *
   */
  this.loadOptions = function (optionsObject, filter) {
    var filtersArray = [];
    if (searchSvc.filterParams[filter]) {
      filtersArray = SearchFilters.removeParenthesis(searchSvc.filterParams[filter]).split(" or ");
      optionsObject.selectedValues = filtersArray;
    }
  };
});
