#include <stdlib.h>
#include <pthread.h>

#include <android/looper.h>

#include "logger.h"

#include "event_thread.h"

static void *event_thread_run(void *event_thread);

struct event_thread *event_thread_create(mpv_handle *mpv) {
    struct event_thread *event_thread = malloc(sizeof(struct event_thread));
    pthread_mutex_init(&event_thread->lock, NULL);
    pthread_cond_init(&event_thread->ready, NULL);

    event_thread->running = 0;

    event_thread->mpv = mpv;

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

    mpv_handle *mpv;
    pthread_mutex_lock(&event_thread->lock);
    mpv = event_thread->mpv;
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
        switch (event->event_id) {
            case MPV_EVENT_LOG_MESSAGE: {
                mpv_event_log_message *msg = event->data;
                LOGI("[%s:%s] %s", msg->prefix, msg->level, msg->text);
                break;
            }
            case MPV_EVENT_PROPERTY_CHANGE: {
                mpv_event_property *property = event->data;
                LOGI("Property %s changed", property->name);
                break;
            }
            default: {
                LOGI("Unhandled event %s", mpv_event_name(event->event_id));
                break;
            }
        }

        // loop
    }

    pthread_mutex_lock(&event_thread->lock);

    // Free resources

    pthread_mutex_unlock(&event_thread->lock);

    return NULL;
}
