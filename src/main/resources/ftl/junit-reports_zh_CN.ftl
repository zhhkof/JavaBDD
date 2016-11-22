<#ftl strip_whitespace=true>
<#macro renderStat stats name>
<#if stats.get(name) ??>
<#assign value = stats.get(name)>${value}<#else>
<#assign value = 0>${value}
</#if>
</#macro>
<#macro renderMillis stats name>
<#if stats.get(name) ??>
<#assign millis = stats.get(name)/1000>${millis}<#else>
<#assign millis = 0>${millis}
</#if>
</#macro>
<?xml version="1.0" encoding="UTF-8" ?>
<#assign reportNames = reports.getReportNames()>
<#assign totalReports = reportNames.size() - 3>
<#assign stats = reports.getReport("Totals").getStats()>
<testsuite errors="<@renderStat stats "scenariosFailed"/>" tests="${totalReports}" time="<@renderMillis stats "duration"/>" failures="<@renderStat stats "scenariosFailed"/>" name="${storyName}">
<#list reportNames as name>
<#assign report = reports.getReport(name)>
<#if name != "Totals">
<#assign stats = report.getStats()>
<#assign stepsFailed = stats.get("stepsFailed")!0>
<#assign scenariosFailed = stats.get("scenariosFailed")!0>
<#assign stepsSuccessful = stats.get("stepsSuccessful")!0>
<#assign pending = stats.get("pending")!0>
<#assign storyClass = "story">
<#assign filesByFormat = report.filesByFormat>
<#list filesByFormat.keySet() as format>
<#assign file = filesByFormat.get(format)>
</#list>
<#if stepsFailed != 0 && stepsSuccessful == 0 >
	<#assign results = "blocked">
<#elseif stepsFailed != 0 || scenariosFailed != 0>
    <#assign results = "failed">
<#elseif pending != 0>
    <#assign results = "pending">
<#else>
    <#assign results = "successful">
</#if>
<#if results == "failed">
	<testcase time="<@renderMillis stats "duration"/>" name="${"${file.name}"?replace(".html","")}">
		<failure type="Failure" message="Steps fail">
		</failure>
	</testcase>
</#if>
<#if results == "blocked">
	<testcase time="<@renderMillis stats "duration"/>" name="${"${file.name}"?replace(".html","")}">
		<failure type="Blocked" message="Because the front of story is fail">
		</failure>
	</testcase>
</#if>
<#if results == "pending">
	<testcase time="<@renderMillis stats "duration"/>" name="${"${file.name}"?replace(".html","")}">
		<failure type="Not running" message="Not running">
		</failure>
	</testcase>
</#if>
<#if results == "successful">
	<testcase time="<@renderMillis stats "duration"/>" name="${"${file.name}"?replace(".html","")}"/>
</#if>
<#assign filesByFormat = report.filesByFormat>
</#if>
</#list>
</testsuite>