if (-not $env:GEMINI_API_KEY) {
    Write-Host "[NOTICE] GEMINI_API_KEY environment variable is not set." -ForegroundColor Yellow
    Write-Host "Set it using: `$env:GEMINI_API_KEY = 'your-key'" -ForegroundColor Yellow
}
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host "FoodScan Vision AI is starting..." -ForegroundColor Green
Write-Host "Open browser at: http://localhost:8085/foodscanner/" -ForegroundColor Yellow
Write-Host "=========================================================" -ForegroundColor Cyan
mvn jetty:run