#include "XmlConfig.h"
#include "tinyxml/tinystr.h"
#include "tinyxml/tinyxml.h"
#include <time.h>

XmlConfig::XmlConfig(void)
{
	memset(&xmlConfig, 0x00, sizeof(XML_CONFIG_T));
}


XmlConfig::~XmlConfig(void)
{
}


int		XmlConfig::LoadConfig(const char *filename, XML_CONFIG_T *pConfig)
{
	if (NULL == filename)			return -1;

	memset(&xmlConfig, 0x00, sizeof(XML_CONFIG_T));

	int ret = -1;
	TiXmlDocument m_DocR;
	if (!m_DocR.LoadFile(filename))
	{
		//libLogger_Print(NULL, LOG_TYPE_DEBUG, "XmlConfig:: Failed to read configuration file.\nuse default parameters. file:%s\n", filename);

		char szTime[64] = { 0 };
		time_t tt = time(NULL);
		struct tm* _timetmp = NULL;
		_timetmp = localtime(&tt);
		if (NULL != _timetmp)   strftime(szTime, 32, "%Y%m%d%H%M%S", _timetmp);

		xmlConfig.SipType = 0;
		strcpy(xmlConfig.serverSipID, "34020000002000000001");
		strcpy(xmlConfig.serverIP, "192.168.1.106");
		xmlConfig.serverSipPort = 15060;
		xmlConfig.registerExpire = 3600;
		xmlConfig.heartbeatCount = 3;
		xmlConfig.heartbeatInterval = 60;
		strcpy(xmlConfig.protocol, "UDP");
		strcpy(xmlConfig.password, "12345678");
		strcpy(xmlConfig.localSipID, "34020000001110000009");
		xmlConfig.localPort = 15090;
		sprintf(xmlConfig.deviceName, "EasyGBD%s", szTime);
		strcpy(xmlConfig.sourcePath, "EasyDarwin.mp4");

		SaveConfig(filename, &xmlConfig);			//不存在配置文件, 生成一个新的配置文件
		if (NULL != pConfig)		memcpy(pConfig, &xmlConfig, sizeof(XML_CONFIG_T));
		return ret;
	}

	//libLogger_Print(NULL, LOG_TYPE_DEBUG, "XmlConfig::Open the configuration file successfully: %s\n", filename);

	TiXmlHandle hDoc(&m_DocR);
	TiXmlHandle hRoot(0);

	TiXmlElement *pServerConfigXML = hDoc.FirstChild("XmlConfig").ToElement();
	if (NULL != pServerConfigXML)
	{
		TiXmlElement *pE;

		pE = pServerConfigXML->FirstChildElement("SipType");
		if (pE && pE->GetText())		xmlConfig.SipType = atoi(pE->GetText());

		pE = pServerConfigXML->FirstChildElement("serverSipID");
		if (pE && pE->GetText())		strcpy(xmlConfig.serverSipID, pE->GetText());

		pE = pServerConfigXML->FirstChildElement("serverIP");
		if (pE && pE->GetText())		strcpy(xmlConfig.serverIP, pE->GetText());

		pE = pServerConfigXML->FirstChildElement("serverSipPort");
		if (pE && pE->GetText())		xmlConfig.serverSipPort = atoi(pE->GetText());

		pE = pServerConfigXML->FirstChildElement("registerExpire");
		if (pE && pE->GetText())		xmlConfig.registerExpire = atoi(pE->GetText());

		pE = pServerConfigXML->FirstChildElement("heartbeatCount");
		if (pE && pE->GetText())		xmlConfig.heartbeatCount = atoi(pE->GetText());

		pE = pServerConfigXML->FirstChildElement("heartbeatInterval");
		if (pE && pE->GetText())		xmlConfig.heartbeatInterval = atoi(pE->GetText());

		pE = pServerConfigXML->FirstChildElement("protocol");
		if (pE && pE->GetText())		strcpy(xmlConfig.protocol, pE->GetText());

		pE = pServerConfigXML->FirstChildElement("password");
		if (pE && pE->GetText())		strcpy(xmlConfig.password, pE->GetText());

		pE = pServerConfigXML->FirstChildElement("localSipID");
		if (pE && pE->GetText())		strcpy(xmlConfig.localSipID, pE->GetText());

		pE = pServerConfigXML->FirstChildElement("localPort");
		if (pE && pE->GetText())		xmlConfig.localPort = atoi(pE->GetText());

		pE = pServerConfigXML->FirstChildElement("deviceName");
		if (pE && pE->GetText())		strcpy(xmlConfig.deviceName, pE->GetText());

		pE = pServerConfigXML->FirstChildElement("sourcePath");
		if (pE && pE->GetText())		strcpy(xmlConfig.sourcePath, pE->GetText());

		ret = 0;
	}

	if (NULL != pConfig)		memcpy(pConfig, &xmlConfig, sizeof(XML_CONFIG_T));

	return ret;
}


int	AddElement(char *propertyName, char *propertyValue, TiXmlElement *pParent)
{
	TiXmlElement *pProperty = new TiXmlElement(propertyName);
	TiXmlText* pValue = new TiXmlText(propertyValue);
	pProperty->InsertEndChild(*pValue);
	pParent->InsertEndChild(*pProperty);
	delete pValue;
	delete pProperty;

	return 0;
}

int	AddElement(char *propertyName, int value, TiXmlElement *pParent)
{
	char sztmp[16] = {0};
	sprintf(sztmp, "%d", value);

	TiXmlElement *pProperty = new TiXmlElement(propertyName);
	TiXmlText* pValue = new TiXmlText(sztmp);
	pProperty->InsertEndChild(*pValue);
	pParent->InsertEndChild(*pProperty);
	delete pValue;
	delete pProperty;

	return 0;
}
void	XmlConfig::SaveConfig(const char *filename, XML_CONFIG_T *pConfig)
{
	if (NULL == filename)		return;
	if (NULL != pConfig)
	{
		memcpy(&xmlConfig, pConfig, sizeof(XML_CONFIG_T));
	}


	TiXmlDocument xmlDoc(filename);
	TiXmlDeclaration Declaration( "1.0", "UTF-8", "yes" );
	xmlDoc.InsertEndChild( Declaration );

	TiXmlElement* pRootElm = NULL;
	pRootElm = new TiXmlElement( "XmlConfig" );


	AddElement((char*)"SipType",			xmlConfig.SipType,				pRootElm);
	AddElement((char*)"serverSipID",		xmlConfig.serverSipID,			pRootElm);
	AddElement((char*)"serverIP",			xmlConfig.serverIP,				pRootElm);
	AddElement((char*)"serverSipPort",		xmlConfig.serverSipPort,		pRootElm);
	AddElement((char*)"registerExpire",		xmlConfig.registerExpire,		pRootElm);
	AddElement((char*)"heartbeatCount",		xmlConfig.heartbeatCount,		pRootElm);
	AddElement((char*)"heartbeatInterval", xmlConfig.heartbeatInterval,		pRootElm);
	AddElement((char*)"protocol",			xmlConfig.protocol,				pRootElm);
	AddElement((char*)"password",			xmlConfig.password,				pRootElm);
	AddElement((char*)"localSipID",			xmlConfig.localSipID,			pRootElm);
	AddElement((char*)"localPort",			xmlConfig.localPort,			pRootElm);
	AddElement((char*)"deviceName",			xmlConfig.deviceName,			pRootElm);
	AddElement((char*)"sourcePath",			xmlConfig.sourcePath,			pRootElm);

	xmlDoc.InsertEndChild(*pRootElm) ;

	//xmlDoc.Print() ;
	if (xmlDoc.SaveFile())
	{
	}
	delete pRootElm;
}

