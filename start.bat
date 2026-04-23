@echo off
IF "%~1"=="" (
    ECHO Usage: start.bat ^<PORT^>
    EXIT /B 1
)

SET APP_PORT=%1
docker-compose up --build -d --wait

ECHO Server started on http://localhost:%APP_PORT%
ECHO API cluster is running with 3 instances.