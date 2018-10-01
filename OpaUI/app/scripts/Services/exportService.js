opaApp.service("exportSvc", function (Auth, Exports, Print, searchSvc, ListsService, configServices, OpaUtils, $timeout, $location) {

  this.formats = [
    'CSV', 'JSON', 'PDF', 'TXT', 'XML'
  ];

  this.thumbnails = false;
  this.descriptions = 'brief';
  this.format = null;
  this.naid = null;

  this.selectedList = null;

  this.userName = null;

  this.recordsInList = null;

  this.contributions = {};

  var displayError = null;

  var responseURL = null;

  var printingLists = false;

  this.generatePrintPage = function (printData, type) {
    var w = window.open();
    var html = "<head><title>Print Selected Records</title><style>.print {padding: 0; }.print .title {text-align: center; font-size: 1.2em; font-family: Arial, Helvetica, sans-serif;color: blue;margin: 10px auto 0; font-weight: bold;}" +
      ".print .title span {display: block; line-height: 22px; }.print .title p {margin-top: 25px;color: black;font-size: 0.813em;font-weight: normal; }.print hr {" +
      "margin: 0 0 8px; }.print .firsthr {border-width: 3px;margin-top: 10px; }.print .result {font-size: 1em;color: black;margin: 0; }.print .result .title {" +
      "text-align: left;font-size: 1.1em;margin: 10px 0; }.print .result span {display: inline-block; margin: 3px 0; }.print .result .fields {text-align: left;font-size: 1em;" +
      "color: black; }.print .result .fields .labelPrint {display: inline-block;font-weight: bold; } .print .result .section {display: inline-block;font-weight: bold; font-size: 1.2em; margin: 30px 0 20px;} .print .result .fields .valuePrint {display: inline-block;" +
      "font-weight: normal; text-indent: 20px; }.print .result .fields .thumbnail-div {display: inline-block;vertical-align: top;margin: 20px 10px 0 0; }.print .result .fields .thumbnail-content {" +
      "display: inline-block} .thumbnail-caption { width: 100px;} .print .result .blue-link {color: #0070c0;font-size: 1.1em;font-weight: bold; } .footer{font-size: 0.813em;font-family: Arial, Helvetica, sans-serif; margin-top:20px; } .footer span {display: block; text-align: center; font-style: italic}</style>";
    html += "<body onload='window.print(); window.close();'><div class='print'><div class='center-div title'><span>National Archives Catalog</span><span>U.S.National Archives & Records Administration</span><p>" +
    printData.length + " results</p></div><hr class='firsthr'><hr>";
    var htmlTemp;
    var index;
    for (index = 0; index < printData.length; index++) {
      var section = null;
      var result = printData[index];
      var title = (index + 1) + ". " + result.documentTitle;
      if (result.date) {
        title += ", " + result.date;
      }
      htmlTemp = "<div class='result'><span class='title'>" + title + "</span>";
      var newIndex = 0;
      if (result.fields) {
        if (result.fields.length) {
          for (newIndex = 0; newIndex < result.fields.length; newIndex++) {
            var field = result.fields[newIndex];
            if (field.section && section !== field.section) {
              htmlTemp += "<br><span class='section'>" + field.section + "</span>";
            }
            section = field.section;
            htmlTemp += "<div class='fields'>";
            if (field.label !== 'URL' && field.label !== 'Thumbnails') {
              if (field.label && field.value) {
                if (field.label !== field.section) {
                  htmlTemp += "<span class='labelPrint'>" + field.label + ":&nbsp;</span><br>";
                }
                if (typeof field.value === 'string' || field.value instanceof String || angular.isNumber(field.value)) {
                  try {
                    var replaced = field.value.replace(/\n/g, "<br>");
                    htmlTemp += "<span style='display: block' class='valuePrint'>" + replaced + "</span>";
                  } catch (e) {
                    htmlTemp += "<span style='display: block' class='valuePrint'>" + field.value + "</span>";
                  }
                }
                else {
                  for (var i = 0; i < field.value.length; i++) {
                    // “Creator(s)” - separate multiple creators with “;” (semi-colon) (Document: Archive Description DDM page 42)
                    if (field.label === 'Creator(s)') {
                      if (i < field.value.length - 1) {
                        htmlTemp += "<span class='valuePrint'>" + field.value[i] + "; </span>";
                      } else {
                        htmlTemp += "<span class='valuePrint'>" + field.value[i] + "</span>";
                      }
                    }
                    else {
                      htmlTemp += "<span class='valuePrint'>" + field.value[i] + "</span><br>";
                    }
                  }
                }
              }
            }
            if (field.label === 'Thumbnails') {
              htmlTemp += "<span class='labelPrint'>" + field.label + ":&nbsp;</span><br>";
              var thumbnails = getThumbnails(field.value);
              for (var indexThumbnails = 0; indexThumbnails < thumbnails.length; indexThumbnails++) {
                var thumbnailURL = thumbnails[indexThumbnails][0];
                if (thumbnailURL && thumbnailURL !== ' ') {
                  htmlTemp += "<div class='thumbnail-div'>" +
                    "<img  src='" + thumbnailURL + "'>";
                }
                var thumbnailDescription = thumbnails[indexThumbnails][1];
                if (thumbnailDescription) {
                  htmlTemp += "<div class='thumbnail-caption'>" + thumbnailDescription + "</div>";
                }
                htmlTemp += "</div>";
              }
            }
            /*
             * Print url field for webpages. Web pages does not have a nara Id
             */
            if (field.label === 'URL' && !result.naId) {
              htmlTemp += "<span class='labelPrint'>" + field.label + ":&nbsp;</span><br>";
              htmlTemp += "<a style='display: block' class='valuePrint' href='" + field.value + "'>" + field.value + "</a>";
            }
            htmlTemp += "</div>";
          }
        } else {
          htmlTemp += "<div class='fields'>";
          htmlTemp += "<span class='labelPrint'>" + result.fields.label + ":&nbsp;</span>";
          htmlTemp += "<span class='valuePrint'>" + result.fields.value + "</span>";
          htmlTemp += "</div>";
        }
      }
      if (type !== "full" && result.naId) {
        htmlTemp += "<a class='blue-link' href='/id/" + result.naId + "'>" +
          "<span>" + getHost() + "/id/" + result.naId + "</span></a>";
      }
      htmlTemp += "<hr class='firsthr'><hr></div>";
      html += htmlTemp;
    }
    html += "<div class='footer'><hr><span>National Archives Catalog</span><span>U.S. National Archives and Records Administration</span>" +
    "<span>8601 Adelphi Road</span><span>College Park, MD 20740</span><span>Email: <a href='mailto:catalog@nara.gov'>catalog@nara.gov</a></span><span>On the web: <a href='http://www.archives.gov/research/search/'>http://www.archives.gov/research/search/</a></span>" +
    "<span>Disclaimer:</span><span>The user contributed portion of this description has been contributed by a Citizen Archivist. NARA has not reviewed these contributions and cannot guarantee the information is complete, accurate or authoritative.</span></body>";
    w.document.write(html);
    w.document.close();
  };

  this.createExport = function (format) {
    var auth;
    var what;
    var additionalFilters;
    var opaIds;
    var payload;
    var cleanNaid;
    var searchparams = {};
    var item;
    var key;

    if (!format) {
      displayError = "You must select a format";
      return;
    }
    if (format) {
      this.format = format;
    }
    auth = 'noauth';
    if (Auth.isLoggedIn()) {
      auth = 'auth';
    }
    what = ['metadata'];
    for (item in this.contributions) {
      if (this.contributions[item]) {
        what.push(item);
      }
    }
    if (this.thumbnails) {
      what.push('thumbnails');
    }
    what = what.join(',');
    searchparams.auth = auth;
    searchparams['export.what'] = what;
    searchparams['export.type'] = this.descriptions;
    searchparams['export.format'] = this.format.toLowerCase();
    searchparams.rows = searchSvc.selectedResults.length;
    if (searchSvc.topResults) {
      searchparams.tabType = searchSvc.searchParams.tabType;
      searchparams.offset = searchSvc.searchParams.offset;
      searchparams.q = searchSvc.query;
      searchparams.rows = searchSvc.topResults;
      additionalFilters = {};
      searchSvc.additionalFilters.forEach(function (filter) {
        filter.v.forEach(function (value) {
          if (!additionalFilters['f.' + filter.searchEngineName]) {
            additionalFilters['f.' + filter.searchEngineName] = [];
          }
          additionalFilters['f.' + filter.searchEngineName].push(value.searchEngineName);
        });
      });
      for (key in additionalFilters) {
        if (additionalFilters[key].length > 1) {
          additionalFilters[key] = additionalFilters[key].join(" or ");
          additionalFilters[key] = '(' + additionalFilters[key] + ')';
        }
        searchparams[key] = additionalFilters[key];
      }
    }
    opaIds = [];
    searchSvc.selectedResults.forEach(function (id) {
      opaIds.push(id);
    });
    opaIds = opaIds.join(',');
    payload = {};
    if (this.naid) {
      searchparams.rows = 1;
      searchparams.naIds = this.naid;
    }
    else if (!searchSvc.topResults) {
      payload['export.ids'] = opaIds;
    }
    cleanNaid = false;

    Exports.create(searchparams, payload,
      function (data) {
        responseURL = configServices.API_LOCATION + data.opaResponse.exportFile.url;
        cleanNaid = true;
        if (OpaUtils.isOldIE && (searchparams['export.format'] === 'json' || searchparams['export.format'] === 'csv')) {
          document.getElementById('hiddenExportButton').target = '_self';
        }
        $timeout(function () {
          $('#exportModal').modal('hide');
          document.getElementById('hiddenExportButton').click();
          responseURL = null;
          searchSvc.cleanUpAccordion();
        }, 1000);
      },
      function (error) {
        if (!error.data) {
          error = null;
          searchSvc.cleanUpAccordion();
          $('#exportModal').modal('hide');
          return;
        }
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse) {
            if (error.data.opaResponse.error['@code'] === 'BULK_EXPORT' || error.data.opaResponse.error['@code'] === 'BULK_EXPORT_NON_AUTH') {
              $('#exportModal').modal('hide');
              $('#bulkExportModal').modal('show');
              OpaUtils.isBulkExport = true;
              displayError = error.data.opaResponse.error.description;
            }
          }
        }
      }
    );
    if (cleanNaid) {
      this.naid = null;
    }
  };

  this.getURL = function () {
    return responseURL;
  };

  this.cleanUp = function () {
    this.thumbnails = false;
    this.descriptions = 'brief';
    this.contributions = {};
    this.exportFormat = null;
    this.format = null;
    displayError = null;
    this.enableLists(false);
    this.selectedList = null;
    this.userName = null;
    this.recordsInList = null;
  };

  this.createPrint = function () {
    if (searchSvc.selectedResults.length <= 0 && ListsService.selectedRecords.length <= 0 && !this.selectedList && !this.naid && !searchSvc.topResults) {
      displayError = "You must select records to print";
      return;
    }
    if (this.recordsInList > configServices.PRINT_LIMIT || ListsService.selectedRecords.length > configServices.PRINT_LIMIT) {
      displayError = "User should not exceed " + configServices.PRINT_LIMIT + " records at a time when printing";
      return;
    }
    var results = searchSvc.selectedResults;
    var searchparams = {};
    var what = ['metadata'];
    for (var item in this.contributions) {
      if (this.contributions[item]) {
        what.push(item);
      }
    }
    if (this.thumbnails) {
      what.push('thumbnails');
    }
    what = what.join(',');
    searchparams['export.what'] = what;
    searchparams['export.type'] = this.descriptions;
    searchparams['export.format'] = 'print';
    searchparams.auth = 'noauth';
    if (printingLists) {
      results = ListsService.selectedRecords;
    }
    searchparams.rows = results.length;
    if (this.selectedList) {
      searchparams.listName = this.selectedList;
      searchparams.userName = this.userName;
      searchparams.rows = this.recordsInList;
      results = [];
    }
    if (searchSvc.topResults) {
      searchparams.tabType = searchSvc.searchParams.tabType;
      searchparams.offset = searchSvc.searchParams.offset;
      searchparams.q = searchSvc.query;
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
    }
    var opaIds = [];
    results.forEach(function (id) {
      opaIds.push(id);
    });
    opaIds = opaIds.join(',');
    var payload = {};
    if (this.naid) {
      searchparams.rows = 1;
      searchparams.naIds = this.naid;
    } else if (!searchSvc.topResults && opaIds) {
      payload['export.ids'] = opaIds;
    }
    if (!opaIds) {
      delete searchparams['export.ids'];
    }
    var cleanNaid = false;
    var generatePrintPage = this.generatePrintPage;
    Print.create(searchparams, payload,
      function (data) {
        cleanNaid = true;
        $('#printModal').modal('hide');
        searchSvc.cleanUpAccordion();
        generatePrintPage(data, searchparams['export.type']);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          if (error.data.opaResponse) {
            displayError = error.data.opaResponse.error.description;
          }
        }
      }
    );
    if (cleanNaid) {
      this.naid = null;
    }
  };

  this.enableLists = function (param) {
    printingLists = param;
  };

  var getHost = function () {
    return $location.host();
  };

  var getThumbnails = function (thumbnails) {
    var array = [];
    var index;
    try {

      for	(index = 0; index < thumbnails.length; index++) {

        array[index] = [];
        array[index][0] = thumbnails[index].object.url;
        array[index][1] = thumbnails[index].object.caption;
      }

    }
    catch (e) {
    }
    return array;
  };

  this.getError = function () {
    return displayError;
  };
});
