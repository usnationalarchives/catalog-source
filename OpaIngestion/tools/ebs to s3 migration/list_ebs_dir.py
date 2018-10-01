import os
import sys
import hashlib

def md5(filename, blocksize=128):
    m = hashlib.md5()
    with open(filename, "rb") as f:
        while True:
            buf = f.read(blocksize)
            if not buf:
                break
            m.update( buf )
    return m.hexdigest()	

baseDirLen = len("/opt/vol/")
dirPath = sys.argv[1]

for (dirPath, dirs, files) in os.walk(dirPath):
	for filename in files:
		fullPath = os.path.join(dirPath, filename)
		relativePath = fullPath[baseDirLen:]
		digest = md5(fullPath)
		size = os.stat(fullPath).st_size		
		print(size, digest, relativePath, sep=',')