#!/bin/bash -ex
# NB: This is the *server* version, which is not to be confused with the client library version.
# The important compatibility point is the *protocol* version, which hasn't changed in ages.
VERSION=10.6-1

RSRC_DIR=$PWD/target/generated-resources
EXT_DIR=$PWD/ext
EXT_UNPACKED_DIR=$PWD/target/ext

[ -e $RSRC_DIR/.repacked ] && echo "Already repacked, skipping..." && exit 0

cd `dirname $0`

PACKDIR=$(mktemp -d -t wat.XXXXXX)
LINUX_DIST=dist/postgresql-$VERSION-linux-x64-binaries.tar.gz
OSX_DIST=dist/postgresql-$VERSION-osx-binaries.zip
WINDOWS_DIST=dist/postgresql-$VERSION-win-binaries.zip

mkdir -p dist/ target/generated-resources/
[ -e $LINUX_DIST ] || wget -O $LINUX_DIST "http://get.enterprisedb.com/postgresql/postgresql-$VERSION-linux-x64-binaries.tar.gz"
[ -e $OSX_DIST ] || wget -O $OSX_DIST "http://get.enterprisedb.com/postgresql/postgresql-$VERSION-osx-binaries.zip"
[ -e $WINDOWS_DIST ] || wget -O $WINDOWS_DIST "http://get.enterprisedb.com/postgresql/postgresql-$VERSION-windows-x64-binaries.zip"

mkdir -p $EXT_UNPACKED_DIR/share/postgresql/tsearch_data
unzip $EXT_DIR/ru-dict.zip -d $EXT_UNPACKED_DIR/share/postgresql/tsearch_data

tar xzf $LINUX_DIST -C $PACKDIR
cp -f $EXT_UNPACKED_DIR/share/postgresql/tsearch_data/* $PACKDIR/pgsql/share/postgresql/tsearch_data
pushd $PACKDIR/pgsql
tar cJf $RSRC_DIR/postgresql-Linux-x86_64.txz \
  share/postgresql \
  lib \
  bin/initdb \
  bin/pg_ctl \
  bin/postgres
popd

rm -fr $PACKDIR && mkdir -p $PACKDIR

unzip -q -d $PACKDIR $OSX_DIST
cp -f $EXT_UNPACKED_DIR/share/postgresql/tsearch_data/* $PACKDIR/pgsql/share/postgresql/tsearch_data
pushd $PACKDIR/pgsql
tar cJf $RSRC_DIR/postgresql-Darwin-x86_64.txz \
  share/postgresql \
  lib/libicudata.57.dylib \
  lib/libicui18n.57.dylib \
  lib/libicuuc.57.dylib \
  lib/libxml2.2.dylib \
  lib/libssl.1.0.0.dylib \
  lib/libcrypto.1.0.0.dylib \
  lib/libuuid.1.1.dylib \
  lib/postgresql/*.so \
  bin/initdb \
  bin/pg_ctl \
  bin/postgres
popd

rm -fr $PACKDIR && mkdir -p $PACKDIR

unzip -q -d $PACKDIR $WINDOWS_DIST
cp -f $EXT_UNPACKED_DIR/share/postgresql/tsearch_data/* $PACKDIR/pgsql/share/tsearch_data
pushd $PACKDIR/pgsql
tar cJf $RSRC_DIR/postgresql-Windows-x86_64.txz \
  share \
  lib/iconv.lib \
  lib/libxml2.lib \
  lib/ssleay32.lib \
  lib/ssleay32MD.lib \
  lib/*.dll \
  bin/initdb.exe \
  bin/pg_ctl.exe \
  bin/postgres.exe \
  bin/*.dll
popd

rm -rf $EXT_UNPACKED_DIR
rm -rf $PACKDIR
touch $RSRC_DIR/.repacked
