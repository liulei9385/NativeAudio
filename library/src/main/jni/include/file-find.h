#include <android/log.h>

#define uchar unsigned char
#define ushort unsigned short

typedef struct FileTemp {
    char **filename;
    int len;
} Filetemp;

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "native_audio", __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "native_audio", __VA_ARGS__)

int scan_dir(char *path, char *format, char **source, int depth);

int scan_dir_only(char *path, int depth);

int search_mp3file(char *path, Filetemp *filetemp);