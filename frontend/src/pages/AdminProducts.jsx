import { useEffect, useState } from 'react';
import {
  getProducts, createProduct, updateProduct, deleteProduct,
  addVariant, updateVariant, deleteVariant,
} from '../api/api';
import { VARIANT_SIZES } from '../utils/size';

const EMPTY_PRODUCT = { name: '', description: '', price: '', category: 'T-Shirts', imageUrl: '' };

export default function AdminProducts() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_PRODUCT);
  const [error, setError] = useState('');

  // Variant management for one selected product.
  const [variantProductId, setVariantProductId] = useState(null);
  const [newVariant, setNewVariant] = useState({ size: 'M', stockQuantity: '' });
  const [variantError, setVariantError] = useState('');

  useEffect(() => { loadProducts(); }, []);

  async function loadProducts() {
    setLoading(true);
    try { setProducts(await getProducts()); } catch { setProducts([]); }
    setLoading(false);
  }

  // Re-derived from products each render so it stays fresh after a reload.
  const variantProduct = products.find(p => p.id === variantProductId) || null;

  function resetForm() {
    setForm(EMPTY_PRODUCT);
    setEditing(null);
    setError('');
  }

  function startEdit(product) {
    setEditing(product.id);
    setForm({
      name: product.name,
      description: product.description || '',
      price: product.price,
      category: product.category,
      imageUrl: product.imageUrl || '',
    });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    const payload = { ...form, price: parseFloat(form.price) };
    try {
      if (editing) {
        await updateProduct(editing, payload);
      } else {
        await createProduct(payload);
      }
      resetForm();
      loadProducts();
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete(id) {
    if (!confirm('Delete this product?')) return;
    try {
      await deleteProduct(id);
      if (variantProductId === id) setVariantProductId(null);
      loadProducts();
    } catch (err) {
      alert(err.message);
    }
  }

  function toggleVariants(productId) {
    setVariantError('');
    setNewVariant({ size: 'M', stockQuantity: '' });
    setVariantProductId(prev => (prev === productId ? null : productId));
  }

  async function handleAddVariant(e) {
    e.preventDefault();
    setVariantError('');
    try {
      await addVariant(variantProductId, { size: newVariant.size, stockQuantity: parseInt(newVariant.stockQuantity) });
      setNewVariant({ size: 'M', stockQuantity: '' });
      await loadProducts();
    } catch (err) {
      setVariantError(err.message);
    }
  }

  async function handleSaveVariant(variant, stockQuantity) {
    setVariantError('');
    try {
      await updateVariant(variant.id, { size: variant.size, stockQuantity: parseInt(stockQuantity) });
      await loadProducts();
    } catch (err) {
      setVariantError(err.message);
    }
  }

  async function handleDeleteVariant(variantId) {
    setVariantError('');
    try {
      await deleteVariant(variantId);
      await loadProducts();
    } catch (err) {
      setVariantError(err.message);
    }
  }

  return (
    <>
      {/* Product Form */}
      <div className="admin-form-card">
        <h3>{editing ? 'Edit Product' : 'Add New Product'}</h3>
        {error && <p className="error-msg">{error}</p>}
        <form className="admin-form" onSubmit={handleSubmit}>
          <input placeholder="Name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
          <textarea placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} rows={2} />
          <div className="admin-form-row">
            <input type="number" step="0.01" placeholder="Price" value={form.price} onChange={e => setForm({ ...form, price: e.target.value })} required />
            <select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })}>
              <option>T-Shirts</option>
              <option>Hats</option>
            </select>
          </div>
          <input placeholder="Image URL" value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} />
          <div className="admin-form-actions">
            <button className="btn btn-primary" type="submit">{editing ? 'Update' : 'Add Product'}</button>
            {editing && <button className="btn btn-outline" type="button" onClick={resetForm}>Cancel</button>}
          </div>
        </form>
        <p className="admin-hint" style={{ opacity: 0.7, fontSize: '0.85em', marginTop: 8 }}>
          Sizes &amp; stock are managed per product via the <strong>Variants</strong> button below.
        </p>
      </div>

      {/* Product Table */}
      {loading ? <p className="loading-text">Loading...</p> : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Category</th>
                <th>Price</th>
                <th>Sizes</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id}>
                  <td>{p.id}</td>
                  <td>{p.name}</td>
                  <td>{p.category}</td>
                  <td>${p.price?.toFixed(2)}</td>
                  <td>{p.variants?.length ? p.variants.map(v => v.size).join(', ') : '—'}</td>
                  <td>
                    <button className="btn btn-sm btn-outline" onClick={() => startEdit(p)}>Edit</button>
                    <button className="btn btn-sm btn-outline" onClick={() => toggleVariants(p.id)}>
                      {variantProductId === p.id ? 'Hide' : 'Variants'}
                    </button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Variant management panel */}
      {variantProduct && (
        <div className="admin-form-card" style={{ marginTop: '1.5rem' }}>
          <h3>Variants — {variantProduct.name}</h3>
          {variantError && <p className="error-msg">{variantError}</p>}

          <table className="admin-table">
            <thead>
              <tr>
                <th>Size</th>
                <th>Stock</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {(variantProduct.variants || []).map(v => (
                <VariantRow
                  key={v.id}
                  variant={v}
                  onSave={stock => handleSaveVariant(v, stock)}
                  onDelete={() => handleDeleteVariant(v.id)}
                />
              ))}
              {(variantProduct.variants || []).length === 0 && (
                <tr><td colSpan={3} style={{ opacity: 0.7 }}>No variants yet — add a size below.</td></tr>
              )}
            </tbody>
          </table>

          <form className="admin-form-row" onSubmit={handleAddVariant} style={{ marginTop: '1rem' }}>
            <select value={newVariant.size} onChange={e => setNewVariant({ ...newVariant, size: e.target.value })}>
              {VARIANT_SIZES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
            <input
              type="number"
              min="0"
              placeholder="Stock"
              value={newVariant.stockQuantity}
              onChange={e => setNewVariant({ ...newVariant, stockQuantity: e.target.value })}
              required
            />
            <button className="btn btn-primary" type="submit">Add Variant</button>
          </form>
        </div>
      )}
    </>
  );
}

// One editable variant row: stock is locally editable and saved on demand.
function VariantRow({ variant, onSave, onDelete }) {
  const [stock, setStock] = useState(variant.stockQuantity);
  const dirty = String(stock) !== String(variant.stockQuantity);

  return (
    <tr>
      <td>{variant.size}</td>
      <td>
        <input type="number" min="0" value={stock} onChange={e => setStock(e.target.value)} style={{ width: 90 }} />
      </td>
      <td>
        <button className="btn btn-sm btn-primary" type="button" disabled={!dirty} onClick={() => onSave(stock)}>Save</button>
        <button className="btn btn-sm btn-danger" type="button" onClick={onDelete}>Remove</button>
      </td>
    </tr>
  );
}
