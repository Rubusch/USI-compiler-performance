syscall:::entry
/pid==$target/
{
    self->start = timestamp
}

syscall:::return
{
    @c[probefunc] = sum(timestamp - self->start)
}

