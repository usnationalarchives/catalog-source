<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="aditionalInfo">
        <div class="panel panel-default col-xs-12 hidden-xs hidden-sm borderless">
            <div ng-click="toggle('#additionalInfo','#additionalInfoLink')" class="panel-heading">
                <span class="panel-title">
                    <a href="" rel="#" data-toggle="collapse" data-target="#additionalInfo">
                        Additional Information About this <xsl:value-of select="translate($lvlOfDescriotion,'&#xA;','')"/>
                    </a>
                </span>
                <span class="panel-title pull-right">
                    <a id="additionalInfoLink" class="content-toggle" data-toggle="collapse"
                        data-target="#additionalInfo"/>
                </span>
            </div>
            <div id="additionalInfo" class="panel-collapse collapse in">
                <div class="panel-body">
                    <xsl:call-template name="aditionalInfoInner"/>
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template name="aditionalInfoInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="$mobile">
            <span class="mobile-section">
                Additional Information About this <xsl:value-of select="translate($lvlOfDescriotion,'&#xA;','')"/>
            </span>
        </xsl:if>
        <table class="table table-condensed">
            <col width="33.33%"/>
            <col width="66.66%"/>
            <tbody>
                <xsl:apply-templates select="/*/*:naId"/>
                <xsl:apply-templates select="/*/*:localIdentifier"/>
                <xsl:call-template name="VCNAD"/>
                <xsl:apply-templates
                    select="/*/*/*/*:creatingOrganizationArray | /*/*/*:creatingOrganizationArray | /*/*:creatingOrganizationArray | *:creatingOrganizationArray | /*/*/*/*:creatingIndividualArray | /*/*/*:creatingIndividualArray | /*/*:creatingIndividualArray | *:creatingIndividualArray"/>
                <!--<xsl:apply-templates select="/*/*:parentSeries | /*/*:parentCollection | /*/*:parentFileUnit | /*/*:parentRecordGroup"/>-->
                <xsl:call-template name="fromSubSection"/>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template match="*:naId">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">National Archives Identifier:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:localIdentifier">
        <tr>
            <td class="text-right">
                <span class="text-right bold">Local Identifier:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </td>
        </tr>
    </xsl:template>
    <xsl:template name="VCNAD">
        <xsl:if test="/*/*:variantControlNumberArray">
            <!-- NARA-1437 -> [*:type/*:naId = 10641292] -->
            <xsl:if test="/*/*:variantControlNumberArray/*[*:type/*:naId = 10641292]">
                <tr>
                    <td class="text-right">
                        <span class="text-right bold">HMS Entry Number(s):</span>
                    </td>
                    <td>
                        <xsl:for-each select="/*/*:variantControlNumberArray/*:variantControlNumber[*:type/*:naId = 10641292]">
                            <span class="text-left">
                                <xsl:value-of select="*:number" disable-output-escaping="yes"/>
                            </span>
                            <br/>
                        </xsl:for-each>
                    </td>
                </tr>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:creatingOrganizationArray | *:creatingIndividualArray">
        <xsl:if test=".">           
                <tr>
                    <td class="text-right">
                        <span class="text-right bold">Creator(s):</span>                        
                    </td>
                    <td>
                        <!-- Print the Current -->
                        <xsl:for-each select="*:creatingIndividual | *:creatingOrganization">
                            <xsl:if test="*:creatorType/(*:termName)[1] = 'Most Recent'">
                                <span class="text-left">
                                    <a href="/id/{*:creator/*:naId}">
                                        <!-- NARAOPA-246: Creator term name may contain a date, DAS sends the 9999 invalid date sometimes and it should be removed -->
                                        <xsl:value-of select="replace(*:creator/(*:termName)[1], '9999', '')" disable-output-escaping="yes"/>
                                    </a>
                                    &#160;<xsl:if test="*:creatorType">(<xsl:value-of select="*:creatorType/(*:termName)[1]"
                                        disable-output-escaping="yes"/>)</xsl:if>
                                </span>
                                <br/>
                            </xsl:if>
                        </xsl:for-each>
                        
                        <xsl:for-each select="*:creatingIndividual | *:creatingOrganization">
                            <xsl:sort select="*:creator/*:establishDate/*:year" data-type="number" order="ascending"/>
                            <xsl:if test="not(*:creatorType/(*:termName)[1] = 'Most Recent')">                            
                             <span class="text-left">
                                 <a href="/id/{*:creator/*:naId}">
                                     <xsl:value-of select="replace(*:creator/(*:termName)[1], '9999', '')" disable-output-escaping="yes"/>
                                 </a>
                                 &#160;<xsl:if test="*:creatorType">(<xsl:value-of select="*:creatorType/(*:termName)[1]"
                                     disable-output-escaping="yes"/>)</xsl:if>
                             </span>
                             <br/>
                            </xsl:if>
                        </xsl:for-each>
                    </td>
                </tr>            
        </xsl:if>
    </xsl:template>
    <xsl:template name="fromSubSection">
        <xsl:if test="/*/*:parentSeries | /*/*:parentCollection | /*/*:parentFileUnit | /*/*:parentRecordGroup">
            <tr>
                <td class="text-right">
                    <span class="text-right bold">From:</span>
                </td>
                <td>
                    <xsl:apply-templates select="/*/*:parentSeries | /*/*:parentCollection | /*/*:parentFileUnit | /*/*:parentRecordGroup"/>
                    <!--<span class="text-left">
                        <a href="/id/{*:naId}">
                            <xsl:value-of select="*:title" disable-output-escaping="yes"/>
                        </a>
                    </span>-->
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:parentSeries | *:parentCollection | *:parentFileUnit | *:parentRecordGroup">
        <span class="text-left">
            <a href="/id/{*:naId}">
                <xsl:choose>
                    <xsl:when test="local-name(.) = 'parentSeries'">
                        <xsl:text>Series: </xsl:text>
                    </xsl:when>
                    <xsl:when test="local-name(.) = 'parentCollection'">
                        <xsl:text>Collection: </xsl:text>
                    </xsl:when>
                    <xsl:when test="local-name(.) = 'parentFileUnit'">
                        <xsl:text>File Unit: </xsl:text>
                    </xsl:when>
                    <xsl:when test="local-name(.) = 'parentRecordGroup'">
                        <xsl:text>Record Group </xsl:text>
                        <xsl:if test="recordGroupNumber">
                            <xsl:value-of select="recordGroupNumber"/>
                        </xsl:if>
                        <xsl:text>: </xsl:text>
                    </xsl:when>
                </xsl:choose>
                <xsl:value-of select="*:title" disable-output-escaping="yes"/>
                <xsl:if test="*:inclusiveDates">
                    <xsl:text>, </xsl:text>
                    <xsl:call-template name="getDate">
                        <xsl:with-param name="dateObject"
                            select="*:inclusiveDates/*:inclusiveStartDate"/>
                    </xsl:call-template> - <xsl:call-template name="getDate">
                        <xsl:with-param name="dateObject"
                            select="*:inclusiveDates/*:inclusiveEndDate"/>
                    </xsl:call-template>
                </xsl:if>
            </a>
        </span>
        <br/>
        <xsl:apply-templates select="./*:parentSeries | ./*:parentCollection | ./*:parentFileUnit | ./*:parentRecordGroup"/>
        <!--</td>
        </tr>-->
    </xsl:template>
</xsl:stylesheet>