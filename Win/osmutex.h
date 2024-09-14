#ifndef __OS_MUTEX_H__
#define __OS_MUTEX_H__

#ifdef _WIN32
#include <winsock2.h>

#else
#include <pthread.h>

#endif

#ifdef _WIN32
typedef	CRITICAL_SECTION	OSMutex;
#else
typedef	pthread_mutex_t		OSMutex;
#endif


int InitMutex(OSMutex *mutex);
int	LockMutex(OSMutex *mutex);
int UnlockMutex(OSMutex *mutex);
int DeinitMutex(OSMutex *mutex);



#endif
