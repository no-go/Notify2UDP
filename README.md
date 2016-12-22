# Notify2UDP

**send notifications to GNOME, KDE, Unity or xfce**

Grab Notifications and send them PLAIN via WiFi to a device (IP or Broadcast and Port) in your LAN.
A small daemon get them and send them to GNOME, KDE, Unity or xfce.

## App Icon

![The App Icon](app/src/main/res/mipmap-xhdpi/ic_launcher.png)

## Screenshot (old)

![Screenshot](photo_v01.jpg)

## Download

Signed Apk: [Notify to UDP](https://raw.githubusercontent.com/no-go/Notify2UDP/master/app/app-release.apk)

UDP 2 Notify for your Linux System: [here](tree/master/udp2notify)

## Modes (udp2notify binary)

- as daemon (default)
- listen to broadcast (default)
- listen on Port 58000 (default)

non Daemon and listen to 65000:

    ./udp2notify 65000 -d

listen to 65000 and non broadcast UDP packages:

    ./udp2notify 65000 -b
