#!/bin/bash

for m in `cat ./methods.txt`; do
	echo $m
	java -cp bin:lib/asm-all-4.0.jar ch.usi.inf.sp.cfg.Analyzer bin/ExampleClass.class $m > dots/$m-da.dot
	dot -Tpng dots/$m-da.dot -o dots/$m-da.png
done

