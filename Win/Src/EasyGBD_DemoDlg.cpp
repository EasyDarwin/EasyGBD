﻿
// EasyGBD_DemoDlg.cpp: 实现文件
//

#include "pch.h"
#include "framework.h"
#include "EasyGBD_Demo.h"
#include "EasyGBD_DemoDlg.h"
#include "afxdialogex.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif




// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = IDD_ABOUTBOX };
#endif

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(IDD_ABOUTBOX)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CEasyGBDDemoDlg 对话框



CEasyGBDDemoDlg::CEasyGBDDemoDlg(CWnd* pParent /*=nullptr*/)
	: CDialogEx(IDD_EASYGBD_DEMO_DIALOG, pParent)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);

	InitMutex(&mLogMutex);
	memset(&mGB28181Device, 0x00, sizeof(GB28181_DEVICE_T));

	memset(&mGB28181DeviceList, 0x00, sizeof(GB28181_DEVICE_LIST_T));
}

void CEasyGBDDemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CEasyGBDDemoDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_STARTUP, &CEasyGBDDemoDlg::OnBnClickedButtonStart)
	ON_WM_DESTROY()
	ON_BN_CLICKED(IDC_BUTTON_SHUTDOWN, &CEasyGBDDemoDlg::OnBnClickedButtonShutdown)
	ON_BN_CLICKED(IDC_BUTTON_BROWSE, &CEasyGBDDemoDlg::OnBnClickedButtonBrowse)
	ON_MESSAGE(WM_UPDATE_LOG, OnUpdateLog)
END_MESSAGE_MAP()


// CEasyGBDDemoDlg 消息处理程序

BOOL CEasyGBDDemoDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != nullptr)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。  当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码

	SetWindowText(TEXT("EasyGBD_Demo"));

	pEdtServerSipID = (CEdit*)GetDlgItem(IDC_EDIT_SERVER_SIPID);
	pEdtServerIP = (CEdit*)GetDlgItem(IDC_EDIT_SERVER_IP);
	pEdtServerPort = (CEdit*)GetDlgItem(IDC_EDIT_SERVER_PORT);
	pEdtRegExpire = (CEdit*)GetDlgItem(IDC_EDIT_REG_EXPIRE);
	pEdtHeartbeatCount = (CEdit*)GetDlgItem(IDC_EDIT_HEARTBEAT_COUNT);
	pEdtHeartbeatInterval = (CEdit*)GetDlgItem(IDC_EDIT_HEARTBEAT_INTERVAL);
	pComboxProtocol = (CComboBox*)GetDlgItem(IDC_COMBO_PROTOCOL);
	pEdtPassword = (CEdit*)GetDlgItem(IDC_EDIT_PASSWORD);
	pEdtLocalSipID = (CEdit*)GetDlgItem(IDC_EDIT_LOCAL_SIPID);
	pEdtLocalPort = (CEdit*)GetDlgItem(IDC_EDIT_LOCAL_PORT);
	pEdtDeviceName = (CEdit*)GetDlgItem(IDC_EDIT_DEVICE_NAME);
	pEdtSourceURL = (CEdit*)GetDlgItem(IDC_EDIT_SOURCE_URL);

	pBtnStartup = (CButton*)GetDlgItem(IDC_BUTTON_STARTUP);
	pBtnShutdown = (CButton*)GetDlgItem(IDC_BUTTON_SHUTDOWN);
	pRichEditLog = (CRichEditCtrl*)GetDlgItem(IDC_RICHEDIT2_LOG);


	pEdtServerSipID->SetWindowTextW(TEXT("34020000002000000001"));
	pEdtServerIP->SetWindowTextW(TEXT("demo.easygbs.com"));
	//pEdtServerIP->SetWindowTextW(TEXT("192.168.0.77"));
	//
	wchar_t wszDeviceName[32] = { 0 };

	time_t currentTime = time(NULL);
	unsigned int randValue = currentTime & 0xFFFFF;

	wchar_t wszRandSipID[32] = { 0 };
	wsprintf(wszRandSipID, TEXT("34020000001110%06d"), randValue);
	wsprintf(wszDeviceName, TEXT("EasyGBD-%06d"), randValue);
	int sipIdLen = (int)wcslen(wszRandSipID);
	for (int i = sipIdLen; i > 20; i--)
	{
		wszRandSipID[i - 1] = TEXT('\0');
	}

	pEdtServerPort->SetWindowTextW(TEXT("15060"));
	pEdtRegExpire->SetWindowTextW(TEXT("3600"));
	pEdtHeartbeatCount->SetWindowTextW(TEXT("3"));
	pEdtHeartbeatInterval->SetWindowTextW(TEXT("60"));
	pComboxProtocol->AddString(TEXT("UDP"));
	pComboxProtocol->AddString(TEXT("TCP"));
	pComboxProtocol->SetCurSel(0);
	pEdtPassword->SetWindowTextW(TEXT("12345678"));
	pEdtLocalSipID->SetWindowTextW(wszRandSipID);// TEXT("34020000001110000009"));
	pEdtLocalPort->SetWindowTextW(TEXT("15090"));
	pEdtDeviceName->SetWindowTextW(wszDeviceName);
	pEdtSourceURL->SetWindowTextW(TEXT("G:/VideoSamples/Alizee_La_Isla_Bonita_Mixiran_Arash.avi"));


	wchar_t wszPath[128] = { 0 };
	GetModuleFileName(NULL, wszPath, sizeof(wszPath));
	int nSize = (int)wcslen(wszPath);

	int i = nSize;
	for (i = nSize; i > 0; i--)
	{
		if ((unsigned char)wszPath[i] == '\\')
		{
			//wszPath[i] = '\0';
			break;
		}
		wszPath[i] = '\0';
	}

	wcscat(wszPath, TEXT("EasyDarwin.mp4"));
	pEdtSourceURL->SetWindowTextW(wszPath);

	OnBnClickedButtonShutdown();

	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CEasyGBDDemoDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。  对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CEasyGBDDemoDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CEasyGBDDemoDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}



bool MByteToWChar(LPCSTR lpcszStr, LPWSTR lpwszStr, DWORD dwSize)
{
	// Get the required size of the buffer that receives the Unicode
	// string.
	DWORD dwMinSize;
	dwMinSize = MultiByteToWideChar(CP_ACP, 0, lpcszStr, -1, NULL, 0);

	if (dwSize < dwMinSize)
	{
		return false;
	}

	// Convert headers from ASCII to Unicode.
	MultiByteToWideChar(CP_ACP, 0, lpcszStr, -1, lpwszStr, dwMinSize);
	return true;
}
bool WCharToMByte(LPCWSTR lpcwszStr, LPSTR lpszStr, DWORD dwSize)
{
	DWORD dwMinSize;
	dwMinSize = WideCharToMultiByte(CP_UTF8, NULL, lpcwszStr, -1, NULL, 0, NULL, FALSE);
	if (dwSize < dwMinSize)
	{
		return false;
	}
	WideCharToMultiByte(CP_UTF8, NULL, lpcwszStr, -1, lpszStr, dwSize, NULL, FALSE);
	return true;
}

void CEasyGBDDemoDlg::OnBnClickedButtonStart()
{
	// TODO: 在此添加控件通知处理程序代码

	wchar_t wszServerSipId[32] = { 0 };
	char szServerSipId[32] = { 0 };
	pEdtServerSipID->GetWindowTextW(wszServerSipId, sizeof(wszServerSipId));
	WCharToMByte(wszServerSipId, szServerSipId, sizeof(szServerSipId) / sizeof(szServerSipId[0]));

	wchar_t wszServerIP[32] = { 0 };
	char szServerIP[32] = { 0 };
	pEdtServerIP->GetWindowTextW(wszServerIP, sizeof(wszServerIP));
	WCharToMByte(wszServerIP, szServerIP, sizeof(szServerIP) / sizeof(szServerIP[0]));

	wchar_t wszServerPort[16] = { 0 };
	char szServerPort[16] = { 0 };
	pEdtServerPort->GetWindowTextW(wszServerPort, sizeof(wszServerPort));
	WCharToMByte(wszServerPort, szServerPort, sizeof(szServerPort) / sizeof(szServerPort[0]));

	wchar_t wszRegExpire[16] = { 0 };
	char szRegExpire[16] = { 0 };
	pEdtRegExpire->GetWindowTextW(wszRegExpire, sizeof(wszRegExpire));
	WCharToMByte(wszRegExpire, szRegExpire, sizeof(szRegExpire) / sizeof(szRegExpire[0]));

	wchar_t wszHeartbeatCount[16] = { 0 };
	char szHeartbeatCount[16] = { 0 };
	pEdtHeartbeatCount->GetWindowTextW(wszHeartbeatCount, sizeof(wszHeartbeatCount));
	WCharToMByte(wszHeartbeatCount, szHeartbeatCount, sizeof(szHeartbeatCount) / sizeof(szHeartbeatCount[0]));

	wchar_t wszHeartbeatInterval[16] = { 0 };
	char szHeartbeatInterval[16] = { 0 };
	pEdtHeartbeatInterval->GetWindowTextW(wszHeartbeatInterval, sizeof(wszHeartbeatInterval));
	WCharToMByte(wszHeartbeatInterval, szHeartbeatInterval, sizeof(szHeartbeatInterval) / sizeof(szHeartbeatInterval[0]));

	int protocol = pComboxProtocol->GetCurSel();

	wchar_t wszPassword[32] = { 0 };
	char szPassword[32] = { 0 };
	pEdtPassword->GetWindowTextW(wszPassword, sizeof(wszPassword));
	WCharToMByte(wszPassword, szPassword, sizeof(szPassword) / sizeof(szPassword[0]));

	wchar_t wszLocalSipID[32] = { 0 };
	char szLocalSipID[32] = { 0 };
	pEdtLocalSipID->GetWindowTextW(wszLocalSipID, sizeof(wszLocalSipID));
	WCharToMByte(wszLocalSipID, szLocalSipID, sizeof(szLocalSipID) / sizeof(szLocalSipID[0]));

	wchar_t wszLocalPort[16] = { 0 };
	char szLocalPort[16] = { 0 };
	pEdtLocalPort->GetWindowTextW(wszLocalPort, sizeof(wszLocalPort));
	WCharToMByte(wszLocalPort, szLocalPort, sizeof(szLocalPort) / sizeof(szLocalPort[0]));

	wchar_t wszDeviceName[128] = { 0 };
	char szDeviceName[128] = { 0 };
	pEdtDeviceName->GetWindowTextW(wszDeviceName, sizeof(wszDeviceName));
	WCharToMByte(wszDeviceName, szDeviceName, sizeof(szDeviceName) / sizeof(szDeviceName[0]));

	wchar_t wszSourceURL[1024] = { 0 };
	char szSourceURL[1024] = { 0 };
	pEdtSourceURL->GetWindowTextW(wszSourceURL, sizeof(wszSourceURL));
	WCharToMByte(wszSourceURL, szSourceURL, sizeof(szSourceURL) / sizeof(szSourceURL[0]));

	Startup(szServerSipId, szServerIP, atoi(szServerPort), atoi(szRegExpire), atoi(szHeartbeatCount), atoi(szHeartbeatInterval),
		protocol, szPassword, 1, szLocalSipID, atoi(szLocalPort), szDeviceName, szSourceURL);


	pBtnStartup->EnableWindow(FALSE);
	pBtnShutdown->EnableWindow(TRUE);
}


void CEasyGBDDemoDlg::OnBnClickedButtonShutdown()
{
	Shutdown();

	pBtnStartup->EnableWindow(TRUE);
	pBtnShutdown->EnableWindow(FALSE);
}


int CEasyGBDDemoDlg::OutputLog(char* szFormat, ...)
{
	char	pbuf[1024] = { 0 };

	va_list args;
	va_start(args, szFormat);
	vsnprintf(pbuf, sizeof(pbuf) - 1, szFormat, args);
	va_end(args);



	LockLogMutex();
	mLogVector.push_back(pbuf);
	UnlockLogMutex();

	PostMessage(WM_UPDATE_LOG);

	return 0;
}

LRESULT CEasyGBDDemoDlg::OnUpdateLog(WPARAM, LPARAM)
{

	OutputLog2UI();
	return 0;
}

void CEasyGBDDemoDlg::OutputLog2UI()
{
	LockLogMutex();
	LOG_VECTOR::iterator it = mLogVector.begin();
	while (it != mLogVector.end())
	{
		wchar_t wszBuf[1024] = { 0 };
		MByteToWChar(it->data(), wszBuf, sizeof(wszBuf) / sizeof(wszBuf[0]));

		pRichEditLog->SetSel(-1, -1);
		pRichEditLog->ReplaceSel(wszBuf);

		mLogVector.erase(it);
		it = mLogVector.begin();
		if (it == mLogVector.end())		break;
	}
	UnlockLogMutex();
}



int CALLBACK __GB28181DeviceCALLBACK(void* userPtr, int channelId, int eventType, char* eventParams, int paramLength)
{
	GB28181_DEVICE_T* pGB28181Device = (GB28181_DEVICE_T*)userPtr;
	GB28181_CHANNEL_T* pChannel = &pGB28181Device->pChannel[channelId];

	if (NULL == pChannel)		return 0;

	CEasyGBDDemoDlg* pThis = (CEasyGBDDemoDlg*)pChannel->userptr;

	if (GB28181_DEVICE_EVENT_CONNECTING == eventType)
	{
		pThis->OutputLog("GB/T28181服务连接中...  %s\n", eventParams);

	}
	else if (GB28181_DEVICE_EVENT_REGISTER_ING == eventType)
	{
		pThis->OutputLog("GB/T28181 注册中....\n");
	}
	if (GB28181_DEVICE_EVENT_REGISTER_OK == eventType)
	{
		pThis->OutputLog("GB/T28181 注册成功.\n");
	}
	else if (GB28181_DEVICE_EVENT_REGISTER_AUTH_FAIL == eventType)
	{
		pThis->OutputLog("GB/T28181 鉴权失败.\n");
	}

	else if (GB28181_DEVICE_EVENT_START_AUDIO_VIDEO == eventType)
	{
		pThis->OutputLog("GB/T28181 请求视频.  ID: %s\n", eventParams);
		pChannel->sendStatus = 1;
	}
	else if (GB28181_DEVICE_EVENT_STOP_AUDIO_VIDEO == eventType)
	{
		pThis->OutputLog("GB/T28181 停止视频.  ID: %s\n", eventParams);
		pChannel->sendStatus = 0;
	}
	else if (GB28181_DEVICE_EVENT_TALK_AUDIO_DATA == eventType)
	{
		pThis->OutputLog("GB/T28181 音频对讲数据...\n");

		static FILE* f = fopen("1_talk.pcm", "wb");
		if (f)
		{
			fwrite(eventParams, 1, paramLength, f);
		}

	}
	else if (GB28181_DEVICE_EVENT_SUBSCRIBE_ALARM == eventType)
	{
		pThis->OutputLog("GB/T28181 订阅报警:%d.\n", paramLength);
	}
	else if (GB28181_DEVICE_EVENT_SUBSCRIBE_CATALOG == eventType)
	{
		pThis->OutputLog("GB/T28181 订阅目录:%d.\n", paramLength);
	}
	else if (GB28181_DEVICE_EVENT_SUBSCRIBE_MOBILEPOSITION == eventType)
	{
		pThis->OutputLog("GB/T28181 订阅位置:%d.\n", paramLength);
	}
	//

	return 0;
}

int Easy_APICALL __EasyStreamClientCallBack(void* _channelPtr, int _frameType, void* pBuf, EASY_FRAME_INFO* _frameInfo)
{
	GB28181_CHANNEL_T* pChannel = (GB28181_CHANNEL_T*)_channelPtr;
	CEasyGBDDemoDlg* pThis = (CEasyGBDDemoDlg*)pChannel->userptr;

	if (_frameType == EASY_SDK_VIDEO_FRAME_FLAG)
	{
		if (pChannel->videoCodec < 1 && (pChannel->mediaInfo.u32AudioCodec>0 || pChannel->videoFrameNum>25))
		{
			pThis->OutputLog("设置视频编码格式: %s\n", _frameInfo->codec == EASY_SDK_VIDEO_CODEC_H264 ? "H264" : "H265");

			libGB28181Device_SetVideoFormat(pChannel->id, _frameInfo->codec, 0, 0, 0);
			libGB28181Device_SetAudioFormat(pChannel->id, pChannel->mediaInfo.u32AudioCodec,
				pChannel->mediaInfo.u32AudioSamplerate,
				pChannel->mediaInfo.u32AudioChannel > 2 ? 2 : pChannel->mediaInfo.u32AudioChannel,
				pChannel->mediaInfo.u32AudioBitsPerSample);

			//libGB28181Device_SetAudioFormat(pChannel->id, EASY_SDK_AUDIO_CODEC_AAC,//EASY_SDK_AUDIO_CODEC_G711U,
			//	pChannel->mediaInfo.u32AudioSamplerate,			
			//	pChannel->mediaInfo.u32AudioChannel > 2 ? 2 : pChannel->mediaInfo.u32AudioChannel,
			//	pChannel->mediaInfo.u32AudioBitsPerSample);
			pChannel->videoCodec = _frameInfo->codec;
		}
		pChannel->videoFrameNum++;

		if (pChannel->sendStatus == 1)
		{
			if (_frameInfo->type == 0x01)
			{
				char* buf = (char*)pBuf;
				pThis->OutputLog("推送关键帧: %d  Bytes: %02X %02X %02X %02X %02X %02X %02X %02X\n", _frameInfo->length,
					(unsigned char)buf[0], (unsigned char)buf[1], (unsigned char)buf[2], (unsigned char)buf[3],
					(unsigned char)buf[4], (unsigned char)buf[5], (unsigned char)buf[6], (unsigned char)buf[7]);


				pChannel->longitude += 0.1;
				pChannel->latitude += 0.001;
				libGB28181Device_SetLotLat(pChannel->id, pChannel->longitude, pChannel->latitude);
			}

			libGB28181Device_AddVideoData(pChannel->id, (char*)pBuf, _frameInfo->length, _frameInfo->type);
		}

		//Sleep(33);
	}
	else if (_frameType == EASY_SDK_AUDIO_FRAME_FLAG)
	{
		pChannel->mediaInfo.u32AudioCodec = _frameInfo->codec;
		pChannel->mediaInfo.u32AudioSamplerate = _frameInfo->sample_rate;
		pChannel->mediaInfo.u32AudioChannel = _frameInfo->channels;
		pChannel->mediaInfo.u32AudioBitsPerSample = _frameInfo->bits_per_sample;

		if (pChannel->sendStatus == 1)
		{
			libGB28181Device_AddAudioData(pChannel->id, _frameInfo->codec, (char*)pBuf, _frameInfo->length, _frameInfo->length);
		}

		//if (pChannel->sendStatus == 1)
		//{
		//	if (NULL == pChannel->atcHandle)
		//	{
		//	    ATC_Init(&pChannel->atcHandle, pChannel->audioOutputFormat,//TRANSCODE_AUDIO_TYPE_AAC,
		//	        //pChannel->srcAudioCodec, frameinfo->sample_rate, 1, frameinfo->bitsPerSample);
		//	        pChannel->srcAudioCodec, _frameInfo->sample_rate, _frameInfo->channels, _frameInfo->bits_per_sample);
		//	}
		//	if (pChannel->atcHandle)
		//	{
		//	    unsigned char* dstBuf = NULL;
		//	    int dstBufSize = 0;
		//	    if (0 == ATC_Transcode(pChannel->atcHandle, &dstBuf, &dstBufSize, (unsigned char*)pBuf, _frameInfo->length))
		//	    {
		//	        //printf("Audio frame length: %d   -->    %d\n", frameinfo->length, dstBufSize);

		//	        //if (NULL != pChannel->pDSPlayer)
		//	        //{
		//	        //    pChannel->pDSPlayer->Write((char*)dstBuf, dstBufSize);
		//	        //}

		//	        if (pChannel->sendStatus == 1)
		//	        {
		//	            libGB28181Device_AddAudioData(pChannel->id, pChannel->audioOutputFormat, (char*)dstBuf, dstBufSize, dstBufSize);// dstBufSize);
		//	            //libGB28181Device_AddAudioData(7, (char*)dstBuf, dstBufSize, dstBufSize);// dstBufSize);
		//	        }

		//	        //if (NULL == pChannel->fDat)
		//	        //{
		//	        //    //pGB28181Device->fDat = fopen("1.aac", "wb");
		//	        //    pChannel->fDat = fopen("1.711u", "wb");
		//	        //}
		//	        //if (NULL != pChannel->fDat)
		//	        //{
		//	        //    fwrite(pBuf, 1, _frameInfo->length, pChannel->fDat);
		//	        //}
		//	    }
		//	}

		//	//libGB28181Device_AddAudioData(pChannel->id, _frameInfo->codec, (char*)pBuf, _frameInfo->length, _frameInfo->length);
		//}
	}
	else if (_frameType == EASY_SDK_EVENT_FRAME_FLAG)
	{
		if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_DISCONNECTED)
		{
			pThis->OutputLog("当前视频源已断开!\n");
		}
		else if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_CONNECTED)
		{
			pThis->OutputLog("当前视频源连接成功!\n");
		}
		else if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_EXIT)
		{
			pThis->OutputLog("当前视频源已正常断开连接!\n");
		}
		else if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_CONNECT_FAILED)
		{
			pThis->OutputLog("当前视频源连接失败!\n");
		}
		else if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_CONNECTING)
		{
			pThis->OutputLog("当前视频源连接中...\n");
		}
		else if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_CONNECTED)
		{
			pThis->OutputLog("当前视频源连接成功.\n");
		}
		else if (_frameInfo->codec == EASY_STREAM_CLIENT_STATE_CONNECT_ABORT)
		{
			pThis->OutputLog("当前视频源连接异常中断.\n");
		}
	}
	else if (_frameType == EASY_SDK_MEDIA_INFO_FLAG)
	{
		EASY_MEDIA_INFO_T* pMediaInfo = (EASY_MEDIA_INFO_T*)pBuf;

		//memcpy(&pChannel->mediaInfo, pMediaInfo, sizeof(EASY_MEDIA_INFO_T));// pChannel = pMediaInfo->u32AudioCodec;

	}

	return 0;
}



int		CEasyGBDDemoDlg::Startup(const char* serverSIPId, const char* serverIP, const int serverPort,
								const int reg_expires, const int heartbeatCount, const int heartbeatInterval,
								const int protocol, const char* password,
								const int deviceNum, const char* localSIPId, const int localPort, 
								const char* deviceName, const char* sourceURL)
{
	int ret = 0;

	if (NULL == mGB28181DeviceList.pGB28181Device && deviceNum>1)
	{
		OutputLog("启动中...\n");

		mGB28181DeviceList.nDeviceNum = deviceNum;
		mGB28181DeviceList.pGB28181Device = new GB28181_DEVICE_T[deviceNum];
		if (mGB28181DeviceList.pGB28181Device)
		{
			memset(mGB28181DeviceList.pGB28181Device, 0x00, sizeof(GB28181_DEVICE_T) * deviceNum);

			for (int i = 0; i < deviceNum; i++)
			{
				mGB28181DeviceList.pGB28181Device[i].pChannel = new GB28181_CHANNEL_T[MAX_GB28181_CHANNEL_NUM];
				if (mGB28181DeviceList.pGB28181Device[i].pChannel)
				{
					memset(mGB28181DeviceList.pGB28181Device[i].pChannel, 0x00, sizeof(GB28181_CHANNEL_T) * MAX_GB28181_CHANNEL_NUM);
					for (int i = 0; i < MAX_GB28181_CHANNEL_NUM; i++)
					{
						mGB28181DeviceList.pGB28181Device[i].pChannel[i].id = i;
						mGB28181DeviceList.pGB28181Device[i].pChannel[i].userptr = this;
						strcpy(mGB28181DeviceList.pGB28181Device[i].pChannel[i].sourceURL, sourceURL);// "rtsp://admin:admin12345@192.168.6.41");


						mGB28181DeviceList.pGB28181Device[i].pChannel[i].longitude = 121.29;
						mGB28181DeviceList.pGB28181Device[i].pChannel[i].latitude = 31.14;
					}

					GB28181_DEVICE_INFO_T   gb28181DeviceInfo;
					memset(&gb28181DeviceInfo, 0x00, sizeof(GB28181_DEVICE_INFO_T));
					strcpy(gb28181DeviceInfo.device_name, deviceName);// "GB/T28181 Client");

					gb28181DeviceInfo.version = 0;
					strcpy(gb28181DeviceInfo.server_id, serverSIPId);
					memcpy(gb28181DeviceInfo.server_domain, gb28181DeviceInfo.server_id, 10);
					strcpy(gb28181DeviceInfo.server_ip, serverIP);
					gb28181DeviceInfo.server_port = serverPort;
					gb28181DeviceInfo.reg_expires = reg_expires;
					gb28181DeviceInfo.heartbeat_count = heartbeatCount;
					gb28181DeviceInfo.heartbeat_interval = heartbeatInterval;
					gb28181DeviceInfo.protocol = protocol; //0 - udp; 1 - tcp
					gb28181DeviceInfo.media_protocol = protocol;
					strcpy(gb28181DeviceInfo.password, password);
					gb28181DeviceInfo.log_enable = 1;         // log enable flag
					gb28181DeviceInfo.log_level = 0;          // log level(0:TRACE,1:DEBUG,2:INFO,3:WARNING,4:ERROR,5:FATAL)

					strcpy(gb28181DeviceInfo.device_id, localSIPId);
					gb28181DeviceInfo.localSipPort = localPort;

					gb28181DeviceInfo.channel_nums = MAX_GB28181_CHANNEL_NUM;
					for (int i = 0; i < gb28181DeviceInfo.channel_nums; i++)
					{
						sprintf(gb28181DeviceInfo.channel[i].id, "%s1310%06d", gb28181DeviceInfo.server_domain, i + 1);
						//sprintf(gb28181DeviceInfo.channel[i].name, "ch %02d", i + 1);
						strcpy(gb28181DeviceInfo.channel[i].name, "EasyIPC");
						strcpy(gb28181DeviceInfo.channel[i].model, "IPCamera");
						sprintf(gb28181DeviceInfo.channel[i].owner, "Owner %02d", i + 1);
						strcpy(gb28181DeviceInfo.channel[i].manufacturer, "Easy");
						gb28181DeviceInfo.channel[i].longitude = (double)i + 100;
						gb28181DeviceInfo.channel[i].latitude = (double)i + 10;
					}

					libGB28181Device_Create(&gb28181DeviceInfo/*为NULL则表示从当前目录下读取config.xml*/, __GB28181DeviceCALLBACK, (void*)&mGB28181Device);

					for (int i = 0; i < MAX_GB28181_CHANNEL_NUM; i++)
					{
						if (0 == strcmp(mGB28181DeviceList.pGB28181Device[i].pChannel[i].sourceURL, "\0"))    continue;


						EasyStreamClient_Init(&mGB28181DeviceList.pGB28181Device[i].pChannel[i].streamClientHandle, 0);
						EasyStreamClient_SetCallback(mGB28181DeviceList.pGB28181Device[i].pChannel[i].streamClientHandle, __EasyStreamClientCallBack);
						EasyStreamClient_OpenStream(mGB28181DeviceList.pGB28181Device[i].pChannel[i].streamClientHandle,
							mGB28181DeviceList.pGB28181Device[i].pChannel[i].sourceURL, EASY_RTP_OVER_TCP, &mGB28181DeviceList.pGB28181Device[i].pChannel[i], 1000, 20, 1);
						EasyStreamClient_SetAudioEnable(mGB28181DeviceList.pGB28181Device[i].pChannel[i].streamClientHandle, 1);
						//EasyStreamClient_SetAudioOutFormat(mGB28181DeviceList.pGB28181Device[i].pChannel[i].streamClientHandle, EASY_SDK_AUDIO_CODEC_AAC, 8000, 1);
						//mGB28181DeviceList.pGB28181Device[i].pChannel[i].audioOutputFormat = TRANSCODE_AUDIO_TYPE_G711U;// TRANSCODE_AUDIO_TYPE_G711U;

						//libGB28181Device_SetVideoFormat(i, EASY_SDK_VIDEO_CODEC_H265, 0, 0, 0);
						//libGB28181Device_SetAudioFormat(i, EASY_SDK_AUDIO_CODEC_AAC, 8000, 1, 16);
					}

					OutputLog("已启动..\n");
				}
				else
				{
					OutputLog("启动失败...\n");
					ret = -1000;
					return ret;
				}
			}
		}

		return 0;
	}

	if (NULL == mGB28181Device.pChannel)
	{
		OutputLog("启动中...\n");

		mGB28181Device.pChannel = new GB28181_CHANNEL_T[MAX_GB28181_CHANNEL_NUM];
		if (mGB28181Device.pChannel)
		{
			memset(mGB28181Device.pChannel, 0x00, sizeof(GB28181_CHANNEL_T) * MAX_GB28181_CHANNEL_NUM);
			for (int i = 0; i < MAX_GB28181_CHANNEL_NUM; i++)
			{
				mGB28181Device.pChannel[i].id = i;
				mGB28181Device.pChannel[i].userptr = this;
				strcpy(mGB28181Device.pChannel[i].sourceURL, sourceURL);// "rtsp://admin:admin12345@192.168.6.41");


				mGB28181Device.pChannel[i].longitude = 121.29;
				mGB28181Device.pChannel[i].latitude = 31.14;
			}

			GB28181_DEVICE_INFO_T   gb28181DeviceInfo;
			memset(&gb28181DeviceInfo, 0x00, sizeof(GB28181_DEVICE_INFO_T));
			strcpy(gb28181DeviceInfo.device_name, deviceName); //"GB/T28181 Client");

			gb28181DeviceInfo.version = 0;
			strcpy(gb28181DeviceInfo.server_id, serverSIPId);
			memcpy(gb28181DeviceInfo.server_domain, gb28181DeviceInfo.server_id, 10);
			strcpy(gb28181DeviceInfo.server_ip, serverIP);
			gb28181DeviceInfo.server_port = serverPort;
			gb28181DeviceInfo.reg_expires = reg_expires;
			gb28181DeviceInfo.heartbeat_count = heartbeatCount;
			gb28181DeviceInfo.heartbeat_interval = heartbeatInterval;
			gb28181DeviceInfo.protocol = protocol; //0 - udp; 1 - tcp
			gb28181DeviceInfo.media_protocol = protocol;
			strcpy(gb28181DeviceInfo.password, password);
			gb28181DeviceInfo.log_enable = 1;         // log enable flag
			gb28181DeviceInfo.log_level = 0;          // log level(0:TRACE,1:DEBUG,2:INFO,3:WARNING,4:ERROR,5:FATAL)

			strcpy(gb28181DeviceInfo.device_id, localSIPId);
			gb28181DeviceInfo.localSipPort = localPort;

			gb28181DeviceInfo.channel_nums = MAX_GB28181_CHANNEL_NUM;
			for (int i = 0; i < gb28181DeviceInfo.channel_nums; i++)
			{
				sprintf(gb28181DeviceInfo.channel[i].id, "%s1310%06d", gb28181DeviceInfo.server_domain, i + 1);
				//sprintf(gb28181DeviceInfo.channel[i].name, "ch %02d", i + 1);
				strcpy(gb28181DeviceInfo.channel[i].name, "EasyIPC");
				strcpy(gb28181DeviceInfo.channel[i].model, "IPCamera");
				sprintf(gb28181DeviceInfo.channel[i].owner, "Owner %02d", i + 1);
				strcpy(gb28181DeviceInfo.channel[i].manufacturer, "Easy");
				gb28181DeviceInfo.channel[i].longitude = (double)i + 100;
				gb28181DeviceInfo.channel[i].latitude = (double)i + 10;
			}

			libGB28181Device_Create(&gb28181DeviceInfo/*为NULL则表示从当前目录下读取config.xml*/, __GB28181DeviceCALLBACK, (void*)&mGB28181Device);

			for (int i = 0; i < MAX_GB28181_CHANNEL_NUM; i++)
			{
				if (0 == strcmp(mGB28181Device.pChannel[i].sourceURL, "\0"))    continue;


				EasyStreamClient_Init(&mGB28181Device.pChannel[i].streamClientHandle, 0);
				EasyStreamClient_SetCallback(mGB28181Device.pChannel[i].streamClientHandle, __EasyStreamClientCallBack);
				EasyStreamClient_OpenStream(mGB28181Device.pChannel[i].streamClientHandle,
					mGB28181Device.pChannel[i].sourceURL, EASY_RTP_OVER_TCP, &mGB28181Device.pChannel[i], 1000, 20, 1);
				EasyStreamClient_SetAudioEnable(mGB28181Device.pChannel[i].streamClientHandle, 1);
				//EasyStreamClient_SetAudioOutFormat(mGB28181Device.pChannel[i].streamClientHandle, EASY_SDK_AUDIO_CODEC_AAC, 8000, 1);
				//mGB28181Device.pChannel[i].audioOutputFormat = TRANSCODE_AUDIO_TYPE_G711U;// TRANSCODE_AUDIO_TYPE_G711U;

				//libGB28181Device_SetVideoFormat(i, EASY_SDK_VIDEO_CODEC_H265, 0, 0, 0);
				//libGB28181Device_SetAudioFormat(i, EASY_SDK_AUDIO_CODEC_AAC, 8000, 1, 16);
			}

			OutputLog("已启动..\n");
		}
		else
		{
			OutputLog("启动失败...\n");
			ret = -1000;
			return ret;
		}
	}


	return ret;
}
void	CEasyGBDDemoDlg::Shutdown()
{
	if (mGB28181DeviceList.pGB28181Device)
	{
		for (int i = 0; i < mGB28181DeviceList.nDeviceNum; i++)
		{
			for (int ch = 0; ch < MAX_GB28181_CHANNEL_NUM; ch++)
			{
				if (0 == strcmp(mGB28181DeviceList.pGB28181Device[i].pChannel[ch].sourceURL, "\0"))    continue;

				mGB28181DeviceList.pGB28181Device[i].pChannel[ch].sendStatus = 0;

				EasyStreamClient_Deinit(mGB28181DeviceList.pGB28181Device[i].pChannel[ch].streamClientHandle);
				mGB28181DeviceList.pGB28181Device[i].pChannel[ch].streamClientHandle = NULL;

				//ATC_Deinit(&mGB28181Device.pChannel[i].atcHandle);

				if (NULL != mGB28181DeviceList.pGB28181Device[i].pChannel[ch].fDat)
				{
					fclose(mGB28181DeviceList.pGB28181Device[i].pChannel[ch].fDat);
					mGB28181DeviceList.pGB28181Device[i].pChannel[ch].fDat = NULL;
				}
			}

			delete[]mGB28181DeviceList.pGB28181Device[i].pChannel;
			mGB28181DeviceList.pGB28181Device[i].pChannel = NULL;
		}

		delete[]mGB28181DeviceList.pGB28181Device;
		mGB28181DeviceList.pGB28181Device = NULL;
	}



	if (NULL == mGB28181Device.pChannel)		return;

	OutputLog("停止中...\n");

	for (int i = 0; i < MAX_GB28181_CHANNEL_NUM; i++)
	{
		if (0 == strcmp(mGB28181Device.pChannel[i].sourceURL, "\0"))    continue;

		mGB28181Device.pChannel[i].sendStatus = 0;

		EasyStreamClient_Deinit(mGB28181Device.pChannel[i].streamClientHandle);
		mGB28181Device.pChannel[i].streamClientHandle = NULL;

		//ATC_Deinit(&mGB28181Device.pChannel[i].atcHandle);

		if (NULL != mGB28181Device.pChannel[i].fDat)
		{
			fclose(mGB28181Device.pChannel[i].fDat);
			mGB28181Device.pChannel[i].fDat = NULL;
		}
	}

	delete[]mGB28181Device.pChannel;
	mGB28181Device.pChannel = NULL;

	libGB28181Device_Release();


	OutputLog("已停止.\n");
}

void CEasyGBDDemoDlg::OnDestroy()
{
	CDialogEx::OnDestroy();

	Shutdown();
	DeinitMutex(&mLogMutex);
}



void CEasyGBDDemoDlg::OnBnClickedButtonBrowse()
{
	//wchar_t szDefaultOpenPath[MAX_PATH] = { 0 };
	//MByteToWChar(globalRecordingPath, szDefaultOpenPath, sizeof(szDefaultOpenPath) / sizeof(szDefaultOpenPath[0]));

	CFileDialog file(TRUE, NULL, NULL, OFN_HIDEREADONLY | OFN_OVERWRITEPROMPT, L"媒体文件(*.*)|*.*||");
	//file.m_ofn.lpstrInitialDir = szDefaultOpenPath;
	if (file.DoModal() != IDOK)		return;

	//获取文件名称
	file.GetFileName();
	//获取文件路径,此处只想说明下file.GetPathName()的返回值类型。
	CString filePath = file.GetPathName();

	pEdtSourceURL->SetWindowTextW(filePath);
}



