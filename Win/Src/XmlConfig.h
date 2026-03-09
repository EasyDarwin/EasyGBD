#pragma once


#define	SERVER_CONFIG_FILENAME "Config.xml"

typedef struct __XML_CHANNEL_T
{
	char cid[32];
}XML_CHANNEL_T;
typedef struct __XML_CONFIG_T
{
	int			SipType;		// 0x00:GB2818
	char		serverSipID[32];
	char		serverDomain[16]; //”Ú
	char		serverIP[64];
	int			serverSipPort;
	int			registerExpire;
	int			heartbeatCount;
	int			heartbeatInterval;
	char		protocol[16];
	char		password[32];
	char		localSipID[32];
	int			localPort;
	char		deviceName[128];
	char		sourcePath[1024];
	XML_CHANNEL_T channels;
}XML_CONFIG_T;

class XmlConfig
{
public:
	XmlConfig(void);
	~XmlConfig(void);

	int		LoadConfig(const char *filename, XML_CONFIG_T *pConfig);
	void	SaveConfig(const char *filename, XML_CONFIG_T *pConfig);
	//int		LoadConfigToJson(int serverType, char *filename, char *outJsonStr);

	XML_CONFIG_T	*GetConfig()	{return &xmlConfig;}
protected:

	XML_CONFIG_T		xmlConfig;
};

