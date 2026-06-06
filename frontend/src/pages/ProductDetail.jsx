import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProduct } from '../api/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { isOneSize } from '../utils/size';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { user } = useAuth();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [selectedVariantId, setSelectedVariantId] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    getProduct(id)
      .then(p => {
        setProduct(p);
        // Auto-select when there's only one variant (e.g. a one-size item).
        if (p?.variants?.length === 1) setSelectedVariantId(p.variants[0].id);
      })
      .catch(() => setProduct(null))
      .finally(() => setLoading(false));
  }, [id]);

  const variants = product?.variants || [];
  const selectedVariant = variants.find(v => v.id === selectedVariantId) || null;
  // Hide the size selector for single one-size variants ("Adjustable").
  const oneSize = variants.length === 1 && isOneSize(variants[0].size);
  // Cap quantity at the selected variant's stock (no limit until a size is picked).
  const maxQuantity = selectedVariant?.stockQuantity ?? Infinity;

  // Switching sizes can lower the available stock, so clamp the quantity down to fit.
  function selectVariant(variant) {
    setSelectedVariantId(variant.id);
    setQuantity(q => Math.min(q, variant.stockQuantity));
  }

  async function handleAddToCart() {
    if (!user) {
      navigate('/login');
      return;
    }
    if (!selectedVariant) return;
    setAdding(true);
    try {
      await addToCart(selectedVariant.id, quantity);
      alert('Added to cart!');
    } catch (err) {
      alert(err.message);
    } finally {
      setAdding(false);
    }
  }

  function addToCartLabel() {
    if (adding) return 'Adding...';
    if (variants.length === 0) return 'Out of Stock';
    if (!selectedVariant) return 'Select a Size';
    if (selectedVariant.stockQuantity === 0) return 'Out of Stock';
    return 'Add to Cart';
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

          {variants.length === 0 ? (
            <p className="product-stock">Out of stock</p>
          ) : oneSize ? null : (
            <div className="size-selector">
              <p className="size-selector-label">Size:</p>
              <div className="size-options">
                {variants.map(v => (
                  <button
                    key={v.id}
                    type="button"
                    className={`size-option ${selectedVariantId === v.id ? 'active' : ''}`}
                    disabled={v.stockQuantity === 0}
                    onClick={() => selectVariant(v)}
                  >
                    {v.size}{v.stockQuantity === 0 ? ' (Sold out)' : ''}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="quantity-selector">
            <p className="quantity-selector-label">Quantity:</p>
            <div className="quantity-controls">
              <button
                type="button"
                className="quantity-btn"
                onClick={() => setQuantity(q => Math.max(1, q - 1))}
                disabled={quantity <= 1}
              >
                −
              </button>
              <span className="quantity-value">{quantity}</span>
              <button
                type="button"
                className="quantity-btn"
                onClick={() => setQuantity(q => Math.min(maxQuantity, q + 1))}
                disabled={quantity >= maxQuantity}
              >
                +
              </button>
            </div>
          </div>

          <button
            className="btn btn-primary btn-lg"
            onClick={handleAddToCart}
            disabled={adding || !selectedVariant || selectedVariant.stockQuantity === 0}
          >
            {addToCartLabel()}
          </button>
          <button className="btn btn-outline" style={{ marginLeft: '1rem' }} onClick={() => navigate('/products')}>
            Back to Shop
          </button>
        </div>
      </div>
    </div>
  );
}
