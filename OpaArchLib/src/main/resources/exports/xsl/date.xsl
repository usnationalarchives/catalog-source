<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:inclusiveDates">
        <xsl:if test="*:inclusiveEndDate | *:inclusiveStartDate">
             <value>
             <xsl:if test="*:inclusiveStartDate/dateQualifier/termName"><xsl:if test="*:inclusiveStartDate/dateQualifier/termName!='?'"><xsl:value-of select="*:inclusiveStartDate/dateQualifier/termName"/><xsl:text> </xsl:text></xsl:if></xsl:if>
             <xsl:if test="*:inclusiveStartDate/*:month"><xsl:value-of select="*:inclusiveStartDate/*:month"/>/</xsl:if>
             <xsl:if test="*:inclusiveStartDate/*:day"><xsl:value-of select="*:inclusiveStartDate/*:day"/>/</xsl:if>
             <xsl:if test="*:inclusiveStartDate/*:year"><xsl:value-of select="*:inclusiveStartDate/*:year"/></xsl:if>
             <xsl:if test="*:inclusiveStartDate/dateQualifier/termName='?'"><xsl:value-of select="*:inclusiveStartDate/dateQualifier/termName"/></xsl:if>                 
             <xsl:text> - </xsl:text>
             <xsl:if test="*:inclusiveEndDate/dateQualifier/termName"><xsl:if test="*:inclusiveEndDate/dateQualifier/termName!='?'"><xsl:value-of select="*:inclusiveEndDate/dateQualifier/termName"/><xsl:text> </xsl:text></xsl:if></xsl:if>
             <xsl:if test="*:inclusiveEndDate/*:month"><xsl:value-of select="*:inclusiveEndDate/*:month"/>/</xsl:if>
             <xsl:if test="*:inclusiveEndDate/*:day"><xsl:value-of select="*:inclusiveEndDate/*:day"/>/</xsl:if>
             <xsl:if test="*:inclusiveEndDate/*:year"><xsl:value-of select="*:inclusiveEndDate/*:year"/></xsl:if>
             <xsl:if test="*:inclusiveEndDate/dateQualifier/termName='?'"><xsl:value-of select="*:inclusiveEndDate/dateQualifier/termName"/></xsl:if>                     
             </value>
        </xsl:if>
    </xsl:template>
    
    
    <xsl:template match="text()"/>
</xsl:stylesheet>