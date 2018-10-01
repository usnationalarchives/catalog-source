<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:productionDateArray">
        <stringList>
            <xsl:if test="*:proposableQualifiableDate">
                <xsl:for-each select="*:proposableQualifiableDate">
                   <value>
				       <xsl:if test="*:dateQualifier/termName"><xsl:value-of select="*:dateQualifier/termName"/><xsl:text> </xsl:text></xsl:if>
					   <xsl:if test="*:month"><xsl:value-of select="*:month"/><xsl:text>/</xsl:text></xsl:if>
				       <xsl:if test="*:day"><xsl:value-of select="*:day"/><xsl:text>/</xsl:text></xsl:if>
                       <xsl:value-of select="*:year"/>
                    </value>        
                </xsl:for-each>
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>