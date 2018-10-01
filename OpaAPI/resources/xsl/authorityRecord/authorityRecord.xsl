<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
                version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="html"/>
    <xsl:template name="authorityRecord">
        <div class="content-header col-xs-12 hidden-xs hidden-sm">
            <h2>
                <xsl:value-of select="/*/*:termName" disable-output-escaping="yes"/>
                <xsl:if test="/*/*:inclusiveDates">
                    <xsl:text>, </xsl:text>
                    <xsl:call-template name="getDate">
                        <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveStartDate"/>
                    </xsl:call-template> - <xsl:call-template name="getDate">
                    <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveEndDate"/>
                </xsl:call-template>
                </xsl:if>
            </h2>
            <xsl:call-template name="authorityType"/>
        </div>

        <!--<xsl:variable name="escapedTitle">
            <xsl:apply-templates select="/*/*:termName" mode="escape"/>
        </xsl:variable>
        <script type="">
            title = '<xsl:value-of select="translate($escapedTitle,'&#xA;','')"/>';
        </script>-->

        <!--CUSTOM HEADER FOR MOBILE-->
        <div class="col-xs-12 visible-xs visible-sm">
            <div class="content-header panel" ng-click="toogleHeader()"
                data-toggle="collapse" data-target="#detailsMobile">
                <span><xsl:value-of select="/*/*:termName" disable-output-escaping="yes"/></span>
                <span class="glyphicons pull-right angleHeader"
                    ng-class="{{'glyphicons-chevron-down': !expanded, 'glyphicons-chevron-up': expanded}}"/>
                <xsl:call-template name="authorityType"/>
            </div>

            <!-- DETAILS FOR MOBILE -->
            <div id="detailsMobile" class="panel-collapse collapse in">
                <div class="panel-body">
                    <xsl:call-template name="authorityRecordInner"/>
                </div>
            </div>

            <!-- FOOTER LINKS IN MOBILE -->
            <xsl:call-template name="headerLinksMobile"/>
            

            <!-- METADATA FOR MOBILE -->
            <!--  <div class="visible-xs visible-sm">-->
            <div class="visible-xs visible-sm">
                <div id="xsdropdown" class="panel-collapse collapse">
                    <!--TAGS MOBILE-->
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse"
                            data-target="#tagsCollapse"
                            ng-click="toggle('#tagsCollapse', '#tagsCollapseLink', '', 'facet-toggle')">
                            <a class="panel-title">Tags</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="tagsCollapseLink"/>
                            </span>
                        </div>
                        <div id="tagsCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <div data-tags="list-inline" data-add-tag="addTag" tag-list="Tags"
                                    data-tag-text="tagText" data-is-loged-in="isLoggedIn"
                                    data-show-all-tags="showAllTags"
                                    data-check-user-tag="checkUserTag" data-delete-tag="deleteTag"
                                    class="noMarginsSides tagList col-xs-12"> </div>
                            </div>
                        </div>
                    </div>
                    <!--ADDITIONAL DETAILS-->
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse"
                            data-target="#additionalInfoCollapse"
                            ng-click="toggle('#additionalInfoCollapse', '#additionalInfoCollapseLink', '', 'facet-toggle')">
                            <a class="panel-title">Additional Details</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="additionalInfoCollapseLink"/>
                            </span>
                        </div>
                        <div id="additionalInfoCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <xsl:call-template name="authorityRecordInner"/>
                            </div>
                        </div>
                    </div>
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse" 
                            data-target="#commentsCollapse"
                            ng-click="toggle('#commentsCollapse', '#commentsCollapseLink', '', 'facet-toggle')" >
                            <a class="panel-title">Comments</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="commentsCollapseLink"></a>
                            </span>
                        </div>
                        <div id="commentsCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <xsl:call-template name="commentsInner"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="panel panel-default col-xs-12 borderless hidden-xs hidden-sm">
            <div id="additionalInfo" class="panel-collapse collapse in">
                <div class="panel-body">
                    <xsl:call-template name="authorityRecordInner"/>
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template name="authorityRecordInner">
        <table class="table table-condensed">
            <tbody>
                <colgroup>
                    <col class="opaCol1"/>
                    <col class="opaCol2"/>
                </colgroup>
                <xsl:if test="/*:person | /*:topicalSubject | /*:geographicPlaceName | /*:specificRecordsType">
                    <tr>
                        <td class="text-right">
                            <xsl:choose>
                                <xsl:when test="/*:person">
                                    <span class="text-right bold-martinique">Person
                                        Name:</span>
                                </xsl:when>
                                <!--<xsl:when test="/*:organization">
                                    <span class="text-right bold-martinique">Organization
                                        Name:</span>
                                </xsl:when>-->
                                <xsl:when test="/*:topicalSubject">
                                    <span class="text-right bold-martinique">Topical
                                        Subject:</span>
                                </xsl:when>
                                <xsl:when test="/*:geographicPlaceName">
                                    <span class="text-right bold-martinique">Geographic
                                        Subject:</span>
                                </xsl:when>
                                <xsl:when test="/*:specificRecordsType">
                                    <span class="text-right bold-martinique">Specific
                                        Records Type:</span>
                                </xsl:when>
                            </xsl:choose>
                        </td>

                        <td>
                            <span class="text-left">
                                <xsl:value-of select="/*/*:termName" disable-output-escaping="yes"/>
                            </span>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="/*:person">
                        <xsl:call-template name="roles">
                            <xsl:with-param name="currNode" select="/*:person" tunnel="yes"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="/*:organization"/>
                    <xsl:otherwise>
                        <xsl:call-template name="referencedIn"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="organizationNameArray"/>
                <xsl:if test="/*:geographicPlaceName">
                    <xsl:call-template name="jurisdictionOf"/>
                </xsl:if>
                <xsl:apply-templates select="/*/*:nonPreferredHeadingArray"/>
                <xsl:apply-templates select="/*/*:broaderTermArray"/>
                <xsl:apply-templates select="/*/*:narrowerTermArray"/>
                <xsl:apply-templates select="/*/*:relatedTermArray"/>
                <xsl:apply-templates select="/*/*:latitudelongitude"/>
                <xsl:apply-templates select="/*/*:biographicalNote"/>
                <!--<xsl:apply-templates select="/*/*:predecessorArray"/> for each organization
                <xsl:apply-templates select="/*/*:successorArray"/> for each organization 
                <xsl:apply-templates select="/*/*:variantNameArray"/> for each organization -->
                <xsl:apply-templates select="/*/*:programAreaArray"/> <!-- outside organizationNameArray-->
                <xsl:apply-templates select="/*/*:personalReferenceArray"/> <!-- outside organizationNameArray -->
                <xsl:apply-templates select="/*/*:jurisdictionArray"/> <!--outside organizationNameArray-->
                <xsl:call-template name="administrativeHistoryNotes"/> <!-- outside organizationNameArray -->
                <!--<xsl:apply-templates select="/*/*:scopeNote"/>--> <!-- Removed scopeNote by NARAOPA-293 -->
            </tbody>
        </table>
    </xsl:template>
    <xsl:template name="roles">
        <xsl:param name="currNode" tunnel="yes" />
        <xsl:variable name="relatedSum">
            <xsl:value-of
                    select="number($currNode/*:linkCounts/*:contributorLinkCount)
                + number($currNode/*:linkCounts/*:creatorLinkCount)
                + number($currNode/*:linkCounts/*:subjectLinkCount)
                + number($currNode/*:linkCounts/*:donorLinkCount)"/>
        </xsl:variable>
        <xsl:if test="number($relatedSum) > 0">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Role(s):</span>
                </td>
                <td>
                    <span class="text-left">
                        <a href="/search?q=*:*&amp;f.allAuthorityIds={$currNode/*:naId}">
                            Related to <xsl:value-of select="$relatedSum"/> catalog description(s)
                        </a>
                    </span>
                    <br/>
                    <xsl:if test="number($currNode/*:linkCounts/*:contributorLinkCount) > 0">
                        <span class="text-left">
                            <a href="/search?q=*:*&amp;f.level=(series or item or fileUnit)&amp;f.contributorIds={$currNode/*:naId}">
                                Contributor in <xsl:value-of select="number($currNode/*:linkCounts/*:contributorLinkCount)"/> description(s)
                            </a>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="number($currNode/*:linkCounts/*:creatorLinkCount) > 0">
                        <span class="text-left">
                            <a
                                    href="/search?q=*:*&amp;f.level=series&amp;f.creatorIds={$currNode/*:naId}"
                                    > Created <xsl:value-of select="number($currNode/*:linkCounts/*:creatorLinkCount)"/> series
                            </a>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="number($currNode/*:linkCounts/*:subjectLinkCount) > 0">
                        <span class="text-left">
                            <a
                                    href="/search?q=*:*&amp;f.level=(series or fileUnit or item)&amp;f.subjectIds={$currNode/*:naId}"
                                    > Subject in <xsl:value-of select="number($currNode/*:linkCounts/*:subjectLinkCount)"/> description(s) </a>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="number($currNode/*:linkCounts/*:donorLinkCount) > 0">
                        <span class="text-left">
                            <a
                                    href="/search?q=*:*&amp;f.level=collection&amp;f.donorIds={$currNode/*:naId}"
                                    > Donor of <xsl:value-of select="number($currNode/*:linkCounts/*:donorLinkCount)"/> collection(s) </a>
                        </span>
                        <br/>
                    </xsl:if>
                    <xsl:if test="number($currNode/*:linkCounts/*:organizationAuthorityLinkCount) > 0 and name($currNode) = 'person'">
                        <span class="text-left">
                            <a
                                    href="/search?q=*:*&amp;f.type=organization&amp;f.personalReferenceIds={$currNode/naId}"
                                    > Personal Reference in <xsl:value-of select="number($currNode/*:linkCounts/*:organizationAuthorityLinkCount)"/> organization(s) </a>
                        </span>
                        <br/>
                    </xsl:if>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="organizationNameArray">
        <xsl:if test="/*/*:organizationNameArray">
            <xsl:for-each select="/*/*:organizationNameArray/*:organizationName">
                <tr>
                    <td class="text-right">
                        <span class="text-right bold-martinique">Organization Name:</span>
                    </td>
                    <td>
                        <span class="text-left">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </span>
                        <br/>
                    </td>
                </tr>
                <xsl:call-template name="roles">
                    <xsl:with-param name="currNode" select="." tunnel="yes"/>
                </xsl:call-template>
                <xsl:apply-templates select="predecessorArray"/>
                <xsl:apply-templates select="successorArray"/>
                <xsl:apply-templates select="variantNameArray"/>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:nonPreferredHeadingArray">
        <tr>
            <td class="text-right">
                <xsl:choose>
                    <xsl:when test="/*:person | /*:organization">
                        <span class="text-right bold-martinique">Variant Name(s):</span>
                    </xsl:when>
                    <xsl:otherwise>
                        <span class="text-right bold-martinique">Use for:</span>
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td>
                <xsl:for-each select="*">
                    <span class="text-left">
                        <xsl:value-of select="replace(*:termName, '9999', '')"/>
                    </span>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:biographicalNote">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Biographical Note:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="replace(text(), '\n', '&lt;br/>')" disable-output-escaping="yes"/>
                </span>
                <br/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:programAreaArray">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Program Area:</span>
            </td>
            <td>
                <xsl:for-each select="*:programArea">
                    <span class="text-left">
                        <xsl:value-of select="replace(*:termName, '9999', '')"/>
                    </span>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:variantNameArray">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Variant Name(s):</span>
            </td>
            <td>
                <xsl:for-each select="*">
                    <span class="text-left">
                        <xsl:value-of select="*:name"/>
                    </span>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:predecessorArray">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Predecessor(s):</span>
            </td>
            <td>
                <xsl:for-each select="*">
                    <span class="text-left">
                        <a href="/id/{*:naId}" target="_blank">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </a>
                    </span>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:successorArray">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Successor(s):</span>
            </td>
            <td>
                <xsl:for-each select="*">
                    <span class="text-left">
                        <a href="/id/{*:naId}" target="_blank">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </a>
                    </span>
                    <br/>
                </xsl:for-each>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:personalReferenceArray">
        <xsl:if test="./*">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Personal Reference(s):</span>
                </td>
                <td>
                    <xsl:for-each select="*:person">
                        <span class="text-left">
                            <a href="/id/{naId}" target="_blank">
                                <xsl:value-of select="replace(*:termName, '9999', '')"/>
                            </a>
                        </span>

                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:jurisdictionArray">
        <xsl:if test="./*">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Jurisdiction(s):</span>
                </td>
                <td>
                    <xsl:for-each select="*">
                        <span class="text-left">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="administrativeHistoryNotes">
        <xsl:if test="/*/*:administrativeHistoryNote">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Administrative History Note:</span>
                </td>
                <td>
                    <xsl:for-each select="/*/*:administrativeHistoryNote">
                        <span class="text-left">
                            <xsl:value-of select="replace(., '\n', '&lt;br/>')" disable-output-escaping="yes"/>                            
                        </span>
                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:scopeNote">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Scope Note:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="replace(text(), '\\n', '&lt;br/>')" disable-output-escaping="yes"/>
                </span>
                <br/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template name="referencedIn">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Referenced in:</span>
            </td>
            <td>
                <span class="text-left">
                    <a href="/search?q=*:*&amp;f.level=(series or item or fileUnit)&amp;f.subjectIds={/*/*:naId}">
                        <xsl:value-of select="/*/*:linkCounts/*:approvedDescriptionLinkCount "/> catalog description(s)</a>
                </span>
                <br/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:broaderTermArray">
        <xsl:if test="./*">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Broader Term(s):</span>
                </td>
                <td>
                    <xsl:for-each select="*">
                        <span class="text-left">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:narrowerTermArray">
        <xsl:if test="./*">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Narrower Term(s):</span>
                </td>
                <td>
                    <xsl:for-each select="*">
                        <span class="text-left">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:relatedTermArray">
        <xsl:if test="./*">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Related Term(s):</span>
                </td>
                <td>
                    <xsl:for-each select="*">
                        <span class="text-left">
                            <xsl:value-of select="replace(*:termName, '9999', '')"/>
                        </span>
                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template name="jurisdictionOf">
        <xsl:if test="number(/*/*:linkCounts/*:organizationAuthorityLinkCount) &gt; 0">
            <tr>
                <td class="text-right">
                    <span class="text-right bold-martinique">Jurisdiction of:</span>
                </td>
                <td>
                    <xsl:for-each select="*">
                        <a class="text-left" href="/search?q=authority.organization.jurisdictionArray.geographicPlaceName.naId:{/*/*:naId}">
                            <xsl:value-of select="number(/*/*:linkCounts/*:organizationAuthorityLinkCount)" /> organization(s)</a>
                        <br/>
                    </xsl:for-each>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="*:latitudelongitude">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">Coordinates:</span>
            </td>
            <td>
                <span class="text-left">
                    <xsl:value-of select="."/>
                </span>
                <br/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template name="authorityType">
        <xsl:choose>
            <xsl:when test="/*:person">
                <h2 class="sub">Person Authority Record</h2>
            </xsl:when>
            <xsl:when test="/*:organization">
                <h2 class="sub">Organization Authority Record</h2>
            </xsl:when>
            <xsl:when test="/*:geographicPlaceName">
                <h2 class="sub">Geographic Subject Authority Record</h2>
            </xsl:when>
            <xsl:when test="/*:topicalSubject">
                <h2 class="sub">Topical Subject Authority Record</h2>
            </xsl:when>
            <xsl:when test="/*:specificRecordsType">
                <h2 class="sub">Specific Records Type Authority Record</h2>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>