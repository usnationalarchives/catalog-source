<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math" exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="containerList">
        <xsl:if test="/*/*:physicalOccurrenceArray/*/*:containerList">
            <!--PANEL CONTAINER LIST-->
            <div class="panel panel-default col-xs-12 borderless hidden-xs hidden-sm">
                <div ng-click="toggle('#containerList','#containerListLink')" class="panel-heading">
                    <span class="panel-title">
                        <a href="" rel="#" data-toggle="collapse" data-target="#containerList">Container List</a>
                    </span>
                    <span class="panel-title pull-right">
                        <a id="containerListLink" class="content-toggle" data-toggle="collapse" data-target="#containerList"/>
                    </span>
                </div>
                <div id="containerList" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <xsl:call-template name="containerListInner"/>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template name="containerListInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="/*/*:physicalOccurrenceArray/*/*:containerList">
            <xsl:if test="$mobile">
                <span class="mobile-section"> Variant Control Numbers </span>
            </xsl:if>
            <xsl:variable name="containerText">
                <xsl:value-of select="/*/*:physicalOccurrenceArray/*/*:containerList" disable-output-escaping="yes"/>
            </xsl:variable>
            <table class="table table-condensed">
                <tbody>
                    <tr>
                        <td>
                            <span class="text-left">
                                <pre class="containerList"><xsl:value-of select="string-join(/*/*:physicalOccurrenceArray/*/*:containerList,', ')" disable-output-escaping="yes"/></pre>
                            </span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>