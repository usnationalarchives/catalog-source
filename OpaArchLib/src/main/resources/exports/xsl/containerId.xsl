<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:physicalOccurrenceArray">
        <stringList>
            <xsl:if test="*/*:mediaOccurrenceArray/*:mediaOccurrence[1]/*:containerId">
                <value><xsl:value-of select="*/*:mediaOccurrenceArray/*:mediaOccurrence[1]/*:containerId"></xsl:value-of></value>
            </xsl:if>
            <xsl:if test="*/*:mediaOccurrenceArray/*:itemMediaOccurrence[1]/*:containerId">
                <value><xsl:value-of select="*/*:mediaOccurrenceArray/*:itemMediaOccurrence[1]/*:containerId"></xsl:value-of></value>
            </xsl:if>
        </stringList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>