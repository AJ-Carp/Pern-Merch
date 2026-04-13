const BASE_URL = '/api';

function getToken() {
  return localStorage.getItem('token');
}

function authHeaders() {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } : { 'Content-Type': 'application/json' };
}

async function handleResponse(res) {
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `HTTP ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

// ---- Auth ----

export async function register(username, email, password) {
  const res = await fetch(`${BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, email, password }),
  });
  return handleResponse(res);
}

export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  return handleResponse(res);
}

// ---- Products ----

export async function getProducts(category) {
  const url = category ? `${BASE_URL}/products?category=${encodeURIComponent(category)}` : `${BASE_URL}/products`;
  const res = await fetch(url);
  return handleResponse(res);
}

export async function getProduct(id) {
  const res = await fetch(`${BASE_URL}/products/${id}`);
  return handleResponse(res);
}

export async function createProduct(product) {
  const res = await fetch(`${BASE_URL}/products`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(product),
  });
  return handleResponse(res);
}

export async function updateProduct(id, product) {
  const res = await fetch(`${BASE_URL}/products/${id}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(product),
  });
  return handleResponse(res);
}

export async function deleteProduct(id) {
  const res = await fetch(`${BASE_URL}/products/${id}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  return handleResponse(res);
}

// ---- Cart ----

export async function getCart() {
  const res = await fetch(`${BASE_URL}/cart`, { headers: authHeaders() });
  return handleResponse(res);
}

export async function addToCart(productId, quantity = 1) {
  const res = await fetch(`${BASE_URL}/cart`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ productId, quantity }),
  });
  return handleResponse(res);
}

export async function updateCartItem(cartItemId, quantity) {
  const res = await fetch(`${BASE_URL}/cart/${cartItemId}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify({ quantity }),
  });
  return handleResponse(res);
}

export async function removeCartItem(cartItemId) {
  const res = await fetch(`${BASE_URL}/cart/${cartItemId}`, {
    method: 'DELETE',
    headers: authHeaders(),
  });
  return handleResponse(res);
}

// ---- Orders ----

export async function checkout() {
  const res = await fetch(`${BASE_URL}/orders/checkout`, {
    method: 'POST',
    headers: authHeaders(),
  });
  return handleResponse(res);
}

export async function getOrders() {
  const res = await fetch(`${BASE_URL}/orders`, { headers: authHeaders() });
  return handleResponse(res);
}
