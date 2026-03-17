@echo off
git add -A
git commit -m "fix password persistence, add Excel import feature"
git push origin main
del commit.bat
