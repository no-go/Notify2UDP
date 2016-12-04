#include <libnotify/notify.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <string>
#include <cstring> // memset
#include <cstdlib>
#include <cstdio>

#define BUFLEN          512
#define SPLITTOKEN      " || "
#define SPLITTOKEN_LEN  4
#define DEFAULT_PORT    58000

/*
static GMainLoop *loop;

static void ExampleAct(NotifyNotification *n, const char * action) {
    g_assert(action != NULL);
    printf("You clicked.\n");
    notify_notification_close(n, NULL);
    g_main_loop_quit(loop);
}

void exampleAddAction(NotifyNotification *notify) {
    bool accepts_actions = false;
    GList * capabilities = notify_get_server_caps();
    GList * c;
    if(capabilities != NULL) {
        for(c = capabilities; c != NULL; c = c->next) {
            if(std::strcmp((char*)c->data, "actions") == 0 ) {
                accepts_actions = true;
                break;
            }
        }
        g_list_foreach(capabilities, (GFunc) g_free, NULL);
        g_list_free(capabilities);
    }

    if(accepts_actions) {
        notify_notification_add_action(
            notify, "media-skip-backward", "Previous", (NotifyActionCallback) ExampleAct, NULL, NULL
        );
    }
}
*/

void daemonProcess(int port, bool broad) {
	struct sockaddr_in si_me, si_other;
	
	int s;
	socklen_t recv_len;
	unsigned int slen = sizeof(si_other);
	s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    if (s < 0) {
        printf("ERROR opening socket\n");
        return;
    }
	memset((char *) &si_me, 0, sizeof(si_me));
	si_me.sin_family = AF_INET;
	si_me.sin_port = htons(port);
	if (broad) {
	    si_me.sin_addr.s_addr = htonl(INADDR_BROADCAST);
	} else {
	    si_me.sin_addr.s_addr = htonl(INADDR_ANY);
	}

	if (bind(s, (struct sockaddr*)&si_me, sizeof(si_me)) < 0) {
	    printf("ERROR on binding\n");
	    return;
	}
	
	printf("listening ...\n");

	while(1==1) {
		char buf[BUFLEN] = {0};
		recv_len = recvfrom(
			s, buf, BUFLEN, 0, (struct sockaddr *) 
			&si_other, &slen
		);
		if (recv_len > 0) {
			//loop = g_main_loop_new(NULL, false);
			NotifyNotification *notify;
			notify_init("UDP to Notify");
			std::string title_msg = buf;
			std::string title = "";
			std::string msg = "";
			title = title_msg.substr(0, title_msg.find(SPLITTOKEN));
			msg = title_msg.substr(title_msg.find(SPLITTOKEN)+SPLITTOKEN_LEN);
			notify = notify_notification_new(
				title.c_str(),
				msg.c_str(),
				NULL
//				"dialog-information"
//				"/tmp/dummy/app/src/main/res/mipmap-xhdpi/ic_launcher.png"
			);
			//exampleAddAction(notify);
			notify_notification_set_timeout(notify, 50000); // -1 for ever?!
			notify_notification_show(notify, NULL);
			g_object_unref(G_OBJECT (notify));
			notify_uninit();
			//g_main_loop_run(loop);
		}
	}
	// never reached ?!
	close(s);
	printf("daemon killed ?!\n");
}

int main(int argc, char *argv[]) {
	int port = DEFAULT_PORT;
	bool broad = true;
	bool nonDaemon = false;

	if (argc > 1) {
	    printf("Hint Usages\n===========\n"
	        "on default port, listen on broadcast as daemon: %s\n"
	        "other port, listen on broadcast as daemon:      %s [port]\n"
	        "other port, listen on broadcast and NON-daemon: %s [port] -d\n"
	        "other port, listen on IP and NON-daemon:        %s [port] -d -b\n"
	        "                                                %s [port] -b -d\n"
	        "other port, listen on IP and as daemon:         %s [port] -b\n\n",
	        argv[0],argv[0],argv[0],argv[0],argv[0],argv[0]
	    );
	    port = std::atoi(argv[1]);
	    if (argc > 2) {
	        std::string op1 = argv[2];
	        if (op1 == (std::string) "-d") nonDaemon = true;
	        if (op1 == (std::string) "-b") broad = false;
	    }
	    if (argc > 3) {
	        std::string op1 = argv[3];
	        if (op1 == (std::string) "-d") nonDaemon = true;
	        if (op1 == (std::string) "-b") broad = false;
	    }
	}

	if (broad == false) {
	    printf("=> listen on IP and not Broadcast Packages\n");
    }

	if (nonDaemon) {
	    printf("=> running as non-daemon\n");
	    daemonProcess(port, broad);
	    return 0;
	}

	pid_t pid;
	pid = fork();
	if (pid < 0) {
		return 1;
	}
	if (pid > 0) {
		printf("=> daemon started (pid = %d)\n", pid);
		return 0;
	}
	daemonProcess(port, broad);
	return 0;
}
