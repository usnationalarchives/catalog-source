opaApp.service("logControllerSvc", function (LogController) {
  this.logClick = function (url) {
    var logController = new LogController();
    logController.$logClick({'url': url});
  };
});
