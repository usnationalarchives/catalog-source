<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:physicalOccurrenceArray">
        <fieldList>
            <xsl:for-each select="fileUnitPhysicalOccurrence">
                <xsl:for-each select="copyStatus">
                    <field><name>copy<xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/></name><label>Copy <xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/></label>
                        <value>
                            <xsl:value-of select="termName"/>
                        </value>
                    </field>
                </xsl:for-each>   
                <xsl:for-each select="extent">
                    <field><name>extent<xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/></name><label>Extent (size)</label>
                        <value>
                            <xsl:value-of select="text()"/>
                        </value>
                    </field>
                </xsl:for-each>  
                <xsl:for-each select="note">
                    <field><name>note<xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/></name><label>Physical Occurrence Note</label>
                        <value>
                            <xsl:value-of select="text()"/>
                        </value>
                    </field>
                </xsl:for-each>
                <xsl:for-each select="referenceUnitArray/referenceUnit">    
                    <field><name>contacts<xsl:value-of select="count(parent::*/parent::*/preceding-sibling::*) + 1"/></name><label>Contact(s)</label>
                        <value>
                            <stringList>
                                <value><xsl:value-of select="*:termName"></xsl:value-of> <xsl:if test="*:mailCode">(<xsl:value-of select="*:mailCode"/>)</xsl:if></value>
                                <xsl:if test="*:address1"><value><xsl:value-of select="*:address1"/></value></xsl:if>
                                <xsl:if test="*:address2"><value><xsl:value-of select="*:address2"/></value></xsl:if>
                                <xsl:if test="*:city"><value><xsl:value-of select="*:city"/>, <xsl:value-of select="*:state"/>, <xsl:value-of select="*:postCode"/></value></xsl:if>
                                <xsl:if test="*:phone"><value>Phone: <xsl:value-of select="*:phone"/></value></xsl:if>
                                <xsl:if test="*:fax"><value>Fax: <xsl:value-of select="*:fax"/></value></xsl:if>
                                <xsl:if test="*:email"><value>Email: <xsl:value-of select="*:email"/></value></xsl:if></stringList>
                        </value>
                    </field>
                </xsl:for-each>
                <xsl:for-each select="totalFootage">
                    <field><name>totalFootage<xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/></name><label>Total Footage</label>
                        <value>
                            <xsl:value-of select="text()"/>
                        </value>
                    </field>
                </xsl:for-each>
                <xsl:for-each select="totalRunningTime">
                    <field><name>totalRunningTime<xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/></name><label>Total Running Time</label>
                        <value>
                            <xsl:value-of select="text()"/>
                        </value>
                    </field>
                </xsl:for-each>
                <xsl:for-each select="holdingsMeasurementArray">
                    <field>
                        <name>countHoldingsMeasurement</name><label>Count</label>
                        <value>
                            <stringList>
                                <xsl:call-template name="holdingMeasurement"/>
                            </stringList>               
                        </value>
                    </field>
                </xsl:for-each>  
                <xsl:for-each select="mediaOccurrenceArray">
                    <field><name>copy<xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/>MediaInformation</name><label>Copy <xsl:value-of select="count(parent::*/preceding-sibling::*) + 1"/> Media Information</label>
                        <value>
                            <stringList>
                                <xsl:call-template name="mediaOccurence"/>
                            </stringList>               
                        </value>
                    </field>
                </xsl:for-each>        
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    
    
    <xsl:template name="mediaOccurence">
        <xsl:for-each select="*">
            <xsl:if test="*:specificMediaType/termName"><value>Specific Media Type: <xsl:value-of select="*:specificMediaType/termName"/></value></xsl:if>
            <xsl:if test="*:color/termName"><value>Color: <xsl:value-of select="*:color/termName"/></value></xsl:if>
            <xsl:if test="*:containerId"><value>Container ID: <xsl:value-of select="*:containerId"/></value></xsl:if>
            <xsl:if test="*:dimension/termName"><value>Dimension: <xsl:value-of select="*:dimension/termName"/></value></xsl:if>
            <xsl:if test="*:height"><value>Height: <xsl:value-of select="*:height"/></value></xsl:if>
            <xsl:if test="*:width"><value>Width: <xsl:value-of select="*:width"/></value></xsl:if>
            <xsl:if test="*:mediaOccurrenceNote"><value>Media Occurrence Note: <xsl:value-of select="*:mediaOccurrenceNote"/></value></xsl:if>
            <xsl:if test="*:physicalRestrictionNote"><value>Physical Restriction Note: <xsl:value-of select="*:physicalRestrictionNote"/></value></xsl:if>
            <xsl:if test="*:pieceCount"><value>Piece Count: <xsl:value-of select="*:pieceCount"/></value></xsl:if>
            <xsl:if test="*:process/termName"><value>Process: <xsl:value-of select="*:process/termName"/></value></xsl:if>
            <xsl:if test="*:reproductionCount"><value>Reproduction Count: <xsl:value-of select="*:reproductionCount"/></value></xsl:if>
            <xsl:if test="*:technicalAccessRequirementsNote"><value>Technical Access Requirements Note: <xsl:value-of select="*:technicalAccessRequirementsNote"/></value></xsl:if>
            <xsl:if test="*:emulsion/termName"><value>Emulsion: <xsl:value-of select="*:emulsion/termName"/></value></xsl:if>
            <xsl:if test="*:footage"><value>Footage: <xsl:value-of select="*:footage"/></value></xsl:if>
            <xsl:if test="*:format/termName"><value>Format: <xsl:value-of select="*:format/termName"/></value></xsl:if>
            <xsl:if test="*:recordingSpeed/termName"><value>Record Speed: <xsl:value-of select="*:recordingSpeed/termName"/></value></xsl:if>
            <xsl:if test="*:reelTapeDiscNumber/termName"><value>Reel/Tape/Disc Number: <xsl:value-of select="*:reelTapeDiscNumber/termName"/></value></xsl:if>
            <xsl:if test="*:elementNumber/termName"><value>Element Number: <xsl:value-of select="*:elementNumber/termName"/></value></xsl:if>
            <xsl:if test="*:rollType/termName"><value>Roll: <xsl:value-of select="*:rollType/termName"/></value></xsl:if>
            <xsl:if test="*:runningTime"><value>Running Time: <xsl:value-of select="*:runningTime"/></value></xsl:if>
            <xsl:if test="*:soundtrackConfig/termName"><value>Soundtrack Configuration: <xsl:value-of select="*:soundtrackConfig/termName"/></value></xsl:if>
            <xsl:if test="*:soundtrackLang/termName"><value>Soundtrack Language: <xsl:value-of select="*:soundtrackLang/termName"/></value></xsl:if>
            <xsl:if test="*:tapeThickness/termName"><value>Tape Thickness: <xsl:value-of select="*:tapeThickness/termName"/></value></xsl:if>
            <xsl:if test="*:wind/termName"><value>Wind: <xsl:value-of select="*:wind/termName"/></value></xsl:if>
        </xsl:for-each>        
    </xsl:template>
    
    <xsl:template name="holdingMeasurement">
        <xsl:for-each select="*">
            <value><xsl:value-of select="*:count"/><xsl:text> </xsl:text><xsl:value-of select="*:type/termName"/></value>
        </xsl:for-each>        
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>