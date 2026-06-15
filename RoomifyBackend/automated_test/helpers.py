"""Helpers for RoomifyBackend automated tests."""

import json
import os
import urllib.error
import urllib.parse
import urllib.request
import time

WORKSPACE_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
INPUT_PATHS = [
    os.path.join(WORKSPACE_ROOT, "input.json"),
    os.path.join(os.path.dirname(__file__), "input.json"),
]


def load_input():
    for path in INPUT_PATHS:
        if os.path.isfile(path):
            with open(path, "r", encoding="utf-8") as handle:
                return json.load(handle), path
    raise FileNotFoundError(
        "input.json not found. Create one at RoomifyBackend/input.json or RoomifyBackend/automated_test/input.json."
    )


def normalize_url(base_url, path):
    return urllib.parse.urljoin(base_url.rstrip("/") + "/", path.lstrip("/"))


def request_json(method, url, headers=None, body=None, timeout=10):
    headers = headers.copy() if headers else {}
    if body is not None and "Content-Type" not in headers:
        headers["Content-Type"] = "application/json"
    data = None
    if body is not None:
        data = json.dumps(body).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    start = time.time()
    try:
        with urllib.request.urlopen(req, timeout=timeout) as response:
            content = response.read().decode("utf-8", errors="replace")
            return {
                "status": response.getcode(),
                "body": content,
                "elapsed_ms": int((time.time() - start) * 1000),
                "headers": dict(response.getheaders()),
            }
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        return {
            "status": exc.code,
            "body": body,
            "elapsed_ms": int((time.time() - start) * 1000),
            "headers": dict(exc.headers.items()),
        }
    except urllib.error.URLError as exc:
        return {
            "status": None,
            "error": str(exc),
            "elapsed_ms": int((time.time() - start) * 1000),
            "headers": {},
        }


def safe_json_dumps(value):
    try:
        return json.dumps(value, indent=2)
    except (TypeError, ValueError):
        return str(value)
