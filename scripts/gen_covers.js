/**
 * 从 seed_books.sql 解析图书信息，为每本书生成本地 SVG 封面
 * 使用: node scripts/gen_books.js 1> sql/seed_books.sql 2>/dev/null && node scripts/gen_covers.js
 */
const fs = require('fs');
const path = require('path');

const COVERS_DIR = path.join(__dirname, '..', 'frontend', 'public', 'covers');
const SQL_FILE = path.join(__dirname, '..', 'sql', 'seed_books.sql');

const COLORS = {
  '1': '#2563EB', '7': '#4F46E5', '8': '#0D9488', '9': '#E11D48',
  '2': '#DC2626', '4': '#059669', '5': '#D97706', '6': '#0891B2', '3': '#7C3AED',
};
const CAT_LABEL = {
  '1':'计算机科学','7':'编程语言','8':'数据库','9':'人工智能',
  '2':'文学小说','4':'自然科学','5':'哲学宗教','6':'经济管理','3':'社会科学',
};

function escXml(s) { return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }

function darker(hex, d) {
  const n = parseInt(hex.slice(1), 16);
  const r = Math.max(0,Math.min(255,((n>>16)&0xFF)+d));
  const g = Math.max(0,Math.min(255,((n>>8)&0xFF)+d));
  const b = Math.max(0,Math.min(255,(n&0xFF)+d));
  return '#'+((r<<16)|(g<<8)|b).toString(16).padStart(6,'0');
}

function wrapLines(text, max) {
  if (text.length <= max) return [text];
  const lines = [];
  let r = text;
  while (r.length > max) { lines.push(r.slice(0, max)); r = r.slice(max); }
  if (r) lines.push(r);
  return lines.slice(0, 3);
}

function makeSvg(title, author, catId) {
  const bg = COLORS[catId] || '#6B7280';
  const label = CAT_LABEL[catId] || '';
  const authorShort = author.length > 14 ? author.slice(0,14)+'…' : author;
  const lines = wrapLines(title, 9);

  const lh = 42;
  const sy = 220 - (lines.length-1)*(lh/2);
  let texts = '';
  lines.forEach((l,i) => {
    texts += `<text x="200" y="${sy + i*lh}" class="title">${escXml(l)}</text>\n`;
  });

  return `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="560" viewBox="0 0 400 560">
  <defs>
    <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" style="stop-color:${bg}"/>
      <stop offset="100%" style="stop-color:${darker(bg,-35)}"/>
    </linearGradient>
  </defs>
  <rect width="400" height="560" fill="url(#g)" rx="6"/>
  <rect x="22" y="22" width="356" height="516" rx="3" fill="none" stroke="rgba(255,255,255,0.12)" stroke-width="2"/>
  <rect x="22" y="52" width="100" height="24" rx="12" fill="rgba(255,255,255,0.13)"/>
  <text x="72" y="69" fill="rgba(255,255,255,0.85)" font-size="12" text-anchor="middle" font-family="sans-serif">${label}</text>
  ${texts}
  <text x="200" y="330" fill="rgba(255,255,255,0.65)" font-size="15" text-anchor="middle" font-family="sans-serif">${escXml(authorShort)} 著</text>
  <text x="200" y="520" fill="rgba(255,255,255,0.18)" font-size="48" text-anchor="middle">📚</text>
  <style>
    .title{fill:#fff;font-size:26px;font-weight:700;font-family:'PingFang SC','Microsoft YaHei','Noto Sans SC',sans-serif;text-anchor:middle;letter-spacing:1px}
  </style>
</svg>`;
}

function main() {
  if (!fs.existsSync(COVERS_DIR)) fs.mkdirSync(COVERS_DIR, { recursive: true });

  const sql = fs.readFileSync(SQL_FILE, 'utf-8');
  // 正则匹配 INSERT 中的关键字段：cover_url是第6个, isbn第1个, title第2个, author第3个, category_id第7个
  const re = /INSERT INTO book_info \(.+\) VALUES \('([^']*)', '([^']*)', '([^']*)', '[^']*', '[^']*', '([^']*)', (\d+),/g;
  let m, count = 0;

  while ((m = re.exec(sql)) !== null) {
    const coverUrl = m[4];
    const title = m[2];
    const author = m[3];
    const catId = m[5];
    const fileName = path.basename(coverUrl);
    const svg = makeSvg(title, author, catId);
    fs.writeFileSync(path.join(COVERS_DIR, fileName), svg, 'utf-8');
    count++;
  }

  console.log(`✓ 生成了 ${count} 个 SVG 封面文件 → ${COVERS_DIR}`);
}

main();
