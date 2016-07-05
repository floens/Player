#pragma once

#include <stdbool.h>
#include <pthread.h>

#include <mpv/client.h>

struct event_thread {
    pthread_t thread;

    pthread_mutex_t lock;
    pthread_cond_t ready;

    int running;

    mpv_handle *mpv;
};

struct event_thread *event_thread_create(mpv_handle *mpv);

void event_thread_start(struct event_thread *event_thread);

void event_thread_stop(struct event_thread *event_thread);

void event_thread_destroy(struct event_thread *event_thread);
