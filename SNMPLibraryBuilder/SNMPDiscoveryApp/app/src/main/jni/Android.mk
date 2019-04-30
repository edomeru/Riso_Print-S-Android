LOCAL_PATH := $(call my-dir)
SHARED_PATH := snmplib
include $(CLEAR_VARS)

LOCAL_EXPORT_LDLIBS := -lz
LOCAL_MODULE    := snmp
LOCAL_LDLIBS    := -L$(SYSROOT)/usr/lib -llog 
LOCAL_CFLAGS += -std=c99
LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/$(SHARED_PATH)/net-snmp/include	\
	$(LOCAL_PATH)/$(SHARED_PATH)/net-snmp/src \
	$(LOCAL_PATH)/$(SHARED_PATH)
LOCAL_SRC_FILES := \
	snmp-interface/SNMP_JNI_Wrapper.c \
	$(SHARED_PATH)/SNMP_Manager.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_client.c	\
	$(SHARED_PATH)/net-snmp/src/mib.c 	\
	$(SHARED_PATH)/net-snmp/src/parse.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_api.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp.c		\
	$(SHARED_PATH)/net-snmp/src/snmp_auth.c 	\
	$(SHARED_PATH)/net-snmp/src/asn1.c 	\
	$(SHARED_PATH)/net-snmp/src/md5.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_parse_args.c	\
	$(SHARED_PATH)/net-snmp/src/system.c 	\
	$(SHARED_PATH)/net-snmp/src/vacm.c 	\
	$(SHARED_PATH)/net-snmp/src/int64.c 	\
	$(SHARED_PATH)/net-snmp/src/read_config.c 	\
	$(SHARED_PATH)/net-snmp/src/pkcs.c		\
	$(SHARED_PATH)/net-snmp/src/snmp_debug.c 	\
	$(SHARED_PATH)/net-snmp/src/tools.c  	\
	$(SHARED_PATH)/net-snmp/src/snmp_logging.c 	\
	$(SHARED_PATH)/net-snmp/src/text_utils.c	\
	$(SHARED_PATH)/net-snmp/src/large_fd_set.c		\
	$(SHARED_PATH)/net-snmp/src/cert_util.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_openssl.c 		\
	$(SHARED_PATH)/net-snmp/src/snmpv3.c 	\
	$(SHARED_PATH)/net-snmp/src/lcd_time.c 	\
	$(SHARED_PATH)/net-snmp/src/keytools.c 	\
	$(SHARED_PATH)/net-snmp/src/scapi.c 	\
	$(SHARED_PATH)/net-snmp/src/callback.c 	\
	$(SHARED_PATH)/net-snmp/src/default_store.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_alarm.c		\
	$(SHARED_PATH)/net-snmp/src/data_list.c 	\
	$(SHARED_PATH)/net-snmp/src/oid_stash.c 	\
	$(SHARED_PATH)/net-snmp/src/fd_event_manager.c 	\
	$(SHARED_PATH)/net-snmp/src/check_varbind.c 	\
	$(SHARED_PATH)/net-snmp/src/mt_support.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_enum.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp-tc.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_service.c	\
	$(SHARED_PATH)/net-snmp/src/snprintf.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_transport.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpUDPBaseDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpUDPIPv4BaseDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpTCPBaseDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpSocketBaseDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpIPv4BaseDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpUDPDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpTCPDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpAliasDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpUnixDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/transports/snmpCallbackDomain.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_secmod.c 	\
	$(SHARED_PATH)/net-snmp/src/snmpusm.c 	\
	$(SHARED_PATH)/net-snmp/src/snmp_version.c 	\
	$(SHARED_PATH)/net-snmp/src/container_null.c 	\
	$(SHARED_PATH)/net-snmp/src/container_list_ssll.c 	\
	$(SHARED_PATH)/net-snmp/src/container_iterator.c 	\
	$(SHARED_PATH)/net-snmp/src/ucd_compat.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_sha1.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_md5.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_set_key.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_des_enc.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_cbc_enc.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_aes_cfb.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_aes_core.c 	\
	$(SHARED_PATH)/net-snmp/src/openssl/openssl_cfb128.c	\
	$(SHARED_PATH)/net-snmp/src/dir_utils.c 	\
	$(SHARED_PATH)/net-snmp/src/file_utils.c 	\
	$(SHARED_PATH)/net-snmp/src/container.c 	\
	$(SHARED_PATH)/net-snmp/src/container_binary_array.c
	

include $(BUILD_SHARED_LIBRARY)
