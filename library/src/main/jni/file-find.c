#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include "include/file-find.h"
#include "time.h"

int len = 0;

int scan_dir_only(char *path, int depth) {
    scan_dir(path, NULL, NULL, depth);
}

long get_time() {
    time_t rawtime;
    //struct tm *timeinfo;
    time(&rawtime);
    //timeinfo = localtime(&rawtime);
    //long time = timeinfo->tm_gmtoff;
    //rawtime
    //LOGE("The current date time is: %s", asctime(timeinfo));
    //LOGE("The current date time is: %ld", rawtime);
    return rawtime;
}

/**
 * 扫描目录
 */
int scan_dir(char *path, char *format, char **source, int depth) {

    DIR *dir;
    //为了获取某文件夹目录内容
    struct dirent *file;
    struct stat buf;
    char childpath[512];

    memset(childpath, 0, sizeof(childpath));

    //LOGE("path=%s", path);

    if (!(dir = opendir(path))) {
        LOGE("error opendir %s !!!", path);
        return -1;
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
                    if (format != NULL && source != NULL) {
                        char *last = malloc(4 * sizeof(char));
                        int fileLen = strlen(file->d_name);
                        if (fileLen >= 4) {
                            //"a.mp3" ".mp3"
                            strncpy(last, (file->d_name) + (fileLen - 4), 4);
                            last[4] = '\0';
                            if (strstr(last, format)) {
                                if (len < 256) {
                                    sprintf(childpath, "%s/%s", path, file->d_name);
                                    int max = fileLen + 1 + strlen(path);
                                    childpath[max] = '\0';
                                    char *temp = malloc(max * sizeof(char));
                                    strcpy(temp, childpath);

                                    *(source + (len++)) = temp;
                                    //LOGE("WE１ %s,size=%d", *(source + (len - 1)), max);
                                    //TODO 要好好查看下错误
                                    //strcpy(*(source), file->d_name);
                                }
                            }
                        }
                        free(last);
                    }
                }
            } else {
                if (depth <= 3) {
                    //是目录
                    sprintf(childpath, "%s/%s", path, file->d_name);
                    childpath[strlen(file->d_name) + 1 + strlen(path)] = '\0';
                    scan_dir(childpath, format, source, depth + 1);
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
    scan_dir(path, ".mp3", filename, 0);
    //LOGE("end search_mp3file");
    Filetemp *fileTemp;
    fileTemp = (Filetemp *) malloc(sizeof(Filetemp));
    fileTemp->filename = filename;
    fileTemp->len = len;
    //数据是正确的
    filetemp->filename = filename;
    filetemp->len = len;
    //LOGE("spend time= %ld", get_time() - time);
    return 0;
}