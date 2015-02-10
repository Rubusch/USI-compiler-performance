#!/usr/sbin/dtrace -s
#pragma D option flowindent

/*
 * Assignment09
 * Software Performance
 * Lothar Rubusch L.Rubusch@gmx.ch
 * 2013-11-06
 */
syscall:::entry
/pid==$target/
{
    self->start = timestamp

/*    @[arg0] = quantize(arg2) */

/*    @[ustack()] = quantize(arg2); */
}

syscall:::return
{

/*    @c[probefunc] = sum(timestamp - self->start) */
    @c[probefunc] = count() 
/*  printf( "%s %d %d\n", probefunc, count(), sum(timestamp - self->start) ) */
}

