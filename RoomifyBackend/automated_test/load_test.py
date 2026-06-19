"""Baseline load test for Roomify backend.

This script runs a sustained burst of concurrent requests against an API endpoint,
collects response times and status codes, and writes the results to an Excel file.

Example:
  python load_test.py --base-url http://127.0.0.1:8000 --path /furniture --users 100 --duration 60 --output load_test_results.xlsx
"""

import argparse
import json
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime

import pandas as pd
import requests


def parse_headers(header_strings):
    headers = {}
    if not header_strings:
        return headers
    for header in header_strings:
        if ':' not in header:
            raise ValueError(f"Invalid header format: {header}. Use 'Name: Value'.")
        name, value = header.split(':', 1)
        headers[name.strip()] = value.strip()
    return headers


def parse_json(body_text):
    if not body_text:
        return None
    try:
        return json.loads(body_text)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON body: {exc}") from exc


def request_worker(worker_id, base_url, path, method, headers, json_body, stop_time):
    session = requests.Session()
    url = base_url.rstrip('/') + '/' + path.lstrip('/')
    request_rows = []
    while time.time() < stop_time:
        start = time.perf_counter()
        timestamp = datetime.utcnow().isoformat() + 'Z'
        try:
            response = session.request(method=method, url=url, headers=headers, json=json_body, timeout=30)
            elapsed_ms = (time.perf_counter() - start) * 1000.0
            request_rows.append(
                {
                    'worker_id': worker_id,
                    'timestamp': timestamp,
                    'method': method,
                    'url': url,
                    'status_code': response.status_code,
                    'elapsed_ms': round(elapsed_ms, 2),
                    'error': None,
                }
            )
        except Exception as exc:
            elapsed_ms = (time.perf_counter() - start) * 1000.0
            request_rows.append(
                {
                    'worker_id': worker_id,
                    'timestamp': timestamp,
                    'method': method,
                    'url': url,
                    'status_code': None,
                    'elapsed_ms': round(elapsed_ms, 2),
                    'error': str(exc),
                }
            )
    return request_rows


def run_load_test(base_url, path, method, users, duration, headers, json_body):
    end_time = time.time() + duration
    request_rows = []

    with ThreadPoolExecutor(max_workers=users) as executor:
        futures = [executor.submit(request_worker, i + 1, base_url, path, method, headers, json_body, end_time) for i in range(users)]
        for future in as_completed(futures):
            request_rows.extend(future.result())

    return request_rows


def build_summary(request_rows, duration, users, base_url, path, method):
    total = len(request_rows)
    successes = [row for row in request_rows if row['status_code'] and 200 <= row['status_code'] < 400]
    errors = [row for row in request_rows if row['error']]
    status_counts = {}
    for row in request_rows:
        key = row['status_code'] if row['status_code'] is not None else 'ERROR'
        status_counts[key] = status_counts.get(key, 0) + 1

    latencies = [row['elapsed_ms'] for row in request_rows if row['elapsed_ms'] is not None]
    avg_latency = round(sum(latencies) / len(latencies), 2) if latencies else None
    min_latency = round(min(latencies), 2) if latencies else None
    max_latency = round(max(latencies), 2) if latencies else None
    p50 = round(sorted(latencies)[int(len(latencies) * 0.5)] if latencies else None, 2)
    p90 = round(sorted(latencies)[int(len(latencies) * 0.9)] if latencies else None, 2)
    rps = round(total / duration, 2) if duration > 0 else 0

    return {
        'run_timestamp': datetime.utcnow().isoformat() + 'Z',
        'base_url': base_url,
        'path': path,
        'method': method,
        'duration_seconds': duration,
        'virtual_users': users,
        'total_requests': total,
        'success_count': len(successes),
        'failure_count': len(errors) + sum(1 for row in request_rows if row['status_code'] and row['status_code'] >= 400),
        'rps': rps,
        'min_latency_ms': min_latency,
        'avg_latency_ms': avg_latency,
        'max_latency_ms': max_latency,
        'p50_latency_ms': p50,
        'p90_latency_ms': p90,
        'status_counts': status_counts,
    }


def write_excel(output_path, summary, request_rows):
    summary_rows = [
        {'Metric': 'Run timestamp', 'Value': summary['run_timestamp']},
        {'Metric': 'Base URL', 'Value': summary['base_url']},
        {'Metric': 'Path', 'Value': summary['path']},
        {'Metric': 'Method', 'Value': summary['method']},
        {'Metric': 'Virtual users', 'Value': summary['virtual_users']},
        {'Metric': 'Duration (seconds)', 'Value': summary['duration_seconds']},
        {'Metric': 'Total requests', 'Value': summary['total_requests']},
        {'Metric': 'Requests per second', 'Value': summary['rps']},
        {'Metric': 'Success count', 'Value': summary['success_count']},
        {'Metric': 'Failure count', 'Value': summary['failure_count']},
        {'Metric': 'Min latency (ms)', 'Value': summary['min_latency_ms']},
        {'Metric': 'Avg latency (ms)', 'Value': summary['avg_latency_ms']},
        {'Metric': 'Max latency (ms)', 'Value': summary['max_latency_ms']},
        {'Metric': 'P50 latency (ms)', 'Value': summary['p50_latency_ms']},
        {'Metric': 'P90 latency (ms)', 'Value': summary['p90_latency_ms']},
    ]

    df_summary = pd.DataFrame(summary_rows)
    df_requests = pd.DataFrame(request_rows)
    df_status = pd.DataFrame([{'status_code': key, 'count': count} for key, count in summary['status_counts'].items()])

    with pd.ExcelWriter(output_path, engine='openpyxl') as writer:
        df_summary.to_excel(writer, index=False, sheet_name='Summary')
        df_requests.to_excel(writer, index=False, sheet_name='Requests')
        df_status.to_excel(writer, index=False, sheet_name='StatusCounts')

    print(f"Wrote Excel results to {output_path}")


def main():
    parser = argparse.ArgumentParser(description='Run a baseline load test against the Roomify backend API and export Excel results.')
    parser.add_argument('--base-url', default='http://127.0.0.1:8000', help='API base URL')
    parser.add_argument('--path', default='/', help='API path to test')
    parser.add_argument('--method', default='GET', help='HTTP method to use')
    parser.add_argument('--users', type=int, default=100, help='Number of concurrent virtual users')
    parser.add_argument('--duration', type=int, default=60, help='Load test duration in seconds')
    parser.add_argument('--header', action='append', help="Request header in the form 'Name: Value'. Can be repeated.")
    parser.add_argument('--json-body', help='JSON payload for POST/PUT requests')
    parser.add_argument('--output', default='load_test_results.xlsx', help='Excel output file path')
    args = parser.parse_args()

    headers = parse_headers(args.header)
    json_body = parse_json(args.json_body)

    full_url = args.base_url.rstrip('/') + '/' + args.path.lstrip('/')
    print(f"Starting load test: {args.users} users for {args.duration}s against {full_url}...")
    rows = run_load_test(args.base_url, args.path, args.method.upper(), args.users, args.duration, headers, json_body)
    summary = build_summary(rows, args.duration, args.users, args.base_url, args.path, args.method.upper())

    print('Summary:')
    print(f"  Total requests: {summary['total_requests']}")
    print(f"  RPS: {summary['rps']}")
    print(f"  Avg latency: {summary['avg_latency_ms']} ms")
    print(f"  Min latency: {summary['min_latency_ms']} ms")
    print(f"  Max latency: {summary['max_latency_ms']} ms")
    print(f"  Failures: {summary['failure_count']}")

    write_excel(args.output, summary, rows)


if __name__ == '__main__':
    main()
