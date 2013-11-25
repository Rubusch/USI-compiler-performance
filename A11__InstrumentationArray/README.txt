README


application

iDEBIAN<11:28:56>::user("/opt/git_java_performance/A11__InstrumentationArray/");
$ java -cp application-bin Application
READY.



instrumentation

iDEBIAN<13:53:35>::user("/opt/git_java_performance/A11__InstrumentationArray/");
$ jar cfm agent.jar Manifest.txt -C agent-bin .

iDEBIAN<13:53:35>::user("/opt/git_java_performance/A11__InstrumentationArray/");
$ java -javaagent:agent.jar=blablabla -cp application-bin Application
XXX Agent starting (agentArgs: 'blablabla')
READY.
