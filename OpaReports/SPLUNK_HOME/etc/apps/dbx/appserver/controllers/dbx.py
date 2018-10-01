import logging
from spp.java.bridge import executeBridgeCommand
from spp.decorators import require_app_access
from splunk.appserver.mrsparkle.lib.decorators import expose_page

import splunk.appserver.mrsparkle.controllers as controllers
import splunk.appserver.mrsparkle.lib.util as util

logger = logging.getLogger("dbx.controllers.dbx")
logger.setLevel(logging.DEBUG)

class DBXController(controllers.BaseController):
	
	@expose_page(must_login=True)
	@require_app_access()
	def status(self,*args,**kwargs):
		ret, out, err = executeBridgeCommand("com.splunk.bridge.stats.SystemStatus", [], fetchOutput=True)
		return out