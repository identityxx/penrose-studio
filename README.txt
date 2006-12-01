Penrose Studio
--------------
Version ${product.version}
Copyright (c) 2000-2006, Identyx Corporation.

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

   svn co https://svn.safehaus.org/repos/penrose-mt/trunk

Building
--------

Building Penrose Studio for all platforms:

   ant dist-all

Building Penrose Studio for MacOS X platform:

   ant dist-macosx-carbon

Building Penrose Studio for Solaris GTK platform:

   ant dist-solaris-gtk

Building Penrose Studio for Solaris Motif platform:

   ant dist-solaris-motif

Building Penrose Studio for Win32 platform:

   ant dist-win32

Building Penrose Studio for Linux platforms:

   ant -Dplatform=<platform> dist-linux

Supported Linux platforms:
 - linux-gtk
 - linux-gtk-ia64
 - linux-gtk-x86_64
 - linux-motif

Building Penrose Studio for other Unix platforms:

   ant -Dplatform=<platform> dist-unix

Supported Unix platforms:
 - aix-motif
 - hpux-motif

The distribution files can be found under the dist directory.
