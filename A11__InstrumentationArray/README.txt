README


application

iDEBIAN<11:28:56>::user("/opt/git_java_performance/A11__InstrumentationArray/");
$ java -cp application-bin Application
READY.



disassembly (linux)

iDEBIAN<19:01:37>::user("/opt/git_java_performance/A11__InstrumentationArray/");
$ java -cp lib/asm-all-4.1.jar:disassembler-bin ch/usi/inf/sp/disassembler/JavaClassDisassembler ./application-bin/Application.class 



instrumentation, with profiler

iDEBIAN<13:53:35>::user("/opt/git_java_performance/A11__InstrumentationArray/");
$ jar cfm agent.jar Manifest.txt -C agent-bin .
$ java -javaagent:agent.jar -Xbootclasspath/p:profiler-bin -cp application-bin Application
