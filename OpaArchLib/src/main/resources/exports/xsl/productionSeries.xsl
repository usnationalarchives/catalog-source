<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*">
        <stringList>
            <xsl:if test="productionSeriesTitle">
                    <value>Title: <xsl:value-of select="productionSeriesTitle"/></value> 
            </xsl:if>
            <xsl:if test="productionSeriesSubtitle">
                <value>Subtitle: <xsl:value-of select="productionSeriesSubtitle"/></value> 
            </xsl:if>
            <xsl:if test="productionSeriesNumber">
                <value>Number: <xsl:value-of select="productionSeriesNumber"/></value> 
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>