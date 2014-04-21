<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <!-- Below is some pretty insane XSLT to convert the DateParameters to be AbsoluteDatePeriodParameters -->
    <xsl:template
            match="parameter[@class='com.atlassian.jira.issue.search.parameters.lucene.DateParameter' and */@name = concat(local-name(*), ':before')]">
        <xsl:call-template name="newParam">
            <xsl:with-param name="from">
                <xsl:value-of
                        select="../parameter[@class='com.atlassian.jira.issue.search.parameters.lucene.DateParameter' and */@name=concat(local-name(current()/*), ':after')]/*/@timestamp"/>
            </xsl:with-param>
            <xsl:with-param name="to">
                <xsl:value-of select="*/@timestamp"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>


    <xsl:template
            match="parameter[@class='com.atlassian.jira.issue.search.parameters.lucene.DateParameter' and concat(local-name(*), ':after') = */@name]">

        <!-- If we don't have a matching :before node -->
        <xsl:choose>
            <xsl:when
                    test="../parameter[generate-id(.)!=generate-id(current())]/*/@name = concat(local-name(*), ':before')"/>
            <xsl:otherwise>
                <xsl:call-template name="newParam">
                    <xsl:with-param name="from">
                        <xsl:value-of select="*/@timestamp"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>


    <xsl:template name="newParam">
        <xsl:param name="from"/>
        <xsl:param name="to"/>
        <parameter class='com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter'>
            <xsl:element name="{name(*)}">
                <xsl:attribute name="name">
                    <xsl:value-of select="concat(local-name(*),':absolute')"/>
                </xsl:attribute>
                <xsl:if test="$from and $from != ''">
                    <fromDate>
                        <xsl:value-of select="$from"/>
                    </fromDate>
                </xsl:if>
                <xsl:if test="$to and $to != ''">
                    <toDate>
                        <xsl:value-of select="$to"/>
                    </toDate>
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
