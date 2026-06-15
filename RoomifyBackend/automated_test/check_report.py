import json

with open('report_full.json', 'r') as f:
    data = json.load(f)

total = len(data)
passing = sum(1 for t in data if not t.get('finding'))
failing = sum(1 for t in data if t.get('finding'))

print(f"Total: {total}")
print(f"Passing: {passing}")
print(f"Failing: {failing}")

# Show which tests are failing
print("\nFailing tests:")
for t in data:
    if t.get('finding'):
        print(f"  [{t['case_id']}] {t['endpoint']} -> {t['status']} (expected {t['expected_status']})")
