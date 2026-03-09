#pragma once


// EasyGBD_VideoDlg 对话框

class EasyGBD_VideoDlg : public CDialogEx
{
	DECLARE_DYNAMIC(EasyGBD_VideoDlg)

public:
	EasyGBD_VideoDlg(CWnd* pParent = nullptr);   // 标准构造函数
	virtual ~EasyGBD_VideoDlg();

// 对话框数据
#ifdef AFX_DESIGN_TIME
	enum { IDD = ID_GBD_DIALOG_VIDEO };
#endif

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()
};
