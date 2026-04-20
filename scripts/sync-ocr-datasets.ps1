param(
    [string]$SourceDir = (Join-Path (Resolve-Path (Join-Path $PSScriptRoot "..")) "ocrTesting"),
    [string]$TargetDir = "/sdcard/Pictures/OtsoOCR",
    [string]$Serial,
    [switch]$Watch,
    [switch]$DeleteMissing,
    [int]$PollSeconds = 2
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-DeviceSerial {
    param([string]$Preferred)

    $devices = @(adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\tdevice$" } | ForEach-Object { ($_ -split "`t")[0].Trim() })

    if ($Preferred) {
        if ($devices -contains $Preferred) { return $Preferred }
        throw "Requested device '$Preferred' is not connected. Connected devices: $($devices -join ', ')"
    }

    if ($devices.Count -eq 0) { throw "No adb device/emulator connected." }
    return $devices[0]
}

function Get-ImageFiles {
    param([string]$Dir)

    if (-not (Test-Path $Dir)) {
        throw "Source directory not found: $Dir"
    }

    $ext = @("*.jpg", "*.jpeg", "*.png", "*.webp")
    $all = @()
    foreach ($pattern in $ext) {
        $all += Get-ChildItem -Path $Dir -File -Filter $pattern -ErrorAction SilentlyContinue
    }
    return $all | Sort-Object Name -Unique
}

function Get-FileState {
    param([System.IO.FileInfo[]]$Files)

    $map = @{}
    foreach ($f in $Files) {
        $map[$f.Name] = "{0}|{1}" -f $f.Length, $f.LastWriteTimeUtc.Ticks
    }
    return $map
}

function Ensure-TargetDir {
    param([string]$Device, [string]$RemoteDir)

    adb -s $Device shell "mkdir -p '$RemoteDir'" | Out-Null
}

function Push-File {
    param([string]$Device, [System.IO.FileInfo]$File, [string]$RemoteDir)

    $remotePath = "$RemoteDir/$($File.Name)"
    Write-Host "[SYNC] $($File.Name) -> $remotePath"
    adb -s $Device push "$($File.FullName)" "$remotePath" | Out-Null
    adb -s $Device shell "am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d 'file://$remotePath'" | Out-Null
}

function Remove-RemoteFile {
    param([string]$Device, [string]$Name, [string]$RemoteDir)

    $remotePath = "$RemoteDir/$Name"
    Write-Host "[DELETE] $remotePath"
    adb -s $Device shell "rm -f '$remotePath'" | Out-Null
}

function Sync-All {
    param([string]$Device, [string]$LocalDir, [string]$RemoteDir, [switch]$DeleteRemoteMissing)

    Ensure-TargetDir -Device $Device -RemoteDir $RemoteDir
    $files = Get-ImageFiles -Dir $LocalDir

    foreach ($file in $files) {
        Push-File -Device $Device -File $file -RemoteDir $RemoteDir
    }

    if ($DeleteRemoteMissing) {
        $remoteNames = @(adb -s $Device shell "ls -1 '$RemoteDir' 2>/dev/null" | ForEach-Object { $_.Trim() } | Where-Object { $_ })
        $localNames = @($files | ForEach-Object { $_.Name })
        foreach ($r in $remoteNames) {
            if ($localNames -notcontains $r) {
                Remove-RemoteFile -Device $Device -Name $r -RemoteDir $RemoteDir
            }
        }
    }

    Write-Host "[DONE] Synced $($files.Count) file(s) to $RemoteDir on $Device"
    return $files
}

function Watch-And-Sync {
    param([string]$Device, [string]$LocalDir, [string]$RemoteDir, [switch]$DeleteRemoteMissing, [int]$IntervalSec)

    Write-Host "[WATCH] Monitoring $LocalDir every $IntervalSec second(s). Press Ctrl+C to stop."

    $files = Sync-All -Device $Device -LocalDir $LocalDir -RemoteDir $RemoteDir -DeleteRemoteMissing:$DeleteRemoteMissing
    $state = Get-FileState -Files $files

    while ($true) {
        Start-Sleep -Seconds $IntervalSec

        $latestFiles = Get-ImageFiles -Dir $LocalDir
        $latestState = Get-FileState -Files $latestFiles

        $changed = @()
        foreach ($f in $latestFiles) {
            if (-not $state.ContainsKey($f.Name) -or $state[$f.Name] -ne $latestState[$f.Name]) {
                $changed += $f
            }
        }

        foreach ($f in $changed) {
            Push-File -Device $Device -File $f -RemoteDir $RemoteDir
        }

        if ($DeleteRemoteMissing) {
            $deletedLocal = @($state.Keys | Where-Object { -not $latestState.ContainsKey($_) })
            foreach ($name in $deletedLocal) {
                Remove-RemoteFile -Device $Device -Name $name -RemoteDir $RemoteDir
            }
        }

        if ($changed.Count -gt 0) {
            Write-Host "[DONE] Incremental sync: $($changed.Count) updated file(s)"
        }

        $state = $latestState
    }
}

$device = Get-DeviceSerial -Preferred $Serial
Write-Host "[DEVICE] Using $device"
Write-Host "[SOURCE] $SourceDir"
Write-Host "[TARGET] $TargetDir"

if ($Watch) {
    Watch-And-Sync -Device $device -LocalDir $SourceDir -RemoteDir $TargetDir -DeleteRemoteMissing:$DeleteMissing -IntervalSec $PollSeconds
} else {
    Sync-All -Device $device -LocalDir $SourceDir -RemoteDir $TargetDir -DeleteRemoteMissing:$DeleteMissing | Out-Null
}