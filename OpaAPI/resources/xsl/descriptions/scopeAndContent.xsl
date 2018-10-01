<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="scopeAndContent">
        <xsl:if test="/*/*:scopeAndContentNote">
            <div class="panel panel-default col-xs-12 borderless hidden-xs hidden-sm"
                ng-class="{{'hidden-xs' : showScopeContent, 'hidden-sm': showScopeContent}}">
                <div ng-click="toggle('#scopeContent', '#scopeContentLink')" class="panel-heading">
                    <span class="panel-title">
                        <a href="" rel="#" data-toggle="collapse" data-target="#scopeContent">Scope &amp;
                            Content</a>
                    </span>
                    <span class="panel-title pull-right">
                        <a id="scopeContentLink" class="content-toggle" data-toggle="collapse" data-target="#scopeContent"
                        />
                    </span>
                </div>
                <div id="scopeContent" class="panel-collapse collapse in">
                    <div class="panel-body">
                        <xsl:call-template name="scopeAndContentInner"/>
                    </div>
                </div>
            </div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="scopeAndContentInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="/*/*:scopeAndContentNote">
            <xsl:if test="$mobile">
                <span class="mobile-section"> Scope &amp; Content </span>
            </xsl:if>
            <table class="table table-condensed">
                <tbody>
                    <tr>
                        <td>
                            <span class="text-left">                                
                                <xsl:value-of select="replace(/*/*:scopeAndContentNote, '\n', '&lt;br/>')" disable-output-escaping="yes"/>
                            </span>
                        </td>
                    </tr>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
