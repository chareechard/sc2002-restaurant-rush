$ErrorActionPreference = 'Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    & ./build.ps1 test
    'obsolete resource' | Set-Content -Encoding utf8 build/classes/obsolete-resource.txt
    'obsolete class' | Set-Content -Encoding utf8 build/classes/RemovedFeature.class
    'obsolete test' | Set-Content -Encoding utf8 build/test-classes/RemovedTest.class
    & ./build.ps1 test
    $entries = @(& jar --list --file build/restaurant-rush.jar)
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect the built JAR' }
    if ($entries -contains 'obsolete-resource.txt' -or $entries -contains 'RemovedFeature.class' -or
        (Test-Path -LiteralPath build/test-classes/RemovedTest.class)) {
        throw 'A rebuild retained deleted classes or resources.'
    }
    Write-Output 'PASS: rebuild excludes obsolete classes, tests and resources'
} finally { Pop-Location }
