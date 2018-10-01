import sys
import urllib.parse
from boto.s3.connection import S3Connection
from boto.s3.connection import OrdinaryCallingFormat
from boto.s3.key import Key
import hashlib
from s3credentials import *

def getS3Key(filename):
	return urllib.parse.quote(filename)

def md5(filename, blocksize=128):
    m = hashlib.md5()
    with open(filename, "rb") as f:
        while True:
            buf = f.read(blocksize)
            if not buf:
                break
            m.update( buf )
    return m.hexdigest()	

filename = sys.argv[1]
acl = sys.argv[2] if len(sys.argv) > 2 else None

conn = S3Connection(get_access_key(), get_secret_key(), calling_format=OrdinaryCallingFormat())
bucket = conn.get_bucket(get_bucket_name())

with open(filename) as f:
	for line in f:
		ebsfilename = line.rstrip()
	
		print("uploading file: ", ebsfilename)
		print("checksum: ", md5(ebsfilename))

		s3Key = getS3Key(ebsfilename)		
		key = Key(bucket, s3Key)
		with open(ebsfilename, 'rb') as ebsfile:
			key.set_contents_from_file(ebsfile)

		if acl is not None:
			key.set_canned_acl(acl)

		print("s3 key:", s3Key)

		resultSet = bucket.list(prefix=s3Key)
		for k in resultSet:
			print("s3 checksum:", k.etag.strip('"'))

		print("-------------------------------")

