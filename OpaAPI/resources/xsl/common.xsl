<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="getDate">
        <xsl:param name="dateObject"/> <!-- dateQualifier => example ca. 1980? - ca. 1990? -->
        <xsl:if test="$dateObject/*:year != 9999">
            <xsl:if test="$dateObject/*:dateQualifier/*:termName != '?'"><xsl:value-of select="$dateObject/*:dateQualifier/*:termName"/><xsl:text> </xsl:text></xsl:if>
            <xsl:if test="$dateObject/*:month"><xsl:value-of select="$dateObject/*:month"/>/</xsl:if>
            <xsl:if test="$dateObject/*:day"><xsl:value-of select="$dateObject/*:day"/>/</xsl:if>
            <xsl:if test="$dateObject/*:year"><xsl:value-of select="$dateObject/*:year"/></xsl:if>
            <xsl:if test="$dateObject/*:dateQualifier/*:termName = '?'"><xsl:value-of select="$dateObject/*:dateQualifier/*:termName"/>
            </xsl:if>
        </xsl:if>
    </xsl:template>
   
    <xsl:template name="headerLinksMobile">
        <!-- FOOTER LINKS IN MOBILE -->
        <div class="top-bar col-xs-12  visible-inline-xs visible-inline-sm">
            <ul class="footer-links">
                <li ng-hide="offlineContent">
                    <span class="hidden-inline-xxs">Item </span>
                    <span ng-bind-template="{{{{currentIndex + 1}}}} of {{{{itemTotal}}}}"/>
                </li>
                <li class="divider-vertical" ng-hide="offlineContent"/>
                <li>
                    <a data-toggle="collapse" data-target="#xsdropdown"
                        class="content-toggle collapsed">More Info </a>
                </li>
                <li class="divider-vertical" ng-show="Auth.isLoggedIn()"/>
                <li ng-show="Auth.isLoggedIn()">
                    <a data-toggle="collapse" data-parent="#accordion" data-target="#listscollapse">
                        Lists </a>
                </li>
                <li class="divider-vertical"/>
                <li>
                    <a addthis-toolbox=""/>
                </li>
                <li class="divider-vertical"  ng-hide="offlineContent || DDI"/>
                    <li ng-hide="offlineContent || DDI">
                    <a ng-click="visorSvc.loadWorkspace(true, true)">Contribute <span
                        class="glyphicons glyphicons-chevron-right bold baseline" />
                    </a>
                </li>
            </ul>
        </div>
    </xsl:template>
    
    <xsl:template name="recordHierarchy">
        <xsl:if test="/*:recordGroup | /*:collection | /*:series | /*:fileUnit | /*:item | /*:itemAv">
            <div class="side-box col-md-12">
                <span class="title1 hidden-xs hidden-sm">Record Hierarchy</span>
                
                <div>
                    <div class="record-group col-xs-12">
                        <xsl:choose>
                            <xsl:when
                                test="/*:recordGroup | /*/*:parentRecordGroup | /*/*:parentSeries/*:parentRecordGroup | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup">
                                <span class="bold-martinique"> Record Group <xsl:value-of
                                    select="/*:recordGroup/*:recordGroupNumber | /*/*:parentRecordGroup/*:recordGroupNumber | /*/*:parentSeries/*:parentRecordGroup/*:recordGroupNumber | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:recordGroupNumber"
                                />: </span>
                                <br/>
                                <xsl:choose>
                                    <xsl:when test="/*:recordGroup">
                                        <span>
                                            <xsl:choose>
                                                <xsl:when test="/*:recordGroup/*:title">
                                                    <xsl:value-of select="/*:recordGroup/*:title" disable-output-escaping="yes"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="/*:recordGroup/*:naId"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </span>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a href="/id/{/*:recordGroup/*:naId | /*/*:parentRecordGroup/*:naId | /*/*:parentSeries/*:parentRecordGroup/*:naId | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:naId}">
                                            <xsl:choose>
                                                <xsl:when test="/*/*:parentRecordGroup/*:title | /*/*:parentSeries/*:parentRecordGroup/*:title | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:title">
                                                    <xsl:value-of select="/*/*:parentRecordGroup/*:title | /*/*:parentSeries/*:parentRecordGroup/*:title | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:title"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="/*/*:parentRecordGroup/*:naId | /*/*:parentSeries/*:parentRecordGroup/*:naId | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:naId"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:if test="/*:recordGroup/*:inclusiveDates | /*/*:parentRecordGroup/*:inclusiveDates | /*/*:parentSeries/*:parentRecordGroup/*:inclusiveDates | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:inclusiveDates">
                                                <xsl:text>, </xsl:text>
                                                <xsl:call-template name="getDate">
                                                    <xsl:with-param name="dateObject" select="/*:recordGroup/*/*:inclusiveStartDate | /*/*:parentRecordGroup/*/*:inclusiveStartDate | /*/*:parentSeries/*/*/*:inclusiveStartDate | /*/*:parentFileUnit/*/*/*/*:inclusiveStartDate"/>
                                                </xsl:call-template> - <xsl:call-template name="getDate">
                                                    <xsl:with-param name="dateObject" select="/*:recordGroup/*/*:inclusiveEndDate | /*/*:parentRecordGroup/*/*:inclusiveEndDate | /*/*:parentSeries/*/*/*:inclusiveEndDate | /*/*:parentFileUnit/*/*/*/*:inclusiveEndDate"/>
                                                </xsl:call-template>
                                            </xsl:if>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:when test="/*:collection | /*/*:parentCollection | /*/*:parentSeries/*:parentCollection | /*/*:parentFileUnit/*:parentSeries/*:parentCollection">
                                <span class="bold-martinique"> Collection <xsl:value-of
                                    select="/*:collection/*:collectionIdentifier | /*/*:parentCollection/*:collectionIdentifier | /*/*:parentSeries/*:parentCollection/*:collectionIdentifier | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:collectionIdentifier"
                                />: </span>
                                <br/>
                                <xsl:choose>
                                    <xsl:when test="/*:collection">
                                        <span>
                                            <xsl:choose>
                                                <xsl:when test="/*:collection/*:title">
                                                    <xsl:value-of select="/*:collection/*:title" disable-output-escaping="yes"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="/*:collection/*:naId"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </span>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <a
                                            href="/id/{/*:collection/*:naId | /*/*:parentCollection/*:naId | /*/*:parentSeries/*:parentCollection/*:naId | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:naId}">
                                            <xsl:choose>
                                                <xsl:when test="/*/*:parentCollection/*:title | /*/*:parentSeries/*:parentCollection/*:title | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:title">
                                                    <xsl:value-of select="/*/*:parentCollection/*:title | /*/*:parentSeries/*:parentCollection/*:title | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:title" disable-output-escaping="yes"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="/*/*:parentCollection/*:naId | /*/*:parentSeries/*:parentCollection/*:naId | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:naId" disable-output-escaping="yes"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                            <xsl:if test="/*:collection/*:inclusiveDates |/*/*:parentCollection/*:inclusiveDates | /*/*:parentSeries/*:parentCollection/*:inclusiveDates | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:inclusiveDates">
                                                <xsl:text>, </xsl:text>
                                                <xsl:call-template name="getDate">
                                                    <xsl:with-param name="dateObject" select="/*:collection/*/*:inclusiveStartDate | /*/*:parentCollection/*/*:inclusiveStartDate | /*/*:parentSeries/*/*/*:inclusiveStartDate | /*/*:parentFileUnit/*/*/*/*:inclusiveStartDate"/>
                                                </xsl:call-template> - <xsl:call-template name="getDate">
                                                    <xsl:with-param name="dateObject" select="/*:collection/*/*:inclusiveEndDate | /*/*:parentCollection/*/*:inclusiveEndDate | /*/*:parentSeries/*/*/*:inclusiveEndDate | /*/*:parentFileUnit/*/*/*/*:inclusiveEndDate"/>
                                                </xsl:call-template>
                                            </xsl:if>
                                        </a>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                        </xsl:choose>
                    </div>
                    <xsl:if test="/*:series | /*/*:parentSeries | /*/*:parentFileUnit/*:parentSeries">
                        <div class="record-group col-xs-11 col-xs-offset-1">
                            <span class="bold-martinique"> Series: </span>
							<br/>
                            <xsl:choose>
                                <xsl:when test="/*:series">
                                    <span>
                                        <xsl:choose>
                                            <xsl:when test="/*:series/*:title">
                                                <xsl:value-of select="/*:series/*:title" disable-output-escaping="yes"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="/*:series/*:naId"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:if test="/*:series/*:inclusiveDates">
                                            <xsl:text>, </xsl:text>
                                            <xsl:call-template name="getDate">
                                                <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveStartDate"/>
                                            </xsl:call-template> - <xsl:call-template name="getDate">
                                                <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveEndDate"/>
                                            </xsl:call-template>
                                        </xsl:if>
                                        <!--<span ng-show="recordHierarchy.level2.dates">, {{recordHierarchy.level2.dates}}</span>-->
                                    </span>
                                </xsl:when>
                                <xsl:otherwise>
                                    <a
                                        href="/id/{/*:series/*:naId | /*/*:parentSeries/*:naId | /*/*:parentFileUnit/*:parentSeries/*:naId}">
                                        <xsl:choose>
                                            <xsl:when test="/*/*:parentSeries/*:title | /*/*:parentFileUnit/*:parentSeries/*:title">
                                                <xsl:value-of select="/*/*:parentSeries/*:title | /*/*:parentFileUnit/*:parentSeries/*:title" disable-output-escaping="yes"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="/*/*:parentSeries/*:naId | /*/*:parentFileUnit/*:parentSeries/*:naId"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:if test="/*:series/*:inclusiveDates |  /*/*:parentSeries/*:inclusiveDates | /*/*:parentFileUnit/*:parentSeries/*:inclusiveDates">
                                            <xsl:text>, </xsl:text>
                                            <xsl:call-template name="getDate">
                                                <xsl:with-param name="dateObject" select="/*:series/*/*:inclusiveStartDate | /*/*:parentSeries/*/*:inclusiveStartDate | /*/*:parentFileUnit/*/*/*:inclusiveStartDate"/>
                                            </xsl:call-template> - <xsl:call-template name="getDate">
                                                <xsl:with-param name="dateObject" select="/*:series/*/*:inclusiveEndDate | /*/*:parentSeries/*/*:inclusiveEndDate | /*/*:parentFileUnit/*/*/*:inclusiveEndDate"/>
                                            </xsl:call-template>
                                        </xsl:if>                                            
                                        
                                        <!--<span ng-show="recordHierarchy.level2.dates">, {{recordHierarchy.level2.dates}}</span>-->
                                    </a>
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </xsl:if>
                    
                    <xsl:if test="/*/*:parentFileUnit | /*:fileUnit | /*:item | /*:itemAv">
                        <div class="record-group col-xs-10 col-xs-offset-2">
                            <span class="bold-martinique">
                                <xsl:choose>
                                    <xsl:when test="/*/*:parentFileUnit | /*:fileUnit"> File Unit: </xsl:when>
                                    <xsl:when test="/*:item | /*:itemAv"> Item: </xsl:when>
                                </xsl:choose>
                            </span>
                            <br/>
                            <xsl:choose>
                                <xsl:when test="/*/*:parentFileUnit">
                                    <a href="/id/{/*/*:parentFileUnit/*:naId}">
                                        <xsl:choose>
                                            <xsl:when test="/*/*:parentFileUnit/*:title">
                                                <xsl:value-of select="/*/*:parentFileUnit/*:title" disable-output-escaping="yes"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="/*/*:parentFileUnit/*:naId"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:if test="/*/*:parentFileUnit/*:productionDateArray">
                                            <xsl:text>, </xsl:text>
                                            <xsl:call-template name="getDate">
                                                <xsl:with-param name="dateObject" select="/*/*/*:proposableQualifiableDate"/>
                                            </xsl:call-template>
                                        </xsl:if>
                                    </a>
                                </xsl:when>
                                <xsl:otherwise>
                                    <span>
                                        <xsl:choose>
                                            <xsl:when test="/*/*:title">
                                                <xsl:value-of select="/*/*:title" disable-output-escaping="yes"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="/*/*:naId"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:if test="/*/*:productionDateArray">
                                            <xsl:text>, </xsl:text>
                                            <xsl:call-template name="getDate">
                                                <xsl:with-param name="dateObject" select="/*/*/*:proposableQualifiableDate"/>
                                            </xsl:call-template>
                                        </xsl:if>
                                    </span>
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </xsl:if>
                    
                    <xsl:if test="/*/*:parentFileUnit">
                        <div class="record-group col-xs-9 col-xs-offset-3">
                            <span class="bold-martinique"> Item: </span>
                            <br/>
                            <span>
                                <xsl:choose>
                                    <xsl:when test="/*/*:title">
                                        <xsl:value-of select="/*/*:title" disable-output-escaping="yes"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="/*/*:naId"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:if test="/*/*:productionDateArray">
                                    <xsl:text>, </xsl:text>
                                    <xsl:call-template name="getDate">
                                        <xsl:with-param name="dateObject" select="/*/*/*:proposableQualifiableDate"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </span>
                        </div>
                    </xsl:if>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@* | node()" mode="escape">
        <!-- Escape the apostrophes second -->
        <xsl:call-template name="replace">
            <xsl:with-param name="pTarget" select='"&apos;"' />
            <xsl:with-param name="pReplacement" select='"\&apos;"'/>
            <xsl:with-param name="pText">
                <!-- Escape the backslashes first, and then pass that result directly into the next template -->
                <xsl:call-template name="replace">
                    <xsl:with-param name="pTarget" select="'\'" />
                    <xsl:with-param name="pReplacement" select="'\\'" />
                    <xsl:with-param name="pText" select="." />
                </xsl:call-template>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="replace">
        <xsl:param name="pText"/>
        <xsl:param name="pTarget" select='"&apos;"'/>
        <xsl:param name="pReplacement" select="'\&quot;'"/>
        
        <xsl:if test="$pText">
            <xsl:value-of select='substring-before(concat($pText,$pTarget),$pTarget)'/>
            <xsl:if test='contains($pText, $pTarget)'>
                <xsl:value-of select='$pReplacement'/>
            </xsl:if>
            
            <xsl:call-template name="replace">
                <xsl:with-param name="pText" select='substring-after($pText, $pTarget)'/>
                <xsl:with-param name="pTarget" select="$pTarget"/>
                <xsl:with-param name="pReplacement" select="$pReplacement"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="comments">
        <div class="panel panel-default col-xs-12 hidden-xs hidden-sm borderless"  ng-controller="commentsController">
            <div ng-click="toggle('#comments','#commentsLink')" class="panel-heading">
                <span class="panel-title">
                    <a id="commentHeaderLink" href="" rel="#" data-toggle="collapse" data-target="#comments">
                        Comments on this <xsl:value-of select="translate($lvlOfDescriotion,'&#xA;','')"/> ({{commentsService.commentsCount}})
                    </a>
                </span>
                <span class="panel-title pull-right">
                    <a id="commentsLink" class="content-toggle" data-toggle="collapse"
                        data-target="#comments"/>
                </span>
            </div>
            <div id="comments" class="panel-collapse collapse in">
                <div class="panel-body">
                    <xsl:call-template name="commentsInner"/>
                </div>
            </div>
        </div>
    </xsl:template>
    
    <xsl:template name="commentsInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="$mobile">
            <span class="mobile-section">
                Comments on this <xsl:value-of select="translate($lvlOfDescriotion,'&#xA;','')"/>
            </span>
        </xsl:if>
        <div comments="comments"></div>
    </xsl:template>

</xsl:stylesheet>