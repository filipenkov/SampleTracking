<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Below is some pretty insane XSLT to convert the DateParameters to be RelativeDateRangeParameter -->
    <xsl:template
            match="parameter[@class='com.atlassian.jira.issue.search.parameters.lucene.PeriodParameter' and */@name = concat(local-name(*), ':previous')]">
        <xsl:call-template name="newParam">
            <xsl:with-param name="nextOffset">
                <xsl:value-of
                        select="../parameter[@class='com.atlassian.jira.issue.search.parameters.lucene.PeriodParameter' and */@name=concat(local-name(current()/*), ':next')]/*/@value"/>
            </xsl:with-param>
            <xsl:with-param name="previousOffset">
                <xsl:value-of select="*/@value"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>


    <xsl:template
            match="parameter[@class='com.atlassian.jira.issue.search.parameters.lucene.PeriodParameter' and concat(local-name(*), ':next') = */@name]">

        <!-- If we don't have a matching :previous node -->
        <xsl:choose>
            <xsl:when
                    test="../parameter[generate-id(.)!=generate-id(current())]/*/@name = concat(local-name(*), ':previous')"/>
            <xsl:otherwise>
                <xsl:call-template name="newParam">
                    <xsl:with-param name="nextOffset">
                        <xsl:value-of select="*/@value"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


    <xsl:template name="newParam">
        <xsl:param name="previousOffset"/>
        <xsl:param name="nextOffset"/>
        <parameter class='com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter'>
            <xsl:element name="{name(*)}">
                <xsl:attribute name="name">
                    <xsl:value-of select="concat(local-name(*),':relative')"/>
                </xsl:attribute>
                <xsl:if test="$previousOffset and $previousOffset != ''">
                    <previousOffset>
                        <xsl:value-of select="$previousOffset"/>
                    </previousOffset>
                </xsl:if>
                <xsl:if test="$nextOffset and $nextOffset != ''">
                    <nextOffset>
                        <xsl:value-of select="$nextOffset"/>
                    </nextOffset>
                </xsl:if>
            </xsl:element>
        </parameter>

    </xsl:template>

    <xsl:template match="node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
