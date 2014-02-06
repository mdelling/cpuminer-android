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

void Java_com_mdelling_cpuminer_MainActivity_startMiner(JNIEnv * env, jobject this, jint number, jstring parameters)
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
	cpuminer_start(number, argv);
	free(argv);
	(*env)->ReleaseStringUTFChars(env, parameters, szParameters);
}

void Java_com_mdelling_cpuminer_MainActivity_stopMiner(JNIEnv * env, jobject this)
{
	stop_miner();
}

jlong Java_com_mdelling_cpuminer_MainActivity_getAccepted(JNIEnv * env, jobject this)
{
	return get_accepted();
}

jlong Java_com_mdelling_cpuminer_MainActivity_getRejected(JNIEnv * env, jobject this)
{
	return get_rejected();
}

jint Java_com_mdelling_cpuminer_MainActivity_getThreads(JNIEnv * env, jobject this)
{
	return get_threads();
}

jint Java_com_mdelling_cpuminer_MainActivity_getHashRate(JNIEnv * env, jobject this, jint cpu)
{
	return get_hash_rate(cpu);
}
