Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: 简单web打开测试
Given start test
When open webpage <url>
And wait <a>
And 控制台打印 <timeout>
And 控制台打印 <ts1>
And 控制台打印 <rand1>
Examples:
|a|url|timeout|ts1|rand1|
|3|http://www.baidu.com|${config['timeout']}|${ts('ts1')}|${rand('rand1')}|