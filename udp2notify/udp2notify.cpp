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

void daemonProcess(int port) {
	struct sockaddr_in si_me, si_other;
	
	int s;
	socklen_t recv_len;
	unsigned int slen = sizeof(si_other);
	s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	
	memset((char *) &si_me, 0, sizeof(si_me));
	si_me.sin_family = AF_INET;
	si_me.sin_port = htons(port);
	/// @todo make broadcast and deamon optional !! -------------
	si_me.sin_addr.s_addr = htonl(INADDR_BROADCAST);
	//si_me.sin_addr.s_addr = htonl(INADDR_ANY);
	bind(s, (struct sockaddr*)&si_me, sizeof(si_me) );
	
	printf("daemon running\n");

	while(1==1) {
		char buf[BUFLEN] = {0};
		recv_len = recvfrom(
			s, buf, BUFLEN, 0, (struct sockaddr *) 
			&si_other, &slen);
		if (recv_len > 0) {
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
				"dialog-information"
//				"/tmp/dummy/app/src/main/res/mipmap-xhdpi/ic_launcher.png"
			);
			notify_notification_set_timeout(notify, -1);
			notify_notification_show(notify, NULL);
			g_object_unref(G_OBJECT (notify));
			notify_uninit();
		}
	}
	// never reached ?!
	close(s);
	printf("daemon killed ?!\n");
}

int main(int argc, char *argv[]) {
	int port = DEFAULT_PORT;
	if (argc >1) port = std::atoi(argv[1]);
	pid_t pid;

	pid = fork();
	if (pid < 0) {
		return 1;
	}
	if (pid > 0) {
		printf("daemon started (pid = %d)\n", pid);
		return 0;
	}
	daemonProcess(port);
	return 0;
}
