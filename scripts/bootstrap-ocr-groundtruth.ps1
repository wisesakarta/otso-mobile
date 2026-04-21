param(
    [string]$DatasetDir = (Join-Path (Get-Location) "ocrTesting")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $DatasetDir)) {
    throw "Dataset directory not found: $DatasetDir"
}

$images = Get-ChildItem -Path $DatasetDir -File | Where-Object {
    $_.Extension -in @(".jpg", ".jpeg", ".png", ".webp")
}

foreach ($img in $images) {
    $stem = [System.IO.Path]::GetFileNameWithoutExtension($img.Name)
    $gtPath = Join-Path $DatasetDir "$stem.gt.txt"
    if (-not (Test-Path $gtPath)) {
        Set-Content -Path $gtPath -Value "" -NoNewline -Encoding utf8
        Write-Host "[CREATE] $gtPath"
    } else {
        $current = Get-Content -Path $gtPath -Raw -ErrorAction SilentlyContinue
        if ($current -like "*Fill expected text*") {
            Set-Content -Path $gtPath -Value "" -NoNewline -Encoding utf8
            Write-Host "[RESET] $gtPath"
        }
    }
}

Write-Host "[DONE] Ground-truth bootstrap complete."
