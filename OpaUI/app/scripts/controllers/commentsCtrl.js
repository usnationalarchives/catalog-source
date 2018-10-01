opaApp.controller('commentsController', function ($scope,Account, Auth, commentsService, visorSvc, OpaUtils,$timeout,LoginService,$routeParams) {

  $scope.Auth = Auth;
  $scope.OpaUtils = OpaUtils;
  $scope.commentText = '';
  $scope.editMode = false;
  $scope.LoginService = LoginService;
  $scope.isWorkspace = false;
  $scope.currentCommentId = $routeParams.commentId;

  $scope.canEditDelete = function (user) {
    return user === Auth.getUserId();
  };

  $scope.addComment = function () {

    if($scope.commentText) {

      commentsService.addComment($scope.$parent.naId, $scope.commentText, function (response, error) {
          if (!error) {
            $scope.init();
            $scope.commentText = '';
          } else {
            if (!OpaUtils.checkForAPIError(error)) {
              //check for other errors block
              OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            }
          }
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
          }
        });
    }
    else{
      OpaUtils.showErrorGlobalNotification("Please enter a valid comment");
    }
  };

  $scope.loadComments = function () {
    commentsService.getComments($scope.$parent.naId, function (data, error) {
      if (!error) {
        $scope.comments = data;
        commentsService.commentsCount = (parseInt($scope.comments['@total']) || 0) + (parseInt($scope.comments['@replies']) || 0);
      }
    });
  };

  $scope.refreshComments = $scope.init = function () {
    $scope.loadComments();
  };

  /**
   * This function checks if the current link is a reply sharing link on content detail, which by default hides replies.
   * This function make the reply visible.
   * @param comment
   * @returns {boolean}
   */

  $scope.checkSharing = function(comment){
    if($scope.currentCommentId === comment['@id']){
      comment.hide = false;
      return false;
    }
    return true;
  };

  $scope.init();


});
