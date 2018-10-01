<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:releaseDateArray">
        <stringList>
            <xsl:if test="*:proposableQualifiableDate">
                <value>
                 <xsl:for-each select="*:proposableQualifiableDate">
                     <xsl:if test="*:dateQualifier/termName"><xsl:value-of select="*:dateQualifier/termName"/><xsl:text> </xsl:text></xsl:if>
                     <xsl:value-of select="*:year"/>
                     <xsl:if test="*:month"><xsl:text>-</xsl:text><xsl:value-of select="*:month"/></xsl:if>
                     <xsl:if test="*:day"><xsl:text>-</xsl:text><xsl:value-of select="*:day"/></xsl:if>
                 </xsl:for-each>
                </value>
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>