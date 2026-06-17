import os
import subprocess

# Write correct .gitignore
gitignore_path = r"C:\Users\vasan\Downloads\Roomify_Full\AR_APP - Copy\.gitignore"
ignores = [
    "build/",
    ".gradle/",
    "*.so",
    "*.a",
    "*.aar",
    "*.bin",
    ".cxx/",
    "unityLibrary/build/",
    "unityLibrary/.cxx/",
    "unityLibrary/src/main/Il2CppOutputProject/",
    "unityLibrary/src/main/jniLibs/",
    "unityLibrary/symbols/"
]

with open(gitignore_path, 'w', encoding='utf-8') as f:
    for item in ignores:
        f.write(item + '\n')

print("Gitignore written successfully.")
