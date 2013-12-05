#!/bin/bash

die(){
    echo $@
    exit 0
}

usage(){
cat <<EOF
usage: $0 <filename>
EOF
}


## main                                                                        
#if test -z $1; then
#    usage
#    die "ABORT"
#fi


dacapos=(
    "trace-antlr.csv"
    "trace-bloat.csv"
    "trace-chart.csv"
    "trace-fop.csv"
    "trace-hsqldb.csv"
    "trace-jython.csv"
    "trace-luindex.csv"
    "trace-lusearch.csv"
    "trace-pmd.csv"
    "trace-xalan.csv"
)

for filename in ${dacapos[*]}; do
    if ! test -f ${filename}; then
		continue
#        die "no file '${filename}'"
    fi

    newfilename=${filename//.csv/.modif.csv}
    echo "${newfilename}"
    echo "instr, atype, arg0, arg1, arg2" > "${newfilename}"
    cat "${filename}" >> "${newfilename}"
done

echo "READY"

