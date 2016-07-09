#include <stdlib.h>
#include <pthread.h>

#include <android/looper.h>
#include <jni.h>

#include "logger.h"

#include "player_interface.h"
#include "event_thread.h"
#include "player.h"

static void *event_thread_run(void *event_thread);

struct event_thread *event_thread_create(struct player_context *player_context) {
    struct event_thread *event_thread = malloc(sizeof(struct event_thread));
    pthread_mutex_init(&event_thread->lock, NULL);
    pthread_cond_init(&event_thread->ready, NULL);

    event_thread->running = 0;

    event_thread->player_context = player_context;

    return event_thread;
}

void event_thread_start(struct event_thread *event_thread) {
    pthread_mutex_lock(&event_thread->lock);
    pthread_create(&event_thread->thread, NULL, event_thread_run, event_thread);
    while (!event_thread->running) {
        pthread_cond_wait(&event_thread->ready, &event_thread->lock);
    }
    pthread_mutex_unlock(&event_thread->lock);
}

void event_thread_stop(struct event_thread *event_thread) {
    pthread_mutex_lock(&event_thread->lock);
    event_thread->running = 0;
    pthread_mutex_unlock(&event_thread->lock);
    pthread_join(event_thread->thread, NULL);
}

void event_thread_destroy(struct event_thread *event_thread) {
    pthread_mutex_destroy(&event_thread->lock);
    pthread_cond_destroy(&event_thread->ready);
    free(event_thread);
}

static void *event_thread_run(void *data) {
    struct event_thread *event_thread = data;

    JavaVM *vm = event_thread->player_context->vm;
    JNIEnv *env = NULL;
    (*vm)->AttachCurrentThread(vm, &env, NULL);

    mpv_handle *mpv = event_thread->player_context->mpv;
    jobject core_instance = event_thread->player_context->core_instance;

    pthread_mutex_lock(&event_thread->lock);
    event_thread->running = 1;
    pthread_cond_broadcast(&event_thread->ready);
    pthread_mutex_unlock(&event_thread->lock);

    mpv_event *event;
    while (1) {
        int run;
        pthread_mutex_lock(&event_thread->lock);
        run = event_thread->running;
        pthread_mutex_unlock(&event_thread->lock);
        if (!run) {
            break;
        }

        event = mpv_wait_event(mpv, -1.0);

        // Ignoring all deprecated and async() events
        switch (event->event_id) {
            case MPV_EVENT_SHUTDOWN: {
                // TODO
                break;
            }
            case MPV_EVENT_START_FILE:
            case MPV_EVENT_FILE_LOADED:
            case MPV_EVENT_IDLE:
            case MPV_EVENT_VIDEO_RECONFIG:
            case MPV_EVENT_AUDIO_RECONFIG:
            case MPV_EVENT_SEEK:
            case MPV_EVENT_PLAYBACK_RESTART: {
                const char *event_name = mpv_event_name(event->event_id);
                event_no_data(env, core_instance, event_name);
                break;
            }
            case MPV_EVENT_END_FILE: {
                mpv_event_end_file *end_file = event->data;
                int reason = end_file->reason;
                break;
            }
            case MPV_EVENT_LOG_MESSAGE: {
                mpv_event_log_message *msg = event->data;
                LOGI("[%s:%s] %s", msg->prefix, msg->level, msg->text);
                break;
            }
            case MPV_EVENT_PROPERTY_CHANGE: {
                mpv_event_property *property = event->data;
                uint64_t userdata = event->reply_userdata;

                switch (property->format) {
                    case MPV_FORMAT_NODE: {
                        event_property(env, core_instance, userdata, property);
                        break;
                    }
                    default: {
                        LOGE("Property format %d not handled", property->format);
                        break;
                    }
                }
                break;
            }
            case MPV_EVENT_QUEUE_OVERFLOW: {
                LOGE("MPV_EVENT_QUEUE_OVERFLOW received");
                break;
            }
            default: {
                // Ignore
                break;
            }
        }
    }

    pthread_mutex_lock(&event_thread->lock);

    // Free resources

    pthread_mutex_unlock(&event_thread->lock);

    (*vm)->DetachCurrentThread(vm);

    return NULL;
}
