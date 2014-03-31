/*
 * native.c
 *
 *  Created on: Jan 26, 2014
 *      Author: matthewdellinger
 */

#include <jni.h>
#include <string.h>
#include <android/log.h>
#define DEBUG_TAG "CPUMiner_NativeLauncher"

jint Java_com_mdelling_cpuminer_MainActivity_startMiner(JNIEnv * env, jobject this, jint number, jstring parameters)
{
	jboolean isCopy;
	const char * szParameters = (*env)->GetStringUTFChars(env, parameters, &isCopy);
	char **argv = malloc(number * sizeof(char *));
	int count = 0;
	__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK: [Starting miner in native code]");
	__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK: [%d: %s]", number, szParameters);
	char *tok = strtok(szParameters, " ");
	while (tok != NULL) {
		argv[count++] = tok;
		__android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK: [%d: %s]", count - 1, argv[count - 1]);
	    tok = strtok (NULL, " ");
	}
	int retval = cpuminer_start(number, argv);
	free(argv);
	(*env)->ReleaseStringUTFChars(env, parameters, szParameters);
	return retval;
}

void Java_com_mdelling_cpuminer_MainActivity_stopMiner(JNIEnv * env, jobject this)
{
	stop_miner();
}

extern long get_accepted(void);
jlong Java_com_mdelling_cpuminer_CPUMinerApplication_getAccepted(JNIEnv * env, jobject this)
{
	return get_accepted();
}

extern long get_rejected(void);
jlong Java_com_mdelling_cpuminer_CPUMinerApplication_getRejected(JNIEnv * env, jobject this)
{
	return get_rejected();
}

extern int get_threads(void);
jint Java_com_mdelling_cpuminer_CPUMinerApplication_getThreads(JNIEnv * env, jobject this)
{
	return get_threads();
}

extern double get_hash_rate(int cpu);
jdouble Java_com_mdelling_cpuminer_CPUMinerApplication_getHashRate(JNIEnv * env, jobject this, jint cpu)
{
	return get_hash_rate(cpu);
}
