#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <stdio.h>

/* inet_ntoa */
char * inet_ntoa(struct in_addr in)
{
	static char ret[16];
	unsigned char * b = (unsigned char*)&in.s_addr;
	unsigned int i;
	unsigned int pos = 0;

	ret[0] = '\0';
	for(i = 0; pos <= sizeof(ret) && i < sizeof(in.s_addr); i++)
		pos += snprintf(&ret[pos], sizeof(ret) - pos, "%s%u",
				i ? "." : "", b[i]);
	return ret;
}
