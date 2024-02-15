//
//  directprint.c
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2023 RISO KAGAKU CORPORATION. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <errno.h>
#include <stdbool.h>
#include "common.h"
#include "printsettings.h"
#include <sys/stat.h>
#include <libgen.h>

#define ENABLE_DEBUG_LOG 0 // enable to show debug logs (only confirmed to be displayed in Xcode)

#define ENABLE_JOB_DUMP 0
#if ENABLE_JOB_DUMP

#define DUMP_DIR_NAME "_DUMP_"
#define DUMP_EXT ""

#include <sys/types.h>

#endif // ENABLE_JOB_DUMP

#ifdef IS_IN_UT

int FD_ISSET_MOCK(int socket, const fd_set * fd);
#define DP_FD_ISSET FD_ISSET_MOCK

int fseek_mock(FILE *, long, int);
#define dp_fseek fseek_mock

long ftell_mock(FILE *);
#define dp_ftell ftell_mock

size_t fread_mock(void *ptr, size_t size, size_t nmemb, FILE *stream);
#define dp_fread fread_mock

#else
#define DP_FD_ISSET FD_ISSET
#define dp_fseek fseek
#define dp_ftell ftell
#define dp_fread fread

#endif

/**
 Constants
 */
#define PORT_LPR "515"
#define PORT_RAW "9100"
#define PORT_WAKE "9" // UDP Port 9 is commonly used by default for Wake-On-LAN (other possible ports: UDP Port 0, UDP Port 7)

#define TIMEOUT_CONNECT 10
#define TIMEOUT_SEND_RECV 10

#define BUFFER_SIZE 4096

#define QUEUE_NAME "normal"
#define QUEUE_NAME_FWGDFTGL "lp"
#define HOST_NAME "RISO PRINT-S"

#define PJL_ESCAPE "\x1B%-12345X"
#define PJL_LANGUAGE "@PJL ENTER LANGUAGE = PDF\x0d\x0a"
#define PJL_EOJ "@PJL EOJ\x0d\x0a"

#define IPV6_LINK_LOCAL_PREFIX "fe80"
#define IP_ADDRESS_LENGTH 128

#define AZA_DEVICE_NAME_COUNT 3

#define MAX_PRINTJOB_UNFINISHED_PROGRESS_PERCENTAGE 99.99f
#define PRINTJOB_SENT_PROGRESS_PERCENTAGE 100.0f

#define FT_PRINTER_TYPE "FT"
#define GL_PRINTER_TYPE "GL"
#define CEREZONA_PRINTER_TYPE "CEREZONA S"

#define PDF_PJL_FILE_DIR_NAME "PDF_PJL_TMP"
#define PDF_PJL_FILENAME "PDF_PJL.pdf"
#define DUMP_EXT ""

/** @def
 * ホスト名UTF-8の16進数変換の際に使用する定数
 * 文字一文字あたりに必要な最大バイト数
 */
#define STRING_MAX_BYTE 6

#define MAC_ADDRESS_OFFSET 3
#define MAC_ADDRESS_BYTE_LENGTH 2
#define MAC_ADDRESS_BYTE_NUM 6
#define MAC_ADDRESS_DELIMITER ":"
#define IPV4_ADDRESS_BROADCAST "255.255.255.255"
#define IPV6_ADDRESS_MULTICAST "FF02::2"

static const char *AZA_DEVICE_NAMES[] = {
    "RISO IS1000C-J",
    "RISO IS1000C-G",
    "RISO IS950C-G",
};

#define FW_DEVICE_NAME_COUNT 21
#define MAX_NUM_PRINT_RETRIES 1
#define MAX_JOB_NUM_VALUE 999

enum kPrintType
{
    kPrintTypeUnknown = -1,
    kPrintTypeLPR = 0,
    kPrintTypeRAW = 1,
    kPrintTypeIPPS = 2
};

/**
 Print Job
 */
struct directprint_job_s
{
    // for ORPHIS FW start
    char *printer_name;
    char *app_name;
    char *app_version;
    // for ORPHIS FW end
    
    char *user_name;
    
    // ホスト名出力処理の追加 Start
    char *host_name;
    // ホスト名出力処理の追加 END

    int job_num;
    char *job_name;
    char *filename;
    char *print_settings;
    char *ip_address;
    char *mac_address;
    directprint_callback callback;
    
    pthread_t main_thread;
    pthread_mutex_t mutex;
    float progress;
    int cancel_print;
    int retry_print;
    enum kPrintType print_type;
    
    void *caller_data;
    
};

// Main functions
int directprint_job_lpr_print(directprint_job *print_job);
int directprint_job_raw_print(directprint_job *print_job);
int directprint_job_create_pdf_pjl(directprint_job *print_job);
void directprint_job_cancel(directprint_job *print_job);

// Direct print job accessors
void directprint_job_free(directprint_job *print_job);
void *directprint_job_get_caller_data(directprint_job *print_job);
void directprint_job_set_caller_data(directprint_job *print_job, void *caller_data);

// Utility functions
int can_start_print(directprint_job *print_job);
int connect_to_port(directprint_job *print_job, const char *port);
void notify_callback(directprint_job *print_job, int status);
int is_cancelled(directprint_job *print_job);
void send_magic_packet(directprint_job *print_job, const char *port);
int str_to_uint16(const char *str, uint16_t *res);

// Thread functions
void *do_lpr_print(void *parameter);
void *do_raw_print(void *parameter);
void *do_save_pdf_pjl(void *parameter);

// Save PDF+PJL File functions
FILE *create_pdf_pjl_file(directprint_job *print_job);
void pdf_pjl_file_write(FILE *file, void *buffer, size_t buffer_len);

#if ENABLE_JOB_DUMP

// Dump functions
FILE *job_dump_create_file(directprint_job *print_job);
void job_dump_write(FILE *file, void *buffer, size_t buffer_len);

#endif

/**
 Public Methods
 */
/**
* @brief Android(java)のコードから、印刷設定等を受け取り、print-job構造体を作成する関数
* @param プリンター名、ホスト名、アプリ名、アプリバージョン、ユーザ名、ジョブ名、ファイル名、印刷設定、IPアドレス、コールバック
* @return 構造体print_job
*/
directprint_job *directprint_job_new(const char *printer_name, const char *host_name, const char *app_name, const char *app_version,
                                     const char *user_name, int job_num, const char *job_name, const char *filename,
                                     const char *print_settings, const char *ip_address, const char *mac_address, directprint_callback callback)
{
    directprint_job *print_job = (directprint_job *)malloc(sizeof(directprint_job));
    if(print_job == NULL){
        return NULL;
    }
    print_job->app_name = strdup(app_name);
    if(print_job->app_name == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->app_version = strdup(app_version);
    if(print_job->app_version == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->user_name = strdup(user_name);
    if(print_job->user_name == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->job_name = strdup(job_name);
    if(print_job->job_name == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->filename = strdup(filename);
    if(print_job->filename == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->print_settings = strdup(print_settings);
    if(print_job->print_settings == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->printer_name = strdup(printer_name);
    if(print_job->printer_name == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->job_num = job_num;
    // Mantis 71486 start
    char* host_namebuf = ConvertUTF8String((char*)host_name);
    if(host_namebuf == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    print_job->host_name = strdup(host_namebuf);
    /* To prevent memory leaks, free the buffer pointer. */
    free(host_namebuf);
    if (print_job->host_name == NULL)
    {
        directprint_job_free(print_job);
        return NULL;
    }
    // Mantis 71486 end
    
    print_job->mac_address = strdup(mac_address);
    if(print_job->mac_address == NULL){
        directprint_job_free(print_job);
        return NULL;
    }
    
    // IP address check
    struct in6_addr ip_v6;
    int result = inet_pton(AF_INET6, ip_address, &ip_v6);
    if (result == 1)
    {
        if (strncmp(ip_address, IPV6_LINK_LOCAL_PREFIX, strlen(IPV6_LINK_LOCAL_PREFIX)) == 0)
        {
            char ipv6_address[IP_ADDRESS_LENGTH];
            snprintf(ipv6_address, IP_ADDRESS_LENGTH - 1, "%s%%en0", ip_address);
            print_job->ip_address = strdup(ipv6_address);
        }
        else
        {
            print_job->ip_address = strdup(ip_address);
        }
    }
    else
    {
        print_job->ip_address = strdup(ip_address);
    }
    
    print_job->callback = callback;
    
    print_job->cancel_print = 0;
    print_job->retry_print = 0;
    print_job->print_type = kPrintTypeUnknown;
    print_job->progress = 0.0f;
    pthread_mutex_init(&print_job->mutex, 0);
    
    return print_job;
}
/*
 * UTF8 16進数変換
 * [in] 変換前文字列
 * [out] 変換後文字列 or NULL失敗
 */
char* ConvertUTF8String(char* str){
    // Mantis 71486 Start
    char* buf = (char *)calloc(strlen(str) * STRING_MAX_BYTE + 1, sizeof(char));
    if (buf == NULL){
        return NULL;
    }
    //1文字あたりの最大バイト数(6)の領域を確保
    char* hex = (char *)malloc(STRING_MAX_BYTE + 1);
    if (hex == NULL){
        free(buf);
        return NULL;
    }
    // (char)NULL change to '\0' to prevent warning
    for (int i = 0; str[i] != '\0'; i++){
        sprintf(hex, "%x", str[i] & 0x0000FF);
        buf = strcat(buf, hex);
    }
    char* resultStr = strdup(buf);
    free(hex);
    free(buf);
    
    return resultStr;
    // Mantis 71486 End
}

void directprint_job_free(directprint_job *print_job)
{
    if (print_job == NULL) {
        return;
    }
    pthread_mutex_destroy(&print_job->mutex);
    /* RM 856 Fix: if caller data still exists, free it here */
    if(print_job->caller_data != NULL){
        free(print_job->caller_data);
    }
    if(print_job->app_name != NULL){
        free(print_job->app_name);
    }
    if(print_job->app_version != NULL){
        free(print_job->app_version);
    }
    if(print_job->job_name != NULL){
        free(print_job->job_name);
    }
    if(print_job->job_name != NULL){
        free(print_job->filename);
    }
    if(print_job->print_settings != NULL){
        free(print_job->print_settings);
    }
    if(print_job->ip_address != NULL){
        free(print_job->ip_address);
    }
    if(print_job->mac_address != NULL){
        free(print_job->mac_address);
    }
    if(print_job->printer_name != NULL){
        free(print_job->printer_name);
    }
    if(print_job->host_name != NULL){
        free(print_job->host_name);
    }
    if(print_job->user_name != NULL){
        free(print_job->user_name);
    }
    free(print_job);
}

void *directprint_job_get_caller_data(directprint_job *print_job)
{
    if (print_job == NULL) {
        return NULL;
    }
    return print_job->caller_data;
}

void directprint_job_set_caller_data(directprint_job *print_job, void *caller_data)
{
    if (print_job == NULL) {
        return;
    }
    print_job->caller_data = caller_data;
}

int directprint_job_lpr_print(directprint_job *print_job)
{
    if (can_start_print(print_job) != 1)
    {
        return kJobStatusError;
    }
    
    pthread_create(&print_job->main_thread, 0, do_lpr_print, (void *)print_job);
    
    return kJobStatusStarted;
}

int directprint_job_raw_print(directprint_job *print_job)
{
    if (can_start_print(print_job) != 1)
    {
        return kJobStatusError;
    }
    
    pthread_create(&print_job->main_thread, 0, do_raw_print, (void *)print_job);
    
    return kJobStatusStarted;
}

int directprint_job_create_pdf_pjl(directprint_job *print_job)
{
    if (can_start_print(print_job) != 1)
    {
        return kJobStatusError;
    }

    pthread_create(&print_job->main_thread, 0, do_save_pdf_pjl, (void *)print_job);

    return kJobStatusStarted;
}

void directprint_job_cancel(directprint_job *print_job)
{
    if (print_job == NULL) {
        return;
    }
    pthread_mutex_lock(&print_job->mutex);
    print_job->cancel_print = 1;
    pthread_mutex_unlock(&print_job->mutex);
    pthread_join(print_job->main_thread, 0);
}

/**
 Helper Methods
 */
int can_start_print(directprint_job *print_job)
{
    if (print_job == 0)
    {
        return 0;
    }
    // ホスト名出力処理の追加 Start
    if (print_job->host_name == 0)
    {
        return 0;
    }
    // ホスト名出力処理の追加 End
    if (print_job->user_name == 0)
    {
        return 0;
    }

    if (print_job->job_name == 0 || strlen(print_job->job_name) <= 0)
    {
        return 0;
    }
    if (print_job->filename == 0 || strlen(print_job->filename) <= 0)
    {
        return 0;
    }
    if (print_job->printer_name == 0 || strlen(print_job->printer_name) <= 0)
    {
        return 0;
    }
    if (print_job->print_settings == 0 || strlen(print_job->print_settings) <= 0)
    {
        return 0;
    }
    if (print_job->ip_address == 0 || strlen(print_job->ip_address) <= 0)
    {
        return 0;
    }
    if (print_job->mac_address == 0)
    {
        return 0;
    }
    
    return 1;
}

int connect_to_port(directprint_job *print_job, const char *port)
{
    struct addrinfo hints;
    struct addrinfo *server_info;
    
    memset(&hints, 0, sizeof(hints));
    hints.ai_flags = AI_ADDRCONFIG;
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    
    if (getaddrinfo(print_job->ip_address, port, &hints, &server_info) != 0)
    {
        // Unable to get address info
        return -1;
    }
    
    struct addrinfo *current_address;
    int sock_fd = -1;
    bool is_wakeonlan_done = false;
    bool is_connection_complete = false;
    for (current_address = server_info; current_address != 0; current_address = current_address->ai_next)
    {
        // Try to create socket
        if ((sock_fd = socket(current_address->ai_family, current_address->ai_socktype, current_address->ai_protocol)) == -1)
        {
            // Unable to create socket
            continue;
        }
        
        // Set socket to non-blocking mode
        int flags = fcntl(sock_fd, F_GETFL, 0);
        flags |= O_NONBLOCK;
        fcntl(sock_fd, F_SETFL, flags);
        
        do {
            // Try to establish connection
#if ENABLE_DEBUG_LOG
            printf("connect to socket start\n");
#endif
            if (connect(sock_fd, current_address->ai_addr, current_address->ai_addrlen) == -1)
            {
#if ENABLE_DEBUG_LOG
                printf("connect to socket end\n");
#endif
                if (errno != EINPROGRESS)
                {
                    // Unable to connect
#if ENABLE_DEBUG_LOG
                    printf("unable to connect\n");
#endif
                    sock_fd = -1;
                    break;
                }
                
                fd_set write_fds;
                FD_ZERO(&write_fds);
                FD_SET(sock_fd, &write_fds);
                struct timeval timeout;
                timeout.tv_sec = TIMEOUT_CONNECT;
                timeout.tv_usec = 0;
#if ENABLE_DEBUG_LOG
                printf("select start\n");
#endif
                select(sock_fd + 1, 0, &write_fds, 0, &timeout);
#if ENABLE_DEBUG_LOG
                printf("select end\n");
#endif
                
                if (!DP_FD_ISSET(sock_fd, &write_fds))
                {
                    // Timeout
                    if (is_wakeonlan_done == false)
                    {
                        send_magic_packet(print_job, PORT_WAKE);
                        is_wakeonlan_done = true;
                        if (is_cancelled(print_job) == 1)
                        {
                            sock_fd = -1;
                            break;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    else
                    {
                        sock_fd = -1;
                        break;
                    }
                }
                
                int error;
                socklen_t error_len = sizeof(error);
                if (getsockopt(sock_fd, SOL_SOCKET, SO_ERROR, &error, &error_len) < 0 || error != 0)
                {
                    // Unable to complete connection
                    if (is_wakeonlan_done == false)
                    {
                        send_magic_packet(print_job, PORT_WAKE);
                        is_wakeonlan_done = true;
                        if (is_cancelled(print_job) == 1)
                        {
                            sock_fd = -1;
                            break;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    else
                    {
                        sock_fd = -1;
                        break;
                    }
                }
                
                is_connection_complete = true;
            }
            else
            {
                sock_fd = -1;
                break;
            }
        } while (is_wakeonlan_done == true && is_connection_complete == false);
        
        if (is_connection_complete == true) {
            // Set socket to blocking mode
            flags &= ~O_NONBLOCK;
            fcntl(sock_fd, F_SETFL, flags);
            break;
        }
    }
    
    if (sock_fd == -1) {
        close(sock_fd);
#if ENABLE_DEBUG_LOG
        printf("   socket closed -- print job\n");
#endif
    }
    
    freeaddrinfo(server_info);
    
    return sock_fd;
}

void send_magic_packet(directprint_job *print_job, const char *port)
{
    int                  client_s;           // Client socket descriptor
    struct sockaddr_in   target_addr;        // Target Internet address
    int                  pkt_len;            // Packet length
    char                 out_buf[1024];      // Output buffer for data
    int                  retcode;            // Return code
    int                  i, j;               // Loop counter
    uint16_t             sin_port;
    char                 *tp;                // Token pointer
    char                 *mac_address_copy = (char *)malloc(strlen(print_job->mac_address) + 1);  // MAC address buffer for strtok
    char                 mac_s[MAC_ADDRESS_BYTE_NUM][MAC_ADDRESS_BYTE_LENGTH+1];
    int                  mac_i[MAC_ADDRESS_BYTE_NUM];

    if (is_cancelled(print_job) == 1)
    {
        if (mac_address_copy != NULL) {
            free(mac_address_copy);
            mac_address_copy = NULL;
        }
        return;
    }
    
#if ENABLE_DEBUG_LOG
    printf("Generating magic packet... \n");
#endif
    // Since strtok modifies source string, store mac address first in buffer
    if (mac_address_copy == NULL || strcmp(mac_address_copy,"") == 1) {
#if ENABLE_DEBUG_LOG
        printf("*** ERROR - NULL mac address \n");
#endif
        if (mac_address_copy != NULL) {
            free(mac_address_copy);
            mac_address_copy = NULL;
        }
        return;
    }
    strcpy(mac_address_copy, print_job->mac_address);
    
    // Extract MAC Address bytes
    tp = strtok((char*)mac_address_copy, ":");
    if (tp == NULL) {
#if ENABLE_DEBUG_LOG
        printf("*** ERROR - NULL mac address \n");
#endif
        free(mac_address_copy);
        mac_address_copy = NULL;
        return;
    }
    
    strcpy(mac_s[0], tp);
    mac_i[0] = (int)strtol(mac_s[0], NULL, 16);
    for (i=1; i<MAC_ADDRESS_BYTE_NUM; i++) {
        tp = strtok(NULL, ":");
        if (tp != NULL) {
            strcpy(mac_s[i], tp);
            mac_i[i] = (int)strtol(mac_s[i], NULL, 16);
        }
    }
    
    if(mac_i[0] == 0x00 && mac_i[1] == 0x00 && mac_i[2] == 0x00 &&
       mac_i[3] == 0x00 && mac_i[4] == 0x00 && mac_i[5] == 0x00)
    {
#if ENABLE_DEBUG_LOG
        printf("*** ERROR - MAC address is blank \n");
#endif
        free(mac_address_copy);
        mac_address_copy = NULL;
        return;
    }
#if ENABLE_DEBUG_LOG
    printf("  Target MAC address = %02X-%02X-%02X-%02X-%02X-%02X \n",
           mac_i[0], mac_i[1], mac_i[2], mac_i[3], mac_i[4], mac_i[5]);
#endif
    
    // Fill-in target address information
    if (strncmp(print_job->ip_address, IPV6_LINK_LOCAL_PREFIX, strlen(IPV6_LINK_LOCAL_PREFIX)) == 0)
    {   // ipv6
#if ENABLE_DEBUG_LOG
        printf("  Target IP address  = %s \n", IPV6_ADDRESS_MULTICAST);
#endif
        target_addr.sin_addr.s_addr = inet_addr(IPV6_ADDRESS_MULTICAST);
        target_addr.sin_family = AF_INET6;
    }
    else
    {   // ipv4
#if ENABLE_DEBUG_LOG
        printf("  Target IP address  = %s \n", IPV4_ADDRESS_BROADCAST);
#endif
        target_addr.sin_addr.s_addr = inet_addr(IPV4_ADDRESS_BROADCAST);
        target_addr.sin_family = AF_INET;
    }
    str_to_uint16(port, &sin_port);
    target_addr.sin_port = htons(sin_port);
    
    // Create a client socket
    client_s = socket(target_addr.sin_family, SOCK_DGRAM, IPPROTO_UDP);
    if (client_s < 0)
    {
#if ENABLE_DEBUG_LOG
        printf("*** ERROR - socket() failed \n");
#endif
        free(mac_address_copy);
        mac_address_copy = NULL;
        return;
    }
    
    int broadcast=1;
    if (setsockopt(client_s, SOL_SOCKET, SO_BROADCAST, &broadcast, sizeof(broadcast))==-1) {
#if ENABLE_DEBUG_LOG
        printf("*** ERROR - %s",strerror(errno));
#endif
    }
    
    // Load the Magic Packet pattern into the output buffer
    for(i=0; i<6; i++)
        out_buf[i] = 0xff;
    for(i=0; i<16; i++)
    {
        for(j=0; j<MAC_ADDRESS_BYTE_NUM; j++)
        {
            out_buf[(i+1)*6 + j] = mac_i[j];
        }
    }
    pkt_len = 102;
    
    // Now send the Magic Packet to the target
    notify_callback(print_job, kJobStatusWaking);
    for (i=0; i < 2; i++) {
#if ENABLE_DEBUG_LOG
        printf("Sending Magic Packet to target... %d of 2\n", i+1);
#endif
        retcode = (int) sendto(client_s, out_buf, pkt_len, 0,
                         (struct sockaddr *)&target_addr, sizeof(target_addr));
        if (retcode < 0)
        {
#if ENABLE_DEBUG_LOG
            printf("*** ERROR - sendto() failed \n");
#endif
            break;
        }
        
        // Wait for the packet to be sent
        // 1st loop: 5 seconds, 2nd loop: 10 seconds
#if ENABLE_DEBUG_LOG
        printf("Start sleep for %d seconds \n", 5*(i+1));
#endif
        for(j=0; j<(5*(i+1)); j++)
        {
            if (is_cancelled(print_job) == 1)
            {
                break;
            }
            sleep(1);
        }
        if (is_cancelled(print_job) == 1)
        {
            break;
        }
    }
    notify_callback(print_job, kJobStatusConnecting);
    
    // Free mac address buffer
    free(mac_address_copy);
    mac_address_copy = NULL;
    
    // Close client socket and clean-up
    retcode = close(client_s);
    if (retcode < 0)
    {
#if ENABLE_DEBUG_LOG
        printf("*** ERROR - close() failed \n");
#endif
        return;
    }
#if ENABLE_DEBUG_LOG
    printf("   socket closed -- magic packet\n");
#endif
}

int str_to_uint16(const char *str, uint16_t *res) {
    char *end;
    errno = 0;
    long val = strtol(str, &end, 10);
    if (errno || end == str || *end != '\0' || val < 0 || val >= 0x10000) {
        return 1;
    }
    *res = (uint16_t)val;
    return 0;
}

void notify_callback(directprint_job *print_job, int status)
{
    /* RM 496 Fix: if cancel is performed during print_job send, job free is performed
       at end of do_lpr/raw_print*/
    if (is_cancelled(print_job) == 1)
    {
        return;
    }
    
    if (status <= kJobStatusError)
    {
        print_job->retry_print += 1;
        
        /* Do not send an error status on a retry of an LPR print job. */
        /* Instead, inform the application that the job number will be incremented. */
        if ((print_job->print_type == kPrintTypeLPR) &&
            (print_job->retry_print <= MAX_NUM_PRINT_RETRIES))
        {
            status = kJobStatusJobNumUpdate;
            /* Increment the job number */
            print_job->job_num = (print_job->job_num + 1) % (MAX_JOB_NUM_VALUE + 1);
        }
    }
    else if (status == kJobStatusSent)
    {
        print_job->retry_print = 0;
    }

    if (print_job->callback != 0)
    {
        print_job->callback(print_job, status, print_job->progress);
#if ENABLE_DEBUG_LOG
        printf("notify callback %d\n", status);
#endif
    }
}

int is_cancelled(directprint_job *print_job)
{
    if (print_job == NULL) {
        return 0;
    }
    pthread_mutex_lock(&print_job->mutex);
    int cancelled = print_job->cancel_print;
    pthread_mutex_unlock(&print_job->mutex);
    return cancelled;
}

int is_ISSeries(const char* printer_name)
{
    int index = 0;

    if (printer_name == NULL) {
        return 0;
    }
    
    for (index = 0; index < AZA_DEVICE_NAME_COUNT; index++)
    {
        if (strcmp(AZA_DEVICE_NAMES[index], printer_name) == 0) {
            return 1;
        }
    }
    return 0;
}

int is_FWSeries(const char* printer_name)
{
    // Mantis:68382
    if(printer_name && (strstr(printer_name," FW") != NULL)){//printer_nameに「 FW」があるならば
        return 1;
    }
    
    return 0;
}

/**
 Thread functions
 */
void *do_lpr_print(void *parameter)
{
    directprint_job *print_job = (directprint_job *)parameter;
    
    // RM 496 Fix: Add checking of is_cancelled and perform job free
    if (is_cancelled(print_job) == 1)
    {
        directprint_job_free(print_job);
        return 0;
    }

    char queueName[64];
    // Set Print Type
    print_job->print_type = kPrintTypeLPR;

    // Prepare PJL header
    char pjl_header[2048];
    pjl_header[0] = 0;
    strcat(pjl_header, PJL_ESCAPE);
    long pjl_header_size = strlen(pjl_header);
    if (is_ISSeries(print_job->printer_name))   // IS
    {
        create_pjl(pjl_header, print_job->print_settings);
        strcpy(queueName, QUEUE_NAME);
    }
    else if (is_FWSeries(print_job->printer_name)) // FW Series
    {
        create_pjl_fw(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
        strcpy(queueName, QUEUE_NAME_FWGDFTGL);
    }
    else if (print_job->printer_name &&
             ((strstr(print_job->printer_name, FT_PRINTER_TYPE) != NULL) ||
              (strstr(print_job->printer_name, CEREZONA_PRINTER_TYPE) != NULL))) // FT Series / CEREZONA Series
    {
        create_pjl_ft(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
        strcpy(queueName, QUEUE_NAME_FWGDFTGL);
    }
    else if (print_job->printer_name &&
             (strstr(print_job->printer_name, GL_PRINTER_TYPE) != NULL)) // GL Series
    {
        create_pjl_gl(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
        strcpy(queueName, QUEUE_NAME_FWGDFTGL);
    }
    else    // GD Series
    {
        create_pjl_gd(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
            strcpy(queueName, QUEUE_NAME_FWGDFTGL);
    }
    
    // RM 496 Fix: Check if create_pjl was not performed
    if (strlen(pjl_header) == pjl_header_size) {
        return 0;
    }
    
    strcat(pjl_header, PJL_LANGUAGE);
    pjl_header_size = strlen(pjl_header);
    
    //Mantis 77780 PJLフッダーは不要なためコメントアウト
    // Prepare PJL footer
    //char pjl_footer[256];
    //pjl_footer[0] = 0;
    //strcat(pjl_footer, PJL_ESCAPE);
    //strcat(pjl_footer, PJL_EOJ);
    //strcat(pjl_footer, PJL_ESCAPE);
    //long pjl_footer_size = strlen(pjl_footer);
    //Mantis 77780 end
    
    // RM 496 Fix: Perform job free
    if (is_cancelled(print_job) == 1)
    {
        directprint_job_free(print_job);
        return 0;
    }

    int sock_fd = -1;
    FILE *fd = 0;
    unsigned char *buffer = (unsigned char *)malloc(BUFFER_SIZE);
    
#if ENABLE_JOB_DUMP
    FILE *dump_fd = job_dump_create_file(print_job);
#endif
    
    do
    {
        notify_callback(print_job, kJobStatusConnecting);
        sock_fd = connect_to_port(print_job, PORT_LPR);
        if (sock_fd < 0)
        {
            notify_callback(print_job, kJobStatusErrorConnecting);
            break;
        }
        notify_callback(print_job, kJobStatusConnected);
        
        // Setup receive timeout
        struct timeval tv;
        tv.tv_sec = TIMEOUT_SEND_RECV;
        tv.tv_usec = 0;
        setsockopt(sock_fd, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv, sizeof(struct timeval));
        setsockopt(sock_fd, SOL_SOCKET, SO_SNDTIMEO, (char *)&tv, sizeof(struct timeval));
        //Suppress throwing sigpipes on socket so application will not crash when socket error occurs.

#ifdef SO_NOSIGPIPE
        int no_sigpipe = 1;
        setsockopt(sock_fd, SOL_SOCKET, SO_NOSIGPIPE, (char *)&no_sigpipe, sizeof(no_sigpipe));
#endif
        
        if (is_cancelled(print_job) == 1)
        {
            break;
        }
        
        // Open file
        fd = fopen(print_job->filename, "rb");
        if (!fd)
        {
            notify_callback(print_job, kJobStatusErrorFile);
            break;
        }
        
        // Prepare flags
        ssize_t send_size;
        ssize_t recv_size;
        int has_error = 0;
        int response;
        int pos;
        unsigned long len;
        int i;
        
        notify_callback(print_job, kJobStatusSending);
        // Send queue info
        pos = 0;
        buffer[pos++] = 0x2;
        len = strlen(queueName);
        for (i = 0; i < len; i++)
        {
            buffer[pos++] = queueName[i];
        }
        buffer[pos++] = '\n';
        
        if (is_cancelled(print_job) == 1)
        {
            break;
        }
    
        send_size = send(sock_fd, buffer, pos, 0);
        if (send_size != pos)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }
        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }
        
        // CONTROL FILE : Prepare
        char dname[128];
        char cname[128];
        char control_file[1024];
        sprintf(dname, "dfA%03d%s", print_job->job_num, HOST_NAME);
        sprintf(cname, "cfA%03d%s", print_job->job_num, HOST_NAME);
        
        sprintf(control_file, "H%s\nP%s\nJ%s\nf%s\nU%s\nN%s\n", HOST_NAME, print_job->user_name, print_job->job_name, dname, dname, print_job->job_name);
        
        // CONTROL FILE INFO :  Prepare
        pos = 0;
        buffer[pos++] = 0x2;
        char control_file_len_str[8];
        sprintf(control_file_len_str, "%d", (int)strlen(control_file));
        len = strlen(control_file_len_str);
        for (int i = 0; i < len; i++)
        {
            buffer[pos++] = control_file_len_str[i];
        }
        buffer[pos++] = ' ';
        len = strlen(cname);
        for (int i = 0; i < len; i++)
        {
            buffer[pos++] = cname[i];
        }
        buffer[pos++] = '\n';

        if (is_cancelled(print_job) == 1)
        {
            break;
        }
    
        // CONTROL FILE INFO : Send
        send_size = send(sock_fd, buffer, pos, 0);
        if (send_size != pos)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }
        
        notify_callback(print_job, kJobStatusSending);

        // CONTROL FILE INFO : Acknowledgement
        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }

        // CONTROL FILE : Send
        pos = 0;
        len = strlen(control_file);
        for (int i = 0; i < len; i++)
        {
            buffer[pos++] = control_file[i];
        }
        buffer[pos++] = 0;

        if (is_cancelled(print_job) == 1)
        {
            break;
        }
    
        send_size = send(sock_fd, buffer, pos, 0);
        if (send_size != pos)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }
        
        notify_callback(print_job, kJobStatusSending);


        // CONTROL FILE : Acknowledgement
        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }

        // Get file size
        dp_fseek(fd, 0L, SEEK_END);
        long file_size = dp_ftell(fd);
        dp_fseek(fd, 0L, SEEK_SET);
        
        // DATA FILE INFO : Prepare
        size_t total_data_size = file_size + pjl_header_size; //Mantis77780のためコメントアウト + pjl_footer_size;
        char data_file_len_str[32];
        sprintf(data_file_len_str, "%ld", (long)total_data_size);
        len = strlen(data_file_len_str);
        pos = 0;
        buffer[pos++] = 0x3;
        for (int i = 0; i < len; i++)
        {
            buffer[pos++] = data_file_len_str[i];
        }
        buffer[pos++] = ' ';
        len = strlen(dname);
        for (int i = 0; i < len; i++)
        {
            buffer[pos++] = dname[i];
        }
        buffer[pos++] = '\n';

        if (is_cancelled(print_job) == 1)
        {
            break;
        }
    
        // DATA FILE INFO : Send
        send_size = send(sock_fd, buffer, pos, 0);
        if (send_size != pos)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }
        
        notify_callback(print_job, kJobStatusSending);

        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }

        if (is_cancelled(print_job) == 1)
        {
            break;
        }
        
        // Calculate progress step
        unsigned long step_count = file_size / BUFFER_SIZE + 1;
        float data_step = (MAX_PRINTJOB_UNFINISHED_PROGRESS_PERCENTAGE / (float)step_count);
        float progress = 0.0f;
        
        // DATA FILE : Send
        size_t read = 0;
        send(sock_fd, pjl_header, strlen(pjl_header), 0);
#if ENABLE_JOB_DUMP
        job_dump_write(dump_fd, pjl_header, strlen(pjl_header));
#endif
        notify_callback(print_job, kJobStatusSending);
        while(0 < (read = dp_fread(buffer, 1, BUFFER_SIZE, fd)))
        {
            if (is_cancelled(print_job) == 1)
            {
                break;
            }
    
            send_size = send(sock_fd, buffer, read, 0);
            if (send_size != read) {
                notify_callback(print_job, kJobStatusErrorSending);
                has_error = 1;
                break;
            }
#if ENABLE_JOB_DUMP
            job_dump_write(dump_fd, buffer, read);
#endif

            if (print_job->progress > progress)
            {
                progress += data_step;
            }
            else
            {
                print_job->progress += data_step;
                progress = 100.0f;
            }

            // To prevent user from seeing 100% progress before a successful print job, set to 99.99% instead
            if(print_job->progress >= PRINTJOB_SENT_PROGRESS_PERCENTAGE) {
                print_job->progress = MAX_PRINTJOB_UNFINISHED_PROGRESS_PERCENTAGE;
            }

            notify_callback(print_job, kJobStatusSending);
        }
        
        if (has_error == 1) {
            break;
        }
        
        if (is_cancelled(print_job) == 1)
        {
            break;
        }
        //Mantis 77780 PJLフッダーは不要なためコメントアウト
        //send_size = send(sock_fd, pjl_footer, strlen(pjl_footer), 0);
#if ENABLE_JOB_DUMP
        //job_dump_write(dump_fd, pjl_footer, strlen(pjl_footer));
#endif
        //if (send_size != strlen(pjl_footer)) {
        //    notify_callback(print_job, kJobStatusErrorSending);
        //    break;
        //}
        //Mantis 77780 end
        notify_callback(print_job, kJobStatusSending);
        
        pos = 0;
        buffer[pos++] = 0;
        send(sock_fd, buffer, pos, 0);

        // DATA FILE : Acknowledgement
        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }
        
        // Notify success
        print_job->progress = 100.0f;
        notify_callback(print_job, kJobStatusSending);
        notify_callback(print_job, kJobStatusSent);
    } while (0);
    
#if ENABLE_JOB_DUMP
    if (dump_fd != 0)
    {
        fclose(dump_fd);
    }
#endif
    
    if (fd != 0)
    {
        fclose(fd);
    }
    close(sock_fd);
    free(buffer);
    
    /* Retry only once */
    if ((print_job->retry_print > 0) && (print_job->retry_print <= MAX_NUM_PRINT_RETRIES))
    {
        pthread_create(&print_job->main_thread, 0, do_lpr_print, (void *)print_job);
    }
    else
    {
        // If print job is complete, perform job free here
        directprint_job_free(print_job);
        print_job = NULL;
    }
    return 0;
}

void *do_raw_print(void *parameter)
{
    directprint_job *print_job = (directprint_job *)parameter;

    // RM 496 Fix: add checking of is_cancelled and perform job free
    if (is_cancelled(print_job) == 1)
    {
        directprint_job_free(print_job);
        return 0;
    }

    // Set Print Type
    print_job->print_type = kPrintTypeRAW;

    // Prepare PJL header
    char pjl_header[2048];
    pjl_header[0] = 0;
    strcat(pjl_header, PJL_ESCAPE);
    long pjl_header_size = strlen(pjl_header);
    if (is_ISSeries(print_job->printer_name))   // IS
    {
        create_pjl(pjl_header, print_job->print_settings);
    }
    else if (is_FWSeries(print_job->printer_name)) // FW Series
    {
        // Ver.2.0.0.3 start
//        create_pjl_fw(pjl_header, print_job->print_settings, print_job->app_name, print_job->app_version, print_job->host_name);
        // Ver.2.0.0.3 end
        // Ver.3.0.0.0 start
        create_pjl_fw(pjl_header, print_job->print_settings, print_job->printer_name, print_job->app_version, print_job->host_name);
        // Ver.3.0.0.0 end

    }
    else if (print_job->printer_name &&
             ((strstr(print_job->printer_name, FT_PRINTER_TYPE) != NULL) ||
              (strstr(print_job->printer_name, CEREZONA_PRINTER_TYPE) != NULL))) // FT Series / CEREZONA Series
    {
        create_pjl_ft(pjl_header, print_job->print_settings, print_job->printer_name, print_job->app_version, print_job->host_name);
    }
    else if (print_job->printer_name &&
             strstr(print_job->printer_name, GL_PRINTER_TYPE) != NULL) // GL Series
    {
        create_pjl_gl(pjl_header, print_job->print_settings, print_job->printer_name, print_job->app_version, print_job->host_name);
    }
    else    // GD Series
    {
        // Ver.2.0.0.3 start
//        create_pjl_gd(pjl_header, print_job->print_settings, print_job->app_name, print_job->app_version, print_job->host_name);
        // Ver.2.0.0.3 end
        // Ver.3.0.0.0 start
        create_pjl_gd(pjl_header, print_job->print_settings, print_job->printer_name, print_job->app_version, print_job->host_name);
        // Ver.3.0.0.0 end
    }
    
    // RM 496 Fix: Check if create_pjl was not performed
    if (strlen(pjl_header) == pjl_header_size) {
        return 0;
    }
    
    strcat(pjl_header, PJL_LANGUAGE);
    
    //Mantis 77780 PJLフッダーは不要なためコメントアウト
    // Prepare PJL footer
    //char pjl_footer[256];
    //pjl_footer[0] = 0;
    //strcat(pjl_footer, PJL_ESCAPE);
    //strcat(pjl_footer, PJL_EOJ);
    //strcat(pjl_footer, PJL_ESCAPE);
    //Mantis 77780 end
    
    // RM 496 Fix: Perform job free
    if (is_cancelled(print_job) == 1)
    {
        directprint_job_free(print_job);
        return 0;
    }
    
    int sock_fd = -1;
    FILE *fd = 0;
    unsigned char *buffer = (unsigned char *)malloc(BUFFER_SIZE);
    
#if ENABLE_JOB_DUMP
    FILE *dump_fd = job_dump_create_file(print_job);
#endif
    
    do
    {
        notify_callback(print_job, kJobStatusConnecting);
        sock_fd = connect_to_port(print_job, PORT_RAW);
        if (sock_fd < 0)
        {
            notify_callback(print_job, kJobStatusErrorConnecting);
            directprint_job_free(print_job);
            print_job = NULL;
            break;
        }
        notify_callback(print_job, kJobStatusConnected);
        
        // Setup receive timeout
        struct timeval tv;
        tv.tv_sec = TIMEOUT_SEND_RECV;
        tv.tv_usec = 0;
        setsockopt(sock_fd, SOL_SOCKET, SO_SNDTIMEO, (char *)&tv, sizeof(struct timeval));
        setsockopt(sock_fd, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv, sizeof(struct timeval));
        //Suppress throwing sigpipes on socket so application will not crash when socket error occurs.
#ifdef SO_NOSIGPIPE
        int no_sigpipe = 1;
        setsockopt(sock_fd, SOL_SOCKET, SO_NOSIGPIPE, (char *)&no_sigpipe, sizeof(no_sigpipe));
#endif
        
        if (is_cancelled(print_job) == 1)
        {
            break;
        }
        
        // Open file
        fd = fopen(print_job->filename, "rb");
        if (!fd)
        {
            notify_callback(print_job, kJobStatusErrorFile);
            directprint_job_free(print_job);
            print_job = NULL;
            break;
        }
        
        // Get file size
        dp_fseek(fd, 0L, SEEK_END);
        long file_size = dp_ftell(fd);
        dp_fseek(fd, 0L, SEEK_SET);
        
        // Calculate progress step
        unsigned long step_count = file_size / BUFFER_SIZE + 1;
        float data_step = (MAX_PRINTJOB_UNFINISHED_PROGRESS_PERCENTAGE / (float)step_count);
        
        // Send header
        send(sock_fd, pjl_header, strlen(pjl_header), 0);
#if ENABLE_JOB_DUMP
        job_dump_write(dump_fd, pjl_header, strlen(pjl_header));
#endif
        notify_callback(print_job, kJobStatusSending);
        
        // Send file
        size_t read;
        size_t sent;
        int has_error = 0;
        while(0 < (read = dp_fread(buffer, 1, BUFFER_SIZE, fd)))
        {
            if (is_cancelled(print_job) == 1)
            {
                break;
            }
    
            sent = send(sock_fd, buffer, read, 0);
            if (sent != read)
            {
                notify_callback(print_job, kJobStatusErrorSending);
                directprint_job_free(print_job);
                print_job = NULL;
                has_error = 1;
                break;
            }
#if ENABLE_JOB_DUMP
            job_dump_write(dump_fd, buffer, read);
#endif

            print_job->progress += data_step;

            // To prevent user from seeing 100% progress before a successful print job, set to 99.99% instead
            if(print_job->progress >= PRINTJOB_SENT_PROGRESS_PERCENTAGE) {
                print_job->progress = MAX_PRINTJOB_UNFINISHED_PROGRESS_PERCENTAGE;
            }

            notify_callback(print_job, kJobStatusSending);
        }
        
        if (has_error == 1 || is_cancelled(print_job) == 1)
        {
            break;
        }
        
        //Mantis 77780 PJLフッダーは不要なためコメントアウト
        //send(sock_fd, pjl_footer, strlen(pjl_footer), 0);
#if ENABLE_JOB_DUMP
        //job_dump_write(dump_fd, pjl_footer, strlen(pjl_footer));
        //Mantis 77780 end
#endif
        print_job->progress = 100.0f;
        notify_callback(print_job, kJobStatusSending);
        notify_callback(print_job, kJobStatusSent);
        directprint_job_free(print_job);
        print_job = NULL;
    } while (0);
    
#if ENABLE_JOB_DUMP
    if (dump_fd != 0)
    {
        fclose(dump_fd);
    }
#endif
    
    if (fd != 0)
    {
        fclose(fd);
    }
    
    close(sock_fd);
    free(buffer);
    
    // RM 496 Fix: If cancelled during sending of print job, perform job free here
    if ((print_job != NULL) && is_cancelled(print_job) == 1)
    {
        directprint_job_free(print_job);
    }
    
    return 0;
}

void *do_save_pdf_pjl(void *parameter)
{
    directprint_job *print_job = (directprint_job *)parameter;

    if (is_cancelled(print_job) == 1)
    {
        directprint_job_free(print_job);
        return 0;
    }

    // Set Print Type
    print_job->print_type = kPrintTypeIPPS;

    // Prepare PJL header
    char pjl_header[2048];
    pjl_header[0] = 0;
    strcat(pjl_header, PJL_ESCAPE);
    long pjl_header_size = strlen(pjl_header);
    if (is_ISSeries(print_job->printer_name))   // IS
    {
        create_pjl(pjl_header, print_job->print_settings);
    }
    else if (is_FWSeries(print_job->printer_name)) // FW Series
    {
        create_pjl_fw(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
    }
    else if (print_job->printer_name &&
             ((strstr(print_job->printer_name, FT_PRINTER_TYPE) != NULL) ||
              (strstr(print_job->printer_name, CEREZONA_PRINTER_TYPE) != NULL))) // FT Series / CEREZONA Series
    {
        create_pjl_ft(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
    }
    else if (print_job->printer_name &&
             (strstr(print_job->printer_name, GL_PRINTER_TYPE) != NULL)) // GL Series
    {
        create_pjl_gl(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
    }
    else    // GD Series
    {
        create_pjl_gd(pjl_header, print_job->print_settings, print_job->printer_name, print_job->host_name, print_job->app_version);
    }

    if (strlen(pjl_header) == pjl_header_size) {
        return 0;
    }

    strcat(pjl_header, PJL_LANGUAGE);

    if (is_cancelled(print_job) == 1)
    {
        return 0;
    }

    FILE *fd = 0;
    unsigned char *buffer = (unsigned char *)malloc(BUFFER_SIZE);
    // Create PDF+PJL File
    FILE *pdf_pjl_file_fd = create_pdf_pjl_file(print_job);
#if ENABLE_JOB_DUMP
    FILE *dump_fd = job_dump_create_file(print_job);
#endif

    do
    {
        if (is_cancelled(print_job) == 1)
        {
            break;
        }

        // Open file
        fd = fopen(print_job->filename, "rb");
        if (!fd)
        {
            notify_callback(print_job, kJobStatusErrorFile);
            break;
        }

        // Get file size
        dp_fseek(fd, 0L, SEEK_END);
        long file_size = dp_ftell(fd);
        dp_fseek(fd, 0L, SEEK_SET);

        // Write PJL header to GCP PDF+PJL
        pdf_pjl_file_write(pdf_pjl_file_fd, pjl_header, strlen(pjl_header));
#if ENABLE_JOB_DUMP
        job_dump_write(dump_fd, pjl_header, strlen(pjl_header));
#endif

        // Send file
        size_t read;
        size_t sent;
        int has_error = 0;
        while(0 < (read = dp_fread(buffer, 1, BUFFER_SIZE, fd)))
        {
            if (is_cancelled(print_job) == 1)
            {
                break;
            }
            // Write PDF content to GCP PDF+PJL
            pdf_pjl_file_write(pdf_pjl_file_fd, buffer, read);
#if ENABLE_JOB_DUMP
            job_dump_write(dump_fd, buffer, read);
#endif
        }

        if (has_error == 1 || is_cancelled(print_job) == 1)
        {
            break;
        }

        notify_callback(print_job, kJobStatusPdfPjlCreated);
    } while (0);

    // Close PDF+PJL file pointer
    if (pdf_pjl_file_fd != 0)
    {
        fclose(pdf_pjl_file_fd);
    }

#if ENABLE_JOB_DUMP
    if (dump_fd != 0)
    {
        fclose(dump_fd);
    }
#endif

    if (fd != 0)
    {
        fclose(fd);
    }
    free(buffer);
    return 0;
}

// Save PDF+PJL File functions
FILE *create_pdf_pjl_file(directprint_job *print_job)
{
    // Prepare directory
    char *base_dir = dirname(print_job->filename);
    char *dump_dir = (char *)calloc(1, strlen(base_dir) + strlen(PDF_PJL_FILE_DIR_NAME) + 2);
    sprintf(dump_dir, "%s/%s", base_dir, PDF_PJL_FILE_DIR_NAME);
    int result = mkdir(dump_dir, S_IRWXU | S_IRWXG | S_IRWXO);
    if (result == -1 && errno != EEXIST)
    {
        return 0;
    }

    // Prepare file name
    char *dump_file_name = (char *)calloc(1, strlen(dump_dir) + strlen(PDF_PJL_FILENAME) + strlen(DUMP_EXT) + 2);
    sprintf(dump_file_name, "%s/%s%s", dump_dir, PDF_PJL_FILENAME, DUMP_EXT);

    FILE *file = fopen(dump_file_name, "wb");

    free(dump_file_name);
    free(dump_dir);
    return file;
}

void pdf_pjl_file_write(FILE *file, void *buffer, size_t buffer_len)
{
    if (file == 0)
    {
        return;
    }

    fwrite(buffer, buffer_len, 1, file);
}

#if ENABLE_JOB_DUMP

// Dump functions
FILE *job_dump_create_file(directprint_job *print_job)
{
    // Prepare directory
    char *base_dir = dirname(print_job->filename);
    char *dump_dir = (char *)calloc(1, strlen(base_dir) + strlen(DUMP_DIR_NAME) + 2);
    sprintf(dump_dir, "%s/%s", base_dir, DUMP_DIR_NAME);
    int result = mkdir(dump_dir, S_IRWXU | S_IRWXG | S_IRWXO);
    if (result == -1 && errno != EEXIST)
    {
        return 0;
    }
    
    // Prepare file name
    time_t raw_time;
    time(&raw_time);
    struct tm *time_info = localtime(&raw_time);
    char *dump_file_name = (char *)calloc(1, strlen(dump_dir) + strlen(print_job->job_name) + strlen(DUMP_EXT) + 18);
    sprintf(dump_file_name, "%s/%04d%02d%02d-%02d%02d%02d_%s%s", dump_dir, time_info->tm_year + 1900, time_info->tm_mon + 1, time_info->tm_mday, time_info->tm_hour, time_info->tm_min, time_info->tm_sec, print_job->job_name, DUMP_EXT);
    
    FILE *file = fopen(dump_file_name, "wb");
    
    free(dump_file_name);
    free(dump_dir);
    return file;
}

void job_dump_write(FILE *file, void *buffer, size_t buffer_len)
{
    if (file == 0)
    {
        return;
    }
    
    fwrite(buffer, buffer_len, 1, file);
}

#endif // ENABLE_JOB_DUMP
