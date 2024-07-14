pipenv sync

if($? -eq $false) {
    Write-Host "These software requirements to run the project:"
    Write-Host "- python 3.8 (or later)"
    Write-Host "- pipenv"
    exit 1
}
