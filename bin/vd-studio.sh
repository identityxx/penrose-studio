#!/bin/sh

# load system-wide configuration
if [ -f "/etc/vd.conf" ] ; then
  . /etc/vd.conf
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME is not defined."
  exit 1
fi

cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
           fi
           ;;
esac

if [ -z "$VD_STUDIO_HOME" ] ; then

  PRG="$0"
  progname=`basename "$0"`
  saveddir=`pwd`

  dirname_prg=`dirname "$PRG"`
  cd "$dirname_prg"

  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
  done

  VD_STUDIO_HOME=`dirname "$PRG"`/..

  cd "$saveddir"

  VD_STUDIO_HOME=`cd "$VD_STUDIO_HOME" && pwd`
fi

if $cygwin ; then
  [ -n "$VD_STUDIO_HOME" ] &&
    VD_STUDIO_HOME=`cygpath --unix "$VD_STUDIO_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

LOCALLIBPATH="$JAVA_HOME/lib/ext:$JAVA_HOME/jre/lib/ext"

if $cygwin; then
  VD_STUDIO_HOME=`cygpath --windows "$VD_STUDIO_HOME"`
  JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
  LOCALLIBPATH=`cygpath --path --windows "$LOCALLIBPATH"`
fi

cd "$VD_STUDIO_HOME"
mkdir -p "$VD_STUDIO_HOME/logs"

exec "$VD_STUDIO_HOME/vd-studio" "$@"
