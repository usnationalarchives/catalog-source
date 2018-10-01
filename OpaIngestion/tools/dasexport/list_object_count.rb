require 'rubygems/package'
require 'zlib'
require 'tempfile'
require 'nokogiri'

$DESCRIPTION_TYPES = ['fileUnit', 'item', 'itemAv']

archive = ARGV[0]
outputFilename = ARGV[1]

def printObjectCount(element, output)
	if $DESCRIPTION_TYPES.include? element.name

		objects = element.xpath('digitalObjectArray/digitalObject')

		if objects.length > 0
			naId = element.xpath('naId').first.text
			output.puts "#{naId},#{objects.length}"
		end
	end
end

outputFile = open(outputFilename, "w")

Gem::Package::TarReader.new( Zlib::GzipReader.open archive ) do |tar|
	tar.each do |entry|
		if entry.file?
			puts entry.full_name
			
			extractedEntry = Tempfile.new('dasexportentry')
			extractedEntry.print entry.read			
			extractedEntry.flush
			extractedEntry.rewind

			doc = Nokogiri::XML(extractedEntry)
			doc.remove_namespaces!

			extractedEntry.close!

			root = doc.root
			if root.name == "das_items"
				root.element_children.each { |record| printObjectCount(record, outputFile) } 				
			else					
				printObjectCount(root, outputFile)
			end
		end
	end
end

outputFile.close