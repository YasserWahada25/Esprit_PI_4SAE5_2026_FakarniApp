import requests

BASE_URL = "http://localhost:5000"

# Test 1 — Health
print("=== Test Health ===")
r = requests.get(f"{BASE_URL}/health")
print(r.json())

# Test 2 — Prédiction avec une image
print("\n=== Test Prédiction ===")
with open("test_image.jpg", "rb") as f:
    r = requests.post(
        f"{BASE_URL}/api/v1/predict",
        files={"image": f}
    )
print(r.json())