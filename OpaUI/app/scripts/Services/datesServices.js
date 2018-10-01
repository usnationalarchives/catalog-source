opaApp.service('dateService', function (OpaUtils) {

  this.calendarToolTips = {
    SEARCH_DATE_RANGE: 'Searches a date range.',
    SEARCH_DATE_EXACT: 'Searches an exact date, e.g., December 7, 1941.',
    SEARCH_DATE_RECURRING: 'Searches a day that occurs every year, e.g., March 17.'
  };


  this.getRangeDates = function (yearFrom, monthFrom, dayFrom, yearTo, monthTo, dayTo) {
    var endDate;
    var beginDate;
    var range = [0, 0];
    var dateValues = [];
    if (yearFrom) {
      dateValues.push(parseInt(yearFrom));
      if (monthFrom !== 'MM') {
        dateValues.push(parseInt(monthFrom) - 1);
      } else {
        dateValues.push(0);
      }
      //Months in javascript are 0 based 0-11
      if (dayFrom !== 'DD') {
        dateValues.push(parseInt(dayFrom));
      } else {
        dateValues.push(1);
      }
      beginDate = new Date(dateValues[0], dateValues[1], dateValues[2]);
      range[0] = beginDate;
    }
    if (yearTo) {
      dateValues = [];
      dateValues.push(parseInt(yearTo));
      if (monthTo !== 'MM') {
        dateValues.push(parseInt(monthTo) - 1);
      } else {
        dateValues.push(11);
      }
      //Javascript months are '0' based 0-11
      if (dayTo !== 'DD') {
        dateValues.push(parseInt(dayTo));
      } else {
        if (dateValues[1] === 11) {
          dateValues[0] += 1;
          dateValues[1] = 0;
        } else {
          dateValues[1] += 1;
        }
        dateValues[2] = 0;
      }
      endDate = new Date(dateValues[0], dateValues[1], dateValues[2]);
      range[1] = endDate;
    }
    return range;
  };

  this.validateDates = function (dateRange) {
    var range;
    var errors = [];
    var yearRegex = /^\d{4}$/;
    dateRange.errorFlag = false;

    if (dateRange.currentTab === 'range') {
      if (dateRange.yearFromValue === '' && (dateRange.monthFromValue !== 'MM' || dateRange.dayFromValue !== 'DD')) {
        dateRange.errorFlag = true;
        errors.push('Date Range: Begin year must be set');
      }
      if (dateRange.yearToValue === '' && (dateRange.monthToValue !== 'MM' || dateRange.dayToValue !== 'DD')) {
        dateRange.errorFlag = true;
        errors.push('Date Range: End year must be set');
      }

      if (dateRange.yearToValue !== '' && (!yearRegex.test(dateRange.yearToValue) || !OpaUtils.isNumeric(dateRange.yearToValue))) {
        dateRange.errorFlag = true;
        errors.push('Date Range: End year must be a valid number');
      }
      if (dateRange.yearFromValue !== '' && (!yearRegex.test(dateRange.yearFromValue) || !OpaUtils.isNumeric(dateRange.yearFromValue))) {
        dateRange.errorFlag = true;
        errors.push('Date Range: Begin year must be a valid number');
      }

      range = this.getRangeDates(dateRange.yearFromValue,
        dateRange.monthFromValue,
        dateRange.dayFromValue,
        dateRange.yearToValue,
        dateRange.monthToValue,
        dateRange.dayToValue);

      if (range[0] !== 0 && range[1] !== 0) {
        if (range[0].getTime() > range[1].getTime()) {
          dateRange.errorFlag = true;
          errors.push('Date Range: End date must be after the begin date');
        }
      }
    }
    else if (dateRange.currentTab === 'exact') {
      if (dateRange.exactYearValue === '' && (dateRange.exactMonthValue !== 'MM' || dateRange.exactDayValue !== 'DD')) {
        dateRange.errorFlag = true;
        errors.push('Exact Date: Year must be set');
      }

      if(dateRange.exactYearValue !== '') {
        if (dateRange.exactMonthValue === 'MM') {
          dateRange.errorFlag = true;
          errors.push('Exact Date: Month must be set');
        }

        if (dateRange.exactDayValue === 'DD') {
          dateRange.errorFlag = true;
          errors.push('Exact Date: Day must be set');
        }

        if (!yearRegex.test(dateRange.exactYearValue) || !OpaUtils.isNumeric(dateRange.exactYearValue)) {
          dateRange.errorFlag = true;
          errors.push('Exact Date: Year must be a valid number');
        }
      }
    }
    else if (dateRange.currentTab === 'open') {
      if (dateRange.dayOpenValue !== 'DD') {
        if (dateRange.monthOpenValue === 'MM') {
          dateRange.errorFlag = true;
          errors.push('Recurring Date: Month must be set');
        }
      }
    }

    if (dateRange.errorFlag) {
      dateRange.errors = errors;
    }
  };

});
