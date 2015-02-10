syscall:::entry
/pid==$target/
{
    @c[execname,pid,probefunc] = count()
}

