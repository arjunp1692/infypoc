$ErrorActionPreference = "Stop"
. "$PSScriptRoot\env.ps1"
& "$PSScriptRoot\..\mvnw.cmd" -B test
exit $LASTEXITCODE
