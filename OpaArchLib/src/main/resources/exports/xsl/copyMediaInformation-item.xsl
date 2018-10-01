<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:physicalOccurrenceArray">
        <fieldList>
            <xsl:for-each select="*/mediaOccurrenceArray/*">
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
                            <xsl:if test="*:emulsion/termName"><value>Emulsion: <xsl:value-of select="*:emulsion/termName"/></value></xsl:if>
                            <xsl:if test="*:footage/termName"><value>Footage: <xsl:value-of select="*:footage/termName"/></value></xsl:if>
                            <xsl:if test="*:format/termName"><value>Format: <xsl:value-of select="*:format/termName"/></value></xsl:if>
                            <xsl:if test="*:recordingSpeed/termName"><value>Record Speed: <xsl:value-of select="*:recordingSpeed/termName"/></value></xsl:if>
                            <xsl:if test="*:reelTapeDiscNumber/termName"><value>Reel/Tape/Disc Number: <xsl:value-of select="*:reelTapeDiscNumber/termName"/></value></xsl:if>
                            <xsl:if test="*:elementNumber/termName"><value>Element Number: <xsl:value-of select="*:elementNumber/termName"/></value></xsl:if>
                            <xsl:if test="*:rollType/termName"><value>Roll: <xsl:value-of select="*:rollType/termName"/></value></xsl:if>
                            <xsl:if test="*:runningTime/termName"><value>Running Time: <xsl:value-of select="*:runningTime/termName"/></value></xsl:if>
                            <xsl:if test="*:soundtrackConfig/termName"><value>Soundtrack Configuration: <xsl:value-of select="*:soundtrackConfig/termName"/></value></xsl:if>
                            <xsl:if test="*:soundtrackLang/termName"><value>Soundtrack Language: <xsl:value-of select="*:soundtrackLang/termName"/></value></xsl:if>
                            <xsl:if test="*:tapeThickness/termName"><value>Tape Thickness: <xsl:value-of select="*:tapeThickness/termName"/></value></xsl:if>
                            <xsl:if test="*:wind/termName"><value>Wind: <xsl:value-of select="*:wind/termName"/></value></xsl:if>
                        </stringList>               
                    </value>
                </field>
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>