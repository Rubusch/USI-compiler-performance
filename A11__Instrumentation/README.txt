README



application

iDEBIAN<23:11:50>::user("/opt/git_java_performance/A11__Instrumentation/");
$ java -cp application-bin Application



agent

iDEBIAN<23:22:41>::user("/opt/git_java_performance/A11__Instrumentation/");
$ jar cfm agent.jar Manifest.txt -C agent-bin .

test-run agent

iDEBIAN<23:26:05>::user("/opt/git_java_performance/A11__Instrumentation/");
$ java -javaagent:agent.jar=blabla -cp application-bin Application
Agent stating (arguments: 'blabla')
Hello
