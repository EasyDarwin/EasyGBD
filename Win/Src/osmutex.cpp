#include "osmutex.h"



int InitMutex(OSMutex *mutex)
{
#ifdef _WIN32
	InitializeCriticalSection(mutex);
#else
	pthread_mutex_init(mutex, NULL);
#endif
	return 0;
}

int	LockMutex(OSMutex *mutex)
{
#ifdef _WIN32
	EnterCriticalSection(mutex);
#else
	(void)pthread_mutex_lock(mutex);
#endif
	return 0;
}

int UnlockMutex(OSMutex *mutex)
{
#ifdef _WIN32
	LeaveCriticalSection(mutex);
#else
	pthread_mutex_unlock(mutex);
#endif
	return 0;
}

int DeinitMutex(OSMutex *mutex)
{
#ifdef _WIN32
	DeleteCriticalSection(mutex);
#else
	pthread_mutex_destroy(mutex);
#endif
	return 0;
}

