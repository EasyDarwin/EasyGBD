#include "osthread.h"


#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdarg.h>
#ifndef _WIN32
#include <unistd.h>
#define Sleep(x)			{usleep(x*1000);}
#endif

#ifdef _WIN32
int CreateOSThread(OSTHREAD_OBJ_T **handle, void *procFunc, void *userPtr, int customId)
#else
int CreateOSThread(OSTHREAD_OBJ_T **handle, void *(*procFunc)(void *), void *userPtr, int customId)
#endif
{
	if (NULL == procFunc)		return THREAD_STATUS_PARAM_ERROR;

	OSTHREAD_OBJ_T* pThread = (OSTHREAD_OBJ_T*)malloc(sizeof(OSTHREAD_OBJ_T));
	if (NULL == pThread)		return THREAD_STATUS_CREATE_FAIL;

	memset(pThread, 0x00, sizeof(OSTHREAD_OBJ_T));
	pThread->flag = THREAD_STATUS_CREATE;
	pThread->userPtr = userPtr;
	pThread->customId = customId;

#ifdef _WIN32
	pThread->hHandle = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)procFunc, (LPVOID)pThread, 0, (LPDWORD)&pThread->threadId);
	if (NULL == pThread->hHandle)
	{
		free(pThread);
		return THREAD_STATUS_CREATE_FAIL;
	}
#else
	if(pthread_create(&pThread->hHandle, NULL, procFunc, pThread) < 0)
    {
		free(pThread);
		 return THREAD_STATUS_CREATE_FAIL;
    }
#endif
	while (pThread->flag != THREAD_STATUS_RUNNING && pThread->flag!= THREAD_STATUS_INIT && pThread->flag!= THREAD_STATUS_EXIT_EXT)	{Sleep(10);}
	if (pThread->flag == THREAD_STATUS_INIT)
	{
		free(pThread);
		return THREAD_STATUS_EXIT;
	}
	else if (pThread->flag == THREAD_STATUS_EXIT_EXT)		//表示已处理, 线程正常退出
	{
		free(pThread);
		return THREAD_STATUS_EXIT_EXT;
	}

	*handle = pThread;

	return 0;
}

int SetOSThreadPriority(OSTHREAD_OBJ_T* handle, OSTHREAD_PRIORITY_E priority)
{
	OSTHREAD_OBJ_T* pThread = (OSTHREAD_OBJ_T*)handle;
	if (NULL == pThread)		return -1;


#ifdef _WIN32
	if (OS_THREAD_PRIORITY_HIGH == priority)
	{
		SetThreadPriority(pThread->hHandle, THREAD_PRIORITY_HIGHEST);
	}
	else if (OS_THREAD_PRIORITY_NORMAL == priority)
	{
		SetThreadPriority(pThread->hHandle, THREAD_PRIORITY_NORMAL);
	}
#else
		// Start out with a standard, low-priority setup for the sched params.
		struct sched_param sp;
		bzero((void*)&sp, sizeof(sp));
		int policy = SCHED_OTHER;

		// If desired, set up high-priority sched params structure.
		if (OS_THREAD_PRIORITY_HIGH == priority) {
			// FIFO scheduler, ranked above default SCHED_OTHER queue
			policy = SCHED_FIFO;
			// The priority only compares us to other SCHED_FIFO threads, so we
			// just pick a random priority halfway between min & max.
			const int priority = (sched_get_priority_max(policy) + sched_get_priority_min(policy)) / 2;

			sp.sched_priority = priority;
		}

		// Actually set the sched params for the current thread.
		if (0 == pthread_setschedparam(pThread->hHandle, policy, &sp)) {
			printf("IO Thread #%p using high-priority scheduler!", pThread->hHandle);
		}
#endif

		return 0;
}

int SetOSThreadName(OSTHREAD_OBJ_T* handle, const char* szFormat, ...)
{
	char szBuf[64] = { 0 };
#ifndef _MBCS
	va_list args;
	va_start(args, szFormat);
#ifdef _WIN32
	_vsnprintf(szBuf, sizeof(szBuf) - 1, szFormat, args);
#else
	vsnprintf(szBuf, sizeof(szBuf) - 1, szFormat, args);
#endif
	va_end(args);
#else
	va_list args;
	va_start(args, szFormat);
	vsnprintf(szBuf, sizeof(szBuf) - 1, szFormat, args);
	va_end(args);
#endif

	

	// 以下为三种方式

	

#if LINUX_SET_THREAD_NAME_TYPE==0x00
	//SetThreadDescription(handle->hHandle, TEXT("thread"));			// windows
#elif LINUX_SET_THREAD_NAME_TYPE==0x01
	pthread_setname_np(handle->hHandle, szBuf);
#elif LINUX_SET_THREAD_NAME_TYPE==0x02
	prctl(PR_SET_NAME, szBuf, 0, 0, 0);
#elif LINUX_SET_THREAD_NAME_TYPE==0x03
	size_t stacksize = sysconf(_SC_THREAD_STACK_MIN); // 获取最小栈大小
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_attr_setstacksize(&attr, stacksize + PTHREAD_STACK_MIN); // 设置栈大小，确保有足够的空间存放名称信息
	pthread_setname_np(szBuf); // 设置线程名称
#endif

	return 0;
}



int DeleteOSThread(OSTHREAD_OBJ_T **handle)
{
	if (NULL == handle)			return -1;

	OSTHREAD_OBJ_T *pThread = (OSTHREAD_OBJ_T *)*handle;
	if (NULL == pThread)		return -1;

	if (pThread->flag == THREAD_STATUS_RUNNING)		pThread->flag = THREAD_STATUS_EXIT;
#ifdef _WIN32
	while (pThread->flag != THREAD_STATUS_INIT && pThread->flag != THREAD_STATUS_EXIT_EXT)	{Sleep(10);}
#else
	while (pThread->flag != THREAD_STATUS_INIT && pThread->flag != THREAD_STATUS_EXIT_EXT)	{usleep(1000*10);}
#endif

#ifdef _WIN32
	if (NULL != pThread->hHandle)
	{
		if (pThread->flag == THREAD_STATUS_RUNNING)	pThread->flag = THREAD_STATUS_EXIT;
		while (pThread->flag != THREAD_STATUS_INIT && pThread->flag != THREAD_STATUS_EXIT_EXT)	{Sleep(10);}
		CloseHandle(pThread->hHandle);
		pThread->hHandle = NULL;
	}
#else
	if (pThread->hHandle > 0U)
	{
		if (pThread->flag == THREAD_STATUS_RUNNING)	pThread->flag = THREAD_STATUS_EXIT;
		while (pThread->flag != THREAD_STATUS_INIT && pThread->flag != THREAD_STATUS_EXIT_EXT)	{usleep(1000*10);}
		pthread_join(pThread->hHandle, NULL);
		pThread->hHandle = 0x00;
	}
#endif

	free(pThread);
	*handle = NULL;

	return 0;
}
