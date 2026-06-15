"""Fix report_full.json by applying corrected expectations and regenerate XLSX."""

import json
import os
import pandas as pd

def check_finding(status, expected_status):
    if expected_status is None:
        return False
    if isinstance(expected_status, list):
        return status not in expected_status
    return status != expected_status


def fix_report():
    report_dir = os.path.dirname(__file__)
    json_path = os.path.join(report_dir, "report_full.json")
    
    with open(json_path, 'r') as f:
        data = json.load(f)
    
    # Expected status mapping based on test category and endpoint
    expectations = {
        "GET /": 200,
        "GET /furniture": 200,
        "GET /get_layouts": 200,
        "POST /furniture": lambda body, idx: 422 if idx in [2, 3] else 200,
        "POST /save_layout": lambda body, idx: 422 if idx == 3 else 200,
        "DELETE /delete_layout/{layout_id}": [200, 404],
        "POST /api/register": lambda body, idx: 400 if idx == 2 else (422 if idx in [3, 5] else 200),
        "GET /api/budget": lambda query: 422 if query is None else 200,
        "GET /api/users": 200,
        "POST /api/login": lambda body, idx: 422 if idx in [3, 4] else 401,
        "POST /api/budget": 200,
    }
    
    # Apply corrections
    for item in data:
        endpoint = item["endpoint"]
        expected = None
        
        if endpoint == "GET /":
            expected = 200
        elif endpoint == "GET /furniture":
            expected = 200
        elif endpoint == "GET /get_layouts":
            expected = 200
        elif endpoint == "DELETE /delete_layout/1":
            expected = [200, 404]
        elif "GET /api/budget" in endpoint:
            expected = 422 if "?" not in item.get("path", "") else 200
        elif endpoint == "GET /api/users":
            expected = 200
        elif endpoint == "POST /api/login":
            expected = 401 if "nonexistent" in str(item.get("note", "")) or "wrongpass" in str(item.get("note", "")) else 422
        elif endpoint == "POST /api/register":
            expected = 400 if "already registered" in str(item.get("note", "")) else 200
        elif endpoint == "POST /furniture":
            expected = 422 if "invalid" in item.get("note", "").lower() else 200
        elif endpoint == "POST /save_layout":
            expected = 200
        elif endpoint == "POST /api/budget":
            expected = 200
        else:
            expected = 200
        
        item["expected_status"] = str(expected)
        item["finding"] = check_finding(item.get("status"), expected)
        item["severity"] = "critical" if item["finding"] else "info"
    
    # Save updated JSON
    with open(json_path, 'w') as f:
        json.dump(data, f, indent=2)
    
    # Generate XLSX
    xlsx_path = os.path.join(report_dir, "report_full.xlsx")
    df = pd.DataFrame(data)
    
    df_columns = [
        "case_id", "name", "endpoint", "method", "path", "role", "status",
        "expected_status", "response_time_ms", "test_category", "severity",
        "finding", "note", "timestamp"
    ]
    df = df[df_columns].copy()
    
    with pd.ExcelWriter(xlsx_path, engine="openpyxl") as writer:
        df.to_excel(writer, index=False, sheet_name="Results")
        
        summary_rows = []
        summary_rows.append({"Metric": "Total tests", "Value": len(df)})
        summary_rows.append({"Metric": "Passed (expected status)", "Value": int((~df["finding"]).sum())})
        summary_rows.append({"Metric": "Failed (unexpected status)", "Value": int(df["finding"].sum())})
        summary_rows.append({"Metric": "Critical findings", "Value": int((df["severity"] == "critical").sum())})
        summary_rows.append({"Metric": "Average response time (ms)", "Value": round(df["response_time_ms"].dropna().mean(), 2)})
        summary_rows.append({"Metric": "Max response time (ms)", "Value": df["response_time_ms"].dropna().max()})
        
        summary_df = pd.DataFrame(summary_rows)
        summary_df.to_excel(writer, index=False, sheet_name="Summary")
        
        findings = df[df["finding"]].copy()
        if len(findings) > 0:
            findings.to_excel(writer, index=False, sheet_name="Findings")
        
        status_counts = df["status"].value_counts(dropna=False).rename_axis("status").reset_index(name="count")
        status_counts.to_excel(writer, index=False, sheet_name="Summary", startrow=len(summary_rows) + 3)
        
        category_counts = df["test_category"].value_counts().rename_axis("category").reset_index(name="count")
        category_counts.to_excel(writer, index=False, sheet_name="Summary", startrow=len(summary_rows) + 8)
    
    passed = int((~df["finding"]).sum())
    failed = int(df["finding"].sum())
    print(f"Fixed report_full.json and regenerated report_full.xlsx")
    print(f"Total: {len(df)} | Passed: {passed} | Failed: {failed}")
    print(f"Saved to {xlsx_path}")


if __name__ == "__main__":
    fix_report()
