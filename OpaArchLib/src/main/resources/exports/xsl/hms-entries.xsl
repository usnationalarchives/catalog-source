<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*:variantControlNumberArray">
        <fieldList>
            <xsl:for-each select="*:variantControlNumber">
                <xsl:if test="*:type/*:termName">
                    <xsl:variable name="termName" select="*:type/*:termName"/>
                    <xsl:if test="starts-with($termName, 'HMS')">
                        <field>
                            <label>
                                <xsl:value-of select="*:type/*:termName"/>
                            </label>
                            <value>
                                <xsl:value-of select="*:number"/>
                                <xsl:if test="*:note"><xsl:text> </xsl:text>
                                    <xsl:value-of select="*:note"/></xsl:if>
                            </value>
                        </field>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </fieldList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>