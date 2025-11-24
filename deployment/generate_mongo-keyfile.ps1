# command to run in powershell to create a mongo-key file for
[Convert]::ToBase64String((1..756|%{[byte](Get-Random -Max 256)})) | Out-File -Encoding ascii -NoNewline mongo-keyfile