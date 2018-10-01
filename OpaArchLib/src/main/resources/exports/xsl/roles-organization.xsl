<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*">
        <stringList>
            <xsl:variable name="relatedSum">
                <xsl:value-of
                    select="number(/*/*:linkCounts/*:contributorLinkCount) + number(/*/*:linkCounts/*:creatorLinkCount) + number(/*/*:linkCounts/*:donorLinkCount) + number(/*/*:linkCounts/*:organizationAuthorityLinkCount) + number(/*/*:linkCounts/*:organizationNameAuthorityLinkCount) + number(/*/*:linkCounts/*:personAuthorityLinkCount)"/>            
            </xsl:variable>
            <xsl:if test="number($relatedSum) > 0">
                <value>Related to <xsl:value-of select="$relatedSum"/> catalog description(s)</value>
            </xsl:if>
            <xsl:if test="number(/*/*:linkCounts/*:contributorLinkCount) > 0">
                <value>Contributor in <xsl:value-of select="number(/*/*:linkCounts/*:contributorLinkCount)"/> description(s)</value>
            </xsl:if>
            <xsl:if test="number(/*/*:linkCounts/*:creatorLinkCount) > 0">
                <value>Created <xsl:value-of select="number(/*/*:linkCounts/*:creatorLinkCount)"/> series</value>
            </xsl:if>
            <xsl:if test="number(/*/*:linkCounts/*:organizationAuthorityLinkCount) + number(/*/*:linkCounts/*:organizationNameAuthorityLinkCount) > 0">
                <value>Subject in <xsl:value-of select="number(/*/*:linkCounts/*:organizationAuthorityLinkCount) + number(/*/*:linkCounts/*:organizationNameAuthorityLinkCount)"/> description(s)</value>
            </xsl:if>
            <xsl:if test="number(/*/*:linkCounts/*:donorLinkCount) > 0">
                <value>Donor of <xsl:value-of select="number(/*/*:linkCounts/*:donorLinkCount)"/> description(s)</value>
            </xsl:if>
        </stringList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>