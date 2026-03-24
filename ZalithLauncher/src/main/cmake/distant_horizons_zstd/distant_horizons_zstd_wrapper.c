#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <stdbool.h>
#include <stddef.h>

#define DH_ZSTD_TAG "dh_zstd_wrapper"
#define UPSTREAM_ZSTD_LIB "libzstd-jni-1.5.7-6.so"

static void* dh_open_upstream(JNIEnv* env) {
    static void* handle = NULL;
    if (handle != NULL) return handle;

    handle = dlopen(UPSTREAM_ZSTD_LIB, RTLD_NOW | RTLD_GLOBAL);
    if (handle == NULL) {
        jclass errorClass = (*env)->FindClass(env, "java/lang/UnsatisfiedLinkError");
        if (errorClass != NULL) {
            (*env)->ThrowNew(env, errorClass, dlerror());
        }
        __android_log_print(ANDROID_LOG_ERROR, DH_ZSTD_TAG, "Failed to open %s: %s", UPSTREAM_ZSTD_LIB, dlerror());
    }
    return handle;
}

static void* dh_resolve_symbol(JNIEnv* env, const char* symbolName) {
    void* handle = dh_open_upstream(env);
    if (handle == NULL) return NULL;

    dlerror();
    void* symbol = dlsym(handle, symbolName);
    const char* error = dlerror();
    if (error != NULL || symbol == NULL) {
        jclass errorClass = (*env)->FindClass(env, "java/lang/UnsatisfiedLinkError");
        if (errorClass != NULL) {
            (*env)->ThrowNew(env, errorClass, symbolName);
        }
        __android_log_print(ANDROID_LOG_ERROR, DH_ZSTD_TAG, "Failed to resolve %s: %s", symbolName, error != NULL ? error : "<null>");
        return NULL;
    }
    return symbol;
}

static void* dh_find_symbol(const char* symbolName) {
    void* handle = dlopen(UPSTREAM_ZSTD_LIB, RTLD_NOW | RTLD_GLOBAL);
    if (handle == NULL) return NULL;
    dlerror();
    void* symbol = dlsym(handle, symbolName);
    return dlerror() == NULL ? symbol : NULL;
}

static jobject dh_convert_frame_progression(JNIEnv* env, jobject upstreamFrameProgression) {
    if (upstreamFrameProgression == NULL) return NULL;

    jclass upstreamClass = (*env)->GetObjectClass(env, upstreamFrameProgression);
    if (upstreamClass == NULL) return NULL;

    jfieldID ingestedField = (*env)->GetFieldID(env, upstreamClass, "ingested", "J");
    jfieldID consumedField = (*env)->GetFieldID(env, upstreamClass, "consumed", "J");
    jfieldID producedField = (*env)->GetFieldID(env, upstreamClass, "produced", "J");
    jfieldID flushedField = (*env)->GetFieldID(env, upstreamClass, "flushed", "J");
    jfieldID currentJobIDField = (*env)->GetFieldID(env, upstreamClass, "currentJobID", "I");
    jfieldID activeWorkersField = (*env)->GetFieldID(env, upstreamClass, "nbActiveWorkers", "I");
    if (ingestedField == NULL || consumedField == NULL || producedField == NULL || flushedField == NULL ||
            currentJobIDField == NULL || activeWorkersField == NULL) {
        return NULL;
    }

    jlong ingested = (*env)->GetLongField(env, upstreamFrameProgression, ingestedField);
    jlong consumed = (*env)->GetLongField(env, upstreamFrameProgression, consumedField);
    jlong produced = (*env)->GetLongField(env, upstreamFrameProgression, producedField);
    jlong flushed = (*env)->GetLongField(env, upstreamFrameProgression, flushedField);
    jint currentJobID = (*env)->GetIntField(env, upstreamFrameProgression, currentJobIDField);
    jint activeWorkers = (*env)->GetIntField(env, upstreamFrameProgression, activeWorkersField);

    jclass shadedClass = (*env)->FindClass(env, "dhcomgithubluben/zstd/ZstdFrameProgression");
    if (shadedClass == NULL) return NULL;

    jmethodID ctor = (*env)->GetMethodID(env, shadedClass, "<init>", "(JJJJII)V");
    if (ctor == NULL) return NULL;

    return (*env)->NewObject(env, shadedClass, ctor, ingested, consumed, produced, flushed, currentJobID, activeWorkers);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdBufferDecompressingStreamNoFinalizer_createDStreamNative(JNIEnv* env, jobject thiz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdBufferDecompressingStreamNoFinalizer_createDStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdBufferDecompressingStreamNoFinalizer_freeDStreamNative(JNIEnv* env, jobject thiz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdBufferDecompressingStreamNoFinalizer_freeDStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdBufferDecompressingStreamNoFinalizer_initDStreamNative(JNIEnv* env, jobject thiz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdBufferDecompressingStreamNoFinalizer_initDStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdBufferDecompressingStreamNoFinalizer_decompressStreamNative(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1, jint arg2, jint arg3, jbyteArray arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray, jint, jint, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdBufferDecompressingStreamNoFinalizer_decompressStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdBufferDecompressingStreamNoFinalizer_recommendedDOutSizeNative(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdBufferDecompressingStreamNoFinalizer_recommendedDOutSizeNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_compressUnsafe(JNIEnv* env, jclass clazz, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jint arg4, jboolean arg5) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jlong, jlong, jlong, jint, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_compressUnsafe");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_decompressUnsafe(JNIEnv* env, jclass clazz, jlong arg0, jlong arg1, jlong arg2, jlong arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jlong, jlong, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_decompressUnsafe");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_loadDictDecompress(JNIEnv* env, jclass clazz, jlong arg0, jbyteArray arg1, jint arg2) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jbyteArray, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_loadDictDecompress");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1, arg2);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_loadFastDictDecompress(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_loadFastDictDecompress");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_loadDictCompress(JNIEnv* env, jclass clazz, jlong arg0, jbyteArray arg1, jint arg2) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jbyteArray, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_loadDictCompress");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1, arg2);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_loadFastDictCompress(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_loadFastDictCompress");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_Zstd_registerSequenceProducer(JNIEnv* env, jclass clazz, jlong arg0, jlong arg1, jlong arg2) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong, jlong, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_registerSequenceProducer");
    if (fn == NULL) return;
    fn(env, clazz, arg0, arg1, arg2);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getBuiltinSequenceProducer(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getBuiltinSequenceProducer");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_Zstd_generateSequences(JNIEnv* env, jclass clazz, jlong arg0, jlong arg1, jlong arg2, jlong arg3, jlong arg4) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong, jlong, jlong, jlong, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_find_symbol("Java_com_github_luben_zstd_Zstd_generateSequences");
    if (fn != NULL) {
        fn(env, clazz, arg0, arg1, arg2, arg3, arg4);
    }
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getStubSequenceProducer(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getStubSequenceProducer");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionChecksums(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionChecksums");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionMagicless(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionMagicless");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionLevel(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionLevel");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionLong(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionLong");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionWorkers(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionWorkers");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionOverlapLog(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionOverlapLog");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionJobSize(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionJobSize");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionTargetLength(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionTargetLength");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionMinMatch(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionMinMatch");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionSearchLog(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionSearchLog");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionChainLog(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionChainLog");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionHashLog(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionHashLog");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionWindowLog(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionWindowLog");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setCompressionStrategy(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setCompressionStrategy");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setDecompressionLongMax(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setDecompressionLongMax");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setDecompressionMagicless(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setDecompressionMagicless");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setRefMultipleDDicts(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setRefMultipleDDicts");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setValidateSequences(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setValidateSequences");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setSequenceProducerFallback(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setSequenceProducerFallback");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setSearchForExternalRepcodes(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setSearchForExternalRepcodes");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_setEnableLongDistanceMatching(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_setEnableLongDistanceMatching");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_findFrameCompressedSize0(JNIEnv* env, jclass clazz, jbyteArray arg0, jint arg1, jint arg2) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_findFrameCompressedSize0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_findDirectByteBufferFrameCompressedSize(JNIEnv* env, jclass clazz, jobject arg0, jint arg1, jint arg2) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_findDirectByteBufferFrameCompressedSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getFrameContentSize0(JNIEnv* env, jclass clazz, jbyteArray arg0, jint arg1, jint arg2, jboolean arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jbyteArray, jint, jint, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getFrameContentSize0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_decompressedSize0(JNIEnv* env, jclass clazz, jbyteArray arg0, jint arg1, jint arg2, jboolean arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jbyteArray, jint, jint, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_decompressedSize0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_decompressedDirectByteBufferSize(JNIEnv* env, jclass clazz, jobject arg0, jint arg1, jint arg2, jboolean arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobject, jint, jint, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_decompressedDirectByteBufferSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getDirectByteBufferFrameContentSize(JNIEnv* env, jclass clazz, jobject arg0, jint arg1, jint arg2, jboolean arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobject, jint, jint, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getDirectByteBufferFrameContentSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_compressBound(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_compressBound");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jboolean JNICALL Java_dhcomgithubluben_zstd_Zstd_isError(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jboolean (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_isError");
    if (fn == NULL) return JNI_FALSE;
    return fn(env, clazz, arg0);
}

JNIEXPORT jstring JNICALL Java_dhcomgithubluben_zstd_Zstd_getErrorName(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jstring (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getErrorName");
    if (fn == NULL) return NULL;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getErrorCode(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getErrorCode");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errNoError(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errNoError");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errGeneric(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errGeneric");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errPrefixUnknown(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errPrefixUnknown");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errVersionUnsupported(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errVersionUnsupported");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errFrameParameterUnsupported(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errFrameParameterUnsupported");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errFrameParameterWindowTooLarge(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errFrameParameterWindowTooLarge");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errCorruptionDetected(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errCorruptionDetected");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errChecksumWrong(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errChecksumWrong");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errDictionaryCorrupted(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errDictionaryCorrupted");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errDictionaryWrong(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errDictionaryWrong");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errDictionaryCreationFailed(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errDictionaryCreationFailed");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errParameterUnsupported(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errParameterUnsupported");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errParameterOutOfBound(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errParameterOutOfBound");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errTableLogTooLarge(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errTableLogTooLarge");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errMaxSymbolValueTooLarge(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errMaxSymbolValueTooLarge");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errMaxSymbolValueTooSmall(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errMaxSymbolValueTooSmall");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errStageWrong(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errStageWrong");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errInitMissing(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errInitMissing");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errMemoryAllocation(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errMemoryAllocation");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errWorkSpaceTooSmall(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errWorkSpaceTooSmall");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errDstSizeTooSmall(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errDstSizeTooSmall");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errSrcSizeWrong(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errSrcSizeWrong");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_errDstBufferNull(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_errDstBufferNull");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_trainFromBuffer0(JNIEnv* env, jclass clazz, jobjectArray arg0, jbyteArray arg1, jboolean arg2, jint arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobjectArray, jbyteArray, jboolean, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_trainFromBuffer0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_trainFromBufferDirect0(JNIEnv* env, jclass clazz, jobject arg0, jintArray arg1, jobject arg2, jboolean arg3, jint arg4) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobject, jintArray, jobject, jboolean, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_trainFromBufferDirect0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getDictIdFromFrame(JNIEnv* env, jclass clazz, jbyteArray arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jbyteArray);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getDictIdFromFrame");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getDictIdFromFrameBuffer(JNIEnv* env, jclass clazz, jobject arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getDictIdFromFrameBuffer");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getDictIdFromDict(JNIEnv* env, jclass clazz, jbyteArray arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jbyteArray);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getDictIdFromDict");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_Zstd_getDictIdFromDictDirect(JNIEnv* env, jclass clazz, jobject arg0, jint arg1, jint arg2) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_getDictIdFromDictDirect");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_magicNumber(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_magicNumber");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_windowLogMin(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_windowLogMin");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_windowLogMax(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_windowLogMax");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_chainLogMin(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_chainLogMin");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_chainLogMax(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_chainLogMax");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_hashLogMin(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_hashLogMin");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_hashLogMax(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_hashLogMax");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_searchLogMin(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_searchLogMin");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_searchLogMax(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_searchLogMax");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_searchLengthMin(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_find_symbol("Java_com_github_luben_zstd_Zstd_searchLengthMin");
    if (fn != NULL) return fn(env, clazz);
    return 3;
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_searchLengthMax(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_find_symbol("Java_com_github_luben_zstd_Zstd_searchLengthMax");
    if (fn != NULL) return fn(env, clazz);
    return 7;
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_blockSizeMax(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_blockSizeMax");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_defaultCompressionLevel(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_defaultCompressionLevel");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_minCompressionLevel(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_minCompressionLevel");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_Zstd_maxCompressionLevel(JNIEnv* env, jclass clazz) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_Zstd_maxCompressionLevel");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDictDecompress_init(JNIEnv* env, jobject thiz, jbyteArray arg0, jint arg1, jint arg2) {
    typedef void (*dh_fn_t)(JNIEnv*, jobject, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDictDecompress_init");
    if (fn == NULL) return;
    fn(env, thiz, arg0, arg1, arg2);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDictDecompress_initDirect(JNIEnv* env, jobject thiz, jobject arg0, jint arg1, jint arg2, jint arg3) {
    typedef void (*dh_fn_t)(JNIEnv*, jobject, jobject, jint, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDictDecompress_initDirect");
    if (fn == NULL) return;
    fn(env, thiz, arg0, arg1, arg2, arg3);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDictDecompress_free(JNIEnv* env, jobject thiz) {
    typedef void (*dh_fn_t)(JNIEnv*, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDictDecompress_free");
    if (fn == NULL) return;
    fn(env, thiz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_createDStreamNative(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_createDStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_freeDStreamNative(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_freeDStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_initDStreamNative(JNIEnv* env, jobject thiz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_initDStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_decompressStreamNative(JNIEnv* env, jobject thiz, jlong arg0, jobject arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jobject, jint, jint, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_decompressStreamNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_recommendedDOutSizeNative(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferDecompressingStreamNoFinalizer_recommendedDOutSizeNative");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdInputStreamNoFinalizer_recommendedDInSize(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdInputStreamNoFinalizer_recommendedDInSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdInputStreamNoFinalizer_recommendedDOutSize(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdInputStreamNoFinalizer_recommendedDOutSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdInputStreamNoFinalizer_createDStream(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdInputStreamNoFinalizer_createDStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdInputStreamNoFinalizer_freeDStream(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdInputStreamNoFinalizer_freeDStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdInputStreamNoFinalizer_initDStream(JNIEnv* env, jobject thiz, jlong arg0) {
    typedef jint (*dh_fn_t)(JNIEnv*, jobject, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdInputStreamNoFinalizer_initDStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, thiz, arg0);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdInputStreamNoFinalizer_decompressStream(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1, jint arg2, jbyteArray arg3, jint arg4) {
    typedef jint (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray, jint, jbyteArray, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdInputStreamNoFinalizer_decompressStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3, arg4);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_recommendedCOutSize(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_recommendedCOutSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_createCStream(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_createCStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_freeCStream(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jint (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_freeCStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_resetCStream(JNIEnv* env, jobject thiz, jlong arg0) {
    typedef jint (*dh_fn_t)(JNIEnv*, jobject, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_resetCStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, thiz, arg0);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_compressStream(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1, jint arg2, jbyteArray arg3, jint arg4) {
    typedef jint (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray, jint, jbyteArray, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_compressStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3, arg4);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_flushStream(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1, jint arg2) {
    typedef jint (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_flushStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, thiz, arg0, arg1, arg2);
}

JNIEXPORT jint JNICALL Java_dhcomgithubluben_zstd_ZstdOutputStreamNoFinalizer_endStream(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1, jint arg2) {
    typedef jint (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdOutputStreamNoFinalizer_endStream");
    if (fn == NULL) return (jint) 0;
    return fn(env, thiz, arg0, arg1, arg2);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_recommendedCOutSize(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_recommendedCOutSize");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_createCStream(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_createCStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_freeCStream(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_freeCStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_initCStream(JNIEnv* env, jobject thiz, jlong arg0, jint arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_initCStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_initCStreamWithDict(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1, jint arg2, jint arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_initCStreamWithDict");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_initCStreamWithFastDict(JNIEnv* env, jobject thiz, jlong arg0, jobject arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_initCStreamWithFastDict");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_compressDirectByteBuffer(JNIEnv* env, jobject thiz, jlong arg0, jobject arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jobject, jint, jint, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_compressDirectByteBuffer");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_flushStream(JNIEnv* env, jobject thiz, jlong arg0, jobject arg1, jint arg2, jint arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_flushStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_endStream(JNIEnv* env, jobject thiz, jlong arg0, jobject arg1, jint arg2, jint arg3) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDirectBufferCompressingStreamNoFinalizer_endStream");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1, arg2, arg3);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_init(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_init");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_free(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_free");
    if (fn == NULL) return;
    fn(env, clazz, arg0);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_setLevel0(JNIEnv* env, jclass clazz, jlong arg0, jint arg1) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_setLevel0");
    if (fn == NULL) return;
    fn(env, clazz, arg0, arg1);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_setChecksum0(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_setChecksum0");
    if (fn == NULL) return;
    fn(env, clazz, arg0, arg1);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_setContentSize0(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_setContentSize0");
    if (fn == NULL) return;
    fn(env, clazz, arg0, arg1);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_setDictID0(JNIEnv* env, jclass clazz, jlong arg0, jboolean arg1) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong, jboolean);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_setDictID0");
    if (fn == NULL) return;
    fn(env, clazz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_loadCDictFast0(JNIEnv* env, jobject thiz, jlong arg0, jobject arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_loadCDictFast0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_loadCDict0(JNIEnv* env, jobject thiz, jlong arg0, jbyteArray arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jobject, jlong, jbyteArray);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_loadCDict0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, thiz, arg0, arg1);
}

JNIEXPORT jobject JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_getFrameProgression0(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jobject (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_getFrameProgression0");
    if (fn == NULL) return NULL;
    jobject upstreamFrameProgression = fn(env, clazz, arg0);
    return dh_convert_frame_progression(env, upstreamFrameProgression);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_reset0(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_reset0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_setPledgedSrcSize0(JNIEnv* env, jclass clazz, jlong arg0, jlong arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_setPledgedSrcSize0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_compressDirectByteBufferStream0(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6, jint arg7) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject, jint, jint, jobject, jint, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_compressDirectByteBufferStream0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_compressDirectByteBuffer0(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject, jint, jint, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_compressDirectByteBuffer0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdCompressCtx_compressByteArray0(JNIEnv* env, jclass clazz, jlong arg0, jbyteArray arg1, jint arg2, jint arg3, jbyteArray arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jbyteArray, jint, jint, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdCompressCtx_compressByteArray0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDictCompress_init(JNIEnv* env, jobject thiz, jbyteArray arg0, jint arg1, jint arg2, jint arg3) {
    typedef void (*dh_fn_t)(JNIEnv*, jobject, jbyteArray, jint, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDictCompress_init");
    if (fn == NULL) return;
    fn(env, thiz, arg0, arg1, arg2, arg3);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDictCompress_initDirect(JNIEnv* env, jobject thiz, jobject arg0, jint arg1, jint arg2, jint arg3, jint arg4) {
    typedef void (*dh_fn_t)(JNIEnv*, jobject, jobject, jint, jint, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDictCompress_initDirect");
    if (fn == NULL) return;
    fn(env, thiz, arg0, arg1, arg2, arg3, arg4);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDictCompress_free(JNIEnv* env, jobject thiz) {
    typedef void (*dh_fn_t)(JNIEnv*, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDictCompress_free");
    if (fn == NULL) return;
    fn(env, thiz);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_init(JNIEnv* env, jclass clazz) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_init");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz);
}

JNIEXPORT void JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_free(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef void (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_free");
    if (fn == NULL) return;
    fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_loadDDictFast0(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_loadDDictFast0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_loadDDict0(JNIEnv* env, jclass clazz, jlong arg0, jbyteArray arg1) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jbyteArray);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_loadDDict0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_reset0(JNIEnv* env, jclass clazz, jlong arg0) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_reset0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_decompressDirectByteBufferStream0(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject, jint, jint, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_decompressDirectByteBufferStream0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_decompressDirectByteBuffer0(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject, jint, jint, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_decompressDirectByteBuffer0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_decompressByteArray0(JNIEnv* env, jclass clazz, jlong arg0, jbyteArray arg1, jint arg2, jint arg3, jbyteArray arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jbyteArray, jint, jint, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_decompressByteArray0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_decompressByteArrayToDirectByteBuffer0(JNIEnv* env, jclass clazz, jlong arg0, jobject arg1, jint arg2, jint arg3, jbyteArray arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jobject, jint, jint, jbyteArray, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_decompressByteArrayToDirectByteBuffer0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}

JNIEXPORT jlong JNICALL Java_dhcomgithubluben_zstd_ZstdDecompressCtx_decompressDirectByteBufferToByteArray0(JNIEnv* env, jclass clazz, jlong arg0, jbyteArray arg1, jint arg2, jint arg3, jobject arg4, jint arg5, jint arg6) {
    typedef jlong (*dh_fn_t)(JNIEnv*, jclass, jlong, jbyteArray, jint, jint, jobject, jint, jint);
    static dh_fn_t fn = NULL;
    if (fn == NULL) fn = (dh_fn_t) dh_resolve_symbol(env, "Java_com_github_luben_zstd_ZstdDecompressCtx_decompressDirectByteBufferToByteArray0");
    if (fn == NULL) return (jlong) 0;
    return fn(env, clazz, arg0, arg1, arg2, arg3, arg4, arg5, arg6);
}
