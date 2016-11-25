Meta:

Narrative:
As a user
I want to perform an action
So that I can achieve a business goal

Scenario: scenario description
When 控制台打印 <ts1>
When 控制台打印 <ts2>
And 控制台打印 <rand1>
And 控制台打印 <rand2>

Examples:
|ts1||ts2|rand1|rand2|
|${ts('ts1')}||${ts('ts2')}|${rand('rand1')}|${rand('rand2')}|