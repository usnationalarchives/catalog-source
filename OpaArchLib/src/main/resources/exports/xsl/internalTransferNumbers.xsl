<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:internalTransferNumberArray">
        <stringList>
            <xsl:if test="*:internalTransferNumber">
                <xsl:for-each select="*:internalTransferNumber">
                    <xsl:if test="*:number">
                        <value>
                            <xsl:value-of select="*:number"></xsl:value-of>
                        </value>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>