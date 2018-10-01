<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template name="getDate">
        <xsl:param name="dateObject"/> <!-- dateQualifier => example ca. 1980? - ca. 1990? -->        
        <xsl:if test="$dateObject/*:dateQualifier/*:termName != '?'"><xsl:value-of select="$dateObject/*:dateQualifier/*:termName"/><xsl:text> </xsl:text></xsl:if>
        <xsl:if test="$dateObject/*:month"><xsl:value-of select="$dateObject/*:month"/>/</xsl:if>
        <xsl:if test="$dateObject/*:day"><xsl:value-of select="$dateObject/*:day"/>/</xsl:if>
        <xsl:if test="$dateObject/*:year"><xsl:value-of select="$dateObject/*:year"/></xsl:if>
        <xsl:if test="$dateObject/*:dateQualifier/*:termName = '?'"><xsl:value-of select="$dateObject/*:dateQualifier/*:termName"/></xsl:if>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>