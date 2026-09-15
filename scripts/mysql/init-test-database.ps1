[CmdletBinding()]
param(
    [string] $MySqlClient
)

$ErrorActionPreference = 'Stop'

if (-not $MySqlClient) {
    $mysqlCommand = Get-Command mysql.exe -ErrorAction SilentlyContinue
    $candidates = @(
        $mysqlCommand.Source
        'D:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
        'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
    ) | Where-Object { $_ -and (Test-Path -LiteralPath $_) }
    $MySqlClient = $candidates | Select-Object -First 1
}

if (-not $MySqlClient) {
    throw 'mysql.exe was not found. Pass its path through -MySqlClient.'
}

$sqlPath = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot 'init-test-database.sql')).Path.Replace('\', '/')

Write-Host 'Enter the existing MySQL root password. Input is hidden; the root password is not changed.'
& $MySqlClient --user=root --password "--execute=SOURCE $sqlPath"

if ($LASTEXITCODE -ne 0) {
    throw "Test database initialization failed. mysql.exe exit code: $LASTEXITCODE"
}

Write-Host 'The javafix_shop_test database and javafix_test account are ready.' -ForegroundColor Green
