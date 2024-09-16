/*
	Copyright (c) 2013-2015 EasyDarwin.ORG.  All rights reserved.
	Github: https://github.com/EasyDarwin
	WEChat: EasyDarwin
	Website: http://www.easydarwin.org
*/
#ifndef _EASY_STREAM_CLIENT_API_H
#define _EASY_STREAM_CLIENT_API_H

#include "EasyTypes.h"

/* 推送事件类型定义 */
typedef enum __EASY_STREAM_CLIENT_STATE_T
{
	EASY_STREAM_CLIENT_STATE_CONNECTING = 1,		 /* 连接中 */
	EASY_STREAM_CLIENT_STATE_CONNECTED,              /* 连接成功 */
	EASY_STREAM_CLIENT_STATE_CONNECT_FAILED,         /* 连接失败 */
	EASY_STREAM_CLIENT_STATE_CONNECT_ABORT,          /* 连接中断 */
	EASY_STREAM_CLIENT_STATE_PUSHING,                /* 推流中 */
	EASY_STREAM_CLIENT_STATE_DISCONNECTED,           /* 断开连接 */
	EASY_STREAM_CLIENT_STATE_EXIT,					 /* 退出连接 */
	EASY_STREAM_CLIENT_STATE_ERROR
} EASY_STREAM_CLIENT_STATE_T;

/*
_channelPtr:	通道对应对象,暂时不用
_frameType:		EASY_SDK_VIDEO_FRAME_FLAG/EASY_SDK_AUDIO_FRAME_FLAG/EASY_SDK_EVENT_FRAME_FLAG/...
_pBuf:			回调的数据部分，具体用法看Demo
_frameInfo:		帧结构数据
*/
typedef int (Easy_APICALL *EasyStreamClientCallBack)(void *_channelPtr, int _frameType, void *pBuf, EASY_FRAME_INFO* _frameInfo);
typedef int (Easy_APICALL *EasyDownloadCallBack)(void *userptr, const char* path);

#ifdef __cplusplus
extern "C" 
{
#endif
	/* 创建EasyStreamClient句柄  返回0表示成功，返回非0表示失败 ;  loglevel : 0 - quiet  1 - debug*/
	Easy_API int Easy_APICALL EasyStreamClient_Init(Easy_Handle *handle, int loglevel);

	/* 释放EasyStreamClient 参数为EasyStreamClient句柄 */
	Easy_API int Easy_APICALL EasyStreamClient_Deinit(Easy_Handle handle);

	/* 设置背景音 flag: 1 开启 0:关闭 url:背景音路径包含文件名 ret: 0：成功  < 0：配置失败 1:不支持背景音功能*/
	Easy_API int Easy_APICALL EasyStreamClient_SetBackAudio(Easy_Handle handle, int flag, char* url);

	/* 设置数据回调 */
	Easy_API int Easy_APICALL EasyStreamClient_SetCallback(Easy_Handle handle, EasyStreamClientCallBack callback);

	/* 设置叠加图片 */
	Easy_API int Easy_APICALL EasyStreamClient_SetOverlayImage(Easy_Handle handle, int left, int top, int scaleWidth, int scaleHeight, const char* imageFilePath);

	/* 打开网络流 */
	Easy_API int Easy_APICALL EasyStreamClient_OpenStream(Easy_Handle handle, char *url, EASY_RTP_CONNECT_TYPE connType, void *userPtr, int reconn, int timeout, int useExtraData);

	/* 获取输入流的context */
	Easy_API int Easy_APICALL EasyStreamClient_GetStreamContext(Easy_Handle handle, void** avFormatContext, void** avCodecContext);

	/* 获取快照 */
	Easy_API int Easy_APICALL EasyStreamClient_GetSnap(Easy_Handle handle);


	Easy_API int Easy_APICALL EasyStreamClient_ConvertFrame2Image(const unsigned int videoCodec, const unsigned char* keyFrameData, int keyFrameDataSize, unsigned char** outImage, int* outImageSize);
	Easy_API int Easy_APICALL EasyStreamClient_ReleaseImageData(unsigned char** imageData);



	/* 设置音频是否启用 */
	Easy_API int Easy_APICALL EasyStreamClient_SetAudioEnable(Easy_Handle handle, int enable);

	Easy_API int Easy_APICALL EasyStreamClient_SetAudioOutFormat(Easy_Handle handle, unsigned int audioFormat, int samplerate, int channels);

	/* 获取音频是否启用 */
	Easy_API int Easy_APICALL EasyStreamClient_GetAudioEnable(Easy_Handle handle);

	/*录像下载*/
	Easy_API int Easy_APICALL EasyStreamClient_SetDownloadCallback(EasyDownloadCallBack callback);
	Easy_API int Easy_APICALL EasyStreamClient_DownloadOneRecord(const char* rootPath, const char* streamName, const char* startTime, void* userPtr);
	Easy_API int Easy_APICALL EasyStreamClient_DownloadPeriod(const char* rootPath, const char* streamName, const char* startTime, const char* endTime, void* userPtr);
#ifdef __cplusplus
};
#endif

#endif
