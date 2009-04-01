${product.title}
--------------
Version ${product.version}
Copyright 2009 Red Hat, Inc.

Overview
--------

Penrose is an open source java-based virtual directory server based on Apache Directory server.
A Virtual Directory does not store any information itself, unlike other LDAP implementations.
Requests received from LDAP client applications are processed by the Virtual Directory Server
and passed on to the data source hosting the desired data. Frequently this data source will be
a relational database, and more often than not it will be the authoritative source of the directory
information.

Documentation
-------------

Please find Penrose documentation online at http://penrose.safehaus.org/Documentation.

Getting the Source Code
-----------------------

Checkout the project from:

   svn co https://svn.safehaus.org/repos/penrose-studio/trunk

Building
--------

Building ${product.title} for all platforms:

   ant dist-all

Building ${product.title} for MacOS X platform:

   ant dist-macosx-carbon

Building ${product.title} for Solaris GTK platform:

   ant dist-solaris-gtk

Building ${product.title} for Solaris Motif platform:

   ant dist-solaris-motif

Building ${product.title} for Win32 platform:

   ant dist-win32

Building ${product.title} for Linux platforms:

   ant -Dplatform=<platform> dist-linux

Supported Linux platforms:
 - linux-gtk
 - linux-gtk-ia64
 - linux-gtk-x86_64
 - linux-motif

Building ${product.title} for other Unix platforms:

   ant -Dplatform=<platform> dist-unix

Supported Unix platforms:
 - aix-motif
 - hpux-motif

The distribution files can be found under the dist directory.
