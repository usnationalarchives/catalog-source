<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template match="*:digitalObjectArray">
        <div class="uer col-xs-12" ng-show="DDI">
            <p class="files-avail"><xsl:value-of select="count(./*)"/> files available </p>
            <p class="title">Technical Documentation</p>

            <ol class="uer-list documents">
                <xsl:for-each select="*:digitalObject[*:objectDesignator = 'Technical Documentation']">
                    <xsl:variable name="fileName">
                        <xsl:call-template name="fileName">
                            <xsl:with-param name="url" select="*:accessFilename"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="fileExt">
                        <xsl:call-template name="fileExt">
                            <xsl:with-param name="fileName" select="$fileName"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <li ng-show="{position()} &lt;= 10 || allRecords">
                        <div class="uer-row1">
                            <a class="view" target="_blank" href="{*:accessFilename}">
                                <xsl:value-of select="*:objectDescription"/>
                            </a>
                        </div>
                        <div class="uer-row2"> (<xsl:value-of select="$fileName"/>, <xsl:value-of select="$fileExt"/>, <xsl:call-template name="getSize"><xsl:with-param name="size" select="*:accessFileSize"/></xsl:call-template>) </div>
                    </li>
                </xsl:for-each>
            </ol>
            <p class="title">Electronic Records</p>

            <ol class="uer-list electronic-records">
                <xsl:for-each select="*:digitalObject[*:objectDesignator = 'Electronic Records']">
                    <xsl:variable name="fileName">
                        <xsl:call-template name="fileName">
                            <xsl:with-param name="url" select="*:accessFilename"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <li ng-show="allRecords">
                        <div class="uer-row1">
                            <a class="view" target="_blank" href="http://media.nara.gov/electronic-records/rg-443/CPP/PRBCPP.MDF0378.P.zip">View/Download Compressed Master File [public use]</a>
                        </div>
                        <div class="uer-row2"> (<xsl:value-of select="$fileName"/>, ASCII, <xsl:call-template name="getSize"><xsl:with-param name="size" select="*:accessFileSize"/></xsl:call-template>) </div>
                    </li>
                </xsl:for-each>
            </ol>
            <a href="" rel="#" ng-click="allRecords = true" ng-hide="allRecords" class="show-more-link">show all files</a>
        </div>
    </xsl:template>
    <xsl:template name="fileName">
        <xsl:param name="url" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="matches($url,'^[^/]*\.[^/]*$')">
                <xsl:value-of select="$url"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="fileName">
                    <xsl:with-param name="url" select="substring-after($url, '/')"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="fileExt">
        <xsl:param name="fileName" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="matches($fileName,'^[^/\.]*$')">
                <xsl:value-of select="upper-case($fileName)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="fileExt">
                    <xsl:with-param name="fileName" select="substring-after($fileName, '.')"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template name="getSize">
        <xsl:param name="size"/>
        <xsl:choose>
            <xsl:when test="$size &gt;= 1099511627776">
                <xsl:value-of select="concat(round($size div 1099511627776), 'TB')"/>
            </xsl:when>
            <xsl:when test="$size &gt;= 1073741824">
                <xsl:value-of select="concat(round($size div 1073741824), 'GB')"/>
            </xsl:when>
            <xsl:when test="$size &gt; 1048576">
                <xsl:value-of select="concat(round($size div 1048576), 'MB')"/>
            </xsl:when>
            <xsl:when test="$size &gt;= 1024">
                <xsl:value-of select="concat(round($size div 1024), 'KB')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat(round($size), 'B')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>



















