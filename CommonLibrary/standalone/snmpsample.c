#include <stdio.h>
#include "common.h"

#define SAMPLE_COMMUNITY_NAME "public"

void on_discovery_end(snmp_context *context, int result);

int main(int argc, char *argv[])
{
    snmp_context *context = snmp_context_new(on_discovery_end, 0);
    snmp_device_discovery(context);

    return 0;
}

void on_discovery_end(snmp_context *context, int result)
{
    printf("Ended with %d\n", result);
    snmp_context_free(context);
}
