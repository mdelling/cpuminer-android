/*
 * neon_detect.c
 *
 *  Created on: Feb 1, 2014
 *      Author: matthewdellinger
 */

#include "cpu-features.h"
#include <jni.h>
#include <string.h>
#include <android/log.h>
#define DEBUG_TAG "CPUMiner_NEONDetect"

int Java_com_mdelling_cpuminer_CPUMinerApplication_detectCPUHasNeon( JNIEnv* _env, jobject this )
{
    uint64_t features;

    if (android_getCpuFamily() != ANDROID_CPU_FAMILY_ARM)
    {
        __android_log_write( ANDROID_LOG_INFO, "detectCPU", "Processor is NOT an ARM processor" );
        return 0;
    }

    __android_log_write( ANDROID_LOG_INFO, "detectCPU", "Processor is an ARM processor" );

    features = android_getCpuFeatures();

    __android_log_print( ANDROID_LOG_INFO, "detectCPU", "Processor features: %u", (unsigned int)features );

    if ((features & ANDROID_CPU_ARM_FEATURE_ARMv7) == 0)
    {
        __android_log_write( ANDROID_LOG_INFO, "detectCPU", "Processor is NOT an ARM v7" );
        return 0;
    }

    __android_log_write( ANDROID_LOG_INFO, "detectCPU", "Processor is an ARM v7" );

    if ((features & ANDROID_CPU_ARM_FEATURE_NEON) == 0)
    {
        __android_log_write( ANDROID_LOG_INFO, "detectCPU", "Processor has NO NEON support" );
        return 0;
    }

    __android_log_write( ANDROID_LOG_INFO, "detectCPU", "Processor has NEON support" );

    return 1;
}
