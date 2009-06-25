<xsl:stylesheet 
  xmlns:tei="http://www.tei-c.org/ns/1.0"  
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="2.0"
>

<!-- identity transform -->
<xsl:output method="xml" indent="yes" encoding="utf-8"/>

<xsl:key name="N" match="obn" use="placeName"/>

<xsl:template match="@*|text()|comment()|processing-instruction()">
 <xsl:copy-of select="."/>
</xsl:template>


<xsl:template match="*">
  <xsl:copy>
    <xsl:apply-templates 
	select="*|@*|processing-instruction()|comment()|text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="tei:place[@type='building']">
  <xsl:copy>
    <xsl:apply-templates 
	select="@*"/>
    <xsl:variable name="name">
      <xsl:value-of select="tei:placeName"/>
    </xsl:variable>
    <xsl:variable name="id">
      <xsl:value-of select="@xml:id"/>
    </xsl:variable>
    <xsl:variable name="parentname">
      <xsl:value-of select="ancestor::tei:place[1]/tei:placeName"/>
    </xsl:variable>
    <xsl:if test="not(@obnCode)">
    <xsl:for-each select="document('BuildingList.xml')">
      <xsl:choose>
	<xsl:when test="count(key('N',$name))=1">
	  <xsl:message>Found match for  <xsl:value-of select="$name"/></xsl:message>
	  <xsl:for-each select="key('N',$name)">
	    <xsl:attribute name="obnCode">
	      <xsl:value-of select="code"/>
	    </xsl:attribute>
	    <xsl:for-each select="construction">
	      <tei:event when="{.}" type="construction"/>
	    </xsl:for-each>
	    <xsl:for-each select="acquisition">
	      <tei:event when="{.}" type="acquisition"/>
	    </xsl:for-each>
	  </xsl:for-each>
	</xsl:when>
	<xsl:when test="count(key('N',$name))&gt;1">
	  <xsl:message>Found MULTIPLE matches for  <xsl:value-of
	  select="$name"/></xsl:message>
	</xsl:when>
	<xsl:when test="$name='Oxford Union Society'"/>
	<xsl:when test="$name='57 Woodstock Road'"/>
	<xsl:when test="$parentname='Blackfriars'"/>
	<xsl:when test="$parentname='Christ Church'"/>
	<xsl:when test="$parentname='Christ Church'"/>
	<xsl:when test="contains($parentname,'Greyfriar')"/>
	<xsl:when test="contains($parentname,'St Stephen')"/>
	<xsl:when test="$parentname='Christ Church Cathedral'"/>
	<xsl:when test="$parentname='Christ Church Picture Gallery'"/>
	<xsl:when test="$parentname='The University Church of St Mary the Virgin'"/>
	<xsl:when test="contains($name,' College') or
			contains($parentname,' College')"/>
	<xsl:when test="contains($name,' Hall') or contains($parentname,' Hall')"/>
	<xsl:when test="$name=''">
	  <xsl:message>NO MATCH. <xsl:value-of select="$id"/>. Empty name. Parent is <xsl:value-of select="$parentname"/></xsl:message>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:message>NO MATCH. <xsl:value-of
	  select="$id"/>. <xsl:value-of select="$name"/>. parent is <xsl:value-of select="$parentname"/></xsl:message>
	</xsl:otherwise>
      </xsl:choose>
	</xsl:for-each>
    </xsl:if>
    <xsl:apply-templates 
	select="*|processing-instruction()|comment()|text()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
