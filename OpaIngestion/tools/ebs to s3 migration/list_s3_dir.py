import sys
from boto.s3.connection import S3Connection
from boto.s3.connection import OrdinaryCallingFormat
from s3credentials import *

conn = S3Connection(get_access_key(), get_secret_key(), calling_format=OrdinaryCallingFormat())
bucket = conn.get_bucket(get_bucket_name())

dirPrefix = sys.argv[1]
keys = bucket.list(prefix=dirPrefix)

for key in keys:
	print(key.size, key.etag.strip('"'), key.name, sep=',')