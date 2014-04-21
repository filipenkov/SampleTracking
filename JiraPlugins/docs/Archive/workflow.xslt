<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- if nothing matches just copy it -->
<xsl:template match="*" id="match-all">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="function[@type='class']">
	<xsl:apply-templates select="arg[@name='class.name']"/>
	<xsl:text>
	</xsl:text>
	<xsl:apply-templates select="arg[not(@name='class.name')]"/>
</xsl:template>
<xsl:template match="arg[@name='class.name']">
	    <xsl:call-template name="string-replace-all">
	      <xsl:with-param name="text" select="text()" />
	      <xsl:with-param name="replace" select="'com.atlassian.jira.workflow'" />
	      <xsl:with-param name="by" select="'jira-workflow'" />
	    </xsl:call-template>
	</xsl:template>
	<xsl:template match="arg">
		<xsl:value-of select="@name"/>=<xsl:value-of select="text()"/>
	</xsl:template>

<xsl:template name="string-replace-all">
    <xsl:param name="text" />
    <xsl:param name="replace" />
    <xsl:param name="by" />
    <xsl:choose>
      <xsl:when test="contains($text, $replace)">
        <xsl:value-of select="substring-before($text,$replace)" />
        <xsl:value-of select="$by" />
        <xsl:call-template name="string-replace-all">
          <xsl:with-param name="text"
          select="substring-after($text,$replace)" />
          <xsl:with-param name="replace" select="$replace" />
          <xsl:with-param name="by" select="$by" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
