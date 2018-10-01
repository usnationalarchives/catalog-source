import sys
import tempfile
from boto.s3.connection import S3Connection
from boto.s3.connection import OrdinaryCallingFormat
from boto.s3.key import Key
import hashlib
from s3credentials import *


def getSizeChecksumAndPath(line):
	indexOfFirstComma = line.find(',')
	size = line[:indexOfFirstComma]

	indexOfSecondComma = line.find(',', indexOfFirstComma+1)
	checksum = line[indexOfFirstComma+1 : indexOfSecondComma]

	key = line[indexOfSecondComma+1:].rstrip()

	return (size, checksum, key)

def loadListing(filename, dictionary):
	with open(filename) as f:
		for line in f:
			size, checksum, key = getSizeChecksumAndPath(line)
			dictionary[key] = (size, checksum)

def md5(key, blocksize=128):
    m = hashlib.md5()
    while True:
        buf = key.read(blocksize)
        if not buf:
            break
        m.update( buf )
    return m.hexdigest()
	

ebsfilename = sys.argv[1]
s3filename = sys.argv[2]

ebschecksums = {}
loadListing(ebsfilename, ebschecksums)

s3checksums = {}
loadListing(s3filename, s3checksums)

conn = S3Connection(get_access_key(), get_secret_key(), calling_format=OrdinaryCallingFormat())
bucket = conn.get_bucket(get_bucket_name())

for path, (s3Size, s3Checksum) in s3checksums.items():
	ebsSize, ebsChecksum = ebschecksums[path]

	if path not in ebschecksums:
		print("S3 file not found in EBS: ", path)
	elif s3Checksum == ebsChecksum:
		print("equal", path, sep=',')
	else:
		key = Key(bucket, path)
		key.open()
		calculatedChecksum = md5(key)
		key.close()
		if ebsChecksum == calculatedChecksum:
			print("equal", path, sep=',')
		else:
			print("different", ebsSize, s3Size, ebsChecksum, calculatedChecksum, path, sep=',')

for key in ebschecksums:
	if key not in s3checksums:
		print("EBS file not found in S3: ", key)
