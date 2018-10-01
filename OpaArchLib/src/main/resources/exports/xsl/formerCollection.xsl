<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*:formerCollectionArray">
        <stringList>
            <xsl:if test="*:collection">
                <xsl:for-each select="*:collection">
                    <xsl:if test="*:collectionIdentifier">
                    <value>
                        <xsl:value-of select="*:collectionIdentifier"></xsl:value-of>
                    </value>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
        </stringList>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>