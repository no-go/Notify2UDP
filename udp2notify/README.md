# UDP to Notify

This tool listen on a port for UDP packages and sends them to your
Linux Desktop Enviroment like GNOME, Unity, KDE Plasma or xfce.
It is similar to the command line tool `notify-send`.

The bytes of the UDP packet must be:

    Title message || Body content

Compile with:

    g++ `pkg-config --cflags glib-2.0 gtk+-2.0` \
    udp2notify.cpp -o udp2notify `pkg-config --libs glib-2.0 gtk+-2.0` \
    -lnotify -Wall

Start with:

    ./udp2notify

Or with a different port then 58000 (firewall? is it open?):

    ./udp2notify 64001

To stop or kill the `udp2notify` daemon:

    killall udp2notify

You can test sending UDP packages with:

    netcat -u 127.0.0.1 58000

Because Netcat by be broken in that point, use socat for Broadcast:

    socat - UDP-DATAGRAM:255.255.255.255:58000,broadcast
