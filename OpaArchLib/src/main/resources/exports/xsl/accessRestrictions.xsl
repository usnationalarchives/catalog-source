<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:accessRestriction">
        <stringList>
            <xsl:if test="*:status/termName">
                <value><xsl:value-of select="*:status/termName"></xsl:value-of></value>                        
            </xsl:if>
            <xsl:if test="*:specificAccessRestrictionArray">
                <xsl:for-each select="*:specificAccessRestrictionArray/specificAccessRestriction">
                   <value>
                        <xsl:text>Specific Access Restriction: </xsl:text>
                        <xsl:value-of select="*:restriction/termName"></xsl:value-of>
                        <xsl:text>, </xsl:text>
                        <xsl:value-of select="*:securityClassification/termName"></xsl:value-of>
                   </value>
                </xsl:for-each>
            </xsl:if>
            <xsl:if test="*:accessRestrictionNote">
                <value><xsl:text>Note: </xsl:text><xsl:value-of select="*:accessRestrictionNote"></xsl:value-of></value>                        
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>