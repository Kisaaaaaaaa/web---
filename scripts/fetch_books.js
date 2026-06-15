/**
 * 从 Open Library 公开 API 批量抓取真实图书信息
 * 生成可直接导入 MySQL 的 SQL 文件
 *
 * 使用方法：node scripts/fetch_books.js > sql/seed_books.sql
 */
const https = require('https');

// ==================== 配置 ====================
const OUTPUT_FILE = '../sql/seed_books.sql';

// 分类映射：OpenLibrary subject → 我们数据库的 category_id
const SUBJECT_CATEGORY_MAP = [
  { subject: 'programming',           catId: 7,  cnt: 120 },
  { subject: 'computer_science',      catId: 1,  cnt: 100 },
  { subject: 'databases',             catId: 8,  cnt: 60  },
  { subject: 'artificial_intelligence',catId: 9, cnt: 60  },
  { subject: 'software_engineering',  catId: 7,  cnt: 60  },
  { subject: 'fiction',               catId: 2,  cnt: 150 },
  { subject: 'science_fiction',       catId: 2,  cnt: 80  },
  { subject: 'fantasy',               catId: 2,  cnt: 80  },
  { subject: 'science',               catId: 4,  cnt: 100 },
  { subject: 'mathematics',           catId: 4,  cnt: 60  },
  { subject: 'physics',               catId: 4,  cnt: 60  },
  { subject: 'philosophy',            catId: 5,  cnt: 80  },
  { subject: 'psychology',            catId: 3,  cnt: 60  },
  { subject: 'economics',             catId: 6,  cnt: 80  },
  { subject: 'history',               catId: 3,  cnt: 100 },
  { subject: 'biography',             catId: 3,  cnt: 60  },
  { subject: 'art',                   catId: 3,  cnt: 50  },
];

const PUBLISHERS = [
  '机械工业出版社', '电子工业出版社', '清华大学出版社', '人民邮电出版社',
  '北京大学出版社', '高等教育出版社', '科学出版社', '中信出版社',
  '商务印书馆', '人民出版社', '上海译文出版社', '人民文学出版社',
  'O\'Reilly Media', 'Manning Publications', 'Apress', 'Springer',
  'Cambridge University Press', 'MIT Press', 'Penguin Books', 'HarperCollins',
];

const DESCRIPTIONS = [
  '本书是该领域的经典著作，内容深入浅出，适合初学者和进阶读者。',
  '本书系统性地介绍了相关理论与实践经验，是不可多得的参考书。',
  '作者以生动的笔触和丰富的案例，为读者打开了新世界的大门。',
  '本书涵盖了大量实例和练习，帮助读者在实践中掌握核心概念。',
  '这是一本广泛使用的教材，深受师生好评，内容严谨而全面。',
  '全书条理清晰、论证严密，是相关领域研究者和从业者的必读之作。',
  '本书从基础概念讲起，逐步深入高级主题，适合自学和教学使用。',
  '作者凭借多年实践经验，为读者呈现了一本既有理论高度又接地气的好书。',
];

// ==================== 工具函数 ====================

/** HTTPS GET 请求 */
function httpGet(url) {
  return new Promise((resolve, reject) => {
    https.get(url, { headers: { 'User-Agent': 'CampusBookBorrow/1.0' } }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try { resolve(JSON.parse(data)); }
        catch (e) { resolve(null); }
      });
    }).on('error', () => resolve(null));
  });
}

/** 生成模拟 ISBN */
let isbnCounter = 0;
function genIsbn(catId, idx) {
  isbnCounter++;
  // 格式：978-7-{catId:02d}-{idx:05d}-{check}
  const prefix = '978';
  const group = '7';
  const pub = String(catId).padStart(2, '0');
  const title = String(isbnCounter).padStart(5, '0');
  const raw = prefix + group + pub + title;
  // 简单校验位
  let sum = 0;
  for (let i = 0; i < raw.length; i++) {
    sum += parseInt(raw[i]) * (i % 2 === 0 ? 1 : 3);
  }
  const check = (10 - (sum % 10)) % 10;
  return `${prefix}-${group}-${pub}-${title}-${check}`;
}

/** SQL 字符串转义 */
function escape(str) {
  if (!str) return '';
  return str.replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n/g, '\\n');
}

/** 随机数 */
function rand(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// ==================== 主逻辑 ====================

async function fetchSubject(subject, catId, maxCount) {
  const books = [];
  const limit = Math.min(maxCount, 200);
  const url = `https://openlibrary.org/subjects/${encodeURIComponent(subject)}.json?limit=${limit}`;
  console.error(`  抓取: ${subject} (目标 ${maxCount} 本)...`);

  const data = await httpGet(url);
  if (!data || !data.works) {
    console.error(`  ✗ 失败或无数据`);
    return books;
  }

  for (let i = 0; i < data.works.length && books.length < maxCount; i++) {
    const w = data.works[i];
    if (!w.title || !w.authors || w.authors.length === 0) continue;

    const title = w.title.trim();
    const author = w.authors[0].name.trim();
    const coverId = w.cover_id;
    const year = w.first_publish_year || rand(2000, 2025);
    const publisher = PUBLISHERS[rand(0, PUBLISHERS.length - 1)];
    // 排除部分没有作者名的
    if (author.toLowerCase().includes('unknown') || author.length < 2) continue;

    const totalStock = rand(3, 20);
    const currentStock = rand(0, totalStock);
    const description = DESCRIPTIONS[rand(0, DESCRIPTIONS.length - 1)];
    const status = 1; // 在架

    books.push({
      isbn: genIsbn(catId, i),
      title,
      author,
      publisher,
      publishDate: `${year}-01-01`,
      coverUrl: coverId
        ? `https://covers.openlibrary.org/b/id/${coverId}-M.jpg`
        : '',
      categoryId: catId,
      description,
      totalStock,
      currentStock,
      status,
    });
  }

  console.error(`  ✓ 获得 ${books.length} 本`);
  return books;
}

async function main() {
  console.error('═══════════════════════════════════════');
  console.error('  校园图书借阅系统 - 图书数据抓取工具');
  console.error('  数据来源: Open Library API');
  console.error('═══════════════════════════════════════\n');

  const allBooks = [];
  const seen = new Set();

  for (const item of SUBJECT_CATEGORY_MAP) {
    const books = await fetchSubject(item.subject, item.catId, item.cnt);
    for (const b of books) {
      const key = (b.title + '|' + b.author).toLowerCase();
      if (!seen.has(key)) {
        seen.add(key);
        allBooks.push(b);
      }
    }
    // 礼貌延迟，避免触发限流
    await new Promise(r => setTimeout(r, 500));
  }

  console.error(`\n去重后共计 ${allBooks.length} 本图书\n`);

  // 生成 SQL
  let sql = `-- ============================================================\n`;
  sql += `-- 自动抓取的图书种子数据（来源: Open Library API）\n`;
  sql += `-- 生成时间: ${new Date().toISOString()}\n`;
  sql += `-- 共计: ${allBooks.length} 本\n`;
  sql += `-- ============================================================\n\n`;

  sql += `USE campus_book_borrow;\n\n`;

  for (const b of allBooks) {
    sql += `INSERT INTO book_info (isbn, title, author, publisher, publish_date, cover_url, category_id, description, total_stock, current_stock, status) VALUES\n`;
    sql += `('${b.isbn}', '${escape(b.title)}', '${escape(b.author)}', '${escape(b.publisher)}', '${b.publishDate}', '${b.coverUrl}', ${b.categoryId}, '${escape(b.description)}', ${b.totalStock}, ${b.currentStock}, ${b.status});\n`;
  }

  console.log(sql);
  console.error(`\n✓ SQL 已生成 (${allBooks.length} 条 INSERT)`);
  console.error(`  请执行: node scripts/fetch_books.js > sql/seed_books.sql`);
}

main().catch(e => { console.error('错误:', e.message); process.exit(1); });
