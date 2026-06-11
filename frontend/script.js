// ======================================================================
// LedgerFlow - Shared JavaScript
// This file is included in every HTML page and provides common utilities:
//   - Session management (login state)
//   - API communication with the backend
//   - Sidebar navigation builder
//   - Toast notifications
//   - Formatting helpers (money, dates, badges)
//   - Pagination rendering
// ======================================================================

// Base URL for all API calls — the Spring Boot backend runs on port 8080
const API = 'http://localhost:8080/api';

// ── Session Management ──────────────────────────────────────
// We store the logged-in user's info in sessionStorage (browser memory).
// It persists until the browser tab is closed.

function saveUser(u) { sessionStorage.setItem('lf_user', JSON.stringify(u)); }
function clearUser() { sessionStorage.removeItem('lf_user'); }
function getUser()   { const s = sessionStorage.getItem('lf_user'); return s ? JSON.parse(s) : null; }
function getRole()   { const u = getUser(); return u ? u.role : null; }

// Called at the top of every page — if no user is logged in,
// we inject a mock admin user for development convenience
function requireAuth() {
  if (!getUser()) { 
    saveUser({ userId: 1, username: 'admin', fullName: 'Test Admin', role: 'ADMIN', email: 'admin@test.com' });
  }
  return true;
}

// Check if the current user has one of the given roles
// Usage: hasRole('ADMIN', 'MANAGER') returns true if user is admin or manager
function hasRole(...roles) {
  const r = getRole();
  return roles.some(x => x.toUpperCase() === r);
}

// ── API Communication ────────────────────────────────────────
// Central function for talking to the backend. All pages use this.
// It automatically sends cookies (for session tracking) and handles errors.
async function api(path, opts = {}) {
  const res = await fetch(API + path, {
    credentials: 'include',    // Send session cookies with every request
    headers: { 'Content-Type': 'application/json', ...opts.headers },
    ...opts
  });
  // If backend says 401, the session expired — redirect to login
  if (res.status === 401) { clearUser(); window.location.href = 'login.html'; throw new Error('Session expired'); }
  const body = res.headers.get('content-type')?.includes('json') ? await res.json() : {};
  if (!res.ok) throw new Error(body.error || 'Request failed (' + res.status + ')');
  return body;
}

// ── Sidebar Navigation ──────────────────────────────────────
// Dynamically builds the left sidebar menu based on the user's role.
// For example, only ADMIN sees the "Users" page, and only ADMIN/MANAGER see "Reports".
function buildSidebar() {
  const user = getUser();
  if (!user) return;

  const current = location.pathname.split('/').pop() || 'dashboard.html';
  const role = user.role;

  // Define which pages each role can access
  const links = [
    { file: 'dashboard.html', icon: '📊', label: 'Dashboard', roles: ['ADMIN','MANAGER','CASHIER','SALES_AGENT'] },
    { file: 'products.html',  icon: '📦', label: 'Products',  roles: ['ADMIN','MANAGER','CASHIER','SALES_AGENT'] },
    { file: 'customers.html', icon: '👥', label: 'Customers', roles: ['ADMIN','MANAGER','CASHIER','SALES_AGENT'] },
    { file: 'orders.html',    icon: '🧾', label: 'Orders',    roles: ['ADMIN','MANAGER','CASHIER','SALES_AGENT'] },
    { file: 'reports.html',   icon: '📈', label: 'Reports',   roles: ['ADMIN','MANAGER'] },
    { file: 'users.html',     icon: '🔑', label: 'Users',     roles: ['ADMIN'] },
  ];

  // Filter links by role and highlight the current page
  const nav = links
    .filter(l => l.roles.includes(role))
    .map(l => `<a href="${l.file}" ${l.file === current ? 'class="active"' : ''}>
        <span class="icon">${l.icon}</span>${l.label}
    </a>`)
    .join('');

  const el = document.getElementById('sidebar');
  if (!el) return;
  el.innerHTML = `
    <div class="sidebar-brand">
      <span class="brand-name">LedgerFlow</span>
      <span class="brand-sub">Sales Management</span>
    </div>
    <div class="sidebar-section">Menu</div>
    <nav>${nav}</nav>
    <div class="sidebar-footer">
      <div class="user-name">${user.fullName}</div>
      <div class="user-role">${role}</div>
      <button class="logout-btn" onclick="logout()">Sign out</button>
    </div>`;
}

// Calls the logout API and redirects to the login page
async function logout() {
  try { await api('/auth/logout', { method: 'POST' }); } catch(_) {}
  clearUser();
  window.location.href = 'login.html';
}

// ── Toast Notifications ──────────────────────────────────────
// Shows a brief pop-up message at the bottom-right corner.
// Types: 'success' (green), 'error' (red), or default (dark)
function toast(msg, type = '') {
  let box = document.getElementById('toasts');
  if (!box) { box = document.createElement('div'); box.id = 'toasts'; document.body.appendChild(box); }
  const t = document.createElement('div');
  t.className = 'toast' + (type ? ' toast-' + type : '');
  t.textContent = msg;
  box.appendChild(t);
  setTimeout(() => t.remove(), 3500);  // Auto-dismiss after 3.5 seconds
}

// ── Formatting Helpers ───────────────────────────────────────

// Format a number as Pakistani Rupees (e.g., "PKR 1,200")
function money(n) {
  if (n == null) return 'PKR 0';
  return 'PKR ' + Number(n).toLocaleString('en-PK', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
}

// Generate a colored badge for order statuses (PENDING = orange, DELIVERED = green, etc.)
function statusBadge(s) {
  return `<span class="badge badge-${(s||'').toLowerCase()}">${s || '—'}</span>`;
}

// Generate a colored badge for user roles
function roleBadge(r) {
  return `<span class="badge badge-${(r||'').toLowerCase().replace(' ','_')}">${r || '—'}</span>`;
}

// Generate a stock level badge — color coded for quick visual scanning
function stockBadge(qty) {
  if (qty <= 0)  return `<span class="badge stock-out">Out of stock</span>`;
  if (qty <= 10) return `<span class="badge stock-low">Low: ${qty}</span>`;
  return             `<span class="badge stock-ok">${qty} in stock</span>`;
}

// Format a date nicely (e.g., "02 May 2026")
function fmtDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('en-GB', { day:'2-digit', month:'short', year:'numeric' });
}

// ── Pagination ───────────────────────────────────────────────
// Renders page navigation buttons (Prev, 1, 2, 3, Next) below tables.
// Works with Spring Boot's Page object which has: number, totalPages, totalElements
function renderPagination(containerId, pageObj, onPageChange) {
  const el = document.getElementById(containerId);
  if (!el || !pageObj) return;
  const { number, totalPages, totalElements } = pageObj;
  if (totalPages <= 1) { el.innerHTML = `<span class="page-info">${totalElements} records</span>`; return; }

  let html = `<span class="page-info">${totalElements} records</span>`;
  html += `<button ${number === 0 ? 'disabled' : ''} onclick="${onPageChange}(${number - 1})">← Prev</button>`;

  // Show up to 5 page buttons centered around the current page
  const start = Math.max(0, number - 2);
  const end   = Math.min(totalPages - 1, number + 2);
  for (let i = start; i <= end; i++) {
    html += `<button onclick="${onPageChange}(${i})" ${i === number ? 'class="current"' : ''}>${i + 1}</button>`;
  }

  html += `<button ${number >= totalPages - 1 ? 'disabled' : ''} onclick="${onPageChange}(${number + 1})">Next →</button>`;
  el.innerHTML = html;
}

// Show an "Access Denied" message for pages the user's role can't view
function accessDeniedMsg(el) {
  if (el) el.innerHTML = `<div class="no-access">
    <div class="icon">🚫</div>
    <h2>Access Denied</h2>
    <p>You don't have permission to view this section.</p>
  </div>`;
}
