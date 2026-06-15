import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const root = path.resolve(__dirname, '..');
const yamlPath = path.join(root, 'dependencies.yml');
const packagePath = path.join(root, 'package.json');

function parseSimpleYaml(yamlText) {
  const result = {};
  let currentKey = null;

  for (const rawLine of yamlText.split(/\r?\n/)) {
    const line = rawLine.replace(/\t/g, '  ');
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;

    const sectionMatch = trimmed.match(/^([A-Za-z0-9_-]+):\s*$/);
    if (sectionMatch) {
      currentKey = sectionMatch[1];
      result[currentKey] = {};
      continue;
    }

    if (currentKey && /^\s+/.test(line)) {
      const entryMatch = line.match(/^\s+(["']?[^"']+["']?):\s*(["']?[^"']+["']?)\s*$/);
      if (entryMatch) {
        const key = entryMatch[1].replace(/^['"]|['"]$/g, '');
        const value = entryMatch[2].replace(/^['"]|['"]$/g, '');
        result[currentKey][key] = value;
      }
    }
  }

  return result;
}

const yamlContent = fs.readFileSync(yamlPath, 'utf8');
const parsed = parseSimpleYaml(yamlContent);
const packageJson = JSON.parse(fs.readFileSync(packagePath, 'utf8'));
packageJson.dependencies = parsed.dependencies || {};
packageJson.devDependencies = parsed.devDependencies || {};
fs.writeFileSync(packagePath, JSON.stringify(packageJson, null, 2) + '\n', 'utf8');
console.log('Synced dependencies from dependencies.yml into package.json');
