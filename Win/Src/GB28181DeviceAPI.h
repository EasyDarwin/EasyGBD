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
	GB28181_DEVICE_EVENT_CONNECTING = 1,		//连接中
	GB28181_DEVICE_EVENT_REGISTER_ING,			//注册中
	GB28181_DEVICE_EVENT_REGISTER_OK,			//注册成功
	GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL,	//用户验证失败
	GB28181_DEVICE_EVENT_START_AUDIO_VIDEO,		//请求推送音视频
	GB28181_DEVICE_EVENT_STOP_AUDIO_VIDEO,		//请求停止推送音视频
	GB28181_DEVICE_EVENT_TALK_AUDIO_DATA,		//对端对讲数据
	GB28181_DEVICE_EVENT_DISCONNECT,			//已断线

	GB28181_DEVICE_EVENT_SUBSCRIBE_ALARM,		// 报警订阅
	GB28181_DEVICE_EVENT_SUBSCRIBE_CATALOG,		// 目录订阅
	GB28181_DEVICE_EVENT_SUBSCRIBE_MOBILEPOSITION,		// 位置订阅


	GB28181_DEVICE_EVENT_PTZ_MOVE_LEFT,			// 云台-左
	GB28181_DEVICE_EVENT_PTZ_MOVE_UP,			// 云台-上
	GB28181_DEVICE_EVENT_PTZ_MOVE_RIGHT,		// 云台-右
	GB28181_DEVICE_EVENT_PTZ_MOVE_DOWN,			// 云台-下
	GB28181_DEVICE_EVENT_PTZ_MOVE_STOP,			// 云台-停
	GB28181_DEVICE_EVENT_PTZ_ZOOM_IN,			// 云台-拉近
	GB28181_DEVICE_EVENT_PTZ_ZOOM_OUT,			// 云台-拉远


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
	char	device_name[64];	// device name  2024.12.04
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


//创建GB28181设备端资源
/*
pDeviceInfo:		设备端信息, 为NULL则从当前目录中读取config.xml
callbackPtr:		回调函数
userPtr:			用户自定义指针,用于在回调函数中回调给调用者
*/
int GB28181DEVICE_API	libGB28181Device_Create(GB28181_DEVICE_INFO_T *pDeviceInfo, GB28181DeviceCALLBACK callbackPtr, void *userPtr);

int GB28181DEVICE_API	libGB28181Device_Create2(char* serverIP, int serverPort, char* serverId, char* serverDomain,
													char* deviceId, char* deviceName, int localSipPort, int channelNum,
													GB28181_CHANNEL_INFO_T*pChannel,
													char* password, int protocol, int mediaProtocol,
													int regExpires, int heartbeatInterval, int heartbeatCount,
													GB28181DeviceCALLBACK callbackPtr, void* userPtr);

//指定视频格式
/*
codec:
		#define EASY_SDK_VIDEO_CODEC_H264	0x1C		//H264
		#define EASY_SDK_VIDEO_CODEC_H265	0xAE		//H265
width:	可选
height:	可选
framerate: 可选
*/
int GB28181DEVICE_API	libGB28181Device_SetVideoFormat(int channelId, unsigned int codec, int width, int height, int framerate);

//指定音频格式
/*
audioDstCodec:
		#define EASY_SDK_AUDIO_CODEC_AAC	0x15002		// AAC
		#define EASY_SDK_AUDIO_CODEC_G711U	0x10006		// G711 ulaw
		#define EASY_SDK_AUDIO_CODEC_G711A	0x10007		// G711 alaw
samplerate:		采样率
channels:		声道数
bitPerSample:	采样精度
*/
int GB28181DEVICE_API	libGB28181Device_SetAudioFormat(int channelId, unsigned int audioDstCodec, int samplerate, int channels, int bitPerSamples);

//设置通道实时经纬度信息
int GB28181DEVICE_API	libGB28181Device_SetLotLat(int channelId, double longitude, double latitude);

//推送视频数据
/*
framedata:	视频帧数据
framesize:	视频帧大小
keyframe:	关键帧填1, 否则填0
*/
int GB28181DEVICE_API	libGB28181Device_AddVideoData(int channelId, char* framedata, int framesize, int keyframe);

//推送音频数据
/*
audioSrcCodec:	当前音频源格式
		#define EASY_SDK_AUDIO_CODEC_G711U	0x10006		// G711 ulaw
		#define EASY_SDK_AUDIO_CODEC_G711A	0x10007		// G711 alaw
		#define EASY_SDK_AUDIO_CODEC_PCM	0x00007		// PCM

		如果audioSrcCodec和libGB28181Device_SetAudioFormat中指定的audioDstCodec不一致时, 则会自动进行转换, 
		但仅限于从PCM转为G711,即此处的audioSrcCodec为PCM, 而audioDstCodec为G711
framedata:	音频帧数据
framesize:  音频帧大小
nbsamples:	同音频帧大小
*/
int GB28181DEVICE_API	libGB28181Device_AddAudioData(int channelId, unsigned int audioSrcCodec, char* framedata, int framesize, int nbsamples);


//释放资源
int GB28181DEVICE_API	libGB28181Device_Release();


#ifdef ANDROID
#include <jni.h>
extern JavaVM *g_vm;
#endif


#endif
