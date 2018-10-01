opaApp.service("statisticsSvc", function (Statistics) {
  this.getStatistics = function () {
    var statistics = new Statistics();
    return statistics.$get({},
      function (response) {
        return response;
      },
      function (error) {
        return error;
      });
  };
});
