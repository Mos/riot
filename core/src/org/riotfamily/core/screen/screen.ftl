<@template.root>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Riot | ${FormatUtils.stripTagsAndSpaces(context.title)}</title>
	<@riot.scripts srcs=[
		"jquery/jquery.js",
		"jquery/ui/jquery-ui.js",
		"jquery/jquery.fixpngs.js",
		"riot/notification/notification.js"] 
	/>
	<@riot.stylesheets hrefs=[
		"jquery/ui/jquery-ui.css",
		"riot/notification/notification.css",
		"style/common.css"
		] + (customStyleSheets![]) + (template.vars.stylesheets![])
	/>
	
</head>
<body class="${template.vars.bodyClass!"screen"}">
	<div id="page">
		<div id="header">
			<@template.block name="header">
				<@renderPath path!context.path />	
			</@template.block>
			<div id="logo"></div>
		</div>
		<div id="content">
			<@template.block name="content">
				<div class="main">
					<@template.block name="main" />	
				</div>
				<div id="extra" class="extra">
					<@template.block name="extra">
					</@template.block>
					<@renderScreenlets />
				</div>
			</@template.block>
		</div>
	</div>
	<div id="footer">
		<div id="footer-content">
			<a href="${c.urlForHandler('logoutController')}" class="logout"><@c.message "logout">Logout</@c.message></a>
		</div>
	</div>
	<script>
	   /*
		new PeriodicalExecuter(function() {
			new Ajax.Request('${c.resolve(riot.resource("/ping"))}');
		}, 180);
		*/
	</script>
	<#if notification??>
		<script language="JavaScript" type="text/javascript">
			riot.showNotification(${FormatUtils.toJSON(notification)});
		</script>
	</#if>
</body>
</html>
</@template.root>

<#macro renderScreenlets>
	<#if context.screen.screenlets??>
		<div id="screenlets">
			<#list context.screen.screenlets as screenlet>
				${screenlet.render(context)}
			</#list>
		</div>
	</#if>
</#macro>

<#macro renderPath path>
	<div id="path">
		<#list path as link>
			<#if link_has_next>
		 	 	<a href="${c.url(link.url)}" class="screen<#if link_index == 0> first</#if>"><@renderLabel link /></a><#t>
			<#else>
				<span class="screen <#if link_index == 0> first</#if>"><@renderLabel link true /></span><#t>
			</#if>
		</#list>
	</div>
</#macro>

<#macro renderLabel link active=false>
	<b<#if active> class="active"</#if>><b><#if link.icon??><span class="icon" style="${riot.iconStyle(link.icon)}"></span></#if><span class="title<#if link.new> new</#if>">${link.title!}</span></b></b><#t>
</#macro>