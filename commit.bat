@echo off
git add -A
git commit -m "fix: fix gradlew script and download wrapper jar"
git push origin main --force
del commit.bat
