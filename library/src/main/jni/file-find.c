#include <stdio.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <malloc.h>
#include "include/file-find.h"
//#include "time.h"

int len = 0;

int scan_dir_only(char *path, int depth) {
    scan_dir(path, NULL, NULL, depth);
}

/*long get_time() {
    time_t rawtime;
    //struct tm *timeinfo;
    time(&rawtime);
    //timeinfo = localtime(&rawtime);
    //long time = timeinfo->tm_gmtoff;
    //rawtime
    //LOGE("The current date time is: %s", asctime(timeinfo));
    //LOGE("The current date time is: %ld", rawtime);
    return rawtime;
}*/

/**
 * 扫描目录
 */
int scan_dir(char *path, char **formats, char **source, int depth) {

    DIR *dir;
    //为了获取某文件夹目录内容
    struct dirent *file;
    struct stat buf;
    char childPath[512];

    memset(childPath, 0, sizeof(childPath));

    if (!(dir = opendir(path))) {
        LOGE("error opendir %s !!!", path);
        return -1;
    }

    int arrarLen = 0;
    char *tem = formats[0];
    while (tem != NULL) {
        arrarLen++;
        tem = formats[arrarLen];
    }

    chdir(path);
    while ((file = readdir(dir)) != NULL) {
        //第一个字符为"."
        if (strncmp(file->d_name, ".", 1) == 0
            || strncmp(file->d_name, "..", 2) == 0)
            continue;
        if (stat(file->d_name, &buf) >= 0) {

            if (!S_ISDIR(buf.st_mode)) {
                //具有读权限
                //RRR 444
                //S_IRUSR | S_IRGRP | S_IROTH
                //是普通文件
                if (!S_ISREG(buf.st_mode))
                    continue;
                if ((buf.st_mode & S_IRUSR) || (buf.st_mode & S_IRGRP)
                    || (buf.st_mode & S_IROTH)) {

                    if (source != NULL) {

                        for (int i = 0; i < arrarLen; i++) {

                            char *ft = formats[i];
                            size_t formatLen = strlen(ft);
                            int fileLen = (int) strlen(file->d_name);

                            if (fileLen >= formatLen) {
                                // '.'最后出现的str
                                char *str = strrchr(file->d_name, '.');
                                if (str != NULL && strstr(str, ft)) {
                                    if (len < 256) {
                                        sprintf(childPath, "%s/%s", path, file->d_name);
                                        int max = (int) (fileLen + 1 + strlen(path));
                                        childPath[max] = '\0';
                                        char *temp = malloc((max + 1) * sizeof(char));
                                        strncpy(temp, childPath, (size_t) max);
                                        *(temp + max) = '\0';
                                        *(source + (len++)) = temp;
                                    }
                                    break;
                                }
                            }
                        }

                    }
                }
            } else {
                if (depth <= 3) {
                    //是目录
                    sprintf(childPath, "%s/%s", path, file->d_name);
                    childPath[strlen(file->d_name) + 1 + strlen(path)] = '\0';
                    scan_dir(childPath, formats, source, depth + 1);
                }
            }
        }
    }

    chdir("..");
    closedir(dir);
    return 0;
}

int search_mp3file(char *path, Filetemp *filetemp) {

    //long time = get_time();
    len = 0;
    char **filename;
    filename = malloc(256 * 256 * sizeof(char));
    // .mp3 .flac
    char **formats = malloc(5 * 2 * sizeof(char));
    *(formats) = ".mp3";
    *(formats + 1) = ".flac";
    *(formats + 2) = NULL;

    scan_dir(path, formats, filename, 0);

    //数据是正确的
    filetemp->filename = filename;
    filetemp->len = len;

    free(formats);

    return 0;
}