#ifndef __LIB_AUDIO_CAPTURER_API_H__
#define __LIB_AUDIO_CAPTURER_API_H__

#ifdef _DEBUG
//#include <vld.h>
#endif

#include <winsock2.h>
#include <mmsystem.h>
#include <map>
#include <string>
using namespace std;

#ifdef _WIN32
#define LIB_AUDIO_CAPTURER_API  __declspec(dllexport)
#ifndef LIB_APICALL
#define LIB_APICALL  __stdcall
#endif
#define WIN32_LEAN_AND_MEAN
#else
#define LIB_AUDIO_CAPTURER_API
#define LIB_APICALL 
#endif


#define AUDIO_CODEC_ID_PCM		1
#define AUDIO_CODEC_ID_ALAW		2
#define AUDIO_CODEC_ID_MULAW	3


//声音设备信息
#define		MAX_MIXER_DEVICE_NUM		16
typedef struct __MIXER_DEVICE_INFO_T
{
	int		id;
	char	name[128];
	char	version[16];
}MIXER_DEVICE_INFO_T;

//音频采集设备信息
typedef struct __AUDIO_CAPTURE_DEVICE_INFO
{
	int			id;
	//LPGUID		lpGuid;
	//LPTSTR		lpstrDescription;
	//LPTSTR		lpstrModule;

	char		description[128];
	char		module[128];
}AUDIO_CAPTURE_DEVICE_INFO;

typedef map<int, AUDIO_CAPTURE_DEVICE_INFO>		AUDIO_CAPTURER_DEVICE_MAP;

//音频采集格式
//typedef struct __AUDIO_WAVE_FORMAT_INFO
//{
//	WORD        wFormatTag;         /* format type */
//	WORD        nChannels;          /* number of channels (i.e. mono, stereo...) */
//	DWORD       nSamplesPerSec;     /* sample rate */
//	DWORD       nAvgBytesPerSec;    /* for buffer estimation */
//	WORD        nBlockAlign;        /* block size of data */
//	WORD        wBitsPerSample;     /* number of bits per sample of mono data */
//	WORD        cbSize;             /* the count in bytes of the size of */
//									/* extra information (after cbSize) */
//}AUDIO_WAVE_FORMAT_INFO;
typedef map<int, WAVEFORMATEX>		AUDIO_WAVE_FORMAT_INFO_MAP;

typedef int (CALLBACK *AudioDataCallBack)(void *userptr, char *pbuf, const int bufsize);


#ifdef __cplusplus
extern "C"
{
#endif

	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_Init();

	// 获取音频设备列表
	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_GetAudioCaptureDeviceList(AUDIO_CAPTURER_DEVICE_MAP* pMap);

	// 打开音频采集设备
	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_OpenAudioCaptureDevice(int captureDeviceIndex);

	// 获取支持的格式
	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_GetSupportWaveFormatList(AUDIO_WAVE_FORMAT_INFO_MAP* pMap);

	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_StartAudioCapture(unsigned int codec, int packetSize, 
														int samplerate/*采样率*/, int bitsPerSample/*采样精度*/, int channels/*通道*/,
														AudioDataCallBack callback, void* userptr);


	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_StopAudioCapture();

	// 关闭音频采集设备
	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_CloseAudioCaptureDevice();

	int LIB_AUDIO_CAPTURER_API	LIB_APICALL libAudioCapturer_Deinit();

#ifdef __cplusplus
}
#endif



#endif
