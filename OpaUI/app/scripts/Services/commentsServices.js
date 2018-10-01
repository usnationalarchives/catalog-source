opaApp.service('commentsService', function ($filter, CommentsService, CommentsObjectService, OpaUtils) {

  this.commentsCount = 0;
  this.workspaceCommentsCount = 0;

  var errors = {
    nodFound: 'COMMENT_NOT_FOUND'
  };

  //COMMENTS

  this.getComments = function (naId, callback) {
    CommentsService.getComments({naId: naId}, function (response) {
        var comments = {};
        if (response.opaResponse.header['@status'] === '200') {
          comments = response.opaResponse.comments;
          setEditionFlagAndTooltipContent(comments.comment,comments["@commentsFormat"]);
        }
        callback(comments, false);
      },
      function (error) {
        error = error.data;
        if (error.opaResponse.error['@code'] === errors.nodFound) {
          callback([], false);
        } else {
          callback([], true);
        }
      });
  };

  this.addComment = function (naId, text, callback) {
    if (!text) {
      //This is an error
    } else {

      var commentService = new  CommentsService({naId: naId, text: $filter('removeWordCharacters')(text.trim())});
      commentService.$addComment({}, function (response, header) {
          callback(response, false);
        },
        function (error) {
          if (!OpaUtils.checkForAPIError(error)) {
            OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
          }
        });
    }
  };

  this.removeComment = function (naId, commentId, callback) {
    CommentsService.deleteComment({naId: naId, commentId: commentId}, function (response, header) {
      callback(response, false);
    }, function (error) {
      callback(error, true);
    });
  };

  this.updateComment = function (naId, comment, callback) {
    var commentsService = new CommentsService({naId: naId, commentId: comment['@id'], text: $filter('removeWordCharacters')(comment['@text'].trim())});
    commentsService.$updateComment({}, function (response, header) {
      comment = response.opaResponse.comment;
      setEditionFlagAndTooltipContent([comment]);
      callback(comment, false);
    }, function (error) {
      callback(error, true);
    });
  };

  // REPLIES

  this.addCommentReply = function (naId, commentId, reply, callback) {
    var commentsService = new CommentsService({naId: naId, commentId: commentId, text: $filter('removeWordCharacters')(reply.trim())});
    commentsService.$addCommentReply({}, function (response, header) {
        var comment = {};
        if (response.opaResponse.header['@status'] === '200') {
          comment = response.opaResponse.comment;
          setEditionFlagAndTooltipContent([comment]);
        }
        callback(comment, false);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
        }
      });
  };

  this.updateCommentReply = function (naId, commentId, reply, callback) {
    var commentService = new CommentsService({ naId: naId, commentId: commentId, replyId: reply['@id'], text: $filter('removeWordCharacters')(reply['@text'].trim())});
    commentService.$updateCommentReply({}, function (response, header) {
        var comment = {};
        if (response.opaResponse.header['@status'] === '200') {
          comment = response.opaResponse.comment;
          setEditionFlagAndTooltipContent([comment]);
        }
        callback(comment, false);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
        }
      });
  };

  this.deleteCommentReply = function (naId, commentId, replyId, callback) {
    CommentsService.deleteCommentReply({
      naId: naId,
      commentId: commentId,
      replyId: replyId
    }, {}, function (response, header) {
      callback(response, false);
    }, function (error) {
      callback({}, true);
    });
  };


  // OBJECT COMMENTS
  this.addObjectComment = function (naid, objectid, text, pageNum) {
    var comment = new CommentsObjectService({'naid': naid, 'objectId': objectid, 'text': $filter('removeWordCharacters')(text.trim())});
    var params = { pageNum: pageNum};
    var comments;

    comments = comment.$add(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
          return error;
        }
      }
    );
    return comments;
  };

  this.getCommentsForObject = function (naid, objectid) {
    var comment = new CommentsObjectService({'naid': naid, 'objectId': objectid});
    var call = comment.$get({},
      function (response){
        var comments = response.opaResponse.comments;
        setEditionFlagAndTooltipContent(comments.comment,comments["@commentsFormat"]);
        return comments;
      },
      function (error){
        return error;
      }
    );
    return call;
  };

  this.updateObjectComment = function (naid, objectid,commentId,pageNum,text,callback) {
    var comment = new CommentsObjectService({'naid': naid, 'objectId': objectid, 'commentId':commentId, 'text': $filter('removeWordCharacters')(text.trim())});
    var params = { pageNum: pageNum};

    comment.$update(params, function (response, header) {
      var comment = response.opaResponse.comment;
      setEditionFlagAndTooltipContent([comment]);
      callback(comment, false);
    }, function (error) {
      callback(error, true);
    });
  };


  this.deleteObjectComment = function (naId,objectid, commentId,callback){
    var comment = new CommentsObjectService({'naid': naId, 'objectId': objectid,commentId: commentId});
    comment.$delete({}, function (response, header) {
      callback(response, false);
    }, function (error) {
      callback(error, true);
    });
  };

  //OBJECT COMMENT REPLIES

  this.addCommentObjectReply = function (naId, objectId, commentId,pageNum, reply, callback) {
    var commentObject = new CommentsObjectService({'naid': naId, 'objectId': objectId,'commentId': commentId, text: $filter('removeWordCharacters')(reply.trim())});
    commentObject.$add({'pageNum':pageNum}, function (response, header) {
        var comment = {};
        if (response.opaResponse.header['@status'] === '200') {
          comment = response.opaResponse.comment;
          setEditionFlagAndTooltipContent([comment]);
        }
        callback(comment, false);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
        }
      });
  };

  this.updateCommentObjectReply = function (naId,commentId, objectId ,pageNum, reply, callback) {
    var commentObject = new CommentsObjectService({'naid': naId, 'objectId': objectId,'commentId': commentId,'replyId': reply['@id'], text: $filter('removeWordCharacters')(reply['@text'].trim())});
    commentObject.$update({'pageNum':pageNum}, function (response, header) {
        var comment = {};
        if (response.opaResponse.header['@status'] === '200') {
          comment = response.opaResponse.comment;
          setEditionFlagAndTooltipContent([comment]);
        }
        callback(comment, false);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
        }
      });
  };

  this.deleteCommentObjectReply = function (naId, objectId, commentId,pageNum, reply, callback) {
    var commentObject = new CommentsObjectService({'naid': naId, 'objectId': objectId,'commentId': commentId,'replyId': reply['@id']});
    commentObject.$delete({}, function (response, header) {
        callback(response, false);
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          OpaUtils.showMessageModal("Alert!", error.data.opaResponse.error.description);
        }
      });
  };



  /**
   * This function check if the comment was edited and enable a flag to each comment and reply edited.
   * Also creates the tooltip content for each comment and its replies.
   * @param comments The object contain the comments
   */
  var setEditionFlagAndTooltipContent = function(comments,format){
    var i = 0;
    for(i; i < comments.length; i++){
      var ri = 0;
      //Set collapse or uncollapse comments from format value
      if(format){
        if(format === 'collapsed'){
          comments[i].hide = true;
        }
        else if(format === 'expanded'){
          comments[i].hide = false;
        }
      }

      if(moment(comments[i]['@lastModified']).isAfter(comments[i]['@created'])){
        comments[i].edited = true;
      }

      var date = OpaUtils.fancyDateCompleteFormat( comments[i]['@created']);
      if (comments[i].edited) {
        date = "Edited " + OpaUtils.fancyDate( comments[i]['@lastModified']);
      }
      comments[i].tooltipContent = "<div><span>" + date + "</span></div>";

      if(comments[i].replies) {
        for (ri; ri < comments[i].replies.length; ri++) {
          if (moment(comments[i].replies[ri]['@lastModified']).isAfter(comments[i].replies[ri]['@created'])) {
            comments[i].replies[ri].edited = true;
          }
          var date = OpaUtils.fancyDateCompleteFormat(comments[i].replies[ri]['@created']);
          if (comments[i].replies[ri].edited) {
            date = "Edited " + OpaUtils.fancyDate(comments[i].replies[ri]['@lastModified']);
          }
          comments[i].replies[ri].tooltipContent = "<div><span>" + date + "</span></div>";
        }
      }
    }
  };



});
