<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:findingAidArray">
        <fieldList>
        <xsl:for-each select="*:findingAid">
            <xsl:if test="*:type">
                <field><name>findingAidType</name><label>Finding Aid Type</label><value><xsl:value-of select="*:type/*:termName"/></value></field>
            </xsl:if>
            <xsl:if test="*:note">
                <field><name>findingAidNote</name><label>Finding Aid Note</label><value><xsl:value-of select="*:note"/></value></field>
            </xsl:if>
            <xsl:if test="*:source">
                <field><name>findingAidSource</name><label>Finding Aid Source</label><value><xsl:value-of select="*:source"/></value></field>                
           </xsl:if>
        </xsl:for-each>
        </fieldList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>