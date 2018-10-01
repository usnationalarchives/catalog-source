<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
	<!--FINGERPRINT={FINGERPRINT}-->
    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>
    <xsl:template name="getDate">
        <xsl:param name="dateObject"/> <!-- dateQualifier => example ca. 1980? - ca. 1990? -->        
        <xsl:if test="$dateObject/*:dateQualifier/*:termName != '?'"><xsl:value-of select="$dateObject/*:dateQualifier/*:termName"/><xsl:text> </xsl:text></xsl:if>
        <xsl:if test="$dateObject/*:month"><xsl:value-of select="$dateObject/*:month"/>/</xsl:if>
        <xsl:if test="$dateObject/*:day"><xsl:value-of select="$dateObject/*:day"/>/</xsl:if>
        <xsl:if test="$dateObject/*:year"><xsl:value-of select="$dateObject/*:year"/></xsl:if>
        <xsl:if test="$dateObject/*:dateQualifier/*:termName = '?'"><xsl:value-of select="$dateObject/*:dateQualifier/*:termName"/></xsl:if>
    </xsl:template>
    <xsl:template match="*">
        <stringList>
            <xsl:if test="/*:series | /*:fileUnit | /*:item | /*:itemAv">
                <xsl:if test="/*/*:parentSeries | /*/*:parentFileUnit/*:parentSeries">
                    <value>
                        <xsl:text>Series: </xsl:text>
                        <xsl:choose>
                            <xsl:when test="/*:series">
                                <xsl:value-of select="/*:series/*:title" />
                                <xsl:if test="/*:series/*:inclusiveDates ">
                                    <xsl:text>, </xsl:text>
                                    <xsl:call-template name="getDate">
                                        <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveStartDate"/>
                                    </xsl:call-template> - <xsl:call-template name="getDate">
                                        <xsl:with-param name="dateObject" select="/*/*:inclusiveDates/*:inclusiveEndDate"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of
                                    select="/*/*:parentSeries/*:title | /*/*:parentFileUnit/*:parentSeries/*:title"/>
                                <xsl:if test="/*/*:parentSeries/*:inclusiveDates | /*/*:parentSeries/*:inclusiveDates">
                                    <xsl:text>, </xsl:text>
                                    <xsl:call-template name="getDate">
                                        <xsl:with-param name="dateObject" select="/*/*:parentSeries/*:inclusiveDates/*:inclusiveStartDate | /*/*:parentSeries/*:inclusiveDates/*:inclusiveStartDate"/>
                                    </xsl:call-template> - <xsl:call-template name="getDate">
                                        <xsl:with-param name="dateObject" select="/*/*:parentSeries/*:inclusiveDates/*:inclusiveEndDate | /*/*:parentSeries/*:inclusiveDates/*:inclusiveEndDate"/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                    </value>
                </xsl:if>
                <xsl:choose>
                    <xsl:when
                        test="/*:recordGroup | /*/*:parentRecordGroup | /*/*:parentSeries/*:parentRecordGroup | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup">
                        <value>
                            <xsl:text>Record Group </xsl:text>
                            <xsl:value-of
                                select="/*:recordGroup/*:recordGroupNumber | /*/*:parentRecordGroup/*:recordGroupNumber | /*/*:parentSeries/*:parentRecordGroup/*:recordGroupNumber | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:recordGroupNumber"/>
                            <xsl:text>: </xsl:text> 
                            <xsl:choose>
                                <xsl:when test="/*:recordGroup">
                                    <xsl:value-of select="/*:recordGroup/*:title"/>
                                    <xsl:if test="/*:recordGroup/*:inclusiveDates">
                                        <xsl:text>, </xsl:text>
                                        <xsl:call-template name="getDate">
                                            <xsl:with-param name="dateObject"
                                                select="*:inclusiveDates/*:inclusiveStartDate"/>
                                        </xsl:call-template> - <xsl:call-template name="getDate">
                                            <xsl:with-param name="dateObject"
                                                select="*:inclusiveDates/*:inclusiveEndDate"/>
                                        </xsl:call-template>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of
                                        select="/*/*:parentRecordGroup/*:title | /*/*:parentSeries/*:parentRecordGroup/*:title | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:title"/>
                                    <xsl:if test="/*/*:parentRecordGroup/*:inclusiveDates | /*/*:parentSeries/*:parentRecordGroup/*:inclusiveDates | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:inclusiveDates">
                                        <xsl:text>, </xsl:text>
                                        <xsl:call-template name="getDate">
                                            <xsl:with-param name="dateObject"
                                                select="/*/*:parentRecordGroup/*:inclusiveDates/*:inclusiveStartDate | /*/*:parentSeries/*:parentRecordGroup/*:inclusiveDates/*:inclusiveStartDate | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:inclusiveDates/*:inclusiveStartDate"/>
                                        </xsl:call-template> - <xsl:call-template name="getDate">
                                            <xsl:with-param name="dateObject"
                                                select="/*/*:parentRecordGroup/*:inclusiveDates/*:inclusiveEndDate | /*/*:parentSeries/*:parentRecordGroup/*:inclusiveDates/*:inclusiveEndDate | /*/*:parentFileUnit/*:parentSeries/*:parentRecordGroup/*:inclusiveDates/*:inclusiveEndDate"/>
                                        </xsl:call-template>

                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>
                        </value>
                    </xsl:when>
                    <xsl:when
                        test="/*:collection | /*/*:parentCollection | /*/*:parentSeries/*:parentCollection | /*/*:parentFileUnit/*:parentSeries/*:parentCollection">
                        <value>
                            <xsl:text>Collection </xsl:text>
                            <xsl:value-of
                                select="/*:collection/*:collectionIdentifier | /*/*:parentCollection/*:collectionIdentifier | /*/*:parentSeries/*:parentCollection/*:collectionIdentifier | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:collectionIdentifier"
                            />
                            <xsl:text>: </xsl:text>
                            <xsl:choose>
                                <xsl:when test="/*:collection">
                                        <xsl:value-of select="/*:collection/*:title"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of
                                        select="/*/*:parentCollection/*:title | /*/*:parentSeries/*:parentCollection/*:title | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:title"
                                    />
                                    
                                    <xsl:if test="/*:parentCollection/*:inclusiveDates | /*/*:parentSeries/*:parentCollection/*:inclusiveDates | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:inclusiveDates">
                                        <xsl:text>, </xsl:text>
                                        <xsl:call-template name="getDate">
                                            <xsl:with-param name="dateObject" select="/*:parentCollection/*:inclusiveDates/*:inclusiveStartDate | /*/*:parentSeries/*:parentCollection/*:inclusiveDates/*:inclusiveStartDate | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:inclusiveDates/*:inclusiveStartDate"/>
                                        </xsl:call-template> - <xsl:call-template name="getDate">
                                            <xsl:with-param name="dateObject" select="/*:parentCollection/*:inclusiveDates/*:inclusiveEndDate | /*/*:parentSeries/*:parentCollection/*:inclusiveDates/*:inclusiveEndDate | /*/*:parentFileUnit/*:parentSeries/*:parentCollection/*:inclusiveDates/*:inclusiveEndDate"/>
                                        </xsl:call-template>
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>
                        </value>
                    </xsl:when>
                </xsl:choose>
            </xsl:if>
        </stringList>
    </xsl:template>
    <xsl:template match="text()"/>
</xsl:stylesheet>