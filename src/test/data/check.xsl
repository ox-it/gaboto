<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:tei="http://www.tei-c.org/ns/1.0"
  version="2.0"
>

<xsl:key name="I" use="@xml:id" match="*"/>

<xsl:output method="xml" indent="yes" encoding="utf-8"/>


<xsl:template match="/">
  <xsl:for-each select="//tei:relation">
    <xsl:if test="not(key('I',substring-after(@passive,'#')))">
      <xsl:message>cannot find anything to point <xsl:value-of
      select="@passive"/> at</xsl:message>
    </xsl:if>
    <xsl:if test="not(key('I',substring-after(@active,'#')))">
      <xsl:message>cannot find anything to point <xsl:value-of
      select="@active"/> at</xsl:message>
    </xsl:if>
  </xsl:for-each>
</xsl:template>


</xsl:stylesheet>
