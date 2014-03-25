#!/bin/bash
set -eu

export CURL_VERSION="7.35.0"
export TOOLCHAIN_VERSION="4.6"
export ANDROID_API_LEVEL="18"

###############################################################################
# Begin armv7 compile
export ARCH_TARGET="armv7-android-linux"
export PLATFORM="arm-linux-androideabi"
export ROOTDIR="/opt/android-ndk-r9d/arm/"
export SYSROOT="${ROOTDIR}/sysroot"
export DROIDTOOLS="${ROOTDIR}/bin/${PLATFORM}"
export DROID_GCC_LIBS="${ROOTDIR}/lib/gcc/${PLATFORM}/4.6/"

export CC=${DROIDTOOLS}-gcc
export LD=${DROIDTOOLS}-ld
export CPP=${DROIDTOOLS}-cpp
export CXX=${DROIDTOOLS}-g++
export AR=${DROIDTOOLS}-ar
export AS=${DROIDTOOLS}-as
export NM=${DROIDTOOLS}-nm
export STRIP=${DROIDTOOLS}-strip
export CXXCPP=${DROIDTOOLS}-cppf
export RANLIB=${DROIDTOOLS}-ranlib

export LDFLAGS="-Os -fPIC -nostdlib -Wl,-rpath-link=${SYSROOT}/usr/lib -L${SYSROOT}/usr/lib -L${DROID_GCC_LIBS} -L${ROOTDIR}/lib"
export LIBS="-lgcc -lc"
export CFLAGS="-Os -pipe -isysroot ${SYSROOT} -I${ROOTDIR}/include"
export CXXFLAGS="-Os -pipe -isysroot ${SYSROOT} -I${ROOTDIR}/include"

# Move to libcurl path and configure
pushd "${1}"
./configure --host=${ARCH_TARGET} --target=${PLATFORM} --prefix=${ROOTDIR} --without-zlib --without-ssl --with-random=/dev/urandom --enable-optimize --enable-nonblocking --disable-ares --disable-ftp --disable-ldap --disable-ldaps --disable-rtsp --disable-dict --disable-telnet --disable-tftp --disable-pop3 --disable-imap --disable-smtp --disable-gopher --disable-sspi --disable-ipv6 --disable-soname-bump --without-polarssl --without-gnutls --without-cyassl --without-axtls --without-libssh2 --disable-manual --disable-verbose

# Fix libtool to not create versioned shared libraries
mv "libtool" "libtool~"
sed "s/library_names_spec=\".*\"/library_names_spec=\"~##~libname~##~{shared_ext}\"/" libtool~ > libtool~1
sed "s/soname_spec=\".*\"/soname_spec=\"~##~{libname}~##~{shared_ext}\"/" libtool~1 > libtool~2
sed "s/~##~/\\\\$/g" libtool~2 > libtool
chmod u+x libtool

# Fix curl.h to compile on linux based systems
mv "include/curl/curl.h" "include/curl/curl.h~"
sed 's/#include <sys\/types.h>/#include <sys\/select.h>\
#include <sys\/types.h>/' include/curl/curl.h~ > include/curl/curl.h

# Make, install, and clean
pushd "lib"
make
make install
popd
pushd "include"
make
make install
popd
make clean

# Return to original directory and install shared libraries
popd
mkdir -p jni/armeabi/
cp "${ROOTDIR}/lib/libcurl.so" jni/armeabi/
mkdir -p jni/armeabi-v7a/
cp "${ROOTDIR}/lib/libcurl.so" jni/armeabi-v7a/

###############################################################################
# Begin armv7 compile
export ARCH_TARGET="x86-android-linux"
export PLATFORM="i686-linux-android"
export ROOTDIR="/opt/android-ndk-r9d/x86/"
export SYSROOT="${ROOTDIR}/sysroot"
export DROIDTOOLS="${ROOTDIR}/bin/${PLATFORM}"
export DROID_GCC_LIBS="${ROOTDIR}/lib/gcc/${PLATFORM}/4.6/"

export CC=${DROIDTOOLS}-gcc
export LD=${DROIDTOOLS}-ld
export CPP=${DROIDTOOLS}-cpp
export CXX=${DROIDTOOLS}-g++
export AR=${DROIDTOOLS}-ar
export AS=${DROIDTOOLS}-as
export NM=${DROIDTOOLS}-nm
export STRIP=${DROIDTOOLS}-strip
export CXXCPP=${DROIDTOOLS}-cppf
export RANLIB=${DROIDTOOLS}-ranlib

export LDFLAGS="-Os -fPIC -nostdlib -Wl,-rpath-link=${SYSROOT}/usr/lib -L${SYSROOT}/usr/lib -L${DROID_GCC_LIBS} -L${ROOTDIR}/lib"
export LIBS="-lgcc -lc"
export CFLAGS="-Os -pipe -isysroot ${SYSROOT} -I${ROOTDIR}/include"
export CXXFLAGS="-Os -pipe -isysroot ${SYSROOT} -I${ROOTDIR}/include"

# Move to libcurl path and configure
pushd "${1}"
./configure --host=${ARCH_TARGET} --target=${PLATFORM} --prefix=${ROOTDIR} --without-zlib --without-ssl --with-random=/dev/urandom --enable-optimize --enable-nonblocking --disable-ares --disable-ftp --disable-ldap --disable-ldaps --disable-rtsp --disable-dict --disable-telnet --disable-tftp --disable-pop3 --disable-imap --disable-smtp --disable-gopher --disable-sspi --disable-ipv6 --disable-soname-bump --without-polarssl --without-gnutls --without-cyassl --without-axtls --without-libssh2 --disable-manual --disable-verbose

# Fix libtool to not create versioned shared libraries
mv "libtool" "libtool~"
sed "s/library_names_spec=\".*\"/library_names_spec=\"~##~libname~##~{shared_ext}\"/" libtool~ > libtool~1
sed "s/soname_spec=\".*\"/soname_spec=\"~##~{libname}~##~{shared_ext}\"/" libtool~1 > libtool~2
sed "s/~##~/\\\\$/g" libtool~2 > libtool
chmod u+x libtool

# Fix curl.h to compile on linux based systems
mv "include/curl/curl.h" "include/curl/curl.h~"
sed 's/#include <sys\/types.h>/#include <sys\/select.h>\
#include <sys\/types.h>/' include/curl/curl.h~ > include/curl/curl.h

# Make, install, and clean
pushd "lib"
make
make install
popd
pushd "include"
make
make install
popd
make clean

# Return to original directory and install shared libraries
popd
mkdir -p jni/x86/
cp "${ROOTDIR}/lib/libcurl.so" jni/x86/
