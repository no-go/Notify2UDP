/* Kompilen:
 *   g++ `pkg-config --cflags glib-2.0 gtk+-2.0` udp2notify.cpp -o udp2notify `pkg-config --libs glib-2.0 gtk+-2.0` -lnotify -Wall
 *
 * client z.B. mit netcat:
 *   nc -u 127.0.0.1 58000
 */
#include <libnotify/notify.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <string>
#include <cstring> // memset
#include <cstdlib>

#define BUFLEN          512
#define SPLITTOKEN      " || "
#define SPLITTOKEN_LEN  4
#define DEFAULT_PORT    58000

int main(int argc, char *argv[]) {
	
	struct sockaddr_in si_me, si_other;
	int port = DEFAULT_PORT;
	if (argc >1) port = std::atoi(argv[1]);
	
	int s;
	socklen_t recv_len;
	unsigned int slen = sizeof(si_other);
	s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	
	memset((char *) &si_me, 0, sizeof(si_me));
	si_me.sin_family = AF_INET;
	si_me.sin_port = htons(port);
	si_me.sin_addr.s_addr = htonl(INADDR_ANY);
	bind(s, (struct sockaddr*)&si_me, sizeof(si_me) );

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
				"dialog-information");
			notify_notification_set_timeout(notify, -1);
			notify_notification_show(notify, NULL);
			g_object_unref(G_OBJECT (notify));
			notify_uninit();
		}
	}
	
	close(s);
	return 0;
}
