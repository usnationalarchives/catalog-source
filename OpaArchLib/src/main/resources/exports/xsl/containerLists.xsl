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

    <xsl:template match="/*/physicalOccurrenceArray/seriesPhysicalOccurrence">
        <xsl:if
                test="./*">
            <xsl:for-each
                    select="containerList">
                <value>
                    <xsl:value-of select="text()"/>
                </value>
            </xsl:for-each>

        </xsl:if>
    </xsl:template>

    <xsl:template match="text()"/>
</xsl:stylesheet>