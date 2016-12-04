
#include <libnotify/notify.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

static GMainLoop *loop;

static void
previous_callback (NotifyNotification *n,
                   const char         *action)
{
        g_assert (action != NULL);
        g_assert (strcmp (action, "media-skip-backward") == 0);

        printf ("You clicked Previous\n");

        notify_notification_close (n, NULL);

        g_main_loop_quit (loop);
}

int
main (int argc, char **argv)
{
        NotifyNotification *n;

        if (!notify_init ("Action Icon Test"))
                exit (1);

        loop = g_main_loop_new (NULL, FALSE);

        n = notify_notification_new ("Music Player",
                                     "Some solid funk",
                                     NULL);

        notify_notification_set_hint (n, "action-icons", g_variant_new_boolean (TRUE));

        notify_notification_add_action (n,
                                        "media-skip-backward",
                                        "Previous",
                                        (NotifyActionCallback) previous_callback,
                                        NULL,
                                        NULL);
                                        
        if (!notify_notification_show (n, NULL)) {
                fprintf (stderr, "failed to send notification\n");
                return 1;
        }

        g_main_loop_run (loop);

        return 0;
}
