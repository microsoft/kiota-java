# Checks that expected files are present & have contents after the publish process to the local cache
param(
  [Parameter(Mandatory=$true)][string] $ArtifactId,
  [Parameter(Mandatory=$true)][string] $Version,
  [Parameter(Mandatory=$true)][string] $OutputDirectory,
  [Parameter()][string] $GroupId = "com.microsoft.kiota",
  [Parameter()][string] $MavenLocalCachePath = "~" + [System.IO.Path]::DirectorySeparatorChar + ".m2" + [System.IO.Path]::DirectorySeparatorChar + "repository"
)

$groupIdPath = $GroupId -replace "\.", [System.IO.Path]::DirectorySeparatorChar
$packagePath = Join-Path -Path $groupIdPath -ChildPath $ArtifactId
$packageFullPath = Join-Path -Path $MavenLocalCachePath -ChildPath $packagePath -AdditionalChildPath $Version

Write-Output "---------------------------------------------------"
Write-Output "Zipping package contents at $packageFullPath"

if(-not (Test-Path -Path $packageFullPath)) {
  Write-Output "Package not found in local cache."
  exit 1
}

$outputFilePath = Join-Path -Path $OutputDirectory -ChildPath "$ArtifactId-$Version.zip"
# removing any existing file
Remove-Item -Path $outputFilePath -ErrorAction SilentlyContinue
# removing any xml files that are not expected in ESRP release
Remove-Item -Path "$packageFullPath\*.xml" -ErrorAction SilentlyContinue -Verbose
Compress-Archive -Path "$packageFullPath\*" -DestinationPath $outputFilePath

exit 0