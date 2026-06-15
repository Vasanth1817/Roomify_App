import json
with open('./automated_test/report_corrected.json') as f:
    data = json.load(f)
passed = sum(1 for item in data if not item['finding'])
failed = sum(1 for item in data if item['finding'])
print(f'Total tests: {len(data)}')
print(f'Passed (matching expected): {passed}')
print(f'Failed (not matching expected): {failed}')
print(f'\n=== Actual findings ===')
for item in data:
    if item['finding']:
        print(f"[{item['case_id']}] {item['name']}")
        print(f"    Expected: {item['expected_status']}, Got: {item['status']}")
        if item['note']:
            print(f"    Response: {item['note'][:120]}")
        print()
