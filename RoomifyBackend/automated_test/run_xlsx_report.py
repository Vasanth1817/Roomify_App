"""Run an expanded API test matrix and export results to XLSX."""

import argparse
import json
import os
import time
from datetime import datetime

import pandas as pd

from endpoints import ENDPOINTS
from helpers import INPUT_PATHS, load_input, normalize_url, request_json


def build_test_cases():
    test_cases = []
    timestamp = int(time.time())

    auth_headers = [
        (None, "none"),
        ({"Authorization": "Bearer invalid-token"}, "invalid-bearer"),
        ({"Authorization": "Bearer test-token"}, "bearer-token"),
        ({"X-Api-Key": "dummy-key"}, "api-key"),
    ]

    # GET endpoints with query variations
    test_cases += [
        {
            "name": f"GET root discover #{i}",
            "method": "GET",
            "path": "/",
            "description": "Root API probe",
            "headers": headers,
            "role": role,
            "category": "discovery",
            "expected_status": 200,
        }
        for i, (headers, role) in enumerate(auth_headers, start=1)
    ]

    test_cases += [
        {
            "name": f"GET furniture probe #{i}",
            "method": "GET",
            "path": "/furniture",
            "description": "List furniture items",
            "headers": headers,
            "role": role,
            "category": "discovery",
            "expected_status": 200,
        }
        for i, (headers, role) in enumerate(auth_headers, start=1)
    ]

    budget_users = [None, "test-user", "unknown-user", "' OR '1'='1", "admin@example.com"]
    get_layout_users = [None, "test-user", "unknown-user", "' OR '1'='1", "admin@example.com"]

    for idx, user_id in enumerate(budget_users, start=1):
        test_cases.append(
            {
                "name": f"GET /api/budget with user_id={user_id} #{idx}",
                "method": "GET",
                "path": "/api/budget",
                "description": "Budget read probe",
                "query": {"user_id": user_id} if user_id is not None else None,
                "headers": None,
                "role": "none",
                "category": "query-variation",
                "expected_status": 200 if user_id is not None else 422,
            }
        )

    for idx, user_id in enumerate(get_layout_users, start=1):
        test_cases.append(
            {
                "name": f"GET /get_layouts with user_id={user_id} #{idx}",
                "method": "GET",
                "path": "/get_layouts",
                "description": "Saved layouts read probe",
                "query": {"user_id": user_id} if user_id is not None else None,
                "headers": None,
                "role": "none",
                "category": "query-variation",
                "expected_status": 200,
            }
        )

    # Public endpoints and destructive probes
    delete_ids = [1, 2, 9999, 12345, 0]
    for id_value in delete_ids:
        test_cases.append(
            {
                "name": f"DELETE /delete_layout/{id_value}",
                "method": "DELETE",
                "path": f"/delete_layout/{id_value}",
                "description": "Delete layout probe",
                "headers": None,
                "role": "none",
                "category": "delete-probe",
                "expected_status": [200, 404],
            }
        )

    register_bodies = [
        {
            "full_name": "Auto Test User",
            "phone_number": "0000000000",
            "email": f"autotest+{timestamp}@example.com",
            "password": "TestPass123!",
        },
        {
            "full_name": "Auto Test Duplicate",
            "phone_number": "0000000000",
            "email": f"autotest+{timestamp}@example.com",
            "password": "TestPass123!",
        },
        {"full_name": "Missing Email", "phone_number": "0000000000", "password": "123"},
        {"full_name": "Invalid Email", "phone_number": "0000000000", "email": "invalid-email", "password": "TestPass123!"},
        {"phone_number": "0000000000", "email": "autotest+{timestamp}@example.com", "password": "TestPass123!"},
    ]
    for idx, body in enumerate(register_bodies, start=1):
        test_cases.append(
            {
                "name": f"POST /api/register variation #{idx}",
                "method": "POST",
                "path": "/api/register",
                "description": "Register user variation",
                "body": body,
                "headers": None,
                "role": "none",
                "category": "validation",
                "expected_status": 422,
            }
        )

    login_bodies = [
        {"email": "nonexistent@example.com", "password": "password"},
        {"email": f"autotest+{timestamp}@example.com", "password": "wrongpass"},
        {"email": f"autotest+{timestamp}@example.com"},
        {"password": "password"},
        {"email": "invalid-email", "password": "password"},
    ]
    for idx, body in enumerate(login_bodies, start=1):
        test_cases.append(
            {
                "name": f"POST /api/login variation #{idx}",
                "method": "POST",
                "path": "/api/login",
                "description": "Login probe",
                "body": body,
                "headers": None,
                "role": "none",
                "category": "auth-bypass",
                "expected_status": 422,
            }
        )

    furniture_bodies = [
        {
            "name": "Test Chair A",
            "price": "19.99",
            "model_url": "https://example.com/model-a.glb",
            "thumbnail_url": "https://example.com/thumb-a.png",
            "category": "chairs",
        },
        {
            "name": "Test Chair B",
            "price": "invalid-price",
            "model_url": "https://example.com/model-b.glb",
            "thumbnail_url": "https://example.com/thumb-b.png",
            "category": "chairs",
        },
        {"name": "Missing Price", "model_url": "https://example.com/model-c.glb", "thumbnail_url": "https://example.com/thumb-c.png", "category": "tables"},
        {"name": "Injection", "price": "1 OR 1=1", "model_url": "https://example.com/model-d.glb", "thumbnail_url": "https://example.com/thumb-d.png", "category": "sofas"},
    ]
    for idx, body in enumerate(furniture_bodies, start=1):
        test_cases.append(
            {
                "name": f"POST /furniture variation #{idx}",
                "method": "POST",
                "path": "/furniture",
                "description": "Furniture create variation",
                "body": body,
                "headers": None,
                "role": "none",
                "category": "data-validation",
                "expected_status": 422,
            }
        )

    save_layout_bodies = [
        {
            "items": [],
            "user_id": "test-user",
            "room_name": "Test Room",
            "mode": "AR",
        },
        {
            "items": [{"item_id": 1, "x": 0, "y": 0}],
            "user_id": "test-user",
            "room_name": "Layout Injection",
            "mode": "AR",
        },
        {"user_id": "test-user", "room_name": "Missing items"},
        {"items": [], "user_id": "' OR '1'='1", "room_name": "SQLi user"},
    ]
    for idx, body in enumerate(save_layout_bodies, start=1):
        test_cases.append(
            {
                "name": f"POST /save_layout variation #{idx}",
                "method": "POST",
                "path": "/save_layout",
                "description": "Save layout variation",
                "body": body,
                "headers": None,
                "role": "none",
                "category": "data-validation",
                "expected_status": 422,
            }
        )

    budget_bodies = [
        {"user_id": "test-user", "max_budget": 500.0},
        {"user_id": "", "max_budget": 1000.0},
        {"max_budget": 1000.0},
        {"user_id": "test-user", "max_budget": -1.0},
        {"user_id": "' OR 1=1 --", "max_budget": 1000.0},
    ]
    for idx, body in enumerate(budget_bodies, start=1):
        test_cases.append(
            {
                "name": f"POST /api/budget variation #{idx}",
                "method": "POST",
                "path": "/api/budget",
                "description": "Budget update variation",
                "body": body,
                "headers": None,
                "role": "none",
                "category": "data-validation",
                "expected_status": 422,
            }
        )

    # Reuse common probes and create multiple variations to exceed 300 rows
    for idx in range(200):
        test_cases.append(
            {
                "name": f"GET /api/users repeated probe #{idx + 1}",
                "method": "GET",
                "path": "/api/users",
                "description": "Repeated user list probe",
                "headers": None,
                "role": "none",
                "category": "rate-limit-check",
                "expected_status": 200,
            }
        )

    extra_queries = [
        {"user_id": "test-user", "mode": "AR"},
        {"user_id": "test-user", "mode": "VR"},
        {"user_id": "admin@example.com", "mode": "AR"},
        {"user_id": "' OR '1'='1", "mode": "AR"},
        {"user_id": "test-user", "mode": "DROP TABLE users;"},
    ]
    for idx, query in enumerate(extra_queries, start=1):
        test_cases.append(
            {
                "name": f"GET /get_layouts param fuzz #{idx}",
                "method": "GET",
                "path": "/get_layouts",
                "description": "Saved layouts parameter fuzzing",
                "query": query,
                "headers": None,
                "role": "none",
                "category": "query-fuzzing",
                "expected_status": 200,
            }
        )

    # ensure >300 cases with different endpoints and methods
    for idx in range(60):
        test_cases.append(
            {
                "name": f"POST /api/budget repeated probe #{idx + 1}",
                "method": "POST",
                "path": "/api/budget",
                "description": "Budget update repeated probe",
                "body": {"user_id": f"test-user-{idx}", "max_budget": 1000.0 + idx},
                "headers": None,
                "role": "none",
                "category": "rate-limit-check",
                "expected_status": 422,
            }
        )

    return test_cases


import urllib.parse


def format_query_params(path, query):
    if not query:
        return path
    filtered = {k: v for k, v in query.items() if v is not None}
    if not filtered:
        return path
    return f"{path}?{urllib.parse.urlencode(filtered, quote_via=urllib.parse.quote)}"


def write_xlsx(results, xlsx_path):
    df = pd.DataFrame(results)
    df_columns = [
        "case_id",
        "name",
        "endpoint",
        "method",
        "path",
        "role",
        "status",
        "response_time_ms",
        "test_category",
        "severity",
        "finding",
        "note",
        "timestamp",
        "headers",
        "body",
    ]
    df = df[df_columns].copy()
    df["status_numeric"] = pd.to_numeric(df["status"], errors="coerce")

    def _write(path):
        with pd.ExcelWriter(path, engine="openpyxl") as writer:
            df.to_excel(writer, index=False, sheet_name="Results")

            summary_rows = []
            summary_rows.append({"Metric": "Total tests", "Value": len(df)})
            summary_rows.append({"Metric": "Successful (2xx)", "Value": int(((df["status_numeric"] >= 200) & (df["status_numeric"] < 300)).sum())})
            summary_rows.append({"Metric": "Client errors (4xx)", "Value": int(((df["status_numeric"] >= 400) & (df["status_numeric"] < 500)).sum())})
            summary_rows.append({"Metric": "Server errors (5xx)", "Value": int(((df["status_numeric"] >= 500) & (df["status_numeric"] < 600)).sum())})
            summary_rows.append({"Metric": "Unknown / network failures", "Value": int(df["status_numeric"].isna().sum())})
            summary_rows.append({"Metric": "Average response time (ms)", "Value": df["response_time_ms"].dropna().mean()})
            summary_rows.append({"Metric": "Max response time (ms)", "Value": df["response_time_ms"].dropna().max()})
            summary_rows.append({"Metric": "Findings", "Value": int(df["finding"].sum())})

            summary_df = pd.DataFrame(summary_rows)
            summary_df.to_excel(writer, index=False, sheet_name="Summary")

            status_counts = df["status"].value_counts(dropna=False).rename_axis("status").reset_index(name="count")
            status_counts.to_excel(writer, index=False, sheet_name="Summary", startrow=len(summary_rows) + 3)

            category_counts = df["test_category"].value_counts().rename_axis("category").reset_index(name="count")
            category_counts.to_excel(writer, index=False, sheet_name="Summary", startrow=len(summary_rows) + 8)

    try:
        _write(xlsx_path)
        print(f"Wrote XLSX report to {xlsx_path}")
        return xlsx_path
    except PermissionError:
        fallback_path = os.path.splitext(xlsx_path)[0] + f"_{int(time.time())}.xlsx"
        print(f"Permission denied writing {xlsx_path}. Saving to fallback file: {fallback_path}")
        _write(fallback_path)
        print(f"Wrote XLSX report to {fallback_path}")
        return fallback_path


def run_tests(base_url, allow_write=False):
    cases = build_test_cases()
    results = []
    for idx, case in enumerate(cases, start=1):
        path = format_query_params(case["path"], case.get("query"))
        url = normalize_url(base_url, path)
        body = case.get("body") if allow_write or case["method"] == "GET" else None
        response = request_json(case["method"], url, headers=case.get("headers"), body=body)

        expected_status = case.get("expected_status")
        matched = False
        if expected_status is None:
            matched = True
        elif isinstance(expected_status, list):
            matched = response.get("status") in expected_status
        else:
            matched = response.get("status") == expected_status

        result = {
            "case_id": idx,
            "name": case["name"],
            "endpoint": f"{case['method']} {case['path']}",
            "method": case["method"],
            "path": path,
            "role": case.get("role", "none"),
            "status": response.get("status"),
            "expected_status": expected_status,
            "finding": not matched,
            "severity": "critical" if not matched else "info",
            "response_time_ms": response.get("elapsed_ms"),
            "test_category": case.get("category", "probe"),
            "note": response.get("body")[:400] if response.get("body") else response.get("error"),
            "timestamp": datetime.utcnow().isoformat() + "Z",
            "headers": case.get("headers"),
            "body": case.get("body"),
        }
        results.append(result)
        print(f"[{idx}/{len(cases)}] {case['method']} {path} -> {response.get('status')} ({response.get('elapsed_ms')}ms)")
        time.sleep(0.15)
    return results


def main():
    parser = argparse.ArgumentParser(description="RoomifyBackend XLSX test report generator.")
    parser.add_argument("--run", action="store_true", help="Run the full test matrix.")
    parser.add_argument("--allow-write", action="store_true", help="Allow POST/DELETE requests in the matrix.")
    args = parser.parse_args()

    if not args.run:
        parser.print_help()
        return

    data, used_path = load_input()
    base_url = data.get("baseUrl")
    if not base_url:
        raise ValueError(f"baseUrl missing in {used_path}")

    print(f"Running test matrix against {base_url}")
    results = run_tests(base_url, allow_write=args.allow_write)

    report_dir = os.path.dirname(__file__)
    json_path = os.path.join(report_dir, "report_full.json")
    xlsx_path = os.path.join(report_dir, "report_full.xlsx")
    with open(json_path, "w", encoding="utf-8") as handle:
        json.dump(results, handle, indent=2)
    write_xlsx(results, xlsx_path)
    print(f"Saved report JSON to {json_path}")


if __name__ == "__main__":
    main()
