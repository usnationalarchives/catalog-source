opaApp.controller('calendarSearchCtrl', function ($scope, OpaUtils, dateService) {

  $scope.currentDate = new Date();
  $scope.currentMonth = $scope.currentDate.getMonth();
  $scope.currentYear = $scope.currentDate.getFullYear();
  $scope.calendarToolTips = dateService.calendarToolTips;

  /**
   * This options are used to configure kendo datepicker to only show years
   */
  $scope.yearSelectorOptions = {
    format: "yyyy",
    parseFormats: ["yyyy"],
    start: "decade",
    depth: "decade",
    footer: false,
    min: new Date(1700, 0, 1)
  };

  /**
   * Returns an array with numbers from 1 to 31.
   * @returns {Array} of days
   */
  $scope.getDefaultDayList = function () {
    var dateArray = [];
    dateArray.push('DD');
    for (var i = 1; i <= 31; i++) {
      dateArray.push(i);
    }
    return dateArray;
  };


  /**
   * Returns an array containing number from 1 to 12.
   * @returns {Array} of months
   */
  $scope.createMonthList = function () {
    var monthArray = [];
    monthArray.push('MM');

    for (var i = 1; i <= 12; i++) {
      monthArray.push(i);
    }
    return monthArray;
  };


  /**
   * Returns the number of days of a specific month and year taking in considerations leap years and
   * months with less than 31 days.
   * @param month
   * @param year
   * @returns {Array} of days
   */
  $scope.getDateList = function (month, year) {
    var date = new Date(year, month, 1);
    var result = [];
    result.push('DD');
    while (date.getMonth() === month) {
      result.push(date.getDate());
      date.setDate(date.getDate() + 1);
    }
    return result;
  };

  /**
   * Called every time the month or year field value changed.
   * @param from
   * @param month
   * @param year
   */
  $scope.updateDays = function (from, month, year) {
    var yearRegex = /^\d{4}$/;
    var monthValue = month && month !== "MM" ? month - 1 : $scope.currentMonth;
    var yearValue = year ? year : $scope.currentYear;

    $scope.validateDates();

    if (OpaUtils.isNumeric(yearValue)) {
      if (yearRegex.test(yearValue)) {
        var listDays = $scope.getDateList(monthValue, yearValue);
        if(from === -1){
          $scope.openDaysList = listDays;
          if (listDays.indexOf($scope.dateRange.dayOpenValue) === -1) {
            $scope.dateRange.dayOpenValue = 'DD';
          }
        }
        else if (from) {
          $scope.daysListFrom = listDays;
          if (listDays.indexOf($scope.dateRange.dayFromValue) === -1) {
            $scope.dateRange.dayFromValue = 'DD';
          }
        }
        else {
          $scope.daysListTo = listDays;
          if (listDays.indexOf($scope.dateRange.dayToValue) === -1) {
            $scope.dateRange.dayToValue = 'DD';
          }
        }
      }
    }
  };

  $scope.validateDates = function () {
    dateService.validateDates($scope.dateRange);
  };

  $scope.init = function () {
    $scope.openDaysList = $scope.getDefaultDayList();
    $scope.daysListFrom = $scope.getDefaultDayList();
    $scope.daysListTo = $scope.getDefaultDayList();
    $scope.monthList = $scope.createMonthList();
  };

  $scope.setTab = function (tab) {
    $scope.dateRange.currentTab = tab;
    $scope.validateDates();
  };

  $scope.init();

  $scope.$on('resetDaysList', function() {
    $scope.openDaysList = $scope.getDefaultDayList();
    $scope.daysListFrom = $scope.getDefaultDayList();
    $scope.daysListTo = $scope.getDefaultDayList();
  });

});
