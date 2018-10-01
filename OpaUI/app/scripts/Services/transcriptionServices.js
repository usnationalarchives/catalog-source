opaApp.service("TranscriptionService", function (Transcription, ModeratorTranscriptions, OpaUtils) {

  //This function returns a transcription given the naid and the objectId
  this.getTranscription = function (naid, objectid) {
    var transcription = new Transcription({'naid': naid, 'objectId': objectid});
    var params = {};
    var tr;

    tr = transcription.$get(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
        return error;
      }
    );

    return tr;
  };


  //This function returns a specific transcription version given the naid , the objectId and the version requested
  this.getTranscriptionVersion = function (naid, objectid, version) {
    var transcription = new ModeratorTranscriptions({'naid': naid, 'objId': objectid});
    var params = {'version': version};
    var tr;

    tr = transcription.$getVersion(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
        return error;
      }
    );
    return tr;
  };

  //This function returns all the transcription versions given the naid and the objectId
  this.getTranscriptionAllVersions = function (naid, objectid) {
    var transcription = new ModeratorTranscriptions({'naid': naid, 'objId': objectid});
    var params = {'version': 'all'};
    var tr;

    tr = transcription.$getVersion(params,
      function (data) {
        return data;
      },
      function (error) {
        if (!OpaUtils.checkForAPIError(error)) {
          //check for other errors block
        }
        return error;
      }
    );
    return tr;
  };


});
