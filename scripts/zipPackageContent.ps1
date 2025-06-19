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

$outputFilePath = Join-Path -Path $OutputDirectory -ChildPath "$ArtifactId-$Version.zip")
Remove-Item -Path $outputFilePath -ErrorAction SilentlyContinue
Compress-Archive -Path "$packageFullPath\*" -DestinationPath $outputFilePath

exit 0