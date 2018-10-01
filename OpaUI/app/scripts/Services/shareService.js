opaApp.service("shareSvc", function ($location, $rootScope) {

  this.configureAddThis = function (element) {
    var url = $location.protocol() + '://' + $location.host();
    if (!(($location.port() === 80 && $location.protocol() === 'http') || ($location.port() === 443 && $location.protocol() === 'https'))) {
      url += ':' + $location.port();
    }
    url += $location.url();
    addthis_share.url = url;
    addthis_share.title = 'The National Archives Catalog';

    addthis_share.description = '&#8203;';

    if (!PluginDetect.browser.verIE || PluginDetect.browser.verIE > 8) {
      addthis_config.ui_click = 'true';
    }
    addthis_config.services_compact = "facebook,twitter,link,googleplus,google,gmail,favorites,more";
    addthis_config.ui_show_promo = false;
    addthis_config.data_track_addressbar = false;
    addthis_config.data_track_clickback = false;

    addthis.init();
    // Ajax load (bind events)
    // http://support.addthis.com/customer/portal/articles/381263-addthis-client-api#rendering-js-toolbox
    // http://support.addthis.com/customer/portal/questions/548551-help-on-call-back-using-ajax-i-lose-share-buttons
    addthis.button($(element).get());
  };
});
