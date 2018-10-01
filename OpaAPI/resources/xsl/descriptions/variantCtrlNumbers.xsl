<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    exclude-result-prefixes="xs math"
    version="2.0">
<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:template name="variantCtrlNumber">
        <div class="panel panel-default col-xs-12 borderless hidden-xs hidden-sm">
            <div ng-click="toggle('#variantControl', '#variantControlLink')" class="panel-heading">
                <span class="panel-title">
                    <a href="" rel="#" data-toggle="collapse" data-target="#variantControl">Variant Control
                        Numbers</a>
                </span>
                <span class="panel-title pull-right">
                    <a id="variantControlLink" class="content-toggle" data-toggle="collapse" data-target="#variantControl"/>
                </span>
            </div>
            <div id="variantControl" class="panel-collapse collapse in">
                <div class="panel-body">
                    <xsl:call-template name="variantCtrlNumberInner"/>
                </div>
            </div>
        </div>
    </xsl:template>
    <xsl:template name="variantCtrlNumberInner">
        <xsl:param name="mobile" select="false()"/>
        <xsl:if test="$mobile">
            <span class="mobile-section">
                Variant Control Numbers
            </span>
        </xsl:if>
        <table class="table table-condensed">
            <col width="33.33%"/>
            <col width="66.66%"/>
            <tbody>
                <xsl:call-template name="VCN"/>
                <xsl:apply-templates select="/*/*:variantControlNumberArray"/>
            </tbody>
        </table>
    </xsl:template>
    <xsl:template name="VCN">
        <tr>
            <td class="text-right">
                <span class="text-right bold-martinique">ARC Identifier:</span>
            </td>
            <td colspan="2">
                <span class="text-left text-error"><xsl:value-of select="/*/*:naId" disable-output-escaping="yes"/></span>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="*:variantControlNumberArray">
        <xsl:for-each select="*:variantControlNumber">
            <xsl:if test="*:type/(*:termName)[1]">
                <tr>
                    <xsl:attribute name="ng-if">
                        <xsl:value-of select="*:note != '' or *:number != ''"/>
                    </xsl:attribute>
                    <td class="text-right">
                        <span class="text-right bold-martinique">
                            <xsl:value-of select="replace(*:type/(*:termName)[1], '9999', '')"/>
                            <xsl:text>:</xsl:text>
                        </span>
                    </td>
                    <td colspan="2">
                        <span class="text-left text-error">
                            <xsl:value-of select="*:number"/>
                        </span>
                        <xsl:if test="*:note">
                            <br/>
                            <span class="text-left text-error">
                                <xsl:value-of select="*:note" disable-output-escaping="yes"/>
                            </span>
                        </xsl:if>
                    </td>
                </tr>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>