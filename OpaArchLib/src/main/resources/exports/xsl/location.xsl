<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <!--Items, FileUnits and series -->
    <xsl:template match="*:physicalOccurrenceArray">
        <fieldList>
            <xsl:for-each select="itemPhysicalOccurrence|itemAvPhysicalOccurrence|seriesPhysicalOccurrence|fileUnitPhysicalOccurrence">
                <xsl:for-each select="referenceUnitArray/referenceUnit">    
                    <field><name>locationFacility<xsl:value-of select="position()"/></name><label>Contact(s)</label>
                        <value>
                            <stringList>
                                <value><xsl:value-of select="*:termName"></xsl:value-of> <xsl:if test="*:mailCode">(<xsl:value-of select="*:mailCode"/>)</xsl:if></value>
                                <xsl:if test="*:address1"><value><xsl:value-of select="*:address1"/></value></xsl:if>
                                <xsl:if test="*:address2"><value><xsl:value-of select="*:address2"/></value></xsl:if>
                                <xsl:if test="*:city"><value><xsl:value-of select="*:city"/>, <xsl:value-of select="*:state"/>, <xsl:value-of select="*:postCode"/></value></xsl:if>
                                <xsl:if test="*:phone"><value>Phone: <xsl:value-of select="*:phone"/></value></xsl:if>
                                <xsl:if test="*:fax"><value>Fax: <xsl:value-of select="*:fax"/></value></xsl:if>
                                <xsl:if test="*:email"><value>Email: <xsl:value-of select="*:email"/></value></xsl:if>
                            </stringList>
                        </value>
                    </field>
                </xsl:for-each>
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    <!-- Record groups and collections -->
    <xsl:template match="*:referenceUnits">
        <fieldList>
            <xsl:for-each select="referenceUnit">
                <field><name>locationFacility<xsl:value-of select="position()"/></name><label>Contact(s)</label>
                    <value>
                        <stringList>
                            <value><xsl:value-of select="*:termName"></xsl:value-of> <xsl:if test="*:mailCode">(<xsl:value-of select="*:mailCode"/>)</xsl:if></value>
                            <xsl:if test="*:address1"><value><xsl:value-of select="*:address1"/></value></xsl:if>
                            <xsl:if test="*:address2"><value><xsl:value-of select="*:address2"/></value></xsl:if>
                            <xsl:if test="*:city"><value><xsl:value-of select="*:city"/>, <xsl:value-of select="*:state"/>, <xsl:value-of select="*:postCode"/></value></xsl:if>
                            <xsl:if test="*:phone"><value>Phone: <xsl:value-of select="*:phone"/></value></xsl:if>
                            <xsl:if test="*:fax"><value>Fax: <xsl:value-of select="*:fax"/></value></xsl:if>
                            <xsl:if test="*:email"><value>Email: <xsl:value-of select="*:email"/></value></xsl:if>
                        </stringList>
                    </value>
                </field>
                
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>