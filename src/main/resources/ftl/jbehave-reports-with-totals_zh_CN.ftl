<#ftl strip_whitespace=true>
<#macro renderStat stats name class=""><#assign value = stats.get(name)!0><#if (value != 0)><span class="${class}">${value}</span><#else>&nbsp;</#if></#macro>
<#macro renderMillis stats name class=""><#assign millis = stats.get(name)!0><span class="${class}"><#assign time = timeFormatter.formatMillis(millis)>${time}</span></#macro>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta charset="UTF-8">
<title>JBehave 报告</title>
<style type="text/css" media="all">
#footer {
	width: 100%;
	background: #fff url(../images/background-footer.png) repeat;
	color: #fff;
	border: 1px solid;
	padding: 5px 0 5px 0;
	margin-top: 50px;
	font-size: 0.8em;
	position: fixed;
	bottom: 0;
}

#footer div.left {
	float: left;
}

#header {
	margin: 0 auto 0 auto;
	margin-left: 30px;
	margin-bottom: 40px;
}

body {
	margin: 0;
	font-family: "Microsoft YaHei",Arial,sans-serif;
	font-size: 0.84em;
	color: #555;
	text-align: left;
	padding: 0 0 10px 0;
	background-color: #fff;
}

h2 {
	background-color: #ffcd13;
	margin-bottom: 10px;
	padding: 5px 10px;
}

body,td,select,input,li {
	font-size: 13px;
}

a {
	text-decoration: none;
}

a:link {
	color: #39912b;
	font-weight: bold;
}

a:visited {
	color: #39912b;
}

a:active,a:hover {
	color: #39912b;
}

b {
	color: #333;
}

.logo {
	float: right;
	margin-right: 30px;
}

.clear {
	clear: both;
}

.left {
	text-align: left;
	margin-left: 10px;
}

.right {
	text-align: right;
	margin-right: 10px;
}

.reports {
	margin: 30px;
	text-align: left;
}

.reports h2 {
    opacity: 0.85;
}

table {
	border-collapse: collapse;
}

.stories {
    border: 1px solid #000;
}

.scenarios {
	border: 1px solid #000;
}

.steps {
	border: 1px solid #000;
}

.totals {
    border-top: 1px solid #000;
    padding-top: 10px;
    font-weight: bold;
}	

.reports table {
	border: solid;
}

.reports table th {
	border-bottom: 1px solid #000;
	text-align: center;
	font-weight: bold;
	padding: 10px;
}

.reports table td {
	text-align: center;
	padding: 5px;
}

.reports table td.story {
	text-align: left;
	padding: 5px;
}

.reports table td a {
    color: #555;
}

.lane {
    border-left: 1px solid #000;
}

.maps {
	margin-left: 30px;
	margin-top: 30px;
	text-align: left;
}

.maps h2 {
    opacity: 0.85;
}

.maps table {
	border: solid;
}

.maps table th {
	text-align: center;
	font-weight: bold;
	padding: 10px;
}

.maps table td {
	text-align: center;
	padding: 5px;
}

.maps table td.name {
	text-align: left;
	padding: 5px;
}

.views {
    margin-left: 30px;
    margin-top: 30px;
    text-align: left;
}

.views h2 {
    opacity: 0.85;
}

.views table {
    border: solid;
}

.views table th {
    text-align: center;
    font-weight: bold;
    padding: 10px;
}

.views table td {
    text-align: center;
    padding: 5px;
}

.views table td.name {
    text-align: left;
    padding: 5px;
}

.story {
	text-align: left;
	margin-left: 10px;
}

.story h2 {
	background-color: #fff;
	border-color: #ffcd13;
}

.meta {
	text-align: left;
	color: purple;
	margin-bottom: 10px;
}

.filter {
	text-align: left;
	color: red;
	margin-left: 10px;
	margin-bottom: 10px;
}

.keyword {
	margin-left: 10px;
}

.property {
	margin-left: 10px;
}

.narrative {
	text-align: left;
	color: blue;
}

.element {
	padding-left: 10px;
}

span.inOrderTo,span.asA,span.iWantTo {
	font-weight: bold;
}

.givenStories, .path {
	font-weight: bolder;
	font-size: 16px;
    opacity: 0.85;
	margin-bottom: 10px;
	padding: 5px 10px;
}

.scenario {
	text-align: left;
	padding-left: 1px;
}

.step {
	color: black;
	padding: 2px 2px 2px 20px;
}

.successful {
	color: green;
}

.ignorable {
	color: blue;
}

.pending {
	color: orange;
}

.notPerformed {
	color: brown;
}

.failed {
	color: red;
}

.cancelled {
	color: red;
}

.restarted {
	color: orange;
}

.dryRun {
	border: solid;
	color: amber;
	background-color: yellow;
	margin-left: 12px;
	padding: 10px;
	font-size: 20px;
	text-align: center;
	text-weight: bold;
}

.parameter {
	color: purple;
	padding-left: 0px;
	text-weight: bold;
}

.keyword {
	text-weight: bold;
}

.examples {
	margin-left: 10px;
    padding-bottom: 20px;
}

.examples h3 {
	opacity: 0.85;
}

.examples table {
	border: 1px solid;
	margin-top: 12px;
	margin-left: 12px;
}

.examples table th, td {
	text-align: center;
	padding-left: 5px;
    padding-right: 5px;
    border-left: 1px solid;	
}

.examples table th {
    font-weight: bold;
}

.outcomes table {
    border: 1px solid;
    margin-top: 12px;
    margin-left: 12px;
}

.outcomes table th, td {
    text-align: center;
    padding-left: 5px;
    padding-right: 5px;
    border-left: 1px solid; 
}

.outcomes table th {
    font-weight: bold;
}

.outcomes table tr.notVerified {
	color: red;
}

.outcomes table tr.verified {
	color: green;
}

.outcomes {
	padding-bottom: 20px;
}

.parameter table {
    border: 1px solid;
    margin-top: 12px;
    margin-left: 12px;
}

.parameter table th, td {
    text-align: center;
    padding-left: 5px;
    padding-right: 5px;
    border-left: 1px solid; 
}

.parameter table th {
    font-weight: bold;
}

pre.failure {
	padding-left: 10px;
	color: red;
}
</style>
</head>

<body>
<div class="reports">

<h2>故事报告</h2>

<table>
<colgroup span="1" class="stories"></colgroup>
<colgroup span="4" class="scenarios"></colgroup>
<colgroup span="6" class="steps"></colgroup>
<colgroup class="view"></colgroup>
<tr>
    <th colspan="1">故事</th>
    <th colspan="4">场景</th>
    <th colspan="6">步骤</th>
    <th></th>
    <th></th>
</tr>
<tr>
    <th>名称</th>
    <th>总共</th>
    <th>成功</th>
    <th>待解决的</th>
    <th>失败</th>
    <th>总共</th>
    <th>成功</th>
    <th>待解决的</th>
    <th>失败</th>
    <th>未执行的</th>
    <th>已忽略的</th>
    <th>耗时(hh:mm:ss.SSS)</th>
    <th>详细</th>
</tr>
<#assign reportNames = reports.getReportNames()>
<#assign totalReports = reportNames.size() - 1>
<#list reportNames as name>
<#assign report = reports.getReport(name)>
<#if name != "Totals">
<tr>
<#assign stats = report.getStats()>
<#assign stepsFailed = stats.get("stepsFailed")!0>
<#assign scenariosFailed = stats.get("scenariosFailed")!0>
<#assign pending = stats.get("pending")!0>
<#assign stepsSuccessful = stats.get("stepsSuccessful")!0>
<#assign storyClass = "story">
<#if stepsFailed != 0 && stepsSuccessful == 0 >
	<#assign storyClass = storyClass + " pending">
<#elseif stepsFailed != 0 || scenariosFailed != 0>
    <#assign storyClass = storyClass + " failed">
<#elseif pending != 0>
    <#assign storyClass = storyClass + " pending">
<#else>
    <#assign storyClass = storyClass + " successful">
</#if>
<td class="${storyClass}">${report.name}</td>
<!--
<td>
<@renderStat stats "notAllowed" "failed"/>
</td>
-->

<td>
<@renderStat stats "scenarios"/> 
</td>
<td>
<@renderStat stats "scenariosSuccessful" "successful"/> 
</td>
<td>
<@renderStat stats "scenariosPending" "pending"/> 
</td>
<td>
<@renderStat stats "scenariosFailed" "failed"/>
</td>
<!--
<td>
<@renderStat stats "scenariosNotAllowed" "failed"/>
</td>
-->
<!--
<td>
<@renderStat stats "givenStoryScenarios"/> 
</td>
<td>
<@renderStat stats "givenStoryScenariosSuccessful" "successful"/> 
</td>
<td>
<@renderStat stats "givenStoryScenariosPending" "pending"/> 
</td>
<td>
<@renderStat stats "givenStoryScenariosFailed" "failed"/>
</td>
<td>
<@renderStat stats "givenStoryScenariosNotAllowed" "failed"/>
</td>
-->
<td>
<@renderStat stats "steps" />
</td>
<td>
<@renderStat stats "stepsSuccessful" "successful"/>
</td>
<td>
<@renderStat stats "stepsPending" "pending"/>
</td>
<td>
<@renderStat stats "stepsFailed" "failed"/>
</td>
<td>
<@renderStat stats "stepsNotPerformed" "notPerformed" />
</td>
<td>
<@renderStat stats "stepsIgnorable" "ignorable"/>
</td>
<td>
<@renderMillis stats "duration"/>
</td>
<td>
<#assign filesByFormat = report.filesByFormat>
<#list filesByFormat.keySet() as format><#assign file = filesByFormat.get(format)><a href="${file.name}">${format}</a><#if format_has_next> |</#if></#list>
</td>
</tr>
</#if>
</#list>
<tr class="totals">
<td>${totalReports}</td>
<#assign stats = reports.getReport("Totals").getStats()>
<!--
<td>
<@renderStat stats "notAllowed" "failed"/>
</td>
-->
<td>
<@renderStat stats "scenarios"/> 
</td>
<td>
<@renderStat stats "scenariosSuccessful" "successful"/> 
</td>
<td>
<@renderStat stats "scenariosPending" "pending"/> 
</td>
<td>
<@renderStat stats "scenariosFailed" "failed"/>
</td>
<!--
<td>
<@renderStat stats "scenariosNotAllowed" "failed"/>
</td>
-->
<!--
<td>
<@renderStat stats "givenStoryScenarios"/> 
</td>
<td>
<@renderStat stats "givenStoryScenariosSuccessful" "successful"/> 
</td>
<td>
<@renderStat stats "givenStoryScenariosPending" "pending"/> 
</td>
<td>
<@renderStat stats "givenStoryScenariosFailed" "failed"/>
</td>
<td>
<@renderStat stats "givenStoryScenariosNotAllowed" "failed"/>
</td>
-->
<td>
<@renderStat stats "steps" />
</td>
<td>
<@renderStat stats "stepsSuccessful" "successful"/>
</td>
<td>
<@renderStat stats "stepsPending" "pending"/>
</td>
<td>
<@renderStat stats "stepsFailed" "failed"/>
</td>
<td>
<@renderStat stats "stepsNotPerformed" "notPerformed" />
</td>
<td>
<@renderStat stats "stepsIgnorable" "ignorable"/>
</td>
<td>
<@renderMillis stats "duration"/>
</td>
<td>
总计
</td>
</tr>
</table>
<br />
</div>

<div class="clear"></div>
<div id="footer">
<div class="left">生成时间${date?string("yyyy-MM-dd HH:mm:ss")}</div>
<div class="right">JBehave &#169; 2003-2011</div>
<div class="clear"></div>
</div>

</body>

</html>