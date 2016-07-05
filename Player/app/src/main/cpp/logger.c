#include "logger.h"

void log_mpv_error(const char *tag, int error) {
    const char *msg = mpv_error_string(error);
    LOGE("%s: %s", tag, msg);
}
