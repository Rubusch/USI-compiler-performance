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



test-run transformer

iDEBIAN<23:43:48>::user("/opt/git_java_performance/A11__Instrumentation/");
$ jar cfm agent.jar Manifest.txt -C agent-bin .
iDEBIAN<23:51:11>::user("/opt/git_java_performance/A11__Instrumentation/");
$ java -javaagent:agent.jar -cp application-bin Application
Agent stating (arguments: 'null')
About to transform class <null, sun/launcher/LauncherHelper>
About to transform class <null, java/lang/Enum>
About to transform class <null, sun/misc/URLClassPath$FileLoader$1>
About to transform class <sun.misc.Launcher$AppClassLoader@18efd7c, Application>
About to transform class <null, java/lang/Void>
Hello
About to transform class <null, java/lang/Shutdown>
About to transform class <null, java/lang/Shutdown$Lock>
