<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="parameter[issue_author_group]/@class">
		<xsl:attribute name="class">com.atlassian.jira.issue.search.parameters.lucene.UsersGroupParameter</xsl:attribute>
	</xsl:template>

	<xsl:template match="parameter[issue_assignee_group]/@class">
		<xsl:attribute name="class">com.atlassian.jira.issue.search.parameters.lucene.UsersGroupParameter</xsl:attribute>
	</xsl:template>

	<xsl:template match="issue_author_group/@name">
		<xsl:attribute name="userLuceneFieldName">issue_author</xsl:attribute>
	</xsl:template>

	<xsl:template match="issue_assignee_group/@name">
		<xsl:attribute name="userLuceneFieldName">issue_assignee</xsl:attribute>
	</xsl:template>

	<xsl:template match="issue_author_group/@value">
		<xsl:attribute name="groupName"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>

	<xsl:template match="issue_assignee_group/@value">
		<xsl:attribute name="groupName"><xsl:value-of select="."/></xsl:attribute>
	</xsl:template>

	<xsl:template match="node() | @*">
		<xsl:copy>
			<xsl:apply-templates select="node() | @*"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
