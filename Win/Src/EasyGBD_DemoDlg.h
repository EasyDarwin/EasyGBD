
// EasyGBD_DemoDlg.h: 头文件
//

#pragma once

//#include "osthread.h"
#include "osmutex.h"
#include <vector>
//#include "../AudioTranscode/AudioTranscodeAPI.h"
#include "EasyGB28181DeviceAPI.h"
#include "EasyStreamClientAPI.h"
#pragma comment(lib, "libEasyStreamClient.lib")
#pragma comment(lib, "libEasyGBD.lib")

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
	GB28181_CHANNEL_T* pChannel;
}GB28181_DEVICE_T;

#define MAX_GB28181_CHANNEL_NUM		1

typedef struct __GB28181_DEVICE_LIST_T
{
	GB28181_DEVICE_T* pGB28181Device;
	int				nDeviceNum;

	bool			shareSource;
}GB28181_DEVICE_LIST_T;

// CEasyGBDDemoDlg 对话框
class CEasyGBDDemoDlg : public CDialogEx
{
// 构造
public:
	CEasyGBDDemoDlg(CWnd* pParent = nullptr);	// 标准构造函数

	CComboBox* pComboxProtocolType;	//IDC_COMBO_PROTOCOL_TYPE
	CEdit* pEdtServerSipID;			//IDC_EDIT_SERVER_SIPID
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

	CButton* pBtnStartup;			//IDC_BUTTON_STARTUP
	CButton* pBtnShutdown;			//IDC_BUTTON_SHUTDOWN
	CRichEditCtrl* pRichEditLog;	//IDC_RICHEDIT2_LOG



	int		Startup(const char *serverSIPId, const char *serverIP, const int serverPort,
		const int reg_expires, const int heartbeatCount, const int heartbeatInterval,
		const int protocol, const char *password, const int deviceNum, const char *localSIPId, const int localPort, 
		const char *deviceName, const char *sourceURL);
	void	Shutdown();

	GB28181_DEVICE_T	mGB28181Device;
	GB28181_DEVICE_LIST_T	mGB28181DeviceList;

	int OutputLog(char* szFormat, ...);
	void OutputLog2UI();
	void	LockLogMutex() { LockMutex(&mLogMutex); }
	void	UnlockLogMutex() { UnlockMutex(&mLogMutex); }
	LOG_VECTOR		mLogVector;
	OSMutex			mLogMutex;



// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_EASYGBD_DEMO_DIALOG };
#endif

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
};
