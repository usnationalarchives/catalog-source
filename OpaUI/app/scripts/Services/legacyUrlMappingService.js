opaApp.service("legacyUrlMappingService", function ($routeParams) {

  this.dataSourceMap = {
    'all': '',
    'archival descriptions': 'descriptions',
    'archival-descriptions': 'descriptions',
    'archival descriptions with digital objects': 'online',
    'archival-descriptions-with-digital-objects': 'online',
    'authority records': 'authority',
    'authority-records': 'authority',
    'archives gov': 'archives.gov',
    'archives-gov': 'archives.gov',
    'presidential libraries': 'presidential',
    'presidential-libraries': 'presidential',
    'eop sedata digital': 'opt6',
    'eop-sedata-digital': 'opt6'
  };

  this.materialsTypeMap = {
    'textual records': 'text',
    'textual-records': 'text',
    'photographs and other graphic materials': 'photographsandgraphics',
    'photographs-and-other-graphic-materials': 'photographsandgraphics',
    'maps and charts': 'mapsandcharts',
    'maps-and-charts': 'mapsandcharts',
    'moving images': 'movingimages',
    'moving-images': 'movingimages',
    'sound recordings': 'sound',
    'sound-recordings': 'sound',
    'data files': 'datafiles',
    'data-files': 'datafiles',
    'architectural-and-engineering-drawings': 'drawings',
    'artifacts': 'artifacts',
    'web pages': 'web',
    'web-pages': 'web'
  };

  this.levelOfDescriptionMap = {
    'file': 'fileunit',
    'record group': 'recordgroup',
    'record-group': 'recordgroup',
    'item': 'item',
    'series': 'series',
    'collection': 'collection'
  };

  this.fileFormatMap = {
    'ascii text': 'text/plain',
    'ascii-text': 'text/plain',
    'audio visual (real media video stream)': 'application/vnd.rn-realmedia',
    'audio-visual-(real-media-video-stream)': 'application/vnd.rn-realmedia',
    'audio visual file (avi)': 'video/x-msvideo',
    'audio-visual-file-(avi)': 'video/x-msvideo',
    'audio visual file (mov)': 'video/quicktime',
    'audio-visual-file-(mov)': 'video/quicktime',
    'audio visual file (mp4)': 'video/mp4',
    'audio-visual-file-(mp4)': 'video/mp4',
    'audio visual file (wmv)': 'video/x-ms-wmv',
    'audio-visual-file-(wmv)': 'video/x-ms-wmv',
    'compressed file (zip)': 'application/zip',
    'compressed-file-(zip)': 'application/zip',
    'image (bmp)': 'image/bmp',
    'image-(bmp)': 'image/bmp',
    'image (gif)': 'image/gif',
    'image-(gif)': 'image/gif',
    'image (jpg)': 'image/jpeg',
    'image-(jpg)': 'image/jpeg',
    'image (tiff)': 'image/tiff',
    'image-(tiff)': 'image/tiff',
    'ms excel spreadsheet': 'application/excel',
    'ms-excel-spreadsheet': 'application/excel',
    'microsoft powerpoint document': 'application/mspowerpoint',
    'microsoft-powerpoint-document': 'application/mspowerpoint',
    'microsoft word document': 'application/msword',
    'microsoft-word-document': 'application/msword',
    'microsoft write document': 'application/mswrite',
    'microsoft-write-document': 'application/mswrite',
    'portable document file (pdf)': 'application/pdf',
    'portable-document-file-(pdf)': 'application/pdf',
    'sound file (mp3)': 'audio/mpeg3',
    'sound-file-(mp3)': 'audio/mpeg3',
    'sound file (wav)': 'audio/x-wav',
    'sound-file-(wav)': 'audio/x-wav',
    'world wide web page': 'text/html',
    'world-wide-web-page': 'text/html'
  };

  this.mapLegacyParams = function () {
    var mappedParams = {};
    var legacyUrlMappingService = this;
    var mapParam = function (legacyValue, newParamName, mapValuesObj, context) {
      var tempArr = [];
      var i;
      var lowerCaseValue = "";
      if (legacyValue && $.isArray(legacyValue)) {
        for (i = 0; i < legacyValue.length; i++) {
          lowerCaseValue = legacyValue[i].toLowerCase();
          if (mapValuesObj[lowerCaseValue]) {
            tempArr.push(mapValuesObj[lowerCaseValue]);
          } else if (lowerCaseValue === 'all') {
            tempArr = [];
            break;
          }
        }
        if (tempArr.length) {
          context[newParamName] = "(" + tempArr.join(' or ') + ")";
        }
      } else {
        lowerCaseValue = legacyValue.toLowerCase();
        if (mapValuesObj[lowerCaseValue]) {
          context[newParamName] = mapValuesObj[lowerCaseValue];
        }
      }
    };
    var extractDate = function(dateValue, beginDateName, endDateName, context){
      var date;
      date = dateValue.split(':');
      if (date && $.isArray(date) && date.length) {
        if (date[0]){
          context[beginDateName] = date[0];
        }
        if (date.length > 1 && date[1]) {
          context[endDateName] = date[1];
        }
      }
    };
    var extractDateRange = function(dateValue, newDateName, context){
      var date;
      date = dateValue.split(':');
      if (date && $.isArray(date) && date.length > 1 && date[0] && date[1]) {
        context[newDateName] = '"'+ date[0] +' - '+date[1] + '"';
      }
    };

    var setValue = function(paramName, value, context) {
      if ($.isArray(value)) {
        context[paramName] = "(" + value.join(' or ') + ")";
      } else {
        context[paramName] = value;
      }
    };

    angular.forEach($routeParams, function (value, key) {
      if(angular.equals(key,"q")){
        value = value.replace("%","%25");
      }
      var decodedValue = decodeURIComponent(value);
      switch (key.toLowerCase()) {
        case 'searchexpression':
        case 'existing-search':
        case 'expression':
          this.q = decodedValue;
          break;
        case 'data-source':
        case 'refinegrp_data-source':
          mapParam(decodedValue, 'f.oldScope', legacyUrlMappingService.dataSourceMap, this);
          break;
        case 'desc-type':
        case 'refinegrp_material-type':
          mapParam(decodedValue, 'f.materialsType', legacyUrlMappingService.materialsTypeMap, this);
          break;
        case 'desc-level':
        case 'refinegrp_arc-ad-type':
          mapParam(decodedValue, 'f.level', legacyUrlMappingService.levelOfDescriptionMap, this);
          break;
        case 'desc-loc':
        case 'location':
        case 'refinegrp_location':
          setValue('f.locationIds', decodedValue, this);
          break;
        case 'desc-title':
          this['f.allTitles'] = decodedValue;
          break;
        case 'desc-geo':
          this['f.geographicReferences'] = decodedValue;
          break;
        case 'desc-rg':
          this['f.recordGroupNumber'] = decodedValue;
          break;
        case 'desc-collid':
          this['f.collectionIdentifier'] = decodedValue;
          break;
        case 'desc-creator':
          this['f.creators'] = decodedValue;
          break;
        case 'desc-descid':
          this['f.descriptionIdentifier'] = decodedValue;
          break;
        case 'tag-search':
          this['f.tagsKeywordsAdv'] = decodedValue;
          break;
        case 'desc-format':
        case 'refinegrp_format':
          mapParam(decodedValue, 'f.fileFormat', legacyUrlMappingService.fileFormatMap, this);
          break;
        case 'sw_arc_id':
        case 'sw_desclevel':
          setValue('f.ancestorNaIds', decodedValue, this);
          break;
        case 'aut-date':
          extractDate(decodedValue, 'f.authorityStartYear', 'f.authorityEndYear', this);
          break;
        case 'desc-date':
          extractDate(decodedValue, 'f.descriptionStartYear', 'f.descriptionEndYear', this);
          break;
        case 'refinegrp_dates':
          extractDateRange(decodedValue, 'f.dateRangeFacet', this);
          break;
        case 'form':
          if(decodedValue === 'opa-advanced') {
            this.redirectToAdvSearch = true;
          }
          break;
        default:
          break;
      }
    }, mappedParams);

    if (!$.isEmptyObject(mappedParams)) {
      if (mappedParams.q === undefined || typeof mappedParams.q === null || mappedParams.q === '') {
        mappedParams.q = '*:*';
      }
    }
    return mappedParams;
  };
});
