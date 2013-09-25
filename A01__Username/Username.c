/*
  prints the current username

  @author Lothar Rubusch

  compile:
  $ gcc -m32 ./Username.c -o ./Username.exe

  disassemble:
  $ gcc -m32 ./Username.c -S
 */
#include <stdlib.h>
#include <stdio.h>
int main( int args, char* argv[] ){
	char *username = NULL;
	username = getenv("USER");
	if( NULL != username ){
		printf("username '%s'\n", username );
	}
	exit( EXIT_SUCCESS );
}
