import re
from datetime import datetime

with open('DEV_NOTES.txt', 'r', encoding='utf-8') as f:
    text = f.read()

# Split the file by the "====..." header blocks
# A header block is exactly 80 '=' followed by a line with title, followed by 80 '='
header_pattern = re.compile(r'={80}\n\s+(.+?)\n={80}\n', re.MULTILINE)

parts = header_pattern.split(text)

intro = parts[0]
sections = []
for i in range(1, len(parts), 2):
    title = parts[i].strip()
    content = parts[i+1]
    sections.append({"title": title, "content": content})

# Categories of sections
keep_sections = []
records_text = ""

for sec in sections:
    t = sec['title']
    c = sec['content']
    
    if "更新" in t:
        # Extract date from title, e.g., "2026-03-10 更新"
        date_match = re.search(r'\d{4}-\d{2}-\d{2}', t)
        date_str = date_match.group(0) if date_match else "1970-01-01"
        
        # Add the date prefix if not present
        content = c.strip()
        # If it already has 【YYYY-MM-DD】, we don't need to add it, but these don't.
        # Let's format it.
        # Let's check the first line of content
        first_line = content.split('\n')[0]
        if first_line.startswith("【"):
            # e.g., 【新功能】周期食物 (Cycle Food)
            # Replace with 【YYYY-MM-DD】新功能：...
            # Actually, just prepend the date block
            content = f"【{date_str}】系统更新\n{content}\n\n"
        else:
            content = f"【{date_str}】系统更新\n{content}\n\n"
            
        records_text += content
        
    elif t == "修复记录":
        records_text += c + "\n\n"
    else:
        keep_sections.append(sec)

# Now we parse records_text into individual records
# A record starts with 【YYYY-MM-DD】
# We will use re.split
record_pattern = re.compile(r'(?=^【\d{4}-\d{2}-\d{2}】)', re.MULTILINE)
raw_records = record_pattern.split(records_text)

records = []
for r in raw_records:
    r = r.strip()
    if not r:
        continue
    # Extract date
    date_match = re.match(r'^【(\d{4}-\d{2}-\d{2})】', r)
    if date_match:
        date_str = date_match.group(1)
        records.append({'date': date_str, 'text': r})
    else:
        # Append to the previous record if any
        if records:
            records[-1]['text'] += "\n\n" + r

# Sort records by date descending
records.sort(key=lambda x: x['date'], reverse=True)

# Reassemble the file
# 1. Intro
out_text = intro

# 2. Keep sections
for sec in keep_sections:
    out_text += "=" * 80 + "\n"
    # Center the title roughly
    padding = (80 - len(sec['title'])) // 2
    out_text += " " * padding + sec['title'] + "\n"
    out_text += "=" * 80 + "\n"
    out_text += sec['content']

# 3. The unified "修复记录" section
out_text += "=" * 80 + "\n"
out_text += " " * 33 + "修复记录\n"
out_text += "=" * 80 + "\n\n"

for r in records:
    out_text += r['text'] + "\n\n"

# Write back
with open('DEV_NOTES.txt', 'w', encoding='utf-8') as f:
    f.write(out_text)

print("DEV_NOTES.txt has been reorganized and sorted successfully.")
