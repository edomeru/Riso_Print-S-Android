//
//  directprint.c
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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
#include "common.h"
#include "printsettings.h"

#define ENABLE_JOB_DUMP 1
#if ENABLE_JOB_DUMP

#define DUMP_DIR_NAME "_DUMP_"
#define DUMP_EXT ""

#include <sys/types.h>
#include <sys/stat.h>
#include <libgen.h>

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

#define TIMEOUT_CONNECT 10
#define TIMEOUT_SEND_RECV 30

#define BUFFER_SIZE 4096

#define QUEUE_NAME "normal"
#define HOST_NAME "SmartDeviceApp"

#define PJL_ESCAPE "\x1B-12345X"
#define PJL_LANGUAGE "@PJL ENTER LANGUAGE = PDF\x0d\x0a"
#define PJL_EOJ "@PJL EOJ\x0d\x0a"

#define IPV6_LINK_LOCAL_PREFIX "fe80"
#define IP_ADDRESS_LENGTH 128

/**
 Print Job
 */
struct directprint_job_s
{
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

// Main functions
int directprint_job_lpr_print(directprint_job *print_job);
int directprint_job_raw_print(directprint_job *print_job);
void directprint_job_cancel(directprint_job *print_job);

// Direct print job accessors
directprint_job *directprint_job_new(const char *user_name, const char *job_name, const char *filename, const char *print_settings, const char *ip_address, directprint_callback callback);
void directprint_job_free(directprint_job *print_job);
void *directprint_job_get_caller_data(directprint_job *print_job);
void directprint_job_set_caller_data(directprint_job *print_job, void *caller_data);

// Utility functions
int can_start_print(directprint_job *print_job);
int connect_to_port(const char *ip_address, const char *port);
void notify_callback(directprint_job *print_job, int status);
int is_cancelled(directprint_job *print_job);

// Thread functions
void *do_lpr_print(void *parameter);
void *do_raw_print(void *parameter);

#if ENABLE_JOB_DUMP

// Dump functions
FILE *job_dump_create_file(directprint_job *print_job);
void job_dump_write(FILE *file, void *buffer, size_t buffer_len);

#endif

/**
 Public Methods
 */
directprint_job *directprint_job_new(const char *user_name, const char *job_name, const char *filename, const char *print_settings, const char *ip_address, directprint_callback callback)
{
    directprint_job *print_job = (directprint_job *)malloc(sizeof(directprint_job));
    print_job->user_name = strdup(user_name);
    print_job->job_name = strdup(job_name);
    print_job->filename = strdup(filename);
    print_job->print_settings = strdup(print_settings);
    
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
    print_job->progress = 0.0f;
    pthread_mutex_init(&print_job->mutex, 0);
    
    return print_job;
}

void directprint_job_free(directprint_job *print_job)
{
    pthread_mutex_destroy(&print_job->mutex);
    free(print_job->job_name);
    free(print_job->filename);
    free(print_job->print_settings);
    free(print_job->ip_address);
    free(print_job);
    print_job = 0;
}

void *directprint_job_get_caller_data(directprint_job *print_job)
{
    return print_job->caller_data;
}

void directprint_job_set_caller_data(directprint_job *print_job, void *caller_data)
{
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

void directprint_job_cancel(directprint_job *print_job)
{
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
    if (print_job->user_name == 0 || strlen(print_job->user_name) <= 0)
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
    if (print_job->print_settings == 0 || strlen(print_job->print_settings) <= 0)
    {
        return 0;
    }
    if (print_job->ip_address == 0 || strlen(print_job->ip_address) <= 0)
    {
        return 0;
    }
    
    return 1;
}

int connect_to_port(const char *ip_address, const char *port)
{
    struct addrinfo hints;
    struct addrinfo *server_info;
    
    memset(&hints, 0, sizeof(hints));
    hints.ai_flags = AI_ADDRCONFIG;
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    
    if (getaddrinfo(ip_address, port, &hints, &server_info) != 0)
    {
        // Unable to get address info
        return -1;
    }
    
    struct addrinfo *current_address;
    int sock_fd = -1;
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
        
        // Try to establish connection
        if (connect(sock_fd, current_address->ai_addr, current_address->ai_addrlen) == -1)
        {
            if (errno != EINPROGRESS)
            {
                // Unable to connect
                close(sock_fd);
                sock_fd = -1;
                continue;
            }
            
            fd_set write_fds;
            FD_ZERO(&write_fds);
            FD_SET(sock_fd, &write_fds);
            struct timeval timeout;
            timeout.tv_sec = TIMEOUT_CONNECT;
            timeout.tv_usec = 0;
            select(sock_fd + 1, 0, &write_fds, 0, &timeout);
            
            if (!DP_FD_ISSET(sock_fd, &write_fds))
            {
                // Timout
                close(sock_fd);
                sock_fd = -1;
                continue;
            }
            
            int error;
            socklen_t error_len = sizeof(error);
            if (getsockopt(sock_fd, SOL_SOCKET, SO_ERROR, &error, &error_len) < 0 || error != 0)
            {
                // Unable to complete connection
                close(sock_fd);
                sock_fd = -1;
                continue;
            }
            
            // Set socket to blocking mode
            flags &= ~O_NONBLOCK;
            fcntl(sock_fd, F_SETFL, flags);
            break;
        }
        else
        {
            close(sock_fd);
            sock_fd = -1;
            continue;
        }
    }
    freeaddrinfo(server_info);
    
    return sock_fd;
}

void notify_callback(directprint_job *print_job, int status)
{
    if (is_cancelled(print_job) == 1)
    {
        return;
    }
    
    if (print_job->callback != 0)
    {
        print_job->callback(print_job, status, print_job->progress);
    }
}

int is_cancelled(directprint_job *print_job)
{
    pthread_mutex_lock(&print_job->mutex);
    int cancelled = print_job->cancel_print;
    pthread_mutex_unlock(&print_job->mutex);
    return cancelled;
}

/**
 Thread functions
 */
void *do_lpr_print(void *parameter)
{
    directprint_job *print_job = (directprint_job *)parameter;
    
    // Prepare PJL header
    char pjl_header[2048];
    pjl_header[0] = 0;
    strcat(pjl_header, PJL_ESCAPE);
    create_pjl(pjl_header, print_job->print_settings);
    strcat(pjl_header, PJL_LANGUAGE);
    int pjl_header_size = strlen(pjl_header);
    
    // Prepare PJL footer
    char pjl_footer[256];
    pjl_footer[0] = 0;
    strcat(pjl_footer, PJL_ESCAPE);
    strcat(pjl_footer, PJL_EOJ);
    strcat(pjl_footer, PJL_ESCAPE);
    int pjl_footer_size = strlen(pjl_footer);
    
    if (is_cancelled(print_job) == 1)
    {
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
        sock_fd = connect_to_port(print_job->ip_address, PORT_LPR);
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
        int send_size;
        int recv_size;
        int response;
        int pos;
        int len;
        int i;
        
        notify_callback(print_job, kJobStatusSending);
        // Send queue info
        pos = 0;
        buffer[pos++] = 0x2;
        len = strlen(QUEUE_NAME);
        for (i = 0; i < len; i++)
        {
            buffer[pos++] = QUEUE_NAME[i];
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
        sprintf(dname, "dfA%d%s", 1, HOST_NAME);
        sprintf(cname, "cfA%d%s", 1, HOST_NAME);
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
        size_t total_data_size = file_size + pjl_header_size + pjl_footer_size;
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
        int step_count = file_size / BUFFER_SIZE + 1;
        float data_step = (99.0f / (float)step_count);
    
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
    
            send(sock_fd, buffer, read, 0);
#if ENABLE_JOB_DUMP
            job_dump_write(dump_fd, buffer, read);
#endif
            print_job->progress += data_step;
            notify_callback(print_job, kJobStatusSending);
        }
        
        if (is_cancelled(print_job) == 1)
        {
            break;
        }
        
        send(sock_fd, pjl_footer, strlen(pjl_footer), 0);
#if ENABLE_JOB_DUMP
        job_dump_write(dump_fd, pjl_footer, strlen(pjl_footer));
#endif
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
    return 0;
}

void *do_raw_print(void *parameter)
{
    directprint_job *print_job = (directprint_job *)parameter;
    
    // Prepare PJL header
    char pjl_header[2048];
    pjl_header[0] = 0;
    strcat(pjl_header, PJL_ESCAPE);
    create_pjl(pjl_header, print_job->print_settings);
    strcat(pjl_header, PJL_LANGUAGE);
    
    // Prepare PJL footer
    char pjl_footer[256];
    pjl_footer[0] = 0;
    strcat(pjl_footer, PJL_ESCAPE);
    strcat(pjl_footer, PJL_EOJ);
    strcat(pjl_footer, PJL_ESCAPE);
    
    if (is_cancelled(print_job) == 1)
    {
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
        sock_fd = connect_to_port(print_job->ip_address, PORT_RAW);
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
        setsockopt(sock_fd, SOL_SOCKET, SO_SNDTIMEO, (char *)&tv, sizeof(struct timeval));
        
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
        
        // Calculate progress step
        int step_count = file_size / BUFFER_SIZE + 1;
        float data_step = (99.0f / (float)step_count);
        
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
                has_error = 1;
                break;
            }
#if ENABLE_JOB_DUMP
            job_dump_write(dump_fd, buffer, read);
#endif
            
            print_job->progress += data_step;
            notify_callback(print_job, kJobStatusSending);
        }
        
        if (has_error == 1 || is_cancelled(print_job) == 1)
        {
            break;
        }
        
        send(sock_fd, pjl_footer, strlen(pjl_footer), 0);
#if ENABLE_JOB_DUMP
        job_dump_write(dump_fd, pjl_footer, strlen(pjl_footer));
#endif
        print_job->progress = 100.0f;
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
    return 0;
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
    char *dump_file_name = (char *)calloc(1, strlen(dump_dir) + strlen(print_job->job_name) + strlen(DUMP_EXT) + 16);
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