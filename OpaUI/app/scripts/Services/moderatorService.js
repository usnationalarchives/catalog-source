opaApp.service("ModeratorService", function (ModeratorReasons, $modal, $log, OpaUtils) {

  this.filterText = "";
  this.openedLookup = false;

  this.prepareReasonsForSelect = function (reasons, header) {
    var reasonFormated = [];
    var headerRemove = [{text: 'Select reason for removal', value: ""}];
    var headerRestore = [{text: 'Select reason for restoration', value: ""}];
    var headerChange = [{text: 'Select reason for change', value: ""}];
    var headerAdmin = [{text: 'Select a reason', value: ""}];

    angular.forEach(reasons, function (reason, key) {
      var obj = {};
      obj.text = reason['@reason'];
      obj.value = reason['@reasonId'];
      reasonFormated.push(obj);
    });

    var addNewReason = {text: 'Create new reason...', value: -1, group: '─────────────────────'};
    reasonFormated.push(addNewReason);

    switch (header) {
      case 'remove':
        reasonFormated = headerRemove.concat(reasonFormated);
        break;
      case 'restore':
        reasonFormated = headerRestore.concat(reasonFormated);
        break;
      case 'change':
        reasonFormated = headerChange.concat(reasonFormated);
        break;
      case 'admin':
        reasonFormated = headerAdmin.concat(reasonFormated);
        break;
    }

    return reasonFormated;
  };

  this.getReasons = function () {
    var m = new ModeratorReasons();
    var params = {};

    var call = m.$get(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      });

    return call;

  };

  this.addReason = function (reason) {
    var m = new ModeratorReasons();
    var params = {'text': reason};

    var call = m.$add(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      });

    return call;
  };

  /**
   * list of the methods to update the contributions
   * @type {Array}
   */
  var callback = null;

  /**
   * register an updater callback
   * @param callback
   */
  this.registerUpdateCallback = function (p_callback) {
    callback = p_callback;
  };

  /**
   * excecutes the update methods
   */
  this.notifyUpdaters = function () {
    callback();
  };

  this.showNewReasonModal = function () {
    var modal = $modal.open({
      templateUrl: 'views/directives/dialogs/newReason.html',
      controller: ['$scope', '$modalInstance', function ($scope, $modalInstance) {

        $scope.moderator = true;

        $scope.ui =
        {
          newReason: ""
        };

        $scope.add = function () {
          $modalInstance.close($scope.ui.newReason);
        };

        $scope.cancel = function () {
          $modalInstance.dismiss();
        };
      }],
      size: 'sm'
    });

    return modal;
  };
});
