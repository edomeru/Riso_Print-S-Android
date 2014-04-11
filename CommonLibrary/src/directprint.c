//
//  directprint.c
//  SmartDeviceApp
//
//  Created by Seph on 4/9/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <netdb.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <errno.h>
#include "common.h"

/**
 Constants
 */
#define PORT_LPR "515"
#define PORT_RAW "9100"

#define TIMEOUT_CONNECT 10
#define TIMEOUT_SEND 20
#define TIMEOUT_RECEIVE 10

#define BUFFER_SIZE 4096

#define QUEUE_NAME "normal"
#define HOST_NAME "SmartDeviceApp"

/**
 Print Job
 */
struct directprint_job_s
{
    char *job_name;
    char *filename;
    char *print_settings;
    char *ip_address;
    directprint_callback callback;
    
    pthread_mutex_t mutex;
    float progress;
    int is_printing;
    
    void *caller_data;
};

int can_start_print(directprint_job *print_job);
int connect_to_port(const char *ip_address, const char *port);
void notify_callback(directprint_job *print_job, int status);
void *do_lpr_print(void *parameter);

/**
 Public Methods
 */
directprint_job *directprint_job_new(const char *job_name, const char *filename, const char *print_settings, const char *ip_address, directprint_callback callback)
{
    directprint_job *print_job = (directprint_job *)malloc(sizeof(directprint_job));
    print_job->job_name = strdup(job_name);
    print_job->filename = strdup(filename);
    print_job->print_settings = strdup(print_settings);
    print_job->ip_address = strdup(ip_address);
    print_job->callback = callback;
    
    print_job->is_printing = 0;
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

int lpr_print(directprint_job *print_job)
{
    if (can_start_print(print_job) != 1)
    {
        return kJobStatusError;
    }
    
    print_job->is_printing = 1;
    pthread_t thread;
    pthread_create(&thread, 0, do_lpr_print, (void *)print_job);
    
    return kJobStatusStarted;
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
    if (print_job->print_settings == 0 || strlen(print_job->ip_address) <= 0)
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
        return 0;
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
            
            if (!FD_ISSET(sock_fd, &write_fds))
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
    }
    freeaddrinfo(server_info);
    
    return sock_fd;
}

void notify_callback(directprint_job *print_job, int status)
{
    if (print_job->callback != 0)
    {
        print_job->callback(print_job, status, print_job->progress);
    }
}

/**
 Thread functions
 */
void *do_lpr_print(void *parameter)
{
    directprint_job *print_job = (directprint_job *)parameter;
    
    int sock_fd = -1;
    unsigned char *buffer = (unsigned char *)malloc(BUFFER_SIZE);
    do
    {
        notify_callback(print_job, kJobStatusConnecting);
        sock_fd = connect_to_port(print_job->ip_address, PORT_LPR);
        if (sock_fd < 0)
        {
            printf("Unable to connect \n");
            notify_callback(print_job, kJobStatusErrorConnecting);
            break;
        }
        notify_callback(print_job, kJobStatusConnected);
        
        // Open file
        FILE *fd = fopen(print_job->filename, "rb");
        if (!fd)
        {
            printf("Error file\n");
            notify_callback(print_job, kJobStatusErrorFile);
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
        sprintf(control_file, "H%s\nP%s\nJ%s\nf%s\nU%s\nN%s\n", HOST_NAME, "User", print_job->job_name, dname, dname, print_job->job_name);
        
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

        // CONTROL FILE INFO : Send
        send_size = send(sock_fd, buffer, pos, 0);

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
        send_size = send(sock_fd, buffer, pos, 0);

        // CONTROL FILE : Acknowledgement
        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }

        // Get file size
        fseek(fd, 0L, SEEK_END);
        long file_size = ftell(fd);
        fseek(fd, 0L, SEEK_SET);
        
        // DATA FILE INFO : Prepare
        char data_file_len_str[32];
        sprintf(data_file_len_str, "%ld", file_size);
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
        send_size = send(sock_fd, buffer, pos, 0); 

        // DATA FILE INFO : Send
        recv_size = recv(sock_fd, &response, sizeof(response), 0);
        if (recv_size <= 0 || response != 0)
        {
            notify_callback(print_job, kJobStatusErrorSending);
            break;
        }

        // DATA FILE : Send
        size_t read = 0;
        while(0 < (read = fread(buffer, 1, BUFFER_SIZE, fd)))
        {
            send(sock_fd, buffer, read, 0);
            notify_callback(print_job, kJobStatusSending);
        }
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
        notify_callback(print_job, kJobStatusSent);
    } while (0);
    
    close(sock_fd);
    free(buffer);
    return 0;
}