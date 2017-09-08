# MusicDiskPlayer

This library provides a rotated circle music player view. You could customize the

vary variables by yourself.

Let's see the result is faster than I explain. :)

# Demo

![MusicDiskPlayer](https://github.com/pokk/MusicDiskPlayer/raw/master/gif/music_disk_player.gif)

# How to use

The simplest way is as below:

```xml
<com.devrapid.musicdiskplayer.RotatedCircleWithIconImageView
    android:id="@+id/rotatedCircleImageView"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:src="@drawable/sample_lady_gaga"/>
```

The img source is a necessary parameter; otherwise, it won't work.

The parameters of this player view are allowed to control as below:

```xml
<com.devrapid.musicdiskplayer.RotatedCircleWithIconImageView
    android:id="@+id/rotatedCircleImageView"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:src="@drawable/sample_lady_gaga"
    app:progress_color="#2D3BFF"
    app:unprogress_color="#AFE2FF"
    app:progress_width="15"
    app:progress="40"
    app:controller_radius="20"
    app:controller_color="#9F9F9F"
    app:unpress_controller_color="#FFFFFF"
    app:end_time="120"
    app:fore_icon="@drawable/play_icon"
    app:running_icon="@drawable/pause_icon"
    app:time_label="true"/>
```

#### The description of the variables.

- progress_width: the width of the progress bar.
- progress: current progress percent. The range is between `0 ~ 100`.
- controller_radius: the radius of the progress controller.
- end_time: due to `progress` is according to `end time`, this should be set together with `progress`. Unit is `Second`.
- fore_icon: the icon will appear when the player is `stop` state.
- running_icon: the icon will appear when the player is `running` state.
- time_label: `true` → show the time label; `false` → hide the time label.

This is also allowed change by programming! 😄

# Using MusicDiskPlayer

## Gradle

It's easy to import it, you just put them into your gradle file.

```gradle
compile 'com.devrapid.jieyi:musicdiskplayer:0.0.4'
```

## Maven

```maven
<dependency>
  <groupId>com.devrapid.jieyi</groupId>
  <artifactId>musicdiskplayer</artifactId>
  <version>0.0.4</version>
  <type>pom</type>
</dependency>
```

# Feature

# License

```
Copyright (C) 2017 Jieyi Wu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific
language governing permissions and limitations under the License.
```
