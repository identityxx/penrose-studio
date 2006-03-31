; Copyright (c) 2000-2005, Identyx Corporation.
; All rights reserved.

[Setup]

AppName=Penrose Studio
AppVerName=Penrose Studio ${project.version}
DefaultDirName={pf}\Penrose Studio
DefaultGroupName=Penrose Studio
UninstallDisplayName=Penrose Studio
UninstallDisplayIcon={app}\penrose.ico
;Compression=lzma
Compression=zip
SolidCompression=yes
OutputBaseFilename=penrose-studio-${project.version}
OutputDir=..\dist
LicenseFile=..\LICENSE.txt

[Files]

Source: "penrose-studio-${project.version}-win32\*"; DestDir: "{app}"; Flags: recursesubdirs;

[Icons]

Name: "{group}\Documentation\README.txt"; Filename: "{app}\README.txt"
Name: "{group}\Documentation\LICENSE.txt"; Filename: "{app}\LICENSE.txt"
Name: "{group}\Documentation\COPYING.txt"; Filename: "{app}\COPYING.txt"
Name: "{group}\Documentation\INSTALL-BINARY.txt"; Filename: "{app}\INSTALL-BINARY.txt"
Name: "{group}\Documentation\Online Documentation"; Filename: "{app}\docs\Online Documentation.url";
Name: "{group}\Documentation\Penrose Website"; Filename: "{app}\docs\Penrose Website.url";
Name: "{group}\Documentation\Safehaus Website"; Filename: "{app}\docs\Safehaus Website.url";
Name: "{group}\Penrose Studio"; Filename: "{app}\penrose-studio.exe"; IconFilename: "{app}\penrose.ico"; WorkingDir: "{app}"
Name: "{group}\Configuration Files"; Filename: "{app}\conf";
Name: "{group}\Uninstall Penrose Studio"; Filename: "{uninstallexe}"
