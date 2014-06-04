#include <pthread.h>
#include "gtest/gtest.h"

extern "C"
{
#include "common.h"
#include "net-snmp/net-snmp-config.h"
#include "net-snmp/net-snmp-includes.h"
}

// IPv4

TEST(UtilTest, ValidateIP_ValidIPv4)
{
    char ip[128];
    strcpy(ip, "192.168.1.1");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strcmp(formatted_ip, ip) == 0);
}

TEST(UtilTest, ValidateIP_InvalidIPv4)
{
    char ip[128];
    strcpy(ip, "192.168.1");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result != 1);
    ASSERT_TRUE(strlen(formatted_ip) == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv4Extra0)
{
    char ip[128];
    strcpy(ip, "192.168.001.0003");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strcmp(formatted_ip, "192.168.1.3") == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv4FormattedBufferNG)
{
    char ip[128];
    strcpy(ip, "192.168.001.0003");
    char formatted_ip[8];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 8);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strlen(formatted_ip) == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv4FormatVariations)
{
    char ip[4][128];
    char formatted_ip[128];
    int result;

    strcpy(ip[0], "192.168.1.1");
    strcpy(ip[1], "192.168.0001.0001");
    strcpy(ip[2], "192.000168.0001.0001");
    strcpy(ip[3], "000192.000168.0001.0001");

    for (int i = 0; i < 4; i++)
    {
        strcpy(formatted_ip, "");
        result = util_validate_ip(ip[i], formatted_ip, 128);
        ASSERT_TRUE(result == 1);
        ASSERT_TRUE(strcmp(formatted_ip, "192.168.1.1") == 0);
    }
}

// IPv6

TEST(UtilTest, ValidateIP_ValidIPv6)
{
    char ip[128];
    strcpy(ip, "2001:4:4:4:215f:ea2a:5942:65cc");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strcmp(formatted_ip, ip) == 0);
}

TEST(UtilTest, ValidateIP_InvalidIPv6)
{
    char ip[128];
    strcpy(ip, "2001:4:4:4:215f:ea2a:5942");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result != 1);
    ASSERT_TRUE(strlen(formatted_ip) == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv6Omitted0)
{
    char ip[128];
    strcpy(ip, "2001:0:0:4:215f:ea2a:5942:65cc");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strcmp(formatted_ip, "2001::4:215f:ea2a:5942:65cc") == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv6FormattedBufferNG)
{
    char ip[128];
    strcpy(ip, "2001:4:4:4:215f:ea2a:5942:65cc");
    char formatted_ip[8];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 8);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strlen(formatted_ip) == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv6MixedCase)
{
    char ip[128];
    strcpy(ip, "2001:4:4:4:215F:eA2a:5942:65cC");
    char formatted_ip[128];
    strcpy(formatted_ip, "");

    int result = util_validate_ip(ip, formatted_ip, 128);

    ASSERT_TRUE(result == 1);
    ASSERT_TRUE(strcmp(formatted_ip, "2001:4:4:4:215f:ea2a:5942:65cc") == 0);
}

TEST(UtilTest, ValidateIP_ValidIPv6FormatVariations)
{
    char ip[4][128];
    char formatted_ip[128];
    int result;

    strcpy(ip[0], "2001::4:215f:ea2a:5942:65cc");
    strcpy(ip[1], "2001:0:0:4:215f:ea2a:5942:65cc");
    strcpy(ip[2], "2001:0:0:4:215F:Ea2A:5942:65Cc");
    strcpy(ip[3], "02001::4:215F:Ea2A:5942:65Cc");

    for (int i = 0; i < 4; i++)
    {
        strcpy(formatted_ip, "");
        result = util_validate_ip(ip[i], formatted_ip, 128);
        ASSERT_TRUE(result == 1);
        ASSERT_TRUE(strcmp(formatted_ip, "2001::4:215f:ea2a:5942:65cc") == 0);
    }
}
