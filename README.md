ScriptCS plugin
===============

ScriptCS lets you use C# as a scripting language. More information and tutorials can be found at http://scriptcs.net/

Configuration
=============

1. Provide the path to ScriptCS in Global Configuration section of Jenkins.
2. Add the ScriptCS build step to a job.
3. Provide the path to a ScriptCS .csx file. Alternatively define a script directly in the Custom Script area.
4. Enter any optional arguments. (Note these arguments are provided as is to the script and should be processed appropriately there).

Note: ScriptCS must be installed before using this plugin. Instructions can be found at http://scriptcs.net/

Example
=======

*Global Settings:*

ScriptCS Executable Location: c:\Chocolatey\lib\scriptcs.0.8.1\tools\scriptcs\scriptcs.exe

*Build Settings:*

Arguments: C:\Windows C:\Windows\System32

Custom Script:

foreach (var path in ScriptArgs)
{
	Console.WriteLine("List of files in \"{0}\"", path);
	var files = Directory.GetFiles(path).Take(5);
	foreach (var file in files)
	{
		Console.WriteLine(file);
	}
	Console.WriteLine();
}

*Approximate Expected Console Output:*

Using custom script
Executing command: c:\Chocolatey\lib\scriptcs.0.8.1\tools\scriptcs\scriptcs.exe C:\WINDOWS\TEMP\ScriptCS_6237919950225467577.csx -- C:\Windows C:\Windows\System32
[workspace] $ c:\Chocolatey\lib\scriptcs.0.8.1\tools\scriptcs\scriptcs.exe C:\WINDOWS\TEMP\ScriptCS_6237919950225467577.csx -- C:\Windows C:\Windows\System32
List of files in "C:\Windows"
C:\Windows\bfsvc.exe
C:\Windows\bootstat.dat
C:\Windows\comsetup.log
C:\Windows\diagerr.xml
C:\Windows\diagwrn.xml

List of files in "C:\Windows\System32"
C:\Windows\System32\7B296FB0-376B-497e-B012-9C450E1B7327-5P-0.C7483456-A289-439d-8115-601632D005A0
C:\Windows\System32\7B296FB0-376B-497e-B012-9C450E1B7327-5P-1.C7483456-A289-439d-8115-601632D005A0
C:\Windows\System32\@OpenWithToastLogo.png
C:\Windows\System32\@TileEmpty1x1Image.png
C:\Windows\System32\accessibilitycpl.dll