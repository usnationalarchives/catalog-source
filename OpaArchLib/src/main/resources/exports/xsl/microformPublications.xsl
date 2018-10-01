<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:microformPublicationArray">
        <stringList>
            <xsl:if test="*:microformPublication">
                <xsl:for-each select="*:microformPublication">
                    <xsl:if test="*:publication/identifier">
                        <value>
                            <xsl:value-of select="*:publication/identifier"></xsl:value-of>
                        </value>
                    </xsl:if>
                    <xsl:if test="*:note">
                        <value>
                            <xsl:value-of select="*:note"></xsl:value-of>
                        </value>
                    </xsl:if>
                    <xsl:if test="*:publication/title">
                        <value>
                            <xsl:value-of select="*:publication/title"></xsl:value-of>
                        </value>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>