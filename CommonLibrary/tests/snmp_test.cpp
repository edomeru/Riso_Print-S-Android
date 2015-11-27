#include <pthread.h>
#include "gtest/gtest.h"

extern "C"
{
#include "common.h"
#include "net-snmp/net-snmp-config.h"
#include "net-snmp/net-snmp-includes.h"
}

class SNMPTest : public testing::Test
{
public:

    static void SetUpTestCase()
    {
    }

    static void TearDownTestCase()
    {
    }

    void SetUp()
    {
    }
};

#define IP_ADDRESS_LENGTH 128
#define MIB_STRING_LENGTH 256
#define COMMUNITY_NAME "public"

typedef struct
{
    snmp_device *first;
    snmp_device *current;
} caps_queue;

struct snmp_context_s
{
    int state;
    snmp_discovery_ended_callback discovery_ended_callback;
    snmp_printer_added_callback printer_added_callback;
    snmp_device *device_list;
    char ip_address[IP_ADDRESS_LENGTH];
    
    caps_queue device_queue;
    
    pthread_t main_thread;
    pthread_mutex_t mutex;
    pthread_mutex_t queue_mutex;
    
    void *caller_data;
};

enum
{
    MIB_HW_STAT = 0,
    MIB_PDL,
    MIB_DEV_DESCR,
    MIB_HW_CAP_1,
    MIB_HW_CAP_2,
    MIB_HW_CAP_3,
    MIB_HW_CAP_4,
    MIB_HW_CAP_5,
    MIB_HW_CAP_6,
    MIB_HW_CAP_7,
    MIB_HW_CAP_8,
    MIB_INFO_COUNT
};

struct snmp_device_s
{
    char ip_address[IP_ADDRESS_LENGTH];
    char device_info[MIB_INFO_COUNT][MIB_STRING_LENGTH];
    
    snmp_device *next;
};

// Callbacks

void ended_callback(snmp_context *context, int result)
{
}

void added_callback(snmp_context *context, snmp_device *device)
{
}

// SNMP Context

TEST_F(SNMPTest, ContextNew)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);

    ASSERT_TRUE(context != 0);
    ASSERT_TRUE(context->ip_address != 0);
    ASSERT_TRUE(strlen(context->ip_address) == 0);
    ASSERT_TRUE(context->state == kSnmpStateInitialized);
    ASSERT_TRUE(context->discovery_ended_callback == ended_callback);
    ASSERT_TRUE(context->printer_added_callback == added_callback);
    ASSERT_TRUE(context->device_list == 0);
    ASSERT_TRUE(context->device_queue.first == 0);
    ASSERT_TRUE(context->device_queue.current == 0);

    snmp_context_free(context);
}

TEST_F(SNMPTest, SetCallerData)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    
    char test_data[16];
    sprintf(test_data, "sample data");

    snmp_context_set_caller_data(context, test_data);

    ASSERT_TRUE(context->caller_data == test_data);

    snmp_context_free(context);
}

TEST_F(SNMPTest, GetCallerData)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    
    char test_data[16];
    sprintf(test_data, "sample data");

    snmp_context_set_caller_data(context, test_data);
    void *caller_data = snmp_context_get_caller_data(context);

    ASSERT_TRUE(caller_data == test_data);
}

TEST_F(SNMPTest, Cancel)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_cancel(context);

    ASSERT_TRUE(context->state == kSnmpStateCancelled);
}

// SNMP Device

TEST_F(SNMPTest, NewDevice)
{
    const char *ip_address = "192.168.1.1";
    snmp_device *device = snmp_device_new(ip_address);

    ASSERT_TRUE(device != 0);
    ASSERT_TRUE(strcmp(device->ip_address, ip_address) == 0);
    snmp_device_free(device);
}

TEST_F(SNMPTest, GetIpAddress)
{
    const char *ip_address = "192.168.1.1";
    snmp_device *device = snmp_device_new(ip_address);

    ASSERT_TRUE(strcmp(snmp_device_get_ip_address(device), ip_address) == 0);
    snmp_device_free(device);
}

TEST_F(SNMPTest, GetName)
{
    const char *ip_address = "192.168.1.1";
    snmp_device *device = snmp_device_new(ip_address);

    char test_name[16];
    sprintf(test_name, "Test Name");
    sprintf(device->device_info[MIB_DEV_DESCR], "%s", test_name);

    ASSERT_TRUE(strcmp(snmp_device_get_name(device), test_name) == 0);

    snmp_device_free(device);
}

TEST_F(SNMPTest, GetCapabilityStatus)
{
    const char *ip_address = "192.168.1.1";
    snmp_device *device = snmp_device_new(ip_address);

    sprintf(device->device_info[MIB_HW_CAP_1], "Capable");

    ASSERT_TRUE(snmp_device_get_capability_status(device, 0) == 1);
    ASSERT_TRUE(snmp_device_get_capability_status(device, 1) == 0);
    snmp_device_free(device);
}

// Private functions

extern "C" int snmp_context_get_state(snmp_context *context);

TEST_F(SNMPTest, ContextGetState)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    context->state = kSnmpStateStarted;
    
    ASSERT_TRUE(snmp_context_get_state(context) == kSnmpStateStarted);
    snmp_context_free(context);
}

extern "C" void snmp_context_set_state(snmp_context *context, int state);

TEST_F(SNMPTest, ContextSetState)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_context_set_state(context, kSnmpStateStarted);
    
    ASSERT_TRUE(context->state == kSnmpStateStarted);
    snmp_context_free(context);
}

extern "C" void snmp_context_device_add(snmp_context *context, snmp_device *device);

TEST_F(SNMPTest, DeviceAdd_Empty)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device = snmp_device_new("192.168.1.1");
    snmp_context_device_add(context, device);

    ASSERT_TRUE(context->device_list != 0);
    ASSERT_TRUE(context->device_list == device);
    ASSERT_TRUE(context->device_list->next == 0);

    snmp_context_free(context);
}

TEST_F(SNMPTest, DeviceAdd_Duplicate)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device = snmp_device_new("192.168.1.1");
    snmp_device *duplicate = snmp_device_new("192.168.1.1");
    snmp_context_device_add(context, device);
    snmp_context_device_add(context, duplicate);

    ASSERT_TRUE(context->device_list != 0);
    ASSERT_TRUE(context->device_list == device);
    ASSERT_TRUE(context->device_list->next == 0);

    snmp_context_free(context);
}

TEST_F(SNMPTest, DeviceAdd_Unique)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device1 = snmp_device_new("192.168.1.1");
    snmp_device *device2 = snmp_device_new("192.168.1.2");
    snmp_context_device_add(context, device1);
    snmp_context_device_add(context, device2);

    ASSERT_TRUE(context->device_list != 0);
    ASSERT_TRUE(context->device_list == device1);
    ASSERT_TRUE(context->device_list->next == device2);
    ASSERT_TRUE(context->device_list->next->next == 0);

    snmp_context_free(context);
}

TEST_F(SNMPTest, DeviceAdd_Multiple)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device1 = snmp_device_new("192.168.1.1");
    snmp_device *device2 = snmp_device_new("192.168.1.2");
    snmp_device *device3 = snmp_device_new("192.168.1.3");
    snmp_context_device_add(context, device1);
    snmp_context_device_add(context, device2);
    snmp_context_device_add(context, device3);

    ASSERT_TRUE(context->device_list != 0);
    ASSERT_TRUE(context->device_list == device1);
    ASSERT_TRUE(context->device_list->next == device2);
    ASSERT_TRUE(context->device_list->next->next == device3);
    ASSERT_TRUE(context->device_list->next->next->next == 0);

    snmp_context_free(context);
}

extern "C" int snmp_context_device_find_with_ip(snmp_context *context, const char *ip_address);

TEST_F(SNMPTest, FindWithIP_Found)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device1 = snmp_device_new("192.168.1.1");
    snmp_device *device2 = snmp_device_new("192.168.1.2");
    snmp_context_device_add(context, device1);
    snmp_context_device_add(context, device2);
    int found = snmp_context_device_find_with_ip(context, "192.168.1.2");
    
    ASSERT_TRUE(found == 1);
}

TEST_F(SNMPTest, FindWithIP_NotFound)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device1 = snmp_device_new("192.168.1.1");
    snmp_device *device2 = snmp_device_new("192.168.1.2");
    snmp_context_device_add(context, device1);
    snmp_context_device_add(context, device2);
    int found = snmp_context_device_find_with_ip(context, "192.168.1.3");
    
    ASSERT_TRUE(found == 0);
}

extern "C" int snmp_context_device_count(snmp_context *context);

TEST_F(SNMPTest, Count)
{
    snmp_context *context = snmp_context_new(ended_callback, added_callback, COMMUNITY_NAME);
    snmp_device *device1 = snmp_device_new("192.168.1.1");
    snmp_device *device2 = snmp_device_new("192.168.1.2");
    snmp_device *device3 = snmp_device_new("192.168.1.3");

    int count1 = snmp_context_device_count(context);
    snmp_context_device_add(context, device1);
    int count2 = snmp_context_device_count(context);
    snmp_context_device_add(context, device2);
    int count3 = snmp_context_device_count(context);
    snmp_context_device_add(context, device3);
    int count4 = snmp_context_device_count(context);

    ASSERT_TRUE(count1 == 0);
    ASSERT_TRUE(count2 == 1);
    ASSERT_TRUE(count3 == 2);
    ASSERT_TRUE(count4 == 3);
}
