<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:series | *:collection | *:recordGroup">

        <xsl:variable name="series">
            <xsl:choose>
                <xsl:when test="string(number(*:seriesCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="number(*:seriesCount)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="fileUnits">
            <xsl:choose>
                <xsl:when test="string(number(*:fileUnitCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="number(*:fileUnitCount)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="items">
            <xsl:choose>
                <xsl:when test="string(number(*:itemCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="number(*:itemCount)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="itemsAv">
            <xsl:choose>
                <xsl:when test="string(number(*:itemAvCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="number(*:itemAvCount)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="allItems" select="number($items) + number($itemsAv)"/>
        <stringList>
                <xsl:if test="$series > 0">
                    <value><xsl:value-of select="$series"/> series described in the catalog</value>
                </xsl:if>
                <xsl:if test="$fileUnits > 0">
                    <value><xsl:value-of select="$fileUnits"/> file unit(s) described in the catalog</value>
                </xsl:if>
                <xsl:if test="$allItems > 0">
                    <value><xsl:value-of select="$allItems"/> item(s) described in the catalog</value>
                </xsl:if>
        </stringList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>