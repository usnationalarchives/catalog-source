@echo off
set version=%1
(
@echo aggregator
mvn -N versions:set -DnewVersion=%version%

for /d %%g in (*) do (  
  if exist %%g\pom.xml (
    cd %%g
	@echo %%g
	mvn versions:set -DnewVersion=%version%
    cd ..
  )
)
)