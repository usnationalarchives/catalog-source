from StringIO import StringIO
from csv import DictReader
import logging
import cherrypy, json
from splunk.appserver.mrsparkle.lib.decorators import expose_page
import splunk.appserver.mrsparkle.controllers as controllers
from spp.java.bridge import executeBridgeCommand
from spp.decorators import require_app_access

logger = logging.getLogger("dbx.controllers.dbinfo")
logger.setLevel(logging.DEBUG)

class DBInfoController(controllers.BaseController):

	@expose_page(must_login=True)
	@require_app_access()
	def catalogs(self, type, host, **kwargs):
		# DBX-278, pass arguments too.
		params = ["type=%s" % type, "host=%s" % host] + ["%s=%s" % (n, v) for n, v in kwargs.items()]
		
		'''
        try:
            params = ["type=%s" % type, "host=%s" % host]
            if "port" in kwargs: params.append("port=%s" % kwargs["port"])
            if "username" in kwargs: params.append("username=%s" % kwargs["username"])
            if "password" in kwargs: params.append("password=%s" % kwargs["password"])
            if "arguments" in kwargs: params.append("arguments=%s" % kwargs["arguments"])
           '''
		try: 
			ret,out,err = executeBridgeCommand("com.splunk.dbx.info.DatabaseList", params, fetchOutput=True)
			if ret is not 0:
				logger.warn("Received return code %s from java process" % ret)
				return json.dumps(dict( success = False, error = out.strip() ))
			csv = DictReader( StringIO(out) )
			if 'ERROR' in csv.fieldnames:
				msg = "\n".join([ row['ERROR'] for row in csv ])
				raise Exception(msg)
			else:
				result = [ row['catalog'] for row in csv ]
				return json.dumps(dict( catalogs = result, success = True ))
		except Exception,e:
			if type(e) is cherrypy.HTTPError: raise e
			logger.error("Error [catalogs]: %s", e)
			return json.dumps(dict( success = False, error = str(e)))

	@expose_page(must_login=True)
	@require_app_access()
	def database_tables(self, database, includeViews = "1", **kwargs):
		try:
			ret,out,err = executeBridgeCommand("com.splunk.dbx.info.SchemaInfo", [ "type=tables", "db=%s" % database, "includeViews=%s"%includeViews ], fetchOutput=True)
			if ret is not 0:
				logger.warn("Received return code %s from java process" % ret)
				return err
			return out
		except Exception, e:
			if type(e) is cherrypy.HTTPError: raise e
			logger.error("Error [databaseTables]: %s", e)
			return json.dumps(dict( success = False, error = str(e) ))

	@expose_page(must_login=True)
	@require_app_access()
	def table_columns(self, database, table, **kwargs):
		try:
			ret,out,err = executeBridgeCommand("com.splunk.dbx.info.SchemaInfo", ["type=columns", "db=%s" % database, "table=%s" % table], fetchOutput=True)
			if ret is not 0:
				logger.warn("Received return code %s from java process" % ret)
				return err
			return out
		except Exception, e:
			if type(e) is cherrypy.HTTPError: raise e
			logger.error("Error [databaseTables]: %s", e)
			return json.dumps(dict( success = False, error = str(e) ))
