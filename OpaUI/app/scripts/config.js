var opaConfig = angular.module('opaAppConfig', []);

opaConfig.value('configServices', {
  'API_LOCATION': API_LOCATION,
  'API_END_POINT': API_LOCATION + '/OpaAPI/api/v1',
  'IAPI_END_POINT': API_LOCATION + '/OpaAPI/iapi/v1',
  'OPASTORAGE_URL': API_LOCATION + '/OpaAPI/media/',
  'TIMEOUT_TRANSCRIPTION': 1800000,
  'PUBLIC_TOP_RESULTS_LIMIT': 10000,
  'RESULTS_PER_PAGE': [20, 50, 75, 100],
  'PRINT_LIMIT': 4000,
  'MAX_CONTRIBUTION_ROWS': 20000,
  'REVISION': 'revNumberPH',
  'SVN_REVISION': '',
  'SVN': ''
});
