#ifndef __GB28181_DEVICE_API_H__
#define __GB28181_DEVICE_API_H__

#ifdef ANDROID
#include <android/log.h>
#define  LOG_TAG  "GB28181Device"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#endif

#ifdef _WIN32
#include <winsock2.h>
#define GB28181DEVICE_API  __declspec(dllexport)
#define GB28181DEVICE_APICALL  __stdcall
#define WIN32_LEAN_AND_MEAN
#else
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#define GB28181DEVICE_API __attribute__ ((visibility("default")))
#define GB28181DEVICE_APICALL 
#define CALLBACK
#endif

typedef enum __GB28181_DEVICE_EVENT_TYPE_ENUM_T
{
	GB28181_DEVICE_EVENT_CONNECTING = 1,		//������
	GB28181_DEVICE_EVENT_REGISTER_ING,			//ע����
	GB28181_DEVICE_EVENT_REGISTER_OK,			//ע��ɹ�
	GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL,	//�û���֤ʧ��
	GB28181_DEVICE_EVENT_START_AUDIO_VIDEO,		//������������Ƶ
	GB28181_DEVICE_EVENT_STOP_AUDIO_VIDEO,		//����ֹͣ��������Ƶ
	GB28181_DEVICE_EVENT_TALK_AUDIO_DATA,		//�Զ˶Խ�����

	GB28181_DEVICE_EVENT_DISCONNECT				//�Ѷ���
}GB28181_DEVICE_EVENT_TYPE_ENUM_T;

#define MAX_CH_NUMS     8


typedef struct __GB28181_CHANNEL_INFO_T
{
	char	id[32];
	char	name[64];
	char	manufacturer[32];
	char	model[32];
	char	parentId[32];
	char	owner[32];
	char	civilCode[32];
	char	address[64];
	double	longitude;
	double  latitude;

}GB28181_CHANNEL_INFO_T;

typedef struct __GB28181_DEVICE_INFO_T
{
	int		version;			// 0(2016) or 1(2022)
	char	server_ip[128];     // server ip
	int		server_port;        // server port
	char    server_id[32];      // server id
	char	server_domain[64];  // server domain
	char	device_id[32];      // device id
	int		localSipPort;		//Local sip port
	int     channel_nums;       // channel numbers
	GB28181_CHANNEL_INFO_T channel[MAX_CH_NUMS];
	char	password[32];	    // password
	int		protocol;			// 0 - udp; 1 - tcp
	int		media_protocol;		// 0 - udp; 1 - tcp
	int		reg_expires;        // sip reg user expires, unit is second
	int		heartbeat_interval; // gb28181 heartbeat interval, unit is second
	int		heartbeat_count;    // gb28181 heartbeat count
	int     log_enable;         // log enable flag
	int     log_level;          // log level(0:TRACE,1:DEBUG,2:INFO,3:WARNING,4:ERROR,5:FATAL)
}GB28181_DEVICE_INFO_T;

typedef int (CALLBACK* GB28181DeviceCALLBACK)(void *userPtr, int channelId, int eventType, char *eventParams, int paramLength);


//����GB28181�豸����Դ
/*
pDeviceInfo:		�豸����Ϣ, ΪNULL��ӵ�ǰĿ¼�ж�ȡconfig.xml
callbackPtr:		�ص�����
userPtr:			�û��Զ���ָ��,�����ڻص������лص���������
*/
int GB28181DEVICE_API	libGB28181Device_Create(GB28181_DEVICE_INFO_T *pDeviceInfo, GB28181DeviceCALLBACK callbackPtr, void *userPtr);

int GB28181DEVICE_API	libGB28181Device_Create2(char* serverIP, int serverPort, char* serverId, char* serverDomain,
													char* deviceId, int localSipPort, int channelNum,
													GB28181_CHANNEL_INFO_T*pChannel,
													char* password, int protocol, int mediaProtocol,
													int regExpires, int heartbeatInterval, int heartbeatCount,
													GB28181DeviceCALLBACK callbackPtr, void* userPtr);

//ָ����Ƶ��ʽ
/*
codec:
		#define EASY_SDK_VIDEO_CODEC_H264	0x1C		//H264
		#define EASY_SDK_VIDEO_CODEC_H265	0xAE		//H265
width:	��ѡ
height:	��ѡ
framerate: ��ѡ
*/
int GB28181DEVICE_API	libGB28181Device_SetVideoFormat(int channelId, unsigned int codec, int width, int height, int framerate);

//ָ����Ƶ��ʽ
/*
audioDstCodec:
		#define EASY_SDK_AUDIO_CODEC_AAC	0x15002		// AAC
		#define EASY_SDK_AUDIO_CODEC_G711U	0x10006		// G711 ulaw
		#define EASY_SDK_AUDIO_CODEC_G711A	0x10007		// G711 alaw
samplerate:		������
channels:		������
bitPerSample:	��������
*/
int GB28181DEVICE_API	libGB28181Device_SetAudioFormat(int channelId, unsigned int audioDstCodec, int samplerate, int channels, int bitPerSamples);

//����ͨ��ʵʱ��γ����Ϣ
int GB28181DEVICE_API	libGB28181Device_SetLotLat(int channelId, double longitude, double latitude);

//������Ƶ����
/*
framedata:	��Ƶ֡����
framesize:	��Ƶ֡��С
keyframe:	�ؼ�֡��1, ������0
*/
int GB28181DEVICE_API	libGB28181Device_AddVideoData(int channelId, char* framedata, int framesize, int keyframe);

//������Ƶ����
/*
audioSrcCodec:	��ǰ��ƵԴ��ʽ
		#define EASY_SDK_AUDIO_CODEC_G711U	0x10006		// G711 ulaw
		#define EASY_SDK_AUDIO_CODEC_G711A	0x10007		// G711 alaw
		#define EASY_SDK_AUDIO_CODEC_PCM	0x00007		// PCM

		���audioSrcCodec��libGB28181Device_SetAudioFormat��ָ����audioDstCodec��һ��ʱ, ����Զ�����ת��, 
		�������ڴ�PCMתΪG711,���˴���audioSrcCodecΪPCM, ��audioDstCodecΪG711
framedata:	��Ƶ֡����
framesize:  ��Ƶ֡��С
nbsamples:	ͬ��Ƶ֡��С
*/
int GB28181DEVICE_API	libGB28181Device_AddAudioData(int channelId, unsigned int audioSrcCodec, char* framedata, int framesize, int nbsamples);


//�ͷ���Դ
int GB28181DEVICE_API	libGB28181Device_Release();


#ifdef ANDROID
#include <jni.h>
extern JavaVM *g_vm;
#endif


#endif
