<%--
 Copyright (c) 2000, 2003 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials 
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>

<% 
	ToolbarData data = new ToolbarData(application,request, response);
	WebappPreferences prefs = data.getPrefs();
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Toolbar", request)%></title>
 
<style type="text/css">

/* need this one for Mozilla */
HTML { 
	margin:0px;
	padding:0px;
}
 
BODY {
	background:<%=prefs.getToolbarBackground()%>;
}

#titleText {
	font-weight:bold;
}


td.button {
<%
if (data.isIE()) {
	// IE already has 3px padding
%>
	padding-left:2;
	padding-right:2;
<%
} else {
%>
	padding-left:5;
	padding-right:5;
<%
}
%>
}
 
A {
	display:block; 
}
.button a { 
	display:block; 
	width:<%=data.isMozilla()?18:20%>px;
	height:<%=data.isMozilla()?18:20%>px;
	border:1px solid <%=prefs.getToolbarBackground()%>;
	writing-mode:tb-rl;
	vertical-align:middle;
}

.button a:hover { 
	border-top:1px solid ButtonHighlight; 
	border-<%=isRTL?"right":"left"%>:1px solid ButtonHighlight; 
	border-<%=isRTL?"left":"right"%>:1px solid ButtonShadow; 
	border-bottom:1px solid ButtonShadow;
}

#container {
	border-bottom:1px solid ThreeDShadow;
<%
if (data.isIE()) {
%> 
<%
}else if (data.isMozilla()){
%>
	border-top:1px solid ThreeDShadow;
	height:24px;
<%
}
%>
}

<%
// workaround for adding right border on mozilla (ugly..)
if (data.isMozilla() && "content".equals(request.getParameter("toolbar"))) { 
%>

/* need this one for Mozilla */
HTML { 
	margin:0px;
	padding:0px;
	border-<%=isRTL?"left":"right"%>:1px solid ThreeDShadow;
}
<%
}
%>

</style>

<script language="JavaScript">

// Preload images
<%
ToolbarButton[] buttons = data.getButtons();
for (int i=0; i<buttons.length; i++) {
	if (!buttons[i].isSeparator()) {
%>
	var <%=buttons[i].getName()%> = new Image();
	<%=buttons[i].getName()%>.src = "<%=buttons[i].getOnImage()%>";
<%
	}
}
%>

function setTitle(label)
{
	if( label == null) label = "";
	var title = document.getElementById("titleText");
	if (title == null) return;
	var text = title.lastChild;
	if (text == null) return;
	text.nodeValue = label;
}


<% if (data.isIE() || data.isMozilla() && "1.2.1".compareTo(data.getMozillaVersion()) <=0){
%>/**
 * Handler for double click: maximize/restore this view
 * Note: Mozilla browsers prior to 1.2.1 do not support programmatic frame resizing well.
 */
function mouseDblClickHandler(e) {
	// ignore double click on buttons
	var target=<%=data.isIE()?"window.event.srcElement":"e.target"%>;
	if (target.tagName && (target.tagName == "A" || target.tagName == "IMG"))
		return;
		
	// get to the frameset
	var p = parent;
	while (p && !p.toggleFrame)
		p = p.parent;
	
	if (p!= null)
		p.toggleFrame('<%=data.getTitle()%>');
	
	document.selection.clear;	
	return false;
}
<%=data.isIE()?
	"document.ondblclick = mouseDblClickHandler;"
:
	"document.addEventListener('dblclick', mouseDblClickHandler, true);"%>
<%}%>

</script>

<%
if (data.getScript() != null) {
%>
<script language="JavaScript" src="<%=data.getScript()%>"></script>
<%
}
%>

</head>
 
<%
if(buttons.length > 0){
%>
	<body dir="<%=direction%>">
<%
}else{
%>
	<body dir="<%=direction%>" tabIndex="-1">
<%
}
%>

<table id="container" width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style='padding-left:<%=data.isIE()?"5px":"8px"%>;'>

	<tr>
		<td nowrap style="font: <%=prefs.getToolbarFont()%>" valign="middle">
			<div id="titleTextTableDiv" style="overflow:hidden; height:22px;"><table><tr><td nowrap style="font:<%=prefs.getToolbarFont()%>"><div id="titleText" >&nbsp;<%=data.getTitle()%></div></td></tr></table>
			</div>
		
		
		<div style="position:absolute; top:2px; <%=isRTL?"left":"right"%>:0px;">
		<table width="100%" border="0" cellspacing="1" cellpadding="0" height="100%">
			<tr>
				<td align="<%=isRTL?"left":"right"%>">
					<table border="0" cellspacing="0" cellpadding="0" height="100%" style="background:<%=prefs.getToolbarBackground()%>">
					<tr>
<%
	for (int i=0; i<buttons.length; i++) {
		if (buttons[i].isSeparator()) {
%>
						<td align="middle" valign="middle" width="9">
						</td>
<%
		} else {
%>
						<td align="middle" class="button" height=18>
							<a href="javascript:<%=buttons[i].getAction()%>('b<%=i%>');" 
							   onmouseover="window.status='<%=buttons[i].getTooltip()%>';return true;" 
							   onmouseout="window.status='';"
							   id="b<%=i%>">
							   <img src="<%=buttons[i].getOnImage()%>" 
									alt='<%=buttons[i].getTooltip()%>' 
									title='<%=buttons[i].getTooltip()%>' 
									border="0"
									style="float: left;"
									id="<%=buttons[i].getName()%>">
							</a>
						</td>
<%
		}
	}
%>				
					</tr>
					</table>
				</td>
			</tr>
		</table> 
		</div>
		</td>
	</tr>
</table>

<%// special case for content toolbar - internally used live help frame
if ("content".equals(request.getParameter("toolbar"))) {
%>
    <iframe name="liveHelpFrame" title="<%=ServletResources.getString("ignore", "liveHelpFrame", request)%>" style="visibility:hidden" tabindex="-1" frameborder="no" width="0" height="0" scrolling="no">
    </iframe>
<%
}
%>

</body>     
</html>

