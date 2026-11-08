param([ValidateSet('build','run','test')][string]$Task = 'build', [string[]]$GameArgs = @())
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $buildRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot 'build'))
    if (Test-Path -LiteralPath $buildRoot) {
        if ((Get-Item -LiteralPath $buildRoot).Attributes -band [IO.FileAttributes]::ReparsePoint) {
            throw 'Build directory must not be a symbolic link or junction.'
        }
    }
    foreach ($name in @('classes', 'test-classes')) {
        $directory = [IO.Path]::GetFullPath((Join-Path $buildRoot $name))
        if (-not $directory.StartsWith($buildRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
            throw 'Generated directory must stay inside build.'
        }
        if (Test-Path -LiteralPath $directory) { Remove-Item -LiteralPath $directory -Recurse -Force }
        New-Item -ItemType Directory -Force $directory | Out-Null
    }
    $sources = @(Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
    & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes $sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
    & jar --create --file build/restaurant-rush.jar --main-class rush.cli.ConsoleApp -C build/classes .
    if ($LASTEXITCODE -ne 0) { throw 'JAR creation failed' }
    if ($Task -eq 'test') {
        $tests = @(Get-ChildItem src/test/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
        & javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp build/classes -d build/test-classes $tests
        if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
        & java -ea -cp 'build/classes;build/test-classes' rush.AllTests
        if ($LASTEXITCODE -ne 0) { throw 'Tests failed' }
    }
    if ($Task -eq 'run') {
        & java -jar build/restaurant-rush.jar @GameArgs
        if ($LASTEXITCODE -ne 0) { throw 'Game failed' }
    }
} finally { Pop-Location }
