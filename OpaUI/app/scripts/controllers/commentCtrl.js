opaApp.controller('commentController', function ($scope, Account, Auth, commentsService, visorSvc, OpaUtils, $location, $routeParams, $timeout) {

  $scope.OpaUtils = OpaUtils;
  $scope.Auth = Auth;
  $scope.editMode = false;
  $scope.addReply = false;
  $scope.reply = {
    text: ''
  };
  $scope.currentCommentOriginalText = "";
  $scope.currentCommentId = $routeParams.commentId || $location.search().num;


  $scope.canEditDelete = function (user) {
    return (user === Auth.getUserId()) && Auth.isLoggedIn();
  };

  $scope.getUserDisplayName = function () {
    var name = '';
    if ($scope.comment['@isNaraStaff'] === "true") {
      name = $scope.comment['@fullName'] ? $scope.comment['@fullName'] : $scope.comment['@user'];
      name += " (NARA Staff)";
    }
    else if ($scope.comment['@displayFullName'] === "true" && $scope.comment['@fullName']) {
      name = $scope.comment['@fullName'];
    }
    else {
      name = $scope.comment['@user'];
    }

    return name;
  };

  $scope.getUserUrl = function () {
    return '#/accounts/' + $scope.comment["@user"] + '/contributions?contributionType=comments';
  };

  $scope.getPersistentURL = function () {
    if (visorSvc.isWorkspace) {
      return "/id/" + $scope.naId + "/" + (visorSvc.index + 1) + "/public?contributionType=comment&num=" + $scope.comment['@id'];
    }
    else {
      return "/id/" + $scope.naId + "/comment/" + $scope.comment['@id'];
    }
  };


  var removeComment = function () {
    if ($scope.$parent.isWorkspace) {
      commentsService.deleteObjectComment($scope.naId, $scope.$parent.$parent.currentObjectId, $scope.comment['@id'], function (response, error) {
          if (!error) {
            $scope.comment = response.opaResponse.comment;
            commentsService.workspaceCommentsCount = parseInt(commentsService.workspaceCommentsCount) - 1;
            $scope.clickedRemove = false;
          } else {
            if (!OpaUtils.checkForAPIError(error)) {
              //check for other errors block
              OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
            }
          }
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(response)) {
            OpaUtils.showMessageModal("Alert!", response.data.opaResponse.error.description);
          }
        });
    }
    else {
      commentsService.removeComment($scope.naId, $scope.comment['@id'], function (response, error) {
        if (!error) {
          $scope.comment = response.opaResponse.comment;
          commentsService.commentsCount = parseInt(commentsService.commentsCount) - 1;
          $scope.clickedRemove = false;
        } else {
          if (!OpaUtils.checkForAPIError(response)) {
            //check for other errors block
            OpaUtils.showMessageModal("Alert", response.data.opaResponse.error.description);
          }
        }
      });
    }
  };

  $scope.editComment = function () {
    $scope.currentCommentOriginalText = $scope.comment['@text'];
    $scope.editMode = true;
    $scope.$parent.$parent.editMode = true;
  };

  $scope.cancelEdit = function () {
    $scope.comment['@text'] = $scope.currentCommentOriginalText;
    $scope.editMode = false;
    $scope.$parent.$parent.editMode = false;
  };

  $scope.edit = function () {
    if ($scope.isReply) {
      updateCommentReply();
    } else {
      updateComment();
    }
  };

  $scope.remove = function () {
    $scope.clickedRemove = true;
  };

  $scope.cancelRemove = function () {
    $scope.clickedRemove = false;
  };

  $scope.confirmRemove = function () {
    if ($scope.isReply) {
      deleteCommentReply();
    } else {
      removeComment();
    }
  };

  var updateComment = function () {

    if ($scope.comment['@text']) {

      if ($scope.$parent.isWorkspace) {
        commentsService.updateObjectComment($scope.naId, $scope.$parent.$parent.currentObjectId, $scope.comment['@id'], visorSvc.index + 1, $scope.comment['@text'], function (response, error) {
            if (!error) {
              $scope.comment = response;
              $scope.editMode = false;
              $scope.$parent.$parent.editMode = false;
              if(!OpaUtils.isMobileDevice()) {
                $("#t" + $scope.comment['@id']).data("kendoTooltip").options.content = $scope.comment.tooltipContent;
                $("#t" + $scope.comment['@id']).data("kendoTooltip").refresh();
              }
            } else {
              if (!OpaUtils.checkForAPIError(response)) {
                //check for other errors block
                OpaUtils.showMessageModal("Alert", response.data.opaResponse.error.description);
              }
            }
          },
          function (error) {
            if (!OpaUtils.checkForAPIError(error)) {
              OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
            }
          });
      }
      else {
        commentsService.updateComment($scope.naId, $scope.comment, function (response, error) {
            if (!error) {
              $scope.comment = response;
              $scope.editMode = false;
              $scope.$parent.$parent.editMode = false;
              if(!OpaUtils.isMobileDevice()) {
                $("#t" + $scope.comment['@id']).data("kendoTooltip").options.content = $scope.comment.tooltipContent;
                $("#t" + $scope.comment['@id']).data("kendoTooltip").refresh();
              }

            } else {
              if (!OpaUtils.checkForAPIError(response)) {
                //check for other errors block
                OpaUtils.showMessageModal("Alert", response.data.opaResponse.error.description);
              }
            }
          },
          function (error) {
            if (!OpaUtils.checkForAPIError(error)) {
              OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
            }
          });
      }
    }
    else {
      OpaUtils.showErrorGlobalNotification("Please enter a valid comment");
    }
  };

  $scope.addNewReply = function () {
    $scope.addReply = true;
    if ($scope.comment.hide) {
      $scope.comment.hide = false;
    }
    $timeout(function () {
      document.getElementById("replyInput").focus();
    }, 0, false);
  };

  $scope.cancelReply = function () {
    $scope.addReply = false;
    $scope.reply.text = "";
  };


  $scope.addCommentReply = function () {

    if ($scope.reply.text) {
      if ($scope.$parent.isWorkspace) {
        commentsService.addCommentObjectReply($scope.naId, $scope.$parent.$parent.currentObjectId, $scope.comment['@id'], visorSvc.index + 1, $scope.reply.text, function (response, error) {
            if (!error) {
              $scope.addReply = false;
              $scope.comment.replies.push(response);
              commentsService.workspaceCommentsCount = parseInt(commentsService.workspaceCommentsCount) + 1;
              $scope.reply.text = "";
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
      else {
        commentsService.addCommentReply($scope.naId, $scope.comment['@id'], $scope.reply.text, function (response, error) {
            if (!error) {
              $scope.addReply = false;
              $scope.comment.replies.push(response);
              commentsService.commentsCount = parseInt(commentsService.commentsCount) + 1;
              $scope.reply.text = "";

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
    }
    else {
      OpaUtils.showErrorGlobalNotification("Please enter a valid reply");
    }
  };

  var updateCommentReply = function () {

    if ($scope.comment['@text']) {

      if ($scope.$parent.isWorkspace) {
        commentsService.updateCommentObjectReply($scope.naId, $scope.parentId, $scope.$parent.$parent.currentObjectId, visorSvc.index + 1, $scope.comment, function (response, error) {
            if (!error) {
              $scope.editMode = false;
              $scope.comment = $(response.replies).get(-1);
              if(!OpaUtils.isMobileDevice()) {
                $("#t" + $scope.comment['@id']).data("kendoTooltip").options.content = $scope.comment.tooltipContent;
                $("#t" + $scope.comment['@id']).data("kendoTooltip").refresh();
              }
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
      else {
        commentsService.updateCommentReply($scope.naId, $scope.parentId, $scope.comment, function (response, error) {
            if (!error) {
              $scope.editMode = false;
              $scope.comment = $(response.replies).get(-1);
              if(!OpaUtils.isMobileDevice()) {
                $("#t" + $scope.comment['@id']).data("kendoTooltip").options.content = $scope.comment.tooltipContent;
                $("#t" + $scope.comment['@id']).data("kendoTooltip").refresh();
              }

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
    }
    else {
      OpaUtils.showErrorGlobalNotification("Please enter a valid reply");
    }
  };

  var deleteCommentReply = function () {

    if ($scope.$parent.isWorkspace) {
      commentsService.deleteCommentObjectReply($scope.naId, $scope.$parent.$parent.currentObjectId, $scope.parentId, visorSvc.index + 1, $scope.comment, function (response, error) {
          if (!error) {
            $scope.comment = $scope.getReplyFromReplyList($scope.comment['@id'], response.opaResponse.comment.replies);
            $scope.updateReplyToReplyList($scope.$parent.comment, $scope.comment, $scope.comment['@id']);
            commentsService.commentsCount = parseInt(commentsService.commentsCount) - 1;
            $scope.clickedRemove = false;
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
    else {
      commentsService.deleteCommentReply($scope.naId, $scope.parentId, $scope.comment['@id'], function (response, error) {
        if (!error) {
          if ($scope.isReply) {
            $scope.comment = $scope.getReplyFromReplyList($scope.comment['@id'], response.opaResponse.comment.replies);
            $scope.updateReplyToReplyList($scope.$parent.comment, $scope.comment, $scope.comment['@id']);
          }
          else {
            $scope.comment = response.opaResponse.comment;
          }
          commentsService.commentsCount = parseInt(commentsService.commentsCount) - 1;
          $scope.clickedRemove = false;

        } else {
          if (!OpaUtils.checkForAPIError(error)) {
            //check for other errors block
            OpaUtils.showMessageModal("Alert", error.data.opaResponse.error.description);
          }
        }
      });
    }
  };


  //Find and return a reply in a list by ID and return it
  $scope.getReplyFromReplyList = function (idReply, replies) {
    for (var i = 0; i < replies.length; i++) {
      if (replies[i]['@id'] === idReply) {
        return replies[i];
      }
    }
  };


  //Update a reply into the comments replies
  $scope.updateReplyToReplyList = function (parentComment, reply, idReply) {
    for (var i = 0; i < parentComment.replies.length; i++) {
      if (parentComment.replies[i]['@id'] === idReply) {
        parentComment.replies[i] = reply;
        return;
      }
    }
  };

  $scope.showHideLink = function () {
    if ($scope.comment.hide) {
      return 'View all ' + $scope.comment.replies.length + ' Replies';
    }
    else {
      return 'Hide replies';
    }
  };

  $scope.showHideReplies = function () {
    $scope.comment.hide = !$scope.comment.hide;
  };

});
