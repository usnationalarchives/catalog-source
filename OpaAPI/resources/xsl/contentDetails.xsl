<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:import href="descriptions/descriptions.xsl"/>
    <xsl:import href="authorityRecord/authorityRecord.xsl"/>
    <xsl:import href="common.xsl"/>
    <xsl:output method="html"/>
    <xsl:template match="/">
        
        <script type="">
            var DDI = false;
			var title = '';
        </script>
        <div class="contentDetails col-xs-12 noPaddingSides">
                    
			<!--DIFFERENT TOP-BAR FOR SMART PHONES AND PADS.-->
	       	<div class="top-bar visible-inline-xs visible-inline-sm">      
		      <div class="col-xs-12 paging pagination pagination-sm no-margin-top no-margin-bottom">
		         <div ng-show="currentPage > 0" class="text padding-10-top noPaddingSides text-right col-xs-5  col-lg-2"><span>Results: </span></div>
		         <div ng-show="currentPage > 0" class="noPaddingSides ">
		            <ul class="paging pagination pagination-sm ng-pristine ng-valid" data-ng-model="currentPage">
 							<li ng-class="{{disabledArrows: currentPage &lt;= 1}}">
                                <a href="" rel="#" class="padding-5-sides" ng-click="decreasePageNumber()" aria-label="Previous" ><span class="glyphicons glyphicons-chevron-left"/></a>
                            </li>
                            <li ng-class="{{disabledArrows: currentPage == 0}}">
                                <span ng-if="currentPage == 0" class="enabledOption">1</span>
                                <span ng-if="currentPage > 0" class="enabledOption">{{currentPage}}<span ng-show="totalResults"> of {{totalResults | number}}</span></span>
                            </li>
                            <li ng-class="{{disabledArrows: last() || currentPage == 0}}">
                                <a href="" rel="#" class="padding-5-sides" ng-click="increasePageNumber()" aria-label="Next"><span class="glyphicons glyphicons-chevron-right"/></a>
                            </li>		           
  					</ul>
		         </div>	         
		      </div>
		   	</div>     
        
            <div class="top-bar col-xs-12 hidden-inline-xs hidden-inline-sm">
                <div id="returnLink" class="col-xs-4 col-md-3">
                    <a ng-href="{{{{searchSvc.searchURL}}}}" ng-show="searchSvc.searchURL">
                        <span class="glyphicons glyphicons-chevron-left"></span>
                        <span> Return to Search Results</span>
                    </a>
                </div>
                <div class="col-xs-6 col-md-7 col-lg-6 noPaddingRight pull-right paging pagination pagination-sm no-margin-top no-margin-bottom">                                        
                    <div ng-show="currentPage > 0" class="text padding-10-top noPaddingSides text-right col-xs-3 col-md-2 col-lg-2"><span>Results: </span></div>
                    <div ng-show="currentPage > 0" class="noPaddingSides col-xs-5 col-md-4 col-lg-4">
                        <ul class="paging pagination pagination-sm" data-ng-model="currentPage">
                            <li ng-class="{{disabledArrows: currentPage &lt;= 1}}">
                                <a href="" rel="#" class="padding-5-sides" ng-click="decreasePageNumber()" aria-label="Previous" ><span class="glyphicons glyphicons-chevron-left"/></a>
                            </li>
                            <li ng-class="{{disabledArrows: currentPage == 0}}">
                                <span ng-if="currentPage == 0" class="enabledOption">1</span>
                                <span ng-if="currentPage > 0" class="enabledOption">{{currentPage}}<span ng-show="totalResults"> of {{totalResults | number}}</span></span>
                            </li>
                            <li ng-class="{{disabledArrows: last() || currentPage == 0}}">
                                <a href="" rel="#" class="padding-5-sides" ng-click="increasePageNumber()" aria-label="Next"><span class="glyphicons glyphicons-chevron-right"/></a>
                            </li>
                        </ul>
                         
                    </div>
                    <div data-multibar-actions-common="" class="col-xs-5 col-md-6 col-lg-6 pull-right"/>
                </div>
            </div>
            <div accordion-bar=""/>
            <div id="contentDetailSide" ng-class="{{'col-md-3': showSideBar, 'hidden-inline-md': !showSideBar }}"
                class="hidden-xs hidden-sm col-lg-2 bs-sidebar">
                <xsl:call-template name="recordHierarchy"/>
                <div id="tagsComments" class="side-box col-md-12">
                    <span class="title1 pull-left padding-10-left">Tag</span>

                    <div data-tags="" data-tag-list="Tags" data-add-tag="addTag"
                        data-tag-text="tagText" data-is-loged-in="isLoggedIn"
                        data-show-all-tags="showAllTags" data-check-user-tag="checkUserTag"
                        data-delete-tag="deleteTag" class="noMarginsSides col-xs-12 text-left"> </div>
						
					
					<div class="title1 pull-left padding-10-left">Comment</div>
					
					<div class="sideBoxComments">
						<button type="button" class="btn btn-link" ng-click="goToComments()" ng-bind="getTextGoToComments()"></button>
					</div>
						

                    <div class="col-xs-12 text-center footer-links margin-10-top">
                        <ul class="list-inline">
                            <li ng-hide="isLoggedIn">
                                <a href="/#/login">Login</a>
                            </li>
                            <li ng-hide="isLoggedIn" class="divider-vertical"/>
                            <li>
                                <a
                                    href="http://www.archives.gov/social-media/policies/tagging-policy.html"
                                    target="_blank">Policy</a>
                            </li>
                            <li class="divider-vertical"/>
                            <li>
                                <a href="https://www.archives.gov/citizen-archivist/faqs"
                                    target="_blank">Need Help?</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
            <div ng-class="{{'col-md-9': showSideBar, 'col-md-12': !showSideBar }}"
                class="col-sm-12 col-xs-12 col-lg-10 content">
                <a href="" rel="#" class="hide-label visible-md" ng-click="hideShowSideBar()">
                    <span>{{label}}</span>
                    <span class="fa" ng-class="{{'fa-angle-down': show, 'fa-angle-up': !show}}"/>
                </a>

                <xsl:choose>
                    <xsl:when
                        test="/*:person | /*:organization | /*:geographicPlaceName | /*:topicalSubject | /*:specificRecordsType">
                        <xsl:call-template name="authorityRecord"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="description"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:call-template name="comments"/>
            </div>
        </div>
    </xsl:template>
    <xsl:template match="text()">
    </xsl:template>
</xsl:stylesheet>
