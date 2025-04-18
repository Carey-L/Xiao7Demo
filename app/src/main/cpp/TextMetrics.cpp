#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "TextMetrics"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static JavaVM* g_javaVM = nullptr;
static pthread_mutex_t g_envMutex = PTHREAD_MUTEX_INITIALIZER;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_javaVM = vm;
    return JNI_VERSION_1_6;
}

// 获取当前线程的 JNIEnv
JNIEnv* GetJNIEnv() {
    JNIEnv* env = nullptr;
    pthread_mutex_lock(&g_envMutex);

    if (g_javaVM) {
        int status = g_javaVM->GetEnv((void**)&env, JNI_VERSION_1_6);
        if (status == JNI_EDETACHED) {
            // 如果是 Native 线程，需要附加到 JVM
            if (g_javaVM->AttachCurrentThread(&env, nullptr) != JNI_OK) {
                LOGE("Failed to attach thread to JVM");
                env = nullptr;
            }
        } else if (status != JNI_OK) {
            LOGE("Failed to get JNIEnv");
            env = nullptr;
        }
    }

    pthread_mutex_unlock(&g_envMutex);
    return env;
}

bool assetExists(AAssetManager *mAssetManager, const char *filename) {
    AAsset *asset = AAssetManager_open(mAssetManager, filename, AASSET_MODE_UNKNOWN);
    if (asset) {
        AAsset_close(asset);
        return true;
    }
    return false;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_sotest_TextMetrics_getTextWidth(
        JNIEnv *env,
        jclass clazz,
        jstring text,
        jfloat textSize) {

    env = GetJNIEnv();
    if (env == nullptr) {
        LOGE("Failed to get JNIEnv");
        return;
    }

    // 获取 Paint 类
    jclass paintClass = env->FindClass("android/graphics/Paint");
    if (paintClass == nullptr) {
        LOGE("Paint class not found");
        return;
    }

    // 创建 Paint 对象
    jmethodID paintConstructor = env->GetMethodID(paintClass, "<init>", "()V");
    jobject paint = env->NewObject(paintClass, paintConstructor);

    // 设置文本大小
    jmethodID setTextSize = env->GetMethodID(paintClass, "setTextSize", "(F)V");
    env->CallVoidMethod(paint, setTextSize, textSize);

    // 设置默认字体
    jclass typefaceClass = env->FindClass("android/graphics/Typeface");
    jmethodID defaultMethod = env->GetStaticMethodID(typefaceClass, "defaultFromStyle",
                                                     "(I)Landroid/graphics/Typeface;");

    jobject typefaceObj = env->CallStaticObjectMethod(typefaceClass, defaultMethod, 0);;
    jmethodID setTypeface = env->GetMethodID(paintClass, "setTypeface",
                                             "(Landroid/graphics/Typeface;)Landroid/graphics/Typeface;");
    env->CallObjectMethod(paint, setTypeface, typefaceObj);

    // 测量文本宽度
    jmethodID measureText = env->GetMethodID(paintClass, "measureText", "(Ljava/lang/String;)F");
    jfloat width = env->CallFloatMethod(paint, measureText, text);

    // 获取字体度量
    jclass paintMetricsClass = env->FindClass("android/graphics/Paint$FontMetrics");
    jmethodID getFontMetrics = env->GetMethodID(paintClass, "getFontMetrics",
                                                "()Landroid/graphics/Paint$FontMetrics;");
    jobject fontMetrics = env->CallObjectMethod(paint, getFontMetrics);

    // 获取 FontMetrics 字段
    jfieldID topField = env->GetFieldID(paintMetricsClass, "top", "F");
    jfieldID bottomField = env->GetFieldID(paintMetricsClass, "bottom", "F");

    jfloat top = env->GetFloatField(fontMetrics, topField);
    jfloat bottom = env->GetFloatField(fontMetrics, bottomField);
    jfloat height = bottom - top;

    auto textStr = env->GetStringUTFChars(text, nullptr);
    LOGD("lws--test method1 文本：%s，字号：%.2fpx，宽度：%.2f，高度：%.2f", textStr, textSize,
         width, height);
    env->ReleaseStringUTFChars(text, textStr);

    // 释放资源
    env->DeleteLocalRef(paintClass);
    env->DeleteLocalRef(paint);
    env->DeleteLocalRef(typefaceClass);
    env->DeleteLocalRef(typefaceObj);
    env->DeleteLocalRef(fontMetrics);
    env->DeleteLocalRef(paintMetricsClass);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_sotest_TextMetrics_getTextWidthWithFont(
        JNIEnv *env,
        jclass clazz,
        jobject context,
        jstring text,
        jfloat textSize,
        jstring fontPath) {

    env = GetJNIEnv();
    if (env == nullptr) {
        LOGE("Failed to get JNIEnv");
        return;
    }

    // 1. 获取 AssetManager
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getAssetsMethod = env->GetMethodID(contextClass, "getAssets",
                                                 "()Landroid/content/res/AssetManager;");
    jobject assetManager = env->CallObjectMethod(context, getAssetsMethod);
    env->DeleteLocalRef(contextClass);
    if (assetManager == nullptr) {
        LOGE("Failed to get AssetManager");
        return;
    }

    const char *fontPathStr = env->GetStringUTFChars(fontPath, nullptr);
    jobject typeface = nullptr;
    // 检查字体文件是否存在
    if (assetExists(AAssetManager_fromJava(env, assetManager), fontPathStr)) {
        // 2. 创建 Typeface
        jclass typefaceClass = env->FindClass("android/graphics/Typeface");
        jmethodID createFromAssetMethod = env->GetStaticMethodID(
                typefaceClass,
                "createFromAsset",
                "(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;");

        typeface = env->CallStaticObjectMethod(
                typefaceClass,
                createFromAssetMethod,
                assetManager,
                fontPath);

        env->DeleteLocalRef(typefaceClass);
        if (typeface == nullptr) {
            LOGE("Failed to create Typeface from asset");
            return;
        }
    } else {
        LOGE("Font file does not exist: %s", fontPathStr);
    }
    env->ReleaseStringUTFChars(fontPath, fontPathStr);

    // 3. 创建并配置 Paint
    jclass paintClass = env->FindClass("android/graphics/Paint");
    jmethodID paintConstructor = env->GetMethodID(paintClass, "<init>", "()V");
    jobject paint = env->NewObject(paintClass, paintConstructor);

    // 设置文本大小
    jmethodID setTextSize = env->GetMethodID(paintClass, "setTextSize", "(F)V");
    env->CallVoidMethod(paint, setTextSize, textSize);

    // 设置字体
    jmethodID setTypeface = env->GetMethodID(paintClass, "setTypeface",
                                             "(Landroid/graphics/Typeface;)Landroid/graphics/Typeface;");
    env->CallObjectMethod(paint, setTypeface, typeface);

    // 4. 测量文本宽高
    jmethodID measureText = env->GetMethodID(paintClass, "measureText", "(Ljava/lang/String;)F");
    jfloat width = env->CallFloatMethod(paint, measureText, text);

    // 获取字体度量
    jclass paintMetricsClass = env->FindClass("android/graphics/Paint$FontMetrics");
    jmethodID getFontMetrics = env->GetMethodID(paintClass, "getFontMetrics",
                                                "()Landroid/graphics/Paint$FontMetrics;");
    jobject fontMetrics = env->CallObjectMethod(paint, getFontMetrics);

    // 获取 FontMetrics 字段
    jfieldID topField = env->GetFieldID(paintMetricsClass, "top", "F");
    jfieldID bottomField = env->GetFieldID(paintMetricsClass, "bottom", "F");

    jfloat top = env->GetFloatField(fontMetrics, topField);
    jfloat bottom = env->GetFloatField(fontMetrics, bottomField);
    jfloat height = bottom - top;

    // 5. 日志输出
    auto textStr = env->GetStringUTFChars(text, nullptr);
    LOGD("lws--test method1 文本：%s，字号：%.2fpx，宽度：%.2f，高度：%.2f", textStr, textSize, width,
         height);
    env->ReleaseStringUTFChars(text, textStr);

    // 6. 释放资源
    env->DeleteLocalRef(paintClass);
    env->DeleteLocalRef(paint);
    env->DeleteLocalRef(typeface);
    env->DeleteLocalRef(fontMetrics);
    env->DeleteLocalRef(paintMetricsClass);
    env->DeleteLocalRef(assetManager);
    env->DeleteLocalRef(context);
}