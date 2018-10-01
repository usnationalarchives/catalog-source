<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:physicalOccurrenceArray">
        <fieldList>
            <xsl:for-each select="*/mediaOccurrenceArray/mediaOccurrence">
                <field><name>copy<xsl:value-of select="position()"/>MediaInformation</name><label>Copy <xsl:value-of select="position()"/> Media Information</label>
                    <value>
                        <stringList>
                            <xsl:if test="*:specificMediaType/termName"><value>Specific Media Type: <xsl:value-of select="*:specificMediaType/termName"/></value></xsl:if>
                            <xsl:if test="*:color/termName"><value>Color: <xsl:value-of select="*:color/termName"/></value></xsl:if>
                            <xsl:if test="*:containerId/termName"><value>Container ID: <xsl:value-of select="*:containerId/termName"/></value></xsl:if>
                            <xsl:if test="*:dimension/termName"><value>Dimension: <xsl:value-of select="*:dimension/termName"/></value></xsl:if>
                            <xsl:if test="*:height/termName"><value>Height: <xsl:value-of select="*:height/termName"/></value></xsl:if>
                            <xsl:if test="*:depth/termName"><value>Width: <xsl:value-of select="*:depth/termName"/></value></xsl:if>
                            <xsl:if test="*:mediaOccurrenceNote/termName"><value>Media Occurrence Note: <xsl:value-of select="*:mediaOccurrenceNote/termName"/></value></xsl:if>
                            <xsl:if test="*:physicalRestrictionNote/termName"><value>Physical Restriction Note: <xsl:value-of select="*:physicalRestrictionNote/termName"/></value></xsl:if>
                            <xsl:if test="*:pieceCount/termName"><value>Piece Count: <xsl:value-of select="*:pieceCount/termName"/></value></xsl:if>
                            <xsl:if test="*:process/termName"><value>Process: <xsl:value-of select="*:process/termName"/></value></xsl:if>
                            <xsl:if test="*:reproductionCount/termName"><value>Reproduction Count: <xsl:value-of select="*:reproductionCount/termName"/></value></xsl:if>
                            <xsl:if test="*:technicalAccessNote/termName"><value>Technical Access Requirements Note: <xsl:value-of select="*:technicalAccessNote/termName"/></value></xsl:if>
                        </stringList>               
                    </value>
                </field>
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>