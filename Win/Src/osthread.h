#ifndef __OS_THREAD_H__
#define __OS_THREAD_H__


#ifdef _WIN32
#include <WinSock2.h>
#include <stdlib.h>
#else
#include <pthread.h>
#endif




#ifdef _WIN32
typedef HANDLE				OSThreadHandle;
#define						LINUX_SET_THREAD_NAME_TYPE		0x00
#else
typedef pthread_t			OSThreadHandle;
#define						LINUX_SET_THREAD_NAME_TYPE		0x01
#endif

typedef enum __THREAD_STATUS_ENUM
{
	THREAD_STATUS_INIT		=	0x00,
	THREAD_STATUS_CREATE,
	THREAD_STATUS_RUNNING,
	THREAD_STATUS_EXIT,
	THREAD_STATUS_EXIT_EXT,
	THREAD_STATUS_CHANGE_PARAM,

	THREAD_STATUS_PARAM_ERROR	=	-1,
	THREAD_STATUS_CREATE_FAIL	=	-2	//

}THREAD_STATUS_ENUM;


typedef enum __OSTHREAD_PRIORITY_E
{
	OS_THREAD_PRIORITY_HIGH	=	1,
	OS_THREAD_PRIORITY_NORMAL,


}OSTHREAD_PRIORITY_E;

typedef struct OSTHREAD_OBJ_T
{
	int					threadId;
	int					customId;
	OSThreadHandle		hHandle;
	int					flag;
	void				*userPtr;
	void				*pEx;
}OSTHREAD_OBJ_T;


#ifdef WIN32
#define		THREAD_RESULT	DWORD WINAPI
#else
#define		THREAD_RESULT	void*
#endif



#ifdef _WIN32
int CreateOSThread(OSTHREAD_OBJ_T **handle, void *procFunc, void *userPtr, int customId);
#else
int CreateOSThread(OSTHREAD_OBJ_T **handle, void *(*procFunc)(void *), void *userPtr, int customId);
#endif

int SetOSThreadPriority(OSTHREAD_OBJ_T* handle, OSTHREAD_PRIORITY_E priority);

int SetOSThreadName(OSTHREAD_OBJ_T* handle, const char* szFormat, ...);

int DeleteOSThread(OSTHREAD_OBJ_T **handle);





#endif
