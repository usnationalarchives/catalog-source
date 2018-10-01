<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:linkCounts">
        <stringList>
            <xsl:variable name="relatedSum">
                <xsl:value-of
                    select="number(*:contributorLinkCount) + number(*:creatorLinkCount) + number(*:donorLinkCount) + number(*:subjectLinkCount) + number(*:personAuthorityLinkCount)"/>            
            </xsl:variable>
            <xsl:if test="number($relatedSum) > 0">
                <value>Related to <xsl:value-of select="$relatedSum"/> catalog description(s)</value>
            </xsl:if>
            <xsl:if test="number(*:contributorLinkCount) > 0">
                <value>Contributor in <xsl:value-of select="number(*:contributorLinkCount)"/> description(s)</value>
            </xsl:if>
            <xsl:if test="number(*:creatorLinkCount) > 0">
                <value>Created <xsl:value-of select="number(*:creatorLinkCount)"/> series</value>
            </xsl:if>
            <xsl:if test="number(*:subjectLinkCount) > 0">
                <value>Subject in <xsl:value-of select="number(*:subjectLinkCount)"/> description(s)</value>
            </xsl:if>
            <xsl:if test="number(*:donorLinkCount) > 0">
                <value>Donor of <xsl:value-of select="number(*:donorLinkCount)"/> description(s)</value>
            </xsl:if>
            <xsl:if test="number(*:organizationAuthorityLinkCount) > 0">
                <value>Personal Reference in <xsl:value-of select="number(*:organizationAuthorityLinkCount)"/> organization(s)</value>
            </xsl:if>
        </stringList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>