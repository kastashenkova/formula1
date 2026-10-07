@echo off
copy backend\application\.env-example backend\application\.env
call mvnw.cmd spring-boot:run -pl backend/application
