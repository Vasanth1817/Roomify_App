"""Run backend-local automated security tests."""

import argparse
import json
import os
import sys
from datetime import datetime

from endpoints import ENDPOINTS
from helpers import INPUT_PATHS, load_input, normalize_url, request_json


def print_discovery():
    print("Discovered endpoints:")
    for idx, endpoint in enumerate(ENDPOINTS, start=1):
        print(f"{idx}. {endpoint['method']} {endpoint['path']} — {endpoint.get('description', '')}")
    print(f"\nTotal endpoints discovered: {len(ENDPOINTS)}")
    print("\nNote: backend code does not enforce authentication on these routes.")


def create_report_entry(endpoint, role, response, category, note):
    return {
        "endpoint": f"{endpoint['method']} {endpoint['path']}",
        "method": endpoint["method"],
        "path": endpoint["path"],
        "role": role,
        "status": response.get("status"),
        "expected_status": None,
        "finding": False,
        "severity": "info",
        "response_time_ms": response.get("elapsed_ms"),
        "test_category": category,
        "note": note,
        "timestamp": datetime.utcnow().isoformat() + "Z",
    }


def run_safe_tests(base_url, allow_write=False):
    results = []
    for endpoint in ENDPOINTS:
        if endpoint["method"] == "DELETE" and "path_params" not in endpoint:
            continue
        path = endpoint["path"]
        if "path_params" in endpoint:
            path = path.format(**endpoint["path_params"])
        url = normalize_url(base_url, path)
        if endpoint["method"] == "POST" and not allow_write:
            continue
        body = endpoint.get("sample_body")
        response = request_json(endpoint["method"], url, body=body)
        note = "Safe probe"
        if response.get("status") is None:
            note = f"Request failed: {response.get('error')}"
        results.append(create_report_entry(endpoint, "none", response, "safe_probe", note))
        print(f"{endpoint['method']} {path} -> {response.get('status')} ({response.get('elapsed_ms')}ms)")
    return results


def main():
    parser = argparse.ArgumentParser(description="RoomifyBackend automated test harness.")
    parser.add_argument("--discover", action="store_true", help="Show discovered endpoints and exit.")
    parser.add_argument("--run", action="store_true", help="Run safe tests if input.json exists.")
    parser.add_argument("--allow-write", action="store_true", help="Include safe POST probes.")
    args = parser.parse_args()

    if args.discover:
        print_discovery()
        sys.exit(0)

    if not args.run:
        parser.print_help()
        sys.exit(1)

    try:
        data, used_path = load_input()
    except FileNotFoundError as exc:
        print(exc)
        sys.exit(1)

    base_url = data.get("baseUrl")
    if not base_url:
        print(f"input.json found at {used_path}, but baseUrl is missing.")
        sys.exit(1)

    print(f"Using baseUrl from {used_path}: {base_url}\n")
    print_discovery()
    print("\nRunning safe tests...\n")
    results = run_safe_tests(base_url, allow_write=args.allow_write)
    report_path = os.path.join(os.path.dirname(__file__), "report.json")
    with open(report_path, "w", encoding="utf-8") as handle:
        json.dump(results, handle, indent=2)
    print(f"\nSaved results to {report_path}")


if __name__ == "__main__":
    main()
