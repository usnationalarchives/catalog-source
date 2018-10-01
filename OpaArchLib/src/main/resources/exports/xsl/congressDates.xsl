<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*">
        
        <value>
            <xsl:if test="*:beginCongress/termName">
                    <xsl:value-of select="*:beginCongress/termName" disable-output-escaping="yes"/>
            </xsl:if>
            
            <xsl:if test="*:endCongress/termName">
                    <xsl:text> - </xsl:text>
                    <xsl:value-of select="*:endCongress/termName" disable-output-escaping="yes"/>
            </xsl:if>
        </value>
        
    </xsl:template>

    <xsl:template match="text()"/>
</xsl:stylesheet>