<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:physicalOccurrenceArray">
        <fieldList>
            <xsl:for-each select="*:seriesPhysicalOccurrence">
                <xsl:if test="*:copyStatus/*:termName">
                    <field><label>Copy <xsl:value-of select="position()"/></label><value><xsl:value-of select="*:copyStatus/*:termName"/></value><name>copy<xsl:value-of select="position()"/></name></field>
                </xsl:if>
                <xsl:if test="*:extent">
                    <field><label>Extent (Size)</label><value><xsl:value-of select="*:extent"/></value><name>extentSize</name></field>
                </xsl:if>
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>