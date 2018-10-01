opaApp.service('AdministratorService', function (AdministratorReasons, $modal, ManageAccounts, $log, OpaUtils) {

  this.reasons = [];

  this.getReasons = function () {
    var m = new AdministratorReasons();
    var params = {};

    return m.$get(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      });
  };

  this.prepareReasonsForSelect = function (reasons) {
    var reasonFormated = [];
    var header = {text: 'Select a reason', value: ''};
    reasonFormated.push(header);

    angular.forEach(reasons, function (reason) {
      var obj = {};
      obj.text = reason['@reason'];
      obj.value = reason['@reasonId'];
      reasonFormated.push(obj);
    });

    var addNewReason = {text: 'Create new reason...', value: -1, group: '─────────────────────'};
    reasonFormated.push(addNewReason);

    return reasonFormated;
  };

  this.addReason = function (reason) {
    var m = new AdministratorReasons();
    var params = {'text': reason};

    return m.$add(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      });
  };

  this.showNewReasonModal = function () {
    return $modal.open({
      templateUrl: 'views/directives/dialogs/newReason.html',
      controller: ['$scope', '$modalInstance', function ($scope, $modalInstance) {

        $scope.moderator = false;
        $scope.ui =
        {
          newReason: ''
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
  };

  //Get notes
  this.getNotes = function (username) {
    var manageAccounts = new ManageAccounts({'accountOwner': username});
    return manageAccounts.$viewAccountNotes({},
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
      }
    );
  };
});
