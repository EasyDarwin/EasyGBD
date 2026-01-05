#pragma once


#define	SERVER_CONFIG_FILENAME "Config.xml"


typedef struct __XML_CONFIG_T
{
	int			SipType;		// 0x00:GB2818   0x01:¹úÍøB
	char		serverSipID[32];
	char		serverIP[64];
	int			serverSipPort;
	int			registerExpire;
	int			heartbeatCount;
	int			heartbeatInterval;
	char		protocol[16];
	char		password[32];
	char		localSipID[32];
	int			localPort;
	char		deviceName[64];
	char		sourcePath[260];
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

