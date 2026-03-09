
// EasyGBD_DemoDlg.h: 头文件
//

#pragma once
#include "EasyGBD_VideoDlg.h"
//#include "osthread.h"
#include "osmutex.h"
extern "C"
{
#include "osthread.h"
}
#include "XmlConfig.h"
#include <vector>
//#include "../AudioTranscode/AudioTranscodeAPI.h"
#include "EasyGB28181DeviceAPI.h"
#include "EasyStreamClientAPI.h"
#pragma comment(lib, "libEasyStreamClient.lib")
#pragma comment(lib, "libEasyGBD.lib")

#include "AudioCapturer/libAudioCapturerAPI.h"
#pragma comment(lib, "AudioCapturer/AudioCapturer.lib")
#include "FFTranscode/TransCodeAPI.h"
#pragma comment(lib, "FFTranscode/FFTransCode.lib")
#include "D3DRender/D3DRenderAPI.h"
#pragma comment(lib, "D3DRender/D3DRender.lib")

#include <string>
using namespace std;
typedef vector<string>	LOG_VECTOR;

#define WM_UPDATE_LOG		(WM_USER+1001)

typedef struct __GB28181_CHANNEL_T
{
	int         id;
	char        sourceURL[260];
	Easy_Handle  streamClientHandle;
	//ATC_HANDLE  atcHandle;

	EASY_MEDIA_INFO_T	mediaInfo;
	int			videoCodec;
	int         srcAudioCodec;
	int         sendStatus;
	int			videoFrameNum;

	int         audioOutputFormat;

	double		longitude;
	double		latitude;

	FILE* fDat;

	void* userptr;
}GB28181_CHANNEL_T;

typedef struct __GB28181_DEVICE_T
{
	bool isStart;
	GB28181_CHANNEL_T pChannel;
}GB28181_DEVICE_T;

#define MAX_GB28181_CHANNEL_NUM		1

//typedef struct __GB28181_DEVICE_LIST_T
//{
//	GB28181_DEVICE_T* pGB28181Device;
//	int				nDeviceNum;
//
//	bool			shareSource;
//}GB28181_DEVICE_LIST_T;

typedef struct __FRAME_INFO_T
{
	int		rawVideo;
	EASY_FRAME_INFO	info;
	char* pbuf;

}FRAME_INFO_T;
typedef std::vector< FRAME_INFO_T>	FRAME_INFO_VECTOR;

typedef struct __VIDEO_RENDER_T
{
	TRANSCODE_HANDLE	transcodeHandle;
	D3D_HANDLE		d3dHandle;
	D3D_SUPPORT_FORMAT	d3dRenderFormat;
	HWND		renderHwnd;
	RECT		renderRect;

	int			renderFrameNum;			// 已渲染帧数

	time_t		startTime;
}VIDEO_RENDER_T;

// CEasyGBDDemoDlg 对话框
class CEasyGBDDemoDlg : public CDialogEx
{
// 构造
public:
	CEasyGBDDemoDlg(CWnd* pParent = nullptr);	// 标准构造函数

	CComboBox* pComboxProtocolType;	//IDC_COMBO_PROTOCOL_TYPE
	CEdit* pEdtServerSipID;			//IDC_EDIT_SERVER_SIPID
	CEdit* pEdtServerSipDomain;		//IDC_EDIT_SERVER_SIP_DOMAIN
	CEdit* pEdtServerIP;			//IDC_EDIT_SERVER_IP
	CEdit* pEdtServerPort;			//IDC_EDIT_SERVER_PORT
	CEdit* pEdtRegExpire;			//IDC_EDIT_REG_EXPIRE
	CEdit* pEdtHeartbeatCount;		//IDC_EDIT_HEARTBEAT_COUNT
	CEdit* pEdtHeartbeatInterval;	//IDC_EDIT_HEARTBEAT_INTERVAL
	CComboBox* pComboxProtocol;		//IDC_COMBO_PROTOCOL
	CEdit* pEdtPassword;			//IDC_EDIT_PASSWORD
	CEdit* pEdtLocalSipID;			//IDC_EDIT_LOCAL_SIPID
	CEdit* pEdtLocalPort;			//IDC_EDIT_LOCAL_PORT
	CEdit* pEdtDeviceName;			//IDC_EDIT_DEVICE_NAME
	CEdit* pEdtSourceURL;			//IDC_EDIT_SOURCE_URL

	CEdit* pEdtChannel;				//IDC_EDIT_CHANNLE

	CButton* pBtnStartup;			//IDC_BUTTON_STARTUP
	CMFCButton* pMfcBtnStartup;		//IDC_MFCBUTTON_STARTUP
	CFont m_MfcBtnStartupFontLarge;
	CButton* pBtnShutdown;			//IDC_BUTTON_SHUTDOWN
	CRichEditCtrl* pRichEditLog;	//IDC_RICHEDIT2_LOG

	CMFCButton* pMfcBtnPreview;		//IDC_MFCBUTTON_BROWSE

	CComboBox* pComboxSourceType;	//IDC_COMBO_SOURCE_TYPE
	CComboBox* pComboxCameraList;	//IDC_COMBO_CAMERA_LIST
	CComboBox* pComboxAudioList;	//IDC_COMBO_AUDIO_LIST

	EasyGBD_VideoDlg* pGBDVideoDlg; //ID_GBD_DIALOG_VIDEO
	OSTHREAD_OBJ_T* pRenderThread;

	int		Startup(const char *serverSIPId, const char* serverSIPDomain, const char *serverIP, const int serverPort,
		const int reg_expires, const int heartbeatCount, const int heartbeatInterval,
		const int protocol, const char *password, const char *localSIPId, const int localPort, 
		const char *deviceName, const char *sourceURL, XML_CHANNEL_T *channels);
	void	Shutdown();

	GB28181_DEVICE_T	mGB28181Device;
	//GB28181_DEVICE_LIST_T	mGB28181DeviceList;

	int OutputLog(char* szFormat, ...);
	void OutputLog2UI();
	void	LockLogMutex() { LockMutex(&mLogMutex); }
	void	UnlockLogMutex() { UnlockMutex(&mLogMutex); }
	LOG_VECTOR		mLogVector;
	OSMutex			mLogMutex;

	int					sourceType;			// 0x00(USB Camera)		0x01(pull stream)
	OSMutex				mutexLocalFrame;
	FRAME_INFO_VECTOR	localFrameInfoVector; // 本地渲染
	bool			isPreview;		// 预览
	Easy_Handle  streamClientHandle;
	VIDEO_RENDER_T	localVideoRender;
	bool			findLocalKeyframe;



// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_EASYGBD_DEMO_DIALOG };
#endif

	void LoadLocalDevice();
	bool OpenSource();
	bool CloseSource();

	void ProcessLocalVideoToQueue(void* pBuf, EASY_FRAME_INFO* _frameInfo, int rawVideo);
	int ProcessLocalVideoFromQueue(int clear);
	void RenderVideo(VIDEO_RENDER_T* pRender, void* pBuf, EASY_FRAME_INFO* _frameInfo, int rawVideo);

	bool isNumber(const char* str);
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持

// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()
	afx_msg LRESULT OnUpdateLog(WPARAM, LPARAM);
public:
	afx_msg void OnBnClickedButtonStart();
	afx_msg void OnDestroy();
	afx_msg void OnBnClickedButtonShutdown();
	afx_msg void OnBnClickedButtonBrowse();
	afx_msg void OnCbnSelchangeComboSourceType();
	afx_msg void OnBnClickedButtonSetLongLat();
	afx_msg void OnBnClickedMfcbuttonBrowse();
	afx_msg void OnBnClickedMfcbuttonStartup();
};
