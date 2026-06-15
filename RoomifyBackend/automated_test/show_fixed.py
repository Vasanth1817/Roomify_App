import json
with open('./automated_test/report_full.json') as f:
    data = json.load(f)
passed = sum(1 for item in data if not item.get('finding'))
failed = sum(1 for item in data if item.get('finding'))
print(f'Updated report_full.json:')
print(f'  Total: {len(data)}')
print(f'  Passed: {passed}')
print(f'  Failed: {failed}')
print(f'\nFailed tests:')
for item in data:
    if item.get('finding'):
        print(f"  [{item['case_id']}] {item['endpoint']} - Expected {item['expected_status']}, Got {item['status']}")
