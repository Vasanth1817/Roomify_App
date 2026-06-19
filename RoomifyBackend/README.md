# Roomify Backend

## Load Testing

A simple baseline load test helper is available at `RoomifyBackend/automated_test/load_test.py`.

### Requirements

- Python 3.11+ (or the environment already used by the project)
- `pandas`
- `openpyxl`
- `requests`

If needed, install packages with:

```powershell
python -m pip install pandas openpyxl requests
```

### Run the baseline load test

```powershell
cd C:\Users\vasan\Downloads\Roomify_Full\RoomifyBackend
python automated_test\load_test.py --base-url http://127.0.0.1:8000 --path /furniture --users 100 --duration 60 --output load_test_results.xlsx
```

### Example output

The script writes an Excel workbook with the following sheets:

- `Summary`: test settings, total requests, RPS, average/min/max latency, success/failure counts
- `Requests`: per-request timings and status codes
- `StatusCounts`: counts grouped by response status

### Custom endpoint example

```powershell
python automated_test\load_test.py --base-url http://127.0.0.1:8000 --path /api/budget --method POST --users 100 --duration 60 --json-body '{"user_id":"test-user","max_budget":100}' --output budget_load_test.xlsx
```
