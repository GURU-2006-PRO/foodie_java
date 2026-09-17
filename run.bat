@echo off
if "%GEMINI_API_KEY%"=="" (
    echo [NOTICE] GEMINI_API_KEY environment variable is not set.
    echo Please set it using: set GEMINI_API_KEY=your-api-key
)
echo =========================================================
echo FoodScan Vision AI is starting...
echo Open your browser at: http://localhost:8085/foodscanner/
echo =========================================================
mvn jetty:run
pause