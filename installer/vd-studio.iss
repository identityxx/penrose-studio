; Copyright 2009 Red Hat, Inc.
; All rights reserved.

[Setup]

AppName=${product.title}
AppVerName=${product.title} ${product.version}
DefaultDirName={pf}\${product.group}\${product.title} ${product.version}
DefaultGroupName=${product.group}\${product.title} ${product.version}
UninstallDisplayName=${product.title} ${product.version}
UninstallDisplayIcon={app}\${images.icon}
Compression=zip
SolidCompression=yes
OutputBaseFilename=${product.name}-${product.version}
OutputDir=..\dist
LicenseFile=LICENSE.txt

[Files]

Source: "dist\*"; DestDir: "{app}"; Flags: recursesubdirs;

[Icons]

Name: "{group}\Documentation\README.txt"; Filename: "{app}\README.txt"
Name: "{group}\Documentation\LICENSE.txt"; Filename: "{app}\LICENSE.txt"
Name: "{group}\Documentation\COPYING.txt"; Filename: "{app}\COPYING.txt"
Name: "{group}\Documentation\INSTALL-BINARY.txt"; Filename: "{app}\INSTALL-BINARY.txt"
Name: "{group}\Documentation\Online Documentation"; Filename: "{app}\docs\Online Documentation.url";
Name: "{group}\Documentation\Penrose Website"; Filename: "{app}\docs\Penrose Website.url";
Name: "{group}\Documentation\Safehaus Website"; Filename: "{app}\docs\Safehaus Website.url";
Name: "{group}\${product.title}"; Filename: "{app}\${product.name}.exe"; IconFilename: "{app}\${images.icon}"; WorkingDir: "{app}"
Name: "{group}\Configuration Files"; Filename: "{app}\conf";
Name: "{group}\Uninstall ${product.title}"; Filename: "{uninstallexe}"
