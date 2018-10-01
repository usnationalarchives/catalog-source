opaApp.controller('visorCtrl', function ($scope, visorSvc, $window, $location, $log, $routeParams, OpaUtils) {

  /**
   * updates the image/video source
   */
  var updateMedia;
  /**
   * Variable to identify if the mouse is inside
   * the Visor
   * @type {boolean}
   */
  $scope.mouseIn = false;

  /**
   * Reference to the visorSvc in DOM
   */
  $scope.visorSvc = visorSvc;

  /**
   * Variale to show photo or video controler
   * @type {boolean}
   */
  $scope.isVideo = false;

  /**
   * Variale to show the PDF Viewer
   * @type {boolean}
   */
  $scope.isPdf = false;

  /**
   * Variable to know if this is a format not supported in mobile
   * @type {boolean}
   */
  $scope.notFormMobile = false;

  /**
   * route of the video file
   * @type {string}
   */
  $scope.videoSrc = '';

  /**
   * Variable to set the minimun zoom for an image
   * @type {number}
   */
  $scope.minSize = 0;


  /**
   * Indicates if the concatenated Designator and Description label must be displayed
   * @type {number}
   */
  $scope.showDesignatorDescription = true;

  /*
   * Check if we are running on a mobile device
   */
  $scope.mobile = false;
  if (OpaUtils.isMobileDevice()) {
    $scope.mobile = true;
  }

  updateMedia = function () {
    var win;
    var lastsource;
    var sources;
    var video;
    var html;
    var player;
    var version;
    var formatNotSupportedHTML;
    var source;
    var frm;
    var isIE8 = window.navigator.userAgent.indexOf('MSIE 8.0') > -1;

    $scope.isVideo = visorSvc.isVideo;
    $scope.isPdf = visorSvc.isPdf;
    $scope.isWmv = visorSvc.isWmv;
    $scope.isAudio = visorSvc.isAudio;
    formatNotSupportedHTML = '<div class="wmv-fallback"> <br /> <p>Unable to play video</p><img alt="Video format not supported" src="images/video.svg">';
    if (visorSvc.notSupported) {
      html = formatNotSupportedHTML;
      if (OpaUtils.isMobileDevice()) {
        html += '</div>';
      }
      else {
        html += '<p>Please download the video below</p></div>';
      }
      $('#' + $scope.playerId).html(html);
    }
    else if ($scope.isPdf) {
      frm = document.createElement('iframe');
      frm.src = 'scripts/assets/pdfjs/web/viewer.html?file=' + visorSvc.getMediaSource();
      frm.className = 'col-xs-12 pdf-viewer pdfIFrame';
      frm.setAttribute('allowfullscreen','');
      $('.pdfIFrame').replaceWith(frm);
    }
    else if ($scope.viewer && !$scope.isVideo && !$scope.isPdf && !$scope.isAudio) {
      source = visorSvc.getMediaSource();
      if (source) {
        $scope.viewer.close();
        $scope.viewer.open(source);
      }
    }
    else if ($scope.isAudio) {
      $scope.videoSrc = visorSvc.getMediaStreamSource();
      player = $('#' + $scope.playerId);
      if (isIE8) {
        player.html('<object type="application/x-shockwave-flash" data="http://flashfox.googlecode.com/svn/trunk/flashfox.swf" ' +
          'width="100%" height="100%"><param name="movie" value="http://flashfox.googlecode.com/svn/trunk/flashfox.swf"/>' +
          '<param name="allowFullScreen" value="true" /> <param name="wmode" value="transparent"/>'+
          '<param name="flashVars" value="controls=true&amp;src=' + $scope.videoSrc + '"/>' +
          '<img alt="Unable to play video" src="images/video.svg" width="100%" height="100%" ' +
          'title="No video playback capabilities, please download the video below" /></object>');
      } else {
        player.html('<audio controls width="100%" height="100%"> <source src="' + $scope.videoSrc + '" type="audio/mpeg" /></audio>');
      }
    } else {
      $scope.videoSrc = visorSvc.getMediaStreamSource();
      if ($scope.isWmv && !OpaUtils.isMobileDevice()) {
        visorSvc.currentWMV = $scope.playerId + 'c';
        version = PluginDetect.getVersion("WindowsMediaPlayer", true);
        if (version) {
          win = angular.element($window);
          win.bind("resize", function () {
            visorSvc.notifyObservers();
          });
          if (!visorSvc.isWorkspace || (visorSvc.isWorkspace && $scope.hideContributionsButton === 'true')) {
            player = $scope.playerId + '' + Math.floor((Math.random() * 100) + 1);
            $('#' + $scope.playerId).html('<object id="' + $scope.playerId +
              'c" width="100%" height="100%" classid="CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95" ' +
              'codebase="http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701" ' +
              'standby="Loading Microsoft Windows Media Player components..." type="application/x-oleobject">' +
              '<param name="fileName" value="' + $scope.videoSrc +
              '"> <param name="animationatStart" value="true"> <param name="transparentatStart" value="true">' +
              '<param name="autostart" value="false"> <param name="showcontrols" value="true">' +
              '<param name="ShowAudioControls" value="true"> <param name="showstatusbar" value="true">' +
              '<param name="loop" value="false"> <embed type="application/x-mplayer2"' +
              'pluginspage="http://microsoft.com/windows/mediaplayer/en/download/" displaysize="4" autosize="-1" ' +
              'showcontrols="true" showtracker="-1" showdisplay="0" showstatusbar="true" videoborder3d="-1" width="100%" height="100%" src="' +
              $scope.videoSrc + '" autostart="false" loop="false"> </embed> </object> <br/>');
          }
          if ($scope.hideContributionsButton === 'false') {
            visorSvc.wmvPlayers.push('#' + $scope.playerId + 'c');
          }
        } else {
          if (PluginDetect.browser.isGecko) {
            $('#' + $scope.playerId).html('<div class="wmv-fallback"> <br /><p>No plugin detected for WMV files</p><a target="_blank" href="http://www.interoperabilitybridges.com/windows-media-player-firefox-plugin-download/">Plugin for Mozilla Firefox</a></div>');
          }
          else if (PluginDetect.browser.isChrome) {
            $('#' + $scope.playerId).html('<div class="wmv-fallback"><br /><p>No plugin detected for WMV files</p><a target="_blank" href="http://www.interoperabilitybridges.com/wmp-extension-for-chrome/">Plugin for Google Chrome</a></div>');
          } else {
            html = formatNotSupportedHTML;
            if (OpaUtils.isMobileDevice()) {
              html += '</div>';
            }
            else {
              html += '<p>Please download the video below</p> </div>';
            }
            $('#' + $scope.playerId).html(html);
          }
        }
      }
      else {
        if (OpaUtils.isMobileDevice() && visorSvc.notFormMobile) {
          html = formatNotSupportedHTML;
          if (OpaUtils.isMobileDevice()) {
            html += '</div>';
          }
          else {
            html += '<p>Please download the video below</p> </div>';
          }
          $('#' + $scope.playerId).html(html);
        }
        else if (isIE8) {
          $('#' + $scope.playerId).html('<object type="application/x-shockwave-flash" data="http://flashfox.googlecode.com/svn/trunk/flashfox.swf" ' +
            'width="100%" height="100%"><param name="movie" value="http://flashfox.googlecode.com/svn/trunk/flashfox.swf"/>' +
            '<param name="allowFullScreen" value="true" /> <param name="wmode" value="transparent" />' +
            '<param name="flashVars" value="controls=true&amp;src=' + $scope.videoSrc + '" />' +
            '<img alt="Unable to play video" src="images/video.svg" width="100%" height="100%" ' +
            'title="No video playback capabilities, please download the video below" /> </object>');
        } else {
          $('#' + $scope.playerId).html('<video controls="true" width="100%" height="100%"><source id="src-' +
            $scope.playerId + '" src="' + $scope.videoSrc + '" type="video/mp4"></source>' +
            '<object type="application/x-shockwave-flash" data="http://flashfox.googlecode.com/svn/trunk/flashfox.swf" width="100%" height="100%">' +
            '<param name="movie" value="http://flashfox.googlecode.com/svn/trunk/flashfox.swf" />' +
            '<param name="allowFullScreen" value="true" /><param name="wmode" value="transparent" />' +
            '<param name="flashVars" value="controls=true&amp;src=' + $scope.videoSrc + '" />' +
            '<img alt="Unable to play video" src="images/video.svg" width="100%" height="100%" ' +
            'title="No video playback capabilities, please download the file below" /></object></video>');

          video = document.querySelector('video');
          if (video) {
            sources = video.querySelectorAll('source');
            lastsource = sources[sources.length - 1];

            lastsource.addEventListener('error', function (ev) {
              var elem = document.getElementById($scope.playerId);
              var d = document.createElement('div');
              d.innerHTML = '<p>Unable to play video</p><img alt="Unable to play video" src="images/video.svg" ' +
                'title="Unable to play video, please download the file below"/><p class="visible-md visible-lg">Please download the video below</p>';
              elem.replaceChild(d, elem.firstChild);
            }, false);
          }
        }
      }
    }
  };

  /**
   * updates the index for the navigation control
   */
  var updateIndex = function () {
    $scope.index = visorSvc.index + 1;
    $scope.total = visorSvc.total;
  };

  /**
   * registers the updateMedia method in the service
   */
  visorSvc.registerObserverCallback(updateMedia);

  /**
   * registers the update indexd method in the service
   */
  visorSvc.registerIndexObserverCallback(updateIndex);

  /**
   * gets the url for the PDF Viewer
   * @returns {string}
   */
  $scope.getFrameSrc = function () {
    if ($scope.isPdf) {
      visorSvc.currentThumbnailURL = $scope.videoSrc;
      return 'scripts/assets/pdfjs/web/viewer.html?file=' + $scope.videoSrc;
    } else {
      return '';
    }
  };

  /**
   * goes to the first media
   */
  $scope.firstMedia = function () {
    //This prevent to navigate to the tab type of the last contribution added
    visorSvc.orderTabsByDate = false;
    if (visorSvc.isWorkspace) {
      visorSvc.naId = $routeParams.naId;
      var newUrl = '/id/' + visorSvc.naId + '/1/public';
      $location.path(newUrl, false);
    }
    visorSvc.getMediaFromCtrl(0);
  };

  /**
   * goes to the last media
   */
  $scope.lastMedia = function () {
    //This prevent to navigate to the tab type of the last contribution added
    visorSvc.orderTabsByDate = false;
    if (visorSvc.isWorkspace) {
      visorSvc.naId = $routeParams.naId;
      var newUrl = '/id/' + visorSvc.naId + '/' + visorSvc.total + '/public';
      $location.path(newUrl, false);
    }
    visorSvc.getMediaFromCtrl(visorSvc.total - 1);
  };

  /**
   * goes to the next media
   */
  $scope.nextMedia = function () {
    //This prevent to navigate to the tab type of the last contribution added
    visorSvc.orderTabsByDate = false;
    if (visorSvc.index < (visorSvc.total - 1)) {
      if (visorSvc.isWorkspace) {
        visorSvc.naId = $routeParams.naId;
        var newUrl = '/id/' + visorSvc.naId + '/' + (visorSvc.index + 2) + '/public';
        $location.path(newUrl, false);
      }
      visorSvc.getMediaFromCtrl(visorSvc.index + 1);
    }
  };

  /**
   * goes to the previous media
   */
  $scope.previousMedia = function () {
    //This prevent to navigate to the tab type of the last contribution added
    visorSvc.orderTabsByDate = false;
    if (visorSvc.index > 0) {
      if (visorSvc.isWorkspace) {
        visorSvc.naId = $routeParams.naId;
        var newUrl = '/id/' + visorSvc.naId + '/' + visorSvc.index + '/public';
        $location.path(newUrl, false);
      }
      visorSvc.getMediaFromCtrl(visorSvc.index - 1);
    }
  };

  /**
   * goes to a specific media
   */
  $scope.goTo = function () {
    //This prevent to navigate to the tab type of the last contribution added
    visorSvc.orderTabsByDate = false;
    if ($scope.index - 1 >= 0 && $scope.index - 1 < visorSvc.total) {
      if (visorSvc.isWorkspace) {
        visorSvc.naId = $routeParams.naId;
        var newUrl = '/id/' + visorSvc.naId + '/' + $scope.index + '/public';
        $location.path(newUrl, false);
      }
      visorSvc.getMediaFromCtrl($scope.index - 1);
    }
    else {
      $scope.index = '';
    }
  };

  /**
   * waits for the visorId to be created in the directive to create de
   * openseaDragon control
   */
  $scope.$watch($scope.visorId, function () {
    var visor;
    if ($scope.visorId) {
      visor = OpenSeadragon({
        id: $scope.visorId,
        prefixUrl: 'scripts/assets/openseadragon/images/',
        homeButton: 'home',
        showNavigator: false,
        maxZoomLevel: "4",
        minZoomLevel: "0.5",
        crossOriginPolicy: "Anonymous"
      });
      $scope.viewer = visor;


      visor.addHandler('zoom', function () {
        var value;
        if ($scope.viewer.viewport) {
          value = $scope.viewer.viewport.getZoom();
          value = 100 * (value - $scope.minSize) / (4 - $scope.minSize);
          $("#" + $scope.sliderId).slider("value", value);
        }
      });

      visor.addHandler('open', function () {
        if ($scope.viewer.viewport) {
          $scope.minSize = $scope.viewer.viewport.getZoom();
          if ($scope.mobile) {
            $scope.viewer.viewport.zoomTo(1, $scope.viewer.viewport.getCenter(), true);
          }
        }
      });

      visor.addHandler("canvas-double-click", function (args) {
        // Since we are listening to the double click, we want to only deal with the 'double-tap'
        if (args.originalEvent.type === "touchend") {
          $scope.viewer.viewport.goHome(true);
        }
      });

      $("#" + $scope.sliderId).slider({
          min: 0,
          max: 100,
          value: 0,
          slide: function (event, ui) {
            var value = $scope.minSize * (1 - ui.value / 100) + 4 * (ui.value / 100);
            //$log.info("slider " + value);
            $scope.viewer.viewport.zoomTo(value, $scope.viewer.viewport.getCenter(), true);
          }
        }
      );
      $scope.pan = function (x, y) {
        var point = $scope.viewer.viewport.getCenter();
        point.x = x;
        point.y = y;
        $scope.viewer.viewport.panBy(point);
      };

      $scope.zoomIn = function () {
        var value;
        var sliderValue = $("#" + $scope.sliderId).slider("option", "value");
        sliderValue += 1.2;
        if (sliderValue > 100) {
          sliderValue = 100;
        }
        value = $scope.minSize * (1 - sliderValue / 100) + 4 * (sliderValue / 100);
        $scope.viewer.viewport.zoomTo(value, $scope.viewer.viewport.getCenter(), true);
      };

      $scope.zoomOut = function () {
        var value;
        var sliderValue = $("#" + $scope.sliderId).slider("option", "value");
        sliderValue -= 1.2;
        if (sliderValue < 0) {
          sliderValue = 0;
        }
        value = $scope.minSize * (1 - sliderValue / 100) + 4 * (sliderValue / 100);
        $scope.viewer.viewport.zoomTo(value, $scope.viewer.viewport.getCenter(), true);
      };

      $scope.home = function () {
        $scope.viewer.viewport.goHome(true);
      };
      updateMedia();
      updateIndex();
    }
  });

  $scope.getthumbnailURL = function () {
    return visorSvc.currentThumbnailURL;
  };

  $scope.getDesignatorAndDescriptionString = function () {
    var designatorDescriptionStr = "";
    if (visorSvc.currentObject !== null) {
      var designator = visorSvc.currentObject.designator;
      var description = visorSvc.currentObject.description;

      if (designator) {
        designatorDescriptionStr = designator;
      }
      if (description) {
        if (designatorDescriptionStr) {
          designatorDescriptionStr = designatorDescriptionStr + ", ";
        }
        designatorDescriptionStr = designatorDescriptionStr + description;
      }
    }

    if (designatorDescriptionStr) {
      $scope.showDesignatorDescription = true;
    }
    else {
      $scope.showDesignatorDescription = false;
    }

    return designatorDescriptionStr;
  };

  $scope.getShowDesignatorDescription = function () {
    return $scope.showDesignatorDescription;
  };

  $('#workspaceModal').on('hidden.bs.modal', function (e) {
    visorSvc.removeWMP();
    visorSvc.isWorkspace = false;
    visorSvc.notifyObservers();
  });

  $scope.$on('$routeChangeStart', function(next, current) {
    if(OpaUtils.isIE && visorSvc.currentWMV && document.getElementById(visorSvc.currentWMV)){
      document.getElementById(visorSvc.currentWMV).Stop();
      visorSvc.currentWMV = "";
    }
  });
});
