<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:referenceUnits">
        <stringList>
            <xsl:for-each select="*:referenceUnit">            
                <value><xsl:value-of select="*:termName"></xsl:value-of> <xsl:if test="*:mailCode">(<xsl:value-of select="*:mailCode"/>)</xsl:if></value>
                <xsl:if test="*:address1"><value><xsl:value-of select="*:address1"/></value></xsl:if>
                <xsl:if test="*:address2"><value><xsl:value-of select="*:address2"/></value></xsl:if>
                <value><xsl:value-of select="*:city"/>, <xsl:value-of select="*:state"/>, <xsl:value-of select="*:postCode"/></value>
                <xsl:if test="*:phone"><value>Phone: <xsl:value-of select="*:phone"/></value></xsl:if>
                <xsl:if test="*:fax"><value>Fax: <xsl:value-of select="*:fax"/></value></xsl:if>
                <xsl:if test="*:email"><value>Email: <xsl:value-of select="*:email"/></value></xsl:if>
                <value></value>
            </xsl:for-each>
        </stringList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>