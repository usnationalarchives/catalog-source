opaApp.service('Auth', function ($injector, $timeout, $window, $log, ErrorCodesSvc, OpaUtils, configServices) {

  this.timer = null;

  this.isLoggedIn = function () {
    return !!$window.localStorage.getItem('OPAuser');
  };

  this.checkIsNotLoggedIn = function (errorObject) {
    return !!angular.equals(errorObject.data.opaResponse.error['@code'], ErrorCodesSvc.NOT_LOGGED_IN);
  };

  var clearUserInfo = function () {
    $window.localStorage.removeItem('OPAuser');
    $window.localStorage.removeItem('displayFullName');
    $window.localStorage.removeItem('fullName');
    $window.localStorage.removeItem('isNaraStaff');
    $window.localStorage.removeItem('accountRights');
    $window.localStorage.removeItem('timeout');
    $window.localStorage.removeItem('searchMaxRecords');
    $window.localStorage.removeItem('timerFinish');
    //$log.info((new Date()).toTimeString() + ': All values on localStorage have been removed');
  };

  this.logout = function () {
    clearUserInfo();
    //$log.info((new Date()).toTimeString() + ': Logout was called');
    $window.localStorage.removeItem('RowsQueryLimit');
    if (!OpaUtils.isOpenWorkspace()) {
      $injector.get('$route').reload();
    }
  };


  var timerCallback = function () {
    var endTime = localStorage.getItem('timerFinish');
    if ((new Date()).getTime() <= endTime) {
      var timeRemaining = endTime - (new Date()).getTime();
      //$log.info((new Date()).toTimeString() + ': remaining time en callback: ' + timeRemaining);
      if ($injector.get('Auth').timer) {
        $injector.get('$timeout').cancel($injector.get('Auth').timer);
      }
      $injector.get('Auth').timer = $injector.get('$timeout')(timerCallback, timeRemaining);
    } else {
      localStorage.removeItem('timerFinish');
      //$log.info((new Date()).toTimeString() + ': show Dialog');
      $injector.get('OpaUtils').showTimerModal();
    }
  };

  this.timerCallbackRef = timerCallback;

  this.resetTimeout = function () {
    if (this.timer) {
      $timeout.cancel(this.timer);
    }
    var startTimeMS = (new Date()).getTime();
    var timerDuration = ($window.localStorage.getItem('timeout') - 60000) || 1740000;
    localStorage.setItem('timerFinish', startTimeMS + timerDuration);
    var d = new Date();
    d.setTime(localStorage.getItem('timerFinish'));
    //$log.info((new Date()).toTimeString() + ': Logout in: ' + d.toTimeString());
    this.timer = $timeout(timerCallback, timerDuration);
    $window.localStorage.removeItem('timerModal');
  };

  this.setUserInfo = function (userInfo) {
    //Do not save undefined values in local storage, it causes exceptions when retrieving.
    if (typeof userInfo.userName === 'undefined') {
      userInfo.userName = '';
    }
    if (typeof userInfo.userId === 'undefined') {
      userInfo.userId = '';
    }
    if (typeof userInfo.displayFullName === 'undefined') {
      userInfo.displayFullName = false;
    }
    if (typeof userInfo.isFullNamePublic === 'undefined') {
      userInfo.isFullNamePublic = false;
    }
    if (typeof userInfo.fullName === 'undefined') {
      userInfo.fullName = '';
    }
    if (typeof userInfo.isNaraStaff === 'undefined') {
      userInfo.isNaraStaff = false;
    }
    if (typeof userInfo.rights === 'undefined') {
      userInfo.rights = '';
    }
    $window.localStorage.setItem('OPAuser', userInfo.userName || userInfo.userId || userInfo.id);
    $window.localStorage.setItem('displayFullName', userInfo.displayFullName || userInfo.isFullNamePublic);
    $window.localStorage.setItem('fullName', userInfo.fullName);
    $window.localStorage.setItem('isNaraStaff', userInfo.isNaraStaff);
    $window.localStorage.setItem('accountRights', userInfo.rights);
    if (userInfo.timeout) {
      $window.localStorage.setItem('timeout', userInfo.timeout * 1000);
    }
    if (userInfo.searchMaxRecords) {
      $window.localStorage.setItem('searchMaxRecords', userInfo.searchMaxRecords);
    }
  };

  this.userName = function (name) {
    if (name) {
      $window.localStorage.setItem('OPAuser', name);
    }
    return $window.localStorage.getItem('OPAuser');
  };

  this.topResults = function () {
    if (this.isLoggedIn()) {
      return $window.localStorage.getItem('searchMaxRecords');
    }
    return configServices.PUBLIC_TOP_RESULTS_LIMIT;
  };

  this.displayFullName = function (flag) {
    if (flag !== undefined) {
      $window.localStorage.setItem('displayFullName', flag);
    }
    var value = $window.localStorage.getItem('displayFullName');
    if (value) {
      value.toLowerCase();
    }
    else {
      return false;
    }
    return value === 'true';
  };

  this.isNaraStaff = function (flag) {
    if (flag !== undefined) {
      $window.localStorage.setItem('isNaraStaff', flag);
    }
    var value = $window.localStorage.getItem('isNaraStaff');
    if (value) {
      value.toLowerCase();
    }
    else {
      return false;
    }
    return value === 'true';
  };

  this.fullName = function (name) {
    if (name) {
      $window.localStorage.setItem('fullName', name);
    }
    return $window.localStorage.getItem('fullName');
  };

  this.getDisplayName = function () {
    var name = this.userName();

    if (this.displayFullName() || this.isNaraStaff()) {
      name = this.fullName();
      if (this.isNaraStaff()) {
        name += ' (NARA Staff)';
      }
    }
    return name;
  };

  this.isModerator = function () {
    return $window.localStorage.getItem('accountRights') === 'moderator';
  };

  this.isAdministrator = function () {
    return $window.localStorage.getItem('accountRights') === 'accountAdmin';
  };

  this.isAdminMod = function () {
    return $window.localStorage.getItem('accountRights') === 'accountAdminMod';
  };

  this.getAccountRights = function () {
    return $window.localStorage.getItem('accountRights');
  };

  this.setRowsQueryLimit = function (limit) {
    $window.localStorage.setItem('RowsQueryLimit', limit);
  };

  this.getRowsQueryLimit = function () {
    return $window.localStorage.getItem('RowsQueryLimit');
  };

  this.getUserId = function () {
    return $window.localStorage.getItem('OPAuser');
  };
});
