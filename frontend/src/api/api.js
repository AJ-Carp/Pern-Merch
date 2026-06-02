//const BASE_URL = 'https://pe-caa7334a460c428f9116b67c92a1eda8.ecs.us-east-1.on.aws/api';
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

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

// ---- Variants (admin) ----

export async function addVariant(productId, variant) {
  const res = await fetch(`${BASE_URL}/products/variants/${productId}`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(variant),
  });
  return handleResponse(res);
}

export async function updateVariant(variantId, variant) {
  const res = await fetch(`${BASE_URL}/products/variants/${variantId}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(variant),
  });
  return handleResponse(res);
}

export async function deleteVariant(variantId) {
  const res = await fetch(`${BASE_URL}/products/variants/${variantId}`, {
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

export async function addToCart(productVariantId, quantity = 1) {
  const res = await fetch(`${BASE_URL}/cart`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify({ productVariantId, quantity }),
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

export async function createPaymentIntent() {
  const res = await fetch(`${BASE_URL}/orders/checkout`, {
    method: 'POST',
    headers: authHeaders(),
  });
  return handleResponse(res);
}

export async function cancelPendingCheckout() {
  const res = await fetch(`${BASE_URL}/orders/cancel`, {
    method: 'POST',
    headers: authHeaders(),
  });
  if (res.status === 409) {
    const err = new Error('Payment already completed');
    err.alreadyPaid = true;
    throw err;
  }
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || `HTTP ${res.status}`);
  }
  return null;
}

export async function getOrders() {
  const res = await fetch(`${BASE_URL}/orders`, { headers: authHeaders() });
  return handleResponse(res);
}

// ---- User ----

export async function getDefaultAddress() {
  const res = await fetch(`${BASE_URL}/users/me/address`, { headers: authHeaders() });
  return handleResponse(res);
}
