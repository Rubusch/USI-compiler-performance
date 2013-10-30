#!/bin/bash

for m in `cat ./methods.txt`
do
echo $m

#java -cp bin:lib/asm-all-4.0.jar ch.usi.inf.sp.cfg.ControlFlowGraphExtractor build/ExampleClass.class $m > dots/$m.dot
java -cp bin:lib/asm-all-4.0.jar ch.usi.inf.sp.cfg.Analyzer bin/ExampleClass.class $m > dots/$m-dom.dot
dot -Tpng dots/$m-dom.dot -o dots/$m-dom.png
done

