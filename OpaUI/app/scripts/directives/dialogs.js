'use strict';
opaApp.directive('bulkExportDialog', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/bulkExportDialog.html'
  };
});

opaApp.directive('exportDialog', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/exportdialog.html'
  };
});

opaApp.directive('printDialog', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/printdialog.html'
  };
});

opaApp.directive('accordionBar', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/accordionbar.html',
    link: function (scope) {
      $('#accordion').on('show.bs.collapse', function () {
        try {
          scope.showCheckboxes();
        }
        catch (error) {
        }
      });
      $('#listscollapse').on('shown.bs.collapse', function () {
        try {
          scope.setPanel('lists');
        }
        catch (error) {
        }
      });
      $('#printcollapse').on('shown.bs.collapse', function () {
        scope.setPanel('print');
      });
      $('#exportcollapse').on('shown.bs.collapse', function () {
        scope.setPanel('export');
      });
      $('#accordion').on('hide.bs.collapse', function () {
        try {
          scope.clearCheckboxes();
        }
        catch (error) {
        }
      });
    }
  };
});

opaApp.directive('forgotPassword', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/forgotpassworddialog.html'
  };
});

opaApp.directive('forgotUsername', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/forgotusernamedialog.html'
  };
});

opaApp.directive('expandedQuery', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/expandedquerydialog.html'
  };
});


opaApp.directive('workspaceModal', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    controller: 'workspaceCtrl',
    templateUrl: 'views/directives/dialogs/workspace.html',
    link: function postLink(scope, iElement,location) {
      //Created the tooltip with the creation time

      // Trigger when number of children changes, including by directives like ng-repeat, ng-options, etc.
      var watch = scope.$watch(function () {
        return iElement.children().length;
      }, function () {
        // Wait for templates to render
        scope.$evalAsync(function () {

          //scope.scrollToComment();

        });
      });
    }
  };
});

opaApp.directive('workspaceResponsive', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    controller: 'workspaceCtrl',
    templateUrl: 'views/directives/dialogs/workspace-responsive.html'
  };
});

opaApp.directive('createList', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/createList.html',
    link: function (scope) {
      $('#createList')
        .on('show.bs.modal', function () {
          scope.$parent.error = "";
          scope.newListName = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        })
        .on('hide.bs.modal', function () {
          scope.newListName = "";
          scope.$parent.error = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        });
    }
  };
});

opaApp.directive('deleteListsConfirmation', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/deleteListsConfirmationDialog.html'
  };
});

opaApp.directive('deleteListConfirmation', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/deleteListConfirmationDialog.html'
  };
});

opaApp.directive('deleteRecordsConfirmation', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/deleteRecordsConfirmationDialog.html'
  };
});

opaApp.directive('renameListConfirmation', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/renameListConfirmationDialog.html'
  };
});

opaApp.directive('changePassword', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/changePasswordDialog.html',
    link: function (scope) {
      $('#changePassword')
        .on('show.bs.modal', function () {
          scope.error = "";
          scope.oldPassword = "";
          scope.newPassword = "";
          scope.confirmPassword = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        })
        .on('hide.bs.modal', function () {
          scope.error = "";
          scope.oldPassword = "";
          scope.newPassword = "";
          scope.confirmPassword = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        });
    }
  };
});

opaApp.directive('changeEmailAddress', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/changeEmailAddressDialog.html',
    link: function (scope) {
      $('#changeEmail')
        .on('show.bs.modal', function () {
          scope.error = "";
          scope.password = "";
          scope.newEmail = "";
          scope.confirmEmail = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        })
        .on('hide.bs.modal', function () {
          scope.error = "";
          scope.password = "";
          scope.newEmail = "";
          scope.confirmEmail = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        });
    }
  };
});

opaApp.directive('changeFullName', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/changeFullName.html',
    link: function (scope) {
      $('#changeFullName')
        .on('show.bs.modal', function () {
          scope.error = "";
          scope.password = "";
          scope.newName = "";
          scope.confirmName = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        })
        .on('hide.bs.modal', function () {
          scope.error = "";
          scope.password = "";
          scope.newName = "";
          scope.confirmName = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        });
    }
  };
});

opaApp.directive('deactivateAccount', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/deactivateAccountDialog.html',
    link: function (scope) {
      $('#deactivateAccount')
        .on('show.bs.modal', function () {
          scope.error = "";
          scope.deactivatePassword = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        })
        .on('hide.bs.modal', function () {
          scope.error = "";
          scope.deactivatePassword = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        });
    }
  };
});

opaApp.directive('resetPassword', function () {
  return {
    restrict: 'A',
    transclude: true,
    replace: true,
    templateUrl: 'views/directives/dialogs/resetPasswordConfirmationDialog.html',
    link: function (scope) {
      $('#resetPassword')
        .on('show.bs.modal', function () {
          scope.error = "";
          scope.deactivatePassword = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        })
        .on('hide.bs.modal', function () {
          scope.error = "";
          scope.deactivatePassword = "";
          if (!scope.$$phase) {
            scope.$apply();
          }
        });
    }
  };
});


opaApp.directive('onFinishRender', function ($timeout) {
  return {
    restrict: 'A',
    link: function (scope, element, attr) {
      if (scope.$last === true) {
        $timeout(function () {
          scope.$emit('ngRepeatFinished');
        });
      }
    }
  };
});
