import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProduct } from '../api/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { user } = useAuth();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    getProduct(id)
      .then(setProduct)
      .catch(() => setProduct(null))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleAddToCart() {
    if (!user) {
      navigate('/login');
      return;
    }
    setAdding(true);
    try {
      await addToCart(product.id);
      alert('Added to cart!');
    } catch (err) {
      alert(err.message);
    } finally {
      setAdding(false);
    }
  }

  if (loading) return <p className="loading-text">Loading...</p>;
  if (!product) return <p className="loading-text">Product not found.</p>;

  return (
    <div className="section">
      <div className="product-detail">
        <div className="product-detail-image">
          <img src={product.imageUrl || 'https://via.placeholder.com/400x400/1a1a2e/e94560?text=PERN'} alt={product.name} />
        </div>
        <div className="product-detail-info">
          <span className="product-category-badge">{product.category}</span>
          <h1>{product.name}</h1>
          <p className="product-price-large">${product.price?.toFixed(2)}</p>
          <p className="product-description">{product.description}</p>
          {product.size && product.size !== 'ONE_SIZE' && (
            <p className="product-size">Size: <strong>{product.size}</strong></p>
          )}
          <p className="product-stock">
            {product.stockQuantity > 0 ? `${product.stockQuantity} in stock` : 'Out of stock'}
          </p>
          <button
            className="btn btn-primary btn-lg"
            onClick={handleAddToCart}
            disabled={adding || product.stockQuantity === 0}
          >
            {adding ? 'Adding...' : 'Add to Cart'}
          </button>
          <button className="btn btn-outline" style={{ marginLeft: '1rem' }} onClick={() => navigate('/products')}>
            Back to Shop
          </button>
        </div>
      </div>
    </div>
  );
}
