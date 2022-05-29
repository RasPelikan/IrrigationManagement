# Irrigation Management

## Build & Run

```sh
$ mvn clean package
$ java -Dxpi4j.library.path=local \
       -Dorg.slf4j.simpleLogger.showDateTime=true \
       -Dorg.slf4j.simpleLogger.dateTimeFormat="YYYYMMdd-hhmmss" \
       -cp "target/libs/*.jar" \
       -jar target/im-2.*.jar \
       im.properties
```

Use [im.sh](./im.sh) for controlled shutdown of Raspberry Pi triggered by the web-interface.

## web-interface

[http://your-ip-address:8080/im/](http://your-ip-address:8080/im/).

## Requirements

### Java 11

There are precompiled Java distributions from Azul call [Zulu](https://www.azul.com/downloads/?package=jdk#download-openjdk).

### Install pigpio

```sh
$ sudo apt-get install pigpio
$ sudo systemctl disable pigpiod
$ pigpiod -v
79
$ sudo reboot
```

*Hint: *Don't run pigpiod daemon otherwise Pi4J won't work.

### Add iomem

```sh
$ cat /boot/cmdline.txt
console=serial0,115200 console=tty1 root=PARTUUID=60fa2f57-02 rootfstype=ext4 elevator=deadline fsck.repair=yes rootwait iomem=relaxed
```

## Configuration

Use file `im.properties` for configuration:

```
admin.port=8080

# Start of first interval
interval.start=2020-05-01

# set irrigator setting every x seconds
keepalive.seconds=10

irrigator.0.type=GPIO
irrigator.0.gpio=23
irrigator.0.gpio-inverse=true

irrigator.1.type=LOG

irrigator.2.type=URL
irrigator.2.url=http://10.0.0.17/switch

cycle.0.start=1352
cycle.0.end=1353
# interval 1 means every day
cycle.0.interval=1
cycle.0.irrigators=0

cycle.1.start=1353
cycle.1.end=1354
cycle.1.interval=1
cycle.1.irrigators=0,1

cycle.2.start=1354
cycle.2.end=1355
cycle.2.interval=1
cycle.2.irrigators=1
```
