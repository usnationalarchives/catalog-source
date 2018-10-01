<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
    version="2.0">    
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="details">
        <div class="panel panel-default col-xs-12 borderless"
            ng-class="{{'hidden-xs' : !isGeoPlace &amp;&amp; !isAuthorityRecord, 'hidden-sm': !isGeoPlace &amp;&amp; !isAuthorityRecord}}"
            ng-hide="isAuthorityRecord || isGeoPlace">
            <div ng-click="toggle('#details','#detailsLink')" class="panel-heading">
                <span class="panel-title">
                    <a href="" rel="#" data-toggle="collapse" data-target="#details">Details</a>
                </span>
                <span class="panel-title pull-right">
                    <a id="detailsLink" class="content-toggle" data-toggle="collapse"
                        data-target="#details"/>
                </span>
            </div>
            <div id="details" class="panel-collapse collapse in">
                <div class="panel-body">
                    <xsl:call-template name="detailsInner"/>
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template name="detailsInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="$mobile">
            <span class="mobile-section"> Details </span>
        </xsl:if>
        <div class="col-xs-12">
                <xsl:call-template name="lvlOfDescription"/>
                <xsl:apply-templates select="/*/*:generalRecordsTypeArray"/>
                <xsl:apply-templates select="/*/*:broadcastDateArray"/>
                <xsl:apply-templates select="/*/*:copyrightDateArray"/>
                <xsl:apply-templates select="/*/*:productionDateArray"/>
                <xsl:apply-templates select="/*/*:releaseDateArray"/>
                <xsl:apply-templates select="/*:recordGroup/*:recordGroupNumber"/>
                <xsl:apply-templates select="/*:collection/*:collectionIdentifier"/>
                <xsl:apply-templates select="/*/*:referenceUnits"/>
                <xsl:apply-templates
                    select="/*/*:inclusiveDates | /*/*:parentSeries/*:inclusiveDates | /*/*:parentFileUnit/*:parentSeries/*:inclusiveDates"/>
                <xsl:apply-templates select="/*/*:coverageDates"/>
                <xsl:apply-templates select="/*/*:dateNote"/>
                <xsl:call-template name="congressDates"/>            
                <xsl:call-template name="includes"/>
                <xsl:apply-templates select="/*/*:otherTitleArray"/>
                <xsl:call-template name="productionSeries"/>
                <xsl:apply-templates select="/*/*:functionAndUse"/>
                <xsl:apply-templates select="/*/*:scaleNote"/>
                <xsl:apply-templates select="/*/*:numberingNote"/>
                <xsl:apply-templates select="/*/*:generalNoteArray"/>
                <xsl:apply-templates select="/*/*:arrangement"/>
                <xsl:apply-templates select="/*/*:accessRestriction"/>
                <xsl:apply-templates select="/*/*:useRestriction"/>
                <xsl:apply-templates select="/*/*:custodialHistoryNote"/>
                <xsl:apply-templates select="/*/*:transferNote"/>
                <xsl:apply-templates select="/*/*:editStatus"/>
                <xsl:apply-templates select="/*/*:soundType"/>
                <xsl:apply-templates select="/*/*:languageArray"/>
                <!--Donors-->
                <xsl:apply-templates select="/*/*:findingAidArray"/>
                <xsl:apply-templates select="/*/*:accessionNumberArray"/>
                <xsl:apply-templates select="/*/*:recordsCenterTransferArray"/>
                <xsl:apply-templates select="/*/*:dispositionAuthorityNumberArray"/>
                <xsl:apply-templates select="/*/*:recordsCenterTransferNumberArray"/>
                <xsl:apply-templates select="/*/*:internalTransferNumberArray"/>
                <xsl:apply-templates select="/*/*:microformPublicationArray"/>
                <xsl:apply-templates select="/*/*:onlineResourceArray"/>
                <xsl:call-template name="donorsArray"/>
                <!--TODO: CREATE A FUNCTION TO AVOID DUPLICATED HEADER-->
                <!--<xsl:apply-templates select="/*/*:personalReferenceArray | /*/*:organizationalReferenceArray | /*/*:geographicReferenceArray | /*/*:specificRecordsTypeArray | /*/*:topicalSubjectArray"/>-->
                <xsl:call-template name="subjects"/>
                <xsl:call-template name="contributors"/>
                <xsl:apply-templates select="/*/*:formerRecordGroupArray"/>
                <xsl:apply-templates select="/*/*:formerCollectionArray"/>
        </div>
    </xsl:template>
    <xsl:template name="lvlOfDescription">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Level of Description:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:choose>
                        <xsl:when test="local-name(/*) = 'item'"> Item </xsl:when>
                        <xsl:when test="local-name(/*) = 'itemAv'"> Item </xsl:when>
                        <xsl:when test="local-name(/*) = 'series'"> Series </xsl:when>
                        <xsl:when test="local-name(/*) = 'collection'"> Collection </xsl:when>
                        <xsl:when test="local-name(/*) = 'fileUnit'"> File Unit </xsl:when>
                        <xsl:when test="local-name(/*) = 'recordGroup'"> Record Group </xsl:when>
                    </xsl:choose>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:collectionIdentifier">
        <!--<xsl:if test="*:collectionIdentifier">-->
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Collection Identifier:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="."/>
                </span>
            </div>
        </div>
        <!--</xsl:if>-->
    </xsl:template>
    <xsl:template match="*:recordGroupNumber">
        <!--<xsl:if test="*:recordGroupNumber">-->
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Record Group Number:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="."/>
                </span>
            </div>
        </div>
        <!--</xsl:if>-->
    </xsl:template>
    <xsl:template match="*:referenceUnits">
        <xsl:for-each select="*:referenceUnit">
            <xsl:variable name="index" select="position()"/>
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <xsl:if test="$index = 1">
                        <span class="text-right bold">Contact(s):</span>
                    </xsl:if>
                </div>
                <div class="col-xs-8">
                    <span class="text-left">
                        <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                    </span>
                    <xsl:if test="*:mailCode">
                        <span class="text-left"> (<xsl:value-of select="*:mailCode"/>)</span>
                    </xsl:if>
                    <xsl:if test="*:address1">
						<br/>
						<span class="text-left">
							<xsl:value-of select="*:address1"/>
						</span>
                    </xsl:if>
                    <br/>
                    <xsl:if test="*:address2">
                        <span class="text-left">
                            <xsl:value-of select="*:address2"/>
                        </span>
                        <br/>
                    </xsl:if>
                    <span class="text-left"><xsl:value-of select="*:city"/>, <xsl:value-of
                            select="*:state"/>, <xsl:value-of select="*:postCode"/></span>
                    <br/>
                    <xsl:if test="*:phone">
                        <span class="text-left">Phone: <xsl:value-of select="*:phone"/></span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:fax">
                        <span class="text-left">Fax: <xsl:value-of select="*:fax"/></span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="*:email">
                        <span class="text-left">Email: <a href="mailto:{*:email}" target="_top"><xsl:value-of select="*:email" disable-output-escaping="yes"/></a>                            </span>
						
                        <br/>
                    </xsl:if>
                </div>
            </div>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="*:inclusiveDates">
        <xsl:if test="*:inclusiveEndDate | *:inclusiveStartDate">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>
                        <xsl:choose>
                            <xsl:when test="local-name(/*) = 'item'">
                                <xsl:text>The creator compiled or maintained the series between:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'itemAv'">
                                <xsl:text>The creator compiled or maintained the series between:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'series'">
                                <xsl:text>The creator compiled or maintained the series between:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'collection'">
                                <xsl:text>This collection was compiled or maintained between:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'fileUnit'">
                                <xsl:text>The creator compiled or maintained the series between:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'recordGroup'">
                                <xsl:text>This record group was compiled or maintained between:</xsl:text>
                            </xsl:when>
                        </xsl:choose>
                    </span>
                </div>
                <div class="col-xs-8">
                    <span class="text-left text-error">
                        <xsl:call-template name="getDate">
                            <xsl:with-param name="dateObject" select="*:inclusiveStartDate"/>
                        </xsl:call-template>
                    </span> - <span class="text-left text-error">
                        <xsl:call-template name="getDate">
                            <xsl:with-param name="dateObject" select="*:inclusiveEndDate"/>
                        </xsl:call-template>
                    </span>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:coverageDates">
        <xsl:if test="./* and ./*:coverageStartDate != '' and ./*:coverageEndDate != ''">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>
                        <xsl:choose>
                            <xsl:when test="local-name(/*) = 'item'">This item documents the time
                                period:</xsl:when>
                            <xsl:when test="local-name(/*) = 'itemAv'">This item av documents the
                                time period:</xsl:when>
                            <xsl:when test="local-name(/*) = 'series'">
                                <xsl:text>This series documents the time period:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'collection'">
                                <xsl:text>This collection documents the time period:</xsl:text>
                            </xsl:when>
                            <xsl:when test="local-name(/*) = 'fileUnit'">This file unit documents
                                the time period:</xsl:when>
                            <xsl:when test="local-name(/*) = 'recordGroup'">
                                <xsl:text>This record group documents the time period:</xsl:text>
                            </xsl:when>
                        </xsl:choose>
                    </span>
                </div>
                <div class="col-xs-8">
                    <span class="text-left text-error">
                        <xsl:call-template name="getDate">
                            <xsl:with-param name="dateObject" select="*:coverageStartDate"/>
                        </xsl:call-template>
                    </span> - <span class="text-left text-error">
                        <xsl:call-template name="getDate">
                            <xsl:with-param name="dateObject" select="*:coverageEndDate"/>
                        </xsl:call-template>
                    </span>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:dateNote">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Date Note:</span>
                </div>
                <div class="col-xs-8">
                    <span class="text-left">
                        <xsl:value-of select="." disable-output-escaping="yes"/>
                    </span>
                    <br/>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="congressDates">
        <xsl:if test="/*/*:beginCongress or /*/*:endCongress">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>These records document the following Congresses:</span>
                </div>
                <div class="col-xs-8">
                    <span class="text-left">
						<xsl:if test="/*/*:beginCongress/termName">
							<xsl:value-of select="/*/*:beginCongress/termName" disable-output-escaping="yes"/>
						</xsl:if>
						<xsl:if test="/*/*:endCongress/termName">
								<xsl:text> - </xsl:text>
								<xsl:value-of select="/*/*:endCongress/termName" disable-output-escaping="yes"/>
						</xsl:if>
                    </span> 
                </div>
            </div>       
        </xsl:if>
    </xsl:template>
    <xsl:template name="includes">
        <xsl:variable name="naId" select="/*/*:naId"/>
        <xsl:variable name="series">
            <xsl:choose>
                <xsl:when test="string(number(/*/*:seriesCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="number(/*/*:seriesCount)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="fileUnits">
            <xsl:choose>
                <xsl:when test="string(number(/*/*:fileUnitCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="format-number(number(/*/*:fileUnitCount),'0')"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="items">
            <xsl:choose>
                <xsl:when test="string(number(/*/*:itemCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="format-number(number(/*/*:itemCount),'0')"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="itemsAv">
            <xsl:choose>
                <xsl:when test="string(number(/*/*:itemAvCount))='NaN'">0</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="format-number(number(/*/*:itemAvCount),'0')"/>
                </xsl:otherwise>
            </xsl:choose>

        </xsl:variable>
        <xsl:variable name="allItems" select="number($items) + number($itemsAv)"/>
        <xsl:variable name="allCounts" select="number($series) + number($fileUnits) + ($allItems)"/>
        <xsl:variable name="thisNaid">
            <xsl:analyze-string select='/*/*:naId' regex='\d+'>
                <xsl:matching-substring>
                    <xsl:value-of select='.' />
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <!---->
        <xsl:if test="$allCounts > 0">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Includes:</span>
                </div>
                <div class="col-xs-8 col-sm-4">
                    <xsl:if test="$series > 0">
                        <span class="text-left text-error">
                            <a href="" rel="#" ng-click="searchBy('f.parentNaId', naId, 'series')"><xsl:value-of
                                    select="$series"/> series described in the catalog</a>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="$fileUnits > 0">
                        <span class="text-left text-error">
                            <a href="" rel="#" ng-click="searchBy('f.parentNaId', naId, 'fileUnit')"><xsl:value-of
                                    select="$fileUnits"/> file unit(s) described in the catalog</a>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="$allItems > 0">
                        <span class="text-left text-error">
                            <a href="" rel="#" ng-click="searchBy('f.parentNaId', naId, 'item')"><xsl:value-of
                                    select="$allItems"/> item(s) described in the catalog</a>
                        </span>
                        <br/>
                    </xsl:if>
                </div>
                <div class="col-xs-8 col-xs-offset-4 col-sm-4 col-sm-offset-0">
                        <a href="{concat('/search?q=*:*&amp;f.ancestorNaIds=',$thisNaid,'&amp;sort=naIdSort%20asc')}" class="btn btn-primary details">
                            <xsl:choose>
                                <xsl:when test="local-name(/*) = 'series'"> Search within this series </xsl:when>
                                <xsl:when test="local-name(/*) = 'collection'"> Search within this
                                    collection </xsl:when>
                                <xsl:when test="local-name(/*) = 'fileUnit'"> Search within this file
                                    unit </xsl:when>
                                <xsl:when test="local-name(/*) = 'recordGroup'"> Search within this <br class="show-inline-xxs"/>
                                    record group </xsl:when>
                            </xsl:choose>
                        </a>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:findingAidArray">
        <xsl:for-each select="*:findingAid">
            <xsl:if test="*:type">
                <div class="content-row">
                    <div class="col-xs-4 text-right">
                        <span>Finding Aid Type:</span>
                    </div>
                    <div class="col-xs-8">
                        <span class="text-left text-error">
                            <xsl:value-of select="replace(*:type/(*:termName)[1], '9999', '')"/>
                        </span>
                    </div>
                </div>
            </xsl:if>
            <xsl:if test="*:note">
                <div class="content-row">
                    <div class="col-xs-4 text-right">
                        <span>Finding Aid Note:</span>
                    </div>
                    <div class="col-xs-8">
                        <span class="text-left text-error">
                            <xsl:value-of select="*:note" disable-output-escaping="yes"/>
                        </span>
                    </div>
                </div>
            </xsl:if>
            <xsl:if test="*:source">
                <div class="content-row">
                    <div class="col-xs-4 text-right">
                        <span>Finding Aid Source:</span>
                    </div>
                    <div class="col-xs-8">
                        <span class="text-left text-error">
                            <xsl:value-of select="*:source"/>
                        </span>
                    </div>
                </div>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="*:otherTitleArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Other Title(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:otherTitle">
                        <span class="text-left">
                            <xsl:value-of select="*:title" disable-output-escaping="yes"/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:functionAndUse">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Function and Use:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="."/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:scaleNote">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Scale Note:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:numberingNote">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Numbering Note:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:generalNoteArray">
        <xsl:if test="./*">
            <div class="content-row">
                <xsl:attribute name="ng-if">
                    <xsl:value-of select="*:generalNote/*:note != ''"/>
                </xsl:attribute>
                <div class="col-xs-4 text-right">
                    <span>General Note(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:generalNote">
                        <span class="text-left">
                            <xsl:value-of select="*:note" disable-output-escaping="yes"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:arrangement">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Arrangement:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="."/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:accessRestriction">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Access Restriction(s):</span>
                </div>
                <div class="col-xs-8">
                    <span class="text-left">
                        <xsl:value-of select="replace(*:status/(*:termName)[1], '9999', '')"/>
                    </span>
                    <xsl:if test="*:specificAccessRestrictionArray">
                        <br/>
                        <span class="text-left"> Specific Access Restriction: </span>
                        <xsl:for-each select="*:specificAccessRestrictionArray/*:specificAccessRestriction">
                            <span class="text-left">
                                <xsl:value-of select="replace(*:restriction/(*:termName)[1], '9999', '')"/>
                                <xsl:if test="*:securityClassification">, <xsl:value-of
                                    select="replace(*:securityClassification/(*:termName)[1], '9999', '')"/></xsl:if>
                                <xsl:if test="position() != last()">
                                <xsl:text>, </xsl:text>
                            </xsl:if>
                            </span>
                        </xsl:for-each>
                    </xsl:if>
                    <xsl:if test="*:accessRestrictionNote">
                        <br/>
                        <span class="text-left"> Note: <xsl:value-of
                                select="*:accessRestrictionNote" disable-output-escaping="yes"/>
                        </span>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:useRestriction">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Use Restriction(s):</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="replace(*:status/(*:termName)[1], '9999', '')"/>
                </span>
                <xsl:if test="*:specificUseRestrictionArray">
                    <br/>
                    <span class="text-left"> Specific Use Restriction: </span>
                    <xsl:for-each select="*:specificUseRestrictionArray/*:specificUseRestriction">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            <xsl:if test="position() != last()">
                                <xsl:text>, </xsl:text>
                            </xsl:if>
                        </span>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="*:note">
                    <br/>
                    <span class="text-left"> Note: <xsl:value-of select="*:note"
                            disable-output-escaping="yes"/>
                    </span>
                </xsl:if>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:custodialHistoryNote">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Custodial History:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:transferNote">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Transfer Information:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="." disable-output-escaping="yes"/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:editStatus">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Edited:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:soundType">
        <div class="content-row">
            <div class="col-xs-4 text-right">
                <span>Sound Type:</span>
            </div>
            <div class="col-xs-8">
                <span class="text-left">
                    <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                </span>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="*:languageArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Language(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:language">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:accessionNumberArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Accession Number(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:accessionNumber">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:dispositionAuthorityNumberArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Disposition Authority Number(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:dispositionAuthorityNumber">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:recordsCenterTransferNumberArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Records Center Transfer
                        Number(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:recordsCenterTransferNumber">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:internalTransferNumberArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Internal Transfer Number(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:internalTransferNumber">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:microformPublicationArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Microform Publication(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:microformPublication">
                        <span class="text-left">
                            <xsl:value-of select="publication/identifier"/>
                            <br/>
                            <xsl:value-of select="note"/>
                            <br/>
                            <xsl:value-of select="publication/title"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:onlineResourceArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Online Resource(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:onlineResource">
                        <span class="text-left">
                                <a href="{(*:url)[1]}" target="_blank">
                                <xsl:value-of select="*:description"/>
                            </a>
                            <br/>
                        </span>
                        <br/>
                        <span class="text-left">
                            <xsl:value-of select="*:note" disable-output-escaping="yes"/>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="donorsArray">
        <xsl:if
            test="/*/*:personalDonorArray | /*/*:organizationalDonorArray | /*/*:archivalDescriptionsDonorArray">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Donor(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="/*/*:personalDonorArray/*:person">
                        <span class="text-left">
                            <a href="/id/{*:naId}" target="_blank">
                                <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            </a>
                            <br/>
                        </span>
                    </xsl:for-each>
                    <xsl:for-each select="/*/*:organizationalDonorArray/*:organizationName">
                        <span class="text-left">
                            <a href="/id/{*:naId}" target="_blank">
                                <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            </a>
                            <br/>
                        </span>
                    </xsl:for-each>
                    <xsl:for-each select="/*/*:archivalDescriptionsDonorArray/*:descriptionReference">
                        <span class="text-left">
                            <a href="/id/{*:naId}" target="_blank">
                                <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            </a>
                            <br/>
                        </span>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="subjects">
        <xsl:if
            test="/*/*:personalReferenceArray | /*/*:organizationalReferenceArray | /*/*:geographicReferenceArray | /*/*:specificRecordsTypeArray | /*/*:topicalSubjectArray">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Subjects Represented in the Archival
                        Material(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="/*/*:personalReferenceArray/*:person | /*/*:organizationalReferenceArray/*:organizationName | /*/*:geographicReferenceArray/*:geographicPlaceName | /*/*:specificRecordsTypeArray/*:specificRecordsType | /*/*:topicalSubjectArray/*:topicalSubject">
                        <span class="text-left">
                            <a href="/id/{*:naId}">
                                <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                            </a>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="contributors">
        <xsl:if test="/*/organizationalContributorArray | /*/personalContributorArray | /*/archivalDescriptionsContributorArray">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Contributors to Authorship and/or
                        Production of the Archival Material(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="/*/organizationalContributorArray/organizationalContributor">
                        <span class="text-left">
                            <a href="/id/{contributor/*:naId}">
                                <xsl:value-of
                                    select="replace(contributor/(*:termName)[1], '9999', '')"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of
                                    select="replace(contributorType/(*:termName)[1], '9999', '')"/>
                            </a>
                        </span>
                        <br/>
                    </xsl:for-each>
                    <xsl:for-each select="/*/personalContributorArray/personalContributor">
                        <span class="text-left">
                            <a href="/id/{contributor/*:naId}">
                                <xsl:value-of select="replace(contributor/(*:termName)[1], '9999', '')"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of select="replace(contributorType/(*:termName)[1], '9999', '')"/>
                            </a>
                        </span>
                        <br/>
                    </xsl:for-each>
                    <xsl:for-each select="/*/archivalDescriptionsContributorArray/descriptionReference">
                        <span class="text-left">
                            <a href="/id/{contributor/*:naId}">
                                <xsl:value-of select="replace(contributor/(*:termName)[1], '9999', '')"/>
                                <xsl:text>, </xsl:text>
                                <xsl:value-of
                                    select="replace(contributorType/(*:termName)[1], '9999', '')"/>
                            </a>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:formerRecordGroupArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Former Record Group(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:recordGroup">
                        <span class="text-left">
                            <a href="/id/{*:naId}">
                                <xsl:value-of select="*:title"/>
                            </a>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:formerCollectionArray">
        <xsl:if test=".">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Former Collection(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:collection">
                        <span class="text-left">
                            <a href="/id/{*:naId}">
                                <xsl:value-of select="*:title"/>
                            </a>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:generalRecordsTypeArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Type(s) of Archival Materials:</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:generalRecordsType">
                        <span class="text-left">
                            <xsl:value-of select="replace(((*:termName)[1])[1], '9999', '')"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:recordsCenterTransferArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Records Center Transfer
                        Number(s):</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:recordsCenterTransferNumber">
                        <span class="text-left">
                            <xsl:value-of select="replace((*:termName)[1], '9999', '')"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:broadcastDateArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>This item was broadcast:</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:proposableQualifiableDate">
                        <span class="text-left">
                            <xsl:call-template name="getDate">
                                <xsl:with-param name="dateObject" select="."/>
                            </xsl:call-template>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:copyrightDateArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>This item’s copyright was
                        established:</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:proposableQualifiableDate">
                        <span class="text-left">
                            <xsl:call-template name="getDate">
                                <xsl:with-param name="dateObject" select="."/>
                            </xsl:call-template>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:productionDateArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>This item was produced or
                        created:</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:proposableQualifiableDate">
                        <span class="text-left">
                            <xsl:call-template name="getDate">
                                <xsl:with-param name="dateObject" select="."/>
                            </xsl:call-template>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:releaseDateArray">
        <xsl:if test="./*">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>This item was released:</span>
                </div>
                <div class="col-xs-8">
                    <xsl:for-each select="*:proposableQualifiableDate">
                        <span class="text-left">
                            <xsl:call-template name="getDate">
                                <xsl:with-param name="dateObject" select="."/>
                            </xsl:call-template>
                        </span>
                        <br/>
                    </xsl:for-each>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="productionSeries">
        <xsl:if
            test="/*/*:productionSeriesTitle | /*/*:productionSeriesSubtitle | /*/*:productionSeriesNumber">
            <div class="content-row">
                <div class="col-xs-4 text-right">
                    <span>Production Series:</span>
                </div>
                <div class="col-xs-8">
                    <xsl:if test="/*/*:productionSeriesTitle">
                        <span class="text-left"> Title: <xsl:value-of
                                select="/*/*:productionSeriesTitle"/>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="/*/*:productionSeriesSubtitle">
                        <span class="text-left"> Subtitle: <xsl:value-of
                                select="/*/*:productionSeriesSubtitle"/>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="/*/*:productionSeriesNumber">
                        <span class="text-left"> Number: <xsl:value-of
                                select="/*/*:productionSeriesNumber"/>
                        </span>
                        <br/>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
