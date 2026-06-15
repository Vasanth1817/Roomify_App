import json
import pandas as pd
import os

report_dir = os.path.dirname(__file__)

# Load the correct JSON
with open(os.path.join(report_dir, "report_full.json"), 'r') as f:
    data = json.load(f)

# Create DataFrame
df = pd.DataFrame(data)

# Ensure we have the right columns
required_cols = [
    "case_id", "name", "endpoint", "method", "path", "role", "status",
    "expected_status", "response_time_ms", "test_category", "severity",
    "finding", "note", "timestamp"
]

# Filter to only available columns
available_cols = [col for col in required_cols if col in df.columns]
df = df[available_cols]

xlsx_path = os.path.join(report_dir, "report_full.xlsx")

# Write XLSX with multiple sheets
with pd.ExcelWriter(xlsx_path, engine="openpyxl") as writer:
    # Results sheet - all tests
    df.to_excel(writer, index=False, sheet_name="Results")
    
    # Summary sheet
    summary_rows = [
        {"Metric": "Total tests", "Value": len(df)},
        {"Metric": "Passed (status matches expected)", "Value": int((~df["finding"]).sum())},
        {"Metric": "Failed (status differs from expected)", "Value": int(df["finding"].sum())},
        {"Metric": "Average response time (ms)", "Value": round(df["response_time_ms"].dropna().mean(), 2)},
        {"Metric": "Max response time (ms)", "Value": int(df["response_time_ms"].dropna().max())},
        {"Metric": "Min response time (ms)", "Value": int(df["response_time_ms"].dropna().min())},
    ]
    summary_df = pd.DataFrame(summary_rows)
    summary_df.to_excel(writer, index=False, sheet_name="Summary")
    
    # Findings sheet - only failures
    failures = df[df["finding"]].copy()
    if len(failures) > 0:
        failures.to_excel(writer, index=False, sheet_name="Findings")
    else:
        # Empty findings sheet if no failures
        pd.DataFrame(columns=["Info"]).to_excel(writer, index=False, sheet_name="Findings")
    
    # Status distribution
    status_dist = df["status"].value_counts().reset_index()
    status_dist.columns = ["Status Code", "Count"]
    status_dist.to_excel(writer, index=False, sheet_name="StatusCodes")

print(f"✅ Updated XLSX: {xlsx_path}")
print(f"Total tests: {len(df)}")
print(f"Passing: {int((~df['finding']).sum())}")
print(f"Failing: {int(df['finding'].sum())}")
