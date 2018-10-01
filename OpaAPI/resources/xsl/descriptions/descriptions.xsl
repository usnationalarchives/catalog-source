<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
                version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:import href="additionalInfo.xsl"/>
    <xsl:import href="details.xsl"/>
    <xsl:import href="scopeAndContent.xsl"/>
    <xsl:import href="variantCtrlNumbers.xsl"/>
    <xsl:import href="archivedCopies.xsl"/>
    <xsl:import href="containerList.xsl"/>
    <xsl:import href="shotList.xsl"/>
    <xsl:import href="../common.xsl"/>
    <xsl:import href="electronicRecords.xsl"/>
    <xsl:output method="html"/>
    <xsl:variable name="lvlOfDescriotion">
        <xsl:choose>
            <xsl:when test="local-name(/*) = 'item'">
                Item
            </xsl:when>
            <xsl:when test="local-name(/*) = 'itemAv'">
                Item
            </xsl:when>
            <xsl:when test="local-name(/*) = 'series'">
                Series
            </xsl:when>
            <xsl:when test="local-name(/*) = 'collection'">
                Collection
            </xsl:when>
            <xsl:when test="local-name(/*) = 'fileUnit'">
                File Unit
            </xsl:when>
            <xsl:when test="local-name(/*) = 'recordGroup'">
                Record Group
            </xsl:when>
        </xsl:choose>
    </xsl:variable>
    <xsl:template name="description">

        <div class="content-header col-xs-12 hidden-xs hidden-sm">
            <h2>
                <xsl:choose>
                    <xsl:when test="/*/*:title">
                        <xsl:value-of select="/*/*:title" disable-output-escaping="yes"/>
                        <xsl:if test="/*/*:subtitle">
                            <xsl:text>: </xsl:text>
                            <xsl:value-of select="/*/*:subtitle" disable-output-escaping="yes"/>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="/*/*:naId" disable-output-escaping="yes"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="/*/*:inclusiveDates">
                    <xsl:text>, </xsl:text>
                    <xsl:call-template name="getDate">
                        <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveStartDate"/>
                    </xsl:call-template> - <xsl:call-template name="getDate">
                    <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveEndDate"/>
                </xsl:call-template>
                </xsl:if>
            </h2>
        </div>

        <!--CUSTOM HEADER FOR MOBILE-->
        <div class="col-xs-12 visible-xs visible-sm">
            <div class="content-header panel collapsed" ng-click="toogleHeader()"
                 data-toggle="collapse" data-target="#detailsMobile">
                <span class="mobile-header"><xsl:value-of select="/*/*:title" disable-output-escaping="yes"/></span>
                <span class="glyphicons pull-right angleHeader"
                      ng-class="{{'glyphicons-chevron-down': !expanded, 'glyphicons-chevron-up': expanded}}"/>
            </div>

            <!-- DETAILS FOR MOBILE -->
            <div id="detailsMobile" class="panel-collapse collapse">
                <div class="panel-body">
                    <xsl:call-template name="aditionalInfoInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="detailsInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="scopeAndContentInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="variantCtrlNumberInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="archivedCopiesInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="containerListInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                    <xsl:call-template name="shotListInner">
                        <xsl:with-param name="mobile" select="true()"/>
                    </xsl:call-template>
                </div>
            </div>

            <!-- FOOTER LINKS IN MOBILE -->
            <xsl:call-template name="headerLinksMobile"/>

            <!-- METADATA FOR MOBILE -->
            <!--  <div class="visible-xs visible-sm">-->
            <div id="xsdropdown" class="panel-collapse collapse">
                <!--TAGS MOBILE-->
                <div class="panel panel-default col-xs-12 borderless">
                    <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#tagsCollapse"
                        ng-click="toggle('#tagsCollapse', '#tagsCollapseLink', '', 'facet-toggle')">
                        <a class="panel-title">Tags</a>
                        <span class="panel-title pull-right">
                            <a class="facet-toggle collapsed" id="tagsCollapseLink"></a>
                        </span>
                    </div>
                    <div id="tagsCollapse" class="panel-collapse collapse">
                        <div class="panel-body">
                            <div data-tags="list-inline" data-add-tag="addTag" tag-list="Tags" data-tag-text="tagText"
                                 data-is-loged-in="isLoggedIn" data-show-all-tags="showAllTags"
                                 data-check-user-tag="checkUserTag" data-delete-tag="deleteTag"
                                 class="noMarginsSides tagList col-xs-12">
                            </div>
                        </div>
                    </div>
                </div>
                <!-- HIERARCHY -->
                <xsl:if
                    test="/*:recordGroup | /*:collection | /*:series | /*:fileUnit | /*:item | /*:itemAv">
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#recordHierarchyCollapse"
                            ng-click="toggle('#recordHierarchyCollapse', '#recordHierarchyCollapseLink', '', 'facet-toggle')" >
                            <a class="panel-title">Record Hierarchy</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="recordHierarchyCollapseLink"></a>
                            </span>
                        </div>
                        <div id="recordHierarchyCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <xsl:call-template name="recordHierarchy"/>
                            </div>
                        </div>
                    </div>
                </xsl:if>
                <!--ADDITIONAL DETAILS-->
                <div class="panel panel-default col-xs-12 borderless">
                    <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#additionalInfoCollapse"
                        ng-click="toggle('#additionalInfoCollapse', '#additionalInfoCollapseLink', '', 'facet-toggle')" >
                        <a class="panel-title">Additional Details</a>
                        <span class="panel-title pull-right">
                            <a class="facet-toggle collapsed" id="additionalInfoCollapseLink"></a>
                        </span>
                    </div>
                    <div id="additionalInfoCollapse" class="panel-collapse collapse">
                        <div class="panel-body">
                            <xsl:call-template name="aditionalInfoInner"/>
                        </div>
                    </div>
                </div>
                <!--DETAILS-->
                <div class="panel panel-default col-xs-12 borderless">
                    <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#detailsCollapse"
                        ng-click="toggle('#detailsCollapse', '#detailsCollapseLink', '', 'facet-toggle')" >
                        <a class="panel-title">Details</a>
                        <span class="panel-title pull-right">
                            <a class="facet-toggle collapsed" id="detailsCollapseLink"></a>
                        </span>
                    </div>
                    <div id="detailsCollapse" class="panel-collapse collapse">
                        <div class="panel-body">
                            <xsl:call-template name="detailsInner"/>
                        </div>
                    </div>
                </div>
                <!--SCOPE AND CONTENT-->
                <xsl:if test="/*/*:scopeAndContentNote">
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#scopeAndContentCollapse"
                            ng-click="toggle('#scopeAndContentCollapse', '#scopeAndContentCollapseLink', '', 'facet-toggle')" >
                            <a class="panel-title">Scope &amp; Content</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="scopeAndContentCollapseLink"></a>
                            </span>
                        </div>
                       <div id="scopeAndContentCollapse" class="panel-collapse collapse">
                           <div class="panel-body">
                               <xsl:call-template name="scopeAndContentInner"/>
                           </div>
                       </div>
                   </div>
                </xsl:if>
                <!--VARIANT CONTROL NUMBERS-->
                <div class="panel panel-default col-xs-12 borderless">
                    <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#variantCtrlNumberCollapse"
                        ng-click="toggle('#variantCtrlNumberCollapse', '#variantCtrlNumberCollapseLink', '', 'facet-toggle')" >
                        <a class="panel-title">Variant Control Numbers</a>
                        <span class="panel-title pull-right">
                            <a class="facet-toggle collapsed" id="variantCtrlNumberCollapseLink"></a>
                        </span>
                    </div>
                    <div id="variantCtrlNumberCollapse" class="panel-collapse collapse">
                        <div class="panel-body">
                            <xsl:call-template name="variantCtrlNumberInner"/>
                        </div>
                    </div>
                </div>
                <!--ARCHIVED COPIES-->
                <xsl:if test="/*/*:physicalOccurrenceArray">
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#archivedCopiesCollapse"
                            ng-click="toggle('#archivedCopiesCollapse', '#archivedCopiesCollapseLink', '', 'facet-toggle')" >
                            <a class="panel-title">Archived Copies</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="archivedCopiesCollapseLink"></a>
                            </span>
                        </div>
                        <div id="archivedCopiesCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <xsl:call-template name="archivedCopiesInner"/>
                            </div>
                        </div>
                    </div>
                </xsl:if>
                <!--CONTAINER LIST-->
                <xsl:if test="/*/*:physicalOccurrenceArray/*/*:containerList">
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#containerListCollapse"
                            ng-click="toggle('#containerListCollapse', '#containerListCollapseLink', '', 'facet-toggle')" >
                            <a class="panel-title">Container List</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="containerListCollapseLink"></a>
                            </span>
                        </div>
                        <div id="containerListCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <xsl:call-template name="containerListInner"/>
                            </div>
                        </div>
                    </div>
                </xsl:if>
                <!--SHOT LIST-->
                <xsl:if test="/*/*:shotList">
                    <div class="panel panel-default col-xs-12 borderless">
                        <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#shotListCollapse"
                            ng-click="toggle('#shotListCollapse', '#shotListCollapseLink', '', 'facet-toggle')" >
                            <a class="panel-title">Shot List</a>
                            <span class="panel-title pull-right">
                                <a class="facet-toggle collapsed" id="shotListCollapseLink"></a>
                            </span>
                        </div>
                        <div id="shotListCollapse" class="panel-collapse collapse">
                            <div class="panel-body">
                                <xsl:call-template name="shotListInner"/>
                            </div>
                        </div>
                    </div>
                </xsl:if>                
                <!--COMMENTS-->
                <div class="panel panel-default col-xs-12 borderless" ng-controller="commentsController">
                    <div class="panel-heading" ng-show="true" data-toggle="collapse" data-target="#commentsCollapse"
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
            <!-- </div> -->
        </div>

        <xsl:if test="/*/*:variantControlNumberArray">
            <xsl:for-each select="/*/*:variantControlNumberArray/*">
                <xsl:if test="*:number = 'DDI'">
                    <script type="">
                        DDI = true;
                    </script>
                    <div data-electronic-records=""></div>
                    <xsl:if test="//technicalAccessRequirementsNote">
                        <span class="text-left bold-martinique">Technical Access Requirements Note: </span>
                        <xsl:value-of select="//technicalAccessRequirementsNote" disable-output-escaping="yes"/>
                        <br/>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>

        <!--VISOR-->
        <div id="openSeaDragonVisor" ng-if="showVisor" class="col-xs-12">
            <div data-visor="false" class="col-xs-12 dataVisor"></div>

            <div id="openSeadragonNavigator" class="navigator-container col-xs-12" ng-if="thumbs.length > 0">
                <div class="navigator-buttons col-xs-12 text-left">
                    <ul class="list-unstyled list-inline">
                        <li ng-repeat="thumb in thumbs">
                            <a href="" rel="#" ng-click="changeMedia(objectIndex(thumb))">
                                <div class="img-with-text">
                                    <span ng-click="showWorkspace(objectIndex(thumb))" ng-if="thumb['@hasAnnotation']" class="glyphicons glyphicons-tag flip bluetag" title="Has user contributions"></span>
                                    <div>
                                        <img ng-src="{{{{thumb.thumbnail.thumbnailFile}}}}" alt="{{{{thumb['@id']}}}}" height="auto"/>                                    
                                    </div>
                                    <p ng-class="{{'is-selected': objectIndex(thumb) === currentIndex &amp;&amp; !docVisor }}">{{objectIndex(thumb) + 1}}</p>
                                </div>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="navigator-buttons col-xs-12 text-center">
                    <button ng-click="loadMore()" ng-show="totalObjects>maxThumbs" class="btn btn-default">Load More</button>
                    <button ng-click="loadAll()" ng-show="totalObjects>maxThumbs" class="btn btn-default">Load All</button>
                </div>
            </div>
        </div>

		<xsl:if test="/*/*:online-availability">
			<xsl:for-each select="/*/*:online-availability">
				<xsl:choose>
					<xsl:when test="/*/*:online-availability[@enabled = 'true']">
						<xsl:if test="/*/*:online-availability != ''">
							<div id="offlineContent" class="col-xs-12">
								<span class="glyphicons glyphicons-alert warning-text"></span>&#160;
								<xsl:value-of select="/*/*:online-availability"
									disable-output-escaping="yes" />
							</div>
						</xsl:if>
						<xsl:if test="/*/*:online-availability = ''">
							<div id="offlineContent" class="col-xs-12" ng-show="offlineContent">
							<span class="warning-text">
					            <span class="glyphicons glyphicons-alert warning-text"></span> This<xsl:choose>
					                <xsl:when test="name(/*) = 'item'"> Item </xsl:when>
					                <xsl:when test="name(/*) = 'itemAv'"> Item </xsl:when>
					                <xsl:when test="name(/*) = 'series'"> Series </xsl:when>
					                <xsl:when test="name(/*) = 'collection'"> Collection </xsl:when>
					                <xsl:when test="name(/*) = 'fileUnit'"> File Unit </xsl:when>
					                <xsl:when test="name(/*) = 'recordGroup'"> Record Group </xsl:when>
					            </xsl:choose>contains records, some of which may not be available online.</span>
					            <br/>
					            <span class="text">To obtain a copy or view the records, please contact or visit the
					                National Archives and Records Administration location(s) listed in the Contact
					                information below.</span>
					        </div>
						</xsl:if>
					</xsl:when>
				</xsl:choose>
			</xsl:for-each>
		</xsl:if>

        <!--DOCUMENTS PANEL-->
        <div class="panel panel-default col-xs-12" ng-if="docs.length > 0">
            <div ng-click="toggle('#documents','#documentsLink')" class="panel-heading">
                <span class="panel-title">
                    <a href="" rel="#" data-toggle="collapse" data-target="#documents"
                            >Documents</a>
                </span>
                <span class="panel-title pull-right">
                    <a id="documentsLink" class="content-toggle" data-toggle="collapse" data-target="#documents"/>
                </span>
            </div>
            <div id="documents" class="panel-collapse collapse in">
                <div class="panel-body">
                    <ul class="list-unstyled list-inline">
                        <li ng-repeat="doc in docs">                            
                            <div class="img-with-text" ng-if="!doc.downloadOnly">								
                                <a href="" rel="#" ng-click="changeMedia(objectIndex(doc))">
                                    <span ng-click="showWorkspace(objectIndex(doc))" ng-if="doc['@hasAnnotation']" class="glyphicons glyphicons-tag flip bluetag" title="Has user contributions"></span>
                                    <div>
                                        <span class="{{{{doc.thumbnail.thumbnailFile}}}} x3"></span>
                                    </div>
                                </a>
                            </div>
                            <div class="img-with-text" ng-if="doc.downloadOnly">
                                <div>
                                    <a ng-if="doc.downloadOnly" href="{{{{doc.href}}}}" target="_blank">
										<!--><img ng-src="{{{{doc.thumbnail.thumbnailFile}}}}" alt="{{{{doc['@id']}}}}" width="64px" height="auto"/><!-->
										<div>
											<span class="{{{{doc.thumbnail.thumbnailFile}}}} x3"></span>
										</div>
									</a>
                                </div>
                                <a href="{{{{doc.href}}}}" target="_blank">{{decodeURI(doc.file['@name'])}}</a>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <!--PANEL ADDITIONAL INFO-->
        <xsl:call-template name="aditionalInfo"/>
        <!--PANEL DETAILS-->
        <xsl:call-template name="details"/>
        <!--PANEL SCOPE AND CONTENT-->
        <xsl:call-template name="scopeAndContent"/>
        <!--PANEL VARIANT CONTROL NUMBERS-->
        <xsl:call-template name="variantCtrlNumber"/>
        <!--PANEL ARCHIVED COPIES-->
        <xsl:call-template name="archivedCopies"/>
        <!--PANEL CONTAINER LIST-->
        <xsl:call-template name="containerList"/>
        <!--PANEL SHOT LIST-->
        <xsl:call-template name="shotList"/>
    </xsl:template>
</xsl:stylesheet>
