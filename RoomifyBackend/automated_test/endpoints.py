"""API endpoint definitions discovered from RoomifyBackend/main.py."""

ENDPOINTS = [
    {
        "path": "/",
        "method": "GET",
        "description": "API root health check",
        "expected_access": "public",
    },
    {
        "path": "/furniture",
        "method": "GET",
        "description": "List all furniture items",
        "expected_access": "public",
    },
    {
        "path": "/furniture",
        "method": "POST",
        "description": "Add a new furniture item",
        "expected_access": "public",
        "sample_body": {
            "name": "Test Chair",
            "price": "19.99",
            "model_url": "https://example.com/model.glb",
            "thumbnail_url": "https://example.com/thumb.png",
            "category": "chairs",
        },
    },
    {
        "path": "/save_layout",
        "method": "POST",
        "description": "Save a room layout from Unity",
        "expected_access": "public",
        "sample_body": {
            "items": [],
            "user_id": "test-user",
            "room_name": "Test Room",
            "mode": "AR",
        },
    },
    {
        "path": "/get_layouts",
        "method": "GET",
        "description": "Fetch saved layouts for a user or all layouts",
        "expected_access": "public",
    },
    {
        "path": "/delete_layout/{layout_id}",
        "method": "DELETE",
        "description": "Delete a saved layout by ID",
        "expected_access": "public",
        "path_params": {"layout_id": 1},
    },
    {
        "path": "/api/register",
        "method": "POST",
        "description": "Register a new user",
        "expected_access": "public",
        "sample_body": {
            "full_name": "Test User",
            "phone_number": "0000000000",
            "email": "autotest+{}@example.com",
            "password": "TestPass123!",
        },
    },
    {
        "path": "/api/budget",
        "method": "POST",
        "description": "Update user budget settings",
        "expected_access": "public",
        "sample_body": {
            "user_id": "test-user",
            "max_budget": 1000.0,
        },
    },
    {
        "path": "/api/budget",
        "method": "GET",
        "description": "Get budget settings for a user",
        "expected_access": "public",
    },
    {
        "path": "/api/users",
        "method": "GET",
        "description": "List all registered users",
        "expected_access": "public",
    },
    {
        "path": "/api/login",
        "method": "POST",
        "description": "Authenticate a user",
        "expected_access": "public",
        "sample_body": {
            "email": "autotest@example.com",
            "password": "invalid-password",
        },
    },
    {
        "path": "/api/migrate",
        "method": "GET",
        "description": "Run database migration SQL",
        "expected_access": "public",
    },
]
