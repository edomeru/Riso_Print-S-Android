#include <pthread.h>
#include <netdb.h>
#include <netinet/in.h>
#include <errno.h>

#include "gtest/gtest.h"
#include "fff.h"

#define TEST_APP_NAME   "RISO-PRINT-S"
#define TEST_APP_VER    "2.0.0"

DEFINE_FFF_GLOBALS;

FAKE_VALUE_FUNC(int, getaddrinfo, const char *, const char *, const struct addrinfo *, struct addrinfo **); 
FAKE_VOID_FUNC(freeaddrinfo, struct addrinfo *);
FAKE_VALUE_FUNC(int, socket, int, int, int);
FAKE_VOID_FUNC(fcntl);
FAKE_VALUE_FUNC(int, connect, int, const struct sockaddr *, socklen_t);
FAKE_VALUE_FUNC(int, close, int)
FAKE_VALUE_FUNC(int, select, int, fd_set *, fd_set *, fd_set *, struct timeval *);
FAKE_VALUE_FUNC(int, FD_ISSET_MOCK);
FAKE_VALUE_FUNC(int, getsockopt, int, int, int, void *, socklen_t *);
FAKE_VALUE_FUNC(int, setsockopt, int, int, int, const void *, socklen_t);
FAKE_VALUE_FUNC(FILE *, fopen, const char *, const char *);
FAKE_VALUE_FUNC(int, fclose, FILE *);
FAKE_VALUE_FUNC(ssize_t, send, int, const void *, size_t, int);
FAKE_VALUE_FUNC(ssize_t, recv, int, void *, size_t, int);
FAKE_VALUE_FUNC(int, fseek_mock);
FAKE_VALUE_FUNC(long, ftell_mock);
FAKE_VALUE_FUNC(size_t, fread_mock);

extern "C"
{
#include "common.h"
}

class DirectPrintTest : public testing::Test
{
public:

    static void SetUpTestCase()
    {
        server_info_ = new struct addrinfo;
        //server_info_ = (struct addrinfo *)malloc(sizeof(struct addrinfo));
        server_info_->ai_next = 0;
    }

    static void TearDownTestCase()
    {
        delete server_info_;
        server_info_ = 0;
    }

    void SetUp()
    {
        RESET_FAKE(getaddrinfo);
        RESET_FAKE(freeaddrinfo);
        RESET_FAKE(socket);
        RESET_FAKE(fcntl);
        RESET_FAKE(connect);
        RESET_FAKE(close);
        RESET_FAKE(select);
        RESET_FAKE(FD_ISSET_MOCK);
        RESET_FAKE(getsockopt);
        RESET_FAKE(setsockopt);
        RESET_FAKE(fopen);
        RESET_FAKE(send);
        RESET_FAKE(recv);
        RESET_FAKE(fseek_mock);
        RESET_FAKE(ftell_mock);
        RESET_FAKE(fread_mock);

        FFF_RESET_HISTORY();

        // Defaults
        getaddrinfo_fake.return_val = 0;
        socket_fake.return_val = 1;
        connect_fake.return_val = 1;
        close_fake.return_val = 0;
        select_fake.return_val = 0;
        FD_ISSET_MOCK_fake.return_val = 0;
        getsockopt_fake.return_val = 0;
        setsockopt_fake.return_val = 0;
        fopen_fake.return_val = (FILE *)1;
        fclose_fake.return_val = 0;
        send_fake.return_val = 0;
        recv_fake.return_val = 0;
        fseek_mock_fake.return_val = 0;
        ftell_mock_fake.return_val = 1;
        fread_mock_fake.return_val = 1;
    }

    static struct addrinfo *server_info_;
};


struct addrinfo *DirectPrintTest::server_info_ = 0;

struct directprint_job_s
{
    char *printer_name;
    char *app_name;
    char *app_version;
    
    char *user_name;
    char *job_name;
    char *filename;
    char *print_settings;
    char *ip_address;
    directprint_callback callback;
    
    pthread_t main_thread;
    pthread_mutex_t mutex;
    float progress;
    int cancel_print;
    
    void *caller_data;
};

pthread_mutex_t _mutex;
int _callback_received = 0;
int _status[32];

void print_callback(directprint_job *job, int status, float progress)
{
    pthread_mutex_lock(&_mutex);
    _status[_callback_received++] = status;
    pthread_mutex_unlock(&_mutex);
}

// Custom fakes
int getaddrinfo_one_fake(const char* ip_address, const char *port, const struct addrinfo *hints, struct addrinfo **server_in)
{
    *server_in = DirectPrintTest::server_info_;
    return getaddrinfo_fake.return_val;
}

int connect_EPIPE_fake(int socket, const struct sockaddr * sock_address, socklen_t sock_len)
{
    errno = EPIPE;
    return connect_fake.return_val;
}

int connect_EINPROGRESS_fake(int socket, const struct sockaddr * sock_address, socklen_t sock_len)
{
    errno = EINPROGRESS;
    return connect_fake.return_val;
}

int getsockopt_NG(int socket, int sock_level, int sock_opt, void *error, socklen_t *error_len)
{
    int *value = (int *)error;
    *value = 1;
    return getsockopt_fake.return_val;
}

int getsockopt_OK(int socket, int sock_level, int sock_opt, void *error, socklen_t *error_len)
{
    int *value = (int *)error;
    *value = 0;
    return getsockopt_fake.return_val;
}

ssize_t send_helper()
{
    RETURN_FAKE_RESULT(send);
}

ssize_t send_custom_fake(int socket, const void *buffer, size_t length, int flags)
{
    ssize_t value = send_helper();
    if (value == 0)
    {
        return 0;
    }
    return length;
}

ssize_t recv_helper()
{
    RETURN_FAKE_RESULT(recv);
}

ssize_t recv_custom_fake(int socket, void *buffer, size_t length, int flags)
{
    ssize_t value = recv_helper();
    if (value == 2)
    {
        return 0;
    }
    int *output = (int *)buffer;
    *output = value;
    return sizeof(int);
}

TEST(DirectPrintTestJobTest, New)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "User name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    ASSERT_TRUE(job != 0);
    ASSERT_TRUE(strcmp(job_name, job->job_name) == 0);
    ASSERT_TRUE(strcmp(filename, job->filename) == 0);
    ASSERT_TRUE(strcmp(print_settings, job->print_settings) == 0);
    ASSERT_TRUE(strcmp(ip_address, job->ip_address) == 0);
    ASSERT_TRUE(print_callback == job->callback);

    directprint_job_free(job);
}

/*TEST(DirectPrintTestJobTest, Free)
{
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_free(job);

    //ASSERT_TRUE(job == 0);
}*/

TEST(DirectPrintTestJobTest, CallerData)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    char test_data[16];
    strcpy(test_data, "TESTDATA");

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_set_caller_data(job, (void *)test_data);
    char *caller_data = (char *)directprint_job_get_caller_data(job);

    ASSERT_TRUE(strcmp(test_data, caller_data) == 0);

    directprint_job_free(job);
}

TEST(DirectPrintTestJobTest, NotCancelled)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    ASSERT_TRUE(job->cancel_print == 0);

    directprint_job_free(job);
}

TEST(DirectPrintTestJobTest, Cancelled)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_cancel(job);

    ASSERT_TRUE(job->cancel_print == 1);

    directprint_job_free(job);
}

// Parameter Tests

TEST_F(DirectPrintTest, LPR_JobIsNull)
{
    int status = directprint_job_lpr_print(0);
    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, UserNameIsNull)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    free(job->user_name);
    job->user_name = 0;
    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

#if 0
TEST_F(DirectPrintTest, UserNameIsEmpty)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}


TEST_F(DirectPrintTest, JobNameIsNull)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    free(job->job_name);
    job->job_name = 0;
    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}
#endif

TEST_F(DirectPrintTest, JobNameIsEmpty)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, FileNameIsNull)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    free(job->filename);
    job->filename = 0;
    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, FileNameIsEmpty)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, PrintSettingsIsNull)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    free(job->print_settings);
    job->print_settings = 0;
    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, PrintSettingsIsEmpty)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, IPAddresIsNull)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    free(job->ip_address);
    job->ip_address = 0;
    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, IPAddressIsEmpty)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    int status = directprint_job_lpr_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}

TEST_F(DirectPrintTest, InvalidAddress)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "Invalid IP";
    
    // Setup mock and mutex
    getaddrinfo_fake.return_val = 1;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

TEST_F(DirectPrintTest, IPv6Address_Global)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "2001:10::1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    
    int result = strcmp(ip_address, job->ip_address);

    directprint_job_free(job);
    
    //IP Address should be used as is
    ASSERT_EQ(result, 0);
}

TEST_F(DirectPrintTest, IPv6Address_LinkLocal)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "fe80:10::1";
    
    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    
    int result = strcmp(ip_address, job->ip_address);
    int has_en0 = (strstr(job->ip_address, "%en0")) != 0;

    directprint_job_free(job);
    
    // IP Address should be modified
    ASSERT_NE(result, 0);
    // IP Address should have %en0 suffix
    ASSERT_NE(has_en0, 0);
}

// Connection tests

TEST_F(DirectPrintTest, CannotCreateSocket)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = -1;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

TEST_F(DirectPrintTest, CannotStartConnect)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = 0;
    connect_fake.custom_fake = connect_EPIPE_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

TEST_F(DirectPrintTest, CannotConnect)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EPIPE_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

TEST_F(DirectPrintTest, ConnectTimeout)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 0;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

TEST_F(DirectPrintTest, ConnectCannotComplete)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.return_val = -1;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

// File test

TEST_F(DirectPrintTest, LPR_FileCannotOpen)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)0;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 3)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusErrorFile, _status[2]);
}

// Transimission tests

TEST_F(DirectPrintTest, LPR_QueueInfoSendFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 0 };
    SET_RETURN_SEQ(send, send_return, 1);
    send_fake.custom_fake = send_custom_fake;
    

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 4)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(1, send_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusErrorSending, _status[3]);
}

TEST_F(DirectPrintTest, LPR_QueueInfoAckNone)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 };
    SET_RETURN_SEQ(send, send_return, 1);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 2 };
    SET_RETURN_SEQ(recv, recv_return, 1);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 4)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(1, send_fake.call_count);
    ASSERT_EQ(1, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusErrorSending, _status[3]);
}

TEST_F(DirectPrintTest, LPR_QueueInfoAckFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 };
    SET_RETURN_SEQ(send, send_return, 1);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 1 };
    SET_RETURN_SEQ(recv, recv_return, 1);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 4)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(1, send_fake.call_count);
    ASSERT_EQ(1, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusErrorSending, _status[3]);
}

TEST_F(DirectPrintTest, LPR_ControlInfoSendFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 , 0 };
    SET_RETURN_SEQ(send, send_return, 2);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0 };
    SET_RETURN_SEQ(recv, recv_return, 1);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 4)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(2, send_fake.call_count);
    ASSERT_EQ(1, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusErrorSending, _status[3]);
}

TEST_F(DirectPrintTest, LPR_ControlInfoAckNone)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 , 1 };
    SET_RETURN_SEQ(send, send_return, 2);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 2 };
    SET_RETURN_SEQ(recv, recv_return, 2);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 5)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(2, send_fake.call_count);
    ASSERT_EQ(2, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusErrorSending, _status[4]);
}

TEST_F(DirectPrintTest, LPR_ControlInfoAckFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 , 1 };
    SET_RETURN_SEQ(send, send_return, 2);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 1 };
    SET_RETURN_SEQ(recv, recv_return, 2);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 5)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(2, send_fake.call_count);
    ASSERT_EQ(2, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusErrorSending, _status[4]);
}

TEST_F(DirectPrintTest, LPR_ControlSendFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 , 1, 0 };
    SET_RETURN_SEQ(send, send_return, 3);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0 };
    SET_RETURN_SEQ(recv, recv_return, 2);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 5)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(3, send_fake.call_count);
    ASSERT_EQ(2, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusErrorSending, _status[4]);
}

TEST_F(DirectPrintTest, LPR_ControlAckNone)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 , 1, 1 };
    SET_RETURN_SEQ(send, send_return, 3);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 2 };
    SET_RETURN_SEQ(recv, recv_return, 3);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 6)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(3, send_fake.call_count);
    ASSERT_EQ(3, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusErrorSending, _status[5]);
}

TEST_F(DirectPrintTest, LPR_ControlAckFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1 , 1, 1 };
    SET_RETURN_SEQ(send, send_return, 3);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 1 };
    SET_RETURN_SEQ(recv, recv_return, 3);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 6)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(3, send_fake.call_count);
    ASSERT_EQ(3, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusErrorSending, _status[5]);
}

TEST_F(DirectPrintTest, LPR_DataInfoSendFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1, 1, 1, 0 };
    SET_RETURN_SEQ(send, send_return, 4);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 0 };
    SET_RETURN_SEQ(recv, recv_return, 3);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 6)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(4, send_fake.call_count);
    ASSERT_EQ(3, recv_fake.call_count);
    ASSERT_EQ(2, fseek_mock_fake.call_count);
    ASSERT_EQ(1, ftell_mock_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusErrorSending, _status[5]);
}

TEST_F(DirectPrintTest, LPR_DataInfoAckNone)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1, 1, 1, 1 };
    SET_RETURN_SEQ(send, send_return, 4);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 0, 2 };
    SET_RETURN_SEQ(recv, recv_return, 4);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 7)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(4, send_fake.call_count);
    ASSERT_EQ(4, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusSending, _status[5]);
    ASSERT_EQ(kJobStatusErrorSending, _status[6]);
}

TEST_F(DirectPrintTest, LPR_DataInfoAckFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1, 1, 1, 1 };
    SET_RETURN_SEQ(send, send_return, 4);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 0, 1 };
    SET_RETURN_SEQ(recv, recv_return, 4);
    recv_fake.custom_fake = recv_custom_fake;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 7)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(4, send_fake.call_count);
    ASSERT_EQ(4, recv_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusSending, _status[5]);
    ASSERT_EQ(kJobStatusErrorSending, _status[6]);
}

TEST_F(DirectPrintTest, LPR_DataAckNone)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1, 1, 1, 1, 1, 1 };
    SET_RETURN_SEQ(send, send_return, 6);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 0, 0, 2 };
    SET_RETURN_SEQ(recv, recv_return, 5);
    recv_fake.custom_fake = recv_custom_fake;
    size_t fread_mock_return[] = { 1, 0 };
    SET_RETURN_SEQ(fread_mock, fread_mock_return, 2);

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 10)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(8, send_fake.call_count);
    ASSERT_EQ(5, recv_fake.call_count);
    ASSERT_EQ(2, fread_mock_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusSending, _status[5]);
    ASSERT_EQ(kJobStatusSending, _status[6]);
    ASSERT_EQ(kJobStatusSending, _status[7]);
    ASSERT_EQ(kJobStatusSending, _status[8]);
    ASSERT_EQ(kJobStatusErrorSending, _status[9]);
}

TEST_F(DirectPrintTest, LPR_DataAckFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1, 1, 1, 1, 1, 1 };
    SET_RETURN_SEQ(send, send_return, 6);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 0, 0, 1 };
    SET_RETURN_SEQ(recv, recv_return, 5);
    recv_fake.custom_fake = recv_custom_fake;
    size_t fread_mock_return[] = { 1, 0 };
    SET_RETURN_SEQ(fread_mock, fread_mock_return, 2);

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 10)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(8, send_fake.call_count);
    ASSERT_EQ(5, recv_fake.call_count);
    ASSERT_EQ(2, fread_mock_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusSending, _status[5]);
    ASSERT_EQ(kJobStatusSending, _status[6]);
    ASSERT_EQ(kJobStatusSending, _status[7]);
    ASSERT_EQ(kJobStatusSending, _status[8]);
    ASSERT_EQ(kJobStatusErrorSending, _status[9]);
}

TEST_F(DirectPrintTest, LPR_Success)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
   const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    ssize_t send_return[] = { 1, 1, 1, 1, 1, 1 };
    SET_RETURN_SEQ(send, send_return, 6);
    send_fake.custom_fake = send_custom_fake;
    ssize_t recv_return[] = { 0, 0, 0, 0, 0 };
    SET_RETURN_SEQ(recv, recv_return, 5);
    recv_fake.custom_fake = recv_custom_fake;
    size_t fread_mock_return[] = { 1, 0 };
    SET_RETURN_SEQ(fread_mock, fread_mock_return, 2);

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_lpr_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 10)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(8, send_fake.call_count);
    ASSERT_EQ(5, recv_fake.call_count);
    ASSERT_EQ(2, fread_mock_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusSending, _status[5]);
    ASSERT_EQ(kJobStatusSending, _status[6]);
    ASSERT_EQ(kJobStatusSending, _status[7]);
    ASSERT_EQ(kJobStatusSending, _status[8]);
    ASSERT_EQ(kJobStatusSending, _status[9]);
    ASSERT_EQ(kJobStatusSent, _status[10]);
}

// Raw
#if 0
TEST_F(DirectPrintTest, Raw_CannotStartPrint)
{
    // Note: All parameter checks are already tested using LPR, so only the branch will be tested here
    // user_name = empty
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);

    int status = directprint_job_raw_print(job);
    directprint_job_free(job);

    ASSERT_TRUE(status == kJobStatusError);
}
#endif

TEST_F(DirectPrintTest, Raw_CannotConnect)
{
    // Note: All connection checks are already testes using LPR, os only the branch will be tested here
    // error = socket creation failed
    const char *user_name = "Sample User Name";
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = -1;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_raw_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 2)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusErrorConnecting, _status[1]);
}

// File test

TEST_F(DirectPrintTest, Raw_FileCannotOpen)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)0;

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_raw_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 3)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusErrorFile, _status[2]);
}

TEST_F(DirectPrintTest, Raw_SendFailed)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    size_t fread_mock_return[] = { 100, 0 };
    SET_RETURN_SEQ(fread_mock, fread_mock_return, 2);
    ssize_t send_return[] = { 1, 99 };
    SET_RETURN_SEQ(send, send_return, 2);

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_raw_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 4)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(1, fread_mock_fake.call_count);
    ASSERT_EQ(2, send_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusErrorSending, _status[3]);
}

TEST_F(DirectPrintTest, Raw_Success)
{
    const char *printer_name = "RISO IS1000C-J";
    const char *app_name = TEST_APP_NAME;
    const char *app_version = TEST_APP_VER;
    const char *user_name = "Sample User Name";
    const char *job_name = "Sample Job Name";
    const char *filename = "Sample.pdf";
    const char *print_settings = "colorMode=0";
    const char *ip_address = "127.0.0.1";
    
    getaddrinfo_fake.return_val = 0;
    getaddrinfo_fake.custom_fake = getaddrinfo_one_fake;
    socket_fake.return_val = 1;
    connect_fake.return_val = -1;
    connect_fake.custom_fake = connect_EINPROGRESS_fake;
    FD_ISSET_MOCK_fake.return_val = 1;
    getsockopt_fake.custom_fake = getsockopt_OK;
    getsockopt_fake.return_val = 0;
    fopen_fake.return_val = (FILE *)1;
    size_t fread_mock_return[] = { 100, 0 };
    SET_RETURN_SEQ(fread_mock, fread_mock_return, 2);
    ssize_t send_return[] = { 1, 100, 1 };
    SET_RETURN_SEQ(send, send_return, 3);

    // Setup mutex for callback
    _callback_received = 0;
    pthread_mutex_init(&_mutex, 0);

    directprint_job *job = directprint_job_new(printer_name, app_name, app_version, user_name, job_name, filename, print_settings, ip_address, print_callback);
    directprint_job_raw_print(job);

    int callback_recevied;
    while (1)
    {
        pthread_mutex_lock(&_mutex);
        int callback_receieved = _callback_received;
        pthread_mutex_unlock(&_mutex);

        if (callback_receieved == 5)
        {
            break;
        }
    }

    directprint_job_free(job);
    
    // Destroy mutex
    pthread_mutex_destroy(&_mutex);

    ASSERT_EQ(1, getaddrinfo_fake.call_count);
    ASSERT_EQ(1, socket_fake.call_count);
    ASSERT_EQ(1, connect_fake.call_count);
    ASSERT_EQ(1, FD_ISSET_MOCK_fake.call_count);
    ASSERT_EQ(1, getsockopt_fake.call_count);
    ASSERT_EQ(1, fopen_fake.call_count);
    ASSERT_EQ(2, fread_mock_fake.call_count);
    ASSERT_EQ(3, send_fake.call_count);
    ASSERT_EQ(kJobStatusConnecting, _status[0]);
    ASSERT_EQ(kJobStatusConnected, _status[1]);
    ASSERT_EQ(kJobStatusSending, _status[2]);
    ASSERT_EQ(kJobStatusSending, _status[3]);
    ASSERT_EQ(kJobStatusSending, _status[4]);
    ASSERT_EQ(kJobStatusSent, _status[5]);
}
