import { useEffect, useState } from 'react';
import { getProducts } from '../api/api';
import ProductCard from '../components/ProductCard';

const CATEGORIES = ['All', 'T-Shirts', 'Hoodies', 'Vinyl', 'Posters', 'Accessories'];

export default function Products() {
  const [products, setProducts] = useState([]);
  const [activeCategory, setActiveCategory] = useState('All');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const category = activeCategory === 'All' ? null : activeCategory;
    getProducts(category)
      .then(setProducts)
      .catch(() => setProducts([]))
      .finally(() => setLoading(false));
  }, [activeCategory]);

  return (
    <div className="section">
      <h2 className="section-title">All Merch</h2>

      <div className="category-tabs">
        {CATEGORIES.map(cat => (
          <button
            key={cat}
            className={`tab ${activeCategory === cat ? 'active' : ''}`}
            onClick={() => setActiveCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="loading-text">Loading...</p>
      ) : products.length === 0 ? (
        <p className="loading-text">No products found.</p>
      ) : (
        <div className="product-grid">
          {products.map(p => <ProductCard key={p.id} product={p} />)}
        </div>
      )}
    </div>
  );
}
