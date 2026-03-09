#ifndef __TRANS_CODE_API_H__
#define __TRANS_CODE_API_H__



#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
//#include <windows.h>
#include <winsock2.h>

#define TRANSCODE_API  __declspec(dllexport)
#define TRANSCODE_APICALL  __stdcall
#define WIN32_LEAN_AND_MEAN
#else
#define TRANSCODE_API  __attribute__ ((visibility("default")))
#define TRANSCODE_APICALL 
#define CALLBACK
#endif



#define TRANSCODE_VIDEO_CODEC_ID_H264		27
#define TRANSCODE_VIDEO_CODEC_ID_H265		173
#define TRANSCODE_VIDEO_CODEC_ID_MPEG4		12
#define TRANSCODE_VIDEO_CODEC_ID_MJPEG		7


typedef struct __TRANSCODE_HW_CODEC_T
{
	int			id;
	char		name[16];
}TRANSCODE_HW_CODEC_T;



//typedef int (CALLBACK* NVSourceCallBack)(int channelId, void* userPtr, int mediaType, char* pBuf, NVS_FRAME_INFO* frameInfo);


typedef void (CALLBACK* TRANSCODE_DECODE_CALLBACK)(void* userPtr, int width, int height, unsigned char* decode_data, int datasize, int keyframe);
typedef void (CALLBACK* TRANSCODE_ENCODE_CALLBACK)(void *userPtr, int keyFrame, int width, int height, unsigned char* transcode_framedata, int transcode_framesize, unsigned int pts);
#define TRANSCODE_HANDLE void*

#ifdef __cplusplus
extern "C"
{
#endif

	TRANSCODE_API int TRANSCODE_APICALL TransCode_Create(TRANSCODE_HANDLE* handle);

	// 根据decoderId, 获取相应的硬解码能力
	TRANSCODE_API int TRANSCODE_APICALL TransCode_GetSupportHardwareCodecs(TRANSCODE_HANDLE handle, int codecType/*0(Decoder)  1(Encoder)*/, int codecId, TRANSCODE_HW_CODEC_T** ppCodecs, int* num);

	// ===============================================
	// ===============================================
	// ===============================================
	// ===============================================
	// 调用方式: 1. 完成解码和编码   2. 仅解码  3. 仅编码


	// 初始化
	// decoderId:	视频解码器Id, 如: TRANSCODE_VIDEO_CODEC_ID_H264 TRANSCODE_VIDEO_CODEC_ID_H265
	//				如不解码仅编码,则填0x7FFFFFFF
	// hwDecoderId: 大于0则表示使用硬解, 该值从TransCode_GetSupportHardwareDecoder中获得, 如果初始化硬解失败,则自动切换为软解
	// width:  源分辨率-宽
	// height: 源分辨率-高
	// pixelFormat: 0(YUV420P)  3(BGR24)   23(NV12)   具体可参考ffmpeg4.4.1(AVPixelFormat定义)
	// encoderID: 如为0则表示不编码, 大于0时: 如hwDecoderId大于0(使用硬解码),则使用相应的编码模块, 如果都不符,则使用软编码
	// encFps:  编码帧率
	// encGop:  编码GOP
	// encBitrate: 编码码率

	TRANSCODE_API int TRANSCODE_APICALL TransCode_Init(TRANSCODE_HANDLE handle, int decoderId, int hwDecoderId, int width, int height, 
														int pixelFormat/* 0(YUV420P)  3(BGR24)   23(NV12) */ ,
														int encoderId/*为0则表示不编码*/, int hwEncoderId, int encFps, int encGop, int encBitrate);

	// 重启编码器
	TRANSCODE_API int TRANSCODE_APICALL TransCode_RebootEncoder(TRANSCODE_HANDLE handle, 
														int encoderId/*为0则表示不编码*/, int hwEncoderId, int encFps, int encGop, int encBitrate);



	// TransCode_TransCodeVideo
	// outFrameData: 
	//				如不为NULL, 则表示同步执行, 解码后的数据直接返回
	//				如为NULL, 则为异步输出, 会创建单独的线程进行回调(TRANSCODE_DECODE_CALLBACK)输出, 调用层可在该回调中进行分析等操作
	//				



	TRANSCODE_API int TRANSCODE_APICALL TransCode_TransCodeVideo(TRANSCODE_HANDLE handle, const char* framedata, const int framesize, 
																		int *dstWidth, int* dstHeight,
																		char** outFrameData, int* outFrameSize, int* keyFrame, 
																		TRANSCODE_DECODE_CALLBACK decodeCallback, void *decodeUserPtr,
																		TRANSCODE_ENCODE_CALLBACK encodeCallback, void *encodeUserPtr,
																		unsigned int pts, bool autoParseResolution);

	// ============================================
	TRANSCODE_API int TRANSCODE_APICALL TransCode_GetSwsSize(int pixelFormat, int width, int height);
	TRANSCODE_API int TRANSCODE_APICALL TransCode_CreateSws(TRANSCODE_HANDLE *handle, int srcWidth, int srcHeight, int srcPixelFormat, int dstWidth, int dstHeight, int dstPixelFormat);
	TRANSCODE_API int TRANSCODE_APICALL TransCode_SwsScale(TRANSCODE_HANDLE handle, const unsigned char* inData, unsigned char** outData, int* outSize);
	TRANSCODE_API int TRANSCODE_APICALL TransCode_ReleaseSws(TRANSCODE_HANDLE* handle);
	// ============================================

	TRANSCODE_API int TRANSCODE_APICALL TransCode_Release(TRANSCODE_HANDLE* handle);


#ifdef __cplusplus
};
#endif


#endif
