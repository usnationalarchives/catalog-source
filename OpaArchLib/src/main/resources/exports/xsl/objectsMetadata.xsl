<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    
    <xsl:template match="*">
        <objects>
            <version><xsl:value-of select="@version"/></version>
            <xsl:for-each select="object">
                <object>
                    <id><xsl:value-of select="@id"/></id>  
                    <thumbnailpath><xsl:value-of select="thumbnail/@path"/></thumbnailpath>
                    <thumbnailmimetype><xsl:value-of select="thumbnail/@mime"/></thumbnailmimetype>
                    <filepath><xsl:value-of select="file/@path"/></filepath>
                    <filemimetype><xsl:value-of select="file/@mime"/></filemimetype>
                    <xsl:if test="designator">
                        <designator><xsl:value-of select="designator"/></designator>
                    </xsl:if>
                    <xsl:if test="description">
                        <description><xsl:value-of select="description"/></description>
                    </xsl:if>
                </object>
                </xsl:for-each>
            
        </objects>
    </xsl:template>
    
    <xsl:template match="text()"/>
</xsl:stylesheet>