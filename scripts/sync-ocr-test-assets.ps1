param(
    [string]$SourceDir = (Join-Path (Get-Location) "ocrTesting"),
    [string]$TargetDir = (Join-Path (Get-Location) "app\src\androidTest\assets\ocrTesting")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $SourceDir)) {
    throw "Source directory not found: $SourceDir"
}

New-Item -ItemType Directory -Force $TargetDir | Out-Null

$files = Get-ChildItem -Path $SourceDir -File -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -match '\.(jpg|jpeg|png|webp)$' -or $_.Name -match '\.gt\.txt$'
}
foreach ($file in $files) {
    Copy-Item $file.FullName (Join-Path $TargetDir $file.Name) -Force
}

Write-Host "[DONE] OCR test assets synced to $TargetDir"
