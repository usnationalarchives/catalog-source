<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="archivedCopies">
        <xsl:if test="/*/*:physicalOccurrenceArray">
            <!--PANEL ARCHIVED COPIES-->
            <div class="panel panel-default col-xs-12 borderless hidden-xs hidden-sm">
                <div ng-click="toggle('#arcCop','#arcCopLink')" class="panel-heading">
                    <span class="panel-title">
                        <a href="" rel="#" data-toggle="collapse" data-target="#arcCop">Archived Copies</a>
                    </span>
                    <span class="panel-title pull-right">
                        <a id="arcCopLink" class="content-toggle" data-toggle="collapse" data-target="#arcCop"/>
                    </span>
                </div>
                <div id="arcCop" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <xsl:call-template name="archivedCopiesInner"/>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="archivedCopiesInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="/*/*:physicalOccurrenceArray">
            <xsl:if test="$mobile">
                <span class="mobile-section"> Archived Copies </span>
            </xsl:if>
            <table class="table table-condensed">
                <col width="33.33%"/>
                <col width="66.66%"/>
                <tbody>
                    <!-- NARA-1463 -> [copyStatus/naId = 10026342] -->
                    <xsl:variable name="ReproRefCount" select="count(/*/*:physicalOccurrenceArray/*[copyStatus/naId = 10026342])"/>
                    <xsl:if test="$ReproRefCount > 0">
                        <xsl:for-each select="/*/*:physicalOccurrenceArray/*[copyStatus/naId = 10026342]">
                            <xsl:variable name="copyN" select="position()"/>
                            <xsl:call-template name="archivedCopiesMain">
                                <xsl:with-param name="object" select="."/>
                                <xsl:with-param name="copyN" select="$copyN"/>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:if>
                    <xsl:for-each select="/*/*:physicalOccurrenceArray/*[copyStatus/naId != 10026342]">
                        <xsl:variable name="copyN" select="position()"/>
                        <xsl:call-template name="archivedCopiesMain">
                            <xsl:with-param name="object" select="."/>
                            <xsl:with-param name="copyN" select="$copyN + $ReproRefCount"/>
                        </xsl:call-template>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>
    <xsl:template name="archivedCopiesMain">
        <xsl:param name="object"/>
        <xsl:param name="copyN"/>
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Copy <xsl:value-of
                    select="$copyN"/>:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="replace($object/*:copyStatus/(*:termName)[1], '9999', '')"/>
                </span>
            </td>
        </tr>
        <xsl:apply-templates select="$object/*:extent"/>
        <xsl:apply-templates select="$object/*:physicalOccurrenceNote"/>
        <xsl:apply-templates select="$object/*:referenceUnitArray"/>
        <xsl:apply-templates select="$object/*:totalFootage"/>
        <xsl:apply-templates select="$object/*:totalRunningTime"/>
        <xsl:apply-templates select="$object/*:holdingsMeasurementArray"/>
        <xsl:apply-templates select="$object/*:mediaOccurrenceArray">
            <xsl:with-param name="copyNumber" select="$copyN"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="*:extent">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Extent (Size):</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:referenceUnitArray">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Contact(s):</span>
            </td>
            <td>
                <xsl:for-each select="*:referenceUnit">
                    <span class="text-left">
                        <xsl:value-of select="replace(((*:termName)[1])[1], '9999', '')" disable-output-escaping="yes"/>
                        <xsl:if test="*:mailCode">
                            <xsl:text> (</xsl:text>
                            <xsl:value-of select="*:mailCode" disable-output-escaping="yes"/>
                            <xsl:text>)</xsl:text>
                        </xsl:if>
                    </span>
                    <br/>
                    <span class="text-left">
                        <xsl:if test="*:address1">
                            <xsl:value-of select="*:address1" disable-output-escaping="yes"/>
                        </xsl:if>
                    </span>
                    <br/>
                    <xsl:if test="*:address2">
                        <span>
                            <xsl:value-of select="*:address2" disable-output-escaping="yes"/>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:city | *:state | *:postCode">
                        <span>
                            <xsl:if test="*:city">
                                <xsl:value-of select="*:city" disable-output-escaping="yes"/>
                                <xsl:if test="*:state | *:postCode">
                                    <xsl:text>, </xsl:text>
                                </xsl:if>
                            </xsl:if>
                            <xsl:if test="*:state">
                                <xsl:value-of select="*:state" disable-output-escaping="yes"/>
                            </xsl:if>
                            <xsl:if test="*:postCode">
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="*:postCode" disable-output-escaping="yes"/>
                            </xsl:if>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:phone">
                        <span>
                            <xsl:text>Phone: </xsl:text>
                            <xsl:value-of select="*:phone" disable-output-escaping="yes"/>                            
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:fax">
                        <span>
                            <xsl:text>Fax: </xsl:text>
                            <xsl:value-of select="*:fax" disable-output-escaping="yes"/>                            
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:email">
                        <span>
                            <xsl:text>Email: </xsl:text>
                            <a href="mailto:{*:email}" target="_top"><xsl:value-of select="*:email" disable-output-escaping="yes"/></a>                            
                        </span>
                        <br/>
                    </xsl:if>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:totalFootage">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Total Footage:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:holdingsMeasurementArray">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Count:</span>
            </td>
            <td>
                <xsl:for-each select="*:holdingsMeasurement">
					<span class="text-left">
                        <xsl:value-of select="*:count"/>
                    </span>
                    <xsl:text> </xsl:text>
                    <span class="text-left">
                        <xsl:value-of select="replace(*:type/(*:termName)[1], '9999', '')"/>
                    </span>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>

    </xsl:template>
    <xsl:template match="*:physicalOccurrenceNote">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Physical Occurrence Note:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:mediaOccurrenceArray">
        <xsl:param name="copyNumber"/>
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Copy <xsl:value-of select="$copyNumber"/>
                    Media Information:</span>
            </td>
            <td>
                <xsl:for-each select="*">
                    <span class="text-left bold-martinique">Specific Media Type: </span>
                    <xsl:value-of select="replace(*:specificMediaType/(*:termName)[1], '9999', '')"
                        disable-output-escaping="yes"/>
                    <br/>
                    <xsl:if test="*:color">
                        <span class="text-left bold-martinique"> Color: </span>
                        <xsl:value-of select="replace(*:color/(*:termName)[1], '9999', '')"/>

                        <br/>
                    </xsl:if>
                    <xsl:if test="*:containerId">
                        <span class="text-left bold-martinique">Container ID: </span>
                        <xsl:value-of select="*:containerId"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:dimension">
                        <span class="text-left bold-martinique">Dimension: </span>
                        <xsl:value-of select="replace(*:dimension/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:height">
                        <span class="text-left bold-martinique">Height: </span>
                        <xsl:value-of select="*:height"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:width">
                        <span class="text-left bold-martinique">Width: </span>
                        <xsl:value-of select="*:width"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:depth">
                        <span class="text-left bold-martinique">Depth: </span>
                        <xsl:value-of select="replace(*:depth/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:mediaOccurrenceNote">
                        <span class="text-left bold-martinique">Media Occurrence Note: </span>
                        <xsl:value-of select="*:mediaOccurrenceNote" disable-output-escaping="yes"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:physicalRestrictionNote">
                        <span class="text-left bold-martinique">Physical Restriction Note: </span>
                        <xsl:value-of select="*:physicalRestrictionNote"
                            disable-output-escaping="yes"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:pieceCount">
                        <span class="text-left bold-martinique">Piece Count: </span>
                        <xsl:value-of select="*:pieceCount"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:process">
                        <span class="text-left bold-martinique">Process: </span>
                        <xsl:value-of select="replace(*:process/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:reproductionCount">
                        <span class="text-left bold-martinique">Reproduction Count: </span>
                        <xsl:value-of select="*:reproductionCount"/>
                        <xsl:text> </xsl:text>
                        <xsl:choose>
                            <xsl:when test="*:specificMediaType/*:naId = 10048756">
                                <!-- Paper type is 10048756 -->
                                <xsl:text>Page(s)</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="replace(*:specificMediaType/(*:termName)[1], '9999', '')"/>
                            </xsl:otherwise>
                        </xsl:choose>
                        <br/>
                    </xsl:if>
                    <xsl:if test="not(/*/*:variantControlNumberArray/*/number = 'DDI')">
                        <xsl:if test="*:technicalAccessRequirementsNote">
                            <span class="text-left bold-martinique">Technical Access Requirements Note: </span>
                            <xsl:value-of select="*:technicalAccessRequirementsNote" disable-output-escaping="yes"/>
                            <br/>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if test="*:emulsion">
                        <span class="text-left bold-martinique">Emulsion: </span>
                        <xsl:value-of select="replace(*:emulsion/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:footage">
                        <span class="text-left bold-martinique">Footage: </span>
                        <xsl:value-of select="*:footage"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:format">
                        <span class="text-left bold-martinique">Format: </span>
                        <xsl:value-of select="replace(*:format/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:recordingSpeed">
                        <span class="text-left bold-martinique">Record Speed: </span>
                        <xsl:value-of select="replace(*:recordingSpeed/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:reelTapeDiscNumber">
                        <span class="text-left bold-martinique">Reel/Tape/Disc Number: </span>
                        <xsl:value-of select="*:reelTapeDiscNumber"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:elementNumber">
                        <span class="text-left bold-martinique">Element Number: </span>
                        <xsl:value-of select="replace(*:elementNumber/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:rollType">
                        <span class="text-left bold-martinique">Roll: </span>
                        <xsl:value-of select="replace(*:rollType/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:runningTime">
                        <span class="text-left bold-martinique">Running Time: </span>
                        <xsl:value-of select="*:runningTime"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:soundtrackConfig">
                        <span class="text-left bold-martinique">Soundtrack Configuration: </span>
                        <xsl:value-of select="replace(*:soundtrackConfig/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:soundtrackLang">
                        <span class="text-left bold-martinique">Soundtrack Language: </span>
                        <xsl:value-of select="replace(*:soundtrackLang/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:tapeThickness">
                        <span class="text-left bold-martinique">Tape Thickness: </span>
                        <xsl:value-of select="replace(*:tapeThickness/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:wind">
                        <span class="text-left bold-martinique">Wind: </span>
                        <xsl:value-of select="replace(*:wind/(*:termName)[1], '9999', '')"/>
                        <br/>
                    </xsl:if>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:totalRunningTime">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Total Running Time:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="."/>
                </span>
            </td>
        </tr>
    </xsl:template>
</xsl:stylesheet>