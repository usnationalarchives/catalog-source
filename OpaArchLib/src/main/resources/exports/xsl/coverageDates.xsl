<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:coverageDates">
        <xsl:if test="./*">
            <value>
             <xsl:if test="*:coverageStartDate/dateQualifier/termName"><xsl:if test="*:coverageStartDate/dateQualifier/termName!='?'"><xsl:value-of select="*:coverageStartDate/dateQualifier/termName"/><xsl:text> </xsl:text></xsl:if></xsl:if>   
             <xsl:if test="*:coverageStartDate/*:month"><xsl:value-of select="*:coverageStartDate/*:month"/>/</xsl:if>
             <xsl:if test="*:coverageStartDate/*:day"><xsl:value-of select="*:coverageStartDate/*:day"/>/</xsl:if>
             <xsl:if test="*:coverageStartDate/*:year"><xsl:value-of select="*:coverageStartDate/*:year"/></xsl:if>
             <xsl:if test="*:coverageStartDate/dateQualifier/termName='?'"><xsl:value-of select="*:coverageStartDate/dateQualifier/termName"/></xsl:if>                                        
             <xsl:text> - </xsl:text>
             <xsl:if test="*:coverageEndDate/dateQualifier/termName"><xsl:if test="*:coverageEndDate/dateQualifier/termName!='?'"><xsl:value-of select="*:coverageEndDate/dateQualifier/termName"/><xsl:text> </xsl:text></xsl:if></xsl:if>   
             <xsl:if test="*:coverageEndDate/*:month"><xsl:value-of select="*:coverageEndDate/*:month"/>/</xsl:if>
             <xsl:if test="*:coverageEndDate/*:day"><xsl:value-of select="*:coverageEndDate/*:day"/>/</xsl:if>
             <xsl:if test="*:coverageEndDate/*:year"><xsl:value-of select="*:coverageEndDate/*:year"/></xsl:if>
             <xsl:if test="*:coverageEndDate/dateQualifier/termName='?'"><xsl:value-of select="*:coverageEndDate/dateQualifier/termName"/></xsl:if>                                     
             </value>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>