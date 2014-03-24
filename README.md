cpuminer-android
================

An Android application wrapper for Pooler's CPUMiner

Compiling libcurl
================

To compile libcurl, you first need a standalone Android NDK toolchain. This
can be built using the make-standalone-toolchain.sh script in build/tools.
Here I'm going to assume we're putting the standalone toolchain in /opt/.

	# Setup destination directory
	sudo mkdir -p /opt/android-ndk-r9d

	# Get Android NDK
	curl -O http://dl.google.com/android/ndk/android-ndk-r9d-darwin-x86_64.tar.bz2
	tar -xvf android-ndk-r9d-darwin-x86_64.tar.bz2
	cd android-ndk-r9d

	# Build standalone NDK
	cd build/tools
	sudo ./make-standalone-toolchain.sh --ndk-dir=../../ --install-dir=/opt/android-ndk-r9d/ --verbose --system=darwin-x86_64

Once you have the standalone NDK toolchain, you need to download and extract
the source for libcurl.

	# Get libcurl source
	curl -O http://curl.haxx.se/download/curl-7.35.0.tar.bz2
	tar -xvf curl-7.35.0.tar.bz2

The last step is to use the included build-libcurl.sh script to compile libcurl
and move the compiled library into the project.

	./build-libcurl.sh [path to libcurl]
