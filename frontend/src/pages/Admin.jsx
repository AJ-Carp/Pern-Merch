import { useEffect, useState } from 'react';
import { getProducts, createProduct, updateProduct, deleteProduct } from '../api/api';

export default function Admin() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: '', description: '', price: '', category: 'T-Shirts', size: 'M', imageUrl: '', stockQuantity: '' });
  const [error, setError] = useState('');

  useEffect(() => { loadProducts(); }, []);

  async function loadProducts() {
    setLoading(true);
    try { setProducts(await getProducts()); } catch { setProducts([]); }
    setLoading(false);
  }

  function resetForm() {
    setForm({ name: '', description: '', price: '', category: 'T-Shirts', size: 'M', imageUrl: '', stockQuantity: '' });
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
      size: product.size || '',
      imageUrl: product.imageUrl || '',
      stockQuantity: product.stockQuantity,
    });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    const payload = { ...form, price: parseFloat(form.price), stockQuantity: parseInt(form.stockQuantity) };

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
      loadProducts();
    } catch (err) {
      alert(err.message);
    }
  }

  return (
    <div className="section">
      <h2 className="section-title">Admin Panel</h2>

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
              <option>Hoodies</option>
              <option>Vinyl</option>
              <option>Posters</option>
              <option>Accessories</option>
            </select>
            <input placeholder="Size" value={form.size} onChange={e => setForm({ ...form, size: e.target.value })} />
          </div>
          <input placeholder="Image URL" value={form.imageUrl} onChange={e => setForm({ ...form, imageUrl: e.target.value })} />
          <input type="number" placeholder="Stock Quantity" value={form.stockQuantity} onChange={e => setForm({ ...form, stockQuantity: e.target.value })} required />
          <div className="admin-form-actions">
            <button className="btn btn-primary" type="submit">{editing ? 'Update' : 'Add Product'}</button>
            {editing && <button className="btn btn-outline" type="button" onClick={resetForm}>Cancel</button>}
          </div>
        </form>
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
                <th>Stock</th>
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
                  <td>{p.stockQuantity}</td>
                  <td>
                    <button className="btn btn-sm btn-outline" onClick={() => startEdit(p)}>Edit</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
