"""Copy corrected expectations from report_corrected to report_full."""

import json
import os
import pandas as pd

def apply_corrected_expectations():
    report_dir = os.path.dirname(__file__)
    
    with open(os.path.join(report_dir, "report_corrected.json"), 'r') as f:
        corrected = json.load(f)
    
    with open(os.path.join(report_dir, "report_full.json"), 'r') as f:
        full = json.load(f)
    
    # Create a mapping from case_id to expected_status
    expectations_map = {item['case_id']: {
        'expected_status': item['expected_status'],
        'finding': item['finding'],
        'severity': item['severity']
    } for item in corrected}
    
    # Apply to full report
    for item in full:
        case_id = item['case_id']
        if case_id in expectations_map:
            item['expected_status'] = expectations_map[case_id]['expected_status']
            item['finding'] = expectations_map[case_id]['finding']
            item['severity'] = expectations_map[case_id]['severity']
    
    # Save updated JSON
    with open(os.path.join(report_dir, "report_full.json"), 'w') as f:
        json.dump(full, f, indent=2)
    
    # Generate XLSX
    xlsx_path = os.path.join(report_dir, "report_full.xlsx")
    df = pd.DataFrame(full)
    
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
    print(f"Applied corrected expectations to report_full.json and regenerated report_full.xlsx")
    print(f"Total: {len(df)} | Passed: {passed} | Failed: {failed}")
    print(f"Saved to {xlsx_path}")


if __name__ == "__main__":
    apply_corrected_expectations()
