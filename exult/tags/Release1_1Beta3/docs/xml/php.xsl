<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/1999/xhtml">

<xsl:output method="xml"
	indent="no"/>

<!-- Keys -->
<xsl:key name="sub_ref" match="sub" use="@name"/>
<xsl:key name="section_ref" match="section" use="@title"/>

<xsl:strip-space elements="*"/>


<!-- FAQ Templates -->
<xsl:template name="TOC">
	<xsl:for-each select="section">
		<p>
		<a><xsl:attribute name="href">#<xsl:value-of select="@title"/></xsl:attribute>
			<xsl:number level="multiple"
						count="section"
						format="1. "
						value="position() -1"/>
				<xsl:value-of select="@title"/>
		</a>
		<br/>
		<xsl:for-each select="sub">
			<a><xsl:attribute name="href">#<xsl:value-of select="@name"/></xsl:attribute>
				<xsl:number level="multiple"
							count="section|sub"
							format="1."
							value="count(ancestor::section/preceding-sibling::section)"/>
				<xsl:number format="1. "/>
				<xsl:apply-templates select="header"/>
			</a>
			<br/>
		</xsl:for-each>
		</p>
	</xsl:for-each>
</xsl:template>

<!-- FAQ Template -->
<xsl:template match="faqs">
<test>
	<p>last changed: <xsl:value-of select="@changed"/></p>
	<hr/>
	<p>
		A text only version can be found <a href="http://exult.sourceforge.net/faq.txt">here</a>
	</p>
	<br/>

	<!-- BEGIN TOC -->
	<xsl:call-template name="TOC"/>
	<!-- END TOC -->

	<!-- BEGIN CONTENT -->
	<xsl:apply-templates select="section"/>
	<!-- END CONTENT -->
</test>
</xsl:template>

<!-- Readme Template -->
<xsl:template match="readme">
<test>
	<p>last changed: <xsl:value-of select="@changed"/></p>
	<hr/>
	<p>
		A text only version can be found <a href="http://exult.sourceforge.net/docs.txt">here</a>
	</p>
	<br/>

	<!-- BEGIN TOC -->
	<xsl:call-template name="TOC"/>
	<!-- END TOC -->

	<!-- BEGIN CONTENT -->
	<xsl:apply-templates select="section"/>
	<!-- END CONTENT -->
</test>
</xsl:template>

<!-- Studio Docs Template -->
<xsl:template match="studiodoc">
<test>
	<p>last changed: <xsl:value-of select="@changed"/></p>
	<hr/>
	<p>
		A text only version can be found <a href="http://exult.sourceforge.net/exult_studio.txt">here</a>
	</p>
	<br/>

	<!-- BEGIN TOC -->
	<xsl:call-template name="TOC"/>
	<!-- END TOC -->

	<!-- BEGIN CONTENT -->
	<xsl:apply-templates select="section"/>
	<!-- END CONTENT -->
</test>
</xsl:template>

<!-- Group Template -->
<xsl:template match="section">
	<hr width="100%"/>
	<table width="100%">
		<tr><th align="left">
			<a><xsl:attribute name="name"><xsl:value-of select="@title"/></xsl:attribute>
				<xsl:number format="1. "
				value="position() -1"/>
				<xsl:value-of select="@title"/>
			</a>
		</th></tr>
		<xsl:apply-templates select="sub"/>
	</table>
</xsl:template>


<!-- Entry Template -->
<xsl:template match="sub">
	<xsl:variable name = "num_idx">
		<xsl:number level="single"
					count="section"
					format="1."
					value="count(ancestor::section/preceding-sibling::section)"/>
		<xsl:number format="1. "/>
	</xsl:variable>
	<tr><td><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td></tr>
	<tr><td><strong>
		<a><xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:value-of select="$num_idx"/>
			<xsl:apply-templates select="header"/>
		</a>
	</strong></td></tr>
	<tr><td><xsl:apply-templates select="body"/></td></tr>
</xsl:template>


<xsl:template match="header">
<!--
	<xsl:variable name = "data">
		<xsl:apply-templates/>
	</xsl:variable>
	<xsl:value-of select="normalize-space($data)"/>
 -->
	<xsl:apply-templates/>
</xsl:template>


<xsl:template match="body">
<!--
	<xsl:variable name = "data">
		<xsl:apply-templates/>
	</xsl:variable>
	<xsl:value-of select="normalize-space($data)"/>
 -->
	<xsl:apply-templates/>
</xsl:template>


<!-- Internal Link Templates -->
<xsl:template match="ref">
	<a><xsl:attribute name="href">#<xsl:value-of select="@target"/></xsl:attribute>
		<xsl:value-of select="count(key('sub_ref',@target)/parent::section/preceding-sibling::section)"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="count(key('sub_ref',@target)/preceding-sibling::sub)+1"/>
		<xsl:text>.</xsl:text>
	</a>
</xsl:template>


<xsl:template match="ref1">
	<a><xsl:attribute name="href">#<xsl:value-of select="@target"/></xsl:attribute>
		<xsl:value-of select="count(key('sub_ref',@target)/parent::section/preceding-sibling::section)"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="count(key('sub_ref',@target)/preceding-sibling::sub)+1"/>
		<xsl:text>. </xsl:text>
		<xsl:apply-templates select="key('sub_ref',@target)/child::header"/>
	</a>
</xsl:template>


<xsl:template match="section_ref">
	<a><xsl:attribute name="href">#<xsl:value-of select="@target"/></xsl:attribute>
		<xsl:value-of select="count(key('section_ref',@target)/preceding-sibling::section)"/>
		<xsl:text>. </xsl:text>
  		<xsl:apply-templates select="key('section_ref',@target)/@title"/>
	</a>
</xsl:template>


<!-- External Link Template -->
<xsl:template match="extref">
	<a>
	<xsl:attribute name="href">
		<xsl:choose>
			<xsl:when test="@doc='faq'">
				<xsl:text>faq.php#</xsl:text>
			</xsl:when>
			<xsl:when test="@doc='docs'">
				<xsl:text>docs.php#</xsl:text>
			</xsl:when>
			<xsl:when test="@doc='studio'">
				<xsl:text>studio.php#</xsl:text>
			</xsl:when>
		</xsl:choose>
		<xsl:value-of select="@target"/>
	</xsl:attribute>
	<xsl:choose>
		<xsl:when test="count(child::node())>0">
				<xsl:value-of select="."/>
		</xsl:when>
		<xsl:when test="@doc='faq'">
			<xsl:text>FAQ</xsl:text>
		</xsl:when>
		<xsl:when test="@doc='docs'">
			<xsl:text>Documentation</xsl:text>
		</xsl:when>
		<xsl:when test="@doc='studio'">
			<xsl:text>Studio Documentation</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="@target"/>
		</xsl:otherwise>
	</xsl:choose>
	</a>
</xsl:template>

<!-- Image Link Template -->
<xsl:template match="img">
	<xsl:copy>
		<xsl:for-each select="@*|node()">
	<xsl:copy/>
	</xsl:for-each>
	</xsl:copy>	
</xsl:template>

<!-- Misc Templates -->
<xsl:template match="Exult">
	<em>Exult</em>
</xsl:template>

<xsl:template match="Studio">
	<em>Exult Studio</em>
</xsl:template>

<xsl:template match="Pentagram">
	<em>Pentagram</em>
</xsl:template>

<xsl:template match="cite">
                <p>
                <xsl:value-of select="@name"/>:<br/>
                <cite><xsl:apply-templates/></cite>
                </p>
</xsl:template>

<xsl:template match="para">
	<p><xsl:apply-templates/></p>
</xsl:template>


<xsl:template match="key">
	'<font color="maroon"><xsl:value-of select="."/></font>'
</xsl:template>


<xsl:template match="kbd">
	<font color="maroon"><kbd><xsl:value-of select="."/></kbd></font>
</xsl:template>


<!-- ...................ol|dl|ul + em............... -->
<xsl:template match="ul|ol|li|strong|q|br">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="em">
 <b><i><font size="+1">
<xsl:apply-templates/>
</font></i></b>
</xsl:template>

<!-- Key Command Templates -->
<xsl:template match="keytable">
	<table border="0" cellpadding="0" cellspacing="2" width="80%">
		<tr>
			<th colspan="3" align="left">
				<xsl:value-of select="@title"/>
			</th>
		</tr>
		<xsl:apply-templates select="keydesc"/>
	</table>
</xsl:template>


<xsl:template match="keydesc">
	<tr>
		<td nowrap="nowrap" valign="top">
			<font color="maroon"><xsl:value-of select="@name"/></font>
		</td>
		<td width="10"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></td>
		<td><xsl:value-of select="."/></td>
	</tr>
</xsl:template>


<!-- Config Table Templates -->
<xsl:template match="configdesc">
	<table border="0" cellpadding="0" cellspacing="0">
		<xsl:apply-templates select="line"/>
	</table>
</xsl:template>


<xsl:template match="line">
	<tr>
	<xsl:choose>
		<xsl:when test="count(child::comment)>0">
			<td width="150">
				<xsl:value-of select="text()"/>
			</td>
			<xsl:apply-templates select="comment"/>
		</xsl:when>
		<xsl:otherwise>
			<td colspan="2"><xsl:value-of select="."/></td>
		</xsl:otherwise>
	</xsl:choose>
	</tr>
</xsl:template>


<xsl:template match="comment">
	<td rowspan="2">
		<xsl:apply-templates/>
	</td>
</xsl:template>



</xsl:stylesheet>