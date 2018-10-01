<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template match="*">
        <xsl:if test="/*/*:latitudelongitude">
            <value><xsl:value-of select="/*/*:latitudelongitude"/></value>
        </xsl:if>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>