<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="/">
        <xsl:element name="stringList">
            <xsl:apply-templates select="@*|node()"></xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <xsl:template match="*:personalContributorArray | *:organizationalContributorArray | *:geographicContributorArray | *:specificRecordsTypeArray | *:topicalSubjectArray">
        <xsl:for-each select="*">
            <xsl:if test="*:contributor">
                <value>
                    <xsl:value-of select="*:contributor/termName"/>
               <xsl:if test="*:contributorType">
                   <xsl:text>, </xsl:text>
                   <xsl:value-of select="*:contributorType/termName"/>
               </xsl:if>
                </value>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>        
    <xsl:template match="text()"/>
</xsl:stylesheet>