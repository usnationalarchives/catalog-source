<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="shotList">
        <xsl:if test="/*/*:shotList">
            <!--PANEL SHOT LIST-->
            <div class="panel panel-default col-xs-12 borderless hidden-xs hidden-sm"> <!-- ng-class="{{'hidden-sm' : showArcCop, 'hidden-sm': showArcCop}}"
                ng-show="showArcCop" -->
                <div ng-click="toggle('#shotList','#shotListLink')" class="panel-heading">
                    <span class="panel-title">
                        <a href="" rel="#" data-toggle="collapse" data-target="#shotList">Shot List</a>
                    </span>
                    <span class="panel-title pull-right">
                        <a id="shotListLink" class="content-toggle" data-toggle="collapse" data-target="#shotList"/>
                    </span>
                </div>
                <div id="shotList" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <xsl:call-template name="shotListInner"/>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="shotListInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="/*/*:shotList">
            <xsl:if test="$mobile">
                <span class="mobile-section"> Shot List </span>
            </xsl:if>
            <table>
                <tbody>
                    <tr>
                        <td>
                            <span class="text-left">
                                <xsl:value-of select="/*/*:shotList"/>
                            </span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>