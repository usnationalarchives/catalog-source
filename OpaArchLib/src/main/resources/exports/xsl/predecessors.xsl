<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:organizationNameArray">
        <stringList>
            <xsl:if test="*:organizationName">
                <xsl:for-each select="*:organizationName">
                    <xsl:for-each select="*:predecessorArray">
                        <xsl:for-each select="*:organizationName">
                            <value><xsl:value-of select="*:termName"/></value>        
                        </xsl:for-each>
                    </xsl:for-each>
                </xsl:for-each> 
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>