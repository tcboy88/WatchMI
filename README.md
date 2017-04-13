# WatchMI
WatchMI: Pressure Touch, Twist and Pan Gesture Input on Unmodified Smartwatches, published at MobileHCI 16 with Honorable Mention award.

Watch the demo video here: https://www.youtube.com/watch?v=74roE_cyafk and the paper here: http://dl.acm.org/citation.cfm?id=2935375

We present WatchMI: Watch Movement Input that enhances touch interaction on a smartwatch to support continuous pressure touch, twist, pan gestures and their combinations. Our novel approach relies on software that analyzes, in real-time, the data from a built-in Inertial Measurement Unit (IMU) in order to determine with great accuracy and different levels of granularity the actions performed by the user, without requiring additional hardware or modification of the watch.

This is a simplified version from the paper, the algorithm is simpler but should work equally well.
Not all sample apps from the paper are included in this version.
This app has 3 parts: i) just trying out the 3 techniques ii) user evaluation by performing some targeting trials and iii) demo applications.

Get it from playstore here: https://play.google.com/store/apps/details?id=hsyeo.watchmi1
Install to your phone and wait for it to sync with your smartwatch.
(In some rare occasion it didn't sync then get the wear APK from here and sideload it to your watch.)

Mobile APK with embedded wear APK (for Wear 1.x) https://www.dropbox.com/s/4mh8l85zhdrvpvy/mobile-release.apk?dl=0
Wear APK only (need to sideload it) https://www.dropbox.com/s/31ea9m3k3qimlmq/wear-release.apk?dl=0

Currently it doesn't work well on watch without magnetometer such as Huawei watch v1.

I will also update to Android Wear 2.0 once LG watch sport is release in the UK.

In the future, I also plan to include another paper Sidetap & Slingshot Gestures on Unmodified Smartwatches [UIST2016 Best Poster Award]   https://www.youtube.com/watch?v=3Zc5Yi5C5vU
